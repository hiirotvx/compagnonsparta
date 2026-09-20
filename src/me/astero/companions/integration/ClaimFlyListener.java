package me.astero.companions.integration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.LandType;
import fr.iban.lands.events.PlayerLandEnterEvent;
import fr.iban.lands.model.land.Land;

import me.astero.companions.CompanionsPlugin;
import me.astero.companions.companiondata.PlayerData;

/**
 * Restreint la capacite FLY (compagnon dragon) au claim MSLands du joueur lui-meme.
 *
 * - claim personnel (PLAYER) ou sous-claim d'un claim personnel (SUBLAND) dont il est proprietaire : fly autorise
 * - claim de guilde (GUILD), claim systeme, wilderness, claim d'un autre joueur : fly retire
 * - le fly retire en plein vol accorde une immunite aux degats de chute pendant quelques secondes
 *
 * Si MSLands n'est pas installe, le comportement d'origine du plugin est conserve.
 */
public class ClaimFlyListener implements Listener {

	/** Duree de l'immunite aux degats de chute apres la perte du fly, en millisecondes. */
	private static final long FALL_GRACE_MILLIS = 12_000L;

	private final CompanionsPlugin main;
	private final boolean landsPresent;

	private final Map<UUID, Long> fallGrace = new HashMap<>();

	public ClaimFlyListener(CompanionsPlugin main) {
		this.main = main;
		this.landsPresent = Bukkit.getPluginManager().getPlugin("MSLands") != null;

		if (!landsPresent) {
			main.getLogger().warning("MSLands est absent : la restriction du fly aux claims est desactivee.");
		}
	}

	/**
	 * Le joueur peut-il voler la ou il se trouve ?
	 * Appele depuis CustomAbilities.giveFly().
	 */
	public boolean canFlyHere(Player player) {

		if (!landsPresent) {
			return true;
		}

		if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
			return true;
		}

		return isOwnLand(player, LandsPlugin.getInstance().getLandRepository().getLandAt(player.getLocation()));
	}

	/** Claim personnel du joueur uniquement : les claims de guilde et systeme sont exclus. */
	private boolean isOwnLand(Player player, Land land) {

		if (land == null) {
			return false;
		}

		LandType type = land.getType();

		// GuildLand.getOwner() renvoie l'UUID de la guilde : le filtre sur le type est obligatoire.
		if (type != LandType.PLAYER && type != LandType.SUBLAND) {
			return false;
		}

		UUID owner = land.getOwner();

		return owner != null && owner.equals(player.getUniqueId());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onLandEnter(PlayerLandEnterEvent event) {

		Land from = event.getFromLand();
		Land to = event.getToLand();

		// L'event est appele a chaque deplacement : on ne fait rien tant que le claim ne change pas.
		if (from != null && to != null && from.getId() != null && from.getId().equals(to.getId())) {
			return;
		}

		refresh(event.getPlayer());
	}

	/**
	 * Re-evalue le fly du joueur selon l'endroit ou il se trouve.
	 * A appeler aussi a la connexion et a l'activation d'un compagnon.
	 */
	public void refresh(Player player) {

		PlayerData data = PlayerData.instanceOf(player);

		if (!data.hasActiveCompanionSelected()) {
			return;
		}

		boolean hadFly = data.isFlyMode();

		main.getCustomAbility().giveFly(player); // re-evalue via canFlyHere()

		if (hadFly && !data.isFlyMode() && !player.isOnGround()) {
			fallGrace.put(player.getUniqueId(), System.currentTimeMillis() + FALL_GRACE_MILLIS);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onFallDamage(EntityDamageEvent event) {

		if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
			return;
		}

		if (!(event.getEntity() instanceof Player)) {
			return;
		}

		Player player = (Player) event.getEntity();

		Long until = fallGrace.remove(player.getUniqueId());

		if (until != null && System.currentTimeMillis() <= until) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		fallGrace.remove(event.getPlayer().getUniqueId());
	}
}
