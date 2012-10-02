package com.alk.battleShops.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.alk.battleShops.BattleShops;
import com.alk.battleShops.Defaults;
import com.alk.battleShops.controllers.MessageController;
import com.alk.battleShops.controllers.PermissionController;
import com.alk.battleShops.util.KeyUtil;
import com.alk.battleShops.util.Util;

/**
 * 
 * @author alkarin
 *
 */
public class WorldShop {

	private static Map<World, Map<String, Shop >> allshops = new HashMap<World, Map<String, Shop>>();
	private static Map<World, Map<String, ShopSign >> shopsigns = new HashMap<World, Map<String, ShopSign>>();
	private static Map<World, Map<String, ShopChest >> shopchests = new HashMap<World, Map<String, ShopChest>>();
	private static Map<String, PlayerActivity> playeractivity =
			new HashMap<String, PlayerActivity>(); // Is this user and shop active


	public static Map<World, Map<String, Shop>> getAllShops() {return allshops;}
	public static Map<World, Map<String, ShopSign>> getAllSigns() {return shopsigns;}
	public static Map<World, Map<String, ShopChest>> getAllChests() {return shopchests;}

	public static int addShopSign(ShopSign sign) {
		ShopOwner so = sign.getOwner();
		if (Defaults.DEBUG_TRACE) System.out.println("addShopSign.. owner=" + so + "  type=" + sign.getCommonName() +" world=" + sign.getWorld());
		Shop shop = loadShop(sign.getWorld(), so);

		Map<String, ShopSign> signs = getSigns(sign.getWorld());
		if (signs == null){
			signs = new HashMap<String,ShopSign>();
			addSigns(sign.getWorld(),signs);
		}
		String lockey = KeyUtil.getStringLoc(sign);
		synchronized(signs){
			signs.put(lockey, sign);
		}
		int chestCount = shop.addShopSign(sign);
		Player soplayer = BattleShops.getBukkitServer().getPlayer(so.getName());
		if (soplayer == null || PermissionController.isAdmin(so.getName()))
			return -1;
		if (chestCount > 0){
			soplayer.sendMessage(MessageController.getMessage("setup_shop",sign.getCommonName(),chestCount,Util.getChestOrChests(chestCount)));
		} else {
			soplayer.sendMessage(MessageController.getMessage("setup_shop_no_chests",sign.getCommonName()));
		}
		return chestCount;
	}

	private static Map<String, ShopSign> getSigns(World w) {
		return shopsigns.get(Defaults.MULTIWORLD ? null : w);
	}

	private static Map<String, ShopChest> getChests(World w) {
		return shopchests.get(Defaults.MULTIWORLD ? null : w);
	}

	private static Map<String, Shop> getShops(World w) {
		return allshops.get(Defaults.MULTIWORLD ? null : w);
	}

	private static void addShops(World w, Map<String, Shop> shops) {
		allshops.put(Defaults.MULTIWORLD ? null : w, shops);
	}
	private static void addSigns(World w, Map<String, ShopSign> signs) {
		shopsigns.put(Defaults.MULTIWORLD ? null : w, signs);
	}
	private static void addChests(World w, Map<String, ShopChest> chests) {
		shopchests.put(Defaults.MULTIWORLD ? null : w, chests);
	}

	public static void removeChest(ShopChest lc) {
		Map<String,ShopChest> chests = getChests(lc.getWorld());
		if (chests == null || chests.isEmpty())
			return;
		chests.remove(KeyUtil.getStringLoc(lc));
		BattleShops.getStorageController().deleteShopChest(lc);
		Chest neighbor = lc.getNeighborChest();
		if (neighbor != null){
			shopchests.remove(KeyUtil.getStringLoc(neighbor));
			BattleShops.getStorageController().deleteChest(neighbor);
		}
	}

	public static void removeChest(ShopChest mc, ShopOwner owner) {
		Shop shop = getShop(mc.getWorld(), owner);
		if (shop != null){
			shop.removeDoubleChestFromShop(mc);
		}
	}

	public static Shop getShop(World world, ShopOwner so) {
		Map<String,Shop> shops = getShops(world);
		if (shops==null || shops.isEmpty())
			return null;
		return shops.get(so.getKey());
	}

	private static Shop loadShop(World world, ShopOwner so) {
		Shop shop = getShop(world,so);
		if (shop == null){
			Map<String,Shop> shops = getShops(world);
			if (shops==null){
				shops = new HashMap<String,Shop>();
				addShops(world, shops);
			}
			shop = new Shop(so);
			shops.put(so.getKey(), shop);
		}
		return shop;
	}


	public static ShopSign findShopSign(Sign sign) {
		Map<String, ShopSign> signs = getSigns(sign.getWorld());
		if (signs == null || signs.isEmpty())
			return null;
		return signs.get(KeyUtil.getStringLoc(sign));
	}

	public static boolean hasShopSignAt(Location loc) {
		Map<String,ShopSign> signs = getSigns(loc.getWorld());
		if (signs == null || signs.isEmpty())
			return false;
		return signs.containsKey(KeyUtil.getStringLoc(loc));
	}

	public static ShopSign getShopSign(Location loc) {
		Map<String,ShopSign> signs = getSigns(loc.getWorld());
		if (signs == null || signs.isEmpty())
			return null;
		return signs.get(KeyUtil.getStringLoc(loc));
	}

	public static boolean hasShopChestAt(Chest chest){return getShopChest(chest) == null ? false : true;}

	public static ShopChest getShopChest(Chest chest) {
		return getShopChest(chest.getLocation());
	}

	public static ShopChest getShopChest(Location location) {
		Map<String, ShopChest> chests = getChests(location.getWorld());
		if (chests == null || chests.isEmpty())
			return null;
		String loc = KeyUtil.getStringLoc(location);
		/// do we have a chest here
		ShopChest sc = chests.get(loc);
		/// if its null, we might have a chest nearby, try and return that
		if (sc == null){
			Chest neighbor = ShopChest.getNeighborChest(location);
			if (neighbor == null) return null;
			loc = KeyUtil.getStringLoc(neighbor);
			sc = chests.get(loc);			
		}
		return sc;
	}

	public static Shop addShop(World w, ShopOwner owner) {
		return loadShop(w, owner);
	}


	public static void addShopChest(ShopChest shopchest) {
		Map<String, ShopChest> chests = getChests(shopchest.getWorld());
		if (chests == null){
			chests = new HashMap<String,ShopChest>();
			addChests(shopchest.getWorld(),chests);
		}
		chests.put(KeyUtil.getStringLoc(shopchest), shopchest);
		Shop shop = loadShop(shopchest.getWorld(), shopchest.getOwner());
		shop.addChest(shopchest);
	}


	public static void removeShopSign(Sign sign) {
		ShopSign ss = null;
		Map<String, ShopSign> signs = getSigns(sign.getWorld());
		synchronized(signs){
			ss = signs.remove(KeyUtil.getStringLoc(sign));
		}
		if (ss != null){
			ShopOwner so = ss.getOwner();
			Shop shop = loadShop(sign.getWorld(), so);
			if (shop != null){
				shop.removeSign(ss);
			}
		}
		BattleShops.getStorageController().deleteShopSign(ss);
	}


	public static void printShops() {
		System.out.println("@@########");
		//		for ( ShopSign ss : shopsigns.values()){
		//			System.out.println(ss);
		//		}
		//		System.out.println("----------");
		//		for ( ShopChest chest : shopchests.values()){
		//			System.out.println(chest);
		//		}
		System.out.println("@@########");
	}

	public static Shop getShop(Player player){
		return loadShop(player.getWorld(), new ShopOwner(player));
	}

	/**
	 * Called after adding a sign to the world
	 * also temporarily is called from after a transaction has taken place
	 * @param so
	 * @param ss
	 */
	public static void updateAffectedSigns(World w, ShopOwner so, Set<Integer> ids) {
		Shop shop = getShop(w, so);
		if (shop != null && ids != null && !ids.isEmpty())
			shop.updateSignsByItemId(ids);
	}


	public static void updateAffectedSigns(ShopOwner so, ShopSign ss) {
		Shop shop = getShop(ss.getWorld(), so);
		if (shop != null)
			shop.updateSignsByItemId(ss.getItemId());
	}

	public static void updateAllSigns() {
		for (World w: shopsigns.keySet()){
			updateAllSigns(w);
		}
	}

	public static void updateAllSigns(World w) {
		HashSet<Integer> ids = null;
		Map<String, Shop> shops = getShops(w);
		for (Shop shop : shops.values()){
			ids = new HashSet<Integer>();
			for (ShopSign sign : shop.getSigns()){
				ids.add(sign.getItemId());
			}
			updateAffectedSigns(w, shop.getOwner(), ids);
		}
	}

	public static PlayerActivity getPlayerActivity(String name){
		PlayerActivity pa = playeractivity.get(name);
		if (pa == null){
			pa = new PlayerActivity(name);
			playeractivity.put(name, pa);
		}
		return pa;

	}
	public static void playerShopTransaction(ShopOwner owner) {
		PlayerActivity pa = getPlayerActivity(owner.getName());
		pa.shopTransaction();
	}

	public static void onPlayerLogin(String name) {
		PlayerActivity pa = getPlayerActivity(name);
		pa.playerEntered();
	}

	public static void playerUpdatedShop(String name) {
		PlayerActivity pa = getPlayerActivity(name);
		pa.playerUpdatedShop();
	}

	public static Map<String, PlayerActivity> getPlayerActivity() {return playeractivity;}
	public static void setPlayerAcitivity(Map<String, PlayerActivity> pa) {playeractivity = pa;}

	public static Collection<ShopSign> getShopsWithInventory(World w, int itemid) {
		Map<String,ShopSign> signs = getSigns(w);
		Collection<ShopSign> retsigns = new ArrayList<ShopSign>();
		if (signs == null)
			return retsigns;
		synchronized(signs){
			for (ShopSign ss : signs.values()){
				if (w != null && !ss.getWorld().equals(w))
					continue;
				if (ss.getItemId() == itemid && 
						((!ss.isUnlinked() && !ss.isEmpty())) ){
					retsigns.add(ss);
				}
			}
		}
		return retsigns;
	}

	public static Collection<ShopSign> getShopsNotFull(World w, int itemid) {
		Map<String,ShopSign> signs = getSigns(w);
		Collection<ShopSign> retsigns = new ArrayList<ShopSign>();
		if (signs == null)
			return retsigns;
		synchronized(signs){
			for (ShopSign ss : signs.values()){
				if (w != null && !ss.getWorld().equals(w))
					continue;
				if (ss.getItemId() == itemid && !ss.isUnlinked() ){
					retsigns.add(ss);
				}
			}
		}
		return retsigns;
	}



}
