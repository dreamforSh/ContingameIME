package com.xinian.contingameime.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class CompositionWidget extends Widget {
    private String compositionText = null;
    private int compositionCaret = 0;
    private static final int CARET_WIDTH = 3;

    public CompositionWidget(Font font) {
        super(font);
    }

    public String getCompositionText() { return compositionText; }
    public int getCompositionCaret() { return compositionCaret; }

    public void setCompositionData(String text, int caret) {
        this.compositionText = text;
        this.compositionCaret = caret;
    }

    @Override
    public boolean isActive() { return compositionText != null; }

    @Override
    public int getWidth() {
        return super.getWidth() + font.width(compositionText != null ? compositionText : "") + CARET_WIDTH;
    }

    @Override
    public int getHeight() { return super.getHeight() + font.lineHeight; }

    @Override
    public int getPaddingX() { return 1; }

    @Override
    public int getPaddingY() { return 1; }

    @Override
    public void draw(GuiGraphics guiGraphics, int ox, int oy, int mouseX, int mouseY, float delta) {
        if (compositionText == null) return;

        super.draw(guiGraphics, ox, oy, mouseX, mouseY, delta);

        String part1 = compositionText.substring(0, compositionCaret);
        String part2 = compositionText.substring(compositionCaret);

        int dx = ox + getPaddingX();
        int dy = oy + getPaddingY();
        dx = guiGraphics.drawString(font, part1, dx, dy, textColor, false);
        // Caret blink 0.5s
        if ((System.currentTimeMillis() % 1000) > 500) {
            guiGraphics.fill(dx + 1, dy, dx + 2, dy + font.lineHeight, textColor);
        }
        dx += CARET_WIDTH;
        guiGraphics.drawString(font, part2, dx, dy, textColor, false);
    }
}

