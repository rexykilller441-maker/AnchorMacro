package com.anchormacro;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Non-destructive test runner. Call requestTest() to trigger a test on the next tick.
 */
public class AnchorMacroTestRunner {
    private static final AtomicBoolean requested = new AtomicBoolean(false);

    public static void requestTest() {
        requested.set(true);
    }

    public static void tickIfRequested() {
        if (!requested.getAndSet(false)) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;
        ClientPlayerEntity p = mc.player;
        if (p == null) return;

        AnchorMacroConfig cfg = AnchorMacroConfig.get();
        int anchorSlot = AnchorMacroConfig.guiToInternalSlot(cfg.anchorSlot);
        int glowstoneSlot = AnchorMacroConfig.guiToInternalSlot(cfg.glowstoneSlot);
        int totemSlot = AnchorMacroConfig.guiToInternalSlot(cfg.totemSlot);

        if (cfg.autoSearchHotbar) {
            anchorSlot = findSlotFor(p, Items.RESPAWN_ANCHOR, anchorSlot);
            glowstoneSlot = findSlotFor(p, Items.GLOWSTONE, glowstoneSlot);
            totemSlot = findSlotFor(p, Items.TOTEM_OF_UNDYING, totemSlot);
        }

        // checks
        boolean okAnchor = p.getInventory().getStack(anchorSlot).isOf(Items.RESPAWN_ANCHOR);
        boolean okGlow = p.getInventory().getStack(glowstoneSlot).isOf(Items.GLOWSTONE);
        boolean okTotem = p.getInventory().getStack(totemSlot).isOf(Items.TOTEM_OF_UNDYING) || p.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING);

        if (!okAnchor) {
            p.sendMessage(net.minecraft.text.Text.literal("§cAnchor test failed: anchor not in hotbar."), false);
            return;
        }
        if (!okGlow) {
            p.sendMessage(net.minecraft.text.Text.literal("§cAnchor test failed: glowstone not in hotbar."), false);
            return;
        }
        if (!okTotem && cfg.explodeOnlyIfTotemPresent) {
            p.sendMessage(net.minecraft.text.Text.literal("§cAnchor test failed: totem not in hotbar/offhand."), false);
            return;
        }

        // raycast checks (non-destructive) - ensure player has a target or space to place
        Vec3d start = p.getEyePos();
        Vec3d look = p.getRotationVec(1.0F);
        Vec3d end = start.add(look.multiply(5.0));
        net.minecraft.util.hit.HitResult hr = mc.world.raycast(new net.minecraft.world.RaycastContext(start, end, net.minecraft.world.RaycastContext.ShapeType.OUTLINE, net.minecraft.world.RaycastContext.FluidHandling.NONE, p));
        boolean rayOk = (hr != null);

        if (!rayOk) {
            p.sendMessage(net.minecraft.text.Text.literal("§cAnchor test failed: no valid target (look at a block/space)."), false);
            return;
        }

        p.sendMessage(net.minecraft.text.Text.literal("§aAnchorMacro test successful — mod seems to be working."), false);
    }

    private static int findSlotFor(ClientPlayerEntity p, net.minecraft.item.Item item, int preferred) {
        if (p.getInventory().getStack(preferred).isOf(item)) return preferred;
        for (int i = 0; i < 9; i++) {
            if (p.getInventory().getStack(i).isOf(item)) return i;
        }
        return preferred;
    }
}
