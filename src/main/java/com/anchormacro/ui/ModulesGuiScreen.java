package com.anchormacro.ui;

import com.anchormacro.AnchorMacroConfig;
import com.anchormacro.modules.Module;
import com.anchormacro.modules.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.List;

/**
 * Separate Modules GUI (scrollable). Opens from main config screen.
 */
public class ModulesGuiScreen extends Screen {
    private final Screen parent;
    private int contentTop = 20;
    private double scroll = 0;
    private int contentHeight = 0;

    public ModulesGuiScreen(Screen parent) {
        super(Text.literal("Modules"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        rebuild();
    }

    private void rebuild() {
        this.clearChildren();
        int left = 30;
        int width = this.width - 60;
        int y = contentTop;

        // header (non-clickable)
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Modules"), b -> {}).position(left, y).size(width, 22).build());
        y += 28;

        List<Module> modules = ModuleManager.getModules();
        AnchorMacroConfig cfg = new AnchorMacroConfig();

        for (Module m : modules) {
            String name = m.getName();
            boolean enabled = m.isEnabled();

            ButtonWidget toggle = ButtonWidget.builder(Text.literal(name + ": " + (enabled ? "ON" : "OFF")), b -> {
                boolean newVal = !m.isEnabled();
                m.setEnabled(newVal);
                b.setMessage(Text.literal(name + ": " + (newVal ? "ON" : "OFF")));

                // persist to AnchorMacroConfig where appropriate
                if (name.equalsIgnoreCase("AutoTotem")) AnchorMacroConfig.autoSearchHotbar = newVal;
                if (name.equalsIgnoreCase("TotemHit")) AnchorMacroConfig.explodeOnlyIfTotemPresent = newVal;
                if (name.equalsIgnoreCase("Hitbox")) AnchorMacroConfig.hitboxEnabled = newVal;
                AnchorMacroConfig.save();
            }).position(left, y).size(width, 20).build();
            this.addDrawableChild(toggle);
            y += 24;

            // Hitbox settings UI (sliders)
            if (name.equalsIgnoreCase("Hitbox")) {
                com.anchormacro.modules.combat.Hitboxes hb = (com.anchormacro.modules.combat.Hitboxes) ModuleManager.getByName("Hitbox");
                if (hb != null) {
                    // expand slider
                    double normalized = (hb.expand - 0.0) / (2.0 - 0.0);
                    SliderWidget s1 = new SliderWidget(left + 8, y, width - 16, 12, Text.literal("  Expand: " + String.format("%.2f", hb.expand)), normalized) {
                        private float last = hb.expand;
                        @Override protected void applyValue() {
                            float val = (float)(0.0 + this.value * 2.0);
                            val = MathHelper.clamp(val, 0f, 2f);
                            if (val != last) {
                                last = val;
                                hb.expand = val;
                                AnchorMacroConfig.hitboxExpand = val;
                                this.setMessage(Text.literal("  Expand: " + String.format("%.2f", val)));
                                AnchorMacroConfig.save();
                            }
                        }
                        @Override protected void updateMessage() {}
                    };
                    this.addDrawableChild(s1);
                    y += 20;

                    // distance slider
                    double norm2 = (hb.distance - 1.0) / (40.0 - 1.0);
                    SliderWidget s2 = new SliderWidget(left + 8, y, width - 16, 12, Text.literal("  Distance: " + String.format("%.1f", hb.distance)), norm2) {
                        private float last = hb.distance;
                        @Override protected void applyValue() {
                            float val = (float)(1.0 + this.value * (40.0 - 1.0));
                            val = MathHelper.clamp(val, 1f, 40f);
                            if (val != last) {
                                last = val;
                                hb.distance = val;
                                AnchorMacroConfig.hitboxDistance = val;
                                this.setMessage(Text.literal("  Distance: " + String.format("%.1f", val)));
                                AnchorMacroConfig.save();
                            }
                        }
                        @Override protected void updateMessage() {}
                    };
                    this.addDrawableChild(s2);
                    y += 26;
                }
            }
        }

        // bottom controls
        int btnW = MathHelper.clamp((width - 40) / 3, 70, 140);
        int gap = (width - (btnW * 3)) / 4;
        int bx = left + gap;
        int by = this.height - 36;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save"), b -> {
            AnchorMacroConfig.save();
            this.client.setScreen(parent);
        }).position(bx, by).size(btnW, 20).build());

        bx += btnW + gap;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), b -> {
            AnchorMacroConfig.load();
            this.client.setScreen(parent);
        }).position(bx, by).size(btnW, 20).build());

        bx += btnW + gap;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), b -> {
            AnchorMacroConfig.load(); // reset file reloads defaults in your config design or you can set values here
            rebuild();
        }).position(bx, by).size(btnW, 20).build());

        contentHeight = y + 20;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // draw background + title
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Modules"), this.width / 2, 10, 0xFFFFFF);

        // apply scroll translation for children draw positions
        context.getMatrices().push();
        context.getMatrices().translate(0.0F, (float)-scroll, 0.0F);

        super.render(context, mouseX, mouseY, delta);

        context.getMatrices().pop();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        scroll += -amount * 24;
        scroll = MathHelper.clamp(scroll, 0, Math.max(0, contentHeight - (this.height - 80)));
        return true;
    }

    @Override
    public void onClose() {
        this.client.setScreen(parent);
    }
                        }
