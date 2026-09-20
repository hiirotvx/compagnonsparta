package me.astero.companions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import me.astero.companions.companiondata.PlayerData;
import me.astero.companions.companiondata.packets.ArmorStandCompanionPacket;
import me.astero.companions.companiondata.packets.CompanionPacket;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import me.astero.companions.api.PlaceholderAPI;
import me.astero.companions.command.ClearCompanionDataCommand;
import me.astero.companions.command.CompanionCommand;
import me.astero.companions.command.ForceCompanionActiveCommand;
import me.astero.companions.command.ForceCompanionDeactiveCommand;
import me.astero.companions.command.ForceCompanionUpgradeCommand;
import me.astero.companions.command.GiveCompanionCommand;
import me.astero.companions.command.GiveCompanionItemCommand;
import me.astero.companions.command.RemoveCompanionCommand;
import me.astero.companions.command.TradeCompanionCommand;
import me.astero.companions.companiondata.Companions;
import me.astero.companions.companiondata.abilities.CustomAbilities;
import me.astero.companions.companiondata.abilities.PotionEffectAbility;
import me.astero.companions.companiondata.animations.Animation;
import me.astero.companions.currency.CompanionCoin;
import me.astero.companions.database.Database;
import me.astero.companions.economy.EconomyHandler;
import me.astero.companions.filemanager.FileHandler;
import me.astero.companions.filemanager.FileManager;
import me.astero.companions.items.CompanionToken;
import me.astero.companions.listener.ChatListener;
import me.astero.companions.listener.PlayerListener;
import me.astero.companions.listener.VanishListener;
import me.astero.companions.listener.VehicleListener;
import me.astero.companions.listener.companions.CompanionCache;
import me.astero.companions.listener.companions.CompanionFollow;
import me.astero.companions.listener.companions.CompanionInteraction;
import me.astero.companions.listener.menu.OwnedMenuListener;
import me.astero.companions.listener.menu.PlayerDetailsMenuListener;
import me.astero.companions.listener.menu.UpgradeMenuListener;
import me.astero.companions.util.CompanionUtil;
import me.astero.companions.util.FormatNumbers;


@SuppressWarnings("deprecation")
public class CompanionsPlugin extends JavaPlugin {
	

	@Getter private FileHandler fileHandler;
	@Getter private FileManager fileManager;
	@Getter private Companions companions;
	@Getter private CompanionUtil companionUtil;
	@Getter private PotionEffectAbility potionEffectAbility;
	@Getter private CustomAbilities customAbility;
	@Getter private me.astero.companions.permission.CompanionAccess companionAccess;
	@Getter private me.astero.companions.integration.ClaimFlyListener claimFly;
	@Getter private Animation animation;
	@Getter private FormatNumbers formatNumbers;
	@Getter private Database database;
	@Getter private CompanionCoin companionCoin;
	@Getter private CompanionPacket companionPacket;

	private String source = "com.mysql.jdbc.jdbc2.optional.MysqlDataSource";

	@Override
	public void onEnable() 
	{
		getLogger().info(ChatColor.GOLD + "Companions Renewed" + ChatColor.GRAY + " by Chronic Reflexes" + ChatColor.GOLD + " is loading up...");
		
		getConfig().options().copyDefaults();
		saveDefaultConfig();
		
		animation = new Animation(this);
	
		fileManager = new FileManager(this);
		formatNumbers = new FormatNumbers();
		getLogger().info(ChatColor.GOLD + ">" + ChatColor.GRAY + " YAML files are loaded up!");
		companionUtil = new CompanionUtil(this);
		fileHandler = new FileHandler(this);
		getLogger().info(ChatColor.GOLD + ">" + ChatColor.GRAY + " Caching files is done!");
		
		companions = new Companions(this);
		potionEffectAbility = new PotionEffectAbility(this);
		customAbility = new CustomAbilities(this);
		companionAccess = new me.astero.companions.permission.CompanionAccess(this);
		claimFly = new me.astero.companions.integration.ClaimFlyListener(this);
		

		new EconomyHandler(this);
		
		
		getLogger().info(ChatColor.GOLD + ">" + ChatColor.GRAY + " Misc files are loaded up!");


		
		Bukkit.getPluginManager().registerEvents(new CompanionFollow(this), this);
		Bukkit.getPluginManager().registerEvents(new CompanionCache(this), this);
		Bukkit.getPluginManager().registerEvents(new OwnedMenuListener(this), this);
		Bukkit.getPluginManager().registerEvents(new UpgradeMenuListener(this), this);
		Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
		Bukkit.getPluginManager().registerEvents(new ChatListener(this), this);
		Bukkit.getPluginManager().registerEvents(new CompanionInteraction(this), this);
		Bukkit.getPluginManager().registerEvents(new CompanionToken(this), this);
		Bukkit.getPluginManager().registerEvents(customAbility, this);
		Bukkit.getPluginManager().registerEvents(claimFly, this);
		Bukkit.getPluginManager().registerEvents(new VanishListener(this), this);
		Bukkit.getPluginManager().registerEvents(new VehicleListener(this), this);
		Bukkit.getPluginManager().registerEvents(new PlayerDetailsMenuListener(this), this);	
		getLogger().info(ChatColor.GOLD + ">" + ChatColor.GRAY + " Event Listeners are loaded up!");
		
		getCommand("companions").setExecutor(new CompanionCommand(this));
		getCommand("givecompanion").setExecutor(new GiveCompanionCommand(this));
		getCommand("removecompanion").setExecutor(new RemoveCompanionCommand(this));
		getCommand("givecompanionitem").setExecutor(new GiveCompanionItemCommand(this));
		getCommand("clearcompaniondata").setExecutor(new ClearCompanionDataCommand(this));
		getCommand("forceupgrade").setExecutor(new ForceCompanionUpgradeCommand(this));
		getCommand("forceactive").setExecutor(new ForceCompanionActiveCommand(this));
		getCommand("tradecompanion").setExecutor(new TradeCompanionCommand(this));
		getCommand("forcedeactive").setExecutor(new ForceCompanionDeactiveCommand(this));
		
		getLogger().info(ChatColor.GOLD + ">" + ChatColor.GRAY + " Commands are loaded up!");

		// Le cache est normalement rempli a la connexion. Si le plugin est (re)charge
		// alors que des joueurs sont deja en ligne, PlayerJoinEvent ne se declenche pas
		// pour eux : on les resynchronise ici, une fois les caches disponibles.
		Bukkit.getScheduler().runTaskLater(this, () -> {
			for(org.bukkit.entity.Player online : Bukkit.getOnlinePlayers())
			{
				companionAccess.sync(online);
			}
		}, 20L);

		
		getLogger().info(ChatColor.GOLD + "              >--------------------------<");
		getLogger().info(ChatColor.GOLD + "              A total of " + ChatColor.YELLOW + this.getFileHandler().getCompanionDetails().size() + ChatColor.GOLD + " Companions have");
		getLogger().info(ChatColor.GOLD + "                    been loaded up.");
		getLogger().info(ChatColor.GOLD + "              >--------------------------<");
		
		




		companionCoin = new CompanionCoin(this);
		
	    if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
	    {
            new PlaceholderAPI().register();
           
	    }


		getLogger().info(ChatColor.GOLD + "Companions Renewed" + ChatColor.GRAY + " by Chronic Reflexes" + ChatColor.GOLD + " has been sucessfully loaded up!");
		
		//VersionChecker vc = new VersionChecker(this);
		
		setupNMS();

		database = new Database(this, source);
		
		
		
	
		

		

	}
	
	@Override
	public void onDisable() {
		//int companionCount = 0;

		getLogger().info(ChatColor.GOLD + "Companions Renewed" + ChatColor.GRAY + " is disabling and saving necessary files..");

		if(companionUtil != null)
		{
			companionUtil.despawnAllCompanions();
		}


		PreparedStatement p = null;
		Connection conn = null;

		if (getFileHandler().isDatabase()) {
			for (PlayerData pd : PlayerData.getPlayers().values()) {

				getCompanionUtil().saveCache(pd.getPlayer(), pd, p, conn);

				//pd.removeCompanion();
				//companionCount++;
				//System.out.println(pd.getActiveCompanionName());
			}


			database.close(conn, p, null);
		}
		
		//System.out.println(ChatColor.GOLD + "  >" + ChatColor.GRAY + " Removed " + ChatColor.YELLOW + companionCount + ChatColor.GRAY + " Companion(s)..\n");
		
		this.getFileManager().saveFile();
		
		database.onDisabled();
		
		getLogger().info(ChatColor.GOLD + "Companions Renewed" + ChatColor.GRAY + " by Chronic Reflexes" + ChatColor.GOLD + " has been sucessfully disabled!");
	}
	
	public void saveActiveCompanion(String getCompanionName, Player player, PreparedStatement p, Connection conn) // method not in used
	{
		if(!getFileHandler().isDatabase())
			getFileManager().getCompanionsData().set("companions." + player.getUniqueId()
			+ ".active" , getCompanionName.toUpperCase());
		
		else
		{
			
					  
						
						try
						{
							
							conn = getDatabase().getHikari().getConnection();
							
							p = conn.prepareStatement("INSERT INTO `" + getDatabase().getTablePrefix() 
									+"active` (`UUID`,`name`,`companion`) VALUES (?,?,?)" + 
									"  ON DUPLICATE KEY UPDATE companion=\"" + getCompanionName.toUpperCase() + "\"");
							p.setString(1, player.getUniqueId().toString());
							p.setString(2, player.getName().toString());
							p.setString(3, getCompanionName.toUpperCase());
							//p.setString(4, player.getUniqueId().toString());
		
							p.execute();
							
							
						} 
						catch (SQLException e1) 
						{
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

		}
	}
	
	public void setupNMS()
	{
		source = "com.mysql.cj.jdbc.MysqlDataSource";
		companionPacket = new ArmorStandCompanionPacket(this);
		getLogger().info("Using ArmorStand companion handler for modern servers.");
	}
	

}
