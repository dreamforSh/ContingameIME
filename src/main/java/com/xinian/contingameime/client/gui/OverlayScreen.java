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

    private final AlphaModeWidget alphaModeWidget;
    private final CompositionWidget compositionWidget;
    private final CandidateListWidget candidateListWidget;

    private int caretX = 0;
    private int caretY = 0;

    private OverlayScreen() {
        alphaModeWidget = new AlphaModeWidget(Minecraft.getInstance().font);
        compositionWidget = new CompositionWidget(Minecraft.getInstance().font);
        candidateListWidget = new CandidateListWidget(Minecraft.getInstance().font);
    }

    public void setCaretPos(int x, int y) {
        if (caretX == x && caretY == y) return;
        caretX = x;
        caretY = y;
        adjustCompositionPos();
        adjustPosByComposition(alphaModeWidget);
    }

    public void setShowAlphaMode(boolean value) {
        alphaModeWidget.setActive(value);
        adjustPosByComposition(alphaModeWidget);
    }

    public void setCandidates(String[] candidates) {
        candidateListWidget.setCandidates(candidates);
        adjustPosByComposition(candidateListWidget);
    }

    public void setComposition(String text, int caret) {
        compositionWidget.setCompositionData(text, caret);
        adjustCompositionPos();
        adjustPosByComposition(candidateListWidget);
    }

    public int[] getCompositionExt() {
        double scale = Minecraft.getInstance().getWindow().getGuiScale();
        return new int[]{
                (int) (compositionWidget.getOffsetX() * scale),
                (int) (compositionWidget.getOffsetY() * scale),
                (int) ((compositionWidget.getOffsetX() + compositionWidget.getWidth()) * scale),
                (int) ((compositionWidget.getOffsetY() + compositionWidget.getHeight()) * scale)
        };
    }

    public boolean isComposing() {
        return compositionWidget.getCompositionText() != null;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
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

