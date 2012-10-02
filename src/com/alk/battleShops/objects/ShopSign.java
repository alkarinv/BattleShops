package com.alk.battleShops.objects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.alk.battleShops.Defaults;
import com.alk.battleShops.Exceptions.SignFormatException;
import com.alk.battleShops.controllers.PermissionController;
import com.alk.battleShops.util.InventoryUtil;
import com.alk.battleShops.util.Util;
/**
 * 
 * @author alkarin
 *
 */
public class ShopSign{
	private ShopOwner owner;
    private int itemid;
//    private int datavalue = -1;
    private int quantity;
    private float buyPrice;
    private float sellPrice;
    private int x,y,z;
    private World world;
    static final int ITEMM = 100000;
    
    private int amount_in_chests;
    private int space_left_in_chests;
    public ShopSign(){}
	public ShopSign(ShopOwner p, Sign cs, SignValues sv) {
		owner = p;
		setSignValues(sv);
		world = cs.getWorld();
		x = cs.getX(); y = cs.getY(); z = cs.getZ();
	}

	private void setSignValues(SignValues sv) {
		buyPrice = sv.buyPrice;
		sellPrice = sv.sellPrice;
		quantity = sv.quantity;
		itemid = sv.itemStack.getTypeId();
//		System.out.println("itemids=" + itemid + "   dura=" + sv.itemStack.getDurability());

		if (sv.itemStack.getDurability() > 0){
			itemid += ITEMM * sv.itemStack.getDurability();
		}
	}

	public int getQuantity() {return quantity;}
	public World getWorld() {return world;}
	public int getX(){return x;}
	public int getY(){return y;}
	public int getZ(){return z;}
	public float getSellPrice() {return sellPrice;}
	public boolean isSelling() {return buyPrice > 0;}
	public float getBuyPrice() {return buyPrice;}
	public boolean isBuying() {return sellPrice > 0;}
	public int getItemId() {return itemid;}
	
	public static int getShopItemID(ItemStack itemStack) {
		int id =itemStack.getTypeId();
		int data = itemStack.getData() != null ?itemStack.getData().getData() *ITEMM : 0;
		if (data < 0) data = 0;
		if (id < 0){
			System.err.println("[BattleShops] itemStack to id bad.  " + itemStack + "   " + id);
		}
		return id + data;
	}
	
	public ItemStack getItemStack() {
		int nid = itemid % ITEMM;
		ItemStack is = new ItemStack(nid);
		int datavalue = getDataValueFromId(itemid);
		MaterialData d = new MaterialData(nid, (byte) datavalue);
		is = d.toItemStack();
		is.setAmount(quantity);
		is.setData(d);
//		System.out.println(" is=" + is.getTypeId() + ":" + is.getDurability() + " : " + is.getAmount() + "  " + 
//				is.getData().getItemTypeId() + "   " + is.getData().getData());
		return is;
	}
	public ShopOwner getOwner() {return owner;}

	public String getCommonName() {return getCommonName(itemid);}
	public static String getCommonName(int id) {
		/// I dont really understand in what world id==Defaults.EVERYTHING_ID isnt sufficient
		/// But for some reason its not, the 2nd catches all cases.
		if (id == Defaults.EVERYTHING_ID || (id - Defaults.EVERYTHING_ID == 0)) return Defaults.EVERYTHING_NAME;
		int datavalue = getDataValueFromId(id);
//		System.out.println(id + "   " + datavalue)

		String iname = "";
		try {
			if (datavalue > 0){
				id = id % ITEMM;
				iname = Material.getMaterial(id).toString() + ":" + datavalue;
			}
			String idkey = id +":" + datavalue;

			String cname = InventoryUtil.idToCommon.get(idkey);
			if (cname != null)
				return cname;
			iname = Material.getMaterial(id).toString().toLowerCase() + " durability(" + datavalue+")";
		} catch (Exception e){
			System.err.println("Error getting commonName id=" + id + "   iname=" + iname + "   datavalue=" + datavalue);
			e.printStackTrace();
		}
		return iname;
	}

	public static ItemStack getItemStackByShopID(int itemid) {
		int nid = itemid % ITEMM;
		ItemStack is = new ItemStack(nid);
		int datavalue = getDataValueFromId(itemid);
		MaterialData d = new MaterialData(nid, (byte) datavalue);
		is = d.toItemStack();
		return is;
	}
	private static int getDataValueFromId(int id) {
		return (int) Math.floor(id/ITEMM);
	}
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
		else if (quantity <=0 || quantity > Defaults.MAX_SHOPSIGN_QUANTITY) 
			throw new SignFormatException("Quantity must be between [0-" + Defaults.MAX_SHOPSIGN_QUANTITY+"]", 2);
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
	
	public void setSignValues(Sign sign) throws SignFormatException {
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

	private String getFormattedItem() {
		String name = getCommonName();
		if (name.length() > Defaults.MAX_NAME_ON_SIGN_LENGTH) return name.substring(0, Defaults.MAX_NAME_ON_SIGN_LENGTH);
		return name;
	}

	public static boolean isShopSignOfPlayer(String[] line, Player player, Sign sign) {
		/// we might have the "iConomyChestShop" format or we have lost the link
		if (!playerNameMatches(player, line[0], sign) ) {
			if (Defaults.DEBUG_LINKING)  System.out.println("Didnt match name   <" + 
						player.getName().toLowerCase()  + "><" + line[0].toLowerCase() + ">");
//			System.out.println("names dont match");
			if (PermissionController.isAdmin(player) && line[0].equals(Defaults.ADMIN_NAME)) 
				return true;
			return false;
		}
		
		try {
			return parseShopSign(line) ==  null ? false : true;
		} catch (SignFormatException e) {
			return false;
		}
	}

	private static boolean playerNameMatches(Player player, String line0, Sign sign) {
		if (!PermissionController.hasPermissions(player, sign.getBlock())) return false;
		String name = player.getName();
		
		String signname = line0;
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

	public static boolean isShopSign(String[] line) {
		try {
			return parseShopSign(line) ==  null ? false : true;
		} catch (SignFormatException e) {
			return false;
		}
	}
	

	public static SignValues parseShopSign(String[] text) throws SignFormatException{
		SignValues sv = new SignValues();
		parseQuantity(text[1],sv);
		parseBuySell(text[2], sv);
		parseItemID(text[3],sv);
		return sv;
	}
	
	public static void parseQuantity(String text, SignValues sv) throws SignFormatException {
		try {
			String ql[] = text.split(":");
			if (ql.length == 2){
				sv.quantity = Integer.parseInt(ql[0].replaceAll(" ", ""));
			} else{
				sv.quantity = Integer.parseInt(text.replaceAll(" ", ""));
			}

		} catch (NumberFormatException e){
			throw new SignFormatException("Quantity is bad",2);
		}
    	if (sv.quantity <= 0) throw new SignFormatException("Quantity is bad",2);
    }

    private static void parseBuySell(String text, SignValues sv) throws SignFormatException {
    	String[] bs = text.split(":");
    	if (bs.length>2) throw new SignFormatException("Buy/Sell Line Invalid", 3);
    	if (bs.length == 1){  /// Either just buying, just selling, or bad
    		/// B and S are confusing, these denote what the "Player" sees, 
    		/// but we are selling what they are buying, and vice versa
    		if (bs[0].contains("S")){
    			parseSellText(bs[0],sv);
    		} else if (bs[0].contains("B")){
    			parseBuyText(bs[0],sv);
    		}
    	} else { /// we have both buy and sell
    		parseBuyText(bs[0],sv);
    		parseSellText(bs[1],sv);
    	}
    	if (!sv.isSelling && !sv.isBuying){
			throw new SignFormatException("Sign is neither buying or selling",3);
    	}
	}

	private static void parseBuyText(String text,SignValues sv) throws SignFormatException {
		String btext = text.replaceAll("[Bb]", "");
		try{
			sv.buyPrice = Float.parseFloat(btext);
		} catch (NumberFormatException e){
			throw new SignFormatException("Buy price is bad",3);
		}
		if (sv.buyPrice > 0 ) sv.isBuying = true;
	}

	private static void parseSellText(String text,SignValues sv) throws SignFormatException {
		String stext = text.replaceAll("[Ss]", "");
		try{
			sv.sellPrice = Float.parseFloat(stext);
		} catch (NumberFormatException e){
			throw new SignFormatException("Sell price is bad",3);
		}
		if (sv.sellPrice > 0) sv.isSelling = true;
	}

    private static void parseItemID(String text, SignValues sv) throws SignFormatException {
    	try {
        	sv.itemStack = InventoryUtil.getItemStack(text);
        	sv.coloredText = Util.colorChat(text);
    	} catch(Exception e){
    		throw new SignFormatException("ItemStack is Unrecognized " + text,4);
    	}
    	if (sv.itemStack == null) throw new SignFormatException("ItemStack is Unrecognized " + text,4);
	}

	public String toString(){
		StringBuffer sb = new StringBuffer("ShopSign[");
		sb.append( owner == null ? "null, " : owner.getName() + ",");
		sb.append( getItemId() + " ,");
		sb.append( quantity + " ,");
		sb.append( buyPrice + " :" + sellPrice + " ]");
	
		return sb.toString();
	}

	private void setFull(int amount) {
		Sign sign = getCraftSign();
		if (sign == null) return;

		sign.setLine(1, Util.colorChat(getQuantity() + " : &6Full") );
		sign.update(true);
	}

	private void setEmpty(int quantity, int amount) {
		Sign sign = getCraftSign();
		if (sign == null) return;
		if (amount == 0){
			sign.setLine(1, Util.colorChat(getQuantity() + " : &8 0") );
		} else {
			sign.setLine(1, Util.colorChat(getQuantity() + " : &8<" + getQuantity() ) );
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

		Sign sign = getCraftSign();
		if (sign == null) return;
		sign.setLine(1, Util.colorChat(getQuantity() + " : &2" + amountStr) );
		sign.update(true);
	}

	public void setUnlinked() {
		amount_in_chests = -1;
		Sign sign = getCraftSign();
		if (sign == null || isAdminShop()) return;
		sign.setLine(1, Util.colorChat(quantity + " : &8 U"));
		sign.update(true);		
	}
	
	private Sign getCraftSign() {
		int x = getX();
		int y = getY();
		int z = getZ();

		Block b = world.getBlockAt(x, y, z);
		Material mat = b.getType();
		if (!(mat.equals(Material.SIGN) || mat.equals(Material.SIGN_POST) 
        		|| 	mat.equals(Material.WALL_SIGN))){
			return null;
		}
		Sign sign = (Sign) b.getState();
		return sign;
	}
	public String getQuantityLine() {
		Sign sign = getCraftSign();
		if (sign == null) return "";
		return sign.getLine(1);
	}

	public void setSignAmount(int amount, int free) {
		if (isAdminShop())
			return;
		amount_in_chests = amount;
		space_left_in_chests = free;
		if (quantity > amount){
			setEmpty(quantity,amount);
		} else if (free < quantity){
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
		return space_left_in_chests < quantity;
	}
	public boolean isEmpty(){
		if (isAdminShop()) return false;
		return quantity > amount_in_chests;
	}
	public Location getLocation() {
		return new Location(world,x,y,z);
	}
}
