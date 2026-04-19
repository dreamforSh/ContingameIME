package com.xinian.contingameime.mixin;

import com.xinian.contingameime.client.TextFieldDetector;
import com.xinian.contingameime.client.event.ClientScreenEventHooks;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Detect non-standard text field widgets by scanning the focused element during screen render.
 * Also detects when focus leaves a detected text field to fire editClose.
 */
@Mixin(Screen.class)
public abstract class MixinAbstractWidget implements ContainerEventHandler {

    @Unique
    private GuiEventListener contingameime$lastDetectedTextField = null;

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderDetectTextField(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        GuiEventListener focused = this.getFocused();

        // Check if the previously detected non-EditBox text field lost focus
        if (contingameime$lastDetectedTextField != null && contingameime$lastDetectedTextField != focused) {
            ClientScreenEventHooks.fireEditClose(contingameime$lastDetectedTextField);
            contingameime$lastDetectedTextField = null;
        }

        if (focused != null
                && !(focused instanceof EditBox)
                && focused instanceof AbstractWidget aw
                && aw.isFocused()
                && TextFieldDetector.isTextField(focused.getClass())) {
            contingameime$lastDetectedTextField = focused;
            ClientScreenEventHooks.fireEditOpen(focused, aw.getX(), aw.getY());
        }
    }
}
