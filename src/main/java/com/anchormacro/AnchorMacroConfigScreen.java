package com.anchormacro;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.function.IntConsumer;
import java.util.function.Consumer;

public class AnchorMacroConfigScreen extends Screen {
    private final Screen parent;
    private final AnchorMacroConfig cfg;

    private int UI_WIDTH;
    private int LEFT;
    private int Y_START;
    private int SPACING;

    private final int MIN_DELAY = 0;
    private final int MAX_DELAY = 200;
    private final int SLOT_MIN = 1;
    private final int SLOT_MAX = 9;

    private ButtonWidget safeModeBtn;
    private ButtonWidget explodeIfTotemBtn;
    private ButtonWidget autoSearchBtn;
    private ButtonWidget notificationsBtn;

    protected AnchorMacroConfigScreen(Screen parent) {
        super(Text.literal("Anchor Macro Settings"));
        this.parent = parent;
        this.cfg = AnchorMacroConfig.get();
    }

    @Override
    protected void init() {
        UI_WIDTH = MathHelper.clamp(this.width - 40, 220, 420);
        LEFT = (this.width - UI_WIDTH) / 2;
        SPACING = Math.max(22, this.textRenderer.fontHeight + 10);
        Y_START = 14;

        this.clearChildren();
        super.init();

        int y = Y_START;

        addHeader(LEFT, y, UI_WIDTH, "Anchor Macro Settings");
        y += SPACING + 4;

        addSectionHeader(LEFT, y, UI_WIDTH, "Delays (ticks)");
        y += SPACING;

        addDelaySlider(LEFT, y, "Place Anchor delay", cfg.delayPlaceAnchor, MIN_DELAY, MAX_DELAY, v -> cfg.delayPlaceAnchor = v);
        y += SPACING;
        addDelaySlider(LEFT, y, "Switch → Glowstone delay", cfg.delaySwitchToGlowstone, MIN_DELAY, MAX_DELAY, v -> cfg.delaySwitchToGlowstone = v);
        y += SPACING;
        addDelaySlider(LEFT, y, "Charge Anchor delay", cfg.delayChargeAnchor, MIN_DELAY, MAX_DELAY, v -> cfg.delayChargeAnchor = v);
        y += SPACING;
        addDelaySlider(LEFT, y, "Switch → Totem delay", cfg.delaySwitchToTotem, MIN_DELAY, MAX_DELAY, v -> cfg.delaySwitchToTotem = v);
        y += SPACING;
        addDelaySlider(LEFT, y, "Explode Anchor delay", cfg.delayExplodeAnchor, MIN_DELAY, MAX_DELAY, v -> cfg.delayExplodeAnchor = v);
        y += SPACING + 6;

        addSectionHeader(LEFT, y, UI_WIDTH, "Hotbar Slots (1–9)");
        y += SPACING;

        addSlotSlider(LEFT, y, "Anchor slot", cfg.anchorSlot, SLOT_MIN, SLOT_MAX, v -> cfg.anchorSlot = v);
        y += SPACING;
        addSlotSlider(LEFT, y, "Glowstone slot", cfg.glowstoneSlot, SLOT_MIN, SLOT_MAX, v -> cfg.glowstoneSlot = v);
        y += SPACING;
        addSlotSlider(LEFT, y, "Totem slot", cfg.totemSlot, SLOT_MIN, SLOT_MAX, v -> cfg.totemSlot = v);
        y += SPACING + 6;

        addSectionHeader(LEFT, y, UI_WIDTH, "Safety & Misc");
        y += SPACING;

        safeModeBtn = addToggleButton(LEFT, y, UI_WIDTH, "Safe Anchor Mode", cfg.safeAnchorMode, val -> cfg.safeAnchorMode = val);
        y += SPACING;

        explodeIfTotemBtn = addToggleButton(LEFT, y, UI_WIDTH, "Explode only if totem present", cfg.explodeOnlyIfTotemPresent, val -> cfg.explodeOnlyIfTotemPresent = val);
        y += SPACING;

        autoSearchBtn = addToggleButton(LEFT, y, UI_WIDTH, "Auto-search hotbar (1–9)", cfg.autoSearchHotbar, val -> cfg.autoSearchHotbar = val);
        y += SPACING;

        notificationsBtn = addToggleButton(LEFT, y, UI_WIDTH, "Show notifications", cfg.showNotifications, val -> cfg.showNotifications = val);
        y += SPACING + 6;

        int bottomY = this.height - 36;
        int btnW = MathHelper.clamp((UI_WIDTH - 40) / 3, 70, 140);
        int gap = (UI_WIDTH - (btnW * 3)) / 4;
        int x = LEFT + gap;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save"), b -> {
            clampAndSave();
            this.close();
        }).position(x, bottomY).size(btnW, 20).build());

        x += btnW + gap;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), b -> {
            AnchorMacroConfig.reload();
            this.close();
        }).position(x, bottomY).size(btnW, 20).build());

        x += btnW + gap;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), b -> {
            cfg.resetToDefaults();
            cfg.save();
            this.clearChildren();
            init();
        }).position(x, bottomY).size(btnW, 20).build());
    }

    private void addHeader(int left, int y, int width, String text) {
        ButtonWidget hdr = ButtonWidget.builder(Text.literal(text), b -> {}).position(left, y).size(width, 22).build();
        hdr.active = false;
        this.addDrawableChild(hdr);
    }

    private void addSectionHeader(int left, int y, int width, String text) {
        ButtonWidget hdr = ButtonWidget.builder(Text.literal(text), b -> {}).position(left, y).size(width, 18).build();
        hdr.active = false;
        this.addDrawableChild(hdr);
    }

    private void addDelaySlider(int left, int y, String label, int current, int minVal, int maxVal, IntConsumer setter) {
        final int min = minVal;
        final int max = (maxVal <= minVal) ? minVal + 1 : maxVal;
        final int startVal = clamp(current, min, max);
        final ButtonWidget lbl = ButtonWidget.builder(Text.literal(label + ": " + startVal), b -> {}).position(left, y).size(UI_WIDTH, 20).build();
        lbl.active = false;
        this.addDrawableChild(lbl);

        double normalized = (double)(startVal - min) / (double)(max - min);
        SliderWidget slider = new SliderWidget(left, y + 18, UI_WIDTH, 10, Text.literal(String.valueOf(startVal)), normalized) {
            private int last = startVal;
            @Override
            protected void applyValue() {
                int intVal = min + (int)Math.round(this.value * (max - min));
                intVal = clamp(intVal, min, max);
                if (intVal != last) {
                    last = intVal;
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

    private void addSlotSlider(int left, int y, String label, int current, int min, int max, IntConsumer setter) {
        addDelaySlider(left, y, label, current, min, max, setter);
    }

    private ButtonWidget addToggleButton(int left, int y, int width, String label, boolean current, Consumer<Boolean> setter) {
        final ButtonWidget[] ref = new ButtonWidget[1];
        ref[0] = ButtonWidget.builder(Text.literal(label + ": " + (current ? "ON" : "OFF")), b -> {
            boolean newVal = !getToggleValue(ref[0]);
            setter.accept(newVal);
            cfg.save();
            updateToggleLabel(ref[0], label, newVal);
        }).position(left, y).size(width, 20).build();
        this.addDrawableChild(ref[0]);
        return ref[0];
    }

    private boolean getToggleValue(ButtonWidget btn) {
        String msg = btn.getMessage().getString();
        return msg.toUpperCase().contains("ON");
    }

    private void updateToggleLabel(ButtonWidget btn, String label, boolean value) {
        btn.setMessage(Text.literal(label + ": " + (value ? "ON" : "OFF")));
    }

    private void clampAndSave() {
        cfg.delayPlaceAnchor = clamp(cfg.delayPlaceAnchor, MIN_DELAY, MAX_DELAY);
        cfg.delaySwitchToGlowstone = clamp(cfg.delaySwitchToGlowstone, MIN_DELAY, MAX_DELAY);
        cfg.delayChargeAnchor = clamp(cfg.delayChargeAnchor, MIN_DELAY, MAX_DELAY);
        cfg.delaySwitchToTotem = clamp(cfg.delaySwitchToTotem, MIN_DELAY, MAX_DELAY);
        cfg.delayExplodeAnchor = clamp(cfg.delayExplodeAnchor, MIN_DELAY, MAX_DELAY);

        cfg.anchorSlot = clamp(cfg.anchorSlot, SLOT_MIN, SLOT_MAX);
        cfg.glowstoneSlot = clamp(cfg.glowstoneSlot, SLOT_MIN, SLOT_MAX);
        cfg.totemSlot = clamp(cfg.totemSlot, SLOT_MIN, SLOT_MAX);
        cfg.save();
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        clampAndSave();
        this.client.setScreen(parent);
    }
    }
