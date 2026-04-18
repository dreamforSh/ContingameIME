package com.xinian.contingameime.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;

public abstract class Widget implements Renderable {
    protected final Font font;
    protected int offsetX = 0;
    protected int offsetY = 0;
    protected int textColor = 0xFF000000;
    protected int backgroundColor = 0xEBEBEBEB;

    public Widget(Font font) {
        this.font = font;
    }

    public boolean isActive() { return false; }
    public int getWidth() { return getPaddingX() * 2; }
    public int getHeight() { return getPaddingY() * 2; }
    public int getPaddingX() { return 0; }
    public int getPaddingY() { return 0; }
    public int getOffsetX() { return offsetX; }
    public int getOffsetY() { return offsetY; }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        if (isActive()) {
            draw(guiGraphics, offsetX, offsetY, mouseX, mouseY, delta);
        }
    }

    public void draw(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY, float delta) {
        guiGraphics.fill(offsetX, offsetY, offsetX + getWidth(), offsetY + getHeight(), backgroundColor);
    }

    public void moveTo(int x, int y) {
        this.offsetX = x;
        this.offsetY = y;
    }
}

