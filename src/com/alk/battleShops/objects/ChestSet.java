package com.alk.battleShops.objects;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.bukkit.inventory.ItemStack;

import com.alk.battleShops.Defaults;
import com.alk.battleShops.util.Pair;

/**
 * A class that represents a collection of chests that are "linked" together
 * @author alkarin
 *
 */
public class ChestSet{
	Collection<ShopChest> chests =null;
	Collection<ShopChest> everything_chests =null;

	public ChestSet(Map<Integer, Collection<ShopChest>> chests) {		
		for (Integer id : chests.keySet()){ /// There should only be two ids at maximum in here
			if (id == Defaults.EVERYTHING_ID){
				everything_chests = chests.get(id);		
			} else {
				this.chests = chests.get(id);
			}
		}
	}

	public ChestSet(int itemid, Collection<ShopChest> chests) {
		if (itemid != Defaults.EVERYTHING_ID){
			this.chests = chests;
		} else {
			everything_chests = chests;
		}
	}

	public boolean hasEnough(ItemStack itemStack, int stockAmount) {
		int quantity = 0;
		if (chests != null){
			for (ShopChest chest : chests){
				quantity += chest.amount(itemStack);}			
		}

		if (everything_chests != null){
			for (ShopChest chest : everything_chests){
				quantity += chest.amount(itemStack);}			
		}

		return quantity >= stockAmount;
	}

	public int amountFreeSpace(ItemStack itemStack){
		int free = 0;
		if (chests != null){
			for (ShopChest chest : chests){
				free += chest.amountFreeSpace(itemStack, 0);
//				System.out.println("free=" + chest.amountFreeSpace(itemStack, 0));
			}			
		}
		if (everything_chests != null){
			for (ShopChest chest : everything_chests){
				free += chest.amountFreeSpace(itemStack, 0);}			
		}
		return free;
	}

	public boolean fits(ItemStack itemStack, int stockAmount) {
		return amountFreeSpace(itemStack) >= stockAmount;
	}

	public Pair<Integer,Integer> addItem(ItemStack itemStack, int stockAmount,Set<ShopChest> affectedChests) {
		int amount_left = stockAmount;
		if (chests != null){
			amount_left = addItem(chests, itemStack, stockAmount,affectedChests);}
		if (amount_left >0 && everything_chests != null){
			addItem(everything_chests,itemStack,amount_left,affectedChests);}
		return null;
	}

	public static int addItem(Collection<ShopChest> chests, ItemStack itemStack, int quantity,Set<ShopChest> affectedChests){
		if (Defaults.DEBUG_TRACE) System.out.println("ChestSet::addItem");
		for (ShopChest chest : chests){
			int free = chest.amountFreeSpace(itemStack, 0);
			if (free > 0){
				if (free >= quantity){
					affectedChests.add(chest);
					chest.addItem(itemStack, quantity);
					return 0;
				} else if (free > 0) {
					affectedChests.add(chest);
					chest.addItem(itemStack, free);
					quantity -= free;
				}
			}
		}
		return quantity;
	}

	public Set<Integer> removeItem(ItemStack itemStack, int quantity, Set<ShopChest> affectedChests) {
		if (Defaults.DEBUG_TRANSACTION)  System.out.println("ChestSet::removeItem   is=" + itemStack + "     quantity=" + quantity + 
				" chests=" + chests + "  everything_chests=" + everything_chests);
		int amount_left = quantity;
		if (chests != null){
			amount_left = removeItem(chests, itemStack, quantity,affectedChests);}
		if (amount_left >0 && everything_chests != null)
			removeItem(everything_chests,itemStack,amount_left,affectedChests);
		return null;
	}

	public static int removeItem(Collection<ShopChest> chests, ItemStack itemStack, int quantity, Set<ShopChest> affectedChests) {	
		for (ShopChest chest : chests){
			int quantityInChest = chest.amount(itemStack);
			if (Defaults.DEBUG_TRANSACTION) System.out.println(" quantityInChest=" + quantityInChest + " quantity=" + quantity);
			if (quantityInChest > quantity){
				affectedChests.add(chest);
				chest.removeItem(itemStack, quantity);
				return 0;
			} else if (quantityInChest >0){
				affectedChests.add(chest);				
				chest.removeItem(itemStack, quantityInChest);
				quantity -= quantityInChest;
			}
		}
		return quantity;
	}

	public int amount(ItemStack itemStack) {
		int quantity = 0;
		if (chests != null){
			for (ShopChest chest : chests){
				quantity += chest.amount(itemStack);}			
		}

		if (everything_chests != null){
			for (ShopChest chest : everything_chests){
				quantity += chest.amount(itemStack);}			
		}

		return quantity;		
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		if (chests != null){
			sb.append("---------------- chests\n");
			for (ShopChest chest : chests){
				ItemStack[] inv = chest.getContents();
				for (int i =0; i< inv.length;i++){
					if (inv[i] != null){
						sb.append(i + "  " + inv[i].getTypeId() + "  " + inv[i].getAmount() + "   " + inv[i].getDurability() + "\n");	
					} else {
						sb.append(i + " null\n");
					}

				}
			}			
		}
		if (everything_chests != null){
			sb.append("---------------- everything_chests\n");
			for (ShopChest chest : everything_chests){
				ItemStack[] inv = chest.getContents();
				for (int i =0; i< inv.length;i++){
					if (inv[i] != null){
						sb.append(i + "  " + inv[i].getTypeId() + "  " + inv[i].getAmount() + "   " + inv[i].getDurability() + "\n");	
					} else {
						sb.append(i + " null\n");
					}
				}
			}			
		}
		return sb.toString();
	}

	public boolean isEmpty() {
		boolean hasItems1 = (chests != null && !chests.isEmpty());
		boolean hasItems2 = (everything_chests != null && !everything_chests.isEmpty());
		return !(hasItems1 || hasItems2);
	}

	public int size() {
		int count = 0;
		if (chests!=null) count += chests.size();
		if (everything_chests!=null) count += everything_chests.size();
		return count;
	}

	public ShopChest getFirst() {
		if (chests != null && chests.size() >0){
			for (ShopChest c : chests){
				return c;
			}
		}
		if (everything_chests != null && everything_chests.size() > 0){
			for (ShopChest c : chests){
				return c;
			}
		}
		return null;
	}
}
