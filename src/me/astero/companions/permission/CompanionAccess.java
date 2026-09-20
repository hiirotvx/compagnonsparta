package me.astero.companions.permission;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import me.astero.companions.CompanionsPlugin;
import me.astero.companions.companiondata.CustomCompanion;
import me.astero.companions.companiondata.PlayerCache;
import me.astero.companions.companiondata.PlayerData;
import me.astero.companions.filemanager.CompanionDetails;
import me.astero.companions.util.MessageUtil;

/**
 * Remplace l'achat en boutique : la propriete d'un compagnon devient la permission
 * companions.buy.<compagnon>. Le stockage (DB ou YML) ne sert plus qu'a conserver
 * les personnalisations (nom custom, arme, niveau d'ability).
 */
public class CompanionAccess {

	public static final String NODE = "companions.buy.";

	private final CompanionsPlugin main;

	public CompanionAccess(CompanionsPlugin main) {
		this.main = main;
	}

	/** Ce joueur a-t-il acces a ce compagnon ? */
	public boolean owns(Player player, String companion) {

		if (player == null || companion == null) {
			return false;
		}

		return player.hasPermission(NODE + companion.toLowerCase());
	}

	/** Liste des compagnons accessibles au joueur, dans l'ordre de companions.yml. */
	public List<String> owned(Player player) {

		List<String> list = new ArrayList<>();

		for (String name : main.getFileHandler().getCompanionDetails().keySet()) {
			if (owns(player, name)) {
				list.add(name);
			}
		}

		return list;
	}

	/**
	 * Aligne le cache memoire sur les permissions : ajoute ce qui a ete accorde,
	 * retire ce qui a ete revoque, et desactive le compagnon actif s'il ne l'est plus.
	 * A appeler a la connexion et avant toute activation.
	 */
	public void sync(Player player) {

		UUID uuid = player.getUniqueId();
		Map<String, CustomCompanion> cache = PlayerCache.instanceOf(uuid).getOwnedCache();

		for (String name : main.getFileHandler().getCompanionDetails().keySet()) {

			if (owns(player, name)) {

				if (!cache.containsKey(name)) {

					CompanionDetails details = main.getFileHandler().getCompanionDetails().get(name);

					main.getCompanionUtil().updateCache(uuid, name,
							details.getName(),
							details.getWeapon(),
							details.isNameVisible(),
							details.getAbilityLevel());
				}
			}
			else {
				cache.remove(name);
			}
		}

		PlayerData data = PlayerData.instanceOf(player);

		if (data.hasActiveCompanionSelected() && !owns(player, data.getActiveCompanionName())) {
			deactivate(player, false);
		}
	}

	/** Active le compagnon demande. Renvoie false si le joueur n'y a pas droit ou s'il n'existe pas. */
	public boolean activate(Player player, String companion) {

		String name = companion.toLowerCase();

		if (!owns(player, name)) {
			return false;
		}

		if (!main.getFileHandler().getCompanionDetails().containsKey(name)) {
			return false;
		}

		sync(player);

		PlayerData data = PlayerData.instanceOf(player);

		if (data.hasActiveCompanionSelected()) {
			data.removeCompanion();
		}

		data.setActiveCompanionName(name.toUpperCase());

		main.getCompanionUtil().storeActiveDB(name, player);
		main.getCompanionUtil().storeActiveYML(player, name);

		main.getCompanionPacket().loadCompanion(player);

		return true;
	}

	/** Retire le compagnon actif. Renvoie false s'il n'y en avait aucun. */
	public boolean deactivate(Player player, boolean notify) {

		PlayerData data = PlayerData.instanceOf(player);

		if (!data.hasActiveCompanionSelected()) {
			return false;
		}

		data.removeCompanion();

		main.getCompanionUtil().storeActiveDB("NONE", player);
		main.getCompanionUtil().storeActiveYML(player, "NONE");

		data.setActiveCompanionName("NONE");

		if (notify) {
			MessageUtil.send(player, main.getCompanionUtil().getPrefix()
					+ main.getFileHandler().getRemoveCompanionMessage());
		}

		return true;
	}
}
