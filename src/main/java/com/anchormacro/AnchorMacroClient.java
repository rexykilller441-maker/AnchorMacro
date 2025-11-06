package com.anchormacro;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;

public class AnchorMacroClient implements ClientModInitializer {
    public static MinecraftClient mc;

    @Override
    public void onInitializeClient() {
        mc = MinecraftClient.getInstance();

        // Register keybinds
        AnchorMacroKeybinds.register();

        // Load config
        AnchorMacroConfig cfg = AnchorMacroConfig.get();

        // Tick handler (runs every client tick)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client == null) return;
            if (client.player == null) return;

            // === Open Config Screen ===
            if (AnchorMacroKeybinds.openConfigKey.wasPressed()) {
                openConfigScreen();
            }

            // === Execute Macro ===
            if (client.currentScreen == null && AnchorMacroKeybinds.executeKey.wasPressed()) {
                AnchorMacroExecutor.execute(client);
            }

            // === Testing Mode Key ===
            int testKeyCode = AnchorMacroConfig.get().testKey;
            long window = client.getWindow().getHandle();
            if (GLFW.glfwGetKey(window, testKeyCode) == GLFW.GLFW_PRESS) {
                // Request a test — runs once per press
                AnchorMacroTestRunner.requestTest();
            }

            // === Run Test if Requested ===
            AnchorMacroTestRunner.tickIfRequested();
        });

        log("Anchor Macro initialized successfully!");
    }

    public void openConfigScreen() {
        if (mc == null) mc = MinecraftClient.getInstance();
        Screen s = new AnchorMacroConfigScreen(mc.currentScreen);
        mc.setScreen(s);
    }

    public static void log(String s) {
        System.out.println("[AnchorMacro] " + s);
    }
}
