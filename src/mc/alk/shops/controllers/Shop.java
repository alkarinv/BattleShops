package mc.alk.shops.controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mc.alk.mc.MCItemStack;
import mc.alk.mc.MCLocation;
import mc.alk.mc.MCPlayer;
import mc.alk.mc.blocks.MCChest;
import mc.alk.shops.Defaults;
import mc.alk.shops.objects.ChestSet;
import mc.alk.shops.objects.EverythingItem;
import mc.alk.shops.objects.ShopChest;
import mc.alk.shops.objects.ShopOwner;
import mc.alk.shops.objects.ShopSign;
import mc.alk.shops.utils.CompositeMap;
import mc.alk.shops.utils.InventoryUtil;
import mc.alk.shops.utils.KeyUtil;
import mc.alk.util.Log;

/**
 *
 * @author alkarin
 *
 */
public class Shop implements Serializable {
	private static final long serialVersionUID = 1L;

	private CompositeMap<HashMap<String,ShopChest>> chests_by_itemid = new CompositeMap<HashMap<String,ShopChest>>();
	private CompositeMap<HashMap<String,ShopSign>> signs_by_itemid = new CompositeMap<HashMap<String,ShopSign>>();
	private HashMap<String,ShopChest> chests_by_loc = new HashMap<String,ShopChest>();
	private HashMap<String,ShopSign> signs_by_loc = new HashMap<String,ShopSign>();
	private final ShopOwner owner;

	private HashSet<String> associates;

	public Shop(ShopOwner owner) {
		this.owner = owner;
	}

	public ShopOwner getOwner() {return owner;}

	public Set<String> getAssociates() {return associates;}

	public Collection<ShopSign> getSigns() {return signs_by_loc.values();}

	//	public long toKey(MCItemStack item){
	//		long key = (((long)item.getType()) << 32) | (item.getDataValue() & 0xffffffffL) << 1;
	//		if ( (item.getEnchantments() != null && !item.getEnchantments().isEmpty()) ||
	//				item.hasMetaData()){
	//			key +=1; /// flip the unique bit
	//		}
	//		return key;
	//	}

	public int getNumChestsAttachedToSign(ShopSign sign){
		MCItemStack item = sign.getItemStack();
		long key = KeyUtil.toKey(item);
		int count = 0;
		count += chests_by_itemid.containsKey(key) ? chests_by_itemid.get(key).size() : 0;
		count += chests_by_itemid.containsKey(Defaults.EVERYTHING_ID) ? chests_by_itemid.get(Defaults.EVERYTHING_ID).size() : 0;
		return count;
	}

	public int addShopSign(ShopSign sign) {
		String key = KeyUtil.getStringLoc(sign);
		signs_by_loc.put(key, sign);
		MCItemStack item = sign.getItemStack();
		Long itemid = KeyUtil.toKey(item);
		if (!signs_by_itemid.containsKey(itemid)){
			signs_by_itemid.put(itemid, new HashMap<String,ShopSign>());
		}
		Map<String,ShopSign> ss = signs_by_itemid.get(itemid);
		ss.put(key, sign);
		int count = 0;
		count += chests_by_itemid.containsKey(itemid) ? chests_by_itemid.get(itemid).size() : 0;
		count += chests_by_itemid.containsKey(Defaults.EVERYTHING_ID) ? chests_by_itemid.get(Defaults.EVERYTHING_ID).size() : 0;

		return count;
	}

	public HashMap<String,Integer> addChest(ShopChest chest) {
		if (Defaults.DEBUG_LINKING) Log.info("[BattleShops] addChest at " + chest.getLocation());
		List<MCItemStack> items = chest.getItemIds();
		/// Trying to activate a chest with nothing in it, abort, everything_id is something and would show up here
		if (items == null || items.isEmpty())
			return null;

		MCLocation cl = chest.getLocation();
		/// Somehow occasionally,
		for (ShopChest sc : chests_by_loc.values()){
			if (!sc.getWorld().getName().equals(cl.getWorld().getName()))
				continue;
			int distance = (cl.getBlockY() == sc.getY()) ? (int) (Math.abs(cl.getBlockX() - sc.getX()) + Math.abs(cl.getBlockZ() - sc.getZ())): Integer.MAX_VALUE;
			if (distance <= 1){
				return null;}
		}
		String key = KeyUtil.getStringLoc(chest);
		chests_by_loc.put(key, chest);
		HashMap<String,Integer> itemname_count = new HashMap<String, Integer>();

		List<Long> ids_with_signs = new ArrayList<Long>();
		for (MCItemStack item : chest.getItemIds()){
			if (item== null || item.getType()==0){
				continue;}
			Long id = KeyUtil.toKey(item);
			final String cn = item.getCommonName();
			if (cn.isEmpty()){ /// As of 1.0.0 sometimes this is empty for some reason, figure out why
				System.err.println("Commonname id=" + id  +"  location=" + chest.getLocation());
				continue;
			}
			addItemId(key,chest,id, cn, itemname_count,ids_with_signs);
		}
		return itemname_count;
	}
	private void addItemId(String key, ShopChest chest, long id, String commonName,
			HashMap<String,Integer> itemname_count, List<Long> ids_with_signs){
		if (!chests_by_itemid.containsKey(id)){
			chests_by_itemid.put(id, new HashMap<String,ShopChest>());
		}
		Map<String,ShopChest> c = chests_by_itemid.get(id);
		c.put(key, chest);
		if (signs_by_itemid.containsKey(id)){
			ids_with_signs.add(id);
			itemname_count.put(commonName, signs_by_itemid.get(id).size());
		} else {
			itemname_count.put(commonName, 0);
		}
	}

	public Map<Long,Collection<ShopChest>> getChestsByID(long itemId) {
		Map<Long,Collection<ShopChest>> chests = new HashMap<Long, Collection<ShopChest>>();
		if (chests_by_itemid.containsKey(itemId))
			chests.put(itemId,chests_by_itemid.get(itemId).values());
		if (chests_by_itemid.containsKey(Defaults.EVERYTHING_ID))
			chests.put(Defaults.EVERYTHING_ID,chests_by_itemid.get(Defaults.EVERYTHING_ID).values());
		return chests;
	}

	public void removeSign(ShopSign ss) {
		String lockey = KeyUtil.getStringLoc(ss);
		signs_by_loc.remove(lockey);
		long key = KeyUtil.toKey(ss.getItemStack());
		Map<String,ShopSign> map = signs_by_itemid.get(key);
		if (map != null){
			map.remove(lockey);
			if (map.size() <= 0){
				signs_by_itemid.remove(key);}
		}

	}

	/**
	 *
	 * @param chest
	 */
	public void removeDoubleChestFromShop(ShopChest chest) {
		//
		String lockey = KeyUtil.getStringLoc(chest);
		String lockey2 = getNeighborChestLoc(chest);
		if (Defaults.DEBUG_LINKING) System.out.println("Shop::removeDoubleChest  " + chest + "    " + lockey + ":" + lockey2);
		chests_by_loc.remove(lockey);
		if (lockey2 != null) chests_by_loc.remove(lockey2);

		List<MCItemStack> items = chest.getItemIds();

		/// Otherwise remove all these item ids from shop
		for (MCItemStack item : items){
			Long key = KeyUtil.toKey(item);
			Map<String, ShopChest> map = chests_by_itemid.get(key);
			if (map != null){
				map.remove(lockey);
				if (lockey2 != null) map.remove(lockey2);
				if (map.size() <= 0){
					chests_by_itemid.remove(key);}
			}
		}
	}

	private static String getNeighborChestLoc(ShopChest lc) {
		MCChest neighbor = lc.getChest().getNeighborChest();
		return (neighbor != null) ? KeyUtil.getStringLoc(neighbor.getLocation()) : null;
	}

	public void updateSignsByItem(long itemid) {
		if (itemid == Defaults.EVERYTHING_ID){
			Set<Long> ids = new HashSet<Long>();
			for (Long id : signs_by_itemid.keySet()){
				if (id != Defaults.EVERYTHING_ID && !ids.contains(id)){
					ids.add(id);
					updateSigns(id);
				}
			}
		} else {
			updateSigns(itemid);
		}
	}

	public void updateSignsByItem(MCItemStack item) {
		if (EverythingItem.isEverythingItem(item)){
			for (Long id : signs_by_itemid.keySet()){
				if (id != Defaults.EVERYTHING_ID)
					updateSigns(id);
			}
		} else {
			updateSigns(KeyUtil.toKey(item));
		}
	}

	public void updateSignsByItems(Collection<MCItemStack> list) {
		for (MCItemStack item: list){
			if (EverythingItem.isEverythingItem(item)){
				for (Long id : signs_by_itemid.keySet()){
					if (id != Defaults.EVERYTHING_ID)
						updateSigns(id);
				}
			} else {
				updateSigns(KeyUtil.toKey(item));
			}
		}
	}

//	public void updateSignsByItemId(Set<Long> itemIds) {
//		Set<Long> ids = new HashSet<Long>();
//		for (Long id : itemIds){
//			if (id == 0) continue;
//			if (!ids.contains(id)){
//				updateSigns(id);
//			}
//		}
//	}
	private void updateSigns(long itemid) {
		Map<String,ShopSign> signs = signs_by_itemid.get(itemid);
		if (signs == null || signs.size() == 0)
			return;

		List<ShopChest> chests = new ArrayList<ShopChest>();
		if (chests_by_itemid.containsKey(itemid))
			chests.addAll(chests_by_itemid.get(itemid).values());
		if (chests_by_itemid.containsKey(Defaults.EVERYTHING_ID))
			chests.addAll(chests_by_itemid.get(Defaults.EVERYTHING_ID).values());
		if (chests.isEmpty()){
			for (ShopSign sign: signs.values()){
				sign.setUnlinked();}}
		else{
			ChestSet chestset = new ChestSet(itemid,chests);

			int id = CompositeMap.getHOB(itemid);
			int datavalue = CompositeMap.getLOB(itemid);

			MCItemStack is = InventoryUtil.parseItemStack(id,(short) datavalue);
			int amount = chestset.amount(is);
			int free = chestset.freeSpaceAfter(is);
			for (ShopSign sign : signs.values()){
				sign.setSignAmount(amount, free);
			}
		}
	}



	public boolean playerHasPermission(MCPlayer player) {
		return owner.getName().equals(player.getName()) || (associates != null ? associates.contains(player.getName().toLowerCase()) : false) ;
	}
	public void addToAssociates(String player){
		if (associates == null){
			associates = new HashSet<String>();}
		associates.add(player.toLowerCase());
	}
	public void removeFromAssociates(String player){
		if (associates == null)
			return;
		associates.remove(player.toLowerCase());
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder("Shop[owner=" + owner.getName());
		for (Long itemid : chests_by_itemid.keySet()){
			Map<String,ShopChest> list = chests_by_itemid.get(itemid);
			sb.append("\n - item id = " + itemid +" ");
			for (ShopChest l: list.values()){
				if (l != null) sb.append("\t" + l + " ,");
				else sb.append("\t" + "null ,");
			}
		}
		sb.append("-----");
		for (ShopChest chest: chests_by_loc.values()){
			sb.append("\t" + chest);
		}
		for (ShopSign ss : signs_by_loc.values()){
			sb.append("\t" + ss);
		}
		sb.append("]");
		return sb.toString();
	}

	public Collection<ShopChest> getChests() {
		return chests_by_loc.values();
	}



}
