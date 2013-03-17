package mc.alk.shops.controllers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mc.alk.controllers.MoneyController;
import mc.alk.mc.MCItemStack;
import mc.alk.mc.MCPlayer;
import mc.alk.mc.MCWorld;
import mc.alk.shops.Defaults;
import mc.alk.shops.bukkit.controllers.BukkitMessageController;
import mc.alk.shops.objects.ChestSet;
import mc.alk.shops.objects.ShopChest;
import mc.alk.shops.objects.ShopOwner;
import mc.alk.shops.objects.ShopSign;
import mc.alk.shops.utils.KeyUtil;



/**
 * This class deals with the buy and sell transactions for the shops
 * @author alkarin
 *
 */
public class TransactionController {
	private HashMap<String, Long> userTime = new HashMap<String, Long>();
	static final int spam_interval = 60000;
	TransactionLogger logger;

	public TransactionController(TransactionLogger transactionLogger) {
		this.logger = transactionLogger;
	}

	/**
	 * Sell the given item to the shop
	 * @param ShopSign
	 * @param Seller
	 * @param mult
	 * @return true or false based on whether the transaction completed
	 */
	public boolean sellToShop(ShopSign ss, MCPlayer seller, Integer mult) {
		boolean isAdminShop = ss.isAdminShop();
		if (Defaults.DEBUG_TRACE) System.out.println("sellToShop admin_shop=" + isAdminShop + "   mult=" + mult +"  item=" +ss.getItemStack());

		ShopOwner so = ss.getOwner();
		Shop shop = ShopController.getShop(ss.getWorld(), so);
		/// valid shop?
		if (!isAdminShop && shop == null){
			seller.sendMessage("badness shop doesnt exist!");
			return false;
		}
		/// Get our chests associated with this shopsign
		Map<Long, Collection<ShopChest>> chests = shop.getChestsByID(KeyUtil.toKey(ss.getItemStack()));

		/// Shop sign actually refers to a chest with this item?
		if (!isAdminShop && noChests(chests)){
			seller.sendMessage(BukkitMessageController.getMessage("No_chests_attached", ss.getCommonName()));
			return false;
		}
		ChestSet chestset = new ChestSet(chests);

		/// Verify that this shop buys the item
		if (!ss.isBuying()){
			seller.sendMessage(BukkitMessageController.getMessage("No_selling_to_this_shop"));
			return false;
		}

		final float sellPrice = ss.getSellPrice() * mult;
		final String itemName = ss.getCommonName();
		final int quantity = Math.max(1,ss.getQuantity() * mult);
		MCItemStack itemStack = ss.getItemStack().clone();

		final MCWorld w = ss.getWorld();

		/// Need to set the quantity b/c it could be a multiple of the original amount
		itemStack.setQuantity(quantity);

		if (!MoneyController.hasAccount(seller.getName())){
			seller.sendMessage(BukkitMessageController.getMessage("Seller_has_no_account"));
			return false;
		}

		/// Verify that the seller has what he claims to want to sell
		if(seller.getInventory().getItemAmount(itemStack) < quantity){
			seller.sendMessage(BukkitMessageController.getMessage("You_have_not_enough_items", quantity));
			return false;
		}

		/// Owner Transaction
		if (!isAdminShop){
			/// Verify funds of the shop owner
			if (!MoneyController.hasEnough(so.getName(), sellPrice, w.getName())){
				seller.sendMessage(BukkitMessageController.getMessage("owner_has_not_enough_money"));
				return false;
			}

			/// Make sure the chest is not full
			if(!chestset.fits(itemStack, quantity)){
				/// Only notify the owner if the original item amount is empty
				if (mult == 1){
					seller.sendMessage(BukkitMessageController.getMessage("Chest_is_full",quantity));
					String msg = BukkitMessageController.getMessage("Your_shop_is_full",itemName, seller.getName());
					BukkitMessageController.sendMessage(so, msg);
				} else {
					seller.sendMessage(BukkitMessageController.getMessage("Buyer_cant_fit_this_many",quantity));
				}
				return false;
			}

			BukkitMessageController.sendMessage(so,BukkitMessageController.getMessage("Somebody_sold_items_to_your_shop",
					seller.getName(), quantity, itemName, sellPrice));
			/// Owner Transaction part
			MoneyController.subtract(so.getName(), sellPrice, w.getName());


			Set<ShopChest> affectedChests = new HashSet<ShopChest>();
			Set<MCItemStack> affectedIDs = new HashSet<MCItemStack>();
			chestset.addItem(itemStack, quantity,affectedChests);
			for (ShopChest c : affectedChests){
				affectedIDs.addAll(c.getItemIds());}

			ShopController.updateAffectedSigns(ss.getWorld(), so, affectedIDs);
			ShopController.playerShopTransaction(so);
		}
		logger.log(so.getName(), seller.getName(), false, itemStack.getType(), itemStack.getDataValue(),
				quantity, sellPrice);

		/// Seller Transaction Part
		seller.sendMessage(BukkitMessageController.getMessage("You_sold_items",
				quantity, itemName, sellPrice, so));
		MoneyController.add(seller.getName(), sellPrice,w.getName());

		seller.getInventory().removeItem(itemStack);
		seller.updateInventory();

		seller.sendMessage(BukkitMessageController.getMessage("Your_balance", MoneyController.balance(seller.getName(),w.getName())));
		return true;
	}

	/**
	 * Buy the given item from the shop
	 * @param ShopSign
	 * @param Buyer
	 * @param mult
	 * @return true or false based on whether the transaction completed
	 */
	public boolean buyFromShop(ShopSign ss, MCPlayer buyer, Integer mult) {
		boolean isAdminShop = ss.isAdminShop();
		if (Defaults.DEBUG_TRACE) System.out.println("buyFromShop   adminShop=" +isAdminShop + "  mult=" + mult);

		ShopOwner so = ss.getOwner();
		Shop shop = ShopController.getShop(ss.getWorld(), so);

		/// valid shop?
		if (!isAdminShop && shop == null){
			buyer.sendMessage("badness shop doesnt exist!");
			return false;
		}

		/// Get our chests associated with this shopsign
		Map<Long, Collection<ShopChest>> chests = shop.getChestsByID(KeyUtil.toKey(ss.getItemStack()));

		final String itemName = ss.getCommonName();

		/// Shop sign actually refers to a chest with this item?
		if (!isAdminShop && noChests(chests)){
			buyer.sendMessage(BukkitMessageController.getMessage("No_chests_attached",itemName));
			return false;
		}

		/// Figure out our name, quantity, item from the sign
		final float buyPrice = ss.getBuyPrice() * mult;
		final int quantity = Math.max(1,ss.getQuantity() * mult);
		MCItemStack itemStack = ss.getItemStack().clone();
		/// Set the amount as we might have a multiple
		itemStack.setQuantity(quantity);

		final MCWorld w = ss.getWorld();

		///
		if (!MoneyController.hasAccount(buyer.getName())){
			buyer.sendMessage(BukkitMessageController.getMessage("Buyer_has_no_account"));
			return false;
		}

		/// Verify that this shop sells this item
		if(!ss.isSelling()){
			buyer.sendMessage(BukkitMessageController.getMessage("No_buying_from_this_shop"));
			return false;
		}
		/// Verify funds of the buyer
		if (!MoneyController.hasEnough(buyer.getName(), buyPrice, w.getName())){
			buyer.sendMessage(BukkitMessageController.getMessage("You_have_got_not_enough_money"));
			return false;
		}

		/// Check inventory space of the buyer

		if(!buyer.getInventory().canFit(itemStack)){
			buyer.sendMessage(BukkitMessageController.getMessage("Your_inventory_is_full"));
			return false;
		}
		if (!isAdminShop){
			ChestSet chestset = new ChestSet(chests);

			/// Verify that the shop chest has the inventory
			if(!chestset.hasEnough(itemStack,quantity)){
				buyer.sendMessage(BukkitMessageController.getMessage("Shop_is_out_of_stock",quantity));

				/// Only show out of stock message if multiplier is 1
				if(mult == 1){
					String msg = BukkitMessageController.getMessage("Your_shop_is_out_of_stock",itemName, buyer.getName());
					/// Prevent the owner from getting repeatedly spammed by someone
					final String hashkey = so.getName()+":" + itemName;
					if(userTime.containsKey(hashkey)){
						if((System.currentTimeMillis() - userTime.get(hashkey)) > spam_interval){
							BukkitMessageController.sendMessage(so, msg);
							userTime.put(hashkey, System.currentTimeMillis());
						}
					} else {
						BukkitMessageController.sendMessage(so, msg);
						userTime.put(hashkey, System.currentTimeMillis());
					}
				} else {
					buyer.sendMessage(BukkitMessageController.getMessage("Seller_cant_sell_this_many",quantity));
				}
				return false;
			}

			BukkitMessageController.sendMessage(so,BukkitMessageController.getMessage("Somebody_bought_items_from_your_shop",
					buyer.getName(), quantity, itemName, buyPrice) );

			if (Defaults.DEBUG_LINKING) System.out.println(chestset);
			/// Owner is Player Transaction part
			MoneyController.add(so.getName(), buyPrice,w.getName());

			Set<ShopChest> affectedChests = new HashSet<ShopChest>();
			Set<MCItemStack> affectedItems = new HashSet<MCItemStack>();

			chestset.removeItem(itemStack, quantity,affectedChests);
			for (ShopChest c : affectedChests){
				affectedItems.addAll(c.getItemIds());}
			ShopController.updateAffectedSigns(ss.getWorld(), so,affectedItems);
			ShopController.playerShopTransaction(so);
		}
		logger.log(so.getName(), buyer.getName(), true, itemStack.getType(),itemStack.getDataValue(),
				quantity, buyPrice);

		buyer.sendMessage(BukkitMessageController.getMessage("You_bought_items",
				quantity, itemName, so , buyPrice));

		/// Buyer Transaction Part
		MoneyController.subtract(buyer.getName(), buyPrice,w.getName());
		buyer.getInventory().addItem(itemStack);
		buyer.updateInventory();
//		InventoryUtil.addItemToInventory(buyer, itemStack, quantity);

		buyer.sendMessage(BukkitMessageController.getMessage("Your_balance", MoneyController.balance(buyer.getName(),w.getName())));
		return true;
	}


	private static boolean noChests(Map<Long, Collection<ShopChest>> chests) {
		if (chests == null || chests.size()==0)
			return true;
		int count = 0;
		for (Long id : chests.keySet()){
			count += chests.get(id).size();}
		return count <= 0;
	}
}
