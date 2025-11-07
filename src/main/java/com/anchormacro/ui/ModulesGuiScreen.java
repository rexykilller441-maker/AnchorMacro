package com.anchormacro.ui;

import com.anchormacro.modules.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

public class ModulesGuiScreen extends Screen {
    private final List<ButtonWidget> moduleButtons = new ArrayList<>();
    private double scroll = 0;

    public ModulesGuiScreen() {
        super(Text.literal("Modules"));
    }

    @Override
    protected void init() {
        moduleButtons.clear();

        int y = 40;
        int spacing = 25;

        // Dynamically load all modules
        for (String moduleName : ModuleManager.getAllModules()) {
            ButtonWidget btn = ButtonWidget.builder(
                Text.literal(moduleName),
                b -> ModuleManager.toggle(moduleName)
            ).dimensions(width / 2 - 80, y, 160, 20).build();

            moduleButtons.add(addDrawableChild(btn));
            y += spacing;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, "Modules", width / 2, 15, 0xFFFFFF);

        context.getMatrices().push();
        context.getMatrices().translate(0, -scroll, 0);

        for (ButtonWidget btn : moduleButtons) {
            btn.render(context, mouseX, mouseY, delta);
        }

        context.getMatrices().pop();
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        scroll -= amount * 10;
        scroll = Math.max(0, scroll);
        return true;
    }
}
