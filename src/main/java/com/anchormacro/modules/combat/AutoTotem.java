package com.anchormacro.modules.combat;

import com.anchormacro.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/**
 * AutoTotem: when enabled, watches main/off hand; if empty, opens inventory (visible) and moves a Totem into chosen hand.
 */
public class AutoTotem implements Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private boolean enabled = true;
    private int cooldown = 0;
    private final int COOLDOWN_TICKS = 8;
    public boolean preferOffhand = true; // configurable

    @Override
    public String getName() { return "AutoTotem"; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean e) { enabled = e; }

    @Override
    public void tick(MinecraftClient client) {
        if (!enabled || client.player == null || client.interactionManager == null) {
            if (cooldown > 0) cooldown--;
            return;
        }
        if (cooldown > 0) { cooldown--; return; }

        boolean needOff = client.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING;
        boolean needMain = client.player.getMainHandStack().getItem() != Items.TOTEM_OF_UNDYING;

        // prioritize
        if (preferOffhand && needOff) { if (performFill(client, true)) { cooldown = COOLDOWN_TICKS; return; } }
        if (needMain) { if (performFill(client, false)) { cooldown = COOLDOWN_TICKS; return; } }
        if (!preferOffhand && needOff) { if (performFill(client, true)) { cooldown = COOLDOWN_TICKS; return; } }
    }

    private boolean performFill(MinecraftClient client, boolean offhand) {
        // find totem slot in player inventory (0..35)
        int found = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack s = client.player.getInventory().getStack(i);
            if (s != null && s.getItem() == Items.TOTEM_OF_UNDYING) { found = i; break; }
        }
        if (found == -1) return false;

        // show inventory screen
        try {
            client.setScreen(new InventoryScreen(client.player));
        } catch (Throwable ignored) {}

        int windowId = client.player.currentScreenHandler.syncId;
        int containerSlot = (found < 9) ? found + 36 : found; // convert
        int destSlot = offhand ? 45 : (client.player.getInventory().selectedSlot + 36);

        try {
            client.interactionManager.clickSlot(windowId, containerSlot, 0, SlotActionType.PICKUP, client.player);
            client.interactionManager.clickSlot(windowId, destSlot, 0, SlotActionType.PICKUP, client.player);
            client.interactionManager.clickSlot(windowId, containerSlot, 0, SlotActionType.PICKUP, client.player);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
