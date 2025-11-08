package com.anchormacro;

import com.anchormacro.ui.ModulesGuiScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Main config screen (anchor-specific settings) with a "Modules..." button that opens the separate modules GUI.
 */
public class AnchorMacroConfigScreen extends Screen {
    private final Screen parent;

    public AnchorMacroConfigScreen(Screen parent) {
        super(Text.literal("AnchorMacro Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int y = 40;

        // simple anchor macro enable/disable (uses AnchorMacroConfig static fields)
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Anchor Macro: " + (AnchorMacroConfig.anchorSlot >= 0 ? "ON" : "OFF")),
                b -> {
                    // toggle a simple boolean by using anchorSlot >=0 hack is not ideal;
                    // For clarity we'll toggle safeAnchorMode as example setting here:
                    AnchorMacroConfig.safeAnchorMode = !AnchorMacroConfig.safeAnchorMode;
                    AnchorMacroConfig.save();
                    b.setMessage(Text.literal("Safe Anchor Mode: " + (AnchorMacroConfig.safeAnchorMode ? "ON" : "OFF")));
                })
                .position(centerX - 110, y).size(220, 20).build());

        y += 28;
        // modules button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Modules..."),
                b -> this.client.setScreen(new ModulesGuiScreen(this))
        ).position(centerX - 110, y).size(220, 20).build());

        y += 40;
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Save & Close"),
                b -> {
                    AnchorMacroConfig.save();
                    this.client.setScreen(parent);
                }).position(centerX - 110, y).size(220, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // new signature for renderBackground
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("AnchorMacro Configuration"), this.width / 2, 12, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}
