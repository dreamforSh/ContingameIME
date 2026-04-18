package com.xinian.contingameime.client.event;

import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Custom event hooks for screen/edit events, replacing the Architectury event system.
 */
public class ClientScreenEventHooks {

    // region Listener interfaces
    @FunctionalInterface
    public interface MouseMoveListener {
        void onMouseMove(int prevX, int prevY, int curX, int curY);
    }

    @FunctionalInterface
    public interface WindowSizeChangedListener {
        void onWindowSizeChanged(int sizeX, int sizeY);
    }

    @FunctionalInterface
    public interface ScreenChangedListener {
        void onScreenChanged(Screen oldScreen, Screen newScreen);
    }

    @FunctionalInterface
    public interface EditOpenListener {
        void onEditOpen(Object edit, int caretX, int caretY);
    }

    @FunctionalInterface
    public interface EditCaretListener {
        void onEditCaret(Object edit, int caretX, int caretY);
    }

    @FunctionalInterface
    public interface EditCloseListener {
        void onEditClose(Object edit);
    }
    // endregion

    // region Listener lists
    private static final List<MouseMoveListener> mouseMoveListeners = new CopyOnWriteArrayList<>();
    private static final List<WindowSizeChangedListener> windowSizeChangedListeners = new CopyOnWriteArrayList<>();
    private static final List<ScreenChangedListener> screenChangedListeners = new CopyOnWriteArrayList<>();
    private static final List<EditOpenListener> editOpenListeners = new CopyOnWriteArrayList<>();
    private static final List<EditCaretListener> editCaretListeners = new CopyOnWriteArrayList<>();
    private static final List<EditCloseListener> editCloseListeners = new CopyOnWriteArrayList<>();
    // endregion

    // region Register methods
    public static void registerMouseMove(MouseMoveListener listener) { mouseMoveListeners.add(listener); }
    public static void registerWindowSizeChanged(WindowSizeChangedListener listener) { windowSizeChangedListeners.add(listener); }
    public static void registerScreenChanged(ScreenChangedListener listener) { screenChangedListeners.add(listener); }
    public static void registerEditOpen(EditOpenListener listener) { editOpenListeners.add(listener); }
    public static void registerEditCaret(EditCaretListener listener) { editCaretListeners.add(listener); }
    public static void registerEditClose(EditCloseListener listener) { editCloseListeners.add(listener); }
    // endregion

    // region Fire methods
    public static void fireMouseMove(int prevX, int prevY, int curX, int curY) {
        for (MouseMoveListener l : mouseMoveListeners) l.onMouseMove(prevX, prevY, curX, curY);
    }

    public static void fireWindowSizeChanged(int sizeX, int sizeY) {
        for (WindowSizeChangedListener l : windowSizeChangedListeners) l.onWindowSizeChanged(sizeX, sizeY);
    }

    public static void fireScreenChanged(Screen oldScreen, Screen newScreen) {
        for (ScreenChangedListener l : screenChangedListeners) l.onScreenChanged(oldScreen, newScreen);
    }

    public static void fireEditOpen(Object edit, int caretX, int caretY) {
        for (EditOpenListener l : editOpenListeners) l.onEditOpen(edit, caretX, caretY);
    }

    public static void fireEditCaret(Object edit, int caretX, int caretY) {
        for (EditCaretListener l : editCaretListeners) l.onEditCaret(edit, caretX, caretY);
    }

    public static void fireEditClose(Object edit) {
        for (EditCloseListener l : editCloseListeners) l.onEditClose(edit);
    }
    // endregion
}

