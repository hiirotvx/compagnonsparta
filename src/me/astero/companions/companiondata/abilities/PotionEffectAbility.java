package me.astero.companions.companiondata.abilities;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.astero.companions.CompanionsPlugin;
import me.astero.companions.companiondata.PlayerCache;
import me.astero.companions.companiondata.PlayerData;
import org.bukkit.ChatColor;

public class PotionEffectAbility {
	private CompanionsPlugin main;

	public PotionEffectAbility(CompanionsPlugin main)
	{
		this.main = main;
	}

	/**
	 * Safely resolves a PotionEffectType by name, returning null if not found or invalid for this server version.
	 */
	private PotionEffectType resolvePotionType(String name)
	{
		try
		{
			PotionEffectType type = PotionEffectType.getByName(name);
			return type;
		}
		catch(IllegalArgumentException | NullPointerException e)
		{
			return null;
		}
	}

	public void give(Player player)
	{
		String activeCompanion = PlayerData.instanceOf(player).getActiveCompanionName().toLowerCase();
		for(String potionEffect : main.getFileHandler().getCompanionDetails().get(activeCompanion).getAbilityList())
		{
			if(!potionEffect.equals("NONE") && !main.getCompanionUtil().getCustomAbilities().contains(potionEffect) && !potionEffect.contains("_DEFENSE_CHANCE")
					&& !potionEffect.contains("_ATTACK_CHANCE") && !potionEffect.contains("COMMAND") )
			{
				String potionName = getPotionName(potionEffect);
				PotionEffectType effectType = resolvePotionType(potionName);

				if(effectType != null)
				{
					try
					{
						player.addPotionEffect(new PotionEffect(effectType, Integer.MAX_VALUE,
								PlayerCache.instanceOf(player.getUniqueId()).getOwnedCache().get(activeCompanion).getAbilityLevel() - 1));
					}
					catch(IllegalArgumentException ignored) {}
				}
				else
				{
					main.getLogger().warning(ChatColor.GOLD + "COMPANIONS → " + ChatColor.YELLOW + potionEffect + ChatColor.GRAY + " potion effect has failed to load. - "
						+ "Please check if the potion effect name is for the correct Minecraft server version. ");
				}
			}
		}
	}

	public void remove(Player player)
	{
		try
		{
			String activeCompanion = PlayerData.instanceOf(player).getActiveCompanionName().toLowerCase();

			for(String potionEffect : main.getFileHandler().getCompanionDetails().get(activeCompanion).getAbilityList())
			{
				if(!main.getCompanionUtil().getCustomAbilities().contains(potionEffect))
				{
					String potionName = getPotionName(potionEffect);
					PotionEffectType effectType = resolvePotionType(potionName);

					if(effectType != null && player.hasPotionEffect(effectType))
					{
						player.removePotionEffect(effectType);
					}
				}
				else
				{
					if(potionEffect.equals("MINING_VISION"))
					{
						PotionEffectType nightVision = resolvePotionType("NIGHT_VISION");
						if(nightVision != null)
						{
							player.removePotionEffect(nightVision);
						}
					}
				}
			}
		}
		catch(NullPointerException noActiveCompanion) {}
	}

	public String getPotionName(String potionEffect)
	{
		String potionName;
		try
		{
			potionName = potionEffect.split("@")[1];
		}
		catch(ArrayIndexOutOfBoundsException e ) {
			potionName = potionEffect;


		} // potion start level not specified

		return potionName;
	}
}
