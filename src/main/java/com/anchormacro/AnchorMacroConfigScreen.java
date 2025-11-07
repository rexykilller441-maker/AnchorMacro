package com.anchormacro;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class AnchorMacroConfigScreen extends Screen {
    private final Screen parent;
    private final List<ButtonWidget> buttons = new ArrayList<>();
    private double scrollOffset = 0;
    private double scrollVelocity = 0;
    private static final int ENTRY_HEIGHT = 28;

    private boolean anchorMacroEnabled = true;
    private boolean autoTotemEnabled = false;
    private boolean totemHitEnabled = false;
    private boolean hitboxEnabled = false;

    public AnchorMacroConfigScreen(Screen parent) {
        super(Text.of("Anchor Macro Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        buttons.clear();
        int y = 40;

        buttons.add(makeToggleButton(width / 2 - 100, y, "Anchor Macro", anchorMacroEnabled, b -> {
            anchorMacroEnabled = !anchorMacroEnabled;
            b.setMessage(Text.of("Anchor Macro: " + (anchorMacroEnabled ? "§aON" : "§cOFF")));
        }));

        y += ENTRY_HEIGHT;
        buttons.add(makeToggleButton(width / 2 - 100, y, "Auto Totem", autoTotemEnabled, b -> {
            autoTotemEnabled = !autoTotemEnabled;
            b.setMessage(Text.of("Auto Totem: " + (autoTotemEnabled ? "§aON" : "§cOFF")));
        }));

        y += ENTRY_HEIGHT;
        buttons.add(makeToggleButton(width / 2 - 100, y, "Totem Hit", totemHitEnabled, b -> {
            totemHitEnabled = !totemHitEnabled;
            b.setMessage(Text.of("Totem Hit: " + (totemHitEnabled ? "§aON" : "§cOFF")));
        }));

        y += ENTRY_HEIGHT;
        buttons.add(makeToggleButton(width / 2 - 100, y, "Hitbox", hitboxEnabled, b -> {
            hitboxEnabled = !hitboxEnabled;
            b.setMessage(Text.of("Hitbox: " + (hitboxEnabled ? "§aON" : "§cOFF")));
        }));

        y += ENTRY_HEIGHT + 10;
        buttons.add(ButtonWidget.builder(Text.of("Back"), btn -> close()).dimensions(width / 2 - 100, y, 200, 20).build());

        addDrawableChilds();
    }

    private ButtonWidget makeToggleButton(int x, int y, String label, boolean initialState, ButtonWidget.PressAction onPress) {
        ButtonWidget button = ButtonWidget.builder(Text.of(label + ": " + (initialState ? "§aON" : "§cOFF")), onPress)
                .dimensions(x, y, 200, 20).build();
        return button;
    }

    private void addDrawableChilds() {
        for (ButtonWidget b : buttons) {
            addDrawableChild(b);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollVelocity += verticalAmount * 10;
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        scrollOffset += scrollVelocity;
        scrollVelocity *= 0.8;

        scrollOffset = Math.max(Math.min(scrollOffset, 0), -Math.max(0, buttons.size() * ENTRY_HEIGHT - height + 100));

        renderBackground(context);
        drawCenteredTextWithShadow(context, textRenderer, "Anchor Macro Settings", width / 2, 15, 0xFFFFFF);

        int startY = (int) (40 + scrollOffset);

        for (int i = 0; i < buttons.size(); i++) {
            ButtonWidget b = buttons.get(i);
            int y = startY + i * ENTRY_HEIGHT;
            if (y + 20 < 30 || y > height - 30) continue; // skip offscreen

            b.setY(y);
            b.render(context, mouseX, mouseY, delta);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        scrollOffset -= deltaY;
        return true;
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }
}
