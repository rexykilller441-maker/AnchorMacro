package com.anchormacro.modules;

import com.anchormacro.modules.combat.*;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class ModuleManager {
    public static void tickAll() {
        AutoTotem.tick();
    }

    public static void handleAttack(Entity target) {
        TotemHit.onAttack(target);
    }

    public static void renderAll(MatrixStack matrices, VertexConsumerProvider provider) {
        Hitboxes.onRender(matrices, provider);
    }
}
