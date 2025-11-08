package com.anchormacro.modules.combat;

import com.anchormacro.AnchorMacroConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/**
 * Automatically replaces the offhand totem visibly when it pops.
 */
public class AutoTotem {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void tick() {
        if (!AnchorMacroConfig.get().autoSearchHotbar || mc.player == null || mc.interactionManager == null)
            return;

        ItemStack offhand = mc.player.getOffHandStack();
        if (offhand.getItem() == Items.TOTEM_OF_UNDYING) return;

        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                int slot = i < 9 ? i + 36 : i;
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 45, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, SlotActionType.PICKUP, mc.player);
                mc.player.sendMessage(net.minecraft.text.Text.literal("§a[AutoTotem] Replaced offhand totem."), true);
                break;
            }
        }
    }
}
