package com.xinian.contingameime.client.gui;

import com.xinian.contingameime.client.handler.ConfigHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * Simple in-game settings screen, reachable from the mod list's config button.
 * Each toggle's setter applies its effect immediately; values are persisted on close.
 */
public class IMEConfigScreen extends Screen {
    private static final int ROW_WIDTH = 310;
    private static final int ROW_HEIGHT = 20;

    private final Screen parent;

    public IMEConfigScreen(Screen parent) {
        super(Component.translatable("config.contingameime.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int left = this.width / 2 - ROW_WIDTH / 2;
        int y = 40;

        addRenderableWidget(CycleButton.onOffBuilder(ConfigHandler.isDisableIMEInCommandMode())
                .create(left, y, ROW_WIDTH, ROW_HEIGHT,
                        Component.translatable("config.contingameime.disableIMEInCommandMode"),
                        (button, value) -> ConfigHandler.setDisableIMEInCommandMode(value)));
        y += 24;

        addRenderableWidget(CycleButton.onOffBuilder(ConfigHandler.isAutoReplaceSlashChar())
                .create(left, y, ROW_WIDTH, ROW_HEIGHT,
                        Component.translatable("config.contingameime.autoReplaceSlashChar"),
                        (button, value) -> ConfigHandler.setAutoReplaceSlashChar(value)));
        y += 24;

        addRenderableWidget(CycleButton.onOffBuilder(ConfigHandler.isShowIndicator())
                .create(left, y, ROW_WIDTH, ROW_HEIGHT,
                        Component.translatable("config.contingameime.showIndicator"),
                        (button, value) -> ConfigHandler.setShowIndicator(value)));

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 200, ROW_HEIGHT)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 18, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        ConfigHandler.saveConfig();
        this.minecraft.setScreen(parent);
    }
}
