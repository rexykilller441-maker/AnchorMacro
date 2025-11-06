package com.anchormacro;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handles the full automation sequence.
 * Supports auto-search (hotbar only) and safe anchor mode.
 * Automatically unregisters its tick listener at end to prevent freeze.
 */
public class AnchorMacroExecutor {
    private static final AtomicBoolean running = new AtomicBoolean(false);
    private static ClientTickEvents.EndTick handlerRef;
    private static int step = 0;
    private static int tickCounter = 0;

    public static void execute(MinecraftClient client) {
        if (running.get()) return;
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        final AnchorMacroConfig cfg = AnchorMacroConfig.get();

        int aSlot = AnchorMacroConfig.guiToInternalSlot(cfg.anchorSlot);
        int gSlot = AnchorMacroConfig.guiToInternalSlot(cfg.glowstoneSlot);
        int tSlot = AnchorMacroConfig.guiToInternalSlot(cfg.totemSlot);

        if (cfg.autoSearchHotbar) {
            aSlot = findHotbarSlotFor(player, Items.RESPAWN_ANCHOR, aSlot);
            gSlot = findHotbarSlotFor(player, Items.GLOWSTONE, gSlot);
            tSlot = findHotbarSlotFor(player, Items.TOTEM_OF_UNDYING, tSlot);
        }

        final int anchorSlot = aSlot;
        final int glowstoneSlot = gSlot;
        final int totemSlot = tSlot;

        if (!player.getInventory().getStack(anchorSlot).isOf(Items.RESPAWN_ANCHOR)) {
            if (cfg.showNotifications)
                player.sendMessage(net.minecraft.text.Text.literal("§cAnchor not found in hotbar!"), false);
            return;
        }
        if (!player.getInventory().getStack(glowstoneSlot).isOf(Items.GLOWSTONE)) {
            if (cfg.showNotifications)
                player.sendMessage(net.minecraft.text.Text.literal("§cGlowstone not found in hotbar!"), false);
            return;
        }
        if (cfg.explodeOnlyIfTotemPresent) {
            boolean hasTotem = player.getInventory().getStack(totemSlot).isOf(Items.TOTEM_OF_UNDYING)
                    || player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING);
            if (!hasTotem) {
                if (cfg.showNotifications)
                    player.sendMessage(net.minecraft.text.Text.literal("§cTotem not found — aborting explosion."), false);
                return;
            }
        }

        running.set(true);
        step = 1;
        tickCounter = 0;

        // remove previous listener safely
        if (handlerRef != null) ClientTickEvents.END_CLIENT_TICK.unregister(handlerRef);

        handlerRef = mc -> {
            if (!running.get()) return;
            ClientPlayerEntity p = mc.player;
            if (p == null) {
                stop();
                return;
            }
            tickCounter++;

            try {
                switch (step) {
                    case 1 -> {
                        if (tickCounter >= cfg.delayPlaceAnchor) {
                            selectSlot(p, anchorSlot);
                            placeBlock(mc, p);
                            step = 2;
                            tickCounter = 0;
                        }
                    }
                    case 2 -> {
                        if (tickCounter >= cfg.delaySwitchToGlowstone) {
                            selectSlot(p, glowstoneSlot);
                            step = 3;
                            tickCounter = 0;
                        }
                    }
                    case 3 -> {
                        if (tickCounter >= cfg.delayChargeAnchor) {
                            rightClick(mc, p);
                            step = 4;
                            tickCounter = 0;
                        }
                    }
                    case 4 -> {
                        if (cfg.safeAnchorMode) {
                            if (tickCounter >= 1) {
                                placeGlowstoneInFrontIfPossible(mc, p, glowstoneSlot);
                                step = 5;
                                tickCounter = 0;
                            }
                        } else if (tickCounter >= cfg.delaySwitchToTotem) {
                            selectSlot(p, AnchorMacroConfig.guiToInternalSlot(cfg.totemSlot));
                            step = 5;
                            tickCounter = 0;
                        }
                    }
                    case 5 -> {
                        if (tickCounter >= cfg.delayExplodeAnchor) {
                            boolean hasTotem = p.getInventory().getStack(AnchorMacroConfig.guiToInternalSlot(cfg.totemSlot)).isOf(Items.TOTEM_OF_UNDYING)
                                    || p.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING);
                            if (cfg.explodeOnlyIfTotemPresent && !hasTotem) {
                                if (cfg.showNotifications)
                                    p.sendMessage(net.minecraft.text.Text.literal("§cTotem missing — skipped explosion."), false);
                                stop();
                                return;
                            }
                            rightClick(mc, p);
                            stop();
                        }
                    }
                }
            } catch (Exception ignored) {
                stop();
            }
        };

        // register and keep handle
        ClientTickEvents.END_CLIENT_TICK.register(handlerRef);
    }

    private static void stop() {
        running.set(false);
        handlerRef = null;
    }

    private static int findHotbarSlotFor(ClientPlayerEntity player, net.minecraft.item.Item item, int preferredSlot) {
        ItemStack pref = player.getInventory().getStack(preferredSlot);
        if (pref != null && pref.getItem() == item) return preferredSlot;
        for (int i = 0; i < 9; i++) {
            ItemStack s = player.getInventory().getStack(i);
            if (s != null && s.getItem() == item) return i;
        }
        return preferredSlot;
    }

    private static void selectSlot(ClientPlayerEntity player, int slot) {
        if (slot < 0 || slot > 8) return;
        player.getInventory().selectedSlot = slot;
    }

    private static void placeBlock(MinecraftClient client, ClientPlayerEntity player) {
        if (client.interactionManager == null) return;
        Vec3d start = player.getEyePos();
        Vec3d look = player.getRotationVec(1.0F);
        Vec3d end = start.add(look.multiply(5.0));
        BlockHitResult hit = client.world.raycast(new RaycastContext(start, end,
                RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));
        if (hit == null)
            hit = new BlockHitResult(player.getPos(), Direction.UP, BlockPos.ofFloored(player.getPos().add(0, -1, 0)), false);
        client.interactionManager.interactBlock(player, Hand.MAIN_HAND, hit);
    }

    private static void rightClick(MinecraftClient client, ClientPlayerEntity player) {
        if (client.interactionManager == null) return;
        Vec3d start = player.getEyePos();
        Vec3d look = player.getRotationVec(1.0F);
        Vec3d end = start.add(look.multiply(5.0));
        BlockHitResult hit = client.world.raycast(new RaycastContext(start, end,
                RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));
        if (hit != null) client.interactionManager.interactBlock(player, Hand.MAIN_HAND, hit);
        else client.interactionManager.interactItem(player, Hand.MAIN_HAND);
    }

    private static void placeGlowstoneInFrontIfPossible(MinecraftClient client, ClientPlayerEntity player, int glowstoneSlot) {
        Vec3d look = player.getRotationVec(1.0F);
        Vec3d pos = player.getPos().add(look.x, 0, look.z).normalize().multiply(1.5).add(player.getPos());
        BlockPos bp = BlockPos.ofFloored(pos.add(0, -1, 0));
        try {
            if (client.world.getBlockState(bp).isReplaceable()) {
                int prev = player.getInventory().selectedSlot;
                selectSlot(player, glowstoneSlot);
                BlockHitResult bhr = new BlockHitResult(player.getPos(), Direction.UP, bp, false);
                client.interactionManager.interactBlock(player, Hand.MAIN_HAND, bhr);
                selectSlot(player, prev);
            }
        } catch (Exception ignored) {}
    }
}
