package com.anchormacro.ui;

import com.anchormacro.modules.Module;
import com.anchormacro.modules.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.List;

/**
 * Modern single-panel modules GUI. Scrollable with mouse wheel.
 */
public class ModulesGuiScreen extends Screen {
    private final Screen parent;
    private int UI_WIDTH, LEFT;
    private int yStart;
    private int spacing;
    private int scrollOffset = 0;
    private int contentHeight = 0;

    public ModulesGuiScreen(Screen parent) {
        super(Text.literal("AnchorMacro Modules"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.clearChildren();

        UI_WIDTH = MathHelper.clamp(this.width - 40, 260, 520);
        LEFT = (this.width - UI_WIDTH) / 2;
        spacing = Math.max(22, this.textRenderer.fontHeight + 8);
        yStart = 16;
        scrollOffset = Math.max(0, Math.min(scrollOffset, 10000)); // keep sane

        rebuildWidgets();
    }

    private void rebuildWidgets() {
        this.clearChildren();

        int y = yStart - scrollOffset;
        addHeader(LEFT, y, UI_WIDTH, "Modules");
        y += spacing;

        List<Module> modules = ModuleManager.getModules();
        for (Module m : modules) {
            // toggle button
            boolean enabled = m.isEnabled();
            ButtonWidget toggle = ButtonWidget.builder(Text.literal(m.getName() + ": " + (enabled ? "ON" : "OFF")), b -> {
                boolean newVal = !m.isEnabled();
                m.setEnabled(newVal);
                // update label
                b.setMessage(Text.literal(m.getName() + ": " + (newVal ? "ON" : "OFF")));
            }).position(LEFT, y).size(UI_WIDTH, 20).build();

            this.addDrawableChild(toggle);
            y += spacing;

            // module specific small UI for Hitbox
            if (m.getName().equalsIgnoreCase("Hitbox") || m.getName().equalsIgnoreCase("Hitboxes")) {
                // render / expand slider
                final com.anchormacro.modules.combat.Hitboxes hb = (com.anchormacro.modules.combat.Hitboxes) m;
                ButtonWidget lbl1 = ButtonWidget.builder(Text.literal("  Render: " + (hb.render ? "ON" : "OFF")), b -> {
                    hb.render = !hb.render;
                    b.setMessage(Text.literal("  Render: " + (hb.render ? "ON" : "OFF")));
                }).position(LEFT + 8, y).size(UI_WIDTH - 16, 18).build();
                this.addDrawableChild(lbl1);
                y += spacing - 6;

                // expand slider
                double normalized = (hb.expand - 0.0) / (2.0 - 0.0); // map 0..2
                SliderWidget slider = new SliderWidget(LEFT + 8, y, UI_WIDTH - 16, 10, Text.literal("  Expand: " + hb.expand), normalized) {
                    private float last = hb.expand;
                    @Override
                    protected void applyValue() {
                        float val = (float)(0.0 + this.value * (2.0));
                        val = MathHelper.clamp(val, 0f, 2f);
                        if (val != last) {
                            last = val;
                            hb.expand = val;
                            this.setMessage(Text.literal("  Expand: " + String.format("%.2f", val)));
                        }
                    }
                    @Override
                    protected void updateMessage() {}
                };
                this.addDrawableChild(slider);
                y += spacing;

                // distance slider
                double norm2 = (hb.distance - 1.0) / (40.0 - 1.0);
                SliderWidget slider2 = new SliderWidget(LEFT + 8, y, UI_WIDTH - 16, 10, Text.literal("  Distance: " + hb.distance), norm2) {
                    private float last = hb.distance;
                    @Override
                    protected void applyValue() {
                        float val = (float)(1.0 + this.value * (40.0 - 1.0));
                        val = MathHelper.clamp(val, 1f, 40f);
                        if (val != last) {
                            last = val;
                            hb.distance = val;
                            this.setMessage(Text.literal("  Distance: " + String.format("%.1f", val)));
                        }
                    }
                    @Override
                    protected void updateMessage() {}
                };
                this.addDrawableChild(slider2);
                y += spacing;
            }
        }

        // bottom buttons
        int bottomY = this.height - 36 - scrollOffset;
        int btnW = MathHelper.clamp((UI_WIDTH - 40) / 3, 70, 140);
        int gap = (UI_WIDTH - (btnW * 3)) / 4;
        int x = LEFT + gap;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save"), b -> {
            // attempt to persist settings if AnchorMacroConfig exists (if present it should have save())
            try {
                com.anchormacro.AnchorMacroConfig.get().save();
            } catch (Throwable ignored) {}
            this.client.setScreen(parent);
        }).position(x, bottomY).size(btnW, 20).build());

        x += btnW + gap;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), b -> {
            try { com.anchormacro.AnchorMacroConfig.reload(); } catch (Throwable ignored) {}
            this.client.setScreen(parent);
        }).position(x, bottomY).size(btnW, 20).build());

        x += btnW + gap;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), b -> {
            try { com.anchormacro.AnchorMacroConfig.get().resetToDefaults(); com.anchormacro.AnchorMacroConfig.get().save(); } catch (Throwable ignored) {}
            rebuildWidgets();
        }).position(x, bottomY).size(btnW, 20).build());

        // compute content height (for wheel bounds)
        contentHeight = y + scrollOffset;
    }

    private void addHeader(int left, int y, int width, String text) {
        ButtonWidget hdr = ButtonWidget.builder(Text.literal(text), b -> {}).position(left, y).size(width, 22).build();
        hdr.active = false;
        this.addDrawableChild(hdr);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        // Draw children with scissor-like behavior by offsetting y positions; we rebuild on scroll
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        // scroll amount positive = up, negative = down
        scrollOffset -= (int)(amount * 20);
        scrollOffset = Math.max(0, Math.min(scrollOffset, Math.max(0, contentHeight - (this.height - 80))));
        rebuildWidgets();
        return true;
    }

    @Override
    public void onClose() {
        this.client.setScreen(parent);
    }
                                            }
