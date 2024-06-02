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
import org.bukkit.permissions.PermissionAttachment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

        Objects.requireNonNull(getCommand("verifafk")).setExecutor(new VerifAFKCommands(this));
        Objects.requireNonNull(getCommand("verifafkconfirm")).setExecutor(new VerifAFKConfirmCommand(this));
    }

    public static VerifAFK getInstance() {
        return plugin;
    }

    public String getMessage(String path) {
        String message = this.messages.get().getString(path);
        if (message != null) {
            return formatMessage(message);
        } else {
            getLogger().warning("Message path '" + path + "' not found in messages.yml");
            return "";
        }
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

    public void updateConfigFile(String configFile, String defaultConfigFile) {
        File file = new File(getDataFolder(), configFile);
        if (!file.exists()) {
            saveResource(configFile, false);
        } else {
            FileConfiguration currentConfig = YamlConfiguration.loadConfiguration(file);
            InputStream defaultStream = getResource(defaultConfigFile);
            if (defaultStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
                boolean changed = false;

                for (String key : defaultConfig.getKeys(true)) {
                    if (!currentConfig.contains(key)) {
                        currentConfig.set(key, defaultConfig.get(key));
                        changed = true;
                    }
                }

                if (changed) {
                    try {
                        currentConfig.save(file);
                    } catch (IOException e) {
                        getLogger().log(Level.SEVERE, "Could not save config to " + file, e);
                    }
                }
            }
        }
    }

    public String formatMessage(String message) {
        if (message == null) {
            return "";
        }
        String prefix = this.languageConfig.getString("messages.prefix");
        assert prefix != null;
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
}
