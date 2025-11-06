package com.anchormacro;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class AnchorMacroConfigScreen extends Screen {
    private final Screen parent;
    private final AnchorMacroConfig cfg;
    private final int UI_WIDTH = 280; // cap for Zalith

    protected AnchorMacroConfigScreen(Screen parent) {
        super(Text.literal("Anchor Macro Settings"));
        this.parent = parent;
        this.cfg = AnchorMacroConfig.get();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int left = centerX - (UI_WIDTH / 2);
        int y = 20;
        int spacing = 26;

        // Title label (disabled button)
        ButtonWidget title = ButtonWidget.builder(Text.literal("Anchor Macro Settings"), b -> {})
                .position(left, y).size(UI_WIDTH, 20).build();
        title.active = false;
        this.addDrawableChild(title);
        y += spacing;

        // Delays - sliders (value 0..200 ticks)
        addIntSlider(left, y, "Place Anchor delay (ticks)", cfg.delayPlaceAnchor, 0, 200, val -> cfg.delayPlaceAnchor = val);
        y += spacing;
        addIntSlider(left, y, "Switch → Glowstone delay (ticks)", cfg.delaySwitchToGlowstone, 0, 200, val -> cfg.delaySwitchToGlowstone = val);
        y += spacing;
        addIntSlider(left, y, "Charge Anchor delay (ticks)", cfg.delayChargeAnchor, 0, 200, val -> cfg.delayChargeAnchor = val);
        y += spacing;
        addIntSlider(left, y, "Switch → Totem delay (ticks)", cfg.delaySwitchToTotem, 0, 200, val -> cfg.delaySwitchToTotem = val);
        y += spacing;
        addIntSlider(left, y, "Explode Anchor delay (ticks)", cfg.delayExplodeAnchor, 0, 200, val -> cfg.delayExplodeAnchor = val);
        y += spacing + 6;

        // Slots - sliders 1..9
        addIntSlider(left, y, "Anchor slot (1-9)", cfg.anchorSlot, 1, 9, val -> cfg.anchorSlot = val);
        y += spacing;
        addIntSlider(left, y, "Glowstone slot (1-9)", cfg.glowstoneSlot, 1, 9, val -> cfg.glowstoneSlot = val);
        y += spacing;
        addIntSlider(left, y, "Totem slot (1-9)", cfg.totemSlot, 1, 9, val -> cfg.totemSlot = val);
        y += spacing + 6;

        // Toggles
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Safe Anchor Mode: " + (cfg.safeAnchorMode ? "ON" : "OFF")), b -> {
            cfg.safeAnchorMode = !cfg.safeAnchorMode;
            init();
        }).position(left, y).size(UI_WIDTH, 20).build());
        y += spacing;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Explode only if totem present: " + (cfg.explodeOnlyIfTotemPresent ? "YES" : "NO")), b -> {
            cfg.explodeOnlyIfTotemPresent = !cfg.explodeOnlyIfTotemPresent;
            init();
        }).position(left, y).size(UI_WIDTH, 20).build());
        y += spacing;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Auto-search hotbar (1-9): " + (cfg.autoSearchHotbar ? "YES" : "NO")), b -> {
            cfg.autoSearchHotbar = !cfg.autoSearchHotbar;
            init();
        }).position(left, y).size(UI_WIDTH, 20).build());
        y += spacing;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Show notifications: " + (cfg.showNotifications ? "YES" : "NO")), b -> {
            cfg.showNotifications = !cfg.showNotifications;
            init();
        }).position(left, y).size(UI_WIDTH, 20).build());
        y += spacing + 6;

        // Save / Cancel / Reset
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save"), b -> {
            cfg.save();
            this.close();
        }).position(left + 10, this.height - 36).size(80, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), b -> {
            AnchorMacroConfig.reload();
            this.close();
        }).position(left + 105, this.height - 36).size(80, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), b -> {
            cfg.resetToDefaults();
            init();
        }).position(left + 200, this.height - 36).size(70, 20).build());
    }

    private void addIntSlider(int left, int y, String label, int value, int min, int max, java.util.function.IntConsumer setter) {
        // label
        ButtonWidget lbl = ButtonWidget.builder(Text.literal(label + ": " + value), b -> {}).position(left, y).size(UI_WIDTH, 20).build();
        lbl.active = false;
        this.addDrawableChild(lbl);

        // slider (subclass)
        double normalized = (double)(value - min) / (double)Math.max(1, (max - min));
        SliderWidget slider = new SliderWidget(left, y + 18, UI_WIDTH, 10, Text.literal(""), normalized) {
            private int lastInt = value;
            @Override protected void applyValue() {
                double val = this.value;
                int intVal = min + (int)Math.round(val * (max - min));
                if (intVal != lastInt) {
                    lastInt = intVal;
                    setter.accept(intVal);
                    // update label text
                    lbl.setMessage(Text.literal(label + ": " + intVal));
                }
            }
            @Override public Text getMessage() {
                int intVal = min + (int)Math.round(this.value * (max - min));
                return Text.literal(String.valueOf(intVal));
            }
        };
        slider.setValue(normalized);
        this.addDrawableChild(slider);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
                                                   }
