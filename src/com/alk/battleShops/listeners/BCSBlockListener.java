package com.alk.battleShops.listeners;

import org.bukkit.ChatColor;
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
				/// Check perms
				if (PermissionController.hasCreatePermissions(player, block)){
					/// if they do have perms, still need to check if the option disableplayerSignBreak is set
					ShopSign ss = WorldShop.getShopSign(block.getLocation());
					if (!PermissionController.isAdmin(player) && Defaults.DISABLE_PLAYER_SIGN_BREAK &&
							!ShopOwner.sameOwner(ss.getOwner(),new ShopOwner(player))){
						event.setCancelled(true); /// Keep the sign around so that text remains w/o logging out
						player.sendMessage(ChatColor.RED+"You can't break a sign you don't own");
					} else {
						breakShopSign(event, player, block);
					}
				} else { // no perms
					event.setCancelled(true); /// Keep the sign around so that text remains w/o logging out
				}
			}
		} else if (mat.equals(Material.CHEST)){
			Player player = event.getPlayer();
			Block block = event.getBlock();
			Chest chest = (Chest) block.getState();
			if (WorldShop.hasShopChestAt(chest)){
				if (PermissionController.hasCreatePermissions(player, block)){
					LinkController.breakChestShop(chest);
				} else {
					event.setCancelled(true); /// chest not destroyed
				}

			}
		}
    }

	private void breakShopSign(BlockBreakEvent event, Player player, Block block) {
		ShopSign ss = WorldShop.getShopSign(block.getLocation());
		LinkController.breakShopSign((Sign) event.getBlock().getState());
		if (ShopOwner.sameOwner(ss.getOwner(),new ShopOwner(player))){
			WorldShop.playerUpdatedShop(ss.getOwner().getName());
		}
	}


}
