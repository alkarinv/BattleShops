package com.alk.battleShops.controllers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.alk.battleShops.Defaults;
import com.alk.battleShops.objects.ChestSet;
import com.alk.battleShops.objects.Shop;
import com.alk.battleShops.objects.ShopChest;
import com.alk.battleShops.objects.ShopOwner;
import com.alk.battleShops.objects.ShopSign;
import com.alk.battleShops.objects.WorldShop;
import com.alk.battleShops.util.InventoryUtil;
import com.alk.battleShops.util.MoneyController;
import com.alk.battleShops.util.MyLogger;


/**
 * This class deals with the buy and sell transactions for the shops
 * @author alkarin
 *
 */
public class TransactionController {
	private HashMap<String, Long> userTime = new HashMap<String, Long>();
	static final int spam_interval = 60000;

	MyLogger logger;
	public TransactionController(MyLogger mylog) {
		this.logger = mylog;
	}

	public void sellToShop(ShopSign ss, Player seller, Integer mult) {
		boolean isAdminShop = ss.isAdminShop();
		if (Defaults.DEBUG_TRACE) System.out.println("sellToShop   admin_shop=" + isAdminShop + "   mult=" + mult);

		ShopOwner so = ss.getOwner();
		Shop shop = WorldShop.getShop(ss.getWorld(), so);
		/// valid shop?
		if (!isAdminShop && shop == null){
			seller.sendMessage("badness shop doesnt exist!");
			return;
		}
		/// Get our chests associated with this shopsign
		Map<Integer, Collection<ShopChest>> chests = shop.getChestsByID(ss.getItemId());

		/// Shop sign actually refers to a chest with this item?
		if (!isAdminShop && noChests(chests)){
			seller.sendMessage(MessageController.getMessage("No_chests_attached", ss.getCommonName()));
			return;
		}
		ChestSet chestset = new ChestSet(chests);

		/// Verify that this shop buys the item
		if (!ss.isBuying()){
			seller.sendMessage(MessageController.getMessage("No_selling_to_this_shop"));
			return;
		}

		final float sellPrice = ss.getSellPrice() * mult;
		final String itemName = ss.getCommonName();
		final int quantity = ss.getQuantity() * mult;
		ItemStack itemStack = ss.getItemStack();
		final World w = ss.getWorld();

		/// Need to set the quantity b/c it could be a multiple of the original amount
		itemStack.setAmount(quantity);

		///
		if (!MoneyController.hasAccount(seller.getName())){
			seller.sendMessage(MessageController.getMessage("Seller_has_no_account"));
			return;
		}

		/// Verify that the seller has what he claims to want to sell
		if( InventoryUtil.getItemAmountFromInventory(seller.getInventory(), itemStack) < quantity){
			seller.sendMessage(MessageController.getMessage("You_have_not_enough_items", quantity));
			return;
		}

		/// Owner Transaction
		if (!isAdminShop){
			/// Verify funds of the shop owner
			if (!MoneyController.hasEnough(so.getName(), sellPrice, w)){
				seller.sendMessage(MessageController.getMessage("owner_has_not_enough_money"));
				return;
			}

			/// Make sure the chest is not full
			if(!chestset.fits(itemStack, quantity)){
				/// Only notify the owner if the original item amount is empty
				if (mult == 1){
					seller.sendMessage(MessageController.getMessage("Chest_is_full",quantity));
					String msg = MessageController.getMessage("Your_shop_is_full",itemName, seller.getName());
					ShopOwner.sendMsgToOwner(so, msg);
				} else {
					seller.sendMessage(MessageController.getMessage("Buyer_cant_fit_this_many",quantity));
				}
				return;
			}

			ShopOwner.sendMsgToOwner(so,MessageController.getMessage("Somebody_sold_items_to_your_shop",
					seller.getName(), quantity, ss.getCommonName(), sellPrice));
			/// Owner Transaction part
			MoneyController.subtract(so.getName(), sellPrice, w);


			Set<ShopChest> affectedChests = new HashSet<ShopChest>();
			Set<Integer> affectedIDs = new HashSet<Integer>();
			chestset.addItem(itemStack, quantity,affectedChests);
			for (ShopChest c : affectedChests){
				affectedIDs.addAll(c.getItemIds());}

			WorldShop.updateAffectedSigns(ss.getWorld(), so, affectedIDs);
			WorldShop.playerShopTransaction(so);
		}
		logger.log(so.getName(), seller.getName(), false, ShopSign.getShopItemID(itemStack), quantity, sellPrice);

		/// Seller Transaction Part
		seller.sendMessage(MessageController.getMessage("You_sold_items",
				quantity, ss.getCommonName(), sellPrice, so));
		MoneyController.add(seller.getName(), sellPrice,w);

		InventoryUtil.removeItem(seller.getInventory(), itemStack);
		seller.sendMessage(MessageController.getMessage("Your_balance", MoneyController.balance(seller.getName(),w)));
	}

	public void buyFromShop(ShopSign ss, Player buyer, Integer mult) {
		boolean isAdminShop = ss.isAdminShop();
		if (Defaults.DEBUG_TRACE) System.out.println("buyFromShop   adminShop=" +isAdminShop + "  mult=" + mult);

		ShopOwner so = ss.getOwner();
		Shop shop = WorldShop.getShop(ss.getWorld(), so);

		/// valid shop?
		if (!isAdminShop && shop == null){
			buyer.sendMessage("badness shop doesnt exist!");
			return;
		}

		/// Get our chests associated with this shopsign
		Map<Integer, Collection<ShopChest>> chests = shop.getChestsByID(ss.getItemId());

		/// Shop sign actually refers to a chest with this item?
		if (!isAdminShop && noChests(chests)){
			buyer.sendMessage(MessageController.getMessage("No_chests_attached",ss.getCommonName()));
			return;
		}

		/// Figure out our name, quantity, item from the sign
		final float buyPrice = ss.getBuyPrice() * mult;
		final String itemName = ss.getCommonName();
		final int quantity = ss.getQuantity() * mult;
		ItemStack itemStack = ss.getItemStack();
		/// Set the amount as we might have a multiple
		itemStack.setAmount(quantity);
		final World w = ss.getWorld();

		///
		if (!MoneyController.hasAccount(buyer.getName())){
			buyer.sendMessage(MessageController.getMessage("Buyer_has_no_account"));
			return;
		}

		/// Verify that this shop sells this item
		if(!ss.isSelling()){
			buyer.sendMessage(MessageController.getMessage("No_buying_from_this_shop"));
			return;
		}

		/// Verify funds of the buyer
		if (!MoneyController.hasEnough(buyer.getName(), buyPrice,w)){
			buyer.sendMessage(MessageController.getMessage("You_have_got_not_enough_money"));
			return;
		}

		/// Check inventory space of the buyer
		if(!InventoryUtil.checkFreeSpace(buyer.getInventory(), itemStack, quantity)){
			buyer.sendMessage(MessageController.getMessage("Your_inventory_is_full"));
			return ;
		}
		if (!isAdminShop){
			ChestSet chestset = new ChestSet(chests);

			/// Verify that the shop chest has the inventory
			if(!chestset.hasEnough(itemStack,quantity)){
				buyer.sendMessage(MessageController.getMessage("Shop_is_out_of_stock",quantity));

				/// Only show out of stock message if multiplier is 1
				if(mult == 1){
					String msg = MessageController.getMessage("Your_shop_is_out_of_stock",itemName, buyer.getName());
					/// Prevent the owner from getting repeatedly spammed by someone
					final String hashkey = so.getName()+":" + itemName;
					if(userTime.containsKey(hashkey)){
						if((System.currentTimeMillis() - userTime.get(hashkey)) > spam_interval){
							ShopOwner.sendMsgToOwner(so, msg);
							userTime.put(hashkey, System.currentTimeMillis());
						}
					} else {
						ShopOwner.sendMsgToOwner(so, msg);
						userTime.put(hashkey, System.currentTimeMillis());
					}
				} else {
					buyer.sendMessage(MessageController.getMessage("Seller_cant_sell_this_many",quantity));
				}
				return;
			}

			ShopOwner.sendMsgToOwner(so,MessageController.getMessage("Somebody_bought_items_from_your_shop",
					buyer.getName(), quantity, itemName, buyPrice) );

			if (Defaults.DEBUG_LINKING) System.out.println(chestset);
			/// Owner is Player Transaction part
			MoneyController.add(so.getName(), buyPrice,w);

			Set<ShopChest> affectedChests = new HashSet<ShopChest>();
			Set<Integer> affectedIDs = new HashSet<Integer>();

			chestset.removeItem(itemStack, quantity,affectedChests);
			for (ShopChest c : affectedChests){
				affectedIDs.addAll(c.getItemIds());}
			//        	WorldShop.updateAffectedSigns(so,ss, cr );
			WorldShop.updateAffectedSigns(ss.getWorld(), so,affectedIDs);
			WorldShop.playerShopTransaction(so);
		}
		logger.log(so.getName(), buyer.getName(), true, ShopSign.getShopItemID(itemStack), quantity, buyPrice);

		buyer.sendMessage(MessageController.getMessage("You_bought_items",
				quantity, itemName, so , buyPrice));

		/// Buyer Transaction Part
		MoneyController.subtract(buyer.getName(), buyPrice,w);
		InventoryUtil.addItemToInventory(buyer, itemStack, quantity);

		buyer.sendMessage(MessageController.getMessage("Your_balance", MoneyController.balance(buyer.getName(),w)));
	}


	private static boolean noChests(Map<Integer, Collection<ShopChest>> chests) {
		if (chests == null || chests.size()==0)
			return true;
		int count = 0;
		for (Integer id : chests.keySet()){
			count += chests.get(id).size();}
		return count <= 0;
	}
}
