package com.anchormacro.modules.combat;

import com.anchormacro.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.text.Text;

/**
 * AutoTotem: when enabled, watches main/off hand and if a totem "pops" (slot empty),
 * it will open the inventory (visible) and move a totem from inventory into the target slot.
 */
public class AutoTotem implements Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private boolean enabled = true;
    // short cooldown to avoid spamming inventory opens
    private int cooldownTicks = 0;
    private final int COOLDOWN = 10; // ticks

    // prefer offhand or mainhand: "offhand" true -> fill offhand, else mainhand
    public boolean preferOffhand = true;

    @Override
    public String getName() { return "AutoTotem"; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean e) { enabled = e; }

    @Override
    public void tick(net.minecraft.client.MinecraftClient client) {
        if (!enabled || client.player == null || client.interactionManager == null) {
            if (cooldownTicks > 0) cooldownTicks--;
            return;
        }
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return;
        }

        try {
            // check offhand and mainhand according to preference
            boolean needOff = client.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING;
            boolean needMain = client.player.getMainHandStack().getItem() != Items.TOTEM_OF_UNDYING;

            // choose which to fill first
            if (preferOffhand && needOff) {
                if (tryFillHand(client, /*offhand=*/true)) { cooldownTicks = COOLDOWN; return; }
            }
            if (needMain) {
                if (tryFillHand(client, /*offhand=*/false)) { cooldownTicks = COOLDOWN; return; }
            }
            // if prefer offhand = false, try offhand last
            if (!preferOffhand && needOff) {
                if (tryFillHand(client, /*offhand=*/true)) { cooldownTicks = COOLDOWN; return; }
            }
        } catch (Exception e) {
            // swallow errors
            cooldownTicks = COOLDOWN;
        }
    }

    /**
     * Attempts to open inventory and move a totem into the selected hand.
     * Returns true if an action was performed.
     */
    private boolean tryFillHand(MinecraftClient client, boolean offhand) {
        if (client.player == null || client.player.currentScreenHandler == null) return false;

        // find a totem in inventory (36 slots)
        int found = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack s = client.player.getInventory().getStack(i);
            if (s != null && s.getItem() == Items.TOTEM_OF_UNDYING) {
                found = i;
                break;
            }
        }
        if (found == -1) return false;

        // convert to container slot index: in player container, hotbar 0..8 -> slots 36..44, main inventory 9..35 -> 9..35
        int containerSlot = (found < 9) ? found + 36 : found;

        // show inventory screen to the player
        try {
            client.setScreen(new InventoryScreen(client.player));
        } catch (Throwable ignored) {
            // fallback: still attempt clicks even if inventory not visible
        }

        int windowId = client.player.currentScreenHandler.syncId;
        int destSlot = offhand ? 45 : client.player.getInventory().selectedSlot; // offhand is index 45 in player container; mainhand uses selectedSlot (0..8) mapped to container slot = selectedSlot

        // If filling mainhand, map selectedSlot (0..8) to container index (36..44)
        if (!offhand) {
            destSlot = client.player.getInventory().selectedSlot + 36;
        }

        try {
            // pick up the found totem
            client.interactionManager.clickSlot(windowId, containerSlot, 0, SlotActionType.PICKUP, client.player);
            // put into destination (offhand or main hotbar slot)
            client.interactionManager.clickSlot(windowId, destSlot, 0, SlotActionType.PICKUP, client.player);
            // pick up remaining (return to original slot)
            client.interactionManager.clickSlot(windowId, containerSlot, 0, SlotActionType.PICKUP, client.player);
        } catch (Exception ignored) {
            return false;
        }

        // leave the inventory open for a short moment so player sees it; cooldown prevents spam
        cooldownTicks = COOLDOWN;
        return true;
    }
}
