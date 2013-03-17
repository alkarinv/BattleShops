package mc.alk.shops.bukkit.util;

import mc.alk.bukkit.blocks.BukkitChest;
import mc.alk.mc.MCLocation;
import mc.alk.mc.blocks.MCChest;
import mc.alk.shops.utils.WorldUtil;

public class BukkitWorldUtil extends WorldUtil {

	@Override
	public MCChest getMCNeighborChest(MCLocation location) {
		return BukkitChest.getNeighborChest(location.getWorld().getBlockAt(location));
	}

}
