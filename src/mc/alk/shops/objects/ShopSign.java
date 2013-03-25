package mc.alk.shops.objects;

import mc.alk.mc.MCBlock;
import mc.alk.mc.MCItemStack;
import mc.alk.mc.MCLocation;
import mc.alk.mc.MCPlayer;
import mc.alk.mc.MCWorld;
import mc.alk.mc.blocks.MCSign;
import mc.alk.shops.Defaults;
import mc.alk.shops.bukkit.controllers.BukkitMessageController;
import mc.alk.shops.controllers.PermController;
import mc.alk.shops.controllers.SignParser;

import org.bukkit.Material;
import org.bukkit.event.block.SignChangeEvent;


/**
 *
 * @author alkarin
 *
 */
public class ShopSign{
	private ShopOwner owner;
    private float buyPrice;
    private float sellPrice;
    MCLocation loc;
    MCItemStack item;
//    HashMap<Integer,Integer> encs;

    private int amount_in_chests;
    private int space_left_in_chests;
    public ShopSign(){}
	public ShopSign(ShopOwner p, MCSign cs, SignValues sv) {
		owner = p;
		setSignValues(sv);
		loc = cs.getLocation();
//		this.encs = sv.enc
	}

	private void setSignValues(SignValues sv) {
		buyPrice = sv.buyPrice;
		sellPrice = sv.sellPrice;
		this.item = sv.itemStack.clone();
	}

	public int getQuantity() {return item.getQuantity();}
	public MCWorld getWorld() {return loc.getWorld();}
	public int getX(){return loc.getBlockX();}
	public int getY(){return loc.getBlockY();}
	public int getZ(){return loc.getBlockZ();}
	public float getSellPrice() {return sellPrice;}
	public boolean isSelling() {return buyPrice > 0;}
	public float getBuyPrice() {return buyPrice;}
	public boolean isBuying() {return sellPrice > 0;}
	public int getItemId() {return item.getType();}
	public int getItemDataValue() {return item.getDataValue();}

	public MCItemStack getItemStack() {return item; }

	public ShopOwner getOwner() {return owner;}

	public boolean isAdminShop() {
		return owner.isAdminShop();
	}

	public boolean validate() throws SignFormatException{
		if ( owner == null)
			throw new SignFormatException("No shop owner!",0);
		else if(!isSelling() && !isBuying())
			throw new SignFormatException("Shop must buy or sell!",3);
		else if(isBuying() && sellPrice <= 0)
			throw new SignFormatException("If you only buy, you must set a buy price!",3);
		else if(isSelling() && buyPrice <= 0)
			throw new SignFormatException("If you only sell, you must set a sell price!", 3);
		else if(isSelling() && buyPrice < Defaults.MIN_BS_PRICE)
			throw new SignFormatException("You must set a sell price >= " + Defaults.MIN_BS_PRICE + "!", 3);
		else if(isBuying() && sellPrice < Defaults.MIN_BS_PRICE)
			throw new SignFormatException("You must set a buy price >= " + Defaults.MIN_BS_PRICE + "!", 3);
		else if(isSelling() && buyPrice > Defaults.MAX_BS_PRICE)
			throw new SignFormatException("You must set a sell price <= " + Defaults.MAX_BS_PRICE + "!", 3);
		else if(isBuying() && sellPrice > Defaults.MAX_BS_PRICE)
			throw new SignFormatException("You must set a buy price <= " + Defaults.MAX_BS_PRICE + "!", 3);
		else if (item.getQuantity() <=0 || item.getQuantity() > Defaults.MAX_SHOPSIGN_QUANTITY)
			throw new SignFormatException("Quantity must be between [1-" + Defaults.MAX_SHOPSIGN_QUANTITY+"]", 2);
		else if (getItemId() < 0)
			throw new SignFormatException("Shop sign must have the item to be sold", 4);
		return true;
	}

	public void setEventValues(SignChangeEvent event) throws SignFormatException {
		event.setLine(0, getFormattedOwnerName());
		event.setLine(1, getFormattedQuantity());
		event.setLine(2, getFormattedBuySell());
		event.setLine(3, getFormattedItem());
	}

	public void setSignValues(MCSign sign) throws SignFormatException {
		sign.setLine(0, getFormattedOwnerName());
		sign.setLine(1, getFormattedQuantity());
		sign.setLine(2, getFormattedBuySell());
		sign.setLine(3, getFormattedItem());
	}

	private String formatPrice(float f){
		if (f==Math.ceil(f)){
			return new Integer((int) f).toString();
		} else {
			String strf = new Float(f).toString();
			if (strf.endsWith("0")){
				strf = strf.substring(0, strf.length()-1);
			}
			return strf;
		}
	}

	private String getFormattedItem() {
		String name = item.getCommonName();
		if (name.length() > Defaults.MAX_NAME_ON_SIGN_LENGTH) return name.substring(0, Defaults.MAX_NAME_ON_SIGN_LENGTH);
		return name;
	}

	private String getFormattedBuySell() throws SignFormatException {
		String strBuyPrice = formatPrice(buyPrice);
		String strSellPrice = formatPrice(sellPrice);
		StringBuffer sb = new StringBuffer();
		if (isSelling() && isBuying()) sb.append("B " + strBuyPrice + " : "  + strSellPrice +" S");
		else if (isSelling()) sb.append("B " + strBuyPrice);
		else if (isBuying()) sb.append("S " + strSellPrice);
		String text = sb.toString();
		int length = text.length();
		if ( length > 15){
			text = text.replaceAll(" ","");
			if ( text.length() > 15){
				throw new SignFormatException("Buy & Sell must be less than 10 chars long", 3);
			}
		}

		return text;
	}

	private String getFormattedQuantity() throws SignFormatException {
		String text = getQuantity() + "";
		if (text.length() > 4) throw new SignFormatException("Quantity must be less than 5 chars long", 2);
		return text;
	}

	private String getFormattedOwnerName() {
		String name = owner.getName();
		if (name.length() > Defaults.MAX_NAME_ON_SIGN_LENGTH) return name.substring(0, Defaults.MAX_NAME_ON_SIGN_LENGTH);
		return name;
	}

	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer("ShopSign[");
		sb.append( owner == null ? "null, " : owner.getName() + ",");
		sb.append( getItemId() + ":"+getItemDataValue()+" ,");
		sb.append( item.getQuantity() + " ,");
		sb.append( buyPrice + " :" + sellPrice + " ]");

		return sb.toString();
	}

	private void setFull(int amount) {
		MCSign sign = getSign();
		if (sign == null) return;

		sign.setLine(1, BukkitMessageController.colorChat(getQuantity() + " : &6Full") );
		sign.update(true);
	}

	private void setEmpty(int quantity, int amount) {
		MCSign sign = getSign();
		if (sign == null) return;
		if (amount == 0){
			sign.setLine(1, BukkitMessageController.colorChat(getQuantity() + " : &8 0") );
		} else {
			sign.setLine(1, BukkitMessageController.colorChat(getQuantity() + " : &8<" + getQuantity() ) );
		}

		sign.update(true);
	}

	private void setAmount(int amount) {
		String amountStr;
		if (amount > Defaults.MAX_SHOPSIGN_REMAINING){
			amountStr = ">" + Defaults.MAX_SHOPSIGN_REMAINING;
		} else {
			amountStr = amount +"";
		}
		final MCSign sign = getSign();
		if (sign == null) return;
		sign.setLine(1, BukkitMessageController.colorChat(getQuantity() + " : &2" + amountStr) );
		sign.update(true);
	}

	public void setUnlinked() {
		amount_in_chests = -1;
		MCSign sign = getSign();
		if (sign == null || isAdminShop()) return;
		sign.setLine(1, BukkitMessageController.colorChat(item.getQuantity() + " : &8 U"));
		sign.update(true);
	}

	private MCSign getSign() {
		MCBlock b = loc.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		int type = b.getType();
		if ( !(type == Material.SIGN.getId() || type == Material.SIGN_POST.getId() ||
				type == Material.WALL_SIGN.getId()))
			return null;
		return (MCSign) b;
	}

	public String getQuantityLine() {
		MCSign sign = getSign();
		if (sign == null) return "";
		return sign.getLine(1);
	}

	public void setSignAmount(int amount, int free) {
		if (isAdminShop())
			return;
		amount_in_chests = amount;
		space_left_in_chests = free;
		if (item.getQuantity() > amount){
			setEmpty(item.getQuantity(),amount);
		} else if (free < item.getQuantity()){
			setFull(amount);
		} else {
			setAmount( amount );
		}
	}

	public boolean isUnlinked(){
		if (isAdminShop()) return false;
		return amount_in_chests == -1;
	}
	public boolean isFull(){
		if (isAdminShop()) return false;
		return space_left_in_chests < item.getQuantity();
	}
	public boolean isEmpty(){
		if (isAdminShop()) return false;
		return item.getQuantity() > amount_in_chests;
	}
	public MCLocation getLocation() {
		return loc;
	}
	public static boolean isShopSignOfPlayer(String[] lines, MCPlayer player, MCSign sign) {
		/// we might have the "iConomyChestShop" format or we have lost the link
		if (!playerNameMatches(player, lines[0], sign) ) {
			if (Defaults.DEBUG_LINKING)  System.out.println("Didnt match name   <" +
						player.getName().toLowerCase()  + "><" + lines[0].toLowerCase() + ">");
			if (PermController.isAdmin(player) && ShopOwner.isAdminShop(lines[0]))
				return true;
			return false;
		}

		try {
			return SignParser.parseShopSign(lines) ==  null ? false : true;
		} catch (SignFormatException e) {
			return false;
		}
	}

	private static boolean playerNameMatches(MCPlayer player, String ownerLine, MCSign sign) {
		if (!PermController.hasAllCreatePermissions(player, sign.getLocation())) return false;
		String name = player.getName();

		String signname = ownerLine;
		int first = signname.indexOf("&");
		if (first != -1){
			signname = signname.substring(0,first);
		}
		if (name.length() > Defaults.MAX_NAME_ON_SIGN_LENGTH){
			name = name.substring(0, Defaults.MAX_NAME_ON_SIGN_LENGTH);
		}
		if (signname.length() > Defaults.MAX_NAME_ON_SIGN_LENGTH){
			signname = signname.substring(0, Defaults.MAX_NAME_ON_SIGN_LENGTH);
		}

		return name.compareToIgnoreCase(signname) == 0;
	}

	public String getCommonName() {
		return item.getCommonName();
	}
}
