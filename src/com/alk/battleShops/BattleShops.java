package com.alk.battleShops;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.alk.battleShops.Serializers.BCSStorageController;
import com.alk.battleShops.Serializers.MySQLSerializer;
import com.alk.battleShops.controllers.BCSExecutor;
import com.alk.battleShops.controllers.ConfigController;
import com.alk.battleShops.controllers.FileController;
import com.alk.battleShops.controllers.LinkController;
import com.alk.battleShops.controllers.MessageController;
import com.alk.battleShops.controllers.PermissionController;
import com.alk.battleShops.controllers.TransactionController;
import com.alk.battleShops.listeners.BCSBlockListener;
import com.alk.battleShops.listeners.BCSOnSignChangeListener;
import com.alk.battleShops.listeners.BCSPlayerListener;
import com.alk.battleShops.listeners.BCSPluginListener;
import com.alk.battleShops.objects.ShopSign;
import com.alk.battleShops.objects.WorldShop;
import com.alk.battleShops.util.FileLogger;
import com.alk.battleShops.util.InventoryUtil;
import com.alk.battleShops.util.Log;
import com.alk.battleShops.util.MoneyController;
import com.alk.battleShops.util.MyLogger;


/**
 * The main plugin
 * @author Alkarin
 *
 */
public class BattleShops extends JavaPlugin {

	private static final int DEFAULT_INTERVAL = 300; // Default time between transactions
    private static Server server = null;
    static private String pluginname; 
    static private String version;
    static private BattleShops plugin;

	static private BCSStorageController sc;
    private final Listener pluginListener = new BCSPluginListener();
    private final Listener blockBreakListener = new BCSBlockListener();
    private final Listener signListener = new BCSOnSignChangeListener();
    private final LinkController linkController  = new LinkController();
    private TransactionController transactionController;
    private BCSPlayerListener playerListener;

    private final PermissionController permissionController = new PermissionController();
    private static MyLogger logger;	
	
    Timer timer = new Timer();

    private BCSExecutor commandListener;
    
	public void onEnable() {
        server = getServer();
		plugin = this;
        PluginDescriptionFile pdfFile = this.getDescription();
        pluginname = pdfFile.getName();
        version = pdfFile.getVersion();
        Log.info("[" + pluginname + "]" + " version " + version + " starting");
		MoneyController.setup();

        InventoryUtil.load();

        /// Create our plugin folder if its not there
        File dir = this.getDataFolder();
        if (!dir.exists()){
        	dir.mkdirs();}

        /// Create or Load config files
        loadConfigFiles();

        permissionController.loadPermissions();

        /// Load Our Shops
        loadShops();
        logger = new MyLogger(sc);
        transactionController = new TransactionController(logger);
        playerListener = new BCSPlayerListener(linkController,transactionController);
        commandListener = new BCSExecutor(playerListener,linkController);
        commandListener.setStorageController(sc);

        // Register our events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(playerListener, this);
        pm.registerEvents(blockBreakListener, this);
        pm.registerEvents(signListener, this);
        pm.registerEvents(pluginListener, this);

        /// Set a timer to save sign data occasionally
        timer.scheduleAtFixedRate(new TimerTask(){
            public void run(){
            	FileController.backupJSONFiles();
            	saveWorldShops();
            	logger.saveAll();
            	FileLogger.saveAll();
            }
            }, 60*1000, Defaults.SAVE_EVERY_X_SECONDS * 1000);

        Log.info("[" + pluginname + "]" + " version " + version + " initialized!");
	}

	private void loadConfigFiles() {
        ConfigController.setConfig(
        		load(getClass().getResourceAsStream(Defaults.DEFAULT_CONFIGURATION_FILE), Defaults.CONFIGURATION_FILE));
        MessageController.setConfig(
        		load(getClass().getResourceAsStream(Defaults.DEFAULT_MESSAGES_FILE), Defaults.MESSAGES_FILE));		
        if (ConfigController.contains("multiworld")){
        	Defaults.MULTIWORLD = ConfigController.getBoolean("multiworld");
        }
        if (ConfigController.getBoolean("useMySQL")){
        	String p = "SQLOptions.";
        	MySQLSerializer.URL = ConfigController.getString(p +"url");
        	MySQLSerializer.PORT = ConfigController.getString(p +"port");
        	MySQLSerializer.USERNAME = ConfigController.getString(p +"username");
        	MySQLSerializer.PASSWORD = ConfigController.getString(p +"password");
        	MySQLSerializer.DB = ConfigController.getString(p +"db");
        }
        int interval;
        if((interval = ConfigController.getInt("intervalBetweenTransactions")) == 0){
            interval = DEFAULT_INTERVAL;}
        BCSPlayerListener.interval = interval;
	}

	public File load(InputStream inputStream, String config_file) {
		File file = new File(config_file);
		if (!file.exists()){ /// Create a new config file from our default
			try{
				OutputStream out=new FileOutputStream(config_file);
				byte buf[]=new byte[1024];
				int len;
				while((len=inputStream.read(buf))>0){
					out.write(buf,0,len);}
				out.close();
				inputStream.close();
			} catch (Exception e){
			}
		}
		return file;
	}
	

	private void saveWorldShops() {
		sc.saveAll();
	}

	void loadShops(){
		if (ConfigController.getBoolean("useMySQL")){
			MySQLSerializer sql = new MySQLSerializer();
			sql.init();
			sc = sql;
		} 
		sc.loadAll();
//		WorldShop.printShops();
	}
	
	public void onDisable() {
        this.getServer().getScheduler().cancelAllTasks();
        PluginDescriptionFile pdfFile = this.getDescription();
		if (Defaults.DEBUG_TRACE) System.out.println("WorldSaveEvent");

		FileController.backupJSONFiles();

		saveWorldShops();
		logger.saveAll();

		if (Defaults.DEBUG_SHOP_PERSISTANCE) WorldShop.printShops();
		
        System.out.println("[" + pluginname + "]"  + " version " + pdfFile.getVersion() + " disabled!");

    }


	/// Get the Server
    public static Server getBukkitServer() {return server;}
	public static WorldShop getWorldShop() {return new WorldShop();}
	public static String getVersion(){return version;}
	public static String getPluginName(){return pluginname;}
	public static BattleShops getSelf() {return plugin;}
	public static BCSStorageController getStorageController() {return sc;}
	
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
    	if (commandListener != null)
    		return commandListener.handleCommand(sender,cmd,commandLabel, args);
    	return true;
    }

    public static MyLogger getMyLogger(){return logger;}

	public Collection<ShopSign> getShopsWithInventory(World w, int itemid) {
		return WorldShop.getShopsWithInventory(w, itemid);
	}

	public Collection<ShopSign> getShopsNotFull(World w, int itemid) {
		return WorldShop.getShopsNotFull(w, itemid);
	}
}
