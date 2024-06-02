package be.achent.verifafk;

import be.achent.verifafk.ChatColorHandler.ChatColorHandler;
import be.achent.verifafk.ChatColorHandler.parsers.custom.MiniMessageParser;
import be.achent.verifafk.ChatColorHandler.parsers.custom.PlaceholderAPIParser;
import be.achent.verifafk.Commands.VerifAFKCommands;
import be.achent.verifafk.Commands.VerifAFKConfirmCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public final class VerifAFK extends JavaPlugin implements Listener {

    public static VerifAFK plugin;
    private Messages messages;
    private FileConfiguration languageConfig;
    private File languageConfigFile;
    private final Set<Player> confirmedPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        plugin = this;
        this.messages = new Messages();
        this.messages.saveDefaultConfig();
        saveDefaultConfig();
        loadLanguageConfig();
        updateConfigFile("config.yml", "config-default.yml");
        updateConfigFile("language.yml", "language-default.yml");

        getCommand("verifafk").setExecutor(new VerifAFKCommands(this));
        getCommand("verifafkconfirm").setExecutor(new VerifAFKConfirmCommand(this));
    }

    public static VerifAFK getInstance() {
        return plugin;
    }

    public String getMessage(String path) {
        String message = this.messages.get().getString(path);
        if (message != null) {
            return formatMessage(message);
        }
        return "";
    }

    public String getLanguageMessage(String path) {
        String message = this.languageConfig.getString(path);
        return message != null ? formatMessage(message) : "";
    }

    public void reloadMessages() {
        this.messages.reload();
    }

    public void saveDefaultsMessages() {
        this.messages.saveDefaultConfig();
    }

    public void reloadLanguageConfig() {
        if (languageConfigFile == null) {
            languageConfigFile = new File(getDataFolder(), "language.yml");
        }
        if (!languageConfigFile.exists()) {
            saveResource("language.yml", false);
        }
        languageConfig = YamlConfiguration.loadConfiguration(languageConfigFile);

        InputStream defaultStream = getResource("language-default.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            languageConfig.setDefaults(defaultConfig);
        }
    }

    public FileConfiguration getLanguageConfig() {
        if (languageConfig == null) {
            reloadLanguageConfig();
        }
        return languageConfig;
    }

    private void loadLanguageConfig() {
        File languageFile = new File(getDataFolder(), "language.yml");
        if (!languageFile.exists()) {
            saveResource("language.yml", false);
        }
        languageConfig = YamlConfiguration.loadConfiguration(languageFile);
    }

    public String formatMessage(String message) {
        if (message == null) {
            return "";
        }
        String prefix = this.languageConfig.getString("messages.prefix");
        message = message.replace("{prefix}", prefix);
        return ChatColorHandler.translateAlternateColorCodes(message, List.of(PlaceholderAPIParser.class, MiniMessageParser.class));
    }

    public void confirmPlayer(Player player) {
        confirmedPlayers.add(player);
    }

    public boolean isPlayerConfirmed(Player player) {
        return confirmedPlayers.contains(player);
    }

    public void removePlayerConfirmation(Player player) {
        confirmedPlayers.remove(player);
    }

    private void updateConfigFile(String fileName, String defaultFileName) {
        File configFile = new File(getDataFolder(), fileName);
        if (!configFile.exists()) {
            saveResource(fileName, false);
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        InputStream defaultConfigStream = getResource(defaultFileName);
        if (defaultConfigStream == null) {
            getLogger().log(Level.SEVERE, "Default configuration file " + defaultFileName + " not found.");
            return;
        }

        FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream));
        for (String key : defaultConfig.getKeys(true)) {
            if (!config.contains(key)) {
                config.set(key, defaultConfig.get(key));
            }
        }

        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
