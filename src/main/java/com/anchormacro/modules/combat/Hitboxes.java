package com.anchormacro.modules.combat;

import com.anchormacro.AnchorMacroConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.client.render.Camera;

/**
 * Expands visible hitboxes for all entities.
 */
public class Hitboxes {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void onRender(MatrixStack matrices, VertexConsumerProvider provider) {
        if (!AnchorMacroConfig.get().hitboxEnabled || mc.world == null || mc.player == null) return;

        Camera camera = mc.gameRenderer.getCamera();
        double camX = camera.getPos().x;
        double camY = camera.getPos().y;
        double camZ = camera.getPos().z;

        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof PlayerEntity)) continue;
            if (e.squaredDistanceTo(mc.player) > AnchorMacroConfig.get().hitboxDistance * AnchorMacroConfig.get().hitboxDistance)
                continue;

            Box box = e.getBoundingBox().expand(AnchorMacroConfig.get().hitboxExpand);
            DebugRenderer.drawBox(matrices, provider, box.offset(-camX, -camY, -camZ), 1f, 0f, 0f, 0.5f);
        }
    }
}
