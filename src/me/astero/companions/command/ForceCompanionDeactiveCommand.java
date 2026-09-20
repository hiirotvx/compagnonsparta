package me.astero.companions.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.astero.companions.CompanionsPlugin;
import me.astero.companions.companiondata.PlayerData;
import me.astero.companions.util.MessageUtil;

public class ForceCompanionDeactiveCommand implements CommandExecutor {
	
	private CompanionsPlugin main;
	
	public ForceCompanionDeactiveCommand(CompanionsPlugin main)
	{
		this.main = main;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		// forcedeactive {player}
		if(args.length >= 1)
		{
			if(sender.hasPermission("companions.admin.forcedeactive"))
			{
				try
				{
					Player target = Bukkit.getPlayer(args[0]);
					
					if(PlayerData.instanceOf(target).hasActiveCompanionSelected())
					{
						PlayerData.instanceOf(target).removeCompanion();
						main.getCompanionUtil().storeActiveDB("NONE", target);
						main.getCompanionUtil().storeActiveYML(target, "NONE");
						
						PlayerData.instanceOf(target).setActiveCompanionName("NONE");
						
						MessageUtil.sendPrefixed(target, main.getCompanionUtil().getPrefix(), main.getFileHandler().getRemoveCompanionMessage());
						MessageUtil.sendPrefixed(sender, main.getCompanionUtil().getPrefix(), main.getFileHandler().getCompanionRemovedMessage());
					}
					else
					{
						MessageUtil.sendPrefixed(sender, main.getCompanionUtil().getPrefix(), main.getFileHandler().getForceUpgradeNotSuccessfulMessage());
					}
				}
				catch(NullPointerException playerNotOnline)
				{
					MessageUtil.sendPrefixed(sender, main.getCompanionUtil().getPrefix(), main.getFileHandler().getPlayerNotOnlineMessage());
				}
			}
			else
			{
				MessageUtil.sendPrefixed(sender, main.getCompanionUtil().getPrefix(), main.getFileHandler().getNoPermissionMessage());
			}
			
		}
		else
		{
			MessageUtil.sendPrefixed(sender, main.getCompanionUtil().getPrefix(), main.getFileHandler().getInvalidUsageMessage());
		}
		return false;
	}

}
