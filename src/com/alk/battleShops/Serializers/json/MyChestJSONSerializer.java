package com.alk.battleShops.Serializers.json;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;

import com.alk.battleShops.Defaults;
import com.alk.battleShops.objects.ShopChest;
import com.alk.battleShops.objects.ShopOwner;
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
public class MyChestJSONSerializer implements JsonSerializer<ShopChest>, JsonDeserializer<ShopChest> {


	public JsonElement serialize(ShopChest src, Type typeOfSrc,JsonSerializationContext context) {
		if (Defaults.DEBUG_TRACE) System.out.println("Chest::serialize");
		JsonObject obj = new JsonObject();
		
    	Gson gson = new Gson();
		obj.add("owner", gson.toJsonTree(src.getOwner()));
    	obj.addProperty("world", src.getWorld().getName());
		obj.addProperty("x", src.getX());
		obj.addProperty("y", src.getY());
		obj.addProperty("z", src.getZ());

		obj.add("itemids", gson.toJsonTree(src.getItemIds()));

//		System.out.println("Obj=" + obj);
		return obj;		
	}
	public ShopChest deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		if (Defaults.DEBUG_TRACE) System.out.println("Chest::deserialize");
		JsonObject jobject = (JsonObject) json;
		Gson gson = new Gson();
		
		/// Recover owner of this chest
		Type t = new TypeToken<ShopOwner>(){}.getType();
		JsonElement jso = jobject.get("owner");		
		ShopOwner owner = gson.fromJson(jso,t);
		
		/// Get x,y,z loc and get the chest there
//		System.out.println("job=" + jso);
		String worldname = jobject.get("world").getAsString();
		int x = jobject.get("x").getAsInt();
		int y = jobject.get("y").getAsInt();
		int z = jobject.get("z").getAsInt();
		
		World world = Bukkit.getServer().getWorld(worldname);
		Block b = world.getBlockAt(x, y, z);
		final Material clickedMat = b.getType();
		if (!(clickedMat.equals(Material.CHEST))){
			return null;
		}
//		CraftChest chest = new CraftChest(b);
		Chest chest = (Chest) b.getState();
		
		/// Get our itemids that this chest sells
		JsonElement jsl = jobject.get("itemids");
		Type listtype = new TypeToken<HashSet<Integer>>(){}.getType();
		Set<Integer> itemids = gson.fromJson(jsl,listtype);

		return new ShopChest(chest,owner,itemids);
	}
	
}
