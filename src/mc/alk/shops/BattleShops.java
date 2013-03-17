package mc.alk.shops;

import java.io.File;

import mc.alk.mc.MCServer;
import mc.alk.shops.bukkit.controllers.BukkitMessageController;
import mc.alk.shops.controllers.PermController;
import mc.alk.shops.serializers.ShopsSerializer;
import mc.alk.shops.utils.FileUtil;
import mc.alk.shops.utils.InventoryUtil;
import mc.alk.shops.utils.WorldUtil;

public enum BattleShops {
	INSTANCE;

	BattleShopsPlugin battleShopsPlugin;

	ShopsSerializer shopSerializer;

	public static BattleShopsPlugin getPlugin(){
		return INSTANCE.battleShopsPlugin.getSelf();
	}

	public static BattleShops getSelf(){
		return INSTANCE;
	}

	public static ShopsSerializer getShopSerializer(){
		return INSTANCE.shopSerializer;
	}

	public void setShopSerializer(ShopsSerializer shopsSerializer) {
		this.shopSerializer = shopsSerializer;
	}

	public static boolean isEnabled() {
		return INSTANCE != null && INSTANCE.battleShopsPlugin != null ?
				INSTANCE.battleShopsPlugin.isEnabled() : false;
	}

	public static void init(MCServer server, BattleShopsPlugin plugin, PermController perm,
			InventoryUtil inventoryUtil, ShopsSerializer shopSerializer, WorldUtil worldUtil){
		MCServer.setInstance(server);
		INSTANCE.battleShopsPlugin = plugin;
		INSTANCE.shopSerializer = shopSerializer;
		InventoryUtil.setInventoryUtil(inventoryUtil);
		PermController.setPermController(perm);
		WorldUtil.setWorldUtil(worldUtil);
		INSTANCE.privateInit();
	}

	private void privateInit() {
		File dir = battleShopsPlugin.getDataDirectory();
        BukkitMessageController.setConfig(FileUtil.load(this.getClass(),dir.getPath() +"/config.yml", Defaults.DEFAULT_MESSAGES_FILE));
	}

}
