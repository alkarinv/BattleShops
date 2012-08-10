package com.alk.battleShops.Serializers.json;

import java.lang.reflect.Type;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import com.alk.battleShops.Defaults;
import com.alk.battleShops.Exceptions.SignFormatException;
import com.alk.battleShops.objects.ShopOwner;
import com.alk.battleShops.objects.ShopSign;
import com.alk.battleShops.objects.SignValues;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
/**
 * 
 * @author alkarin
 *
 */
public class ShopSignJSONSerializer implements JsonSerializer<ShopSign>, JsonDeserializer<ShopSign>{


	public JsonElement serialize(ShopSign sign, Type typeOfSrc,
			JsonSerializationContext context) {
		if (Defaults.DEBUG_TRACE) System.out.println("ShopSign::serialize");
		JsonObject obj = new JsonObject();

    	Gson gson = new Gson();
		
		obj.add("o", gson.toJsonTree(sign.getOwner()));
    	obj.addProperty("w", sign.getWorld().getName());
		obj.addProperty("x", sign.getX());
		obj.addProperty("y", sign.getY());
		obj.addProperty("z", sign.getZ());

		return obj;
	}

	public ShopSign deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
		throws JsonParseException {
		if (Defaults.DEBUG_TRACE) System.out.println("ShopSign::deserialize");
		ShopSign ss = null;
		try{
		JsonObject jobject = (JsonObject) json;
		Gson gson = new Gson();

		/// Retrieve owner
		Type t = new TypeToken<ShopOwner>(){}.getType();
		JsonElement jso = jobject.get("o");

		ShopOwner owner = gson.fromJson(jso,t);
		
		/// Get back world and location
		String worldname = jobject.get("w").getAsString();
		int x = jobject.get("x").getAsInt();
		int y = jobject.get("y").getAsInt();
		int z = jobject.get("z").getAsInt();
		World world = Bukkit.getServer().getWorld(worldname);
		Block b = world.getBlockAt(x, y, z);

//		CraftSign sign = new CraftSign(b);
		Sign sign = (Sign) b.getState();
		String lines[] = sign.getLines();
		SignValues sv = null;
		try {
			sv = ShopSign.parseShopSign(lines);
		} catch (SignFormatException e) {
			System.err.println("!!!!!!!!!!!! couldnt reparse sign!!!!  json=" + json.getAsString());
//			e.printStackTrace();
		}
		
		ss = new ShopSign(owner,sign,sv);
		} catch(Exception e){
			System.err.println("!!!!!!!!!!!! couldnt reparse sign!!!! json=" + json.getAsString());
			e.printStackTrace();
		}
		return ss;
	}

}
