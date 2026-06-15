package com.xinian.contingameime;

import city.windmill.ingameime.client.jni.ExternalBaseIME;
import com.mojang.logging.LogUtils;
import com.xinian.contingameime.client.event.ClientScreenEventHooks;
import com.xinian.contingameime.client.gui.IMEConfigScreen;
import com.xinian.contingameime.client.gui.OverlayScreen;
import com.xinian.contingameime.client.handler.ConfigHandler;
import com.xinian.contingameime.client.handler.IMEHandler;
import com.xinian.contingameime.client.handler.KeyHandler;
import com.xinian.contingameime.client.handler.ScreenHandler;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(Contingameime.MODID)
public class Contingameime {
    public static final String MODID = "contingameime";
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ModContainer modContainer;
    private boolean infoMessageShown = false;

    public Contingameime(IEventBus modEventBus, ModContainer modContainer) {
        this.modContainer = modContainer;
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::onRegisterKeyMappings);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        // Register the config screen and the one-time status message regardless of platform,
        // so non-Windows users still get a clear in-game explanation.
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                (IConfigScreenFactory) (container, parent) -> new IMEConfigScreen(parent));
        NeoForge.EVENT_BUS.addListener(this::onClientTick);

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

    // Surface a single in-game message once the player is in a world explaining why the IME
    // is unavailable (non-Windows platform, or the native library failed to load).
    private void onClientTick(ClientTickEvent.Post event) {
        if (infoMessageShown) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        infoMessageShown = true;
        if (Util.getPlatform() != Util.OS.WINDOWS) {
            mc.player.displayClientMessage(
                    Component.translatable("message.contingameime.unsupported_platform"), false);
        } else if (!ExternalBaseIME.INSTANCE.isInitialized()) {
            mc.player.displayClientMessage(
                    Component.translatable("message.contingameime.native_failed"), false);
        }
    }
}
