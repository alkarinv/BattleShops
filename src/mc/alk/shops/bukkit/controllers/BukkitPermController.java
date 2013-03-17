package mc.alk.shops.bukkit.controllers;

import mc.alk.bukkit.BukkitLocation;
import mc.alk.bukkit.BukkitPlayer;
import mc.alk.bukkit.MCCommandSender;
import mc.alk.mc.MCLocation;
import mc.alk.mc.MCPlayer;
import mc.alk.shops.Defaults;
import mc.alk.shops.controllers.PermController;

import org.bukkit.entity.Player;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
/**
 *
 * @author alkarin
 *
 */
public class BukkitPermController extends PermController{
	public static WorldGuardPlugin worldGuard;

	public BukkitPermController(){}

	@Override
	public boolean hasMCBuildPerms(MCPlayer player, MCLocation location){
		return worldGuard == null ? null :
				worldGuard.canBuild(((BukkitPlayer)player).getPlayer(), ((BukkitLocation)location).getLocation());
	}

	public boolean hasMCCreatePermissions(MCPlayer player, MCLocation location) {
		if (worldGuard != null)
			return worldGuard.canBuild(
					 ((BukkitPlayer)player).getPlayer(), ((BukkitLocation)location).getLocation()) ||
					 isMCAdmin(player);
		if (hasMCPermission(player,Defaults.PERM_CREATE))
			return true;
		return isMCAdmin(player);
	}

	@Override
	public boolean isMCAdmin(MCCommandSender sender) {
		return hasMCPermission(sender,Defaults.PERM_ADMIN);
	}

	@Override
	public boolean hasMCPermission(MCCommandSender sender, String perm) {
		Player p = ((BukkitPlayer)sender).getPlayer();
		return p.hasPermission(perm) || p.isOp();
	}



//	public boolean isMCAdmin(String name) {
//		if (ShopOwner.isAdminShop(name)) return true;
//		Player p = BattleShops.getBukkitServer().getPlayer(name);
//		if (p == null)
//			return false;
//		return BukkitPermController.isAdmin(p);
//	}

}
