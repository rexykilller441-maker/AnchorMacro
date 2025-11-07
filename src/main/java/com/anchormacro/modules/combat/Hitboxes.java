package com.anchormacro.modules.combat;

import com.anchormacro.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;

/**
 * Hitboxes (functional): expanded entity raycast to increase effective hit registration.
 * - expand: extra expansion in meters
 * - distance: max reach
 */
public class Hitboxes implements Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private boolean enabled = true;
    public float expand = 0.5f;
    public float distance = 6.0f; // default reachable distance (client-side)

    @Override
    public String getName() { return "Hitbox"; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean e) { enabled = e; }

    @Override
    public void tick(MinecraftClient client) { /* none per tick */ }

    /**
     * Returns the nearest entity intersected by an expanded bounding box along player's look vector within maxDist.
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
                    if (!(e instanceof PlayerEntity) && !(e instanceof LivingEntity)) return false;
                    return e.squaredDistanceTo(mc.player) <= maxDist * maxDist;
                });

        if (candidates.isEmpty()) return null;

        return candidates.stream()
                .map(e -> {
                    Box b = e.getBoundingBox().expand(expand);
                    HitResult hr = b.raycast(eye, end);
                    double dist = Double.POSITIVE_INFINITY;
                    if (hr != null && hr.getType() != HitResult.Type.MISS) {
                        dist = hr.getPos().distanceTo(eye);
                    }
                    return new java.util.AbstractMap.SimpleEntry<>(e, dist);
                })
                .filter(en -> Double.isFinite(en.getValue()) && en.getValue() <= maxDist)
                .min(Comparator.comparingDouble(java.util.AbstractMap.SimpleEntry::getValue))
                .map(java.util.AbstractMap.SimpleEntry::getKey)
                .orElse(null);
    }
}
