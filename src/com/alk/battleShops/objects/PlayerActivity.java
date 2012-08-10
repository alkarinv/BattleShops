package com.alk.battleShops.objects;

public class PlayerActivity implements Comparable<PlayerActivity>{
	public String name;
	public long lastShopTransaction;
	public long lastPlayerLogin;
	public long lastShopUpdate;
	public PlayerActivity(){}
	public PlayerActivity(String name) {
		this.name = name;
	}

	public void shopTransaction() {
		lastShopTransaction = System.currentTimeMillis();
	}

	public void playerEntered() {
		lastPlayerLogin = System.currentTimeMillis();
	}

	public void playerUpdatedShop() {
		lastShopUpdate = System.currentTimeMillis();
	}
	public int compareTo(PlayerActivity arg0) {
		if (arg0.lastShopTransaction == 0) return 1;
		return new Long(lastShopTransaction).compareTo(arg0.lastShopTransaction);
	}
}
