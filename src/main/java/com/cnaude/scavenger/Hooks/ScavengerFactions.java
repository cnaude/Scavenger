package com.cnaude.scavenger.Hooks;

import com.cnaude.scavenger.Scavenger;
import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.BoardColls;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.UPlayer;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.entity.Player;

/**
 *
 * @author cnaude
 */
public class ScavengerFactions {
    
    private final Scavenger plugin;
    
    public ScavengerFactions(Scavenger plugin) {
        this.plugin = plugin;
    }

    public boolean isPlayerInEnemyFaction(Player player) {
        try {
            UPlayer uplayer = UPlayer.get(player);
            Faction faction = BoardColls.get().getFactionAt(PS.valueOf(player.getLocation()));
            Rel rel = faction.getRelationTo(uplayer);
            plugin.logDebug("Relation: " + rel.name());
            if (rel.equals(Rel.ENEMY)) {
                plugin.logDebug("Player '" + player.getName() + "' is inside enemy territory!");
                plugin.message(player, plugin.config.msgInsideEnemyFaction());
                return true;
            }
        } catch (NoSuchMethodError ex) {
            plugin.logDebug("ERROR: " + ex.getMessage());
        }
        return false;
    }
}
