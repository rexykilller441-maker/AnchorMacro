package com.anchormacro.ui;

import com.anchormacro.modules.Module;
import com.anchormacro.modules.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * GUI screen for managing modules
 */
public class ModulesGuiScreen extends Screen {
    private final Screen parent;

    public ModulesGuiScreen(Screen parent) {
        super(Text.literal("Modules Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int y = 40;

        // Add toggle buttons for each module
        for (Module module : ModuleManager.getModules()) {
            final Module mod = module; // for lambda
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(mod.getName() + ": " + (mod.isEnabled() ? "ON" : "OFF")),
                    b -> {
                        mod.toggle();
                        b.setMessage(Text.literal(mod.getName() + ": " + (mod.isEnabled() ? "ON" : "OFF")));
                    })
                    .position(centerX - 110, y)
                    .size(220, 20)
                    .build());
            y += 25;
        }

        // Back button
        y += 20;
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Back"),
                b -> this.client.setScreen(parent))
                .position(centerX - 110, y)
                .size(220, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Modules Configuration"),
                this.width / 2,
                12,
                0xFFFFFF
        );
        super.render(context, mouseX, mouseY, delta);
    }
}
