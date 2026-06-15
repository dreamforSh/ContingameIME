package city.windmill.ingameime.client.jni;

import net.minecraft.server.packs.resources.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class NativeLoader {
    private static final Logger LOGGER = LogManager.getFormatterLogger("ContingameIME|NativeLoader");

    /**
     * Extract the bundled native library to a stable on-disk path and load it.
     *
     * <p>A stable target (rather than a fresh temp file per launch) avoids leaking one
     * orphaned DLL into the system temp directory on every run, since a {@code System.load}ed
     * DLL stays mapped/locked for the JVM lifetime and {@code deleteOnExit} cannot remove it
     * on Windows.
     *
     * @return {@code true} if the library was loaded, {@code false} on any failure
     *     (including {@link UnsatisfiedLinkError}), so the caller can degrade gracefully.
     */
    public static boolean load(Resource lib, Path target) {
        try {
            Files.createDirectories(target.getParent());
            try (InputStream is = lib.open()) {
                Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException copyFailed) {
                // The target may be locked by another running game instance; fall back to the
                // existing copy if one is present, otherwise propagate the failure.
                if (!Files.exists(target)) throw copyFailed;
                LOGGER.warn("Could not refresh native library (in use?), using existing {}", target);
            }
            System.load(target.toAbsolutePath().toString());
            LOGGER.info("Native library loaded from {}", target);
            return true;
        } catch (Throwable t) {
            LOGGER.error("Failed to load native library:", t);
            return false;
        }
    }
}
