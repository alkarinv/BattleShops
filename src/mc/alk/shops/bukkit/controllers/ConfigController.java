package mc.alk.shops.bukkit.controllers;

import java.io.File;

import mc.alk.shops.Defaults;
import mc.alk.shops.bukkit.listeners.ShopsSignChestListener;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
/**
 *
 * @author alkarin
 *
 */
public class ConfigController {
	private static YamlConfiguration config;
	static File f;

    public static boolean getBoolean(String node) {return config.getBoolean(node, false);}
    public static boolean getBoolean(String node, boolean defaultValue) {return config.getBoolean(node, defaultValue);}
    public static  String getString(String node) {return config.getString(node);}
    public static int getInt(String node, Integer defaultOption) {return config.getInt(node, defaultOption);}
    public static double getDouble(String node) {return config.getDouble(node, -1);}

    public static boolean setConfig(File f){
    	ConfigController.f = f;
		config = new YamlConfiguration();
		if (load()){
			loadAll();
			return true;
		}
		return false;
	}


	private static void loadAll() {
        if (ConfigController.contains("multiworld")){
        	Defaults.MULTIWORLD = ConfigController.getBoolean("multiworld");
        }
        Defaults.DISABLE_PLAYER_SIGN_BREAK = !ConfigController.getBoolean("enableSignBreak", Defaults.DISABLE_PLAYER_SIGN_BREAK);
        Defaults.WAND = ConfigController.getInt("wand",Defaults.WAND);

        if (ConfigController.contains("admin_shop")){
        	Defaults.ADMIN_NAME = ConfigController.getString("admin_shop").toUpperCase();
        	Defaults.ADMIN_NAME_NO_SPACES = Defaults.ADMIN_NAME.replaceAll(" ", "");
        }
        if (ConfigController.contains("admin_string"))
        	Defaults.ADMIN_STR = ConfigController.getString("admin_string");

        Defaults.LEFT_CLICK_SELL_ACTION = ConfigController.getBoolean("leftClickSells",
        		Defaults.LEFT_CLICK_SELL_ACTION);

        if (ConfigController.contains("language"))
        	Defaults.LANGUAGE = ConfigController.getString("language");

        ShopsSignChestListener.interval = ConfigController.getInt("intervalBetweenTransactions",
        		ShopsSignChestListener.interval);
	}

	public static boolean load() {
		try {
			config.load(f);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public static boolean contains(String string) {
		return config.contains(string);
	}
	public static FileConfiguration getConfig() {
		return config;
	}
}
