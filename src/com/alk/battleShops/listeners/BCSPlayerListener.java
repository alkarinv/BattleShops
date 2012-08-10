package com.alk.battleShops.listeners;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import com.alk.battleShops.Defaults;
import com.alk.battleShops.Exceptions.SignFormatException;
import com.alk.battleShops.controllers.ConfigController;
import com.alk.battleShops.controllers.LinkController;
import com.alk.battleShops.controllers.MessageController;
import com.alk.battleShops.controllers.PermissionController;
import com.alk.battleShops.controllers.TransactionController;
import com.alk.battleShops.objects.Shop;
import com.alk.battleShops.objects.ShopOwner;
import com.alk.battleShops.objects.ShopSign;
import com.alk.battleShops.objects.SignValues;
import com.alk.battleShops.objects.WorldShop;
import com.alk.battleShops.util.Pair;
import com.alk.battleShops.util.Util;

/**
 * 
 * @author alkarin
 *
 */
public class BCSPlayerListener implements Listener  {


	public static final int BUY_INT = 0;
	public static final int SELL_INT = 1;

	public static int interval;
	private HashMap<String, Long> userTime = new HashMap<String, Long>();
	private HashMap<String, Long> userCommandTime = new HashMap<String, Long>();

	private HashMap<String, Pair<Integer, Integer>> userMultiplier = 
			new HashMap<String, Pair<Integer,Integer>>();

	private LinkController linkController = null;
	private TransactionController tc = null;
	public BCSPlayerListener(LinkController link, TransactionController tc){
		linkController = link;
		this.tc = tc;
	}
	
	@EventHandler
	public void onInventoryCloseEvent(final InventoryCloseEvent event){
		
	}
	@EventHandler
	public void onInventoryClickEvent(final InventoryClickEvent event){
//		event.g
	}
//	@EventHandler
//	public void onInventoryClickEvent(final InventoryClickEvent event){
//		InventoryView iv = event.getView();
//		final Player p = (Player) event.getWhoClicked();
//		ItemStack curItem = event.getCurrentItem();
//		ItemStack cursorItem = event.getCursor();
//		boolean shiftClick = event.isShiftClick();
//		boolean leftClick = event.isLeftClick();
//		boolean rightClick = event.isRightClick();
//		boolean isTopShopChest = iv.getTopInventory().getType() == InventoryType.CHEST;
//		SlotType st = event.getSlotType();
//		boolean clickPlayerInventory = st==SlotType.QUICKBAR || 
//				(event.getInventory().getType() == InventoryType.PLAYER) ||
//				(event.getRawSlot() > 26 && iv.getBottomInventory().getType()==InventoryType.PLAYER);
//		Log.info("   InventoryView = " +iv.getTitle()+" "+event.getSlotType() +" " + event.getRawSlot() + " iv " +iv.getTopInventory().getType() + " " +iv.getBottomInventory().getType());
//		Log.info("  cur item = " + event.getCurrentItem() +"  cursor=" + event.getCursor() +"  shift="+shiftClick+"  " + leftClick+":" + rightClick +
//				"  clickononPlayerinv="+clickPlayerInventory);
//		if (!isTopShopChest ){
//			Log.info("Skipping b/c its not a shop chest= " + curItem);
//			return;
//		}
//		if (clickPlayerInventory && !shiftClick){
//			Log.info("Skipping b/c we are dealing with player inv stuff = " + curItem);
//			return;
//		}
//		boolean curItemEmpty = curItem == null || curItem.getType() == Material.AIR;
//		boolean cursorItemEmpty = cursorItem == null || cursorItem.getType() == Material.AIR;
//		if (event.getInventory().getType() == InventoryType.PLAYER){
//			Log.info("Skipping Player clicking own inv= " + curItem);
//			return;			
//		}
//		if (!curItemEmpty && !cursorItemEmpty){ /// they are buying something with something in their hands
//			if (curItem.getType() != cursorItem.getType()){ /// This is really a buy of one type.. with a sell of another
//				event.setCancelled(true);
//				Log.info("  buy/sell case");
//				return;
//			}
//			//			
//			//			Log.info("  handling a double case... skipping ");
//			return;			
//
//		}
//		//		else if ( curItemEmpty && !cursorItemEmpty && shiftClick){ /// selling case... but with shift click becomes buying
//		//			Log.info("Buying shiftclick ??= " + cursorItem.getAmount() +"  " + cursorItem);
//		//			return;	
//		//		} 
//		//		else if ( curItemEmpty && !cursorItemEmpty && rightClick){ /// buying with rightclick.. which gives half the items
//		//			Log.info("Buying rightclick ??= " + cursorItem.getAmount() +"  " + cursorItem);
//		//		} 
//		else if ( !curItemEmpty && cursorItemEmpty && rightClick){ /// buying case??
//			int amount = (int) Math.ceil((float)curItem.getAmount()/2.0f);
//			Log.info("Buying rightclick ??= " + amount +"  " + curItem);
//		}
//		else if ( !curItemEmpty && cursorItemEmpty){ ///
//			if (shiftClick && clickPlayerInventory){ /// selling an entire stack
//				Log.info("Selling ??= " + curItem.getAmount() +"  " + curItem);								
//			} else {
//				Log.info("Buying ??= " + curItem.getAmount() +"  " + curItem);				
//			}
//			return;
//		}
//		else if ( curItemEmpty && !cursorItemEmpty){ /// selling case??
//			Log.info("Selling ??= " + cursorItem.getAmount() +"  " + cursorItem);
//			return;			
//		} else {
//			Log.info("  Nothing ");
//			return;
//		}
//		//		MessageController.sendMessage(p, "&c onInv =  item = " + event.getCurrentItem() +"  cursor=" + event.getCursor());
//		//		Bukkit.getScheduler().scheduleSyncDelayedTask(BattleShops.getSelf(), new Runnable(){
//		//			public void run() {
//		//				Inventory i = p.getInventory();
//		//				i.getType();
//		//				Log.info("onRun iteminhand="+ p.getItemInHand() +"  " + p.getItemOnCursor()+")");
//		//				MessageController.sendMessage(p, "&2 onRun iteminhand="+ p.getItemInHand() +"  " + p.getItemOnCursor()+")");
//		//				event.setCancelled(true);
//		//			}
//		//			
//		//		});
//	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.isCancelled()) return;
		final Block clickedBlock = event.getClickedBlock();
		if (clickedBlock == null) return; /// This can happen, minecraft is a strange beast
		final Material clickedMat = clickedBlock.getType();

		/// If this is an uninteresting block get out of here as quickly as we can
		if (!(clickedMat.equals(Material.SIGN) || clickedMat.equals(Material.SIGN_POST) 
				|| 	clickedMat.equals(Material.WALL_SIGN) || clickedMat.equals(Material.CHEST) )) {
			return;
		}

		final Player player = event.getPlayer();

		if (Defaults.DEBUG_TRACE) player.sendMessage("onPlayerInteract  HasPermission= " +
				PermissionController.hasPermissions(player, clickedBlock));

		Action action = event.getAction();
		/// Check to see whether we need to break ownership of this chest with previous owner
		if (clickedBlock.getType().equals(Material.CHEST) ){
			chestClicked(event, player, clickedBlock, action == Action.LEFT_CLICK_BLOCK);
		}

		/// Check to see if we are activating a sign or a chest
		/// This occurs if they are holding a redstone torch and left clicking
		if (event.getItem() != null && event.getItem().getType() == Material.REDSTONE_TORCH_ON &&
				action == Action.LEFT_CLICK_BLOCK){
			boolean hasPermissionToBuild = PermissionController.hasPermissions(player, clickedBlock);
			/// We cant let them link signs/chests where they can't build, otherwise they can "teleport" items
			/// through the use of signs somewhere else
			if (hasPermissionToBuild)
				activateEvent(event, player, clickedBlock);
			return;
		}

		/// We are no longer interested in chest events... leave
		if (clickedBlock.getType().equals(Material.CHEST) ){
			return;
		}          

		/// Check to see if we are performing a left/right click
		if ((action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK)) {
			return;
		}

		/// Find the shopsign the player has clicked on
		Sign sign =null;
		ShopSign ss = null;
		try {
			sign = (Sign) event.getClickedBlock().getState();
			ss = WorldShop.findShopSign(sign);
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
				sv = ShopSign.parseShopSign(sign.getLines());
				boolean isAdminSign = ShopOwner.isAdmin(sign.getLine(0));
				if (sv != null && !isAdminSign){
					sign.setLine(1, sv.quantity + ": U");
					sign.update(true);
				} else if (isAdminSign){
					ShopOwner so = new ShopOwner(Defaults.ADMIN_NAME);
					ss = new ShopSign(so, sign,sv);
					WorldShop.addShopSign(ss);
				}
			} catch (SignFormatException e) {

			}
			if (Defaults.DEBUG_LINKING) System.out.println("failed finding shopsign");
			return;
		}

		if (ss.getOwner().sameOwner(player) && action == Action.RIGHT_CLICK_BLOCK && 
				ConfigController.getBoolean("rightClickSignToOpenChest") ){
			linkController.openChestRemotely(player,ss);
			return;
		}
		/// No sneaking! I like the concept of transactions being public
		if (player.isSneaking()){
			player.sendMessage(MessageController.getMessage("Sneaking"));
			return;
		}

		if (action == Action.RIGHT_CLICK_BLOCK){
			event.setCancelled(true); /// we are now capturing this event so that blocks dont get placed	
		}

		/// Dont let players use their own shop
		ShopOwner so = new ShopOwner(player);
		if (ShopOwner.sameOwner(ss.getOwner(), so) && !PermissionController.isAdmin(player)){
			player.sendMessage(MessageController.getMessage("You_cannot_use_your_own_shop"));
			return;
		}

		/// Probably have a valid shop, dont let them spam
		String playerName = player.getName();
		if(userTime.containsKey(playerName)){
			if((System.currentTimeMillis() - userTime.get(playerName)) < interval){
				player.sendMessage(MessageController.getMessage("wait"));
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
		if (action== Action.LEFT_CLICK_BLOCK){
			Pair<Integer, Integer> mult = userMultiplier.get(player.getName());
			if (mult != null && mult.fst == SELL_INT){
				cancelCommandTimer(player);
				tc.sellToShop(ss,player, mult.snd);	
			} else {
				tc.sellToShop(ss,player, 1);
			}
		} else if (action == Action.RIGHT_CLICK_BLOCK){

			Pair<Integer, Integer> mult = userMultiplier.get(player.getName());
			if (mult != null && mult.fst == BUY_INT){
				cancelCommandTimer(player);
				tc.buyFromShop(ss,player, mult.snd);	
			} else {
				tc.buyFromShop(ss,player, 1);
			}
		}
	}

	private void chestClicked(PlayerInteractEvent event, final Player player, final Block clickedBlock, boolean isLeftClick) {
		Chest chest = (Chest) clickedBlock.getState();
		linkController.chestClick(chest, player,isLeftClick);
	}

	private void activateEvent(PlayerInteractEvent event,final Player player, final Block clickedBlock) {
		if (Defaults.DEBUG_TRACE) System.out.println("RCS::RCSPlayerListener::activateEvent");
		if (clickedBlock.getType().equals(Material.CHEST)){
			Chest chest = (Chest) clickedBlock.getState();
			linkController.activateChestShop(chest, player);
			/// This is a convenience for previously made shops using the chestshop system
			/// In that system the players name is on the first line
			/// This is also convenient if signs get unlinked for some reason
		} else {  
			Sign csign = ((Sign) event.getClickedBlock().getState());
			String[] sLines = csign.getLines();
			boolean isPlayersShopSign = ShopSign.isShopSignOfPlayer(sLines, event.getPlayer(),csign);
			if ( sLines.length < 3 || !isPlayersShopSign) {
				return;
			}
			/// If this sign is already active, just tell them and return, or do something if its an admin
			ShopSign tss = WorldShop.findShopSign(csign);
			if (tss != null){
				boolean isAdminPlayer = PermissionController.isAdmin(player);
				boolean isAdminShop = tss.isAdminShop();
				if (isAdminShop){
					if (isAdminPlayer){
						player.sendMessage("This sign is already active ");	
					} else {
						return;
					}
				} else {

				}
				if (isAdminPlayer && !isAdminShop){
					player.sendMessage("Admins can't link other peoples shops ");
					return;/// Admins cant link other shops
				}	        	
				if (!isAdminPlayer && !isAdminShop){
					Shop s = WorldShop.getShop(csign.getWorld(),new ShopOwner(event.getPlayer()));
					if (s!= null){
						int chestCount = s.getNumChestsAttachedToSign(tss);
						player.sendMessage("This sign already links to " +chestCount  + " " + Util.getChestOrChests(chestCount));
						return;
					}

				}
			}
			/// Else lets make a valid sign
			SignValues sv;
			try {
				sv = ShopSign.parseShopSign(sLines);
			} catch (SignFormatException e) {
				/// We really didnt have an old shop, get out of here
				return;
			}
			if (sv != null){
				Block sign = event.getClickedBlock();
				Sign cs = (Sign) sign.getState();
				ShopOwner so = new ShopOwner(event.getPlayer());
				ShopSign ss = new ShopSign(so, cs,sv);
				boolean isAdminShop = ss.isAdminShop();
				WorldShop.addShopSign(ss);
				if (!isAdminShop){
					WorldShop.updateAffectedSigns(so,ss);		
				} else {
					player.sendMessage("You have activated an admin shop");
				}

			}
		}
	}

	public void setBuyCommand(Player player, int multiplier) {
		if (multiplier < 1 || multiplier > Defaults.MULTIPLIER_LIMIT){
			player.sendMessage("You must specify a multiplier between 1 and " + Defaults.MULTIPLIER_LIMIT);
			return;
		}

		cancelCommandTimer(player);
		userMultiplier.put(player.getName(), new Pair<Integer,Integer>(BUY_INT, multiplier));
		userCommandTime.put(player.getName(), System.currentTimeMillis());
	}
	public void cancelCommandTimer(Player player){
		userMultiplier.remove(player.getName());
		userCommandTime.remove(player.getName());
	}

	public void setSellCommand(Player player, int multiplier) {
		if (multiplier < 1 || multiplier > Defaults.MULTIPLIER_LIMIT){
			player.sendMessage("You must specify a multiplier between 1 and " + Defaults.MULTIPLIER_LIMIT);
			return;
		}
		cancelCommandTimer(player);
		userMultiplier.put(player.getName(), new Pair<Integer,Integer>(SELL_INT, multiplier));			
		userCommandTime.put(player.getName(), System.currentTimeMillis());
	}


	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		WorldShop.onPlayerLogin(event.getPlayer().getName());
	}
}
