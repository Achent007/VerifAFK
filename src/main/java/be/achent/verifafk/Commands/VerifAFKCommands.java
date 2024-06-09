package be.achent.verifafk.Commands;

import be.achent.verifafk.VerifAFK;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.Random;

public class VerifAFKCommands implements CommandExecutor {

    private final VerifAFK plugin;
    private final Random random = new Random();

    public VerifAFKCommands(VerifAFK plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("verifafk.reload")) {
                sender.sendMessage(plugin.getLanguageMessage("messages.No Permission"));
                return true;
            }
            plugin.reloadConfig();
            plugin.reloadLanguageConfig();
            sender.sendMessage(plugin.getLanguageMessage("messages.Reloaded"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(plugin.getLanguageMessage("messages.Incorrect usage"));
            return false;
        }

        if (!sender.hasPermission("verifafk.use")) {
            sender.sendMessage(plugin.getLanguageMessage("messages.No Permission"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(plugin.formatMessage(plugin.getConfig().getString("Verification offline player")).replace("{player}", args[0]));
            return false;
        }

        List<String> messages = plugin.getConfig().getStringList("Confirmations messages");
        if (messages.isEmpty()) {
            sender.sendMessage(plugin.getLanguageMessage("messages.No confirmation messages"));
            return false;
        }

        String message = messages.get(random.nextInt(messages.size()));

        String sound = plugin.getConfig().getString("Verification sound");
        String title = plugin.getConfig().getString("Verification title");
        String subtitle = plugin.getConfig().getString("Verification subtitle");
        String successMessage = plugin.getConfig().getString("Verification message");
        String tooltipMessage = plugin.getConfig().getString("Hover confirmation message");

        TextComponent afkMessage = new TextComponent(plugin.formatMessage(message));
        afkMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(plugin.formatMessage(tooltipMessage)).create()));
        afkMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/verifafkconfirm " + sender.getName()));
        target.spigot().sendMessage(afkMessage);

        try {
            Sound afkSound = Sound.valueOf(sound);
            target.playSound(target.getLocation(), afkSound, (float) plugin.getConfig().getDouble("Sound volume"), (float) plugin.getConfig().getDouble("Sound pitch"));
        } catch (IllegalArgumentException e) {
            sender.sendMessage(plugin.getLanguageMessage("messages.Invalid sound"));
            return false;
        }

        target.sendTitle(
                plugin.formatMessage(title),
                plugin.formatMessage(subtitle),
                plugin.getConfig().getInt("Title fadeIn"),
                plugin.getConfig().getInt("Title stay"),
                plugin.getConfig().getInt("Title fadeOut")
        );

        sender.sendMessage(plugin.formatMessage(successMessage).replace("{player}", target.getName()));

        int permissionDuration = plugin.getConfig().getInt("Expiry duration");
        plugin.addTemporaryPermission(target, "verifafk.confirm", permissionDuration);

        plugin.addVerifInitiator(target, (Player) sender);

        return true;
    }
}