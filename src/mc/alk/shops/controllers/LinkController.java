package mc.alk.shops.controllers;

import java.util.HashMap;
import java.util.Map;

import mc.alk.mc.MCPlayer;
import mc.alk.mc.MCWorld;
import mc.alk.mc.blocks.MCChest;
import mc.alk.mc.blocks.MCSign;
import mc.alk.shops.Defaults;
import mc.alk.shops.bukkit.controllers.BukkitMessageController;
import mc.alk.shops.objects.ShopChest;
import mc.alk.shops.objects.ShopOwner;
import mc.alk.shops.utils.KeyUtil;


/**
 *
 * @author alkarin
 *
 */
public class LinkController {

	Map<String, Integer> bukkittimers = new HashMap<String, Integer>();

	public boolean chestRightClick(MCChest chest, MCPlayer player) {
		if (PermController.isAdmin(player)){ /// dont care if its an admin
			return false;}

		ShopChest schest = ShopController.getShopChest(chest);
		if (schest == null)
			return false;
		ShopOwner prevOwner = schest.getOwner();
		if (prevOwner == null){ /// should this ever really happen?? well test for it
			return false;}
		MCWorld w = schest.getWorld();
		ShopOwner newOwner = new ShopOwner(player);
		Shop prevOwnerShop = ShopController.getShop(chest.getWorld(), prevOwner);

		Boolean hasShopPermission = prevOwnerShop != null ? prevOwnerShop.playerHasPermission(player) : false;
		if (hasShopPermission ){
			return false; /// right Clicking on your own shop does nothing, or if the person has permission
		} else {
			/// new Owner of this chest, remove previous
			breakConnection(schest,prevOwner,newOwner);

			ShopController.updateAffectedSigns(w, prevOwner, schest);
			BukkitMessageController.sendMessage(prevOwner, BukkitMessageController.getMessage("Break_link"));
			return true;
		}
	}


//	public boolean activateChestShop(MCChest chest, MCPlayer player) {
//		return activateChestShop(chest,player,null);
//	}

	public boolean activateChestShop(MCChest chest, MCPlayer player) {
		ShopChest schest = ShopController.getShopChest(chest);
		ShopOwner newOwner = new ShopOwner(player);
		ShopOwner prevOwner = (schest != null) ? schest.getOwner() : null;
		MCWorld w = chest.getWorld();
		if (Defaults.DEBUG_LINKING) System.out.println("prevOwner=" +prevOwner + " ------------ newOwner=" + newOwner);

		if (prevOwner != null){
			/// We want to unactivate if we have clicked on a chest twice
			if (prevOwner.equals(newOwner)){
				breakConnection(schest,prevOwner,newOwner);
				ShopController.updateAffectedSigns(w,prevOwner, schest);
				BukkitMessageController.sendMessage(player, BukkitMessageController.getMessage("Unlink_chest"));
				return false;
			}
			/// We have another player touching the chest
			Shop oldshop = ShopController.getShop(w,prevOwner);
			boolean playerIsAdmin = PermController.isAdmin(player);
			boolean hasPermissionToTouch = oldshop != null ? oldshop.playerHasPermission(player) : false;
			/// If they can touch this chest, then just return, we dont want them linking other peoples stuff
			if (hasPermissionToTouch || playerIsAdmin){
				return true;}

			breakConnection(schest,prevOwner,newOwner);
			if (oldshop != null && schest != null){
				ShopController.updateAffectedSigns(w,prevOwner, schest);
			}
			BukkitMessageController.sendMessage(prevOwner, BukkitMessageController.getMessage("Break_link"));
		}

		/// Add shop to WorldShop
		Shop shop = ShopController.addShop(w,newOwner);
		ShopChest shopchest = new ShopChest(chest, newOwner);

		HashMap<String,Integer> linked = shop.addChest(shopchest);
		ShopController.addShopChest(shopchest);

		/// Deal with linking
		if (linked != null && linked.size() > 0){
			for (String item_name : linked.keySet()){
				if (item_name.equals(Defaults.EVERYTHING_NAME)){
					BukkitMessageController.sendMessage(player,BukkitMessageController.getMessage("Linked_everything"));
					continue;
				}
				int item_count = linked.get(item_name);
				BukkitMessageController.sendMessage(player,
						BukkitMessageController.getMessage("Linked_sign",linked.get(item_name),
						BukkitMessageController.getSignOrSigns(item_count),
						BukkitMessageController.getHasOrHave(item_count),item_name));
			}
		} else {
			BukkitMessageController.sendMessage(player,BukkitMessageController.getMessage("Unlinked_sign"));
		}

		ShopController.updateAffectedSigns(w,newOwner, shopchest);
		if (Defaults.DEBUG_LINKING) System.out.println("   activateCompleted  " + newOwner +"  " +
				shopchest +"   loc " + KeyUtil.getStringLoc(shopchest));
		if (Defaults.DEBUG_LINKING) ShopController.printShops();
		return true;
	}

	public static void breakShopSign(MCSign sign) {
		ShopController.removeShopSign(sign);
	}

	public static void breakChestShop(MCChest chest) {
		ShopChest lc = ShopController.getShopChest(chest);
		if (Defaults.DEBUG_LINKING) System.out.println("  literally breaking  " + lc);
		if (lc == null) return;
		/// Alright, we do have a single or double chest with this location
		ShopOwner previous_owner = lc.getOwner();
		ShopController.removeChest(lc);

		ShopController.updateAffectedSigns(chest.getWorld(),previous_owner, lc);
		if (Defaults.DEBUG_LINKING) System.out.println("   breakCompleted");
		if (Defaults.DEBUG_LINKING) ShopController.printShops();
	}

	private static void breakConnection(ShopChest chest, ShopOwner previous_owner, ShopOwner new_owner){
		/// Remove chest from previous owner and get rid of it in shopchests
		ShopController.removeChest(chest);
	}

}
