package com.alk.battleShops.controllers;

import java.io.File;
import java.util.Formatter;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.alk.battleShops.Defaults;

/**
 * 
 * @author alkarin
 *
 */
public class MessageController {

	private static YamlConfiguration config = new YamlConfiguration();
	static File f;
	
	public static String getMessage(String node, Object... varArgs) {
		return getMsg(Defaults.LANGUAGE,node,varArgs);
	}

	public static String getMessageNP(String node, Object... varArgs) {
		return getMsgNP(Defaults.LANGUAGE,node,varArgs);
	}
	
	private static String getMsg(String prefix,String node, Object... varArgs) {
		try{
			ConfigurationSection n = config.getConfigurationSection(prefix);

			StringBuilder buf = new StringBuilder(n.getString("prefix", "[Shop]"));
			String msg = n.getString(node, "No translation for " + node);
			Formatter form = new Formatter(buf);

			form.format(msg, varArgs);
			return colorChat(buf.toString());
		} catch(Exception e){
			System.err.println("Error getting message " + prefix + "." + node);
			for (Object o: varArgs){ System.err.println("argument=" + o);}
			e.printStackTrace();
			return "Error getting message " + prefix + "." + node;
		}
	}
	private static String getMsgNP(String prefix,String node, Object... varArgs) {
		ConfigurationSection n = config.getConfigurationSection(prefix);
		StringBuilder buf = new StringBuilder();
		String msg = n.getString(node, "No translation for " + node);
		Formatter form = new Formatter(buf);
		try{
			form.format(msg, varArgs);
		} catch(Exception e){
			System.err.println("Error getting message " + prefix + "." + node);
			for (Object o: varArgs){ System.err.println("argument=" + o);}
			e.printStackTrace();
		}
		return colorChat(buf.toString());
	}

	public static String colorChat(String msg) {
		return msg.replaceAll("&", Character.toString((char) 167));
	}

	public static boolean setConfig(File f){
		MessageController.f = f;
		return load();
	}

	public static boolean sendMessage(Player p, String message){
		if (message ==null) return true;
		String[] msgs = message.split("\n");
		for (String msg: msgs){
			if (p == null){
				System.out.println(colorChat(msg));
			} else {
				p.sendMessage(colorChat(msg));			
			}			
		}
		return true;
	}
	public static boolean sendMessage(CommandSender p, String message){
		if (message ==null) return true;
		if (p instanceof Player){
			if (((Player) p).isOnline())
				p.sendMessage(colorChat(message));			
		} else {
			p.sendMessage(colorChat(message));
		}
		return true;
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

	public static String getBoughtOrSold(boolean buying) {
		return buying ? "bought" : "sold";
	}
}
