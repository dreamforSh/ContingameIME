package city.windmill.ingameime.client.jni;

import com.xinian.contingameime.client.gui.OverlayScreen;
import com.xinian.contingameime.client.handler.IMEHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFWNativeWin32;

/**
 * JNI bridge to the native IME library.
 * MUST stay at this exact package path because the native DLL has hardcoded JNI method names.
 */
public class ExternalBaseIME {
    private static final Logger LOGGER = LogManager.getFormatterLogger("ContingameIME|ExternalBaseIME");
    public static final ExternalBaseIME INSTANCE = new ExternalBaseIME();

    private ICommitListener commitListener = IMEHandler.IMEState.COMPANION;

    private volatile boolean initialized = false;
    private volatile boolean state = false;
    private volatile boolean fullScreen = false;
    private volatile boolean alphaMode = false;

    private ExternalBaseIME() {}

    public ICommitListener getCommitListener() { return commitListener; }
    public void setCommitListener(ICommitListener listener) { this.commitListener = listener; }

    public boolean isInitialized() { return initialized; }
    public boolean getState() { return state; }
    public void setState(boolean value) {
        LOGGER.trace("State {} -> {}", state, value);
        state = value;
        if (!initialized) return;
        try {
            nSetState(state);
        } catch (Exception e) {
            LOGGER.error("Failed to set native IME state:", e);
        }
        OverlayScreen.INSTANCE.setShowAlphaMode(state);
    }

    public boolean getFullScreen() { return fullScreen; }
    public void setFullScreen(boolean value) {
        LOGGER.trace("FullScreen {} -> {}", fullScreen, value);
        fullScreen = value;
        if (!initialized) return;
        try {
            // Always pass false to native so IME candidate window shows even in fullscreen/borderless
            nSetFullScreen(false);
        } catch (Exception e) {
            LOGGER.error("Failed to set native fullscreen state:", e);
        }
    }

    public boolean getAlphaMode() { return alphaMode; }

    public void initialize() {
        if (initialized) return;
        try {
            String arch = System.getProperty("os.arch", "");
            String x86 = arch.contains("64") ? "" : "-x86";
            ResourceLocation resourceNative = ResourceLocation.fromNamespaceAndPath("contingameime", "natives/jni" + x86 + ".dll");
            var resource = Minecraft.getInstance().getResourceManager().getResource(resourceNative).orElseThrow();
            NativeLoader.load(resource);
            LOGGER.debug("Initializing window");
            long hwnd = GLFWNativeWin32.glfwGetWin32Window(Minecraft.getInstance().getWindow().getWindow());
            nInitialize(hwnd);
            initialized = true;
            LOGGER.info("Native IME initialized successfully");
            setFullScreen(Minecraft.getInstance().getWindow().isFullscreen());
        } catch (Exception ex) {
            LOGGER.error("Failed in initializing ExternalBaseIME:", ex);
        }
    }

    // region Native methods
    private native void nInitialize(long handle);
    private native void nUninitialize();
    private native void nSetState(boolean state);
    private native void nSetFullScreen(boolean fullscreen);
    // endregion

    // region Callbacks from JNI (called by native code, potentially from IME thread)
    @SuppressWarnings("unused")
    private void onCandidateList(String[] candidates) {
        Minecraft.getInstance().execute(() ->
            OverlayScreen.INSTANCE.setCandidates(candidates));
    }

    @SuppressWarnings("unused")
    private void onComposition(String str, int caret, CompositionState compositionState) {
        Minecraft.getInstance().execute(() -> {
            switch (compositionState) {
                case Commit:
                    OverlayScreen.INSTANCE.setComposition(null, 0);
                    String result = commitListener.onCommit(str);
                    Screen screen = Minecraft.getInstance().screen;
                    if (screen != null && result != null) {
                        for (char ch : result.toCharArray()) {
                            screen.charTyped(ch, 0);
                        }
                    }
                    break;
                case Start:
                case End:
                case Update:
                    if (str == null || str.isEmpty()) {
                        OverlayScreen.INSTANCE.setComposition(null, 0);
                    } else {
                        OverlayScreen.INSTANCE.setComposition(str, caret);
                    }
                    break;
            }
            OverlayScreen.INSTANCE.setShowAlphaMode(false);
        });
    }

    @SuppressWarnings("unused")
    private int[] onGetCompExt() {
        return OverlayScreen.INSTANCE.getCompositionExt();
    }

    @SuppressWarnings("unused")
    private void onAlphaMode(boolean isAlphaMode) {
        LOGGER.trace("AlphaMode {} -> {}", alphaMode, isAlphaMode);
        alphaMode = isAlphaMode;
        Minecraft.getInstance().execute(() ->
            OverlayScreen.INSTANCE.setShowAlphaMode(true));
    }
    // endregion

    /**
     * Enum must stay here with these exact names for JNI compatibility.
     */
    private enum CompositionState {
        Start,
        Update,
        End,
        Commit
    }
}

