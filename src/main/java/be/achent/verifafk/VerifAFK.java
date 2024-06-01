package be.achent.verifafk;

import be.achent.verifafk.ChatColorHandler.ChatColorHandler;
import be.achent.verifafk.ChatColorHandler.parsers.custom.MiniMessageParser;
import be.achent.verifafk.ChatColorHandler.parsers.custom.PlaceholderAPIParser;
import be.achent.verifafk.Commands.VerifAFKCommands;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public final class VerifAFK extends JavaPlugin implements Listener {

    public static VerifAFK plugin;
    private Messages messages;
    private FileConfiguration languageConfig;

    @Override
    public void onEnable() {
        plugin = this;
        this.messages = new Messages();
        this.messages.saveDefaultConfig();
        saveDefaultConfig();
        loadLanguageConfig();

        getCommand("verifafk").setExecutor(new VerifAFKCommands(this));
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
        File languageFile = new File(getDataFolder(), "language.yml");
        if (languageFile.exists()) {
            languageConfig = YamlConfiguration.loadConfiguration(languageFile);
        }
    }

    private void loadLanguageConfig() {
        File languageFile = new File(getDataFolder(), "language.yml");
        if (!languageFile.exists()) {
            saveResource("language.yml", false);
        }
        languageConfig = YamlConfiguration.loadConfiguration(languageFile);
    }

    public String formatMessage(String message) {
        String prefix = this.languageConfig.getString("prefix");
        message = message.replace("{prefix}", prefix);
        return ChatColorHandler.translateAlternateColorCodes(message, List.of(PlaceholderAPIParser.class, MiniMessageParser.class));
    }
}
