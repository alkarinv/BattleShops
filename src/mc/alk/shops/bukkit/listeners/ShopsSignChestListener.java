package mc.alk.shops.bukkit.listeners;

import java.util.HashMap;

import mc.alk.bukkit.BukkitLocation;
import mc.alk.bukkit.BukkitPlayer;
import mc.alk.bukkit.blocks.BukkitChest;
import mc.alk.bukkit.blocks.BukkitSign;
import mc.alk.mc.MCLocation;
import mc.alk.mc.MCPlayer;
import mc.alk.mc.blocks.MCSign;
import mc.alk.shops.Defaults;
import mc.alk.shops.bukkit.controllers.BukkitMessageController;
import mc.alk.shops.controllers.LinkController;
import mc.alk.shops.controllers.PermController;
import mc.alk.shops.controllers.Shop;
import mc.alk.shops.controllers.ShopController;
import mc.alk.shops.controllers.SignParser;
import mc.alk.shops.controllers.TransactionController;
import mc.alk.shops.objects.ShopOwner;
import mc.alk.shops.objects.ShopSign;
import mc.alk.shops.objects.SignFormatException;
import mc.alk.shops.objects.SignValues;
import mc.alk.shops.utils.Pair;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;


/**
 *
 * @author alkarin
 *
 */
public class ShopsSignChestListener implements Listener  {
	public static final int BUY_INT = 0;
	public static final int SELL_INT = 1;

	public static int interval = 300;
	private HashMap<String, Long> userTime = new HashMap<String, Long>();
	private HashMap<String, Long> userCommandTime = new HashMap<String, Long>();

	private HashMap<String, Pair<Integer, Integer>> userMultiplier =
			new HashMap<String, Pair<Integer,Integer>>();

	private LinkController linkController = null;
	private TransactionController tc = null;
	public ShopsSignChestListener(LinkController link, TransactionController tc){
		linkController = link;
		this.tc = tc;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChestInteract(PlayerInteractEvent event) {
		/// No longer check for event.isCancelled as certain protection plugins cancel the interactions.
		/// But we still want to be able to buy/sell
		final Block clickedBlock = event.getClickedBlock();
		if (clickedBlock == null) return; /// This can happen, minecraft is a strange beast
		final Material clickedMat = clickedBlock.getType();
		/// If this is an uninteresting block get out of here as quickly as we can
		/// We only care about chest events if they are not cancelled
		if (!clickedMat.equals(Material.CHEST) || event.isCancelled()) {
			return;}
		final BukkitPlayer player = new BukkitPlayer(event.getPlayer());
		final MCLocation bloc = new BukkitLocation(clickedBlock.getLocation());
		Action action = event.getAction();
		if (Defaults.DEBUG_TRACE) player.sendMessage("onPlayerInteract  HasPermission= " +
				PermController.hasCreatePermissions(player, bloc) +"  action="+action);

		/// Check to see whether we need to break ownership of this chest with previous owner
		if (action == Action.RIGHT_CLICK_BLOCK){
			Chest chest = (Chest) clickedBlock.getState();
			linkController.chestRightClick(new BukkitChest(chest), player);
		} else if (event.getItem() != null && event.getItem().getTypeId() == Defaults.WAND){
			boolean hasPermissionToBuild = PermController.hasCreatePermissions(player, bloc);
			/// We cant let them link signs/chests where they can't build, otherwise they can "teleport" items
			/// through the use of signs somewhere else
			if (hasPermissionToBuild){
				/// dont want people in creative killing the chest
				if (action==Action.LEFT_CLICK_BLOCK &&
						player.getPlayer().getGameMode() == GameMode.CREATIVE){
					event.setCancelled(true);}
				activateChestEvent(event, player, clickedBlock);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerSignInteract(PlayerInteractEvent event) {
		/// No longer check for event.isCancelled as certain protection plugins cancel the interactions.
		/// But we still want to be able to buy/sell
		final Block clickedBlock = event.getClickedBlock();
		if (clickedBlock == null) return; /// This can happen, minecraft is a strange beast
		final Material clickedMat = clickedBlock.getType();

		/// If this is an uninteresting block get out of here as quickly as we can
		if (!(clickedMat.equals(Material.SIGN) || clickedMat.equals(Material.SIGN_POST)
				|| 	clickedMat.equals(Material.WALL_SIGN) )) {
			return;
		}
		final Action action = event.getAction();
		final BukkitPlayer player = new BukkitPlayer(event.getPlayer());
		final MCLocation bloc = new BukkitLocation(clickedBlock.getLocation());
		if (Defaults.DEBUG_TRACE) player.sendMessage("onPlayerInteract  HasPermission= " +
				PermController.hasCreatePermissions(player, bloc) +"  action="+action);
		/// Check to see if we are performing a left/right click
		if ((action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK)) {
			return;
		}

		/// Check to see if we are activating a sign
		/// This occurs if they are holding a redstone torch and left clicking
		if (event.getItem() != null && event.getItem().getTypeId() == Defaults.WAND &&
				action == Action.LEFT_CLICK_BLOCK){
			boolean hasPermissionToBuild = PermController.hasCreatePermissions(player, bloc);
			/// We cant let them link signs/chests where they can't build, otherwise they can "teleport" items
			/// through the use of signs somewhere else
			if (hasPermissionToBuild){
				/// dont want people in creative killing the sign
				if (action==Action.LEFT_CLICK_BLOCK && player.getPlayer().getGameMode() == GameMode.CREATIVE){
					event.setCancelled(true);}
				activateSignEvent(event, player, clickedBlock);
			}
			return;
		}

		/// Find the shopsign the player has clicked on
		MCSign sign =null;
		ShopSign ss = null;
		try {
			sign = new BukkitSign((Sign) event.getClickedBlock().getState());
			ss = ShopController.findShopSign(sign);
		} catch(Exception e){
			System.err.println("Failed on block interact");
			System.err.println("Loc=" + event.getClickedBlock().getLocation());
			e.printStackTrace();
		}
		/// No ShopSign equals we dont care
		if (ss == null){
			if (sign == null) /// Not only is a ShopSign not there, a sign is not there
				return;
			SignValues sv = null;
			try {
				sv = SignParser.parseShopSign(sign.getLines());
				boolean isAdminSign = ShopOwner.isAdminShop(sign.getLine(0));
				if (sv != null && !isAdminSign){
					sign.setLine(1, sv.quantity + ": U");
					sign.update(true);
				} else if (isAdminSign){
					sign.setLine(1, sv.quantity +"");
					ShopOwner so = new ShopOwner(Defaults.ADMIN_NAME);
					ss = new ShopSign(so, sign,sv);
					ShopController.addShopSign(ss);
				}
			} catch (SignFormatException e) {

			}
			if (Defaults.DEBUG_LINKING) System.out.println("failed finding shopsign");
			return;
		}

		//		if (ss.getOwner().sameOwner(player.getName()) && action == Action.RIGHT_CLICK_BLOCK &&
		//				ConfigController.getBoolean("rightClickSignToOpenChest") ){
		//			linkController.openChestRemotely(player,ss);
		//			return;
		//		}

		/// No sneaking! I like the concept of transactions being public
		if (player.getPlayer().isSneaking()){
			player.sendMessage(BukkitMessageController.getMessage("Sneaking"));
			return;
		}

		if (action == Action.RIGHT_CLICK_BLOCK){
			event.setCancelled(true); /// we are now capturing this event so that blocks dont get placed
		}

		/// Dont let players use their own shop
		ShopOwner so = new ShopOwner(player.getName());
		if (ShopOwner.sameOwner(ss.getOwner(), so) && !PermController.isAdmin(player)){
			player.sendMessage(BukkitMessageController.getMessage("You_cannot_use_your_own_shop"));
			return;
		}

		/// Probably have a valid shop, dont let them spam
		String playerName = player.getName();
		if(userTime.containsKey(playerName)){
			if((System.currentTimeMillis() - userTime.get(playerName)) < interval){
				player.sendMessage(BukkitMessageController.getMessage("wait"));
				return;
			}
		}
		userTime.put(playerName, System.currentTimeMillis());

		/// Cancel a buy/sell multiplier command if its been too long, it could be confusing for the player
		if (userCommandTime.containsKey(playerName)){
			if((System.currentTimeMillis() - userCommandTime.get(playerName)) < Defaults.SECONDS_FOR_COMMAND){
				cancelCommandTimer(player);
			}
		}

		/// Perform a buy or sell

		if (Defaults.DEBUG_TRACE) System.out.println("Should be starting to buy/sell");
		if (action == Action.LEFT_CLICK_BLOCK && Defaults.LEFT_CLICK_SELL_ACTION){
			Pair<Integer, Integer> mult = userMultiplier.get(player.getName());
			if (mult != null && mult.fst == SELL_INT){
				cancelCommandTimer(player);
				tc.sellToShop(ss,player, mult.snd);
			} else {
				tc.sellToShop(ss,player, 1);
			}
		} else {
			Pair<Integer, Integer> mult = userMultiplier.get(player.getName());
			if (mult != null && mult.fst == BUY_INT){
				cancelCommandTimer(player);
				tc.buyFromShop(ss,player, mult.snd);
			} else {
				tc.buyFromShop(ss,player, 1);
			}
		}
	}

	private void activateChestEvent(PlayerInteractEvent event,final MCPlayer player, final Block clickedBlock){
		if (Defaults.DEBUG_TRACE) System.out.println("ShopsLinkListener::activateChestEvent");
		Chest chest = (Chest) clickedBlock.getState();
		linkController.activateChestShop(new BukkitChest(chest), player);
	}

	private void activateSignEvent(PlayerInteractEvent event,final MCPlayer player, final Block clickedBlock) {
		if (Defaults.DEBUG_TRACE) System.out.println("ShopsLinkListener::activateSignEvent");
		/// This is a convenience for previously made shops using the chestshop system
		/// In that system the players name is on the first line
		/// This is also convenient if signs get unlinked for some reason
		MCSign csign = new BukkitSign(((Sign) event.getClickedBlock().getState()));
		String[] sLines = csign.getLines();
		boolean isPlayersShopSign = ShopSign.isShopSignOfPlayer(sLines, new BukkitPlayer(event.getPlayer()),csign);

		if ( sLines.length < 3 || !isPlayersShopSign) {
			return;
		}
		/// If this sign is already active, just tell them and return, or do something if its an admin
		ShopSign tss = ShopController.findShopSign(csign);
		if (tss != null){
			boolean isAdminPlayer = PermController.isAdmin(player);
			boolean isAdminShop = tss.isAdminShop();
			if (isAdminShop){
				if (isAdminPlayer){
					player.sendMessage(BukkitMessageController.getMessage("sign_already_active"));}
				return;
			}
			if (isAdminPlayer && !isAdminShop && !tss.getOwner().getName().equalsIgnoreCase(player.getName())){
				player.sendMessage(BukkitMessageController.getMessage("admins_cant_link_other_shops"));
				return;/// Admins cant link other shops
			}
			if (!isAdminPlayer && !isAdminShop){
				Shop s = ShopController.getShop(csign.getWorld(),new ShopOwner(event.getPlayer().getName()));
				if (s!= null){
					int chestCount = s.getNumChestsAttachedToSign(tss);
					player.sendMessage(BukkitMessageController.getMessage("sign_already_links",
							chestCount,BukkitMessageController.getChestOrChests(chestCount)));
					return;
				}

			}
		}
		/// Else lets make a valid sign
		SignValues sv;
		try {
			sv = SignParser.parseShopSign(sLines);
		} catch (SignFormatException e) {
			e.printStackTrace();
			/// We really didnt have an old shop, get out of here
			return;
		}
		if (sv != null){
			Block sign = event.getClickedBlock();
			Sign cs = (Sign) sign.getState();
			ShopOwner so = new ShopOwner( new BukkitPlayer(event.getPlayer()));
			ShopSign ss = new ShopSign(so, new BukkitSign(cs),sv);
			boolean isAdminShop = ss.isAdminShop();
			ShopController.addShopSign(ss);
			if (!isAdminShop){
				ShopController.updateAffectedSigns(so,ss);
			} else {
				player.sendMessage(BukkitMessageController.getMessage("activated_admin_shop"));
			}

		}

	}

	public void setBuyCommand(MCPlayer player, int multiplier) {
		if (multiplier < 1 || multiplier > Defaults.MULTIPLIER_LIMIT){
			player.sendMessage(BukkitMessageController.getMessage("multiplier_bounds", Defaults.MULTIPLIER_LIMIT));
			return;
		}

		cancelCommandTimer(player);
		userMultiplier.put(player.getName(), new Pair<Integer,Integer>(BUY_INT, multiplier));
		userCommandTime.put(player.getName(), System.currentTimeMillis());
	}

	public void cancelCommandTimer(MCPlayer player){
		userMultiplier.remove(player.getName());
		userCommandTime.remove(player.getName());
	}

	public void setSellCommand(MCPlayer player, int multiplier) {
		if (multiplier < 1 || multiplier > Defaults.MULTIPLIER_LIMIT){
			player.sendMessage(BukkitMessageController.getMessage("multiplier_bounds", Defaults.MULTIPLIER_LIMIT));
			return;
		}
		cancelCommandTimer(player);
		userMultiplier.put(player.getName(), new Pair<Integer,Integer>(SELL_INT, multiplier));
		userCommandTime.put(player.getName(), System.currentTimeMillis());
	}


	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		ShopController.onPlayerLogin(event.getPlayer().getName());
	}

}
