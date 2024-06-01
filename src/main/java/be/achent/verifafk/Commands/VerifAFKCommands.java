package be.achent.verifafk.Commands;

import be.achent.verifafk.VerifAFK;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VerifAFKCommands implements CommandExecutor {

    private final VerifAFK plugin;

    public VerifAFKCommands(VerifAFK plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("verifafk.reload")) {
                sender.sendMessage(plugin.getLanguageMessage("NoPermission"));
                return true;
            }
            plugin.reloadConfig();
            plugin.reloadLanguageConfig();
            sender.sendMessage(plugin.getLanguageMessage("Reloaded"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(plugin.getLanguageMessage("Incorrect_usage"));
            return false;
        }

        if (!sender.hasPermission("verifafk.use")) {
            sender.sendMessage(plugin.getLanguageMessage("NoPermission"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.getLanguageMessage("Player_offline"));
            return false;
        }

        // Récupère les configurations du fichier config.yml
        String message = plugin.getConfig().getString("afk-message");
        String sound = plugin.getConfig().getString("afk-sound");
        String title = plugin.getConfig().getString("afk-title");
        String subtitle = plugin.getConfig().getString("afk-subtitle");

        // Récupère le message d'erreur du fichier language.yml
        String invalidSoundMessage = plugin.getLanguageMessage("Invalid_sound");

        // Envoie le message au joueur
        target.sendMessage(message);

        // Joue le son pour le joueur
        try {
            Sound afkSound = Sound.valueOf(sound);
            target.playSound(target.getLocation(), afkSound, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(invalidSoundMessage);
            return false;
        }

        // Envoie le titre au joueur
        target.sendTitle(title, subtitle, 10, 70, 20);

        return true;
    }
}
