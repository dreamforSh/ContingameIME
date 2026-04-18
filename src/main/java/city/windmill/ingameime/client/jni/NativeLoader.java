package city.windmill.ingameime.client.jni;

import net.minecraft.server.packs.resources.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class NativeLoader {
    private static final Logger LOGGER = LogManager.getFormatterLogger("ContingameIME|NativeLoader");

    public static void load(Resource lib) {
        try {
            LOGGER.debug("Try load native from resource");
            Path tempFile = Files.createTempFile("IngameIME-Native", ".dll");
            LOGGER.debug("Copying Native to {}", tempFile);
            try (InputStream is = lib.open()) {
                Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }
            System.load(tempFile.toAbsolutePath().toString());
        } catch (Exception e) {
            LOGGER.error("Failed to load native library:", e);
        }
    }
}

