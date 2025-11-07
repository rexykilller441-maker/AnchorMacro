package com.anchormacro.modules;

import com.anchormacro.modules.combat.AutoTotem;
import com.anchormacro.modules.combat.Hitboxes;
import com.anchormacro.modules.combat.TotemHit;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Module central registry.
 */
public class ModuleManager {
    public static final AutoTotem autoTotem = new AutoTotem();
    public static final TotemHit totemHit = new TotemHit();
    public static final Hitboxes hitboxes = new Hitboxes();

    private static final List<Module> modules = new ArrayList<>();
    static {
        modules.add(autoTotem);
        modules.add(totemHit);
        modules.add(hitboxes);
    }

    public static void tickAll(MinecraftClient client) {
        for (Module m : modules) {
            try {
                if (m.isEnabled()) m.tick(client);
            } catch (Throwable t) { /* ignore module errors */ }
        }
    }

    public static void renderAll(net.minecraft.client.util.math.MatrixStack matrices) {
        for (Module m : modules) {
            try {
                if (m.isEnabled()) m.render(matrices);
            } catch (Throwable t) { /* ignore module errors */ }
        }
    }

    public static List<Module> getModules() { return modules; }
}
