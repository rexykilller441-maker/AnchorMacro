package com.anchormacro;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Executor with configurable delays/slots and safe-mode support.
 */
public class AnchorMacroExecutor {
    private static int step = 0;
    private static int tickCounter = 0;
    private static final AtomicBoolean running = new AtomicBoolean(false);

    public static void execute(MinecraftClient client) {
        if (running.get()) {
            // already running, ignore
            return;
        }

        ClientPlayerEntity player = client.player;
        if (player == null) return;

        AnchorMacroConfig cfg = AnchorMacroConfig.get();

        // Validate slots
        if (!hasItemInHotbar(player, cfg.anchorSlot, Items.RESPAWN_ANCHOR)) {
            if (cfg.showNotifications) player.sendMessage(net.minecraft.text.Text.literal("§cNo respawn anchor in configured slot!"), false);
            return;
        }
        if (!hasItemInHotbar(player, cfg.glowstoneSlot, Items.GLOWSTONE)) {
            if (cfg.showNotifications) player.sendMessage(net.minecraft.text.Text.literal("§cNo glowstone in configured slot!"), false);
            return;
        }
        if (!hasItemInHotbar(player, cfg.totemSlot, Items.TOTEM_OF_UNDYING) && cfg.explodeOnlyIfTotemPresent) {
            if (cfg.showNotifications) player.sendMessage(net.minecraft.text.Text.literal("§cNo totem in configured slot — explosion aborted."), false);
            return;
        }

        step = 1;
        tickCounter = 0;
        running.set(true);

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
                // delay wait
                switch (step) {
                    case 1:
                        // place anchor
                        if (tickCounter >= cfg.delayPlaceAnchor) {
                            selectSlot(p, cfg.anchorSlot);
                            placeBlock(mc, p);
                            step = 2;
                            tickCounter = 0;
                        }
                        break;
                    case 2:
                        // switch to glowstone
                        if (tickCounter >= cfg.delaySwitchToGlowstone) {
                            selectSlot(p, cfg.glowstoneSlot);
                            step = 3;
                            tickCounter = 0;
                        }
                        break;
                    case 3:
                        // charge anchor
                        if (tickCounter >= cfg.delayChargeAnchor) {
                            rightClick(mc, p);
                            step = 4;
                            tickCounter = 0;
                        }
                        break;
                    case 4:
                        // safe anchor mode: optionally place glowstone in front of player
                        if (cfg.safeAnchorMode) {
                            if (tickCounter >= 1) { // short wait to let charge finish
                                placeGlowstoneInFrontIfPossible(mc, p, cfg.glowstoneSlot);
                                step = 5;
                                tickCounter = 0;
                            }
                        } else {
                            // switch to totem
                            if (tickCounter >= cfg.delaySwitchToTotem) {
                                selectSlot(p, cfg.totemSlot);
                                step = 5;
                                tickCounter = 0;
                            }
                        }
                        break;
                    case 5:
                        // switch to totem if we didn't earlier
                        if (!cfg.safeAnchorMode) {
                            // already switched in previous case
                        } else {
                            if (tickCounter >= cfg.delaySwitchToTotem) {
                                selectSlot(p, cfg.totemSlot);
                                tickCounter = 0;
                            }
                        }
                        // proceed to explosion after configured delay
                        if (tickCounter >= cfg.delayExplodeAnchor) {
                            // if explodeOnlyIfTotemPresent, double check
                            boolean hasTotem = hasItemInHotbar(p, cfg.totemSlot, Items.TOTEM_OF_UNDYING) || p.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING;
                            if (cfg.explodeOnlyIfTotemPresent && !hasTotem) {
                                if (cfg.showNotifications) p.sendMessage(net.minecraft.text.Text.literal("§cTotem missing — skipped explosion."), false);
                                running.set(false);
                                unregisterThis();
                                return;
                            }
                            rightClick(mc, p); // attempt to trigger explosion
                            running.set(false);
                            unregisterThis();
                        }
                        break;
                    default:
                        running.set(false);
                        unregisterThis();
                        break;
                }
            }

            // Fabric API provides no direct unregister handle here; this is a lightweight approach:
            private void unregisterThis() {
                // no-op; allowing the lambda to exit will effectively stop behavior because running=false
            }
        });
    }

    // helper: select hotbar slot (0..8)
    private static void selectSlot(ClientPlayerEntity player, int slot) {
        if (slot < 0 || slot > 8) return;
        player.getInventory().selectedSlot = slot;
    }

    private static boolean hasItemInHotbar(ClientPlayerEntity player, int slot, net.minecraft.item.Item target) {
        if (slot < 0 || slot > 8) return false;
        ItemStack st = player.getInventory().getStack(slot);
        return st != null && st.getItem() == target;
    }

    private static void placeBlock(MinecraftClient client, ClientPlayerEntity player) {
        if (client.interactionManager == null) return;
        // place using a BlockHitResult aimed at the block the player is looking at, or at feet
        Vec3d start = player.getEyePos();
        Vec3d look = player.getRotationVec(1.0F);
        Vec3d end = start.add(look.multiply(5.0));
        BlockHitResult hit = client.world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));
        if (hit == null) {
            // fallback: place at player's feet
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
            // try using item in air (swing/use)
            client.interactionManager.interactItem(player, Hand.MAIN_HAND);
        }
    }

    private static void placeGlowstoneInFrontIfPossible(MinecraftClient client, ClientPlayerEntity player, int glowstoneSlot) {
        // attempt to place glowstone one block ahead of player's facing
        Vec3d eye = player.getEyePos();
        Vec3d look = player.getRotationVec(1.0F);
        Vec3d targetPos = player.getPos().add(look.x, 0, look.z).normalize().multiply(1.5).add(player.getPos());
        BlockPos pos = BlockPos.ofFloored(targetPos.add(0, -1, 0));
        // Check if block is replaceable
        if (client.world.getBlockState(pos).canPlaceAt(client.world, pos)) {
            // switch to glowstone slot, place, then restore selected slot
            int previous = player.getInventory().selectedSlot;
            selectSlot(player, glowstoneSlot);
            // Build a BlockHitResult pointing at pos
            BlockHitResult bhr = new BlockHitResult(player.getPos(), Direction.UP, pos, false);
            client.interactionManager.interactBlock(player, Hand.MAIN_HAND, bhr);
            selectSlot(player, previous);
        }
    }
}
