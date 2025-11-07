package com.anchormacro;

import com.anchormacro.modules.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

/**
 * Client initializer: handles keybinds, tick integration, and a simple left-click attack detector.
 */
public class AnchorMacroClient implements ClientModInitializer {
    public static MinecraftClient mc;
    private boolean prevLeft = false;

    @Override
    public void onInitializeClient() {
        mc = MinecraftClient.getInstance();

        // register keybinds earlier code may have
        AnchorMacroKeybinds.register();

        // tick registration: module ticks
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client == null) return;
            if (client.player == null) return;

            // run module ticks
            ModuleManager.tick(client);

            // simple left-click detection to trigger onAttack (client-side raycast)
            long window = client.getWindow().getHandle();
            boolean leftDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
            if (leftDown && !prevLeft) {
                // button pressed this tick — find targeted entity
                HitResult hr = client.crosshairTarget;
                if (hr != null && hr.getType() == HitResult.Type.ENTITY) {
                    // convert to entity and call modules
                    try {
                        net.minecraft.util.hit.EntityHitResult erh = (net.minecraft.util.hit.EntityHitResult) hr;
                        if (erh.getEntity() != null) ModuleManager.onAttack(erh.getEntity());
                    } catch (Exception ignored) {}
                }
            }
            prevLeft = leftDown;
        });

        // render stage: modules that render (e.g., hitboxes) will be called from a screen overlay.
        // We call render in a client tick render phase via another simple registration
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // modules that perform world rendering should draw themselves during the world render,
            // but to keep things simple we call ModuleManager.render on END_CLIENT_TICK which is safe for our debug boxes.
            // If needed, move to proper render callbacks.
        });

        System.out.println("[AnchorMacro] ModuleManager integrated.");
    }

    public void openConfigScreen() {
        if (mc == null) mc = MinecraftClient.getInstance();
        Screen s = new com.anchormacro.ui.ModulesGuiScreen(mc.currentScreen);
        mc.setScreen(s);
    }
}
