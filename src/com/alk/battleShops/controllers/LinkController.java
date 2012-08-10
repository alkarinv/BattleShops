package com.alk.battleShops.controllers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.alk.battleShops.BattleShops;
import com.alk.battleShops.Defaults;
import com.alk.battleShops.objects.Shop;
import com.alk.battleShops.objects.ShopChest;
import com.alk.battleShops.objects.ShopOwner;
import com.alk.battleShops.objects.ShopSign;
import com.alk.battleShops.objects.WorldShop;
import com.alk.battleShops.util.Util;

/**
 * 
 * @author alkarin
 *
 */
public class LinkController {

//	Map<String, Timer> timers = new HashMap<String, Timer>();
	Map<String, Integer> bukkittimers = new HashMap<String, Integer>();
	Map<String, OwnChestClickedTimerTask> ownTasks= new HashMap<String, OwnChestClickedTimerTask>();

	class OwnChestClickedTimerTask extends Thread {
		World w;
		ShopOwner so;
		Set<Integer> ids;
		public OwnChestClickedTimerTask(World w, ShopOwner so) {
			this.w = w;
			this.so = so;
			this.ids = new HashSet<Integer>();
		}
		/**
		 * Timer has gone off, cancel old command
		 */
        public void run() {
        	String sokey = ShopOwner.getShopOwnerKey(so);
        	Integer id = bukkittimers.get(sokey);
    		try{
        		Bukkit.getServer().getScheduler().cancelTask(id);
        		} catch(Exception e){}

        	bukkittimers.remove(sokey);

        	WorldShop.updateAffectedSigns(w, so, ids);
        }
        public void addIds(Set<Integer> ids){
        	this.ids.addAll(ids);
        }
    }

	public void chestClick(Chest chest, Player player, boolean isLeftClick) {
		if (isLeftClick){ /// dont care if its a left click
			return;}
		ShopChest schest = WorldShop.getShopChest(chest);
		if (schest == null || isLeftClick)  
			return;
		ShopOwner prevOwner = schest.getOwner();
		if (prevOwner == null){ /// should this ever really happen?? well test for it
			return;}
		World w = schest.getWorld();
		ShopOwner newOwner = new ShopOwner(player);		
		boolean playerIsAdmin = PermissionController.isAdmin(player);
		Shop prevOwnerShop = WorldShop.getShop(chest.getWorld(), prevOwner);
		
		Boolean hasShopPermission = prevOwnerShop != null ? prevOwnerShop.playerHasPermission(player) : false;
		if (playerIsAdmin || hasShopPermission ){
			/// Clicking on your own shop might mean a change in inventory
			/// If not bukkitcontrib/spout implemented we need another way to figure out change in inv
			/// A kludge is just recheck our signs after a period of time
			/// Only need to check right click events
			if (!isLeftClick)
				setClickTimer(w, prevOwner, schest.getItemIds());
			return; /// Clicking on your own shop does nothing, or if the person has permission
		} else {
			/// new Owner of this chest, remove previous
			breakConnection(schest,prevOwner,newOwner);
			
			WorldShop.updateAffectedSigns(w, prevOwner, schest.getItemIds());
			ShopOwner.sendMsgToOwner(prevOwner, MessageController.getMessage("Break_link"));
			return;	
		}
	}

	public void setClickTimer(World w, ShopOwner so, Set<Integer> ids) {
    	String sokey = ShopOwner.getShopOwnerKey(so);
    	OwnChestClickedTimerTask occtt ;
    	Integer oldid = bukkittimers.get(sokey);
    	if (oldid != null){
    		try{
    		Bukkit.getServer().getScheduler().cancelTask(oldid);
    		occtt = ownTasks.get(sokey);
    		} catch(Exception e){
        		occtt = new OwnChestClickedTimerTask(w,so);
    		}
    	} else {
    		occtt = new OwnChestClickedTimerTask(w,so);
    	}

    	occtt.addIds(ids);
    	int id = Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(BattleShops.getSelf(),
    			occtt, Defaults.SECONDS_TILL_CHESTUPDATE * 20);
    	bukkittimers.put(sokey, id);
    	ownTasks.put(sokey, occtt);

//    	Timer timer = timers.get(sokey);
//		try { if (timer != null) timer.cancel();} catch (Exception e){}/// Get rid of timer
//		timer = new Timer();
//        timer.schedule(new OwnChestClickedTimerTask(so,ids), Defaults.SECONDS_TILL_CHESTUPDATE*1000);
//        timers.put(sokey, timer);
	}

	
	public void activateChestShop(Chest chest, Player player) {
		activateChestShop(chest,player,null);
	}
	
	public void activateChestShop(Chest chest, Player player, Set<Integer> items) {
		ShopChest schest = WorldShop.getShopChest(chest);
		ShopOwner newOwner = new ShopOwner(player);
		ShopOwner prevOwner = (schest != null) ? schest.getOwner() : null;
		World w = chest.getWorld();
		if (Defaults.DEBUG_LINKING) System.out.println(prevOwner + "------------" + newOwner);
//		boolean hasPermission = PermissionController.hasPermissions(player, chest.getBlock());

		if (prevOwner != null){
			/// We want to unactivate if we have clicked on a chest twice
			if (prevOwner.equals(newOwner)){
				breakConnection(schest,prevOwner,newOwner);
				WorldShop.updateAffectedSigns(w,prevOwner, schest.getItemIds());	
				player.sendMessage(MessageController.getMessage("Unlink_chest"));
				return;
			}
			/// We have another player touching the chest
			Shop oldshop = WorldShop.getShop(w,prevOwner);
			boolean playerIsAdmin = PermissionController.isAdmin(player);
			boolean hasPermissionToTouch = oldshop != null ? oldshop.playerHasPermission(player) : false;
			/// If they can touch this chest, then just return, we dont want them linking other peoples stuff
			if (hasPermissionToTouch || playerIsAdmin){
				return;}
			
			breakConnection(schest,prevOwner,newOwner);
			if (oldshop != null && schest != null){
				WorldShop.updateAffectedSigns(w,prevOwner, schest.getItemIds());	
			}
			ShopOwner.sendMsgToOwner(prevOwner, MessageController.getMessage("Break_link"));			
		}


		Shop shop = WorldShop.addShop(w,newOwner);
		ShopChest shopchest ;
		if (items != null){
			shopchest = new ShopChest(chest, newOwner, items);
		} else {
			shopchest = new ShopChest(chest, newOwner);
		}

		HashMap<String,Integer> linked = shop.addChest(shopchest);

		WorldShop.addShopChest(shopchest);

		if (linked != null && linked.size() > 0){
			for (String item_name : linked.keySet()){
				if (item_name.compareTo(Defaults.EVERYTHING_NAME) == 0){
					player.sendMessage(MessageController.getMessage("Linked_everything"));
					continue;
				}
				int item_count = linked.get(item_name);
				player.sendMessage(MessageController.getMessage("Linked_sign",linked.get(item_name),
						Util.getSignOrSigns(item_count), Util.getHasOrHave(item_count),item_name));
			}		
		} else {
			player.sendMessage(MessageController.getMessage("Unlinked_sign"));
		}
		WorldShop.updateAffectedSigns(w,newOwner, shopchest.getItemIds());
		if (Defaults.DEBUG_LINKING) System.out.println("   activateCompleted");
		if (Defaults.DEBUG_LINKING) WorldShop.printShops();
	}
	
	public static void breakShopSign(Sign craftSign) {
		WorldShop.removeShopSign(craftSign);
	}

	public static void breakChestShop(Chest chest) {
		ShopChest lc = WorldShop.getShopChest(chest);
		if (lc == null) return;
		if (Defaults.DEBUG_LINKING) System.out.println("  literally breaking  " + lc); 
		/// Alright, we do have a single or double chest with this location
		ShopOwner previous_owner = lc.getOwner();
		WorldShop.removeChest(lc, previous_owner);
		WorldShop.removeChest(lc);

		WorldShop.updateAffectedSigns(chest.getWorld(),previous_owner, lc.getItemIds());
		if (Defaults.DEBUG_LINKING) System.out.println("   breakCompleted");
		if (Defaults.DEBUG_LINKING) WorldShop.printShops();	
	}
	
	private static void breakConnection(ShopChest chest, ShopOwner previous_owner, ShopOwner new_owner){
		/// Remove chest from previous owner and get rid of it in shopchests
		WorldShop.removeChest(chest, previous_owner);
		WorldShop.removeChest(chest);
	}

	public void openChestRemotely(Player player, ShopSign ss) {
//		Shop shop = WorldShop.getShop(player);
//		ChestSet chestset = new ChestSet(shop.getChestsByID(ss.getItemId()));
//		int size = chestset.size();
//		if (size <= 0){
//			player.sendMessage("This sign doesn't link to any chests");
//			return;
//		}
//		ShopChest c = chestset.getFirst();
//		if (c == null){
//			player.sendMessage("Couldnt find chest");
//			return;
//		}
//		Chest chest = c.getChest();
//
//		Inventory inv = chest.getInventory();
//		chest = c.getNeighborhChest();
//		if (chest != null) { 
//			inv = new InventoryLargeChest(
//					player.getName() + "'s ShopChest", inv, ((CraftInventory) chest.getInventory()).getInventory());
//		}
//
//		((CraftPlayer) player).getHandle().a(inv);
////		player.updateInventory();
//		/// Set our click timer for a right click
//		setClickTimer(new ShopOwner(player), c.getItemIds());
	}

}
