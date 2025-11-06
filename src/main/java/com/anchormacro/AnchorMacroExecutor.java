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
 * Executor honours config. Uses hotbar-only auto-search (1..9 -> 0..8).
 */
public class AnchorMacroExecutor {
    private static final AtomicBoolean running = new AtomicBoolean(false);
    private static int step = 0;
    private static int tickCounter = 0;

    public static void execute(MinecraftClient client) {
        if (running.get()) return; // already running

        ClientPlayerEntity player = client.player;
        if (player == null) return;

        AnchorMacroConfig cfg = AnchorMacroConfig.get();

        // Resolve internal slots
        int anchorSlot = AnchorMacroConfig.guiToInternalSlot(cfg.anchorSlot);
        int glowstoneSlot = AnchorMacroConfig.guiToInternalSlot(cfg.glowstoneSlot);
        int totemSlot = AnchorMacroConfig.guiToInternalSlot(cfg.totemSlot);

        // Helper: attempt to find item in hotbar if auto-search enabled
        if (cfg.autoSearchHotbar) {
            anchorSlot = findHotbarSlotFor(player, Items.RESPAWN_ANCHOR, anchorSlot);
            glowstoneSlot = findHotbarSlotFor(player, Items.GLOWSTONE, glowstoneSlot);
            totemSlot = findHotbarSlotFor(player, Items.TOTEM_OF_UNDYING, totemSlot);
        }

        // Validate presence according to explode-only-if-totemPresent
        if (!player.getInventory().getStack(anchorSlot).isOf(Items.RESPAWN_ANCHOR)) {
            if (cfg.showNotifications) player.sendMessage(net.minecraft.text.Text.literal("§cAnchor not found in hotbar!"), false);
            return;
        }
        if (!player.getInventory().getStack(glowstoneSlot).isOf(Items.GLOWSTONE)) {
            if (cfg.showNotifications) player.sendMessage(net.minecraft.text.Text.literal("§cGlowstone not found in hotbar!"), false);
            return;
        }
        if (cfg.explodeOnlyIfTotemPresent) {
            boolean totemPresent = player.getInventory().getStack(totemSlot).isOf(Items.TOTEM_OF_UNDYING) || player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING);
            if (!totemPresent) {
                if (cfg.showNotifications) player.sendMessage(net.minecraft.text.Text.literal("§cTotem not found in hotbar/offhand — aborting explosion."), false);
                return;
            }
        }

        // Start sequence
        running.set(true);
        step = 1;
        tickCounter = 0;

        ClientTickEvents.END_CLIENT_TICK.register(new ClientTickEvents.EndTick() {
            @Override
            public void onEndTick(MinecraftClient mc) {
                if (!running.get()) return;
                ClientPlayerEntity p = mc.player;
                if (p == null) {
                    running.set(false);
                    return;
                }
                tickCounter++;

                switch (step) {
                    case 1:
                        if (tickCounter >= cfg.delayPlaceAnchor) {
                            selectSlot(p, anchorSlot);
                            placeBlock(mc, p);
                            step = 2;
                            tickCounter = 0;
                        }
                        break;
                    case 2:
                        if (tickCounter >= cfg.delaySwitchToGlowstone) {
                            selectSlot(p, glowstoneSlot);
                            step = 3;
                            tickCounter = 0;
                        }
                        break;
                    case 3:
                        if (tickCounter >= cfg.delayChargeAnchor) {
                            rightClick(mc, p);
                            step = 4;
                            tickCounter = 0;
                        }
                        break;
                    case 4:
                        if (cfg.safeAnchorMode) {
                            if (tickCounter >= 1) {
                                placeGlowstoneInFrontIfPossible(mc, p, glowstoneSlot);
                                step = 5;
                                tickCounter = 0;
                            }
                        } else {
                            if (tickCounter >= cfg.delaySwitchToTotem) {
                                selectSlot(p, AnchorMacroConfig.guiToInternalSlot(cfg.totemSlot));
                                step = 5;
                                tickCounter = 0;
                            }
                        }
                        break;
                    case 5:
                        // ensure totem selected
                        if (tickCounter >= cfg.delaySwitchToTotem) {
                            selectSlot(p, AnchorMacroConfig.guiToInternalSlot(cfg.totemSlot));
                            tickCounter = 0;
                        }
                        if (tickCounter >= cfg.delayExplodeAnchor) {
                            boolean hasTotem = p.getInventory().getStack(AnchorMacroConfig.guiToInternalSlot(cfg.totemSlot)).isOf(Items.TOTEM_OF_UNDYING)
                                    || p.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING);
                            if (cfg.explodeOnlyIfTotemPresent && !hasTotem) {
                                if (cfg.showNotifications) p.sendMessage(net.minecraft.text.Text.literal("§cTotem missing — skipped explosion."), false);
                                running.set(false);
                                return;
                            }
                            rightClick(mc, p); // attempt the explosion
                            running.set(false);
                        }
                        break;
                    default:
                        running.set(false);
                        break;
                }
            }
        });
    }

    // find hotbar slot containing item, prefer preferredSlot; returns internal 0..8 slot
    private static int findHotbarSlotFor(ClientPlayerEntity player, net.minecraft.item.Item item, int preferredSlot) {
        // if preferred slot already has it, return it
        ItemStack pref = player.getInventory().getStack(preferredSlot);
        if (pref != null && pref.getItem() == item) return preferredSlot;
        // search hotbar 0..8
        for (int i = 0; i < 9; i++) {
            ItemStack s = player.getInventory().getStack(i);
            if (s != null && s.getItem() == item) return i;
        }
        // not found — return preferredSlot (may be wrong but caller handles notification)
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
        BlockHitResult hit = client.world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));
        if (hit == null) {
            hit = new BlockHitResult(player.getPos(), Direction.UP, BlockPos.ofFloored(player.getPos().add(0, -1, 0)), false);
        }
        client.interactionManager.interactBlock(player, Hand.MAIN_HAND, hit);
    }

    private static void rightClick(MinecraftClient client, ClientPlayerEntity player) {
        if (client.interactionManager == null) return;
        Vec3d start = player.getEyePos();
        Vec3d look = player.getRotationVec(1.0F);
        Vec3d end = start.add(look.multiply(5.0));
        BlockHitResult hit = client.world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));
        if (hit != null) {
            client.interactionManager.interactBlock(player, Hand.MAIN_HAND, hit);
        } else {
            client.interactionManager.interactItem(player, Hand.MAIN_HAND);
        }
    }

    private static void placeGlowstoneInFrontIfPossible(MinecraftClient client, ClientPlayerEntity player, int glowstoneSlot) {
        Vec3d look = player.getRotationVec(1.0F);
        Vec3d pos = player.getPos().add(look.x, 0, look.z).normalize().multiply(1.5).add(player.getPos());
        BlockPos bp = BlockPos.ofFloored(pos.add(0, -1, 0));
        try {
            if (client.world.getBlockState(bp).getMaterial().isReplaceable()) {
                int prev = player.getInventory().selectedSlot;
                selectSlot(player, glowstoneSlot);
                BlockHitResult bhr = new BlockHitResult(player.getPos(), Direction.UP, bp, false);
                client.interactionManager.interactBlock(player, Hand.MAIN_HAND, bhr);
                selectSlot(player, prev);
            }
        } catch (Exception ignored) {
            // fallback: do nothing
        }
    }
}
