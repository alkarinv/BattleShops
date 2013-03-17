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
public class BukkitPlayerTransactionsExecutor extends TransactionsExecutor  {
	public BukkitPlayerTransactionsExecutor(ShopsSignChestListener pl,
			LinkController link, ShopsSerializer sc){
		super(pl,link,sc);
	}

	@MCCommand()
	public boolean playerTransactions(Player p) {
		return playerTransactions(p, p.getName(), 1, false);
	}

	@MCCommand()
	public boolean playerTransactions(Player p, Integer ndays) {
		return playerTransactions(p, p.getName(), ndays, false);
	}

	@MCCommand(op=true)
	public boolean playerTransactions(CommandSender sender, String player ) {
		return playerTransactions(sender, player, 1, true);
	}

	@MCCommand(op=true)
	public boolean playerTransactions(CommandSender sender, String player, Integer ndays) {
		return playerTransactions(sender, player, ndays, true);
	}

	private boolean playerTransactions(CommandSender sender, String player, Integer ndays, boolean other) {
		return transactions(sender, player, ndays, other, false);
	}

}
