package me.astero.companions.listener.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import me.astero.companions.CompanionsPlugin;
import me.astero.companions.util.MessageUtil;

public class MainMenuListener implements Listener {

	private CompanionsPlugin main;

	public MainMenuListener(CompanionsPlugin main)
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
			boolean mainMenu = title.equals(MessageUtil.parse(main.getFileHandler().getOpenCompanionsTitle()));

			if(mainMenu)
			{
				if(e.getCurrentItem() != null)
				{
					e.setCancelled(true);

					Component currentName = e.getCurrentItem().getItemMeta().displayName();
					if(currentName == null) return;

					if(currentName.equals(MessageUtil.parse(main.getFileHandler().getCompanionShopName())))
					{
						Bukkit.dispatchCommand(player, "companions shop");
					}
					else if(currentName.equals(MessageUtil.parse(main.getFileHandler().getOwnedCompanionsName())))
					{
						Bukkit.dispatchCommand(player, "companions owned");
					}
					else if(currentName.equals(MessageUtil.parse(main.getFileHandler().getUpgradeAbilitiesName())))
					{
						Bukkit.dispatchCommand(player, "companions upgrade");
					}
				}
			}

		}
		catch(NullPointerException e1)
		{

		}
	}

}
