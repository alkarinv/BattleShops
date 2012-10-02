package com.alk.battleShops.objects;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.alk.battleShops.BattleShops;
import com.alk.battleShops.Defaults;
import com.alk.battleShops.util.InventoryUtil;
import com.alk.battleShops.util.KeyUtil;
/**
 * 
 * @author alkarin
 *
 */
public class ShopChest{
	private ShopOwner owner;
	Set<Integer> itemids;
	Chest mc;

	public ShopChest(Chest chest, ShopOwner owner){
		mc = chest;
		this.owner = owner;
		itemids = new HashSet<Integer>();

		ItemStack[] stacks = getContents();
		for (int i=0;i< stacks.length;i++){
//			System.out.println(i + "   "  + stacks[i]);
			if (stacks[i] != null){
				int id = ShopSign.getShopItemID(stacks[i]);
				itemids.add(id);
			}
		}
		if (itemids.size() == 0){
//			System.out.println("got nothing");
			itemids.add(Defaults.EVERYTHING_ID);
		}
	}
	
	public ShopChest(Chest chest, ShopOwner owner, Set<Integer> itemids) {
		this.mc = chest;
		this.owner = owner;
		this.itemids = itemids;
	}
	
	public ItemStack[] getContents(){
		Chest mc = null;
		try {
			mc = (Chest) this.mc.getBlock().getState();
		} catch (Exception e){
			if (Defaults.ERROR_LVL > 1) System.err.println("[BattleShops_v" +BattleShops.getVersion() +
					"] Chest is no longer a chest at "+ getX() +":" +getY() + ":" + getZ() + "  id=" + this.mc.getBlock().getTypeId());
			return new ItemStack[0];
		}
		Inventory inv = mc.getInventory();
		return inv.getContents();
//		Chest mmc = getNeighborChest();
		
//		if (mmc == null){
//
//			return ((Chest)mc.getBlock().getState()).getInventory().getContents();
//		} else { /// Need to deal with merging the contents of a doublechest
//			ItemStack[] return_stack;
////			return 
//			ItemStack[] contents = mc.getInventory().getContents();
//			ItemStack[] contents2 = mmc.getInventory().getContents();
////			final int size1 = mc.getInventory().getSize();
//			final int size1 = ((Chest)mc.getBlock().getState()).getInventory().getSize();
//			final int size2 = mmc.getInventory().getSize();
//			
//			return_stack = new ItemStack[size1 + size2];
//
//			/// The south west rule :P
//	        if (isFirst(mc,mmc)) {
//	        	for (int i=0;i< contents.length;i++){ return_stack[i] = contents[i];}
//	        	for (int i=0;i< contents2.length;i++){ return_stack[i + size1] = contents2[i];}
//	        } else {
//	        	for (int i=0;i< contents2.length;i++){ return_stack[i] = contents2[i];}
//	        	for (int i=0;i< contents.length;i++){ return_stack[i + size2] = contents[i];}
//	        }
//	        return return_stack;
//		} 
	}

//	/**
//	 * Figure out which chests inventory comes first in a double chest situation
//	 * @param c1
//	 * @param c2
//	 * @return true if c1 comes first, false if c2 comes first
//	 */
//	private boolean isFirst(final Chest c1, final Chest c2) {return (c1.getX() + c1.getZ()) < (c2.getX() + c2.getZ());}

	public Chest getChest(){ 
		Chest mc = (Chest) this.mc.getBlock().getState();
		return mc;}
	public ShopOwner getOwner() {return owner;}
	public Set<Integer> getItemIds() {return itemids;}
	public World getWorld() {return mc.getWorld();}
	public Location getLocation() {return mc.getBlock().getLocation();}
	public int getX() {return mc.getX();}
	public int getY() {return mc.getY();}
	public int getZ() {return mc.getZ();}
	
	public int amountFreeSpace(ItemStack itemStack, int amount) {
		return InventoryUtil.amountFreeSpace(getContents(), itemStack, amount);
	}
	public void addItem(ItemStack itemStack, int quantity) {
		Chest mc = (Chest) this.mc.getBlock().getState();
//		Chest mmc = getNeighborChest();
//		if (Defaults.DEBUG_TRANSACTION) System.out.println("ShopChest::addItem,itemStack=" + InventoryUtil.printItemStack(itemStack) +
//				",quantity=" + quantity + ",neighborChest=" + mmc);

		int maxStack = itemStack.getType().getMaxStackSize();
		ItemStack items[];
		/// Deal with differing maxStack sizes.. like goldenApples
		if (maxStack != -1){
			int remaining = quantity;
			/// 130/64 = 64,64 and 6
			float unit = Math.min(maxStack, remaining);
			int nItemStacks = (int) Math.ceil( ((float)quantity)/unit);

			items = new ItemStack[nItemStacks];
			
			for (int i =0;i< items.length;i++){
				items[i] = itemStack.clone();
				if (remaining >= unit){
					items[i].setAmount((int) unit);
					remaining -= unit;
				} else {
					items[i].setAmount(remaining);
				}
//				System.out.println(i + "    has " + InventoryUtil.printItemStack(items[i]));
			}
		} else {
			items = new ItemStack[1];
			items[0] = itemStack.clone();
		}
		
		
//		if (mmc == null){
			mc.getInventory().addItem(items);
//		} else {  /// The more complicated doublechest situation
//	        if (isFirst(mc,mmc)) {
//	        	HashMap<Integer, ItemStack> leftover = mc.getInventory().addItem(items);
//	        	if (leftover != null && leftover.size() > 0){
//	        		for (Integer key : leftover.keySet()){
//	        			mmc.getInventory().addItem(leftover.get(key));
//	        		}}
//	        } else {
//	        	HashMap<Integer, ItemStack> leftover = mmc.getInventory().addItem(items);
//	        	if (leftover != null && leftover.size() > 0){
//	        		for (Integer key : leftover.keySet()){
//	        			mc.getInventory().addItem(leftover.get(key));
//	        		}}
//	        }			
//		} 
	}
	public int amount(ItemStack itemStack) {
		return InventoryUtil.getItemAmount(getContents(), itemStack);
	}
	public void removeItem(ItemStack itemStack, int quantity) {
		Chest mc = (Chest) this.mc.getBlock().getState();
		Inventory inv = mc.getInventory();
		itemStack.setAmount(quantity);
		inv.removeItem(itemStack);
//
//		Chest mmc = getNeighborChest();
//		if (Defaults.DEBUG_TRANSACTION) System.out.println("ShopChest::removeItem,itemStack=" + InventoryUtil.printItemStack(itemStack) +
//				",quantity=" + quantity + ",neighborChest=" + mmc);
//
//		ItemStack is = itemStack.clone();
//		is.setAmount(quantity);
//
//		
//		if (mmc == null){
////			mc.getInventory().removeItem(is);
//        	InventoryUtil.removeItem(mc.getInventory(), is);
//		} else {  /// The more complicated doublechest situation
//	        if (isFirst(mc,mmc)) {
////	        	HashMap<Integer, ItemStack> leftover = mc.getInventory().removeItem(is);
//	        	HashMap<Integer, ItemStack> leftover = InventoryUtil.removeItem(mc.getInventory(), is);
//	        	if (leftover != null && leftover.size() > 0){
//	        		for (Integer key : leftover.keySet()){
//	    	        	InventoryUtil.removeItem(mmc.getInventory(), leftover.get(key));
//	        			//mmc.getInventory().removeItem(leftover.get(key));
//	        			}}
//	        } else {
//	        	HashMap<Integer, ItemStack> leftover = InventoryUtil.removeItem(mmc.getInventory(), is);
//	        	mmc.update(true);
////	        	HashMap<Integer, ItemStack> leftover = mmc.getInventory().removeItem(is);
//	        	if (leftover != null && leftover.size() > 0){
//	        		for (Integer key : leftover.keySet()){
////	        			mc.getInventory().removeItem(leftover.get(key));
//	    	        	InventoryUtil.removeItem(mc.getInventory(), leftover.get(key));
//
//	        			}}
//	        }			
//		}
		update();
	}

	public boolean isDoubleChest() {
		return getChest().getInventory() instanceof DoubleChestInventory;
//		return getNeighborChest(mc) != null;
	}	
	public Chest getNeighborChest() {return getNeighborChest(mc);}

	public static Chest getNeighborChest(final Chest chest){
		return getNeighborChest(chest.getLocation());
	}

	public static Chest getNeighborChest(final Location loc){
		final Block b = loc.getBlock();
		if (b.getRelative(BlockFace.NORTH).getType() == Material.CHEST)
			return (Chest) b.getRelative(BlockFace.NORTH).getState();
		else if (b.getRelative(BlockFace.SOUTH).getType() == Material.CHEST)
			return (Chest) b.getRelative(BlockFace.SOUTH).getState();
		else if (b.getRelative(BlockFace.EAST).getType() == Material.CHEST)
			return (Chest) b.getRelative(BlockFace.EAST).getState();
		else if (b.getRelative(BlockFace.WEST).getType() == Material.CHEST)
			return (Chest) b.getRelative(BlockFace.WEST).getState();
		return null;
	}

	public void update(){
		Chest mc = (Chest) this.mc.getBlock().getState();

		Location loc = mc.getBlock().getLocation();
		Server server = BattleShops.getBukkitServer();
		World world = server.getWorld(loc.getWorld().getName());
		Block b = world.getBlockAt(loc);
//		Chest chest = new Chest(b);
		Chest chest = (Chest) b.getState();
		chest.update(true);
		
		mc.update(true);
//		Chest mmc = getNeighborChest();
//		if (mmc != null){
////			b = world.getBlockAt(mmc.getLocation());
////			chest = new Chest(b);
//			chest = (Chest) mmc.getBlock().getState();
//			chest.update(true);
//			mmc.update(true);
//		}
	}

	public String toString(){
		StringBuffer sb = new StringBuffer("MyChest[");
		sb.append(  ((owner == null) ? "null" : owner.getName()) + " ,"   );
		sb.append(  isDoubleChest()   + ",");
		sb.append(  KeyUtil.getStringLoc(this.mc)   + ",");
//		sb.append(  (isDoubleChest() ? KeyUtil.getStringLoc(getNeighborChest(mc)) : " ")  + " ,");
		sb.append(  itemids);
		sb.append("]");
		return sb.toString();
	}

}
