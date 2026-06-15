package com.xinian.contingameime.mixin;

import city.windmill.ingameime.client.jni.ExternalBaseIME;
import com.xinian.contingameime.client.handler.ConfigHandler;
import com.xinian.contingameime.client.handler.IMEHandler;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class MixinChatScreen {
    @Shadow
    protected EditBox input;

    @Unique
    private boolean contingameime$commandModeActive = false;

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

    /**
     * Detect command format and directly toggle the native IME state,
     * without interfering with the EditState state machine.
     */
    private void checkCommandMode() {
        if (!ConfigHandler.isDisableIMEInCommandMode()) return;
        boolean isCommand = input.getValue().startsWith("/");
        if (isCommand && !contingameime$commandModeActive) {
            contingameime$commandModeActive = true;
            ExternalBaseIME.INSTANCE.setState(false);
        } else if (!isCommand && contingameime$commandModeActive) {
            contingameime$commandModeActive = false;
            // Restore the IME state the user actually wants (e.g. keep it off if they
            // disabled it via the hotkey) instead of unconditionally forcing it on.
            boolean enabled =
                    IMEHandler.IMEState.COMPANION.getImeState() != IMEHandler.IMEState.DISABLED;
            ExternalBaseIME.INSTANCE.setState(enabled);
        }
    }
}
