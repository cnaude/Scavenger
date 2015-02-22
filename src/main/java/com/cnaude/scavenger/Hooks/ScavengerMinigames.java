/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cnaude.scavenger.Hooks;

import com.cnaude.scavenger.Scavenger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author cnaude
 */
public class ScavengerMinigames {
    
    private final Plugin minigamesPlugin;
    
    public ScavengerMinigames(Scavenger plugin) {
        minigamesPlugin = plugin.getServer().getPluginManager().getPlugin("Minigames");
    }
        
    public boolean playerInMinigame(Player player) {
        if (minigamesPlugin.getDescription().getMain().contains("com.pauldavdesign.mineauz.minigames.Minigames")) {
            return ((com.pauldavdesign.mineauz.minigames.Minigames)minigamesPlugin).getPlayerData().playerInMinigame(player);
        } else if (minigamesPlugin.getDescription().getMain().contains("au.com.mineauz.minigames.Minigames")) {
            return ((au.com.mineauz.minigames.Minigames)minigamesPlugin).getPlayerData().playerInMinigame(player);
        }
        return false;
    }
}
