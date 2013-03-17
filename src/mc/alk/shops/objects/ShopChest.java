package mc.alk.shops.objects;

import java.util.ArrayList;
import java.util.List;

import mc.alk.mc.MCBlock;
import mc.alk.mc.MCInventory;
import mc.alk.mc.MCItemStack;
import mc.alk.mc.MCLocation;
import mc.alk.mc.MCWorld;
import mc.alk.mc.blocks.MCChest;
import mc.alk.shops.utils.KeyUtil;


/**
 *
 * @author alkarin
 *
 */
public class ShopChest {
	private ShopOwner owner;
	List<MCItemStack> items = new ArrayList<MCItemStack>();
	MCLocation loc;

	public ShopChest(MCChest chest, ShopOwner owner){
		this.loc = chest.getLocation();
		this.owner = owner;
		if (chest.getItems() != null){
			this.items = new ArrayList<MCItemStack>();
			for (MCItemStack item: chest.getItems()){
				if (item.getType() == 0)
					continue;
				this.items.add(item);
			}
		}

		//		ItemStack[] stacks = getContents();
		//		for (int i=0;i< stacks.length;i++){
		////			System.out.println(i + "   "  + stacks[i]);
		//			if (stacks[i] != null){
		//				int id = ShopSign.getShopItemID(stacks[i]);
		//				itemids.add(id);
		//			}
		//		}
		if (items.size() == 0){
			this.items.add(EverythingItem.EVERYTHING_ITEM);
		}
	}

	public ShopChest(MCChest chest, ShopOwner owner, List<MCItemStack> items) {
		this.loc = chest.getLocation();
		this.owner = owner;
		this.items = items;
//		if (items.size() == 0){
//			everything = true;}
	}

//	public MCItemStack[] getContents() throws IllegalStateException{
//		MCChest chest = getChest();
//		return chest.getItems();
//	}

	public MCChest getChest() throws IllegalStateException{
		MCWorld w = loc.getWorld();
		MCBlock block = w.getBlockAt(loc);
		if (w.isType(block, MCChest.class)){
			return (MCChest) w.toType(block, MCChest.class);
		}
		throw new IllegalStateException("ShopChest at " + loc +" is no longer a chest " + block.getType());
		//		MCChest chest = findNeighborChest(block);
		//		if (chest != null)
		//			return chest;
	}


	public ShopOwner getOwner() {return owner;}
	public MCWorld getWorld() {return loc.getWorld();}
	//	public Collection<Integer> getItemIds() {return itemids;}
	public MCLocation getLocation() {return loc;}
	public int getX() {return loc.getBlockX();}
	public int getY() {return loc.getBlockY();}
	public int getZ() {return loc.getBlockZ();}


	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer("MyChest[");
		sb.append(  ((owner == null) ? "null" : owner.getName()) + " ,"   );
		//		sb.append(  isDoubleChest()   + ",");
		sb.append(  KeyUtil.getStringLoc(this.loc)   + ",");
		//		sb.append(  (isDoubleChest() ? KeyUtil.getStringLoc(getNeighborChest(mc)) : " ")  + " ,");
		sb.append(  items);
		sb.append("]");
		return sb.toString();
	}

	//	public List<MCItemStack> getItems() {
	//		return items;
	//	}

	public List<MCItemStack> getItemIds() {
		return items;
	}

	public int amount(MCItemStack itemStack) throws IllegalStateException {
		MCChest chest = getChest();
		return chest.getInventory().getItemAmount(itemStack);
	}

	public void removeItem(MCItemStack itemStack) throws IllegalStateException {
		MCChest chest = getChest();
		MCItemStack item = itemStack.clone();
		chest.getInventory().removeItem(item);
	}

	public int freeSpaceAfter(MCItemStack itemStack) throws IllegalStateException{
		MCChest chest = getChest();
		return chest.getInventory().freeSpaceAfter(itemStack);
	}

	public void addItem(MCItemStack itemStack) throws IllegalStateException{
		MCChest chest = getChest();
		MCItemStack item = itemStack.clone();
		chest.getInventory().addItem(item);
	}

	public void addItemID(MCItemStack itemStack){
		MCItemStack item = itemStack.clone();
		items.add(item);
//		everything = false;
	}
	//	private Set<Long> getItemIds() {
	//		Set<Long> ids = new HashSet<Long>();
	//		for (MCItemStack item: items){
	//			ids.add(KeyUtil.toKey(item));
	//		}
	//		return ids;
	//	}

//	public boolean isEverythingChest() {
//		return everything;
//	}

	public MCInventory getInventory() {
		return getChest().getInventory();
	}

}
