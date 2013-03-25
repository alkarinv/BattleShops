package mc.alk.shops.bukkit.listeners;

import mc.alk.bukkit.BukkitBlock;
import mc.alk.bukkit.BukkitPlayer;
import mc.alk.bukkit.blocks.BukkitChest;
import mc.alk.bukkit.blocks.BukkitSign;
import mc.alk.mc.MCBlock;
import mc.alk.mc.MCLocation;
import mc.alk.mc.MCPlayer;
import mc.alk.mc.blocks.MCChest;
import mc.alk.shops.Defaults;
import mc.alk.shops.controllers.LinkController;
import mc.alk.shops.controllers.PermController;
import mc.alk.shops.controllers.ShopController;
import mc.alk.shops.objects.ShopOwner;
import mc.alk.shops.objects.ShopSign;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;


/**
 *
 * @author alkarin
 *
 */
public class ShopsBlockListener implements Listener {

    @EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) return;
		if (Defaults.DEBUG_TRACE) System.out.println("onBlockBreak Event");
		final Material mat = event.getBlock().getType();
		if (mat.equals(Material.SIGN) || mat.equals(Material.SIGN_POST) || mat.equals(Material.WALL_SIGN)){
			MCPlayer player = new BukkitPlayer(event.getPlayer());
			MCBlock block = new BukkitBlock(event.getBlock());
			MCLocation loc = block.getLocation();
			if (ShopController.hasShopSignAt(loc) ){
				/// Check perms
				if (PermController.hasAllCreatePermissions(player, loc)){
					/// if they do have perms, still need to check if the option disableplayerSignBreak is set
					ShopSign ss = ShopController.getShopSign(loc);
					if (!PermController.isAdmin(player) && Defaults.DISABLE_PLAYER_SIGN_BREAK &&
							!ShopOwner.sameOwner(ss.getOwner(),new ShopOwner(player.getName()))){
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
			MCPlayer player = new BukkitPlayer(event.getPlayer());
			MCChest chest = new BukkitChest((Chest) event.getBlock().getState());
			MCLocation loc = chest.getLocation();
			if (ShopController.hasShopChestAt(chest)){
				if (PermController.hasAllCreatePermissions(player, loc)){
					LinkController.breakChestShop(chest);
				} else {
					event.setCancelled(true); /// chest not destroyed
				}
			}
		}
    }

	private void breakShopSign(BlockBreakEvent event, MCPlayer player, MCBlock block) {
		ShopSign ss = ShopController.getShopSign(block.getLocation());
		LinkController.breakShopSign( new BukkitSign((Sign) event.getBlock().getState()));
		if (ShopOwner.sameOwner(ss.getOwner(),new ShopOwner(player.getName()))){
			ShopController.playerUpdatedShop(ss.getOwner().getName());
		}
	}


}
