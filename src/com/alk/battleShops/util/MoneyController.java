package com.alk.battleShops.util;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.alk.battleShops.BattleShops;
import com.nijikokun.register.payment.Methods;

public class MoneyController implements Listener{
	static boolean hasVault = false;
	static boolean hasRegister = false;
	static boolean useVault = false;
	public static Economy economy = null;

	public static boolean hasAccount(String name) {
		return useVault? economy.hasAccount(name) : Methods.getMethod().hasAccount(name);
	}
	public static boolean hasEnough(String name, float amount,World w) {
		return useVault? economy.getBalance(name) >= amount :Methods.getMethod().getAccount(name).hasEnough(amount);
	}
	public static boolean hasEnough(String name, float amount) {
		return hasEnough(name,amount,null);
	}
	public static void subtract(String name, double amount, World world) {
		subtract(name,(float) amount);
	}
	public static void subtract(String name, float amount) {
		if (useVault) economy.withdrawPlayer(name, amount);
		else Methods.getMethod().getAccount(name).subtract(amount);
	}
	public static void add(String name, float amount, World world) {
		try{
			if (useVault){economy.depositPlayer(name, amount) ;}
			else Methods.getMethod().getAccount(name).add(amount);
		} catch (Exception e){
			Log.err("[BattleShops] Couldnt deposit " + amount +" into " + name+"'s account.  is an economy plugin in?");
			e.printStackTrace();
		}
	}
	public static Double balance(String name,World world) {
		return useVault ? economy.getBalance(name) : Methods.getMethod().getAccount(name).balance();
	}
	public static void add(String name, double amount) {
		add(name,(float)amount);
	}

	public static void setup() {
		checkRegisteredPlugins();
		if (!useVault)
			Bukkit.getServer().getPluginManager().registerEvents(new MoneyController(), BattleShops.getSelf());
	}

	@EventHandler
	public void setup(PluginEnableEvent event) {
		MoneyController.checkRegisteredPlugins();
	}
	private static void checkRegisteredPlugins(){
		if (useVault) /// We are good to go already
			return;
		Plugin controller = Bukkit.getServer().getPluginManager().getPlugin("Register");
		if (controller != null) {
			hasRegister = true;
			if (!hasVault)
				useVault = false;
			Log.info(BattleShops.getVersion() +" found economy plugin Register");
		}
		if (MoneyController.economy == null){ /// We want to use vault if we can
			controller = Bukkit.getServer().getPluginManager().getPlugin("Vault");
			if (controller != null) {
				RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().
						getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
				if (economyProvider == null) {
					Log.err("economyProvider for Vault was null");
				} else {
					MoneyController.economy = economyProvider.getProvider();
					useVault = hasVault = true;
					Log.info(BattleShops.getVersion() +" found economy plugin Vault. [Default]");
				}
			}
		}

	}

}
