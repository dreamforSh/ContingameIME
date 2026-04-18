package com.xinian.contingameime.mixin;

import com.xinian.contingameime.client.event.ClientScreenEventHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Track mouse button events for IME state management.
 * Ported from IMBlocker's MouseMixin.
 */
@Mixin(MouseHandler.class)
public class MixinMouseHandler {
    @Final
    @Shadow
    private Minecraft minecraft;

    @Inject(method = "onPress", at = @At("HEAD"))
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (minecraft.screen != null) {
            // Trigger mouse move event to help with temporary IME state transitions
            ClientScreenEventHooks.fireMouseMove(0, 0, 0, 0);
        }
    }
}

