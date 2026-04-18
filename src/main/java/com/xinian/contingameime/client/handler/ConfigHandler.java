package com.xinian.contingameime.client.handler;

import city.windmill.ingameime.client.jni.ExternalBaseIME;
import city.windmill.ingameime.client.jni.ICommitListener;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import com.xinian.contingameime.mixin.ChatScreenAccessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ConfigHandler {
    private static final Logger LOGGER = LogManager.getFormatterLogger("ContingameIME|Config");

    private static boolean disableIMEInCommandMode = false;
    private static boolean autoReplaceSlashChar = false;
    private static char[] slashCharArray = {'\u3001'};

    public static boolean isDisableIMEInCommandMode() { return disableIMEInCommandMode; }
    public static boolean isAutoReplaceSlashChar() { return autoReplaceSlashChar; }

    public static void setDisableIMEInCommandMode(boolean value) {
        if (disableIMEInCommandMode == value) return;
        if (value) {
            ScreenHandler.EditState.setEditStateListener(state -> {
                if (state == ScreenHandler.EditState.EDIT_OPEN
                        && ScreenHandler.getCurrentScreen() instanceof ChatScreen chatScreen
                        && "/".equals(((ChatScreenAccessor) chatScreen).getInitial())) {
                    IMEHandler.IMEState.COMPANION.onEditState(ScreenHandler.EditState.NULL_EDIT);
                    return;
                }
                IMEHandler.IMEState.COMPANION.onEditState(state);
            });
        } else {
            ScreenHandler.EditState.resetEditStateListener();
        }
        disableIMEInCommandMode = value;
    }

    public static void setAutoReplaceSlashChar(boolean value) {
        if (autoReplaceSlashChar == value) return;
        if (value) {
            ExternalBaseIME.INSTANCE.setCommitListener(commit -> {
                String result = commit;
                if (ScreenHandler.getCurrentScreen() instanceof ChatScreen
                        && ScreenHandler.EditState.getCurrentEdit() instanceof EditBox editBox
                        && editBox.getCursorPosition() == 0
                        && !commit.isEmpty() && containsChar(slashCharArray, commit.charAt(0))) {
                    result = "/" + commit.substring(1);
                    if (disableIMEInCommandMode) {
                        IMEHandler.IMEState.COMPANION.onEditState(ScreenHandler.EditState.NULL_EDIT);
                    }
                }
                return IMEHandler.IMEState.COMPANION.onCommit(result);
            });
        } else {
            ExternalBaseIME.INSTANCE.setCommitListener(IMEHandler.IMEState.COMPANION);
        }
        autoReplaceSlashChar = value;
    }

    private static boolean containsChar(char[] arr, char c) {
        for (char ch : arr) if (ch == c) return true;
        return false;
    }

    private static Path getConfigPath() {
        return Paths.get(Minecraft.getInstance().gameDirectory.toString(), "config", "contingameime.json");
    }

    public static void initialConfig() {
        readConfig();
    }

    public static void loadDefaultConfig() {
        setDisableIMEInCommandMode(true);
        setAutoReplaceSlashChar(true);
        slashCharArray = new char[]{'\u3001'};
    }

    public static void readConfig() {
        try {
            Path config = getConfigPath();
            if (!Files.exists(config)) {
                loadDefaultConfig();
                saveConfig();
                return;
            }
            try (JsonReader reader = new JsonReader(new FileReader(config.toFile()))) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                setDisableIMEInCommandMode(json.get("disableIMEInCommandMode").getAsBoolean());
                setAutoReplaceSlashChar(json.get("autoReplaceSlashChar").getAsBoolean());
                JsonArray arr = json.get("slashChars").getAsJsonArray();
                char[] chars = new char[arr.size()];
                for (int i = 0; i < arr.size(); i++) {
                    String s = arr.get(i).getAsString();
                    chars[i] = s.isEmpty() ? ' ' : s.charAt(0);
                }
                slashCharArray = chars;
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to read config:", e);
            LOGGER.warn("Loading Default config");
            loadDefaultConfig();
        }
        saveConfig();
    }

    public static void saveConfig() {
        try {
            Path config = getConfigPath();
            Files.createDirectories(config.getParent());
            JsonObject json = new JsonObject();
            json.addProperty("disableIMEInCommandMode", disableIMEInCommandMode);
            json.addProperty("autoReplaceSlashChar", autoReplaceSlashChar);
            JsonArray arr = new JsonArray();
            for (char c : slashCharArray) arr.add(String.valueOf(c));
            json.add("slashChars", arr);

            try (Writer writer = Files.newBufferedWriter(config,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(json));
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save config:", e);
        }
    }
}

