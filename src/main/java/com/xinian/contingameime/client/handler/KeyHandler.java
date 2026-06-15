package com.xinian.contingameime.client.handler;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class KeyHandler {
    private static final Logger LOGGER = LogManager.getFormatterLogger("ContingameIME|KeyHandler");
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "ContingameIME-Key");
        t.setDaemon(true);
        return t;
    });

    public static final KeyMapping TOGGLE_KEY = new KeyMapping(
            "key.contingameime.hotkey",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_HOME,
            "category.contingameime.keybinding"
    );

    private static volatile KeyState keyState = KeyState.PENDING_KEY_DOWN;
    private static volatile ScheduledFuture<?> delayLongPress;
    private static volatile ScheduledFuture<?> longPressRepeat;

    public static boolean onKeyDown(int keyCode, int scanCode, int modifier) {
        if (keyCode == TOGGLE_KEY.getKey().getValue()) {
            keyState = keyState.handleKeyDown();
            return shouldConsume();
        }
        return false;
    }

    public static boolean onKeyUp(int keyCode, int scanCode, int modifier) {
        if (keyCode == TOGGLE_KEY.getKey().getValue()) {
            keyState = keyState.handleKeyUp();
            return shouldConsume();
        }
        return false;
    }

    /**
     * Don't swallow the toggle key while a text field is focused, so a default of Home still
     * performs its in-field action (move cursor to line start) instead of being eaten. The IME
     * gesture is still processed; only event cancellation is suppressed.
     */
    private static boolean shouldConsume() {
        return ScreenHandler.EditState.getEditState() != ScreenHandler.EditState.EDIT_OPEN;
    }

    private enum KeyState {
        PENDING_KEY_DOWN {
            @Override KeyState handleKeyDown() {
                // Cancel any leftover tasks from previous cycles
                if (longPressRepeat != null) longPressRepeat.cancel(false);
                if (delayLongPress != null) delayLongPress.cancel(false);
                longPressRepeat = SCHEDULER.scheduleAtFixedRate(() -> {
                    if (keyState == COUNTING_LONG_PRESS) {
                        Minecraft.getInstance().execute(() -> onKeyAction(KeyAction.KEY_LONG_PRESS));
                    }
                }, 2000, 2000, TimeUnit.MILLISECONDS);
                delayLongPress = SCHEDULER.schedule(() -> Minecraft.getInstance().execute(() -> {
                    // Arm long-press only if the key is still held. Marshalling to the main
                    // thread (and the guard) prevents a cancelled-but-already-running task from
                    // wedging the machine in COUNTING_LONG_PRESS after the key was released.
                    if (keyState == PENDING_KEY_UP) keyState = COUNTING_LONG_PRESS;
                }), 500, TimeUnit.MILLISECONDS);
                return PENDING_KEY_UP;
            }
            @Override KeyState handleKeyUp() { return this; }
        },
        PENDING_KEY_UP {
            @Override KeyState handleKeyDown() { return this; }
            @Override KeyState handleKeyUp() {
                if (delayLongPress != null) delayLongPress.cancel(false);
                if (longPressRepeat != null) longPressRepeat.cancel(false);
                onKeyAction(KeyAction.KEY_CLICKED);
                return PENDING_KEY_DOWN;
            }
        },
        COUNTING_LONG_PRESS {
            @Override KeyState handleKeyDown() { return this; }
            @Override KeyState handleKeyUp() {
                if (longPressRepeat != null) longPressRepeat.cancel(false);
                return PENDING_KEY_DOWN;
            }
        };
        abstract KeyState handleKeyDown();
        abstract KeyState handleKeyUp();
    }

    private enum KeyAction { KEY_CLICKED, KEY_LONG_PRESS }

    // CombinationKey
    private static volatile CombinationKeyState combinationKeyState = CombinationKeyState.PENDING_CLICK;
    private static volatile ScheduledFuture<?> delayDoubleClick;

    private static void onKeyAction(KeyAction action) {
        combinationKeyState = combinationKeyState.handleAction(action);
    }

    private enum CombinationKeyState {
        PENDING_CLICK {
            @Override CombinationKeyState handleAction(KeyAction action) {
                return switch (action) {
                    case KEY_CLICKED -> {
                        delayDoubleClick = SCHEDULER.schedule(() -> Minecraft.getInstance().execute(() -> {
                            // Single-click timeout fires on the main thread; ignore it if a
                            // second click already consumed this window (avoids a stray CLICKED
                            // racing a DOUBLE_CLICKED).
                            if (combinationKeyState == PENDING_DOUBLE_CLICK) {
                                combinationKeyState = PENDING_CLICK;
                                IMEHandler.IMEState.COMPANION.onAction(CombinationKeyAction.CLICKED);
                            }
                        }), 300, TimeUnit.MILLISECONDS);
                        yield PENDING_DOUBLE_CLICK;
                    }
                    case KEY_LONG_PRESS -> {
                        Minecraft.getInstance().execute(() ->
                            IMEHandler.IMEState.COMPANION.onAction(CombinationKeyAction.LONG_PRESS));
                        yield PENDING_CLICK;
                    }
                };
            }
        },
        PENDING_DOUBLE_CLICK {
            @Override CombinationKeyState handleAction(KeyAction action) {
                switch (action) {
                    case KEY_CLICKED -> {
                        if (delayDoubleClick != null) delayDoubleClick.cancel(false);
                        Minecraft.getInstance().execute(() ->
                            IMEHandler.IMEState.COMPANION.onAction(CombinationKeyAction.DOUBLE_CLICKED));
                    }
                    case KEY_LONG_PRESS ->
                        Minecraft.getInstance().execute(() ->
                            IMEHandler.IMEState.COMPANION.onAction(CombinationKeyAction.LONG_PRESS));
                }
                return PENDING_CLICK;
            }
        };
        abstract CombinationKeyState handleAction(KeyAction action);
    }

    public enum CombinationKeyAction { CLICKED, DOUBLE_CLICKED, LONG_PRESS }
}
