package mc.alk.shops.utils;

import mc.alk.mc.MCInventory;
import mc.alk.mc.MCItemStack;
import mc.alk.mc.MCPlayer;

public abstract class InventoryUtil{
	static InventoryUtil util;

	public abstract void addMCItem(MCPlayer player, MCItemStack itemStack);
	public abstract void removeMCItem(MCInventory inventory, MCItemStack itemStack);
	public abstract String getMCCommonName(MCItemStack itemStack);
	public abstract MCItemStack parseMCItemStack(String text);
	public abstract MCItemStack parseMCItemStack(int id, short datavalue);

	public abstract int getMCItemAmount(MCInventory inventory, MCItemStack itemStack);

	public static void addItem(MCPlayer player, MCItemStack itemStack) {
		util.addMCItem(player, itemStack);
	}

	public static MCItemStack parseItemStack(String text) {
		return util.parseMCItemStack(text);
	}

	public static void setInventoryUtil(InventoryUtil inventoryUtil) {
		util = inventoryUtil;
	}
	public static MCItemStack parseItemStack(int id, short datavalue) {
		return util.parseMCItemStack(id,datavalue);
	}

}
