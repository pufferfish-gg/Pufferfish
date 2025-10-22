package gg.pufferfish.pufferfish.compat;

import com.google.common.io.Files;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
import net.minecraft.server.MinecraftServer;
import joptsimple.OptionSet;
import net.minecraft.server.level.ServerLevel;

public class ServerConfigurations {

    private static final OptionSet options = org.bukkit.craftbukkit.Main.options;

    private static final File properties = (File) options.valueOf("config");
    private static final File bukkitConfig = (File) options.valueOf("bukkit-settings");
    private static final File spigotConfig = (File) options.valueOf("spigot-settings");
    private static final File paperConfigDir = (File) options.valueOf("paper-dir");
    public static final File pufferfishConfig = (File) options.valueOf("pufferfish-settings"); // public so we can access it from the config class without repeated logic

    private static final MinecraftServer server = MinecraftServer.getServer();
    private static final List<ServerLevel> levelList = new ArrayList<>();

    public static final List<String> configurationFiles = List.of(
            properties.getPath(),
            bukkitConfig.getPath(),
            spigotConfig.getPath(),
            paperConfigDir.getPath() + "/paper-global.yml",
            paperConfigDir.getPath() + "/paper-world-defaults.yml",
            pufferfishConfig.getPath()
    );

    private static final Map<String, String> configFiles = new HashMap<>(configurationFiles.size());

    public static final List<String> hiddenConfigs = List.of(
            "proxies.velocity.secret",
            "web-services.token",
            "misc.sentry-dsn",
            "database",
            "server-ip",
            "motd",
            "resource-pack",
            "level-seed",
            "rcon.password",
            "rcon.ip",
            "feature-seeds",
            "world-settings.*.feature-seeds",
            "world-settings.*.seed-*",
            "seed-*"
    );

    private static final List<Pattern> regexPatterns = hiddenConfigs.stream()
            .map(s -> Pattern.compile(s.replace(".", "\\.").replace("*", ".*")))
            .collect(Collectors.toList());

    public static Map<String, String> getCleanCopies() throws IOException {
        for (final String file : configurationFiles) {
            if (configFiles.containsKey(file)) continue;
            configFiles.put(file, getCleanCopy(file));
        }

        for (final ServerLevel serverLevel : server.getAllLevels()) {
            if (levelList.contains(serverLevel)) continue;
            final File worldDir = serverLevel.getWorld().getWorldFolder();
            final String paperWorldConfig = new File(worldDir, "paper-world.yml").getPath();
            final String cleanConfig = getCleanCopy(paperWorldConfig);
            levelList.add(serverLevel);
            if (!cleanConfig.isEmpty()) {
                configFiles.put(paperWorldConfig, cleanConfig);
            }
        }
        return configFiles;
    }

    public static boolean matchesRegex(String key) {
        for (final Pattern pattern : regexPatterns) {
            if (pattern.matcher(key).matches()) {
                return true;
            }
        }
        return false;
    }

    public static String getCleanCopy(String configName) throws IOException {
        final File file = new File(configName);

        switch (Files.getFileExtension(configName)) {
            case "properties": {
                final Properties properties = new Properties();
                try (final FileInputStream inputStream = new FileInputStream(file)) {
                    properties.load(inputStream);
                }
                for (final String hiddenConfig : properties.stringPropertyNames()) {
                    if (matchesRegex(hiddenConfig)) properties.remove(hiddenConfig);
                }
                final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                properties.store(outputStream, "");
                return Arrays.stream(outputStream.toString()
                  .split("\n"))
                  .filter(line -> !line.startsWith("#"))
                  .collect(Collectors.joining("\n"));
            }
            case "yml": {
                final YamlConfiguration configuration = new YamlConfiguration();
                try {
                    configuration.load(file);
                } catch (final InvalidConfigurationException e) {
                    throw new IOException(e);
                }
                configuration.options().header(null);

                for (final String key : configuration.getKeys(true)) {
                    if (matchesRegex(key)) {
                        configuration.set(key, null);
                    }
                }
                if (configuration.getKeys(false).size() == 1) {
                    return "";
                } else {
                    return configuration.saveToString();
                }
            }
            default:
                throw new IllegalArgumentException("Bad file type " + configName);
        }
    }

}
