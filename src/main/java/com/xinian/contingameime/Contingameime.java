package com.xinian.contingameime;

import city.windmill.ingameime.client.jni.ExternalBaseIME;
import com.mojang.logging.LogUtils;
import com.xinian.contingameime.client.event.ClientScreenEventHooks;
import com.xinian.contingameime.client.gui.OverlayScreen;
import com.xinian.contingameime.client.handler.ConfigHandler;
import com.xinian.contingameime.client.handler.IMEHandler;
import com.xinian.contingameime.client.handler.KeyHandler;
import com.xinian.contingameime.client.handler.ScreenHandler;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(Contingameime.MODID)
public class Contingameime {
    public static final String MODID = "contingameime";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Contingameime(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::onRegisterKeyMappings);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        if (Util.getPlatform() != Util.OS.WINDOWS) {
            LOGGER.warn("ContingameIME only works on Windows!");
            return;
        }
        LOGGER.info("ContingameIME: Windows detected, loading mod...");

        event.enqueueWork(() -> {
            ConfigHandler.initialConfig();

            // Register event listeners
            ClientScreenEventHooks.registerMouseMove((prevX, prevY, curX, curY) ->
                    IMEHandler.IMEState.COMPANION.onMouseMove());

            ClientScreenEventHooks.registerWindowSizeChanged((sizeX, sizeY) ->
                    ExternalBaseIME.INSTANCE.setFullScreen(Minecraft.getInstance().getWindow().isFullscreen()));

            ClientScreenEventHooks.registerScreenChanged(ScreenHandler::onScreenChange);

            ClientScreenEventHooks.registerEditOpen(ScreenHandler.EditState::onEditOpen);
            ClientScreenEventHooks.registerEditCaret(ScreenHandler.EditState::onEditCaret);
            ClientScreenEventHooks.registerEditClose(ScreenHandler.EditState::onEditClose);

            // Initialize native IME
            ExternalBaseIME.INSTANCE.initialize();
            LOGGER.info("Current IME State: {}", ExternalBaseIME.INSTANCE.getState());
        });

        // Register NeoForge event listeners for screen rendering and key input
        NeoForge.EVENT_BUS.addListener(this::onScreenRenderPost);
        NeoForge.EVENT_BUS.addListener(this::onKeyPressed);
        NeoForge.EVENT_BUS.addListener(this::onKeyReleased);
    }

    private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KeyHandler.TOGGLE_KEY);
    }

    // Track mouse movement
    private int prevMouseX = 0;
    private int prevMouseY = 0;

    private void onScreenRenderPost(ScreenEvent.Render.Post event) {
        int mouseX = event.getMouseX();
        int mouseY = event.getMouseY();
        if (mouseX != prevMouseX || mouseY != prevMouseY) {
            ClientScreenEventHooks.fireMouseMove(prevMouseX, prevMouseY, mouseX, mouseY);
            prevMouseX = mouseX;
            prevMouseY = mouseY;
        }
        OverlayScreen.INSTANCE.render(event.getGuiGraphics(), mouseX, mouseY, event.getPartialTick());
    }

    private void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (KeyHandler.onKeyDown(event.getKeyCode(), event.getScanCode(), event.getModifiers())) {
            event.setCanceled(true);
        }
    }

    private void onKeyReleased(ScreenEvent.KeyReleased.Pre event) {
        if (KeyHandler.onKeyUp(event.getKeyCode(), event.getScanCode(), event.getModifiers())) {
            event.setCanceled(true);
        }
    }
}
