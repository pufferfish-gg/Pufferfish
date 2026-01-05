package gg.pufferfish.pufferfish.flare;

import co.technove.flare.FlareInitializer;
import co.technove.flare.internal.profiling.InitializationException;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Level;
import java.util.Locale;

public class FlareSetup {

    private static final String OS_NAME = System.getProperty("os.name").toLowerCase(Locale.ROOT);
    private static final String OS_ARCH = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
    private static boolean initialized = false;
    private static boolean supported = false;

    public static void init() {
        if (initialized) {
            return;
        }

        if (!OS_NAME.contains("linux") || !OS_ARCH.contains("amd64")) {
            MinecraftServer.LOGGER.warn("Flare does not support running on {} {}, will not enable!", OS_NAME, OS_ARCH);
            return;
        }

        initialized = true;
        try {
            for (String warning : FlareInitializer.initialize()) {
                MinecraftServer.LOGGER.warn("Flare warning: " + warning);
            }
            supported = true;
        } catch (InitializationException e) {
            MinecraftServer.LOGGER.warn("Failed to enable Flare:", e);
        }
    }

    public static boolean isSupported() {
        return supported;
    }

}
