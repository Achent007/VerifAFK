package be.achent.verifafk.Commands;

import be.achent.verifafk.VerifAFK;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VerifAFKConfirmCommand implements CommandExecutor {

    private final VerifAFK plugin;

    public VerifAFKConfirmCommand(VerifAFK plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageMessage("messages.Player only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("verifafk.confirm")) {
            player.sendMessage(plugin.getLanguageMessage("messages.Not under verification"));
            return true;
        }

        plugin.removeTemporaryPermission(player, "verifafk.confirm");
        player.sendMessage(plugin.formatMessage(plugin.getConfig().getString("Presence confirmed")));

        Player initiator = plugin.getVerifInitiator(player);
        if (initiator != null) {
            initiator.sendMessage(plugin.formatMessage(plugin.getConfig().getString("Player make the confirmation").replace("{player}", sender.getName())));
            plugin.removeVerifInitiator(player);
        }

        return true;
    }
}
