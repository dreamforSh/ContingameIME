package com.xinian.contingameime.mixin;

import com.xinian.contingameime.client.handler.ConfigHandler;
import com.xinian.contingameime.client.handler.IMEHandler;
import com.xinian.contingameime.client.handler.ScreenHandler;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class MixinChatScreen {
    @Shadow
    protected EditBox input;

    @Inject(method = "moveInHistory", at = @At("RETURN"))
    private void onHistoryMove(int i, CallbackInfo ci) {
        checkCommandMode();
    }

    /**
     * Detect command format in real-time as user types.
     * If the text starts with '/', disable IME; otherwise enable it.
     */
    @Inject(method = "keyPressed", at = @At("RETURN"))
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfo ci) {
        checkCommandMode();
    }

    @Inject(method = "onEdited", at = @At("RETURN"))
    private void onEdited(String text, CallbackInfo ci) {
        checkCommandMode();
    }

    private void checkCommandMode() {
        if (!ConfigHandler.isDisableIMEInCommandMode()) return;
        if (input.getValue().startsWith("/")) {
            IMEHandler.IMEState.COMPANION.onEditState(ScreenHandler.EditState.NULL_EDIT);
        } else {
            IMEHandler.IMEState.COMPANION.onEditState(ScreenHandler.EditState.EDIT_OPEN);
        }
    }
}

