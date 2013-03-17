package mc.alk.shops.controllers;

import mc.alk.mc.factories.ItemFactory;
import mc.alk.shops.bukkit.controllers.BukkitMessageController;
import mc.alk.shops.objects.SignFormatException;
import mc.alk.shops.objects.SignValues;

public class SignParser {
	public static SignValues parseShopSign(String[] text) throws SignFormatException{
		SignValues sv = new SignValues();
		parseQuantity(text[1],sv);
		parseBuySell(text[2], sv);
		parseItem(text[3],sv);
		sv.itemStack.setQuantity(sv.quantity);
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

    private static void parseItem(String text, SignValues sv) throws SignFormatException {
    	try {
//    		String split[] = MessageController.decolorChat(text).split(" ");
    		String split[] = text.split(" ");
    		if (split.length ==2 && BukkitMessageController.decolorChat(split[0]).trim().equalsIgnoreCase("E")){
            	sv.itemStack = ItemFactory.createItem(split[1]);
    		} else {
            	sv.itemStack = ItemFactory.createItem(text);
    		}
        	sv.coloredText = BukkitMessageController.colorChat(text);
    	} catch(Exception e){
    		throw new SignFormatException("ItemStack is Unrecognized " + text,4);
    	}
    	if (sv.itemStack == null) throw new SignFormatException("ItemStack is Unrecognized " + text,4);
	}


	public static boolean isShopSign(String[] line) {
		try {
			return parseShopSign(line) ==  null ? false : true;
		} catch (SignFormatException e) {
			return false;
		}
	}

}
