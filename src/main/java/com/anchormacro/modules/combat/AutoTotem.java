package com.anchormacro.modules.combat;

import com.anchormacro.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem implements Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private boolean enabled = true;

    @Override
    public String getName() { return "AutoTotem"; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean e) { enabled = e; }

    @Override
    public void tick(MinecraftClient client) {
        if (!enabled || client.player == null || client.interactionManager == null) return;
        ItemStack offhand = client.player.getOffHandStack();
        if (offhand.getItem() == Items.TOTEM_OF_UNDYING) return;

        // search inventory
        for (int i = 0; i < 36; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                int slot = i < 9 ? i + 36 : i; // convert to container slot index
                try {
                    int windowId = client.player.currentScreenHandler.syncId;
                    client.interactionManager.clickSlot(windowId, slot, 0, SlotActionType.PICKUP, client.player);
                    client.interactionManager.clickSlot(windowId, 45, 0, SlotActionType.PICKUP, client.player); // offhand index 45
                    client.interactionManager.clickSlot(windowId, slot, 0, SlotActionType.PICKUP, client.player);
                } catch (Exception ignored) {}
                break;
            }
        }
    }
}
