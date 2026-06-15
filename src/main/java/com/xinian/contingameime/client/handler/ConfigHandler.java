package com.xinian.contingameime.client.handler;

import city.windmill.ingameime.client.jni.ExternalBaseIME;
import city.windmill.ingameime.client.jni.ICommitListener;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.xinian.contingameime.client.gui.OverlayScreen;
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
import java.util.ArrayList;
import java.util.List;

public class ConfigHandler {
    private static final Logger LOGGER = LogManager.getFormatterLogger("ContingameIME|Config");

    private static boolean disableIMEInCommandMode = false;
    private static boolean autoReplaceSlashChar = false;
    private static boolean showIndicator = true;
    private static char[] slashCharArray = {'\u3001'};

    public static boolean isDisableIMEInCommandMode() { return disableIMEInCommandMode; }
    public static boolean isAutoReplaceSlashChar() { return autoReplaceSlashChar; }
    public static boolean isShowIndicator() { return showIndicator; }

    public static void setShowIndicator(boolean value) {
        showIndicator = value;
        OverlayScreen.INSTANCE.setIndicatorEnabled(value);
    }

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
        setShowIndicator(true);
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
            // Seed documented defaults first so keys absent from a partial/older file take the
            // intended value instead of the bare static initializer, then overlay present keys.
            loadDefaultConfig();
            boolean missingKey = false;
            try (JsonReader reader = new JsonReader(new FileReader(config.toFile()))) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                if (json.has("disableIMEInCommandMode")) {
                    setDisableIMEInCommandMode(json.get("disableIMEInCommandMode").getAsBoolean());
                } else missingKey = true;
                if (json.has("autoReplaceSlashChar")) {
                    setAutoReplaceSlashChar(json.get("autoReplaceSlashChar").getAsBoolean());
                } else missingKey = true;
                if (json.has("showIndicator")) {
                    setShowIndicator(json.get("showIndicator").getAsBoolean());
                } else missingKey = true;
                if (json.has("slashChars")) {
                    slashCharArray = parseSlashChars(json.get("slashChars").getAsJsonArray());
                } else missingKey = true;
            }
            // Backfill any keys the file was missing so the on-disk config stays complete.
            if (missingKey) saveConfig();
        } catch (Exception e) {
            LOGGER.warn("Failed to read config:", e);
            LOGGER.warn("Loading Default config");
            loadDefaultConfig();
            saveConfig();
        }
    }

    private static char[] parseSlashChars(JsonArray arr) {
        List<Character> chars = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            String s = arr.get(i).getAsString();
            if (s.isEmpty()) {
                LOGGER.warn("Ignoring empty slashChars entry at index {}", i);
                continue;
            }
            if (s.length() > 1) {
                LOGGER.warn("slashChars entry '{}' has multiple characters; using the first only", s);
            }
            chars.add(s.charAt(0));
        }
        // Keep the current (default) trigger set if the file provided no usable entries.
        if (chars.isEmpty()) return slashCharArray;
        char[] parsed = new char[chars.size()];
        for (int i = 0; i < parsed.length; i++) parsed[i] = chars.get(i);
        return parsed;
    }

    public static void saveConfig() {
        try {
            Path config = getConfigPath();
            Files.createDirectories(config.getParent());
            JsonObject json = new JsonObject();
            json.addProperty("disableIMEInCommandMode", disableIMEInCommandMode);
            json.addProperty("autoReplaceSlashChar", autoReplaceSlashChar);
            json.addProperty("showIndicator", showIndicator);
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

