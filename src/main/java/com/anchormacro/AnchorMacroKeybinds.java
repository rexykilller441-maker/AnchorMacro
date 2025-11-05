package com.anchormacro;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Central keybind registration
 */
public class AnchorMacroKeybinds {
    public static KeyBinding executeKey;
    public static KeyBinding openConfigKey;

    public static void register() {
        // keep original execute key as grave accent default
        executeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.anchormacro.execute",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_GRAVE_ACCENT,
                "category.anchormacro.main"
        ));

        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.anchormacro.open_settings",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O, // default 'O' to open settings
                "category.anchormacro.main"
        ));
    }
}
