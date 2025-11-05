package com.anchormacro;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class AnchorMacroExecutor {
    
    private static int glowstoneSlot = -1;
    private static int totemSlot = -1;
    private static int step = 0;
    private static int tickCounter = 0;
    private static final int DELAY_TICKS = 3;
    
    public static void execute(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        
        if (player == null) return;
        
        findItems(player);
        
        if (glowstoneSlot == -1) {
            player.sendMessage(net.minecraft.text.Text.literal("§cNo glowstone found in hotbar!"), false);
            return;
        }
        
        if (totemSlot == -1) {
            player.sendMessage(net.minecraft.text.Text.literal("§cNo totem found in hotbar!"), false);
            return;
        }
        
        step = 0;
        tickCounter = 0;
        
        ClientTickEvents.END_CLIENT_TICK.register(new ClientTickEvents.EndTick() {
            @Override
            public void onEndTick(MinecraftClient client) {
                if (step > 0) {
                    executeStep(client);
                }
            }
        });
        
        step = 1;
    }
    
    private static void executeStep(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) return;
        
        tickCounter++;
        
        if (tickCounter < DELAY_TICKS) return;
        
        tickCounter = 0;
        
        switch (step) {
            case 1:
                placeBlock(client);
                step = 2;
                break;
            case 2:
                player.getInventory().selectedSlot = glowstoneSlot;
                step = 3;
                break;
            case 3:
                rightClick(client);
                step = 4;
                break;
            case 4:
                player.getInventory().selectedSlot = totemSlot;
                step = 5;
                break;
            case 5:
                rightClick(client);
                step = 0;
                break;
            default:
                step = 0;
                break;
        }
    }
    
    private static void findItems(ClientPlayerEntity player) {
        glowstoneSlot = -1;
        totemSlot = -1;
        
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            
            if (stack.getItem() == Items.GLOWSTONE && glowstoneSlot == -1) {
                glowstoneSlot = i;
            }
            
            if (stack.getItem() == Items.TOTEM_OF_UNDYING && totemSlot == -1) {
                totemSlot = i;
            }
        }
    }
    
    private static void placeBlock(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) return;
        
        Vec3d lookVec = player.getRotationVec(1.0F);
        Vec3d start = player.getEyePos();
        Vec3d end = start.add(lookVec.multiply(4.5));
        
        BlockHitResult hitResult = new BlockHitResult(
            player.getPos().add(0, -0.5, 0),
            Direction.UP,
            BlockPos.ofFloored(player.getPos().add(0, -1, 0)),
            false
        );
        
        client.interactionManager.interactBlock(
            player,
            Hand.MAIN_HAND,
            hitResult
        );
    }
    
    private static void rightClick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) return;
        
        Vec3d lookVec = player.getRotationVec(1.0F);
        Vec3d start = player.getEyePos();
        Vec3d end = start.add(lookVec.multiply(4.5));
        
        BlockHitResult hitResult = client.world.raycast(
            new net.minecraft.world.RaycastContext(
                start,
                end,
                net.minecraft.world.RaycastContext.ShapeType.OUTLINE,
                net.minecraft.world.RaycastContext.FluidHandling.NONE,
                player
            )
        );
        
        if (hitResult != null) {
            client.interactionManager.interactBlock(
                player,
                Hand.MAIN_HAND,
                hitResult
            );
        }
    }
}