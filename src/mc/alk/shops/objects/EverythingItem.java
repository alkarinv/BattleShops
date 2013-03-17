package mc.alk.shops.objects;

import java.util.Map;

import mc.alk.mc.MCItemStack;
import mc.alk.shops.Defaults;

public final class EverythingItem implements MCItemStack{
	public static final EverythingItem EVERYTHING_ITEM = new EverythingItem();
	private EverythingItem(){};

	@Override
	public void setType(int id) {/* do nothing */ }

	@Override
	public int getType() {
		return Integer.MAX_VALUE;
	}

	@Override
	public void setDataValue(short value) { /* do nothing */ }

	@Override
	public short getDataValue() { return -1;}

	@Override
	public void setQuantity(int quantity) {/* do nothing */  }

	@Override
	public int getQuantity() {return 1; }

	@Override
	public Map<Integer, Integer> getEnchantments() { return null;}

	@Override
	public void addEnchantment(int id, int level) {/* do nothing */ }

	@Override
	public boolean hasMetaData() { return false; }

	@Override
	public String getCommonName() { return Defaults.EVERYTHING_NAME; }

	@Override
	public int isSpecial() { return 0; }

	@Override
	public MCItemStack clone(){ return this; }

	public static boolean isEverythingID(int itemid, short datavalue) {
		return itemid == EVERYTHING_ITEM.getType() && datavalue == EVERYTHING_ITEM.getDataValue();
	}

	public static boolean isEverythingItem(MCItemStack item) {
		return item == EVERYTHING_ITEM;
	}

	public static boolean isEverythingID(long id) {
		return id == Defaults.EVERYTHING_ID;
	}

	@Override
	public String toString(){
		return "[EverythingItem]";
	}
}
