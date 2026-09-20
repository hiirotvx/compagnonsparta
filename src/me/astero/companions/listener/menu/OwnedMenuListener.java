package me.astero.companions.listener.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import me.astero.companions.CompanionsPlugin;
import me.astero.companions.companiondata.PlayerCache;
import me.astero.companions.companiondata.PlayerData;
import me.astero.companions.gui.OwnedMenu;
import me.astero.companions.gui.UpgradeMenu;
import me.astero.companions.util.MessageUtil;

public class OwnedMenuListener implements Listener {

	private CompanionsPlugin main;

	public OwnedMenuListener(CompanionsPlugin main)
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
			boolean ownedMenu = title.equals(MessageUtil.parse(main.getFileHandler().getOwnedCompanionsTitle()));

			if(ownedMenu)
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
							player.closeInventory();
						}
						else
						{
							new OwnedMenu(main, player, true);
						}
					}
					else if(currentName.equals(MessageUtil.parse(main.getFileHandler().getNextPageName())))
					{
						PlayerData.instanceOf(player).setPageNumber(PlayerData.instanceOf(player).getPageNumber() + 1);
						new OwnedMenu(main, player, true);
					}
					else if(currentName.equals(MessageUtil.parse(main.getFileHandler().getCompanionDetailName())))
					{
						if(PlayerData.instanceOf(player).isToggled())
						{
							PlayerData.instanceOf(player).setToggled(false);
						}

						PlayerData.instanceOf(player).removeCompanion();

						PlayerData.instanceOf(player).setActiveCompanionName("NONE");

						main.getCompanionUtil().storeActiveYML(player, "NONE");

						player.closeInventory();

						MessageUtil.sendPrefixed(player, main.getCompanionUtil().getPrefix(), main.getFileHandler().getRemoveCompanionMessage());
					}
					for(String getCompanionName : PlayerCache.instanceOf(player.getUniqueId()).getOwnedCache().keySet())
					{
						if(MessageUtil.parse(main.getFileHandler().getCompanionDetails().get(getCompanionName).getItemName()).equals(currentName))
						{
								if(e.isRightClick())
								{
									// Clic droit : editer. L'edition s'applique au compagnon actif uniquement.
									if(getCompanionName.equalsIgnoreCase(PlayerData.instanceOf(player).getActiveCompanionName()))
									{
										new UpgradeMenu(main, player);
									}
									else
									{
										MessageUtil.sendPrefixed(player, main.getCompanionUtil().getPrefix(),
												main.getFileHandler().getMustActivateToEditMessage());
										player.closeInventory();
									}

									return;
								}

								if(PlayerData.instanceOf(player).isToggled())
								{
									PlayerData.instanceOf(player).setToggled(false);
								}

								PlayerData.instanceOf(player).removeCompanion();
								PlayerData.instanceOf(player).setActiveCompanionName(getCompanionName.toUpperCase());

								main.getCompanionUtil().storeActiveYML(player, getCompanionName);

								main.getCompanionPacket().loadCompanion(player);
								player.closeInventory();
						}
					}
				}
			}

		}
		catch(NullPointerException e1)
		{

		}
	}

}
