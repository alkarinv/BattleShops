package mc.alk.shops.objects;

import java.io.Serializable;

import mc.alk.mc.MCPlayer;
import mc.alk.shops.Defaults;


/**
 *
 * @author alkarin
 *
 */
public class ShopOwner implements Serializable{
	private static final long serialVersionUID = 1L;
	final String name;

	public ShopOwner(String player){
		this.name = player;
		if (this.name==null || this.name.isEmpty())
			throw new IllegalStateException();
	}
	public ShopOwner(MCPlayer player){
		this.name = player.getName();
	}

	public String getName() {return name;}
//	public void setName(String name) {this.name = name;}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (obj.getClass() != getClass()) return false;

		ShopOwner owner = (ShopOwner) obj;
		return getName().compareTo(owner.getName()) == 0;
	}

	@Override
	public int hashCode(){return name.hashCode();}

	public static boolean sameOwner(ShopOwner owner1, ShopOwner owner2) {return owner1.equals(owner2);}
	public boolean sameOwner(String name) {
		return getName().equalsIgnoreCase(name);
	}

	public boolean isAdminShop() {return ShopOwner.isAdminShop(name);}
	public static boolean isAdminShop(String name){
		name = name.replaceAll("_", "").toUpperCase();
		return name.equals("ADMIN") ||
				name.equals(Defaults.ADMIN_NAME) || name.equals(Defaults.ADMIN_NAME_NO_SPACES);
	}

	public String getKey() {return ShopOwner.getShopOwnerKey(this);}
	public static String getShopOwnerKey(ShopOwner p){return p.getName();}

	@Override
	public String toString(){return name;}

}
