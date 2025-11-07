package com.anchormacro;

import com.anchormacro.modules.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

/**
 * AnchorMacroClient — main mod entry point.
 * Handles ticking and input for AnchorMacro + modules (AutoTotem, TotemHit, Hitboxes)
 */
public class AnchorMacroClient implements ClientModInitializer {
    private static boolean prevLeft = false;

    @Override
    public void onInitializeClient() {
        System.out.println("[AnchorMacro] Client initialized ✅");

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            // === Tick modules ===
            ModuleManager.tickAll();

            // === Handle left-click attacks ===
            handleAttackInput(client);
        });
    }

    private void handleAttackInput(MinecraftClient client) {
        long window = client.getWindow().getHandle();
        boolean leftDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

        // detect edge (pressed now, not previous)
        if (leftDown && !prevLeft) {
            Entity target = null;

            // Try hitbox-expanded raycast if enabled
            if (ModuleManager.hitboxes != null && ModuleManager.hitboxes.isEnabled()) {
                target = ModuleManager.hitboxes.expandedRaycastTarget(ModuleManager.hitboxes.distance);
            }

            // fallback to vanilla crosshair
            if (target == null && client.crosshairTarget instanceof EntityHitResult ehr) {
                target = ehr.getEntity();
            }

            if (target != null) {
                // If TotemHit enabled, handle with sword-knockback logic
                if (ModuleManager.totemHit.isEnabled()) {
                    ModuleManager.onAttack(target);
                } else {
                    // send normal attack packet
                    try {
                        if (client.getNetworkHandler() != null) {
                            client.getNetworkHandler().sendPacket(
                                    PlayerInteractEntityC2SPacket.attack(target, client.player.isSneaking())
                            );
                        }
                    } catch (Exception ignored) {}
                }
            }
        }

        prevLeft = leftDown;
    }
}
