package com.alk.battleShops.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.alk.battleShops.Defaults;
import com.alk.battleShops.util.KeyUtil;
/**
 * 
 * @author alkarin
 *
 */
public class Shop implements Serializable {
	private static final long serialVersionUID = 1L;
	

	public ShopOwner getOwner() {return owner;}
	private HashMap<Integer, Map<String,ShopChest>> chests_by_itemid;
	private HashMap<Integer, Map<String,ShopSign>> signs_by_itemid;
	private HashMap<String,ShopChest> chests_by_loc;
	private HashMap<String,ShopSign> signs_by_loc;
	private final ShopOwner owner;
	
	private HashSet<String> associates;

	public Shop(ShopOwner player) {
		this.owner = player;
		signs_by_loc = new HashMap<String,ShopSign>();
		chests_by_loc = new HashMap<String,ShopChest>();

		signs_by_itemid = new HashMap<Integer,Map<String,ShopSign>>();
		chests_by_itemid = new HashMap<Integer,Map<String,ShopChest>>();
	}

	public Set<String> getAssociates() {return associates;}

	public Collection<ShopSign> getSigns() {return signs_by_loc.values();}

	public int getNumChestsAttachedToSign(ShopSign sign){
		int itemid = sign.getItemId();
		int count = 0;
		count += chests_by_itemid.containsKey(itemid) ? chests_by_itemid.get(itemid).size() : 0;
		count += chests_by_itemid.containsKey(Defaults.EVERYTHING_ID) ? chests_by_itemid.get(Defaults.EVERYTHING_ID).size() : 0;
		return count;
	}

	public int addShopSign(ShopSign sign) {
		String key = KeyUtil.getStringLoc(sign);
		signs_by_loc.put(key, sign);
		int itemid = sign.getItemId();

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
		Set<Integer> itemids = chest.getItemIds();
		/// Trying to activate a chest with nothing in it, abort, everything_id is something and would show up here
		if (itemids == null || itemids.isEmpty()){
			return null;}
		Location cl = chest.getLocation();
		/// Somehow occasionally, 
		for (ShopChest sc : chests_by_loc.values()){
			if (!sc.getWorld().getName().equals(cl.getWorld().getName()))
				continue;
			int distance = (cl.getY() == sc.getY()) ? (int) (Math.abs(cl.getX() - sc.getX()) + Math.abs(cl.getZ() - sc.getZ())): Integer.MAX_VALUE;
			if (distance <= 1){
				return null;}
		}
		String key = KeyUtil.getStringLoc(chest);
		chests_by_loc.put(key, chest);
		HashMap<String,Integer> itemname_count = new HashMap<String, Integer>();

		List<Integer> ids_with_signs = new ArrayList<Integer>();
		for (Integer id : itemids){
			if (!chests_by_itemid.containsKey(id)){
				chests_by_itemid.put(id, new HashMap<String,ShopChest>());
			}
			Map<String,ShopChest> c = chests_by_itemid.get(id);
			c.put(key, chest);
			final String cn = ShopSign.getCommonName(id);
			if (cn.isEmpty()){ /// As of 1.0.0 sometimes this is empty for some reason, figure out why
				System.err.println("Commonname id=" + id  +"  location=" + chest.getLocation());
				continue;
			}
			if (signs_by_itemid.containsKey(id)){
				ids_with_signs.add(id);
				itemname_count.put(cn, signs_by_itemid.get(id).size());
			} else {
				itemname_count.put(cn, 0);
			}
		}
		return itemname_count;
	}


	public Map<Integer,Collection<ShopChest>> getChestsByID(int itemId) {
		Map<Integer,Collection<ShopChest>> chests = new HashMap<Integer, Collection<ShopChest>>();
		if (chests_by_itemid.containsKey(itemId))
			chests.put(itemId,chests_by_itemid.get(itemId).values());		
		if (chests_by_itemid.containsKey(Defaults.EVERYTHING_ID))
			chests.put(Defaults.EVERYTHING_ID,chests_by_itemid.get(Defaults.EVERYTHING_ID).values());
		return chests;
	}
	

	public void removeSign(ShopSign ss) {
		String lockey = KeyUtil.getStringLoc(ss);
		signs_by_loc.remove(lockey);
		int itemid = ss.getItemId();
		Map<String,ShopSign> map = signs_by_itemid.get(itemid);
		if (map != null){
			map.remove(lockey);
			if (map.size() <= 0){
				signs_by_itemid.remove(itemid);}
		}

	}
	
	/**
	 * 
	 * @param chest
	 */
	public void removeDoubleChestFromShop(ShopChest chest) {

		String lockey = KeyUtil.getStringLoc(chest);
		String lockey2 = getNeighborChestLoc(chest);
		if (Defaults.DEBUG_LINKING) System.out.println("Shop::removeDoubleChest  " + chest + "    " + lockey + ":" + lockey2);
		chests_by_loc.remove(lockey);
		if (lockey2 != null) chests_by_loc.remove(lockey2);

		Set<Integer> itemids = chest.getItemIds();

		/// Otherwise remove all these item ids from shop
		for (Integer itemid : itemids){
			Map<String, ShopChest> map = chests_by_itemid.get(itemid);
			if (map != null){
				map.remove(lockey);
				if (lockey2 != null) map.remove(lockey2);
				if (map.size() <= 0){
					chests_by_itemid.remove(itemid);}
			}			
		}
	}

	private static String getNeighborChestLoc(ShopChest lc) {
		Chest neighbor = lc.getNeighborChest();
		String lockey = null;
		if (neighbor != null){
			lockey = KeyUtil.getStringLoc(neighbor);}
		return lockey;
	}

	
	private void updateSigns(int itemid) {
		Map<String,ShopSign> signs = signs_by_itemid.get(itemid);
		if (signs == null || signs.size() == 0)
			return;
		
		Map<String,ShopChest> chests = new HashMap<String,ShopChest>();
		if (chests_by_itemid.containsKey(itemid))
			chests.putAll(chests_by_itemid.get(itemid));
		if (chests_by_itemid.containsKey(Defaults.EVERYTHING_ID))
			chests.putAll(chests_by_itemid.get(Defaults.EVERYTHING_ID));

		if (chests == null || chests.size() == 0){
			for (ShopSign sign: signs.values()){
				sign.setUnlinked();}}
		else{
			ChestSet chestset = new ChestSet(itemid,chests.values());
			
			ItemStack is = ShopSign.getItemStackByShopID(itemid);
			int amount = chestset.amount(is);
			int free = chestset.amountFreeSpace(is);
//			System.out.println("chests=" + is + "   amount=" + amount + "  free= " + free);
			for (ShopSign sign : signs.values()){
				sign.setSignAmount(amount, free);
			}
		}
	}
	
	public void updateSignsByItemId(int itemid) {
		if (itemid == Defaults.EVERYTHING_ID){
			for (Integer id : signs_by_itemid.keySet()){
				if (id != Defaults.EVERYTHING_ID)
					updateSigns(id);
			}
		} else {
			updateSigns(itemid);
		}
	}

	public void updateSignsByItemId(Set<Integer> itemIds) {
		for (Integer id : itemIds){
			updateSignsByItemId(id);
		}
	}
	
	public boolean playerHasPermission(Player player) {
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

	public String toString(){
		StringBuilder sb = new StringBuilder("Shop[owner=" + getOwner().getName());
		for (Map<String,ShopChest> list : chests_by_itemid.values()){
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

}
