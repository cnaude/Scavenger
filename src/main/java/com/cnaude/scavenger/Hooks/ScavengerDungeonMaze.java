/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cnaude.scavenger.Hooks;

import com.timvisee.dungeonmaze.api.DungeonMazeAPI;
import org.bukkit.entity.Player;

/**
 *
 * @author cnaude
 */
public class ScavengerDungeonMaze {           
    public boolean isPlayerInDungeon(Player player) {
        return (DungeonMazeAPI.isInDMWorld(player));
    }
}
