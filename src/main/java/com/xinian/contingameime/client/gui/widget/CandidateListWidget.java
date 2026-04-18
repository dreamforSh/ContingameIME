package com.xinian.contingameime.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class CandidateListWidget extends Widget {
    private String[] candidates = null;
    private final CandidateEntry drawItem;

    public CandidateListWidget(Font font) {
        super(font);
        this.drawItem = new CandidateEntry(font);
    }

    public String[] getCandidates() { return candidates; }

    public void setCandidates(String[] candidates) { this.candidates = candidates; }

    @Override
    public boolean isActive() { return candidates != null && candidates.length > 0; }

    @Override
    public int getWidth() {
        int w = super.getWidth();
        if (candidates != null) {
            for (String s : candidates) {
                drawItem.setText(s);
                w += drawItem.getWidth();
            }
        }
        return w;
    }

    @Override
    public int getHeight() { return super.getHeight() + font.lineHeight; }

    @Override
    public int getPaddingX() { return 1; }

    @Override
    public int getPaddingY() { return 3; }

    @Override
    public void draw(GuiGraphics guiGraphics, int ox, int oy, int mouseX, int mouseY, float delta) {
        if (candidates == null) return;
        super.draw(guiGraphics, ox, oy, mouseX, mouseY, delta);

        int dx = ox + getPaddingX();
        int dy = oy + getPaddingY();
        int index = 1;
        for (String str : candidates) {
            drawItem.setIndex(index);
            drawItem.setText(str);
            drawItem.draw(guiGraphics, dx, dy, mouseX, mouseY, delta);
            dx += drawItem.getWidth();
            index++;
        }
    }

    public static class CandidateEntry extends Widget {
        private String text = null;
        private int index = 0;
        private final int indexWidth;

        public CandidateEntry(Font font) {
            super(font);
            this.indexWidth = font.width("00") + 5;
        }

        public void setText(String text) { this.text = text; }
        public void setIndex(int index) { this.index = index; }

        @Override
        public int getWidth() {
            return super.getWidth() + font.width(text != null ? text : "") + indexWidth;
        }

        @Override
        public int getHeight() { return super.getHeight() + font.lineHeight; }

        @Override
        public int getPaddingX() { return 2; }

        @Override
        public int getPaddingY() { return 3; }

        @Override
        public void draw(GuiGraphics guiGraphics, int ox, int oy, int mouseX, int mouseY, float delta) {
            int dx = ox + getPaddingX();
            String indexStr = String.valueOf(index);
            int cx = dx + indexWidth / 2 - font.width(indexStr) / 2;
            guiGraphics.drawString(font, indexStr, cx, oy, textColor, false);
            dx += indexWidth;
            guiGraphics.drawString(font, text != null ? text : "", dx, oy, textColor, false);
        }
    }
}

