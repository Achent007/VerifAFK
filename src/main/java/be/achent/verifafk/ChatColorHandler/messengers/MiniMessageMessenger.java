package be.achent.verifafk.ChatColorHandler.messengers;

import be.achent.verifafk.ChatColorHandler.ChatColorHandler;
import be.achent.verifafk.ChatColorHandler.parsers.custom.HexParser;
import be.achent.verifafk.ChatColorHandler.parsers.custom.MiniMessageParser;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class MiniMessageMessenger extends AbstractMessenger {
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public void sendMessage(@NotNull CommandSender recipient, @Nullable String message) {
        if (message == null || message.isBlank()) return;

        Audience audience = Audience.audience((Audience) recipient);

        String legacyParsed = legacyParser(ChatColorHandler.translateAlternateColorCodes(message, (recipient instanceof Player player ? player : null), List.of(MiniMessageParser.class, HexParser.class)));
        Component parsed = miniMessage.deserialize(legacyParsed);

        audience.sendMessage(parsed);
    }

    @Override
    public void broadcastMessage(@Nullable String message) {
        if (message == null || message.isBlank()) return;

        Audience audience = Audience.audience((Audience) Bukkit.getServer());
        String legacyParsed = legacyParser(ChatColorHandler.translateAlternateColorCodes(message, List.of(MiniMessageParser.class, HexParser.class)));
        audience.sendMessage(miniMessage.deserialize(legacyParsed));
    }

    @Override
    public void sendActionBarMessage(@NotNull Player player, @Nullable String message) {
        if (message == null || message.isBlank()) return;

        Audience audience = Audience.audience((Audience) player);
        String legacyParsed = legacyParser(ChatColorHandler.translateAlternateColorCodes(message, player, List.of(MiniMessageParser.class, HexParser.class)));
        audience.sendActionBar(miniMessage.deserialize(legacyParsed));
    }

    private String legacyParser(String string) {
        string = string.replace('§', '&');
        string = HexParser.parseToMiniMessage(string);

        return string
                .replaceAll("&0", "<reset><black>")
                .replaceAll("&1", "<reset><dark_blue>")
                .replaceAll("&2", "<reset><dark_green>")
                .replaceAll("&3", "<reset><dark_aqua>")
                .replaceAll("&4", "<reset><dark_red>")
                .replaceAll("&5", "<reset><dark_purple>")
                .replaceAll("&6", "<reset><gold>")
                .replaceAll("&7", "<reset><grey>")
                .replaceAll("&8", "<reset><dark_grey>")
                .replaceAll("&9", "<reset><blue>")
                .replaceAll("&a", "<reset><green>")
                .replaceAll("&b", "<reset><aqua>")
                .replaceAll("&c", "<reset><red>")
                .replaceAll("&d", "<reset><light_purple>")
                .replaceAll("&e", "<reset><yellow>")
                .replaceAll("&f", "<reset><white>")

                .replaceAll("&m", "<strikethrough>")
                .replaceAll("&k", "<obfuscated>")
                .replaceAll("&n", "<underlined>")
                .replaceAll("&o", "<italic>")
                .replaceAll("&l", "<bold>")
                .replaceAll("&r", "<reset>");
    }
}
