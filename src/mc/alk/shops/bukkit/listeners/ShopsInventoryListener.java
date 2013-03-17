package mc.alk.shops.bukkit.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import mc.alk.bukkit.BukkitItemStack;
import mc.alk.bukkit.BukkitLocation;
import mc.alk.bukkit.BukkitPlayer;
import mc.alk.mc.MCItemStack;
import mc.alk.mc.MCLocation;
import mc.alk.mc.MCPlayer;
import mc.alk.mc.MCServer;
import mc.alk.shops.BattleShops;
import mc.alk.shops.controllers.LinkController;
import mc.alk.shops.controllers.ShopController;
import mc.alk.shops.controllers.TransactionController;
import mc.alk.shops.objects.ShopChest;
import mc.alk.shops.objects.ShopOwner;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;


/**
 *
 * @author alkarin
 *
 */
public class ShopsInventoryListener implements Listener  {
	public static final int BUY_INT = 0;
	public static final int SELL_INT = 1;

	private ConcurrentHashMap<String,MCLocation> checkClicked = new ConcurrentHashMap<String,MCLocation>();

//	private final LinkController linkController;
//	private final TransactionController tc;
	public ShopsInventoryListener(LinkController link, TransactionController tc){
//		linkController = link;
//		this.tc = tc;
	}

	public void closePlayerInventory(Player p){
		MCLocation loc = checkClicked.remove(p.getName());
		if (loc != null){
			ShopChest sc = ShopController.getShopChest(loc);
			if (sc == null)
				return;

			ShopController.updateAffectedSigns(loc.getWorld(), sc.getOwner(), sc);
		}
	}

	@EventHandler
	public void onInventoryOpenEvent(final InventoryOpenEvent event){
		Inventory inventory = event.getView().getTopInventory();
		if(inventory.getType() != InventoryType.CHEST){
			return;}
		final InventoryHolder ih = inventory.getHolder();
		/// the ih can really be null?? obviously it can in some cases.  so now I must check for it
		if (ih == null || !(ih instanceof DoubleChest || ih instanceof Chest))
			return;

		final MCLocation cloc =  new BukkitLocation(
				(ih instanceof DoubleChest) ?  ((DoubleChest) ih).getLocation() : ((Chest) ih).getLocation());

		ShopChest sc = ShopController.getShopChest(cloc);
		if (sc == null)
			return;
		checkClicked.put(event.getPlayer().getName(), cloc);
	}

	@EventHandler
	public void onInventoryClickEvent(final InventoryClickEvent event){
		if (!checkClicked.containsKey(event.getWhoClicked().getName()))
			return;
		InventoryView iv = event.getView();
		if (iv.getTopInventory().getType() != InventoryType.CHEST ){
			return;}

		final InventoryHolder ih = iv.getTopInventory().getHolder();
		if (ih == null)
			return;

		MCItemStack curItem = new BukkitItemStack(event.getCurrentItem());
		MCItemStack cursorItem = new BukkitItemStack(event.getCursor());
		boolean shiftClick = event.isShiftClick();
		SlotType st = event.getSlotType();

		final int endChestSlots = ih instanceof DoubleChest ? 53 : 26; /// 54 -1, and 27 -1
		boolean clickPlayerInventory = st==SlotType.QUICKBAR ||
				(event.getInventory().getType() == InventoryType.PLAYER) ||
				(event.getRawSlot() > endChestSlots && iv.getBottomInventory().getType()==InventoryType.PLAYER);
		if (clickPlayerInventory && !shiftClick){
			return;
		}
		boolean curItemEmpty = curItem == null || curItem.getType() == Material.AIR.getId();
		boolean cursorItemEmpty = cursorItem == null || cursorItem.getType() == Material.AIR.getId();
		final MCPlayer p = new BukkitPlayer((Player) event.getWhoClicked());
		final ShopOwner so = new ShopOwner(p.getName());
		final List<MCItemStack> items = new ArrayList<MCItemStack>();
		if (!curItemEmpty){
			items.add(curItem);}
		if (!cursorItemEmpty){
			items.add(cursorItem);}
		MCServer.scheduleSyncDelayedTask(BattleShops.getPlugin(), new Runnable(){
			public void run() {
				ShopController.updateAffectedSigns(p.getWorld(), so, items);
			}
		});
	}

	@EventHandler
	public void onInventoryCloseEvent(final InventoryCloseEvent event){
		Inventory inventory = event.getView().getTopInventory();
		if(inventory.getType() != InventoryType.CHEST){
			return;}
		final InventoryHolder ih = inventory.getHolder();
		/// the ih can really be null?? obviously it can in some cases.  so now I must check for it
		if (ih == null || !(ih instanceof DoubleChest || ih instanceof Chest))
			return;

		final MCLocation cloc =  new BukkitLocation(
				(ih instanceof DoubleChest) ?  ((DoubleChest) ih).getLocation() : ((Chest) ih).getLocation());
		ShopChest sc = ShopController.getShopChest(cloc);
		if (sc == null)
			return;

		checkClicked.remove(event.getPlayer().getName());
		ShopController.updateAffectedSigns(cloc.getWorld(), sc.getOwner(), sc);
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		ShopController.onPlayerLogin(event.getPlayer().getName());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		closePlayerInventory(event.getPlayer());
	}
	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		closePlayerInventory(event.getPlayer());
	}
}
