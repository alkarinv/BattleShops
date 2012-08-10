package com.alk.battleShops.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import com.alk.battleShops.BattleShops;
import com.alk.battleShops.Defaults;
import com.alk.battleShops.controllers.PermissionController;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

/**
 * 
 * @author alkarin
 *
 */
public class BCSPluginListener implements Listener {

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
		if (Defaults.DEBUG_TRACE) System.out.println("onPluginEnable");

        ///World Guard
        if (PermissionController.worldGuard == null) {
            Plugin wg = BattleShops.getBukkitServer().getPluginManager().getPlugin("WorldGuard");

            if (wg != null) {
                PermissionController.worldGuard = (WorldGuardPlugin) wg;
                PluginDescriptionFile pDesc = wg.getDescription();
                System.out.println("[" + BattleShops.getPluginName() + "] "+ pDesc.getName() +
                		" version " + pDesc.getVersion() + " loaded.");
            }
        }
    }
    
}
