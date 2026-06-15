package com.xinian.contingameime.client.gui;

import city.windmill.ingameime.client.jni.ExternalBaseIME;
import com.xinian.contingameime.client.gui.widget.AlphaModeWidget;
import com.xinian.contingameime.client.gui.widget.CandidateListWidget;
import com.xinian.contingameime.client.gui.widget.CompositionWidget;
import com.xinian.contingameime.client.gui.widget.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;

public class OverlayScreen implements Renderable {
    public static final OverlayScreen INSTANCE = new OverlayScreen();

    private AlphaModeWidget alphaModeWidget;
    private CompositionWidget compositionWidget;
    private CandidateListWidget candidateListWidget;
    private boolean initialized = false;
    private boolean indicatorEnabled = true;

    private int caretX = 0;
    private int caretY = 0;

    private OverlayScreen() {}

    /**
     * Enable or disable the IME mode indicator (the "A" / "ENG" overlay badge).
     */
    public void setIndicatorEnabled(boolean value) {
        indicatorEnabled = value;
        if (!value && initialized) {
            alphaModeWidget.setActive(false);
        }
    }

    /**
     * Lazily initialize widgets - must be called on the render thread after Minecraft is ready.
     */
    private void ensureInitialized() {
        if (initialized) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.font == null) return;
        alphaModeWidget = new AlphaModeWidget(mc.font);
        compositionWidget = new CompositionWidget(mc.font);
        candidateListWidget = new CandidateListWidget(mc.font);
        initialized = true;
    }

    public void setCaretPos(int x, int y) {
        if (caretX == x && caretY == y) return;
        caretX = x;
        caretY = y;
        ensureInitialized();
        if (!initialized) return;
        adjustCompositionPos();
        // Reposition all dependent widgets so they follow the caret while active.
        adjustPosByComposition(alphaModeWidget);
        adjustPosByComposition(candidateListWidget);
    }

    public void setShowAlphaMode(boolean value) {
        ensureInitialized();
        if (!initialized) return;
        if (!indicatorEnabled) {
            alphaModeWidget.setActive(false);
            return;
        }
        alphaModeWidget.setActive(value);
        adjustPosByComposition(alphaModeWidget);
    }

    public void setCandidates(String[] candidates) {
        ensureInitialized();
        if (!initialized) return;
        candidateListWidget.setCandidates(candidates);
        adjustPosByComposition(candidateListWidget);
    }

    public void setComposition(String text, int caret) {
        ensureInitialized();
        if (!initialized) return;
        compositionWidget.setCompositionData(text, caret);
        adjustCompositionPos();
        adjustPosByComposition(candidateListWidget);
    }

    public int[] getCompositionExt() {
        ensureInitialized();
        if (!initialized) return new int[]{0, 0, 0, 0};
        double scale = Minecraft.getInstance().getWindow().getGuiScale();
        return new int[]{
                (int) (compositionWidget.getOffsetX() * scale),
                (int) (compositionWidget.getOffsetY() * scale),
                (int) ((compositionWidget.getOffsetX() + compositionWidget.getWidth()) * scale),
                (int) ((compositionWidget.getOffsetY() + compositionWidget.getHeight()) * scale)
        };
    }

    public boolean isComposing() {
        return initialized && compositionWidget.getCompositionText() != null;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        ensureInitialized();
        if (!initialized) return;
        if (ExternalBaseIME.INSTANCE.getState()) {
            var poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.translate(0.0, 0.0, 500.0);
            compositionWidget.render(guiGraphics, mouseX, mouseY, delta);
            alphaModeWidget.render(guiGraphics, mouseX, mouseY, delta);
            candidateListWidget.render(guiGraphics, mouseX, mouseY, delta);
            poseStack.popPose();
        }
    }

    private void adjustCompositionPos() {
        var window = Minecraft.getInstance().getWindow();
        int maxX = window.getGuiScaledWidth() - compositionWidget.getWidth();
        int maxY = window.getGuiScaledHeight() - compositionWidget.getHeight() + compositionWidget.getPaddingY();
        compositionWidget.moveTo(
                Math.min(caretX, maxX),
                Math.min(caretY - compositionWidget.getPaddingY(), maxY)
        );
    }

    private void adjustPosByComposition(Widget widget) {
        if (!widget.isActive()) return;
        var window = Minecraft.getInstance().getWindow();
        int x = Math.min(compositionWidget.getOffsetX(), Math.max(0, window.getGuiScaledWidth() - widget.getWidth()));
        int y = compositionWidget.getOffsetY() + compositionWidget.getHeight();
        if (y > window.getGuiScaledHeight() - widget.getHeight()) {
            y = compositionWidget.getOffsetY() - widget.getHeight();
        }
        widget.moveTo(x, y);
    }
}
