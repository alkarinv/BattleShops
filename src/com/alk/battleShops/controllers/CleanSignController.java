package com.alk.battleShops.controllers;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import com.alk.battleShops.objects.ShopSign;
import com.alk.battleShops.objects.WorldShop;

public class CleanSignController {

	final static int WIDTH = 100;
	final static int HEIGHT = 40;
	public static int clean(Location l){
		int num = 0;
		final World w = l.getWorld();
		for (int x= (l.getBlockX() - WIDTH); x<l.getBlockX() + WIDTH; x++){
			for (int y= l.getBlockY(); y<l.getBlockY() + HEIGHT; y++){
				for (int z= (l.getBlockZ() - WIDTH); z<l.getBlockZ() + WIDTH; z++){
					Material mat = Material.getMaterial(w.getBlockTypeIdAt(x, y, z));
					if (!(mat.equals(Material.SIGN) || mat.equals(Material.SIGN_POST) || mat.equals(Material.WALL_SIGN) )) {
			            continue;
			        }
					Block b = w.getBlockAt(x, y, z);
					Sign sign = (Sign) b.getState();
					boolean foundShopSign = WorldShop.findShopSign(sign) != null;
					boolean isShopSign = ShopSign.isShopSign(sign.getLines());
					if (isShopSign && !foundShopSign){
						b.setType(Material.AIR);
						num++;
					}

				}
			}
		}
		return num;
	}
}
