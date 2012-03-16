package me.cnaude.plugin.Scavenger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class ScavengerEventListener implements Listener { 
    private final Scavenger plugin;
    
    public ScavengerEventListener(Scavenger Instance) {
        plugin = Instance;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        if ((event.getEntity() instanceof Player)) {
            Player player = (Player)event.getEntity();
            if (player.hasPermission("scavenger.scavenge")) {
                RestorationManager.collect(plugin, (Player)event.getEntity(), event.getDrops());
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        RestorationManager.enable(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent event) {
        RestorationManager.restore(plugin, event.getPlayer());
    }   
}