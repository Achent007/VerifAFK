package be.achent.verifafk;

import be.achent.verifafk.ChatColorHandler.ChatColorHandler;
import be.achent.verifafk.ChatColorHandler.parsers.custom.MiniMessageParser;
import be.achent.verifafk.ChatColorHandler.parsers.custom.PlaceholderAPIParser;
import be.achent.verifafk.Commands.VerifAFKCommands;
import be.achent.verifafk.Commands.VerifAFKConfirmCommand;
import be.achent.verifafk.Commands.VerifAFKTabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.permissions.PermissionAttachment;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class VerifAFK extends JavaPlugin implements Listener {

    public static VerifAFK plugin;
    private Messages messages;
    private FileConfiguration languageConfig;
    private File languageConfigFile;
    private final Map<Player, PermissionAttachment> playerPermissions = new HashMap<>();
    private final Map<Player, Player> verifInitiators = new HashMap<>();

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
        getCommand("verifafk").setTabCompleter(new VerifAFKTabCompleter());
        getCommand("verifafkconfirm").setExecutor(new VerifAFKConfirmCommand(this));
    }

    public static VerifAFK getInstance() {
        return plugin;
    }

    public String getLanguageMessage(String path) {
        String message = this.languageConfig.getString(path);
        if (message != null) {
            return formatMessage(message);
        } else {
            getLogger().warning("Message path '" + path + "' not found in language.yml");
            return "";
        }
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

    public void addTemporaryPermission(Player player, String permission, int duration) {
        PermissionAttachment attachment = player.addAttachment(this, duration);
        assert attachment != null;
        attachment.setPermission(permission, true);
        playerPermissions.put(player, attachment);
    }

    public void removeTemporaryPermission(Player player, String permission) {
        PermissionAttachment attachment = playerPermissions.get(player);
        if (attachment != null) {
            attachment.unsetPermission(permission);
            playerPermissions.remove(player);
        }
    }

    public void addVerifInitiator(Player target, Player initiator) {
        verifInitiators.put(target, initiator);
    }

    public Player getVerifInitiator(Player target) {
        return verifInitiators.get(target);
    }

    public void removeVerifInitiator(Player target) {
        verifInitiators.remove(target);
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
