package mc.alk.shops.bukkit.listeners;

import mc.alk.bukkit.BukkitPlayer;
import mc.alk.bukkit.blocks.BukkitSign;
import mc.alk.mc.MCPlayer;
import mc.alk.mc.MCServer;
import mc.alk.shops.BattleShops;
import mc.alk.shops.Defaults;
import mc.alk.shops.bukkit.controllers.BukkitMessageController;
import mc.alk.shops.controllers.PermController;
import mc.alk.shops.controllers.ShopController;
import mc.alk.shops.controllers.SignParser;
import mc.alk.shops.objects.ShopOwner;
import mc.alk.shops.objects.ShopSign;
import mc.alk.shops.objects.SignFormatException;
import mc.alk.shops.objects.SignValues;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;


/**
 *
 *
 * @author Alkarin
 */
public class ShopsSignListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onSignChange(SignChangeEvent event) {
		if (Defaults.DEBUG_TRACE) System.out.println("onSignChange Event");

		//    	if (event.isCancelled()) return;
		final Block block = event.getBlock();
		final Material type = block.getType();
		/// Is this ever false? anyways onsignchange is a very low frequency event. best to be certain
		if (!(type.equals(Material.SIGN) || type.equals(Material.SIGN_POST) || type.equals(Material.WALL_SIGN))) {
			return;
		}

		/// Check to see if we have a shop sign
		if ( event.getLines().length < 3 || !SignParser.isShopSign(event.getLines())) {
			return;
		}

		try{
			String[] lines = event.getLines();
			SignValues sv = SignParser.parseShopSign(lines);

			if (sv != null){
				MCPlayer p = new BukkitPlayer(event.getPlayer());
				if (!p.hasPermission("shop.create") && !PermController.isAdmin(p)){
					BukkitMessageController.sendMessage(p, "&cYou don't have permissions to create a Shop Sign");
					cancelAndDrop(event,block);
					return;
				}

				Sign sign = (Sign)block.getState();
				ShopOwner so ;
				if (ShopOwner.isAdminShop(lines[0])){
					if (PermController.isAdmin(p)){
						so = new ShopOwner(Defaults.ADMIN_NAME);
					} else {
						BukkitMessageController.sendMessage(p, "&cYou don't have permissions to create an admin shop");
						cancelAndDrop(event,block);
						return;
					}
				} else {
					so = new ShopOwner(p.getName());
				}
				final ShopSign ss = new ShopSign(so, new BukkitSign(sign),sv);
				ss.validate();
				ss.setEventValues(event);
				lines[3] = sv.coloredText; /// Allow for colored signs
				int chestCount = ShopController.addShopSign(ss);
						//                System.out.println("adding shop sign " + ss);
				if (ss.isAdminShop()){
					BukkitMessageController.sendMessage(p,BukkitMessageController.getMessage("activated_admin_shop"));
				} else {
					if (chestCount > 0){
						BukkitMessageController.sendMessage(p,BukkitMessageController.getMessage("setup_shop",
								ss.getCommonName(),
								chestCount,BukkitMessageController.getChestOrChests(chestCount)));
					} else {
						BukkitMessageController.sendMessage(p,BukkitMessageController.getMessage("setup_shop_no_chests",
								ss.getCommonName()));
					}
					MCServer.scheduleSyncDelayedTask(BattleShops.getPlugin(), new Runnable(){
						@Override
						public void run() {
							ShopController.updateAffectedSigns(ss.getOwner(), ss);
						}
					});
				}

			}
		} catch (SignFormatException e){
			event.getPlayer().sendMessage(e.getMessage());
			cancelAndDrop(event,block);
		} catch (Exception e){
			e.printStackTrace();
			cancelAndDrop(event,block);
		}
	}

	public void cancelAndDrop(SignChangeEvent event, Block block){
		event.setCancelled(true);
		block.setType(Material.AIR);
		block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.SIGN, 1));
	}

}
