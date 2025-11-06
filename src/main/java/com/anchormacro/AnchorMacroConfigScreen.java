package com.anchormacro;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class AnchorMacroConfigScreen extends Screen {
    private final Screen parent;
    private final AnchorMacroConfig cfg;
    private final int UI_WIDTH = 280; // good fit for Zalith and desktop

    protected AnchorMacroConfigScreen(Screen parent) {
        super(Text.literal("Anchor Macro Settings"));
        this.parent = parent;
        this.cfg = AnchorMacroConfig.get();
    }

    @Override
    protected void init() {
        // clear old UI to prevent overlapping
        this.clearChildren();
        super.init();

        int centerX = this.width / 2;
        int left = centerX - (UI_WIDTH / 2);
        int y = 20;
        int spacing = 28;

        addLabel(left, y, "Anchor Macro Settings");
        y += spacing;

        addIntSlider(left, y, "Place Anchor delay (ticks)", cfg.delayPlaceAnchor, 0, 200, v -> cfg.delayPlaceAnchor = v);
        y += spacing;
        addIntSlider(left, y, "Switch → Glowstone delay (ticks)", cfg.delaySwitchToGlowstone, 0, 200, v -> cfg.delaySwitchToGlowstone = v);
        y += spacing;
        addIntSlider(left, y, "Charge Anchor delay (ticks)", cfg.delayChargeAnchor, 0, 200, v -> cfg.delayChargeAnchor = v);
        y += spacing;
        addIntSlider(left, y, "Switch → Totem delay (ticks)", cfg.delaySwitchToTotem, 0, 200, v -> cfg.delaySwitchToTotem = v);
        y += spacing;
        addIntSlider(left, y, "Explode Anchor delay (ticks)", cfg.delayExplodeAnchor, 0, 200, v -> cfg.delayExplodeAnchor = v);
        y += spacing + 6;

        addIntSlider(left, y, "Anchor slot (1-9)", cfg.anchorSlot, 1, 9, v -> cfg.anchorSlot = v);
        y += spacing;
        addIntSlider(left, y, "Glowstone slot (1-9)", cfg.glowstoneSlot, 1, 9, v -> cfg.glowstoneSlot = v);
        y += spacing;
        addIntSlider(left, y, "Totem slot (1-9)", cfg.totemSlot, 1, 9, v -> cfg.totemSlot = v);
        y += spacing + 6;

        addToggle(left, y, "Safe Anchor Mode", cfg.safeAnchorMode, v -> cfg.safeAnchorMode = v);
        y += spacing;
        addToggle(left, y, "Explode only if totem present", cfg.explodeOnlyIfTotemPresent, v -> cfg.explodeOnlyIfTotemPresent = v);
        y += spacing;
        addToggle(left, y, "Auto-search hotbar (1–9)", cfg.autoSearchHotbar, v -> cfg.autoSearchHotbar = v);
        y += spacing;
        addToggle(left, y, "Show notifications", cfg.showNotifications, v -> cfg.showNotifications = v);
        y += spacing + 6;

        // bottom buttons
        int bottomY = this.height - 40;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save"), b -> {
            cfg.save();
            this.close();
        }).position(left + 10, bottomY).size(80, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), b -> {
            AnchorMacroConfig.reload();
            this.close();
        }).position(left + 105, bottomY).size(80, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), b -> {
            cfg.resetToDefaults();
            init();
        }).position(left + 200, bottomY).size(70, 20).build());
    }

    private void addLabel(int left, int y, String text) {
        ButtonWidget label = ButtonWidget.builder(Text.literal(text), b -> {}).position(left, y).size(UI_WIDTH, 20).build();
        label.active = false;
        this.addDrawableChild(label);
    }

    private void addIntSlider(int left, int y, String label, int value, int min, int max, java.util.function.IntConsumer setter) {
        ButtonWidget lbl = ButtonWidget.builder(Text.literal(label + ": " + value), b -> {}).position(left, y).size(UI_WIDTH, 20).build();
        lbl.active = false;
        this.addDrawableChild(lbl);

        double normalized = (double)(value - min) / (double)Math.max(1, (max - min));
        SliderWidget slider = new SliderWidget(left, y + 18, UI_WIDTH, 10, Text.literal(String.valueOf(value)), normalized) {
            private int lastInt = (int) value;
            @Override
            protected void applyValue() {
                int intVal = min + (int)Math.round(this.value * (max - min));
                if (intVal != lastInt) {
                    lastInt = intVal;
                    setter.accept(intVal);
                    lbl.setMessage(Text.literal(label + ": " + intVal));
                }
            }
            @Override
            protected void updateMessage() {
                int intVal = min + (int)Math.round(this.value * (max - min));
                this.setMessage(Text.literal(String.valueOf(intVal)));
            }
        };
        this.addDrawableChild(slider);
    }

    private void addToggle(int left, int y, String label, boolean value, java.util.function.Consumer<Boolean> setter) {
        this.addDrawableChild(ButtonWidget.builder(Text.literal(label + ": " + (value ? "ON" : "OFF")), b -> {
            setter.accept(!value);
            init(); // rebuild cleanly
        }).position(left, y).size(UI_WIDTH, 20).build());
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
