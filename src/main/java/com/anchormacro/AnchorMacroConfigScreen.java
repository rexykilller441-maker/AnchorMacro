package com.anchormacro;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

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

        // === Delays ===
        addDelayControl("Place Anchor delay", y, () -> cfg.delayPlaceAnchor, v -> cfg.delayPlaceAnchor = v);
        y += spacing;
        addDelayControl("Switch → Glowstone delay", y, () -> cfg.delaySwitchToGlowstone, v -> cfg.delaySwitchToGlowstone = v);
        y += spacing;
        addDelayControl("Charge Anchor delay", y, () -> cfg.delayChargeAnchor, v -> cfg.delayChargeAnchor = v);
        y += spacing;
        addDelayControl("Switch → Totem delay", y, () -> cfg.delaySwitchToTotem, v -> cfg.delaySwitchToTotem = v);
        y += spacing;
        addDelayControl("Explode Anchor delay", y, () -> cfg.delayExplodeAnchor, v -> cfg.delayExplodeAnchor = v);
        y += spacing + 6;

        // === Slots ===
        addSlotControl("Anchor slot (0-8)", y, () -> cfg.anchorSlot, v -> cfg.anchorSlot = v);
        y += spacing;
        addSlotControl("Glowstone slot (0-8)", y, () -> cfg.glowstoneSlot, v -> cfg.glowstoneSlot = v);
        y += spacing;
        addSlotControl("Totem slot (0-8)", y, () -> cfg.totemSlot, v -> cfg.totemSlot = v);
        y += spacing + 6;

        // === Toggles ===
        this.addDrawableChild(new ButtonWidget(centerX - 120, y, 230, 20,
                Text.literal("Safe Anchor Mode: " + (cfg.safeAnchorMode ? "ON" : "OFF")),
                b -> { cfg.safeAnchorMode = !cfg.safeAnchorMode; init(); }));
        y += spacing;

        this.addDrawableChild(new ButtonWidget(centerX - 120, y, 230, 20,
                Text.literal("Explode only if totem present: " + (cfg.explodeOnlyIfTotemPresent ? "YES" : "NO")),
                b -> { cfg.explodeOnlyIfTotemPresent = !cfg.explodeOnlyIfTotemPresent; init(); }));
        y += spacing;

        // === Bottom buttons ===
        this.addDrawableChild(new ButtonWidget(centerX - 100, height - 40, 80, 20,
                Text.literal("Save"), b -> { cfg.save(); onClose(); }));

        this.addDrawableChild(new ButtonWidget(centerX - 5, height - 40, 80, 20,
                Text.literal("Cancel"), b -> {
                    AnchorMacroConfig.INSTANCE = null; // reload on next get()
                    onClose();
                }));

        this.addDrawableChild(new ButtonWidget(centerX + 90, height - 40, 80, 20,
                Text.literal("Reset"), b -> { cfg.resetToDefaults(); init(); }));
    }

    private void addDelayControl(String label, int y, SupplierInt getter, ConsumerInt setter) {
        int val = getter.get();
        this.addDrawableChild(makeLabel(centerX - 120, y, label + ": " + val + "t"));
        this.addDrawableChild(new ButtonWidget(centerX + 40, y - 6, 20, 20, Text.literal("-"),
                b -> { setter.accept(Math.max(0, val - 1)); init(); }));
        this.addDrawableChild(new ButtonWidget(centerX + 65, y - 6, 20, 20, Text.literal("+"),
                b -> { setter.accept(Math.min(200, val + 1)); init(); }));
    }

    private void addSlotControl(String label, int y, SupplierInt getter, ConsumerInt setter) {
        int val = getter.get();
        this.addDrawableChild(makeLabel(centerX - 120, y, label + ": " + val));
        this.addDrawableChild(new ButtonWidget(centerX + 40, y - 6, 20, 20, Text.literal("-"),
                b -> { setter.accept(clampSlot(val - 1)); init(); }));
        this.addDrawableChild(new ButtonWidget(centerX + 65, y - 6, 20, 20, Text.literal("+"),
                b -> { setter.accept(clampSlot(val + 1)); init(); }));
    }

    private ButtonWidget makeLabel(int x, int y, String text) {
        ButtonWidget b = new ButtonWidget(x, y, 230, 20, Text.literal(text), btn -> {});
        b.active = false;
        return b;
    }

    private int clampSlot(int v) {
        return Math.max(0, Math.min(8, v));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        drawCenteredText(matrices, this.textRenderer, this.title, centerX, 10, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    // tiny helper interfaces (since we can't use lambdas with primitive ints easily)
    @FunctionalInterface private interface SupplierInt { int get(); }
    @FunctionalInterface private interface ConsumerInt { void accept(int v); }
}
