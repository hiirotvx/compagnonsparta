package me.astero.companions.listener.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import me.astero.companions.CompanionsPlugin;
import me.astero.companions.companiondata.PlayerData;
import me.astero.companions.gui.PlayerDetailsMenu;
import me.astero.companions.util.MessageUtil;

public class PlayerDetailsMenuListener implements Listener {

	private CompanionsPlugin main;

	public PlayerDetailsMenuListener(CompanionsPlugin main)
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
			String rawTitle = main.getFileHandler().getPlayerDetailsTitle()
					.replace("%target%", PlayerData.instanceOf(player).getPlayerDetailsTarget().getName().toUpperCase())
					.replace("%target_l%", PlayerData.instanceOf(player).getPlayerDetailsTarget().getName().substring(0, 1).toUpperCase()
							+ PlayerData.instanceOf(player).getPlayerDetailsTarget().getName().substring(1).toLowerCase());
			boolean playerDetailsMenu = title.equals(MessageUtil.parse(rawTitle));

			if(playerDetailsMenu)
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
							new PlayerDetailsMenu(main, player, false);
						}
					}
					else if(currentName.equals(MessageUtil.parse(main.getFileHandler().getNextPageName())))
					{
						PlayerData.instanceOf(player).setPageNumber(PlayerData.instanceOf(player).getPageNumber() + 1);
						new PlayerDetailsMenu(main, player, false);
					}
				}
			}

		}
		catch(NullPointerException e1)
		{

		}
	}

}
