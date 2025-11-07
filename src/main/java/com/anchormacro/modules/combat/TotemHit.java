package com.anchormacro.modules.combat;

import com.anchormacro.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;

/**
 * TotemHit: when holding a totem in main hand and you attack a player, temporarily switch to a sword and send an attack packet
 * so server applies sword knockback.
 */
public class TotemHit implements Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private boolean enabled = true;

    @Override
    public String getName() { return "TotemHit"; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean e) { enabled = e; }

    @Override
    public void onAttack(Entity target) {
        if (!enabled) return;
        if (!(target instanceof PlayerEntity)) return;
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        ItemStack main = mc.player.getMainHandStack();
        if (main.getItem() == Items.TOTEM_OF_UNDYING) {
            for (int i = 0; i < 9; ++i) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack != null && stack.getItem() instanceof SwordItem) {
                    int old = mc.player.getInventory().selectedSlot;
                    mc.player.getInventory().selectedSlot = i;
                    // send attack packet — use mapping variant available in your project
                    try {
                        mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking()));
                    } catch (NoSuchMethodError | NoClassDefFoundError ex) {
                        // fallback if different mapping — attempt alternate constructor if necessary
                    }
                    mc.player.swingHand(Hand.MAIN_HAND);
                    mc.player.getInventory().selectedSlot = old;
                    return;
                }
            }
        }
    }
}
