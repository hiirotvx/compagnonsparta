package me.astero.companions.listener.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import me.astero.companions.CompanionsPlugin;
import me.astero.companions.companiondata.PlayerData;
import me.astero.companions.economy.EconomyHandler;
import me.astero.companions.gui.ShopMenu;
import me.astero.companions.util.MessageUtil;

public class ShopMenuListener implements Listener {

	private CompanionsPlugin main;

	public ShopMenuListener(CompanionsPlugin main)
	{
		this.main = main;
	}

	@EventHandler
	public void onClick (InventoryClickEvent e)
	{
		Player player = (Player) e.getWhoClicked();

		try
		{
			Component title = e.getView().title();
			boolean shopMenu = title.equals(MessageUtil.parse(main.getFileHandler().getCompanionShopTitle()));

			if(shopMenu)
			{
				if(e.getCurrentItem() != null)
				{
					e.setCancelled(true);

					Component currentName = e.getCurrentItem().getItemMeta().displayName();
					if(currentName == null) return;

					if(currentName.equals(MessageUtil.parse(main.getFileHandler().getGoBackName())))
					{
						PlayerData.instanceOf(player).setPageNumber(PlayerData.instanceOf(player).getPageNumber() - 1);

						if(PlayerData.instanceOf(player).getPageNumber() == 0)
						{
							Bukkit.dispatchCommand(player, main.getFileHandler().getCompanionsShopGoBackCommand());
						}
						else
						{
							Bukkit.dispatchCommand(player, "companions shop");
						}
					}
					else if(currentName.equals(MessageUtil.parse(main.getFileHandler().getNextPageName())))
					{
						PlayerData.instanceOf(player).setPageNumber(PlayerData.instanceOf(player).getPageNumber() + 1);
						new ShopMenu(main, player);
					}

					for(String getCompanionName : main.getFileHandler().getCompanionDetails().keySet())
					{
						if(MessageUtil.parse(main.getFileHandler().getCompanionDetails().get(getCompanionName).getItemName()).equals(currentName))
						{

							if(main.getFileHandler().getCompanionDetails().get(getCompanionName).getRawPrice().contains("C"))
							{

								long amount = main.getFileHandler().getCompanionDetails().get(getCompanionName).getItemPrice();

								if(main.getCompanionCoin().has(player, amount))
									main.getCompanionCoin().withdrawPlayer(player, amount);

								else
								{
									MessageUtil.sendPrefixed(player, main.getCompanionUtil().getPrefix(), main.getFileHandler().getNotEnoughMoneyMessage()
											.replace("%price%", main.getFileHandler().getCompanionDetails().get(getCompanionName).getFormatedPrice()));

									return;
								}
							}
							else
							{
								if(main.getFileHandler().isVault())
								{

									if(EconomyHandler.getEconomy().has(player, main.getFileHandler().getCompanionDetails().get(getCompanionName).getItemPrice()))
									{
										EconomyHandler.getEconomy().withdrawPlayer(player, main.getFileHandler().getCompanionDetails().get(getCompanionName).getItemPrice());
									}
									else if(!EconomyHandler.getEconomy().has(player, main.getFileHandler().getCompanionDetails().get(getCompanionName).getItemPrice()))
									{
										MessageUtil.sendPrefixed(player, main.getCompanionUtil().getPrefix(), main.getFileHandler().getNotEnoughMoneyMessage()
												.replace("%price%", main.getFileHandler().getCompanionDetails().get(getCompanionName).getFormatedPrice()));
										return;
									}
								}
							}
								main.getCompanionUtil().storeNewYML(getCompanionName, player);

								main.getCompanionUtil().updateCache(player.getUniqueId(), getCompanionName,
										main.getFileHandler().getCompanionDetails().get(getCompanionName).getName(),
										main.getFileHandler().getCompanionDetails().get(getCompanionName).getWeapon(),
										main.getFileHandler().getCompanionDetails().get(getCompanionName).isNameVisible(),
										main.getFileHandler().getCompanionDetails().get(getCompanionName).getAbilityLevel());


								MessageUtil.sendPrefixed(player, main.getCompanionUtil().getPrefix(), main.getFileHandler().getItemBoughtMessage()
										.replace("%companion%", getCompanionName.toUpperCase())
										.replace("%companion_l%", getCompanionName.substring(0,1).toUpperCase() + getCompanionName.substring(1).toLowerCase()));

								new ShopMenu(main, player);

						}
					}
				}
			}

		}
		catch(NullPointerException e1) {}
	}

}
