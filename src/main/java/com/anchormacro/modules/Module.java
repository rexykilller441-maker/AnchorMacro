package com.anchormacro.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

/**
 * Basic module interface. Each module may tick, render, and handle attack events.
 */
public interface Module {
    String getName();
    boolean isEnabled();
    void setEnabled(boolean e);

    // client tick
    default void tick(MinecraftClient mc) {}

    // render stage (UI/world)
    default void render(MatrixStack matrices) {}

    // attack event (target entity)
    default void onAttack(Entity target) {}
}
