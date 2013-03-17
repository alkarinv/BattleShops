package mc.alk.shops.serializers;

import java.util.Collection;
import java.util.List;

import mc.alk.mc.blocks.MCChest;
import mc.alk.shops.objects.ShopChest;
import mc.alk.shops.objects.ShopSign;
import mc.alk.shops.objects.Transaction;


public interface ShopsSerializer {
	public void saveAll();
	public void loadAll();
	public void deleteShopChest(ShopChest lc);
	public void deleteShopSign(ShopSign ss);
	public void deleteShopChest(MCChest chest);

	public void deleteAssociate(String p1, String p2);
	public void saveTransactions(Collection<Transaction> trs);
	public List<Transaction> getPlayerTransactions(String name, Integer ndays);
	public List<Transaction> getShopTransactions(String name, Integer ndays);
}
