package mc.alk.shops.utils;

import mc.alk.mc.MCBlock;
import mc.alk.mc.MCItemStack;
import mc.alk.mc.MCLocation;
import mc.alk.mc.blocks.MCChest;
import mc.alk.mc.blocks.MCSign;
import mc.alk.shops.objects.ShopChest;
import mc.alk.shops.objects.ShopSign;


/**
 *
 * @author alkarin
 *
 */
public class KeyUtil {

	public static String getStringLoc(MCBlock block) {
		return getStringLoc(block.getLocation());
	}
	public static String getStringLoc(MCSign sign) {
		return getStringLoc(sign.getLocation());
	}

	public static String getStringLoc(ShopChest chest) {
		return getStringLoc(chest.getLocation());
	}

	public static String getStringLoc(ShopSign sign) {
		return getStringLoc(sign.getLocation());
	}

	public static String getStringLoc(MCChest chest) {
		return getStringLoc(chest.getLocation());
	}
	public static String getStringLoc(MCLocation location) {
		return location.getWorld().getName() + ":" + location.getBlockX() +":" + location.getBlockY() + ":" + location.getBlockZ();
	}
	public static long toKey(MCItemStack item){
		return CompositeMap.toKey(item.getType(), item.getDataValue());
	}
	public static int toItemID(Long longid) {
		return CompositeMap.getHOB(longid);
	}
	public static int toItemDataValue(Long longid) {
		return CompositeMap.getLOB(longid);
	}
}
