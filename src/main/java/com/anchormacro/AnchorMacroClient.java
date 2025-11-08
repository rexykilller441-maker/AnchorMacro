package com.anchormacro;

import com.anchormacro.modules.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

public class AnchorMacroClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AnchorMacroConfig.load();
        System.out.println("[AnchorMacro] Config loaded successfully!");

        // Tick updates (e.g., AutoTotem)
        ClientTickEvents.END_CLIENT_TICK.register(client -> ModuleManager.tickAll());

        // Rendering events (e.g., Hitboxes)
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MatrixStack matrices = context.matrixStack();
            VertexConsumerProvider provider = context.consumers();
            ModuleManager.renderAll(matrices, provider);
        });
    }
}
