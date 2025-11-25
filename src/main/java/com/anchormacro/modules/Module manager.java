package com.anchormacro.modules;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages all modules (AutoTotem, Hitboxes, etc.)
 */
public class ModuleManager {
    private static final List<Module> modules = new ArrayList<>();

    static {
        // Register your modules here
        // Example: modules.add(new AutoTotemModule());
        // Example: modules.add(new HitboxesModule());
    }

    public static void tickAll() {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onTick();
            }
        }
    }

    public static void renderAll(MatrixStack matrices, VertexConsumerProvider provider) {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onRender(matrices, provider);
            }
        }
    }

    public static List<Module> getModules() {
        return modules;
    }

    public static void registerModule(Module module) {
        modules.add(module);
    }
}
