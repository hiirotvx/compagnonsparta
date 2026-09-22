package me.astero.companions.listener;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.MetadataValue;

import me.astero.companions.CompanionsPlugin;
import me.astero.companions.companiondata.PlayerData;
import me.astero.companions.util.MessageUtil;

/**
 * Masque le compagnon d'un joueur pendant son vanish, et le lui rend a la sortie.
 *
 * Le vanish est detecte par la metadonnee "vanished", convention partagee par EssentialsX,
 * SuperVanish, PremiumVanish et VanishNoPacket. On ne depend donc ni du plugin de vanish
 * utilise, ni de la commande tapee : /vanish, /v Pseudo, vanish a la connexion ou pose
 * par un autre plugin sont tous couverts.
 *
 * Seuls les compagnons masques PAR CE MECANISME sont reaffiches : un joueur qui avait
 * range son compagnon avant de passer en vanish ne le voit pas revenir tout seul.
 */
public class VanishListener implements Listener {

	private final CompanionsPlugin main;
	private final Set<UUID> hiddenByVanish = new HashSet<>();

	public VanishListener(CompanionsPlugin main)
	{
		this.main = main;
		// Verification toutes les secondes : les plugins de vanish n'exposent pas
		// d'evenement commun, la metadonnee est le seul signal partage.
		Bukkit.getScheduler().runTaskTimer(main, this::checkAll, 20L, 20L);
	}

	public static boolean isVanished(Player player)
	{
		for (MetadataValue value : player.getMetadata("vanished"))
		{
			if (value.asBoolean())
			{
				return true;
			}
		}
		return false;
	}

	private void checkAll()
	{
		for (Player player : Bukkit.getOnlinePlayers())
		{
			check(player);
		}
	}

	private void check(Player player)
	{
		PlayerData data = PlayerData.instanceOf(player);
		UUID id = player.getUniqueId();

		if (isVanished(player))
		{
			if (data.hasActiveCompanionSelected() && !data.isToggled())
			{
				data.toggleCompanion();
				hiddenByVanish.add(id);
				MessageUtil.sendPrefixed(player, main.getCompanionUtil().getPrefix(),
						main.getFileHandler().getPlayerInVanishMessage());
			}
		}
		else if (hiddenByVanish.remove(id))
		{
			if (data.hasActiveCompanionSelected() && data.isToggled())
			{
				data.setToggled(false);
				main.getCompanionPacket().loadCompanion(player);
				// Rend le vol du Dragon si le joueur se trouve dans son propre claim.
				main.getClaimFly().refresh(player);
				MessageUtil.sendPrefixed(player, main.getCompanionUtil().getPrefix(),
						main.getFileHandler().getPlayerNotInVanishMessage());
			}
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		// L'etat "masque" n'est garde qu'en memoire : a la reconnexion, le compagnon est
		// recharge normalement, puis remasque dans la seconde si le joueur est toujours en vanish.
		hiddenByVanish.remove(event.getPlayer().getUniqueId());
	}
}
