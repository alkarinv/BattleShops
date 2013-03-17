package mc.alk.shops.utils;

import mc.alk.mc.MCBlock;
import mc.alk.mc.MCLocation;
import mc.alk.mc.MCWorld;
import mc.alk.mc.blocks.MCChest;

public abstract class WorldUtil {
	static WorldUtil util;

	public MCChest getMCNeighborChest(MCLocation location){
		MCWorld w = location.getWorld();
		int x = location.getBlockX();
		int y = location.getBlockY();
		int z = location.getBlockZ();
		MCBlock b = w.getBlockAt(x-1, y, z);
		if (w.isType(b, MCChest.class))
			return (MCChest) w.toType(b, MCChest.class);
		b = w.getBlockAt(x+1, y, z);
		if (w.isType(b, MCChest.class))
			return (MCChest) w.toType(b, MCChest.class);
		b = w.getBlockAt(x, y, z-1);
		if (w.isType(b, MCChest.class))
			return (MCChest) w.toType(b, MCChest.class);
		b = w.getBlockAt(x, y, z+1);
		if (w.isType(b, MCChest.class))
			return (MCChest) w.toType(b, MCChest.class);
		return null;
	}

	public static MCChest getNeighborChest(MCLocation location) {
		return util.getMCNeighborChest(location);
	}

	public static void setWorldUtil(WorldUtil worldUtil) {
		WorldUtil.util = worldUtil;
	}
}
