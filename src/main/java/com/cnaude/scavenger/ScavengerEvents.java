/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cnaude.scavenger;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

/**
 *
 * @author cnaude
 */
public class ScavengerEvents implements Listener {

    Scavenger plugin;
    RestorationManager rm;

    public ScavengerEvents(Scavenger plugin, RestorationManager restorationManager) {
        this.plugin = plugin;
        this.rm = restorationManager;
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        World fromWorld = event.getFrom();
        plugin.logInfo("Player " + player.getName() + "change world from " + fromWorld.getName() + " to " + world.getName());
        if (plugin.config.blackListWarn()) {
            if (plugin.config.blacklistedWorlds().contains(world.getName().toLowerCase())) {
                plugin.logInfo("B1");
                player.sendMessage(plugin.config.MsgBlacklistedWorld());
            } else {
                plugin.logInfo("B2");
            }
        }
    }
    
}
