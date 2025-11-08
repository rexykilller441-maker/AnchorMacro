package com.anchormacro.modules.combat;

import com.anchormacro.AnchorMacroConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;

/**
 * Gives sword-like knockback when hitting with a totem.
 */
public class TotemHit {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void onAttack(Entity target) {
        if (!AnchorMacroConfig.get().explodeOnlyIfTotemPresent) return;
        if (!(target instanceof PlayerEntity)) return;
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        ItemStack main = mc.player.getMainHandStack();
        if (main.getItem() != Items.TOTEM_OF_UNDYING) return;

        mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking()));
        mc.player.swingHand(Hand.MAIN_HAND);
        target.addVelocity(0, 0.4f, 0);
        target.velocityModified = true;
        mc.player.sendMessage(net.minecraft.text.Text.literal("§e[TotemHit] Knockback applied!"), true);
    }
}
