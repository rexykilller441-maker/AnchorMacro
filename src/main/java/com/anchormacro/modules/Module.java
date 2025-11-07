package com.anchormacro.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public interface Module {
    String getName();
    boolean isEnabled();
    void setEnabled(boolean e);

    default void tick(MinecraftClient client) {}
    default void render(MatrixStack matrices) {}
    default void onAttack(Entity target) {}
}
