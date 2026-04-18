package com.xinian.contingameime.client.handler;

import city.windmill.ingameime.client.jni.ExternalBaseIME;
import city.windmill.ingameime.client.jni.ICommitListener;
import com.xinian.contingameime.client.gui.OverlayScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class IMEHandler {
    private static final Logger LOGGER = LogManager.getFormatterLogger("ContingameIME|IMEHandler");

    public enum IMEState {
        DISABLED {
            @Override
            public IMEState onAction(KeyHandler.CombinationKeyAction action) {
                return switch (action) {
                    case CLICKED -> TEMPORARY;
                    case DOUBLE_CLICKED -> ENABLED;
                    case LONG_PRESS -> this;
                };
            }
            @Override public IMEState onCommit() { return this; }
            @Override public IMEState onMouseMove() { return this; }
            @Override
            public IMEState onScreenState(ScreenHandler.ScreenState state) {
                return switch (state) {
                    case NULL_SCREEN, SCREEN_OPEN -> DISABLED;
                    case SCREEN_DUMMY_EDIT -> ENABLED;
                };
            }
            @Override
            public IMEState onEditState(ScreenHandler.EditState state) {
                return switch (state) {
                    case NULL_EDIT -> DISABLED;
                    case EDIT_OPEN -> ENABLED;
                };
            }
        },
        TEMPORARY {
            private boolean hasCommit = false;

            @Override
            public IMEState onAction(KeyHandler.CombinationKeyAction action) {
                return switch (action) {
                    case CLICKED -> DISABLED;
                    case DOUBLE_CLICKED -> ENABLED;
                    case LONG_PRESS -> this;
                };
            }
            @Override
            public IMEState onCommit() {
                hasCommit = true;
                return this;
            }
            @Override
            public IMEState onMouseMove() {
                if (!OverlayScreen.INSTANCE.isComposing() && hasCommit) {
                    hasCommit = false;
                    return DISABLED;
                }
                return this;
            }
            @Override
            public IMEState onScreenState(ScreenHandler.ScreenState state) {
                return switch (state) {
                    case NULL_SCREEN, SCREEN_OPEN -> DISABLED;
                    case SCREEN_DUMMY_EDIT -> ENABLED;
                };
            }
            @Override
            public IMEState onEditState(ScreenHandler.EditState state) {
                return switch (state) {
                    case NULL_EDIT -> DISABLED;
                    case EDIT_OPEN -> ENABLED;
                };
            }
        },
        ENABLED {
            @Override
            public IMEState onAction(KeyHandler.CombinationKeyAction action) {
                return switch (action) {
                    case CLICKED -> DISABLED;
                    case DOUBLE_CLICKED -> ENABLED;
                    case LONG_PRESS -> this;
                };
            }
            @Override public IMEState onCommit() { return this; }
            @Override public IMEState onMouseMove() { return this; }
            @Override
            public IMEState onScreenState(ScreenHandler.ScreenState state) {
                return switch (state) {
                    case NULL_SCREEN, SCREEN_OPEN -> DISABLED;
                    case SCREEN_DUMMY_EDIT -> ENABLED;
                };
            }
            @Override
            public IMEState onEditState(ScreenHandler.EditState state) {
                return switch (state) {
                    case NULL_EDIT -> DISABLED;
                    case EDIT_OPEN -> ENABLED;
                };
            }
        };

        public abstract IMEState onAction(KeyHandler.CombinationKeyAction action);
        public abstract IMEState onCommit();
        public abstract IMEState onMouseMove();
        public abstract IMEState onScreenState(ScreenHandler.ScreenState state);
        public abstract IMEState onEditState(ScreenHandler.EditState state);

        /**
         * Companion-like static state manager
         */
        public static final IMEStateCompanion COMPANION = new IMEStateCompanion();

        public static class IMEStateCompanion implements ICommitListener, ScreenHandler.EditStateListener {
            private IMEState imeState = DISABLED;

            private void setImeState(IMEState newState) {
                if (imeState == newState) return;
                LOGGER.trace("IMEState {} -> {}", imeState, newState);
                imeState = newState;
                switch (imeState) {
                    case DISABLED -> ExternalBaseIME.INSTANCE.setState(false);
                    case TEMPORARY, ENABLED -> ExternalBaseIME.INSTANCE.setState(true);
                }
            }

            public void onAction(KeyHandler.CombinationKeyAction action) {
                setImeState(imeState.onAction(action));
            }

            public void onScreenState(ScreenHandler.ScreenState state) {
                setImeState(imeState.onScreenState(state));
            }

            @Override
            public void onEditState(ScreenHandler.EditState state) {
                setImeState(imeState.onEditState(state));
            }

            @Override
            public String onCommit(String commit) {
                setImeState(imeState.onCommit());
                return commit;
            }

            public void onMouseMove() {
                setImeState(imeState.onMouseMove());
            }
        }
    }
}

