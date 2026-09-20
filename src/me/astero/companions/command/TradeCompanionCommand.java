package me.astero.companions.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import me.astero.companions.CompanionsPlugin;
import me.astero.companions.companiondata.PlayerData;
import me.astero.companions.util.MessageUtil;

public class TradeCompanionCommand implements CommandExecutor {
	
	private CompanionsPlugin main;
	
	public TradeCompanionCommand(CompanionsPlugin main)
	{
		this.main = main;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// /tradecompanion <player> <companion>
		if(sender instanceof Player) 
		{
			Player player = (Player) sender;
			if(args.length >= 2)
			{
				
				if(player.hasPermission("companions.player.trade"))
				{
					try
					{
						Player target = Bukkit.getPlayer(args[0]);
						
						if(main.getFileHandler().getCompanionDetails().containsKey(args[1].toLowerCase()))
						{
							
							String getCompanionName = args[1].toLowerCase();
							if(!PlayerData.instanceOf(target).getAllCompanions().contains(getCompanionName))
							{
								if(PlayerData.instanceOf(player).getAllCompanions().contains(getCompanionName))
								{
									
									ConsoleCommandSender  console = Bukkit.getServer().getConsoleSender();
									Bukkit.dispatchCommand(console, "removecompanion " + player.getName() + " " + getCompanionName);
									Bukkit.dispatchCommand(console, "givecompanion " + target.getName() + " " + getCompanionName);
									
									MessageUtil.sendPrefixed(player, main.getCompanionUtil().getPrefix(),
											main.getFileHandler().getTradeSuccessfulMessage().replace("%player%", target.getName()));
								}
								else
								{
									MessageUtil.sendPrefixed(player, main.getCompanionUtil().getPrefix(),
											main.getFileHandler().getTradeUnsuccessfulMessage());
								}
							}
							else
							{
								MessageUtil.sendPrefixed(player, main.getCompanionUtil().getPrefix(),
										main.getFileHandler().getTradeAlreadyOwnMessage());
							}
						}
						else
						{
							MessageUtil.sendPrefixed(player, main.getCompanionUtil().getPrefix(),
									main.getFileHandler().getCompanionNotFoundMessage());
						}
					}
					catch(NullPointerException playerNotOnline)
					{
						MessageUtil.sendPrefixed(player, main.getCompanionUtil().getPrefix(),
								main.getFileHandler().getPlayerNotOnlineMessage());
					}
				}
				else
				{
					MessageUtil.sendPrefixed(player, main.getCompanionUtil().getPrefix(), main.getFileHandler().getNoPermissionMessage());
				}
			}
			else
			{
				MessageUtil.sendPrefixed(player, main.getCompanionUtil().getPrefix(), main.getFileHandler().getInvalidUsageMessage());
			}
		}
		else
		{
			MessageUtil.sendPrefixed(sender, main.getCompanionUtil().getPrefix(), main.getFileHandler().getNotPlayerMessage());
		}
		return false;
	}

}
