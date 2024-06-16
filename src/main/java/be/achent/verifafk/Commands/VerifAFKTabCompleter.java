package be.achent.verifafk.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class VerifAFKTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("reload");

            if (args[0].isEmpty()) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
            } else {
                String argument = args[0].toLowerCase();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    String playerName = player.getName();
                    if (playerName.toLowerCase().startsWith(argument)) {
                        completions.add(playerName);
                    }
                }
            }
        }

        return completions;
    }
}