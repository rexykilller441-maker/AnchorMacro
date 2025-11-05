package com.anchormacro;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.PressAction;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AnchorMacroConfigScreen extends Screen {
    private final Screen parent;
    private final AnchorMacroConfig cfg;

    // layout positions
    private int centerX;
    private int startY;

    protected AnchorMacroConfigScreen(Screen parent) {
        super(Text.literal("Anchor Macro Settings"));
        this.parent = parent;
        this.cfg = AnchorMacroConfig.get();
    }

    @Override
    protected void init() {
        super.init();
        centerX = this.width / 2;
        startY = 30;
        int y = startY;
        int spacing = 22;

        // Delays group (ticks)
        this.addDrawableChild(makeLabel(centerX - 120, y, "Place Anchor delay: " + cfg.delayPlaceAnchor + "t"));
        this.addDrawableChild(new ButtonWidget(centerX + 40, y - 6, 20, 20, Text.literal("-"), btn -> {
            cfg.delayPlaceAnchor = Math.max(0, cfg.delayPlaceAnchor - 1);
            init(); // refresh labels
        }));
        this.addDrawableChild(new ButtonWidget(centerX + 65, y - 6, 20, 20, Text.literal("+"), btn -> {
            cfg.delayPlaceAnchor = Math.min(200, cfg.delayPlaceAnchor + 1);
            init();
        }));
        y += spacing;

        this.addDrawableChild(makeLabel(centerX - 120, y, "Switch -> Glowstone delay: " + cfg.delaySwitchToGlowstone + "t"));
        this.addDrawableChild(new ButtonWidget(centerX + 40, y - 6, 20, 20, Text.literal("-"), btn -> { cfg.delaySwitchToGlowstone = Math.max(0, cfg.delaySwitchToGlowstone - 1); init(); }));
        this.addDrawableChild(new ButtonWidget(centerX + 65, y - 6, 20, 20, Text.literal("+"), btn -> { cfg.delaySwitchToGlowstone = Math.min(200, cfg.delaySwitchToGlowstone + 1); init(); }));
        y += spacing;

        this.addDrawableChild(makeLabel(centerX - 120, y, "Charge Anchor delay: " + cfg.delayChargeAnchor + "t"));
        this.addDrawableChild(new ButtonWidget(centerX + 40, y - 6, 20, 20, Text.literal("-"), btn -> { cfg.delayChargeAnchor = Math.max(0, cfg.delayChargeAnchor - 1); init(); }));
        this.addDrawableChild(new ButtonWidget(centerX + 65, y - 6, 20, 20, Text.literal("+"), btn -> { cfg.delayChargeAnchor = Math.min(200, cfg.delayChargeAnchor + 1); init(); }));
        y += spacing;

        this.addDrawableChild(makeLabel(centerX - 120, y, "Switch -> Totem delay: " + cfg.delaySwitchToTotem + "t"));
        this.addDrawableChild(new ButtonWidget(centerX + 40, y - 6, 20, 20, Text.literal("-"), btn -> { cfg.delaySwitchToTotem = Math.max(0, cfg.delaySwitchToTotem - 1); init(); }));
        this.addDrawableChild(new ButtonWidget(centerX + 65, y - 6, 20, 20, Text.literal("+"), btn -> { cfg.delaySwitchToTotem = Math.min(200, cfg.delaySwitchToTotem + 1); init(); }));
        y += spacing;

        this.addDrawableChild(makeLabel(centerX - 120, y, "Explode Anchor delay: " + cfg.delayExplodeAnchor + "t"));
        this.addDrawableChild(new ButtonWidget(centerX + 40, y - 6, 20, 20, Text.literal("-"), btn -> { cfg.delayExplodeAnchor = Math.max(0, cfg.delayExplodeAnchor - 1); init(); }));
        this.addDrawableChild(new ButtonWidget(centerX + 65, y - 6, 20, 20, Text.literal("+"), btn -> { cfg.delayExplodeAnchor = Math.min(200, cfg.delayExplodeAnchor + 1); init(); }));
        y += spacing + 6;

        // Slots
        this.addDrawableChild(makeLabel(centerX - 120, y, "Anchor slot (0-8): " + cfg.anchorSlot));
        this.addDrawableChild(new ButtonWidget(centerX + 40, y - 6, 20, 20, Text.literal("-"), btn -> { cfg.anchorSlot = clampSlot(cfg.anchorSlot - 1); init(); }));
        this.addDrawableChild(new ButtonWidget(centerX + 65, y - 6, 20, 20, Text.literal("+"), btn -> { cfg.anchorSlot = clampSlot(cfg.anchorSlot + 1); init(); }));
        y += spacing;

        this.addDrawableChild(makeLabel(centerX - 120, y, "Glowstone slot (0-8): " + cfg.glowstoneSlot));
        this.addDrawableChild(new ButtonWidget(centerX + 40, y - 6, 20, 20, Text.literal("-"), btn -> { cfg.glowstoneSlot = clampSlot(cfg.glowstoneSlot - 1); init(); }));
        this.addDrawableChild(new ButtonWidget(centerX + 65, y - 6, 20, 20, Text.literal("+"), btn -> { cfg.glowstoneSlot = clampSlot(cfg.glowstoneSlot + 1); init(); }));
        y += spacing;

        this.addDrawableChild(makeLabel(centerX - 120, y, "Totem slot (0-8): " + cfg.totemSlot));
        this.addDrawableChild(new ButtonWidget(centerX + 40, y - 6, 20, 20, Text.literal("-"), btn -> { cfg.totemSlot = clampSlot(cfg.totemSlot - 1); init(); }));
        this.addDrawableChild(new ButtonWidget(centerX + 65, y - 6, 20, 20, Text.literal("+"), btn -> { cfg.totemSlot = clampSlot(cfg.totemSlot + 1); init(); }));
        y += spacing + 6;

        // Toggles
        this.addDrawableChild(new ButtonWidget(centerX - 120, y, 230, 20, Text.literal("Safe Anchor Mode: " + (cfg.safeAnchorMode ? "ON" : "OFF")), btn -> {
            cfg.safeAnchorMode = !cfg.safeAnchorMode;
            init();
        }));
        y += spacing;

        this.addDrawableChild(new ButtonWidget(centerX - 120, y, 230, 20, Text.literal("Explode only if totem present: " + (cfg.explodeOnlyIfTotemPresent ? "YES" : "NO")), btn -> {
            cfg.explodeOnlyIfTotemPresent = !cfg.explodeOnlyIfTotemPresent;
            init();
        }));
        y += spacing;

        // Save / Cancel / Reset
        this.addDrawableChild(new ButtonWidget(centerX - 100, height - 40, 80, 20, Text.literal("Save"), btn -> {
            cfg.save();
            onClose();
        }));

        this.addDrawableChild(new ButtonWidget(centerX - 5, height - 40, 80, 20, Text.literal("Cancel"), btn -> {
            // reload config from disk to discard changes
            AnchorMacroConfig.INSTANCE = null; // force reload next get()
            onClose();
        }));

        this.addDrawableChild(new ButtonWidget(centerX + 90, height - 40, 80, 20, Text.literal("Reset"), btn -> {
            cfg.resetToDefaults();
            init();
        }));
    }

    private ButtonWidget makeLabel(int x, int y, String text) {
        // label simulated with a disabled button-like widget for consistent sizing
        ButtonWidget b = new ButtonWidget(x, y, 230, 20, Text.literal(text), btn -> {});
        b.active = false;
        return b;
    }

    private int clampSlot(int v) {
        if (v < 0) return 0;
        if (v > 8) return 8;
        return v;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        drawCenteredText(matrices, this.textRenderer, this.title.getString(), centerX, 10, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
          }
