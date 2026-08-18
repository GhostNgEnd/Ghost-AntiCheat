package ac.ghost.anticheat.config;

import cn.nukkit.plugin.PluginBase;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public final class ConfigLoader {
    private ConfigLoader() {
    }

    public static Config load(PluginBase plugin) {
        final File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            return Config.DEFAULT_CONFIG;
        }

        final File configFile = new File(dataFolder, "config.yml");
        if (!configFile.exists() && !plugin.saveResource("config.yml", false)) {
            return Config.DEFAULT_CONFIG;
        }

        try {
            final cn.nukkit.utils.Config yaml =
                    new cn.nukkit.utils.Config(configFile, cn.nukkit.utils.Config.YAML);

            
            
            if (!yaml.exists("prefix")) {
                final String prefix = Config.DEFAULT_CONFIG.prefix()
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\r", "\\r")
                        .replace("\n", "\\n");
                final String existing = Files.readString(configFile.toPath(), StandardCharsets.UTF_8);
                Files.writeString(configFile.toPath(),
                        "# Prefix used by alerts, commands and kick messages. Use & for colour codes.\n"
                                + "prefix: \"" + prefix + "\"\n\n" + existing,
                        StandardCharsets.UTF_8);
                yaml.set("prefix", Config.DEFAULT_CONFIG.prefix());
            }

            return Config.fromNukkit(yaml);
        } catch (Exception exception) {
            return Config.DEFAULT_CONFIG;
        }
    }
}
