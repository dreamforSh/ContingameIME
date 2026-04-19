package com.xinian.contingameime.mixin;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.xinian.contingameime.client.handler.ScreenHandler;

/**
 * Ensure edit state is cleaned up when screens are removed.
 * Screen removal is primarily handled via ScreenHandler.onScreenChange -> NULL_SCREEN,
 * but this provides an additional safety net for edge cases.
 */
@Mixin({Screen.class, AbstractSignEditScreen.class})
class MixinScreen {
    @Inject(method = "removed", at = @At("TAIL"))
    private void onRemove(CallbackInfo info) {
        // Force close any open edit state as a safety net
        ScreenHandler.EditState.forceClose();
    }
}
