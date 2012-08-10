package com.alk.battleShops.Serializers.json;

import java.lang.reflect.Type;

import com.alk.battleShops.Defaults;
import com.alk.battleShops.objects.PlayerActivity;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class PlayerActivityJSONSerializer implements JsonSerializer<PlayerActivity>, JsonDeserializer<PlayerActivity>{

	public JsonElement serialize(PlayerActivity pa, Type arg1, JsonSerializationContext arg2) {
		if (Defaults.DEBUG_TRACE) System.out.println("PlayerActivity::serialize");
		JsonObject obj = new JsonObject();
		obj.addProperty("n", pa.name);
		obj.addProperty("lst", pa.lastShopTransaction);
		obj.addProperty("lpl", pa.lastPlayerLogin);
		obj.addProperty("lsu", pa.lastShopUpdate);
		return obj;
	}

	public PlayerActivity deserialize(JsonElement json, Type arg1,
			JsonDeserializationContext arg2) throws JsonParseException {
		JsonObject jobject = (JsonObject) json;
		String name = jobject.get("n").getAsString();
		PlayerActivity pa = new PlayerActivity(name);
		pa.lastShopTransaction = jobject.get("lst").getAsLong();
		pa.lastPlayerLogin = jobject.get("lpl").getAsLong();
		pa.lastShopUpdate = jobject.get("lsu").getAsLong();
		return pa;
	}

}
