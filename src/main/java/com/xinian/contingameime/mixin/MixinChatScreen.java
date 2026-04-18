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
        if (input.getValue().startsWith("/") && ConfigHandler.isDisableIMEInCommandMode()) {
            IMEHandler.IMEState.COMPANION.onEditState(ScreenHandler.EditState.NULL_EDIT);
        } else {
            IMEHandler.IMEState.COMPANION.onEditState(ScreenHandler.EditState.EDIT_OPEN);
        }
    }
}

