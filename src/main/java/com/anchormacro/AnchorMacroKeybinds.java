package com.anchormacro;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class AnchorMacroKeybinds {
    public static KeyBinding executeKey;
    public static KeyBinding openConfigKey;
    public static KeyBinding testKey;

    public static void register() {
        executeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.anchormacro.execute",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_GRAVE_ACCENT,
                "category.anchormacro.main"
        ));

        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.anchormacro.open_settings",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "category.anchormacro.main"
        ));

        // testKey default comes from config; register a default now (will be overridden on load)
        testKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.anchormacro.test",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.anchormacro.main"
        ));
    }
}
