package com.xinian.contingameime.mixin;

import com.xinian.contingameime.client.event.ClientScreenEventHooks;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EditBox.class)
abstract class MixinEditBox extends AbstractWidget {
    @Shadow
    private boolean bordered;

    @Shadow
    private int displayPos;

    private MixinEditBox(int i, int j, int k, int l, Component component) {
        super(i, j, k, l, component);
    }

    @Inject(method = "setFocused", at = @At("HEAD"))
    private void onSelected(boolean selected, CallbackInfo info) {
        int caretX = bordered ? this.getX() + 4 : this.getX();
        int caretY = bordered ? this.getY() + (this.height - 8) / 2 : this.getY();
        if (selected)
            ClientScreenEventHooks.fireEditOpen(this, caretX, caretY);
        else
            ClientScreenEventHooks.fireEditClose(this);
    }

    @Inject(method = "onClick", at = @At("HEAD"))
    private void onFocused(double d, double e, CallbackInfo ci) {
        int caretX = bordered ? this.getX() + 4 : this.getX();
        int caretY = bordered ? this.getY() + (this.height - 8) / 2 : this.getY();
        ClientScreenEventHooks.fireEditOpen(this, caretX, caretY);
    }

    @Inject(method = "renderWidget", at = @At("RETURN"))
    private void onRenderCaret(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.isFocused()) {
            EditBox self = (EditBox) (Object) this;
            int textX = bordered ? this.getX() + 4 : this.getX();
            int textY = bordered ? this.getY() + (this.height - 8) / 2 : this.getY();
            String value = self.getValue();
            int cursorPos = self.getCursorPosition();
            int clampedCursor = Math.min(cursorPos, value.length());
            int clampedDisplay = Math.min(displayPos, clampedCursor);
            String beforeCursor = value.substring(clampedDisplay, clampedCursor);
            int caretX = textX + Minecraft.getInstance().font.width(beforeCursor);
            ClientScreenEventHooks.fireEditCaret(this, caretX, textY);
        }
    }
}

