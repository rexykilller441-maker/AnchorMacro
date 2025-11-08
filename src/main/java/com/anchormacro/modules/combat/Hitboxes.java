package com.anchormacro.modules.combat;

import com.anchormacro.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Functional Hitboxes module:
 * - Expands entity bounding boxes for expanded hit detection (not only visual).
 * - Also can render debug boxes if desired.
 */
public class Hitboxes implements Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private boolean enabled = true;

    // settings (public so GUI code can access)
    public float expand = 0.5f;
    public float distance = 6.0f;
    public boolean renderBoxes = false;
    public boolean expandPlayers = true;
    public boolean expandCreatures = true;

    @Override
    public String getName() { return "Hitbox"; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean e) { enabled = e; }

    @Override
    public void tick(MinecraftClient client) {
        // nothing per tick for now
    }

    @Override
    public void render(MatrixStack matrices) {
        if (!renderBoxes || mc.world == null || mc.player == null) return;

        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        VertexConsumerProvider.Immediate provider = mc.getBufferBuilders().getEntityVertexConsumers();
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (e instanceof PlayerEntity && !expandPlayers) continue;
            if (!(e instanceof LivingEntity) && !expandCreatures) continue;
            if (e.squaredDistanceTo(mc.player) > distance * distance) continue;

            Box box = e.getBoundingBox().expand(expand);
            // draw box offset by camera pos
            DebugRenderer.drawBox(matrices, provider, box.offset(-cam.x, -cam.y, -cam.z), 1f, 0f, 0f, 0.45f);
        }
        // flush provider to ensure boxes render
        provider.draw();
    }

    /**
     * Perform an expanded raycast by checking entities whose expanded bounding box intersects the eye->end ray.
     * Return the nearest hit entity or null.
     */
    public Entity expandedRaycastTarget(double maxDist) {
        if (mc.player == null || mc.world == null) return null;

        Vec3d eye = mc.player.getEyePos();
        Vec3d look = mc.player.getRotationVector().multiply(maxDist);
        Vec3d end = eye.add(look);

        List<Entity> candidates = mc.world.getOtherEntities(mc.player,
                mc.player.getBoundingBox().stretch(mc.player.getRotationVector().multiply(maxDist)).expand(expand),
                e -> {
                    if (e == mc.player) return false;
                    if (!(e instanceof LivingEntity || e instanceof PlayerEntity)) return false;
                    return e.squaredDistanceTo(mc.player) <= maxDist * maxDist;
                });

        if (candidates.isEmpty()) return null;

        return candidates.stream().map(e -> {
            Box b = e.getBoundingBox().expand(expand);
            // Box.raycast returns Optional<Vec3d> in this mapping
            Optional<Vec3d> opt = b.raycast(eye, end);
            double dist = Double.POSITIVE_INFINITY;
            if (opt.isPresent()) dist = opt.get().distanceTo(eye);
            return new java.util.AbstractMap.SimpleEntry<>(e, dist);
        })
        .filter(entry -> Double.isFinite(entry.getValue()) && entry.getValue() <= maxDist)
        .min(Comparator.comparingDouble(java.util.AbstractMap.SimpleEntry::getValue))
        .map(java.util.AbstractMap.SimpleEntry::getKey).orElse(null);
    }
        }
