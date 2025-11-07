package com.anchormacro;

import com.anchormacro.ui.ModulesGuiScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class AnchorMacroConfigScreen extends Screen {
    private final Screen parent;
    private AnchorMacroConfig cfg;

    public AnchorMacroConfigScreen(Screen parent) {
        super(Text.literal("AnchorMacro Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        cfg = AnchorMacroConfig.get();

        int centerX = this.width / 2;
        int y = 40;

        // Example simple anchor toggle
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Anchor Macro: " + (cfg.anchorMacroEnabled ? "ON" : "OFF")), b -> {
            cfg.anchorMacroEnabled = !cfg.anchorMacroEnabled;
            cfg.save();
            b.setMessage(Text.literal("Anchor Macro: " + (cfg.anchorMacroEnabled ? "ON" : "OFF")));
        }).position(centerX - 100, y).size(200, 20).build());

        y += 26;
        // Modules button opens separate modules screen
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Modules..."), b -> {
            this.client.setScreen(new ModulesGuiScreen(this));
        }).position(centerX - 100, y).size(200, 20).build());

        y += 40;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save & Close"), b -> {
            cfg.save();
            this.client.setScreen(parent);
        }).position(centerX - 100, y).size(200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // updated API: renderBackground requires (context, mouseX, mouseY, delta)
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("AnchorMacro Configuration"), this.width / 2, 12, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}
