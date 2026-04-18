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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Detect non-standard text field widgets by scanning the focused element during screen render.
 * Ported from IMBlocker's ClickableWidgetMixin + ParentElementMixin approach.
 */
@Mixin(Screen.class)
public abstract class MixinAbstractWidget implements ContainerEventHandler {

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderDetectTextField(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        GuiEventListener focused = this.getFocused();
        if (focused != null
                && !(focused instanceof EditBox)
                && focused instanceof AbstractWidget aw
                && aw.isFocused()
                && TextFieldDetector.isTextField(focused.getClass())) {
            ClientScreenEventHooks.fireEditOpen(focused, aw.getX(), aw.getY());
        }
    }
}
