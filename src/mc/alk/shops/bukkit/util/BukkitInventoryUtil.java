package mc.alk.shops.bukkit.util;

import mc.alk.bukkit.BukkitInventory;
import mc.alk.bukkit.BukkitItemStack;
import mc.alk.bukkit.BukkitPlayer;
import mc.alk.mc.MCInventory;
import mc.alk.mc.MCItemStack;
import mc.alk.mc.MCPlayer;
import mc.alk.shops.utils.InventoryUtil;

import org.bukkit.inventory.ItemStack;

public class BukkitInventoryUtil extends InventoryUtil {

	@Override
	public void removeMCItem(MCInventory inventory, MCItemStack itemStack) {
		mc.alk.bukkit.util.BukkitInventoryUtil.removeItem(
				((BukkitInventory)inventory).getInventory(), ((BukkitItemStack)itemStack).getItem());
	}

	@Override
	public String getMCCommonName(MCItemStack itemStack) {
		return itemStack.getCommonName();
	}

	@Override
	public MCItemStack parseMCItemStack(String text) {
		ItemStack item = mc.alk.bukkit.util.BukkitInventoryUtil.getItemStack(text);
		return item == null ? null : new BukkitItemStack(item);
	}

	@Override
	public MCItemStack parseMCItemStack(int id, short datavalue) {
		ItemStack item = mc.alk.bukkit.util.BukkitInventoryUtil.getItemStack(id,datavalue);
		return item == null ? null : new BukkitItemStack(item);
	}

	@Override
	public int getMCItemAmount(MCInventory inventory, MCItemStack itemStack) {
		return mc.alk.bukkit.util.BukkitInventoryUtil.getItemAmountFromInventory(
				((BukkitInventory)inventory).getInventory(), ((BukkitItemStack)itemStack).getItem());
	}

	@SuppressWarnings("deprecation")
	@Override
	public void addMCItem(MCPlayer player, MCItemStack itemStack) {
		if (itemStack == null || itemStack.getType() == 0)
			return;
		mc.alk.bukkit.util.BukkitInventoryUtil.addItemToInventory(
				((BukkitPlayer)player).getPlayer(),
				((BukkitItemStack)itemStack).getItem()
				,itemStack.getQuantity());
		((BukkitPlayer)player).getPlayer().updateInventory();
	}


}
