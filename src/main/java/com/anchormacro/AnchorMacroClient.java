package com.anchormacro;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class AnchorMacroClient implements ClientModInitializer {
    
    private static KeyBinding anchorMacroKey;
    
    @Override
    public void onInitializeClient() {
        anchorMacroKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.anchormacro.execute",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_GRAVE_ACCENT,
            "category.anchormacro.main"
        ));
        
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (anchorMacroKey.wasPressed()) {
                if (client.player != null) {
                    AnchorMacroExecutor.execute(client);
                }
            }
        });
        
        System.out.println("Anchor Macro mod initialized!");
    }
}