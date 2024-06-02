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
            sender.sendMessage(plugin.formatMessage(plugin.getConfig().getString("Player_only")));
            return true;
        }

        Player player = (Player) sender;
        if (args.length != 1) {
            player.sendMessage(plugin.formatMessage(plugin.getConfig().getString("Incorrect_confirm_usage")));
            return false;
        }

        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.formatMessage(plugin.getConfig().getString("Player_offline")).replace("{player}", args[0]));
            return true;
        }

        if (plugin.isPlayerConfirmed(player)) {
            player.sendMessage(plugin.formatMessage(plugin.getConfig().getString("Already_Confirmed")));
            return true;
        }

        String message = plugin.getConfig().getString("Player_present");
        target.sendMessage(plugin.formatMessage(message).replace("{player}", player.getName()));

        String confirmMessage = plugin.getConfig().getString("Player_confirmed");
        player.sendMessage(plugin.formatMessage(confirmMessage));

        plugin.confirmPlayer(player);

        return true;
    }
}
