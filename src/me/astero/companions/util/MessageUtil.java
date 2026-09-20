package me.astero.companions.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

public final class MessageUtil {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private MessageUtil() {}

    /**
     * Auto-detects format: uses MiniMessage if tags are present, otherwise legacy '&' codes.
     */
    public static Component parse(String text) {
        if (text == null) return Component.empty();
        if (text.contains("<") && text.contains(">")) {
            return MM.deserialize(text);
        }
        return LEGACY.deserialize(text);
    }

    /**
     * Parses a legacy '&' color-coded string into a Component.
     * Use for player-entered data (custom names, etc.).
     */
    public static Component legacy(String text) {
        if (text == null) return Component.empty();
        return LEGACY.deserialize(text);
    }

    /**
     * Parses a MiniMessage string into a Component.
     */
    public static Component mm(String text) {
        if (text == null) return Component.empty();
        return MM.deserialize(text);
    }

    /**
     * Sends a message to a sender, auto-detecting format.
     */
    public static void send(CommandSender sender, String message) {
        sender.sendMessage(parse(message));
    }

    /**
     * Sends a prefixed message to a sender, auto-detecting format.
     */
    public static void sendPrefixed(CommandSender sender, String prefix, String message) {
        sender.sendMessage(parse(prefix + message));
    }
}
