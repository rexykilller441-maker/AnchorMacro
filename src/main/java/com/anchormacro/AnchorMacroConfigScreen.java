package com.anchormacro;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.function.IntConsumer;
import java.util.function.Consumer;

/**
 * Modern, light-dark themed config screen:
 * - Updates toggle labels in-place (no full init rebuild)
 * - Sliders are clamped and safe (min < max)
 * - Layout scales to screen width and height, avoiding overlap on small screens (Zalith)
 * - Persists config on Save; auto-saves on toggle change
 */
public class AnchorMacroConfigScreen extends Screen {
    private final Screen parent;
    private final AnchorMacroConfig cfg;

    // UI dynamic sizing
    private int UI_WIDTH;
    private int LEFT;
    private int Y_START;
    private int SPACING;

    // Slider bounds
    private final int MIN_DELAY = 0;
    private final int MAX_DELAY = 200;
    private final int SLOT_MIN = 1;
    private final int SLOT_MAX = 9;

    // Toggle buttons (keep references so we update label in-place)
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
        // prepare dynamic layout based on current screen size
        UI_WIDTH = MathHelper.clamp(this.width - 40, 220, 420); // responsive: min 220, max 420
        LEFT = (this.width - UI_WIDTH) / 2;
        SPACING = Math.max(22, this.textRenderer.fontHeight + 10);
        Y_START = 14;

        // clear previous children (safe) and call super.init for internal setup
        this.clearChildren();
        super.init();

        int y = Y_START;

        // Title (disabled, looks like a header)
        addHeader(LEFT, y, UI_WIDTH, "Anchor Macro Settings");
        y += SPACING + 4;

        // Delays group
        addSectionHeader(LEFT, y, UI_WIDTH, "Delays (ticks)");
        y += SPACING;
        addDelaySlider(LEFT, y, "Place Anchor delay", clamp(cfg.delayPlaceAnchor, MIN_DELAY, MAX_DELAY),
                MIN_DELAY, MAX_DELAY, v -> cfg.delayPlaceAnchor = v);
        y += SPACING;
        addDelaySlider(LEFT, y, "Switch → Glowstone delay", clamp(cfg.delaySwitchToGlowstone, MIN_DELAY, MAX_DELAY),
                MIN_DELAY, MAX_DELAY, v -> cfg.delaySwitchToGlowstone = v);
        y += SPACING;
        addDelaySlider(LEFT, y, "Charge Anchor delay", clamp(cfg.delayChargeAnchor, MIN_DELAY, MAX_DELAY),
                MIN_DELAY, MAX_DELAY, v -> cfg.delayChargeAnchor = v);
        y += SPACING;
        addDelaySlider(LEFT, y, "Switch → Totem delay", clamp(cfg.delaySwitchToTotem, MIN_DELAY, MAX_DELAY),
                MIN_DELAY, MAX_DELAY, v -> cfg.delaySwitchToTotem = v);
        y += SPACING;
        addDelaySlider(LEFT, y, "Explode Anchor delay", clamp(cfg.delayExplodeAnchor, MIN_DELAY, MAX_DELAY),
                MIN_DELAY, MAX_DELAY, v -> cfg.delayExplodeAnchor = v);
        y += SPACING + 6;

        // Slots group
        addSectionHeader(LEFT, y, UI_WIDTH, "Hotbar Slots (1–9)");
        y += SPACING;
        addSlotSlider(LEFT, y, "Anchor slot", clamp(cfg.anchorSlot, SLOT_MIN, SLOT_MAX),
                SLOT_MIN, SLOT_MAX, v -> cfg.anchorSlot = v);
        y += SPACING;
        addSlotSlider(LEFT, y, "Glowstone slot", clamp(cfg.glowstoneSlot, SLOT_MIN, SLOT_MAX),
                SLOT_MIN, SLOT_MAX, v -> cfg.glowstoneSlot = v);
        y += SPACING;
        addSlotSlider(LEFT, y, "Totem slot", clamp(cfg.totemSlot, SLOT_MIN, SLOT_MAX),
                SLOT_MIN, SLOT_MAX, v -> cfg.totemSlot = v);
        y += SPACING + 6;

        // Toggles group (we create and keep references; on press update label only)
        addSectionHeader(LEFT, y, UI_WIDTH, "Safety & Misc");
        y += SPACING;

        safeModeBtn = addToggleButton(LEFT, y, UI_WIDTH, "Safe Anchor Mode", cfg.safeAnchorMode, newVal -> {
            cfg.safeAnchorMode = newVal;
            cfg.save();
            updateToggleLabel(safeModeBtn, "Safe Anchor Mode", newVal);
        });
        y += SPACING;

        explodeIfTotemBtn = addToggleButton(LEFT, y, UI_WIDTH, "Explode only if totem present", cfg.explodeOnlyIfTotemPresent, newVal -> {
            cfg.explodeOnlyIfTotemPresent = newVal;
            cfg.save();
            updateToggleLabel(explodeIfTotemBtn, "Explode only if totem present", newVal);
        });
        y += SPACING;

        autoSearchBtn = addToggleButton(LEFT, y, UI_WIDTH, "Auto-search hotbar (1–9)", cfg.autoSearchHotbar, newVal -> {
            cfg.autoSearchHotbar = newVal;
            cfg.save();
            updateToggleLabel(autoSearchBtn, "Auto-search hotbar (1–9)", newVal);
        });
        y += SPACING;

        notificationsBtn = addToggleButton(LEFT, y, UI_WIDTH, "Show notifications", cfg.showNotifications, newVal -> {
            cfg.showNotifications = newVal;
            cfg.save();
            updateToggleLabel(notificationsBtn, "Show notifications", newVal);
        });
        y += SPACING + 6;

        // Bottom row: Save / Cancel / Reset
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
            // Update widgets in-place: simple solution is to re-init UI
            // but to avoid overlap we clearChildren first then init again
            this.clearChildren();
            init();
        }).position(x, bottomY).size(btnW, 20).build());
    }

    // --- UI helpers ---

    private void addHeader(int left, int y, int width, String text) {
        // slightly larger header
        ButtonWidget hdr = ButtonWidget.builder(Text.literal(text), b -> {}).position(left, y).size(width, 22).build();
        hdr.active = false;
        this.addDrawableChild(hdr);
    }

    private void addSectionHeader(int left, int y, int width, String text) {
        ButtonWidget hdr = ButtonWidget.builder(Text.literal(text), b -> {}).position(left, y).size(width, 18).build();
        hdr.active = false;
        this.addDrawableChild(hdr);
    }

    private void addDelaySlider(int left, int y, String label, int current, int min, int max, IntConsumer setter) {
        // enforce min < max
        if (max <= min) max = min + 1;
        int val = clamp(current, min, max);

        // label (read-only)
        ButtonWidget lbl = ButtonWidget.builder(Text.literal(label + ": " + val), b -> {}).position(left, y).size(UI_WIDTH, 20).build();
        lbl.active = false;
        this.addDrawableChild(lbl);

        double normalized = (double)(val - min) / (double)(max - min);
        SliderWidget slider = new SliderWidget(left, y + 18, UI_WIDTH, 10, Text.literal(String.valueOf(val)), normalized) {
            private int last = val;
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
        // same as addDelaySlider but integer slot semantics (1..9 shown)
        addDelaySlider(left, y, label, current, min, max, v -> {
            // clamp slot and forward
            int clamped = clamp(v, min, max);
            setter.accept(clamped);
        });
    }

    private ButtonWidget addToggleButton(int left, int y, int width, String label, boolean current, Consumer<Boolean> changeAction) {
        ButtonWidget btn = ButtonWidget.builder(Text.literal(label + ": " + (current ? "ON" : "OFF")), b -> {
            boolean newVal = !getToggleValue(btn, label);
            // apply change
            changeAction.accept(newVal);
            // update the button text in-place to avoid a full rebuild
            updateToggleLabel(btn, label, newVal);
        }).position(left, y).size(width, 20).build();
        this.addDrawableChild(btn);
        return btn;
    }

    // helper to read current visible label state from button
    private boolean getToggleValue(ButtonWidget btn, String label) {
        String msg = btn.getMessage().getString();
        return msg.toUpperCase().contains("ON");
    }

    private void updateToggleLabel(ButtonWidget btn, String label, boolean value) {
        btn.setMessage(Text.literal(label + ": " + (value ? "ON" : "OFF")));
    }

    private void clampAndSave() {
        // clamp delays
        cfg.delayPlaceAnchor = clamp(cfg.delayPlaceAnchor, MIN_DELAY, MAX_DELAY);
        cfg.delaySwitchToGlowstone = clamp(cfg.delaySwitchToGlowstone, MIN_DELAY, MAX_DELAY);
        cfg.delayChargeAnchor = clamp(cfg.delayChargeAnchor, MIN_DELAY, MAX_DELAY);
        cfg.delaySwitchToTotem = clamp(cfg.delaySwitchToTotem, MIN_DELAY, MAX_DELAY);
        cfg.delayExplodeAnchor = clamp(cfg.delayExplodeAnchor, MIN_DELAY, MAX_DELAY);

        // clamp slots 1..9
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
        // subtle light-dark feel: leave background handling to DrawContext
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        clampAndSave();
        this.client.setScreen(parent);
    }
            }
