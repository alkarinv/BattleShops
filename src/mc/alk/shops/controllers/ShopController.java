package mc.alk.shops.controllers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import mc.alk.mc.MCItemStack;
import mc.alk.mc.MCLocation;
import mc.alk.mc.MCPlayer;
import mc.alk.mc.MCWorld;
import mc.alk.mc.blocks.MCChest;
import mc.alk.mc.blocks.MCSign;
import mc.alk.shops.BattleShops;
import mc.alk.shops.Defaults;
import mc.alk.shops.objects.PlayerActivity;
import mc.alk.shops.objects.ShopChest;
import mc.alk.shops.objects.ShopOwner;
import mc.alk.shops.objects.ShopSign;
import mc.alk.shops.utils.KeyUtil;
import mc.alk.shops.utils.WorldUtil;
import mc.alk.util.Log;


/**
 *
 * @author alkarin
 *
 */
public class ShopController {

	private static Map<MCWorld, Map<String, Shop >> allshops = new HashMap<MCWorld, Map<String, Shop>>();
	private static Map<MCWorld, Map<String, ShopSign >> shopsigns = new HashMap<MCWorld, Map<String, ShopSign>>();
	private static Map<MCWorld, Map<String, ShopChest >> shopchests = new HashMap<MCWorld, Map<String, ShopChest>>();
	private static Map<String, PlayerActivity> playeractivity =
			new HashMap<String, PlayerActivity>(); // Is this user and shop active


	public static Map<MCWorld, Map<String, Shop>> getAllShops() {return allshops;}
	public static Map<MCWorld, Map<String, ShopSign>> getAllSigns() {return shopsigns;}
	public static Map<MCWorld, Map<String, ShopChest>> getAllChests() {return shopchests;}

	public static int addShopSign(ShopSign shopsign) {
		ShopOwner so = shopsign.getOwner();
		if (Defaults.DEBUG_TRACE) System.out.println("addShopSign.. owner=" + so + "  type=" + shopsign.getItemId() +" MCWorld=" + shopsign.getWorld());
		Shop shop = loadShop(shopsign.getWorld(), so);

		Map<String, ShopSign> signs = getSigns(shopsign.getWorld());
		if (signs == null){
			signs = new HashMap<String,ShopSign>();
			addSigns(shopsign.getWorld(),signs);
		}
		String lockey = KeyUtil.getStringLoc(shopsign);
		synchronized(signs){
			signs.put(lockey, shopsign);
		}
		int chestCount = shop.addShopSign(shopsign);
		if (so.isAdminShop())
			return -1;
		ShopController.updateAffectedSigns(so,shopsign);
		return chestCount;
	}

	private static Map<String, ShopSign> getSigns(MCWorld w) {
		return shopsigns.get(Defaults.MULTIWORLD ? null : w);
	}

	private static Map<String, ShopChest> getChests(MCWorld w) {
		return shopchests.get(Defaults.MULTIWORLD ? null : w);
	}

	private static Map<String, Shop> getShops(MCWorld w) {
		return allshops.get(Defaults.MULTIWORLD ? null : w);
	}

	private static void addShops(MCWorld w, Map<String, Shop> shops) {
		allshops.put(Defaults.MULTIWORLD ? null : w, shops);
	}
	private static void addSigns(MCWorld w, Map<String, ShopSign> signs) {
		shopsigns.put(Defaults.MULTIWORLD ? null : w, signs);
	}
	private static void addChests(MCWorld w, Map<String, ShopChest> chests) {
		shopchests.put(Defaults.MULTIWORLD ? null : w, chests);
	}


	public static Shop getShop(MCWorld world, ShopOwner so) {
		Map<String,Shop> shops = getShops(world);
		if (shops==null || shops.isEmpty())
			return null;
		return shops.get(so.getKey());
	}

	private static Shop loadShop(MCWorld world, ShopOwner so) {
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


	public static ShopSign findShopSign(MCSign sign) {
		Map<String, ShopSign> signs = getSigns(sign.getWorld());
		if (signs == null || signs.isEmpty())
			return null;
		return signs.get(KeyUtil.getStringLoc(sign));
	}

	public static boolean hasShopSignAt(MCLocation loc) {
		Map<String,ShopSign> signs = getSigns(loc.getWorld());
		if (signs == null || signs.isEmpty())
			return false;
		return signs.containsKey(KeyUtil.getStringLoc(loc));
	}

	public static ShopSign getShopSign(MCLocation loc) {
		Map<String,ShopSign> signs = getSigns(loc.getWorld());
		if (signs == null || signs.isEmpty())
			return null;
		return signs.get(KeyUtil.getStringLoc(loc));
	}

	public static boolean hasShopChestAt(MCChest chest){return getShopChest(chest) == null ? false : true;}

	public static ShopChest getShopChest(MCChest chest) {
		ShopChest sc = getShopChest(chest.getLocation());
		if (sc == null){
			try {
				chest = chest.getNeighborChest();
				if (chest != null)
					sc = getShopChest(chest.getLocation());
			} catch (IllegalStateException e){
				/* do nothing */
			}
		}
		return sc;
	}

	public static ShopChest getShopChest(MCLocation location) {
		Map<String, ShopChest> chests = getChests(location.getWorld());
		if (chests == null || chests.isEmpty()){
			return null;}
		String loc = KeyUtil.getStringLoc(location);
		/// do we have a chest here
		ShopChest sc = chests.get(loc);
		/// if its null, we might have a chest nearby, try and return that
		if (sc == null){
			MCChest neighbor = WorldUtil.getNeighborChest(location);
			if (neighbor == null) return null;
			loc = KeyUtil.getStringLoc(neighbor);
			sc = chests.get(loc);
		}
		return sc;
	}

	public static Shop addShop(MCWorld w, ShopOwner owner) {
		return loadShop(w, owner);
	}

	public static void addShopChest(ShopChest shopChest) {
		Map<String, ShopChest> chests = getChests(shopChest.getWorld());
		if (chests == null){
			chests = new HashMap<String,ShopChest>();
			addChests(shopChest.getWorld(),chests);
		}
		Shop shop = loadShop(shopChest.getWorld(), shopChest.getOwner());
		shop.addChest(shopChest);

		MCChest chest2 = shopChest.getChest().getNeighborChest();
		if (chest2 != null){
			chests.remove(KeyUtil.getStringLoc(shopChest));
			chests.remove(KeyUtil.getStringLoc(chest2));
			chests.put(minLoc(shopChest.getLocation(),chest2.getLocation()), shopChest);
		} else {
			chests.put(KeyUtil.getStringLoc(shopChest), shopChest);
		}
	}

	private static String minLoc(MCLocation location, MCLocation location2) {
		if (location.getBlockX() < location2.getBlockX() || location.getBlockZ() < location2.getBlockZ())
			return KeyUtil.getStringLoc(location);
		return KeyUtil.getStringLoc(location2);
	}

	public static void removeShopSign(MCSign sign) {
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
		BattleShops.getShopSerializer().deleteShopSign(ss);
	}

	public static void removeChest(ShopChest shopChest) {
		Shop shop = getShop(shopChest.getWorld(), shopChest.getOwner());
		if (shop != null){
			shop.removeDoubleChestFromShop(shopChest);
		}
		Map<String,ShopChest> chests = getChests(shopChest.getWorld());
		if (chests == null || chests.isEmpty())
			return;
		chests.remove(KeyUtil.getStringLoc(shopChest));
		BattleShops.getShopSerializer().deleteShopChest(shopChest);
		try {
			MCChest chest2 = shopChest.getChest().getNeighborChest();
			if (chest2 != null){
				chests.remove(KeyUtil.getStringLoc(chest2));
			}
		} catch (IllegalStateException e){
			/* do nothing */
		}

	}

	public static void printAll(){
		printShopSigns();
		printShopChests();
		printShops();
	}

	public static void printShopSigns() {
		Log.info("Signs # " + shopsigns.size());
		for (MCWorld world : shopsigns.keySet()){
			Log.info(" ## Signs in World = " + world +"   " + shopsigns.get(world).size());
			for ( ShopSign ss : shopsigns.get(world).values()){
				Log.info("   " + ss);
			}
		}
	}
	public static void printShopChests() {
		Log.info("Chests # " + shopsigns.size());
		for (MCWorld world : shopchests.keySet()){
			Log.info(" ## Chests in World = " + world +"   " + shopchests.get(world).size());
			for ( ShopChest chest : shopchests.get(world).values()){
				Log.info(chest+"");
			}
		}
	}
	public static void printShops() {
		Log.info("Shops # " + allshops.size());
		for (MCWorld world : allshops.keySet()){
			Log.info(" ## Shops in World = " + world +"   " + allshops.get(world).size());
			for ( Shop shop : allshops.get(world).values()){
				Log.info(shop+"");
			}
		}
	}

	public static Shop getShop(MCPlayer player){
		return loadShop(player.getWorld(), new ShopOwner(player));
	}

	public static Shop getShop(MCWorld world, String name){
		return loadShop(world, new ShopOwner(name));
	}

	/**
	 * Called after adding a sign to the MCWorld
	 * also temporarily is called from after a transaction has taken place
	 * @param so
	 * @param ss
	 */
	public static void updateAffectedSigns(MCWorld w, ShopOwner so, Collection<MCItemStack> list) {
		Shop shop = getShop(w, so);
		if (shop != null && list != null && !list.isEmpty())
			shop.updateSignsByItems(list);
	}

	public static void updateAffectedSigns(ShopOwner so, ShopSign ss) {
		Shop shop = getShop(ss.getWorld(), so);
		if (shop != null)
			shop.updateSignsByItem(ss.getItemStack());
	}

	public static void updateAllSigns() {
		for (MCWorld w: shopsigns.keySet()){
			updateAllSigns(w);
		}
	}

	public static void updateAllSigns(MCWorld w) {
		HashSet<MCItemStack> items = new HashSet<MCItemStack>();
		Map<String, Shop> shops = getShops(w);
		if (shops == null)
			return;
		for (Shop shop : shops.values()){
			items.clear();
			for (ShopChest chest : shop.getChests()){
				items.addAll(chest.getItemIds());}
			updateAffectedSigns(w, shop.getOwner(), items);
		}
	}

	public static void updateAffectedSigns(MCWorld world, ShopOwner previous_owner, ShopChest lc) {
		HashSet<MCItemStack> items = new HashSet<MCItemStack>();
		items.addAll(lc.getItemIds());
		updateAffectedSigns(lc.getWorld(), lc.getOwner(), items);
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

//	public static Collection<ShopSign> getShopsWithInventory(MCWorld w, int itemid) {
//		Map<String,ShopSign> signs = getSigns(w);
//		Collection<ShopSign> retsigns = new ArrayList<ShopSign>();
//		if (signs == null)
//			return retsigns;
//		synchronized(signs){
//			for (ShopSign ss : signs.values()){
//				if (w != null && !ss.getWorld().equals(w))
//					continue;
//				if (ss.getItemId() == itemid &&
//						((!ss.isUnlinked() && !ss.isEmpty())) ){
//					retsigns.add(ss);
//				}
//			}
//		}
//		return retsigns;
//	}
//
//	public static Collection<ShopSign> getShopsNotFull(MCWorld w, int itemid) {
//		Map<String,ShopSign> signs = getSigns(w);
//		Collection<ShopSign> retsigns = new ArrayList<ShopSign>();
//		if (signs == null)
//			return retsigns;
//		synchronized(signs){
//			for (ShopSign ss : signs.values()){
//				if (w != null && !ss.getWorld().equals(w))
//					continue;
//				if (ss.getItemId() == itemid && !ss.isUnlinked() ){
//					retsigns.add(ss);
//				}
//			}
//		}
//		return retsigns;
//	}



}
