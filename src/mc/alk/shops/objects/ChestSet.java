package mc.alk.shops.objects;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import mc.alk.mc.MCItemStack;
import mc.alk.shops.Defaults;


/**
 * A class that represents a collection of chests that are "linked" together
 * @author alkarin
 *
 */
public class ChestSet{
	Collection<ShopChest> chests =null;
	Collection<ShopChest> everything_chests =null;

	public ChestSet(Map<Long, Collection<ShopChest>> chests) {
		for (Long id : chests.keySet()){ /// There should only be two ids at maximum in here
			if (id == Defaults.EVERYTHING_ID){
				everything_chests = chests.get(id);
			} else {
				this.chests = chests.get(id);
			}
		}
	}

	public ChestSet(long itemid, Collection<ShopChest> chests) {
		if (itemid != Defaults.EVERYTHING_ID){
			this.chests = chests;
		} else {
			everything_chests = chests;
		}
	}

	public boolean hasEnough(MCItemStack itemStack, int stockAmount) {
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

	public int freeSpaceAfter(MCItemStack itemStack){
		int free = 0;
		if (chests != null){
			for (ShopChest chest : chests){
				free += chest.freeSpaceAfter(itemStack);
//				System.out.println("free=" + chest.amountFreeSpace(itemStack, 0));
			}
		}
		if (everything_chests != null){
			for (ShopChest chest : everything_chests){
				free += chest.freeSpaceAfter(itemStack);}
		}
		return free;
	}

	public boolean fits(MCItemStack itemStack, int stockAmount) {
		return freeSpaceAfter(itemStack) >= 0;
	}

	public Integer addItem(MCItemStack itemStack, int stockAmount,Set<ShopChest> affectedChests) {
		int amount_left = stockAmount;
		if (chests != null){
			amount_left = addItem(chests, itemStack, stockAmount, affectedChests);}
		if (amount_left >0 && everything_chests != null){
			addItem(everything_chests,itemStack,stockAmount,affectedChests);}
		return null;
	}

	public static int addItem(Collection<ShopChest> chests, MCItemStack itemStack, int quantity, Set<ShopChest> affectedChests){
		if (Defaults.DEBUG_TRACE) System.out.println("ChestSet::addItem");
		MCItemStack item = itemStack.clone();
		item.setQuantity(0);
		for (ShopChest chest : chests){
			int free = chest.freeSpaceAfter(item);
			if (free > 0){
				if (free >= quantity){
					MCItemStack add = itemStack.clone();
					add.setQuantity(quantity);
					affectedChests.add(chest);
					chest.addItem(add);
					return 0;
				} else if (free > 0) {
					MCItemStack add = itemStack.clone();
					add.setQuantity(free);
					affectedChests.add(chest);
					chest.addItem(add);
					quantity -= free;
				}
			}
		}
		return quantity;
	}

	public Set<Integer> removeItem(MCItemStack itemStack, int quantity, Set<ShopChest> affectedChests) {
		if (Defaults.DEBUG_TRANSACTION)  System.out.println("ChestSet::removeItem   is=" + itemStack + "     quantity=" + quantity +
				" chests=" + chests + "  everything_chests=" + everything_chests);
		int amount_left = quantity;

		if (chests != null){
			amount_left = removeItem(chests, itemStack, quantity, affectedChests);}
		if (amount_left >0 && everything_chests != null)
			removeItem(everything_chests,itemStack,quantity, affectedChests);
		return null;
	}

	public static int removeItem(Collection<ShopChest> chests, MCItemStack oitemStack,
			int quantity, Set<ShopChest> affectedChests) {
		MCItemStack itemStack = oitemStack.clone();
		for (ShopChest chest : chests){
			int free = chest.amount(itemStack);
			if (Defaults.DEBUG_TRANSACTION)
				System.out.println(" quantityInChest=" + quantity + " quantity=" + itemStack.getQuantity());
			if (free > 0){
				if (free >= quantity){
					affectedChests.add(chest);
					chest.removeItem(itemStack);
					return 0;
				} else if (free > 0) {
					affectedChests.add(chest);
					chest.removeItem(itemStack);
					quantity -= free;
				}
			}
		}

		return quantity;
	}

	public int amount(MCItemStack itemStack) {
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

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		if (chests != null){
			sb.append("---------------- chests\n");
			for (ShopChest chest : chests){
				MCItemStack[] inv = chest.getInventory().getContents();
				for (int i =0; i< inv.length;i++){
					if (inv[i] != null){
						sb.append(i + "  " + inv[i].getType() + "  " + inv[i].getQuantity() + "   " + inv[i].getDataValue() + "\n");
					} else {
						sb.append(i + " null\n");
					}

				}
			}
		}
		if (everything_chests != null){
			sb.append("---------------- everything_chests\n");
			for (ShopChest chest : everything_chests){
				MCItemStack[] inv = chest.getInventory().getContents();
				for (int i =0; i< inv.length;i++){
					if (inv[i] != null){
						sb.append(i + "  " + inv[i].getType() + "  " + inv[i].getQuantity() + "   " + inv[i].getDataValue() + "\n");
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
