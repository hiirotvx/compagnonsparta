package me.astero.companions.listener.companions;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import me.astero.companions.CompanionsPlugin;

public class CompanionFollow implements Listener {

    private final CompanionsPlugin main;

    public CompanionFollow(CompanionsPlugin main) {
        this.main = main;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        main.getCompanionPacket().companionFollow(player);
    }
}
