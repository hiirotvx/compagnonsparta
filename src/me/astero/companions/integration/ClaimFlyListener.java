package me.astero.companions.integration;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.iban.lands.LandsPlugin;
import fr.iban.lands.enums.Action;
import fr.iban.lands.enums.LandType;
import fr.iban.lands.events.PlayerLandEnterEvent;
import fr.iban.lands.guild.AbstractGuildDataAccess;
import fr.iban.lands.model.land.GuildLand;
import fr.iban.lands.model.land.Land;
import fr.iban.lands.model.land.PlayerLand;
import fr.iban.lands.model.land.SubLand;

import me.astero.companions.CompanionsPlugin;
import me.astero.companions.companiondata.PlayerData;
import me.astero.companions.util.MessageUtil;

/**
 * Restreint la capacite FLY (compagnon Dragon) selon les claims MSLands.
 *
 * Le vol est autorise dans un claim seulement si le joueur peut A LA FOIS y casser et y
 * poser des blocs : ses propres claims, ceux ou il est trust, et les claims de sa guilde
 * selon les droits de guilde. Les mondes et les claims systeme listes dans fly.yml
 * l'interdisent toujours - la Zone sauvage en particulier, ou tout le monde construit.
 *
 * Le vol retire en plein air accorde une immunite a la premiere chute.
 * Si MSLands n'est pas installe, seule la liste des mondes s'applique.
 */
public class ClaimFlyListener implements Listener {

	/** Duree de l'immunite aux degats de chute apres la perte du fly, en millisecondes. */
	private static final long FALL_GRACE_MILLIS = 12_000L;
	private static final String FILE = "fly.yml";

	private final CompanionsPlugin main;
	private final boolean landsPresent;
	private final Map<UUID, Long> fallGrace = new HashMap<>();

	private Set<String> blockedWorlds = new HashSet<>();
	private Set<String> blockedSystemLands = new HashSet<>();
	private boolean lockUpgrade;
	private YamlConfiguration settings;

	public ClaimFlyListener(CompanionsPlugin main) {
		this.main = main;
		this.landsPresent = Bukkit.getPluginManager().getPlugin("MSLands") != null;

		if (!landsPresent) {
			main.getLogger().warning("MSLands est absent : seule la liste des mondes de fly.yml s'applique au vol.");
		}
		reloadSettings();
	}

	/** Charge fly.yml, en le creant a partir du modele du plugin s'il n'existe pas encore. */
	public void reloadSettings() {
		File file = new File(main.getDataFolder(), FILE);
		if (!file.exists()) {
			main.saveResource(FILE, false);
		}
		settings = YamlConfiguration.loadConfiguration(file);
		blockedWorlds = lower(settings.getStringList("blocked-worlds"));
		blockedSystemLands = lower(settings.getStringList("blocked-system-lands"));
		lockUpgrade = settings.getBoolean("lock-upgrade", true);
	}

	private static Set<String> lower(List<String> values) {
		Set<String> set = new HashSet<>();
		for (String value : values) {
			set.add(value.trim().toLowerCase(Locale.ROOT));
		}
		return set;
	}

	// ------------------------------------------------------------------ vol

	public boolean canFlyHere(Player player) {
		Land land = landsPresent
				? LandsPlugin.getInstance().getLandRepository().getLandAt(player.getLocation())
				: null;
		return canFlyIn(player, land, player.getWorld());
	}

	/**
	 * Le joueur peut-il voler dans ce claim, dans ce monde ?
	 *
	 * A utiliser pendant un deplacement : MSLands declenche PlayerLandEnterEvent pendant
	 * le PlayerMoveEvent, quand player.getLocation() designe encore la case de DEPART.
	 * Il faut donc evaluer le claim de destination fourni par l'evenement.
	 */
	public boolean canFlyIn(Player player, Land land, World world) {

		if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
			return true;
		}

		if (world != null && blockedWorlds.contains(world.getName().toLowerCase(Locale.ROOT))) {
			return false;
		}

		if (!landsPresent) {
			return true;
		}

		if (land == null) {
			return false;
		}

		if (land.getType() == LandType.SYSTEM
				&& land.getName() != null
				&& blockedSystemLands.contains(land.getName().toLowerCase(Locale.ROOT))) {
			return false;
		}

		return can(player, land, Action.BLOCK_BREAK) && can(player, land, Action.BLOCK_PLACE);
	}

	/**
	 * Meme regle que Land#isBypassing de MSLands, SANS ses effets de bord : isBypassing
	 * envoie "Vous n'avez pas la permission..." au joueur quand la reponse est non, ce qui
	 * spammerait tous ceux qui traversent le claim d'un autre.
	 */
	private boolean can(Player player, Land land, Action action) {
		UUID uuid = player.getUniqueId();
		LandsPlugin lands = LandsPlugin.getInstance();

		// Proprietaire d'un claim personnel ou d'un sous-claim de claim personnel.
		if ((land instanceof PlayerLand || land instanceof SubLand) && uuid.equals(land.getOwner())) {
			return true;
		}

		AbstractGuildDataAccess guilds = lands.isGuildsHookEnabled() ? lands.getGuildDataAccess() : null;

		// Claim de guilde : le chef, ou un membre si les droits de guilde l'autorisent.
		if (land instanceof GuildLand guildLand && guilds != null) {
			UUID guildId = guildLand.getGuildId();
			if (guilds.isGuildLeader(uuid, guildId)
					|| (guilds.isGuildMember(uuid, guildId) && hasTrust(land.getGuildTrust(), action))) {
				return true;
			}
		}

		return hasTrust(land.getGlobalTrust(), action)
				|| land.isTrusted(uuid, action)
				|| lands.isBypassing(player)
				|| (guilds != null
					&& land.getOwner() != null
					&& hasTrust(land.getGuildTrust(), action)
					&& guilds.areInSameGuild(land.getOwner(), uuid));
	}

	private static boolean hasTrust(fr.iban.lands.model.Trust trust, Action action) {
		return trust != null && trust.hasPermission(action);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onLandEnter(PlayerLandEnterEvent event) {

		Land from = event.getFromLand();
		Land to = event.getToLand();

		// L'event est appele a chaque deplacement : on ne fait rien tant que le claim ne change pas.
		if (from != null && to != null && from.getId() != null && from.getId().equals(to.getId())) {
			return;
		}

		// Claim de DESTINATION, fourni par l'evenement : le joueur n'y est pas encore.
		Player player = event.getPlayer();
		apply(player, canFlyIn(player, to, player.getWorld()));
	}

	/** Changement de monde (portail, teleportation) : on reevalue une fois arrive. */
	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event) {
		refresh(event.getPlayer());
	}

	public void refresh(Player player) {
		apply(player, canFlyHere(player));
	}

	/** Accorde ou retire le vol selon la decision deja prise, avec la protection contre la chute. */
	private void apply(Player player, boolean allowedHere) {

		PlayerData data = PlayerData.instanceOf(player);

		if (!data.hasActiveCompanionSelected()) {
			return;
		}

		boolean hadFly = data.isFlyMode();

		main.getCustomAbility().giveFly(player, allowedHere);

		if (hadFly && !data.isFlyMode() && !player.isOnGround()) {
			fallGrace.put(player.getUniqueId(), System.currentTimeMillis() + FALL_GRACE_MILLIS);
		}
	}

	// ------------------------------------------------------------------ atelier

	/** L'amelioration de capacite est-elle bloquee pour ce compagnon (compagnons volants) ? */
	public boolean isUpgradeLocked(String companion) {
		if (!lockUpgrade || companion == null) {
			return false;
		}
		var details = main.getFileHandler().getCompanionDetails().get(companion.toLowerCase(Locale.ROOT));
		return details != null && details.getAbilityList().contains("FLY");
	}

	public String upgradeLockedMessage() {
		return settings.getString("messages.upgrade-locked", "<#ff8a8a>Ce compagnon ne peut pas être amélioré.");
	}

	/** Objet affiche dans l'atelier a la place de l'amelioration, quand elle est bloquee. */
	public ItemStack lockedItem() {
		Material type = Material.matchMaterial(settings.getString("locked-item.type", "BARRIER"));
		ItemStack item = new ItemStack(type != null ? type : Material.BARRIER);
		ItemMeta meta = item.getItemMeta();
		meta.displayName(MessageUtil.parse(settings.getString("locked-item.name",
				"<!italic><#8fa0ae><b>Amélioration indisponible</b>")));
		List<String> lore = settings.getStringList("locked-item.description");
		if (!lore.isEmpty()) {
			meta.lore(lore.stream().map(MessageUtil::parse).toList());
		}
		item.setItemMeta(meta);
		return item;
	}

	// ------------------------------------------------------------------ chute

	@EventHandler(ignoreCancelled = true)
	public void onFallDamage(EntityDamageEvent event) {

		if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
			return;
		}

		if (!(event.getEntity() instanceof Player player)) {
			return;
		}

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
