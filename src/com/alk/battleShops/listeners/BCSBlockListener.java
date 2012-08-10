package com.alk.battleShops.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.alk.battleShops.Defaults;
import com.alk.battleShops.controllers.LinkController;
import com.alk.battleShops.controllers.PermissionController;
import com.alk.battleShops.objects.ShopOwner;
import com.alk.battleShops.objects.ShopSign;
import com.alk.battleShops.objects.WorldShop;

/**
 * 
 * @author alkarin
 *
 */
public class BCSBlockListener implements Listener {

    @EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) return;
		if (Defaults.DEBUG_TRACE) System.out.println("onBlockBreak Event");
		final Material mat = event.getBlock().getType();
		if (mat.equals(Material.SIGN) || mat.equals(Material.SIGN_POST) || mat.equals(Material.WALL_SIGN)){
			Player player = event.getPlayer();
			Block block = event.getBlock();
			if (WorldShop.hasShopSignAt(block.getLocation()) ){

				if (PermissionController.hasPermissions(player, block)){
					ShopSign ss = WorldShop.getShopSign(block.getLocation());
					LinkController.breakShopSign((Sign) event.getBlock().getState());
					if (ShopOwner.sameOwner(ss.getOwner(),new ShopOwner(player))){
						WorldShop.playerUpdatedShop(ss.getOwner().getName());
					}
				} else {
					event.setCancelled(true); /// Keep the sign around so that text remains w/o logging out
				}

			}
		} else if (mat.equals(Material.CHEST)){
			Player player = event.getPlayer();
			Block block = event.getBlock();
			Chest chest = (Chest) block.getState();
			if (WorldShop.hasShopChestAt(chest)){
				if (PermissionController.hasPermissions(player, block)){
					LinkController.breakChestShop(chest);
				} else {
					event.setCancelled(true); /// chest not destroyed
				}

			}    			
		}
    }
    

}
