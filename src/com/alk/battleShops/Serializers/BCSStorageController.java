package com.alk.battleShops.Serializers;

import java.util.Collection;
import java.util.List;

import org.bukkit.block.Chest;

import com.alk.battleShops.objects.ShopChest;
import com.alk.battleShops.objects.ShopSign;
import com.alk.battleShops.objects.Transaction;

public interface BCSStorageController {
	public void saveAll();
	public void loadAll();
	public void deleteShopChest(ShopChest lc);
	public void deleteShopSign(ShopSign ss);
	public void deleteAssociate(String name, String string);
	public void saveTransactions(Collection<Transaction> trs);
	public List<Transaction> getPlayerTransactions(String name, Integer ndays);
	public List<Transaction> getShopTransactions(String name, Integer ndays);
	public void deleteChest(Chest neighbor);
}
