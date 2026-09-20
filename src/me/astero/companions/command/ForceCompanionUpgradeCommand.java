package me.astero.companions.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.astero.companions.CompanionsPlugin;
import me.astero.companions.companiondata.PlayerCache;
import me.astero.companions.companiondata.PlayerData;
import me.astero.companions.util.MessageUtil;

public class ForceCompanionUpgradeCommand implements CommandExecutor {
	
	private CompanionsPlugin main;
	
	public ForceCompanionUpgradeCommand(CompanionsPlugin main)
	{
		this.main = main;
	}
	
	
	// /forceupgrade {player} {ability}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if(sender.hasPermission("companions.admin.forceupgrade"))
		{
			if(args.length >= 2)
			{
	
				try
				{
					
					Player player = Bukkit.getPlayer(args[0]);
					if(PlayerData.instanceOf(player).hasActiveCompanionSelected())
					{
						if(args[1].equalsIgnoreCase("ability"))
						{
							
							boolean upgrade;
							
							try
							{
								upgrade = Boolean.valueOf(args[2]);
							}
							catch(ArrayIndexOutOfBoundsException notStated)
							{
								upgrade = true;
							}
	
							if(!upgrade)
							{
								if(PlayerCache.instanceOf(player.getUniqueId()).getOwnedCache().get(PlayerData.instanceOf(player).getActiveCompanionName().toLowerCase()).getAbilityLevel() != 1)
								{
									main.getCompanionUtil().buyUpgradeAbility(player, false);
								}
								else
								{
									MessageUtil.sendPrefixed(player, main.getCompanionUtil().getPrefix(), main.getFileHandler().getAbilityDowngradedMaxedMessage());
								}
							}
							else if(main.getFileHandler().getMaxAbilityLevel() != 
									PlayerCache.instanceOf(player.getUniqueId()).getOwnedCache().get(PlayerData.instanceOf(player).getActiveCompanionName().toLowerCase()).getAbilityLevel())
							{

								
								if(upgrade)
								{
									main.getCompanionUtil().buyUpgradeAbility(player, true);
								}
								
								MessageUtil.sendPrefixed(sender, main.getCompanionUtil().getPrefix(), main.getFileHandler().getForceUpgradeSuccessfulMessage());
							}
							else
							{
								MessageUtil.sendPrefixed(sender, main.getCompanionUtil().getPrefix(), main.getFileHandler().getAbilityMaxedMessage());
							}
						}
						else if(args[1].equalsIgnoreCase("rename"))
						{
							main.getCompanionUtil().upgradeRename(player, false, false);
							MessageUtil.sendPrefixed(sender, main.getCompanionUtil().getPrefix(), main.getFileHandler().getForceUpgradeSuccessfulMessage());
						}
						else if(args[1].equalsIgnoreCase("hidename"))
						{
							main.getCompanionUtil().upgradeHideName(player, false, false);
							MessageUtil.sendPrefixed(sender, main.getCompanionUtil().getPrefix(), main.getFileHandler().getForceUpgradeSuccessfulMessage());
						}
						else if(args[1].equalsIgnoreCase("changeweapon"))
						{
							main.getCompanionUtil().upgradeWeapon(player, false, false);
							MessageUtil.sendPrefixed(sender, main.getCompanionUtil().getPrefix(), main.getFileHandler().getForceUpgradeSuccessfulMessage());
						}
						else
						{
							MessageUtil.sendPrefixed(sender, main.getCompanionUtil().getPrefix(), main.getFileHandler().getInvalidUpgradeArgumentMessage());
						}
					}
					else
					{
						MessageUtil.sendPrefixed(sender, main.getCompanionUtil().getPrefix(), main.getFileHandler().getForceUpgradeNotSuccessfulMessage());
					}
				}
				catch(NullPointerException error)
				{
					MessageUtil.sendPrefixed(sender, main.getCompanionUtil().getPrefix(), main.getFileHandler().getPlayerNotOnlineMessage());
				}
			}
			else
			{
				MessageUtil.sendPrefixed(sender, main.getCompanionUtil().getPrefix(), main.getFileHandler().getInvalidUsageMessage());
			}
		}
		else
		{
			MessageUtil.sendPrefixed(sender, main.getCompanionUtil().getPrefix(), main.getFileHandler().getNoPermissionMessage());
		}
		
		
		return false;
	}

}
