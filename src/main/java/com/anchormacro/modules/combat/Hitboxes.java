package com.anchormacro.modules.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class Hitboxes {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public boolean expandPlayers = true;
    public boolean expandCrystals = true;
    public boolean render = true;
    public float expand = 0.5f;
    public float distance = 10f;

    public void onRender(MatrixStack matrices) {
        if (!render || mc.world == null || mc.player == null) return;

        Vec3d camPos = mc.gameRenderer.getCamera().getPos();
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (entity instanceof PlayerEntity && !expandPlayers) continue;
            if (entity instanceof EndCrystalEntity && !expandCrystals) continue;
            if (entity.squaredDistanceTo(mc.player) > distance * distance) continue;

            Box box = entity.getBoundingBox().expand(expand);
            DebugRenderer.drawBox(
                box.offset(-camPos.x, -camPos.y, -camPos.z),
                1f, 0f, 0f, 0.5f
            );
        }
    }
}
