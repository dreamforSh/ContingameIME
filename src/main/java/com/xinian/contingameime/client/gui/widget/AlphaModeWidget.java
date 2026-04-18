package com.xinian.contingameime.client.gui.widget;

import city.windmill.ingameime.client.jni.ExternalBaseIME;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AlphaModeWidget extends Widget {
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "ContingameIME-AlphaMode");
        t.setDaemon(true);
        return t;
    });

    private boolean active = false;
    private ScheduledFuture<?> hideDelay;

    public AlphaModeWidget(Font font) {
        super(font);
    }

    private String getText() {
        return I18n.get(ExternalBaseIME.INSTANCE.getAlphaMode() ? "alpha.contingameime.mode" : "native.contingameime.mode");
    }

    @Override
    public boolean isActive() { return active; }

    public void setActive(boolean value) {
        if (hideDelay != null) hideDelay.cancel(false);
        if (value) {
            hideDelay = SCHEDULER.schedule(() -> active = false, 3, TimeUnit.SECONDS);
        }
        active = value;
    }

    @Override
    public int getWidth() {
        int w = super.getWidth() + font.width(getText());
        return Math.max(w, getHeight());
    }

    @Override
    public int getHeight() { return super.getHeight() + font.lineHeight; }

    @Override
    public int getPaddingX() { return 2; }

    @Override
    public int getPaddingY() { return 3; }

    @Override
    public void draw(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY, float delta) {
        super.draw(guiGraphics, offsetX, offsetY, mouseX, mouseY, delta);
        String text = getText();
        int tx = offsetX + getWidth() / 2 - font.width(text) / 2;
        int ty = offsetY + getPaddingY();
        guiGraphics.drawString(font, text, tx, ty, textColor, false);
    }
}

