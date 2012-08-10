package com.alk.battleShops.util;

import java.util.logging.Logger;

import org.bukkit.Bukkit;

public class Log {

	private static Logger log = Bukkit.getLogger();

	public static void info(String msg){
		if (log != null)
			log.info(Util.colorChat(msg));
		else 
			System.out.println(Util.colorChat(msg));
	}
	public static void warn(String msg){
		if (log != null)
			log.warning(Util.colorChat(msg));
		else 
			System.err.println(Util.colorChat(msg));
	}
	public static void err(String msg){
		if (log != null)
			log.severe(Util.colorChat(msg));
		else 
			System.err.println(Util.colorChat(msg));
	}
}
