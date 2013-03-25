package mc.alk.shops.bukkit;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import mc.alk.bukkit.BukkitServer;
import mc.alk.controllers.MoneyController;
import mc.alk.serializers.SQLSerializerConfig;
import mc.alk.shops.BattleShops;
import mc.alk.shops.BattleShopsPlugin;
import mc.alk.shops.Defaults;
import mc.alk.shops.bukkit.controllers.BukkitMessageController;
import mc.alk.shops.bukkit.controllers.BukkitPermController;
import mc.alk.shops.bukkit.controllers.ConfigController;
import mc.alk.shops.bukkit.executors.BukkitPlayerTransactionsExecutor;
import mc.alk.shops.bukkit.executors.BukkitShopTransactionsExecutor;
import mc.alk.shops.bukkit.executors.BukkitShopsExecutor;
import mc.alk.shops.bukkit.listeners.ShopsBlockListener;
import mc.alk.shops.bukkit.listeners.ShopsInventoryListener;
import mc.alk.shops.bukkit.listeners.ShopsPluginListener;
import mc.alk.shops.bukkit.listeners.ShopsSignChestListener;
import mc.alk.shops.bukkit.listeners.ShopsSignListener;
import mc.alk.shops.bukkit.util.BukkitInventoryUtil;
import mc.alk.shops.bukkit.util.BukkitWorldUtil;
import mc.alk.shops.controllers.LinkController;
import mc.alk.shops.controllers.TransactionController;
import mc.alk.shops.controllers.TransactionLogger;
import mc.alk.shops.serializers.SQLInstance;
import mc.alk.shops.serializers.YamlMessageUpdater;
import mc.alk.shops.utils.FileUtil;
import mc.alk.util.Log;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


/**
 *
 * @author Alkarin
 *
 */
public class BukkitBattleShops extends JavaPlugin implements BattleShopsPlugin{

	static private String pluginname;
	static private String version;
	static private BukkitBattleShops plugin;
	SQLInstance sql;
	TransactionLogger transactionLogger;

	Timer timer = new Timer();

	@Override
	public void onEnable() {
		plugin = this;
		PluginDescriptionFile pdfFile = this.getDescription();
		pluginname = pdfFile.getName();
		version = pdfFile.getVersion();
		Log.info("[" + pluginname + "]" + " version " + version + " starting");

		/// Create our plugin folder if its not there
		File dir = getDataFolder();
		if (!dir.exists()){
			dir.mkdirs();}
		setEnabled(true); /// for calling async loading of the sql

		/// Create or Load config files
		loadConfigFiles();
		MoneyController.setup(this);
		BattleShops.init(new BukkitServer(),
				this,
				new BukkitPermController(),
				new BukkitInventoryUtil(),
				sql,
				new BukkitWorldUtil());

		LinkController linkController  = new LinkController();
		TransactionController transactionController = new TransactionController(transactionLogger);

		if (!MoneyController.hasEconomy()){
			Log.err("["+pluginname+"] needs an economy but Vault doesn't have one.  Disabling");
			setEnabled(false);
			return;
		}


		ShopsSignChestListener playerListener= new ShopsSignChestListener(linkController,transactionController);
		ShopsInventoryListener invListener= new ShopsInventoryListener(linkController,transactionController);
		BukkitShopsExecutor shopExecutor = new BukkitShopsExecutor(playerListener,linkController,BattleShops.getShopSerializer());
		BukkitShopTransactionsExecutor strExecutor =
				new BukkitShopTransactionsExecutor(playerListener,linkController,
						BattleShops.getShopSerializer());
		BukkitPlayerTransactionsExecutor ptrExecutor =
				new BukkitPlayerTransactionsExecutor(playerListener,linkController,
						BattleShops.getShopSerializer());
		getCommand("shop").setExecutor(shopExecutor);
		getCommand("shoptransactions").setExecutor(strExecutor);
		getCommand("playertransactions").setExecutor(ptrExecutor);

		ShopsPluginListener pluginListener = new ShopsPluginListener();
		pluginListener.loadAll();

		// Register our events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(playerListener, this);
		pm.registerEvents(invListener, this);
		pm.registerEvents(new ShopsBlockListener(), this);
		pm.registerEvents(new ShopsSignListener(), this);
		pm.registerEvents(pluginListener, this);

		/// Set a timer to save sign data occasionally

		timer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run(){
				sql.saveAll();
			}
		}, 60*1000, Defaults.SAVE_EVERY_X_SECONDS * 1000);

		sql.loadAll();

		Log.info("[" + pluginname + "]" + " version " + version + " initialized!");
	}

	@Override
	public void onDisable(){
		sql.saveAll();
	}

	private void loadConfigFiles() {
		/// Load
		ConfigController.setConfig( FileUtil.load(
				getClass(),Defaults.CONFIGURATION_FILE, Defaults.DEFAULT_CONFIGURATION_FILE));
		BukkitMessageController.setConfig( FileUtil.load(
				getClass(), Defaults.MESSAGES_FILE, Defaults.DEFAULT_MESSAGES_FILE ));
		/// Update
		YamlMessageUpdater mu = new YamlMessageUpdater();
		mu.update(BukkitMessageController.getConfig(), BukkitMessageController.getFile(),
				new File(plugin.getDataFolder()+"/backups"));

		transactionLogger = new TransactionLogger();
		sql = new SQLInstance(transactionLogger);

		SQLSerializerConfig.configureSQL(this, sql,
				ConfigController.getConfig().getConfigurationSection("SQLOptions"));

		/// Reload
		BukkitMessageController.setConfig(BukkitMessageController.getFile());
	}

	public static boolean enabled(){
		return plugin.isEnabled();
	}

	public BattleShopsPlugin getSelf() {
		return plugin;
	}

	@Override
	public File getDataDirectory() {
		return getDataFolder();
	}

}
