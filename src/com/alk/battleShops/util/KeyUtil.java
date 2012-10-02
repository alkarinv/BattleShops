package com.alk.battleShops.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;

import com.alk.battleShops.objects.ShopChest;
import com.alk.battleShops.objects.ShopSign;

/**
 * 
 * @author alkarin
 *
 */
public class KeyUtil {

	public static String getStringLoc(Block block) {
		return getStringLoc(block.getLocation());
	}
	public static String getStringLoc(Sign sign) {
		return getStringLoc(sign.getLocation());
	}

	public static String getStringLoc(ShopChest chest) {
		return getStringLoc(chest.getLocation());
	}

	public static String getStringLoc(ShopSign sign) {
		return getStringLoc(sign.getLocation());
	}

	public static String getStringLoc(Chest chest) {
		return getStringLoc(chest.getLocation());
	}
	public static String getStringLoc(Location location) {
		return location.getWorld().getName() + ":" + location.getBlockX() +":" + location.getBlockY() + ":" + location.getBlockZ();
	}
}
