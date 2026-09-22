package me.astero.companions.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.astero.companions.CompanionsPlugin;
import me.astero.companions.companiondata.PlayerCache;
import me.astero.companions.companiondata.PlayerData;
import me.astero.companions.util.InventoryBuilder;
import me.astero.companions.util.MessageUtil;

@SuppressWarnings("deprecation")
public class UpgradeMenu {
	
	private CompanionsPlugin main;
	
	public UpgradeMenu(CompanionsPlugin main, Player player)
	{
		this.main = main;
		
		if(player.hasPermission("companions.player.upgrade"))
		{
			openInventory(player);
		}
		else
		{
			MessageUtil.sendPrefixed(player, main.getCompanionUtil().getPrefix(), main.getFileHandler().getNoPermissionMessage());
		}
	}
	
	private void openInventory(Player player)
	{
		ArrayList<String> setLore = new ArrayList<>();
		
		String activeCompanion;
		ItemStack abilityLevel = main.getFileHandler().getAbilityLevel();
		String selectedCompanion = PlayerData.instanceOf(player).getActiveCompanionName();
		
		if(!PlayerData.instanceOf(player).hasActiveCompanionSelected())
		{
			activeCompanion = "NONE";
		}
		else
		{
			activeCompanion = selectedCompanion;
			

			
			if(PlayerCache.instanceOf(player.getUniqueId()).getOwnedCache().get(activeCompanion.toLowerCase()).getAbilityLevel() == main.getFileHandler().getMaxAbilityLevel())
			{
				abilityLevel = main.getFileHandler().getAbilityLevelM();
			}

			
		}



		
		
		for(String getLore : main.getFileHandler().getUpgradeDetailsDescription())
		{
			
			try
			{
				setLore.add(getLore.replace("%active_companion%", activeCompanion)
					.replace("%companion_level%", String.valueOf(PlayerCache.instanceOf(player.getUniqueId()).getOwnedCache()
							.get(selectedCompanion.toLowerCase()).getAbilityLevel()))
					.replace("%companion_name%", PlayerCache.instanceOf(player.getUniqueId()).getOwnedCache()
								.get(selectedCompanion.toLowerCase()).getCustomName())
					.replace("%active_companion_l%", activeCompanion.substring(0, 1) + activeCompanion.substring(1).toLowerCase()));
			}
			catch(NullPointerException firstJoin)
			{
				setLore.add(getLore.replace("%active_companion%", activeCompanion)
						.replace("%companion_level%", "NONE")
						.replace("%companion_name%", "NONE")
						.replace("%active_companion_l%", activeCompanion.substring(0, 1) + activeCompanion.substring(1).toLowerCase()));
			}
		}

		
		ItemMeta upgradeDetailsMeta = main.getFileHandler().getUpgradeDetails().getItemMeta();
		List<Component> componentLore = setLore.stream()
				.map(MessageUtil::parse)
				.collect(Collectors.toList());
		upgradeDetailsMeta.lore(componentLore);
		
		main.getFileHandler().getUpgradeDetails().setItemMeta(upgradeDetailsMeta);

		// La fiche montre la tete du compagnon actif, avec la description de la config.
		// Rien ne compare cet objet au clic : changer son apparence est sans risque.
		ItemStack details = main.getFileHandler().getUpgradeDetails();
		if(PlayerData.instanceOf(player).hasActiveCompanionSelected()
				&& main.getFileHandler().getCompanionDetails().containsKey(activeCompanion.toLowerCase()))
		{
			details = main.getFileHandler().getCompanionDetails().get(activeCompanion.toLowerCase()).getOwnedItemType().clone();
			ItemMeta headMeta = details.getItemMeta();
			headMeta.lore(componentLore);
			details.setItemMeta(headMeta);
		}
		
		

		
		
		
		Inventory upgradeMenu = new InventoryBuilder(main.getFileHandler().getUpgradeAbilitiesSize(), main.getFileHandler().getUpgradeAbilitiesTitle())
				.setItem(main.getFileHandler().getGoBackUDSlot(), main.getFileHandler().getGoBackUD())
				.setItem(main.getFileHandler().getUpgradeDetailsSlot(), details)
				.setItem(main.getFileHandler().getAbilityLevelSlot(), abilityLevel)
				.setItem(main.getFileHandler().getRenameCompanionSlot(), main.getFileHandler().getRenameCompanion())
				.setItem(main.getFileHandler().getHideCompanionSlot(), main.getFileHandler().getHideCompanionN())
				.setItem(main.getFileHandler().getChangeWeaponSlot(), main.getFileHandler().getChangeWeapon())
				.build();
		main.getMenuDecor().fillEmpty(upgradeMenu);
		
		try
		{
			player.playSound(player.getLocation(), 
					Sound.valueOf(main.getFileHandler().getUpgradeAbilitiesSound()), 1.0F, 1.0F);
		}
		 catch(IllegalArgumentException soundNotFound)
		 {
			 main.getLogger().warning(ChatColor.GOLD + "COMPANIONS → " + ChatColor.RED + "Upgrade Menu sound - " + ChatColor.YELLOW + 
					 main.getFileHandler().getUpgradeAbilitiesSound() + ChatColor.RED +" is not found.");
		 }
		
		player.openInventory(upgradeMenu);
	}

}
