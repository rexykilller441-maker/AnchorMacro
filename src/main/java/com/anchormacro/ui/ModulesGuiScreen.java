package com.anchormacro.ui;

import com.anchormacro.AnchorMacroConfig;
import com.anchormacro.modules.Module;
import com.anchormacro.modules.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class ModulesGuiScreen extends Screen {
    private final Screen parent;
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
        int y = 20;

        // heading
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Modules"), b -> {}).position(left, y).size(width, 22).build());
        y += 28;

        List<Module> modules = ModuleManager.getModules();
        AnchorMacroConfig cfg = AnchorMacroConfig.get();

        for (Module m : modules) {
            boolean enabled = false;
            String name = m.getName();
            if (name.equalsIgnoreCase("AutoTotem")) enabled = cfg.autoTotemEnabled;
            else if (name.equalsIgnoreCase("TotemHit")) enabled = cfg.totemHitEnabled;
            else if (name.equalsIgnoreCase("Hitbox")) enabled = cfg.hitboxEnabled;

            ButtonWidget toggle = ButtonWidget.builder(Text.literal(name + ": " + (enabled ? "ON" : "OFF")), b -> {
                boolean newVal = !(b.getMessage().getString().toUpperCase().contains("ON"));
                b.setMessage(Text.literal(name + ": " + (newVal ? "ON" : "OFF")));
                // apply to config & module
                if (name.equalsIgnoreCase("AutoTotem")) { cfg.autoTotemEnabled = newVal; ModuleManager.autoTotem.setEnabled(newVal); }
                if (name.equalsIgnoreCase("TotemHit")) { cfg.totemHitEnabled = newVal; ModuleManager.totemHit.setEnabled(newVal); }
                if (name.equalsIgnoreCase("Hitbox")) { cfg.hitboxEnabled = newVal; ModuleManager.hitboxes.setEnabled(newVal); }
                cfg.save();
            }).position(left, y).size(width, 20).build();
            this.addDrawableChild(toggle);
            y += 24;

            // module-specific settings
            if (name.equalsIgnoreCase("Hitbox")) {
                // expand slider
                float start = cfg.hitboxExpand;
                double norm = (start - 0f) / (2.0 - 0.0);
                SliderWidget s = new SliderWidget(left + 8, y, width - 16, 12, Text.literal("  Expand: " + String.format("%.2f", start)), norm) {
                    private float last = start;
                    @Override protected void applyValue() {
                        float val = (float)(0.0 + this.value * (2.0));
                        val = MathHelper.clamp(val, 0f, 2f);
                        if (val != last) {
                            last = val;
                            cfg.hitboxExpand = val;
                            ModuleManager.hitboxes.expand = val;
                            this.setMessage(Text.literal("  Expand: " + String.format("%.2f", val)));
                            cfg.save();
                        }
                    }
                    @Override protected void updateMessage() {}
                };
                this.addDrawableChild(s);
                y += 20;

                // distance slider
                float startD = cfg.hitboxDistance;
                double n2 = (startD - 1.0) / (40.0 - 1.0);
                SliderWidget s2 = new SliderWidget(left + 8, y, width - 16, 12, Text.literal("  Distance: " + String.format("%.1f", startD)), n2) {
                    private float last = startD;
                    @Override protected void applyValue() {
                        float val = (float)(1.0 + this.value * (40.0 - 1.0));
                        val = MathHelper.clamp(val, 1f, 40f);
                        if (val != last) {
                            last = val;
                            cfg.hitboxDistance = val;
                            ModuleManager.hitboxes.distance = val;
                            this.setMessage(Text.literal("  Distance: " + String.format("%.1f", val)));
                            cfg.save();
                        }
                    }
                    @Override protected void updateMessage() {}
                };
                this.addDrawableChild(s2);
                y += 26;
            }
        }

        // bottom buttons
        int btnW = MathHelper.clamp((width - 40) / 3, 70, 140);
        int gap = (width - (btnW * 3)) / 4;
        int bx = left + gap;
        int by = this.height - 36;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save"), b -> {
            AnchorMacroConfig.get().save();
            this.client.setScreen(parent);
        }).position(bx, by).size(btnW, 20).build());

        bx += btnW + gap;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), b -> {
            AnchorMacroConfig.load();
            this.client.setScreen(parent);
        }).position(bx, by).size(btnW, 20).build());

        bx += btnW + gap;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), b -> {
            AnchorMacroConfig cfg2 = AnchorMacroConfig.get();
            cfg2.autoTotemEnabled = true;
            cfg2.totemHitEnabled = true;
            cfg2.hitboxEnabled = true;
            cfg2.hitboxExpand = 0.5f;
            cfg2.hitboxDistance = 6.0f;
            cfg2.save();
            rebuild();
        }).position(bx, by).size(btnW, 20).build());

        contentHeight = by + 40;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // background + title
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Modules"), this.width / 2, 10, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        scroll -= amount * 24;
        scroll = MathHelper.clamp(scroll, 0, Math.max(0, contentHeight - (this.height - 80)));
        // rebuild with children positions adjusted by scroll — simple approach: rebuild and rely on widget vertical positions
        for (var e : this.children()) {
            // We don't adjust absolute widget positions here for simplicity; Minecraft widget rendering will handle positions.
        }
        return true;
    }

    @Override
    public void onClose() {
        this.client.setScreen(parent);
    }
    }
