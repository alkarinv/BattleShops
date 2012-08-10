package com.alk.battleShops.controllers;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.alk.battleShops.BattleShops;
import com.alk.battleShops.Defaults;
import com.alk.battleShops.Serializers.BCSStorageController;
import com.alk.battleShops.listeners.BCSPlayerListener;
import com.alk.battleShops.objects.PlayerActivity;
import com.alk.battleShops.objects.Shop;
import com.alk.battleShops.objects.ShopSign;
import com.alk.battleShops.objects.Transaction;
import com.alk.battleShops.objects.WorldShop;
import com.alk.battleShops.util.Util;

/**
 * 
 * @author alkarin
 *
 */
public class BCSExecutor  {
	Map<String, Long> timers = new HashMap<String, Long>();

	BCSPlayerListener playerListener;
	LinkController linkController = null;
	BCSStorageController sc ;
	public BCSExecutor(BCSPlayerListener pl, LinkController link){
		playerListener = pl;
		linkController = link;
	}
	public void setStorageController(BCSStorageController sc){this.sc = sc;}
	public boolean handleCommand(CommandSender sender, Command cmd,String commandLabel, String[] args) {
		Player player = null;
		if (sender instanceof Player)
			player = (Player) sender;

		String commandStr = cmd.getName().toLowerCase();
		for (String arg: args){
			if (!arg.matches("[a-zA-Z0-9_]*")) {
				player.sendMessage(ChatColor.YELLOW + "arguments can be only alphanumeric with underscores");
				return true;
			}
			if (Defaults.DEBUG_TRACE) System.out.println("arguments =" + arg);
		}
		if (commandStr.equalsIgnoreCase("shopclean") && PermissionController.isAdmin(player)){
			int num = CleanSignController.clean(player.getLocation());
			player.sendMessage("Cleaning " + num + " signs");
		}
		if (commandStr.equalsIgnoreCase("shoptransactions")){
			return shopTranscations(sender, player,args);
		}
		if (commandStr.equalsIgnoreCase("playertransactions")){
			return playerTransactions(sender, player,args);
		}

		/// Shop command
		if (commandStr.equalsIgnoreCase("shop")){
			if (args.length == 0){
				return showHelp(sender, player);
			} else if (args.length ==1 && args[0].equalsIgnoreCase("list")){
				listAssociates(sender, player);
				return true;
			} else if (args[0].equalsIgnoreCase("listall")){					
				shopList(sender, player, args);
				return true;
			} else  if (args.length >= 2){
				/// First check for buy/sell commands
				if (args[0].equalsIgnoreCase("buy") || args[0].equalsIgnoreCase("sell")){
					return buyOrSellCommand(sender, player, args);
				} else if (args[0].equalsIgnoreCase("add")){
					addAssociate(sender, player, args);
					return true;
				} else if (args[0].equalsIgnoreCase("remove")){					
					removeAssociate(sender, player, args);
					return true;
				}
			}
		} else if (commandStr.equalsIgnoreCase("link")){
			if (args.length == 1){
			}
			return false;
		} else if (commandStr.equalsIgnoreCase("unlink")){
			if (args.length ==1){				
			}
			return false;
		} else if (commandStr.equalsIgnoreCase("shopactive") && PermissionController.isAdmin(player)){
			if (args.length == 1){
				return displayPlayerActivity(sender, player, args[0]);
			} else {
				return displayPlayerActivity(sender, player, null);
			}
		} else if (commandStr.equalsIgnoreCase("shopactive")){
			return true;
		}
		return false;
	}
	private void shopList(CommandSender sender, Player player, String[] args) {
		if (args.length > 1){
			
		}
		Shop shop = WorldShop.getShop(player);
		MessageController.sendMessage(sender, "&eShop = " + shop);
		
		Collection<ShopSign> signs = shop.getSigns();
		for (ShopSign sign: signs){
			MessageController.sendMessage(sender, "&eSign = &6" + sign + " # chests =" + shop.getNumChestsAttachedToSign(sign));
		}
	}
	private String fromTo(boolean buying){
		return buying ? "from" : "to";
	}
	private boolean playerTransactions(CommandSender sender, Player p, String[] args) {
		Integer ndays = null;
		if (args.length > 0){
			try {ndays = Integer.valueOf(args[0]);} catch (Exception e){}
		}

		if (args.length > 0 && !PermissionController.isAdmin(p) && ndays == null){
			p.sendMessage(Util.colorChat("&eYou don't have permissions to see other players transactions"));
			p.sendMessage(Util.colorChat("&eType &6/ptr &e to see your own"));
			return true;
		}
		String name = null;
		if (args.length >0 && ndays == null){
			name = args[0];
			int ndays_index = 1;
			if (args.length > 1 && name.equalsIgnoreCase("ADMIN")){
				if (args[1].equalsIgnoreCase("SHOP")){
					ndays_index = 2;
					name = "ADMIN SHOP";
				}
			}
			try {ndays = Integer.valueOf(args[ndays_index]);} catch (Exception e){}
		} else {
			name = p.getName();
		}
		if (ndays != null && ndays > 7){
			p.sendMessage(Util.colorChat("&eYou can't see more than 7 days worth of transactions"));
			return true;
		}

		List<Transaction> transactions = sc.getPlayerTransactions(name, ndays);
		if (transactions.isEmpty()){
			p.sendMessage(Util.colorChat("No Transactions Found"));
			return true;
		}
		double total_bought = 0;
		double total_sold = 0;
		for (Transaction tr : transactions){
			p.sendMessage(MessageController.getMessageNP("transaction_list",
					tr.getFormattedDate(), MessageController.getBoughtOrSold(tr.buying),
					tr.quantity, ShopSign.getCommonName(tr.itemid), fromTo(tr.buying), tr.p1,tr.price));
			if (tr.buying){
				total_bought += tr.price;
			} else {
				total_sold += tr.price;
			}
		}
		p.sendMessage(MessageController.getMessageNP("transaction_list_total",dayOrDays(ndays), total_bought, total_sold));
		return true;
	}

	private String dayOrDays(Integer ndays) {
		return ndays == null || ndays == 1 ? "day" : ndays + " days";
	}
	private boolean shopTranscations(CommandSender sender, Player p, String[] args) {
		Integer ndays = null;

		boolean other = false;
		if (args.length > 0){
			try {ndays = Integer.valueOf(args[0]);} catch (Exception e){}
		}
		if (args.length > 0 && !PermissionController.isAdmin(p) && ndays == null){
			p.sendMessage(Util.colorChat("&eYou don't have permissions to see other shops transactions"));
			p.sendMessage(Util.colorChat("&eType &6/ptr &e to see your own"));
			return true;
		}
		String name = null;
		if (args.length >0 && ndays == null){
			name = args[0];
			int ndays_index = 1;
			if (args.length > 1 && name.equalsIgnoreCase("ADMIN")){
				if (args[1].equalsIgnoreCase("SHOP")){
					ndays_index = 2;
					name = "ADMIN SHOP";
				}
			}
			other = true;
			try {ndays = Integer.valueOf(args[ndays_index]);} catch (Exception e){}
		} else {
			name = p.getName();
		}
		if (ndays != null && ndays > 7){
			p.sendMessage(Util.colorChat("&eYou can't see more than 7 days worth of transactions"));
			return true;
		}
		List<Transaction> transactions = sc.getShopTransactions(name, ndays);

		if (transactions.isEmpty()){
			p.sendMessage(Util.colorChat("No Transactions Found"));
			return true;
		}
		double total_bought = 0;
		double total_sold = 0;
		for (Transaction tr : transactions){
			p.sendMessage(MessageController.getMessageNP("shoptransaction_list",
					tr.getFormattedDate(), tr.p2 , MessageController.getBoughtOrSold(tr.buying),
					tr.quantity, ShopSign.getCommonName(tr.itemid), fromTo(tr.buying), 
					youOrOtherPlayer(other,tr.p1),tr.price));
			if (tr.buying){
				total_bought += tr.price;
			} else {
				total_sold += tr.price;
			}
		}
		p.sendMessage(MessageController.getMessageNP("shoptransaction_list_total", dayOrDays(ndays), total_sold,total_bought));
		return true;
	}
	private static String youOrOtherPlayer(boolean other, String name){
		return other? name : "You";
	}

	private void removeAssociate(CommandSender sender, Player player, String[] args) {
		Shop s = WorldShop.getShop(player);
		s.removeFromAssociates(args[1]);
		BattleShops.getStorageController().deleteAssociate(player.getName(), args[1]);
		player.sendMessage("You have removed " + args[1] + " to your chest permissions");
	}

	private void addAssociate(CommandSender sender, Player player, String[] args) {
		Shop s = WorldShop.getShop(player);
		s.addToAssociates(args[1]);
		player.sendMessage("You have added " + args[1] + " to your chest permissions");
	}

	private void listAssociates(CommandSender sender, Player player) {
		Shop shop = WorldShop.getShop(player);
		if (shop == null)
			return;
		Set<String> as = shop.getAssociates();
		if (as != null){
			player.sendMessage("The following people are added to your chest permissions");
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (String p : as){
				if (first){
					first = false;
					sb.append(p);
				} else { 
					sb.append("," + p); }
			}
			player.sendMessage(sb.toString());
		} else {
			player.sendMessage("You have no one added to your chest permissions");
		}
	}

	private boolean showHelp(CommandSender sender, Player player) {
		player.sendMessage(Util.colorChat("&e/shop buy <multiple>x :&f Example /shop buy 64x "));
		player.sendMessage(Util.colorChat("&e/shop sell <multiple>x :&f Example /shop sell 64x"));
		player.sendMessage(Util.colorChat("&e/shop add <name> :&f Add player to your chest permissions "));
		player.sendMessage(Util.colorChat("&e/shop remove <name> :&f Remove player from chest permissions "));
		player.sendMessage(Util.colorChat("&e/shop list :&fList who has permission to your chests"));
		return true;
	}

	private boolean displayPlayerActivity(CommandSender sender, Player player, String string) {
		if (string == null){
			Map<String,PlayerActivity> pas = WorldShop.getPlayerActivity();
			List<PlayerActivity> list = new ArrayList<PlayerActivity>();
			list.addAll(pas.values());
			Collections.sort(list);
			for (int i=0;i< 10 && i < list.size();i++){
				PlayerActivity pa = list.get(i);
				sendPlayerActivityMessage(sender, player, pa);
			}
		} else {
			PlayerActivity pa = WorldShop.getPlayerActivity(string);
			sendPlayerActivityMessage(sender, player, pa);
		}
		return true;
	}

	private void sendPlayerActivityMessage(CommandSender sender, Player player, PlayerActivity pa) {
		//		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd hh:mm");
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
		Calendar cal = new GregorianCalendar();  cal.setTimeInMillis(pa.lastPlayerLogin);
		Calendar cal2 = new GregorianCalendar();  cal2.setTimeInMillis(pa.lastShopUpdate);
		Calendar cal3 = new GregorianCalendar();  cal3.setTimeInMillis(pa.lastShopTransaction);
		String str1 = pa.lastPlayerLogin > 0 ? sdf.format(cal.getTime()) : "";
		String str2 = pa.lastShopUpdate > 0 ? sdf.format(cal2.getTime()) : "";
		String str3 = pa.lastShopTransaction > 0 ? sdf.format(cal3.getTime()) : "";
		player.sendMessage(pa.name + "[Login: " + str1 + "][ShopUpdt: " + str2 
				+ "][ShopTrc: " + str3 + "]");
	}

	private boolean buyOrSellCommand(CommandSender sender, Player player, String[] args) {
		/// our command should look like 'shop buy <integer>x' example 'shop buy 32x'
		if (args[1].endsWith("x") || args[1].endsWith("X")){
			int multiplier;
			try {
				String s = args[1].replaceAll("[Xx]", "");
				float f = Float.valueOf(s);
				if (f <1) return false;
				multiplier = (int) Math.ceil(f);
			} catch (NumberFormatException e){
				return false;
			}
			if (args[0].equalsIgnoreCase("buy")){
				playerListener.setBuyCommand(player, multiplier);
			} else if (args[0].equalsIgnoreCase("sell")){
				playerListener.setSellCommand(player, multiplier);
			}
			return true;
		}
		return false;
	}


}
