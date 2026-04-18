package com.xinian.contingameime.client.handler;

import com.xinian.contingameime.client.gui.OverlayScreen;
import net.minecraft.client.gui.screens.Screen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScreenHandler {
    private static final Logger LOGGER = LogManager.getFormatterLogger("ContingameIME|ScreenHandler");

    private static ScreenState screenState = ScreenState.NULL_SCREEN;
    private static Screen currentScreen = null;

    public static Screen getCurrentScreen() { return currentScreen; }

    public static void onScreenChange(Screen oldScreen, Screen newScreen) {
        LOGGER.trace("{} -> {}", oldScreen, newScreen);
        screenState = screenState.handleScreenChange(oldScreen, newScreen);
    }

    private static void setScreenState(ScreenState newState) {
        LOGGER.trace("ScreenState {} -> {}", screenState, newState);
        screenState = newState;
        IMEHandler.IMEState.COMPANION.onScreenState(screenState);
        EditState.onScreenStateChange(screenState);
    }

    public enum ScreenState {
        NULL_SCREEN {
            @Override
            ScreenState handleScreenChange(Screen oldScreen, Screen newScreen) {
                if (newScreen != null) {
                    currentScreen = newScreen;
                    setScreenState(SCREEN_OPEN);
                    return SCREEN_OPEN;
                }
                return this;
            }
        },
        SCREEN_OPEN {
            @Override
            ScreenState handleScreenChange(Screen oldScreen, Screen newScreen) {
                if (newScreen != null) {
                    setScreenState(NULL_SCREEN);
                    currentScreen = newScreen;
                    setScreenState(SCREEN_OPEN);
                    return SCREEN_OPEN;
                }
                OverlayScreen.INSTANCE.setCaretPos(0, 0);
                setScreenState(NULL_SCREEN);
                return NULL_SCREEN;
            }
        },
        SCREEN_DUMMY_EDIT {
            @Override
            ScreenState handleScreenChange(Screen oldScreen, Screen newScreen) {
                if (newScreen != null) {
                    setScreenState(NULL_SCREEN);
                    currentScreen = newScreen;
                    setScreenState(SCREEN_OPEN);
                    return SCREEN_OPEN;
                }
                OverlayScreen.INSTANCE.setCaretPos(0, 0);
                setScreenState(NULL_SCREEN);
                return NULL_SCREEN;
            }
        };

        abstract ScreenState handleScreenChange(Screen oldScreen, Screen newScreen);
    }

    // region EditState
    public enum EditState {
        NULL_EDIT {
            @Override
            EditState handleEditOpen(Object edit, int cx, int cy) {
                currentEdit = edit;
                OverlayScreen.INSTANCE.setCaretPos(cx, cy);
                return EDIT_OPEN;
            }
            @Override
            EditState handleEditCaret(Object edit, int cx, int cy) { return this; }
            @Override
            EditState handleEditClose(Object edit) { return this; }
        },
        EDIT_OPEN {
            @Override
            EditState handleEditOpen(Object edit, int cx, int cy) {
                if (edit != currentEdit) {
                    setEditState(NULL_EDIT);
                    currentEdit = edit;
                    OverlayScreen.INSTANCE.setCaretPos(cx, cy);
                }
                return EDIT_OPEN;
            }
            @Override
            EditState handleEditCaret(Object edit, int cx, int cy) {
                if (edit == currentEdit) {
                    OverlayScreen.INSTANCE.setCaretPos(cx, cy);
                }
                return EDIT_OPEN;
            }
            @Override
            EditState handleEditClose(Object edit) {
                if (edit == currentEdit) {
                    currentEdit = null;
                    return NULL_EDIT;
                }
                return EDIT_OPEN;
            }
        };

        private static EditState editState = NULL_EDIT;
        private static Object currentEdit = null;
        private static EditStateListener editStateListener = IMEHandler.IMEState.COMPANION;

        public static Object getCurrentEdit() { return currentEdit; }
        public static EditState getEditState() { return editState; }

        public static void setEditStateListener(EditStateListener listener) {
            editStateListener = listener;
        }

        public static void resetEditStateListener() {
            editStateListener = IMEHandler.IMEState.COMPANION;
        }

        private static void setEditState(EditState newState) {
            if (editState == newState) return;
            LOGGER.trace("EditState {} -> {}", editState, newState);
            editState = newState;
            editStateListener.onEditState(editState);
        }

        static void onScreenStateChange(ScreenState state) {
            if (state == ScreenState.NULL_SCREEN) {
                currentEdit = null;
                setEditState(NULL_EDIT);
            }
        }

        public static void onEditOpen(Object edit, int cx, int cy) {
            editState = editState.handleEditOpen(edit, cx, cy);
            setEditState(editState);
        }

        public static void onEditCaret(Object edit, int cx, int cy) {
            editState = editState.handleEditCaret(edit, cx, cy);
        }

        public static void onEditClose(Object edit) {
            EditState newState = editState.handleEditClose(edit);
            setEditState(newState);
        }

        abstract EditState handleEditOpen(Object edit, int cx, int cy);
        abstract EditState handleEditCaret(Object edit, int cx, int cy);
        abstract EditState handleEditClose(Object edit);
    }

    @FunctionalInterface
    public interface EditStateListener {
        void onEditState(EditState state);
    }
    // endregion
}

