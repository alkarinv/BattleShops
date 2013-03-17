package mc.alk.shops.bukkit.controllers;

import java.io.File;
import java.util.Formatter;

import mc.alk.mc.MCPlayer;
import mc.alk.shops.Defaults;
import mc.alk.shops.objects.ShopOwner;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;


/**
 *
 * @author alkarin
 *
 */
public class BukkitMessageController {

	private static YamlConfiguration config = new YamlConfiguration();
	static File f;

	public static String getMessage(String node, Object... varArgs) {
		return getMsg(Defaults.LANGUAGE,node,varArgs);
	}

	public static String getMessageNP(String node, Object... varArgs) {
		return getMsgNP(Defaults.LANGUAGE,node,varArgs);
	}

	public static String getHasOrHave(int itemCount) {
		return itemCount == 1 ? getMessageNP("has") : getMessageNP("have");
	}

	public static String getChestOrChests(int itemCount) {
		return itemCount == 1 ? getMessageNP("chest") : getMessageNP("chests");
	}

	public static String getSignOrSigns(int itemCount) {
		return itemCount == 1 ? getMessageNP("sign") : getMessageNP("signs");
	}

	private static String getMsg(String prefix,String node, Object... varArgs) {
		try{
			ConfigurationSection n = config.getConfigurationSection(prefix);

			StringBuilder buf = new StringBuilder(n.getString("prefix", "[Shop]"));
			String msg = n.getString(node, "No translation for " + node);
			Formatter form = new Formatter(buf);

			form.format(msg, varArgs);
			form.close();
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
			form.close();
		} catch(Exception e){
			System.err.println("Error getting message " + prefix + "." + node);
			for (Object o: varArgs){ System.err.println("argument=" + o);}
			e.printStackTrace();
		}
		return colorChat(buf.toString());
	}

	public static String colorChat(String msg) {
		return msg.replace('&', (char) 167);
	}

	public static String decolorChat(String string) {
		/// Remove all the color codes, first the user defined &[0-9a-fA-F]
		string = string.replaceAll("&[0-9a-fA-F]", "");
		/// Remove the server color codes
		string = ChatColor.stripColor(string);
		return string;
	}

	public static boolean setConfig(File f){
		BukkitMessageController.f = f;
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

	public FileConfiguration getConfig() {
		return config;
	}

	public static void sendMessage(ShopOwner prevOwner, String message) {

	}

	public static void sendMessage(MCPlayer player, String message) {
		player.sendMessage(message);
	}
}
