package com.anchormacro.modules;

import com.anchormacro.modules.combat.AutoTotem;
import com.anchormacro.modules.combat.Hitboxes;
import com.anchormacro.modules.combat.TotemHit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Central manager for modules. Call tick() each client tick, render() each render pass,
 * and forward onAttack() whenever a player attacks an entity.
 */
public class ModuleManager {
    private static final List<Module> modules = new ArrayList<>();
    public static final AutoTotem autoTotem = new AutoTotem();
    public static final TotemHit totemHit = new TotemHit();
    public static final Hitboxes hitboxes = new Hitboxes();

    static {
        // register modules in the order you want them listed in the GUI
        modules.add(autoTotem);
        modules.add(totemHit);
        modules.add(hitboxes);
    }

    public static List<Module> getModules() {
        return modules;
    }

    public static void tick(MinecraftClient mc) {
        for (Module m : modules) {
            if (m.isEnabled()) {
                try { m.tick(mc); } catch (Throwable ignored) {}
            }
        }
    }

    public static void render(MatrixStack matrices) {
        // render modules (Hitboxes does world render)
        for (Module m : modules) {
            if (m.isEnabled()) {
                try { m.render(matrices); } catch (Throwable ignored) {}
            }
        }
    }

    public static void onAttack(Entity target) {
        for (Module m : modules) {
            if (m.isEnabled()) {
                try { m.onAttack(target); } catch (Throwable ignored) {}
            }
        }
    }
}
