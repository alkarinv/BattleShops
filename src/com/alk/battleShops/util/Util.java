package com.alk.battleShops.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;

/**
 * 
 * @author alkarin
 *
 */
public class Util {

    //Checks if string is an integer
    public static boolean isInt(String i) {
        try {Integer.parseInt(i);return true;} catch (Exception e) {return false;}
    }
    
    public static boolean isFloat(String i){
        try{Float.parseFloat(i);return true;} catch (Exception e){return false;}
    }

    //Changes the & to color code sign
    public static String colorChat(String msg) {
        return msg.replaceAll("&", Character.toString((char) 167));
    }

    //Gets the Material from bukkit enum
    public static Material getMat(String name) {
        if (Util.isInt(name)) {
            return getMat(Integer.parseInt(name));
        } else {
        	int id = returnID(name);
        	return id != -1 ? Material.getMaterial(id): null;
        }
    }

    //Gets the Material from ID
    public static Material getMat(int id) {
        return Material.getMaterial(id);
    }

    //Returns the id of a material
    public static int returnID(String name) {
        Material[] mat = Material.values();
        int temp = 9999;
        Material tmp = null;
        for (Material m : mat) {
            if (m.name().toLowerCase().replaceAll("_", "").startsWith(name.toLowerCase().replaceAll("_", "").replaceAll(" ", ""))) {
                if (m.name().length() < temp) {
                    tmp = m;
                    temp = m.name().length();
                }
            }
        }
        if (tmp != null) {
            return tmp.getId();
        }
        return -1;
    }
    

	public static void debug(boolean debug, String string) {
		if (debug) System.out.println(string);
	}

	public static String getHasOrHave(int itemCount) {
		return itemCount == 1 ? "has" : "have";
	}

	public static String getChestOrChests(int itemCount) {
		return itemCount == 1 ? "chest" : "chests";
	}

	public static String getSignOrSigns(int itemCount) {
		return itemCount == 1 ? "sign" : "signs";
	}

	public static String removeColor(String string) {
    	/// Remove all the color codes, first the user defined &[0-9a-fA-F]
		string = string.replaceAll("&[0-9a-fA-F]", "");
    	/// Remove the server color codes
		string = ChatColor.stripColor(string);
		return string;
	}

    
}
