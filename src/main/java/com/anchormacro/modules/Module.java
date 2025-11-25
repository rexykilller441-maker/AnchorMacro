package com.anchormacro.modules;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Base class for all modules
 */
public abstract class Module {
    private final String name;
    private boolean enabled;

    public Module(String name) {
        this.name = name;
        this.enabled = false;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    // Override these methods in your module implementations
    public void onTick() {}
    public void onRender(MatrixStack matrices, VertexConsumerProvider provider) {}
    public void onEnable() {}
    public void onDisable() {}
}
