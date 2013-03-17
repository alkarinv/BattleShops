package mc.alk.shops.bukkit.executors;


import mc.alk.shops.bukkit.listeners.ShopsSignChestListener;
import mc.alk.shops.controllers.LinkController;
import mc.alk.shops.serializers.ShopsSerializer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


/**
 *
 * @author alkarin
 *
 */
public class BukkitShopTransactionsExecutor extends TransactionsExecutor  {

	public BukkitShopTransactionsExecutor(ShopsSignChestListener pl,
			LinkController link, ShopsSerializer sc){
		super(pl,link,sc);
	}

	@MCCommand()
	public boolean shopTransactions(Player p) {
		return shopTransactions(p, p.getName(), 1, false);
	}
	@MCCommand()
	public boolean shopTransactions(Player p, Integer ndays) {
		return shopTransactions(p, p.getName(), ndays, false);
	}

	@MCCommand(op=true)
	public boolean shopTransactions(CommandSender sender, String player) {
		return shopTransactions(sender, player, 1, true);
	}
	@MCCommand(op=true)
	public boolean shopTransactions(CommandSender sender, String player, Integer ndays) {
		return shopTransactions(sender, player, ndays, true);
	}

	private boolean shopTransactions(CommandSender sender, String player, Integer ndays, boolean other) {
		return transactions(sender, player, ndays, other, false);
	}

}
