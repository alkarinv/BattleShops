package mc.alk.shops.bukkit.executors;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import mc.alk.bukkit.BukkitPlayer;
import mc.alk.executors.CustomCommandExecutor;
import mc.alk.shops.BattleShops;
import mc.alk.shops.Defaults;
import mc.alk.shops.bukkit.controllers.BukkitMessageController;
import mc.alk.shops.bukkit.listeners.ShopsSignChestListener;
import mc.alk.shops.controllers.LinkController;
import mc.alk.shops.controllers.Shop;
import mc.alk.shops.controllers.ShopController;
import mc.alk.shops.objects.ShopSign;
import mc.alk.shops.serializers.ShopsSerializer;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


/**
 *
 * @author alkarin
 *
 */
public class BukkitShopsExecutor extends CustomCommandExecutor  {
	Map<String, Long> timers = new HashMap<String, Long>();

	ShopsSignChestListener playerListener;
	LinkController linkController = null;
	ShopsSerializer sc ;
	public BukkitShopsExecutor(ShopsSignChestListener pl, LinkController link,ShopsSerializer sc){
		super();
		playerListener = pl;
		linkController = link;
		this.sc = sc;
	}

	@MCCommand(cmds={"listother"}, perm=Defaults.PERM_ADMIN)
	public void shopList(CommandSender sender, OfflinePlayer player) {
		Shop shop = ShopController.getShop(null,player.getName());
		BukkitMessageController.sendMessage(sender, "&eShop = " + shop);

		Collection<ShopSign> signs = shop.getSigns();
		for (ShopSign sign: signs){
			BukkitMessageController.sendMessage(sender, "&eSign = &6" + sign + " # chests =" + shop.getNumChestsAttachedToSign(sign));
		}
		ShopController.printAll();
	}

	@MCCommand(cmds={"remove"})
	public void removeAssociate(Player p1, OfflinePlayer p2) {
		Shop s = ShopController.getShop(new BukkitPlayer(p1));
		s.removeFromAssociates(p2.getName());
		BattleShops.getShopSerializer().deleteAssociate(p1.getName(), p2.getName());
		p1.sendMessage(BukkitMessageController.getMessage("removed_associate", p2.getName()));
	}

	@MCCommand(cmds={"add"})
	public void addAssociate(Player p1, OfflinePlayer p2) {
		Shop s = ShopController.getShop(new BukkitPlayer(p1));
		s.addToAssociates(p2.getName());
		p1.sendMessage(BukkitMessageController.getMessage("added_associate", p2.getName()));
	}

	@MCCommand(cmds={"list"})
	public void listAssociates(Player sender) {
		Shop shop = ShopController.getShop(new BukkitPlayer(sender));
		if (shop == null)
			return;
		Set<String> as = shop.getAssociates();
		if (as != null){
			sender.sendMessage(BukkitMessageController.getMessage("list_associates_header"));
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (String p : as){
				if (first){
					first = false;
					sb.append(p);
				} else {
					sb.append("," + p); }
			}
			sender.sendMessage(sb.toString());
		} else {
			sender.sendMessage(BukkitMessageController.getMessage("no_associates"));
		}
	}

	private int getMultiplier(String text){
		int multiplier;
		try {
			String s = text.replaceAll("[Xx]", "");
			float f = Float.valueOf(s);
			if (f <1) return -1;
			multiplier = (int) Math.ceil(f);
			return multiplier;
		} catch (NumberFormatException e){
			return -1;
		}
	}

	@MCCommand(cmds={"buy"}, min=2, usage="buy <amount>x : Example: shop buy 32x")
	public boolean buyCommand(Player sender, String[] args) {
		if (args[1].endsWith("x") || args[1].endsWith("X")){
			int multiplier = getMultiplier(args[1]);
			playerListener.setBuyCommand(new BukkitPlayer(sender), multiplier);
			return true;
		} else {
			return false;
		}
	}

	@MCCommand(cmds={"sell"}, min=2, usage="sell <amount>x : Example: shop sell 32x")
	public boolean sellCommand(Player sender, String[] args) {
		if (args[1].endsWith("x") || args[1].endsWith("X")){
			int multiplier = getMultiplier(args[1]);
			playerListener.setSellCommand(new BukkitPlayer(sender), multiplier);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void showHelp(CommandSender sender, Command command){
		sender.sendMessage(BukkitMessageController.getMessage("show_help_buy_multiple"));
		sender.sendMessage(BukkitMessageController.getMessage("show_help_sell_multiple"));
		sender.sendMessage(BukkitMessageController.getMessage("show_help_add_associate"));
		sender.sendMessage(BukkitMessageController.getMessage("show_help_remove_associate"));
		sender.sendMessage(BukkitMessageController.getMessage("show_help_associate_list"));
	}
}
