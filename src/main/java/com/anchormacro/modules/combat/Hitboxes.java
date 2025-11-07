package com.anchormacro.modules.combat;

import com.anchormacro.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;

import java.util.Comparator;
import java.util.List;

/**
 * Hitboxes module (functional):
 * - When enabled, increases practical hit registration by performing an expanded entity raycast
 *   — the module itself does not alter visuals, but the client left-click handler uses it to find targets.
 */
public class Hitboxes implements Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private boolean enabled = true;
    // expand bounding boxes by this amount (meters)
    public float expand = 0.5f;
    // max distance to consider
    public float distance = 6.0f; // default reach; you can increase

    @Override
    public String getName() { return "Hitbox"; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean e) { enabled = e; }

    @Override
    public void tick(MinecraftClient client) {
        // nothing per-tick required; used by client left-click detection
    }

    /**
     * Performs an expanded-entity raycast using player's look vector, returns nearest entity hit within distance or null.
     */
    public Entity expandedRaycastTarget(double maxDist) {
        if (mc.player == null || mc.world == null) return null;
        Vec3d eye = mc.player.getEyePos();
        Vec3d look = mc.player.getRotationVector().multiply(maxDist);
        Vec3d end = eye.add(look);

        List<Entity> list = mc.world.getOtherEntities(mc.player, mc.player.getBoundingBox().stretch(mc.player.getRotationVector().multiply(maxDist)).expand(expand), e -> {
            if (e == mc.player) return false;
            if (!(e instanceof PlayerEntity) && !e.isLiving()) return false;
            // within squared distance
            return e.squaredDistanceTo(mc.player) <= maxDist * maxDist;
        });

        if (list.isEmpty()) return null;

        // find the closest entity intersecting with expanded box along line
        Entity closest = list.stream()
                .map(e -> {
                    Box b = e.getBoundingBox().expand(expand);
                    // check intersect with ray
                    HitResult hr = b.raycast(eye, end);
                    double d = (hr != null) ? hr.getPos().distanceTo(eye) : Double.POSITIVE_INFINITY;
                    return new java.util.AbstractMap.SimpleEntry<>(e, d);
                })
                .filter(entry -> entry.getValue().isFinite() && entry.getValue() <= maxDist)
                .min(Comparator.comparingDouble(java.util.AbstractMap.SimpleEntry::getValue))
                .map(java.util.AbstractMap.SimpleEntry::getKey).orElse(null);

        return closest;
    }
                        }
