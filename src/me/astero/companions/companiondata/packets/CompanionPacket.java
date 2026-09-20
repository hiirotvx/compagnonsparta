package me.astero.companions.companiondata.packets;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Abstraction that allows the plugin to manage companion armor stands
 * regardless of the underlying server implementation.
 */
public interface CompanionPacket {

    void loadCompanion(Player player);

    void companionFollow(Player player);

    void despawnCompanion(Player player, Player packetPlayer);

    void despawnCompanion(Player player);

    void toggleCompanion(Player player);

    void setCustomName(Player player, String newName);

    void setCustomNameVisible(Player player, boolean visible);

    void setCustomWeapon(Player player, ItemStack itemStack);
}
