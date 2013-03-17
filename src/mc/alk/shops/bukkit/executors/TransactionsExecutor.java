package mc.alk.shops.bukkit.executors;

import java.util.HashMap;
import java.util.List;

import mc.alk.bukkit.util.BukkitInventoryUtil;
import mc.alk.executors.CustomCommandExecutor;
import mc.alk.mc.MCServer;
import mc.alk.shops.BattleShops;
import mc.alk.shops.Defaults;
import mc.alk.shops.bukkit.controllers.BukkitMessageController;
import mc.alk.shops.bukkit.listeners.ShopsSignChestListener;
import mc.alk.shops.controllers.LinkController;
import mc.alk.shops.objects.Transaction;
import mc.alk.shops.serializers.ShopsSerializer;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TransactionsExecutor extends CustomCommandExecutor {
	ShopsSignChestListener playerListener;
	LinkController linkController = null;
	ShopsSerializer sc;
	private HashMap<String, Long> userTime = new HashMap<String, Long>();

	public TransactionsExecutor(ShopsSignChestListener pl, LinkController link, ShopsSerializer sc){
		super();
		playerListener = pl;
		linkController = link;
		this.sc = sc;
	}

	public static String dayOrDays(Integer ndays) {
		return ndays == null || ndays == 1 ? "day" : ndays + " days";
	}

	public static String youOrOtherPlayer(boolean other, String name){
		return other? name : "You";
	}

	public static String fromTo(boolean buying){
		return buying ? "from" : "to";
	}

	protected boolean transactions(CommandSender sender, String name, Integer ndays,
			boolean other, boolean player) {
		if (other){
			if (name.equals(Defaults.ADMIN_STR)){
				name = Defaults.ADMIN_NAME;}
			else if (findOfflinePlayer(name) == null){
				BukkitMessageController.sendMessage(sender,"Player " + name+" can not be found");
				return true;
			}
		}
		if (ndays != null && ndays > 7){
			BukkitMessageController.sendMessage(sender,BukkitMessageController.getMessage("too_many_days"));
			return true;
		}
		if (!sender.isOp() && (sender instanceof Player)){
			final String playerName = sender.getName();
			if(userTime.containsKey(playerName)){
				if((System.currentTimeMillis() - userTime.get(playerName)) < 1000){
					BukkitMessageController.sendMessage(sender,BukkitMessageController.getMessage("wait"));
					return true;
				}
			}
			userTime.put(playerName, System.currentTimeMillis());
		}
		doAsync(sender,name,ndays,other,player);
		return true;
	}

	private void doAsync(CommandSender osender, final String name,
			final Integer ndays, final boolean other, final boolean player) {
		final String senderName = osender.getName();
		final boolean isPlayerSender = osender instanceof Player;

		MCServer.scheduleSyncDelayedTask(BattleShops.getPlugin(), new Runnable(){
			@Override
			public void run() {
				List<Transaction> transactions = null;
				String msg_node = null;
				if (player){
					transactions = sc.getPlayerTransactions(name, ndays);
					msg_node="transaction_list_total";
				} else {
					transactions = sc.getShopTransactions(name, ndays);
					msg_node="shoptransaction_list_total";
				}
				CommandSender sender = null;
				/// Reget our sender
				if (isPlayerSender){
					sender = Bukkit.getPlayerExact(senderName);
					if (sender == null || !((Player)sender).isOnline()){
						return;}
				} else {
					sender = Bukkit.getConsoleSender();
				}

				if (transactions.isEmpty()){
					sender.sendMessage(BukkitMessageController.getMessage("no_transactions_found"));
					return;
				}
				double total_bought = 0;
				double total_sold = 0;
				for (Transaction tr : transactions){
					ItemStack is = BukkitInventoryUtil.getItemStack(tr.itemid, (short)tr.datavalue);
					if (is == null) continue;

					sender.sendMessage(BukkitMessageController.getMessageNP("shoptransaction_list",
							tr.getFormattedDate(), tr.p2 , BukkitMessageController.getBoughtOrSold(tr.buying),
							tr.quantity, BukkitInventoryUtil.getCommonName(is), fromTo(tr.buying),
							youOrOtherPlayer(other,tr.p1),tr.price));
					if (tr.buying){
						total_bought += tr.price;
					} else {
						total_sold += tr.price;
					}
				}
				sender.sendMessage(BukkitMessageController.getMessageNP(msg_node, dayOrDays(ndays), total_sold,total_bought));
			}
		});
	}

}
