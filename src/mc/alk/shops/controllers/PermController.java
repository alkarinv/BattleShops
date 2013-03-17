package mc.alk.shops.controllers;

import mc.alk.bukkit.MCCommandSender;
import mc.alk.mc.MCLocation;
import mc.alk.mc.MCPlayer;
import mc.alk.shops.Defaults;

public abstract class PermController {
	static PermController perms;

	public abstract boolean hasMCPermission(MCCommandSender sender, String perm);
	public abstract boolean isMCAdmin(MCCommandSender sender);
	public abstract boolean hasMCBuildPerms(MCPlayer player, MCLocation location);

	public static boolean isAdmin(MCCommandSender sender) {
		return perms.isMCAdmin(sender);
	}

	public boolean hasBuildPerms(MCPlayer player, MCLocation location){
		return perms.hasMCBuildPerms(player,location);
	}

	public static boolean hasCreatePermissions(MCPlayer player, MCLocation location) {
		return perms.isMCAdmin(player) ||
				(perms.hasMCBuildPerms(player,location) &&
						perms.hasMCPermission(player,Defaults.PERM_CREATE));
	}
	public static void setPermController(PermController perm) {
		perms=perm;
	}
	public static PermController getPermController() {
		return perms;
	}


}
