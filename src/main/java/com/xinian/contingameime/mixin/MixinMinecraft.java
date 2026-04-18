package com.xinian.contingameime.mixin;

import city.windmill.ingameime.client.jni.ExternalBaseIME;
import com.mojang.blaze3d.platform.Window;
import com.xinian.contingameime.client.event.ClientScreenEventHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Shadow
    public Screen screen;
    @Final
    @Shadow
    private Window window;

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void onScreenChange(Screen screenIn, CallbackInfo info) {
        ClientScreenEventHooks.fireScreenChanged(screen, screenIn);
    }

    @Inject(method = "resizeDisplay", at = @At("RETURN"))
    private void onScreenSizeChanged(CallbackInfo info) {
        ClientScreenEventHooks.fireWindowSizeChanged(window.getWidth(), window.getHeight());
    }

    /**
     * Sync IME state when window regains focus.
     * Ported from IMBlocker's MinecraftClientMixin.
     */
    @Inject(method = "setWindowActive", at = @At("HEAD"))
    private void onWindowFocusChanged(boolean focused, CallbackInfo ci) {
        if (focused) {
            try {
                ExternalBaseIME.INSTANCE.setFullScreen(
                    Minecraft.getInstance().getWindow().isFullscreen()
                );
            } catch (Exception ignored) {}
        }
    }
}
