package com.alk.battleShops.controllers;

import java.io.File;

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
    public static  String getString(String node) {return config.getString(node);}
    public static int getInt(String node, Integer defaultOption) {return config.getInt(node, defaultOption);}
    public static double getDouble(String node) {return config.getDouble(node, -1);}

    public static boolean setConfig(File f){
    	ConfigController.f = f;
		config = new YamlConfiguration();
		return load();
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
