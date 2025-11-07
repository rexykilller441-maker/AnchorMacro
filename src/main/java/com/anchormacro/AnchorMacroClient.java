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

public class AnchorMacroClient implements ClientModInitializer {
    private static boolean prevLeft = false;
    public static MinecraftClient mc;

    @Override
    public void onInitializeClient() {
        mc = MinecraftClient.getInstance();
        AnchorMacroClient.log("[AnchorMacro] Client initialized");

        // register tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client == null || client.player == null) return;

            // tick modules
            ModuleManager.tickAll(client);

            // left-click detection & expanded hitbox handling
            long window = client.getWindow().getHandle();
            boolean leftDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

            if (leftDown && !prevLeft) {
                Entity target = null;

                // try expanded hitbox first if enabled
                if (ModuleManager.hitboxes != null && ModuleManager.hitboxes.isEnabled()) {
                    target = ModuleManager.hitboxes.expandedRaycastTarget(ModuleManager.hitboxes.distance);
                }

                // fallback to vanilla crosshair entity
                if (target == null) {
                    HitResult hr = client.crosshairTarget;
                    if (hr != null && hr.getType() == HitResult.Type.ENTITY) {
                        target = ((EntityHitResult) hr).getEntity();
                    }
                }

                if (target != null) {
                    // If TotemHit enabled, let it handle switching + attack packet
                    if (ModuleManager.totemHit != null && ModuleManager.totemHit.isEnabled()) {
                        ModuleManager.totemHit.onAttack(target);
                    } else {
                        // send vanilla attack packet
                        try {
                            if (client.getNetworkHandler() != null) {
                                client.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(target, client.player.isSneaking()));
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }

            prevLeft = leftDown;
        });

        // nothing to do for render stage here; modules can render themselves via ModuleManager.render if needed
    }

    public static void log(String s) {
        System.out.println(s);
    }
}
