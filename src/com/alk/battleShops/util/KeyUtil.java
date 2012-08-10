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
		return block.getWorld().getName() + ":" + block.getX() +":" + block.getY() + ":" + block.getZ();
	}
	public static String getStringLoc(Sign sign) {
		return sign.getWorld().getName() + ":" + sign.getX() +":" + sign.getY() + ":" + sign.getZ();
	}

	public static String getStringLoc(ShopChest chest) {
		return chest.getWorld().getName() + ":" + chest.getX() +":" + chest.getY() +":" + chest.getZ() ;
	}

	public static String getStringLoc(ShopSign sign) {
		return sign.getWorld().getName() + ":" + sign.getX() +":" + sign.getY() + ":" + sign.getZ();
	}

	public static String getStringLoc(Chest chest) {
		return chest.getWorld().getName() + ":" + chest.getX() +":" + chest.getY() + ":" + chest.getZ();
	}
	public static String getStringLoc(Location location) {
		return location.getWorld().getName() + ":" + location.getX() +":" + location.getY() + ":" + location.getZ();
	}
}
