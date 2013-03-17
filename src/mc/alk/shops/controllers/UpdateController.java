package mc.alk.shops.controllers;

import mc.alk.shops.serializers.SQLInstance;
import mc.alk.util.Log;

public class UpdateController {
	SQLInstance sql;

	public UpdateController(SQLInstance sql) {
		this.sql = sql;
	}

	public void update(){
		if (shouldUpdateTo3point4()){
			Log.warn("[BattleShops] Updating to database 3.4");
		}
	}

	private boolean shouldUpdateTo3point4(){
		return sql.shouldUpdateTo3point4();
	}

}
