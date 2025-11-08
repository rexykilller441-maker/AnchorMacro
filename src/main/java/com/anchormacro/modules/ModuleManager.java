package com.anchormacro.modules;

import com.anchormacro.modules.combat.AutoTotem;
import com.anchormacro.modules.combat.Hitboxes;
import com.anchormacro.modules.combat.TotemHit;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Central registry + helpers for modules.
 */
public class ModuleManager {
    public static final AutoTotem autoTotem = new AutoTotem();
    public static final TotemHit totemHit = new TotemHit();
    public static final Hitboxes hitboxes = new Hitboxes();

    private static final List<Module> modules = new ArrayList<>();

    static {
        modules.add(autoTotem);
        modules.add(totemHit);
        // Hitboxes implements Module so it's safe to add
        modules.add(hitboxes);
    }

    public static List<Module> getModules() {
        return modules;
    }

    /** Return list of module names (for the GUI) */
    public static List<String> getAllModuleNames() {
        return modules.stream().map(Module::getName).collect(Collectors.toList());
    }

    /** Toggle module by name */
    public static void toggle(String moduleName) {
        for (Module m : modules) {
            if (m.getName().equalsIgnoreCase(moduleName)) {
                m.setEnabled(!m.isEnabled());
                return;
            }
        }
    }

    public static Module getByName(String name) {
        for (Module m : modules) if (m.getName().equalsIgnoreCase(name)) return m;
        return null;
    }

    /** Tick all enabled modules (call from client tick) */
    public static void tickAll(MinecraftClient client) {
        for (Module m : modules) {
            try {
                if (m.isEnabled()) m.tick(client);
            } catch (Throwable t) { /* silent: protect client */ }
        }
    }
}
