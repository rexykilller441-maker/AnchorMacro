package com.anchormacro;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

public class AnchorMacroClient implements ClientModInitializer {
    public static MinecraftClient mc;

    @Override
    public void onInitializeClient() {
        mc = MinecraftClient.getInstance();

        // register keybinds
        AnchorMacroKeybinds.register();

        // ensure config is loaded
        AnchorMacroConfig.get();

        // tick listener for key actions
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.currentScreen == null) { // only respond when not typing in chat/gui
                if (AnchorMacroKeybinds.executeKey.wasPressed()) {
                    if (client.player != null) {
                        AnchorMacroExecutor.execute(client);
                    }
                }

                if (AnchorMacroKeybinds.openConfigKey.wasPressed()) {
                    openConfigScreen();
                }
            } else {
                // allow opening config even when a screen is open? currently restricted
            }
        });

        log("Anchor Macro initialized!");
    }

    private void openConfigScreen() {
        if (mc == null) mc = MinecraftClient.getInstance();
        Screen screen = new AnchorMacroConfigScreen(mc.currentScreen);
        mc.setScreen(screen);
    }

    public static void log(String s) {
        System.out.println("[AnchorMacro] " + s);
    }
}
