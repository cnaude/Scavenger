package me.cnaude.plugin.Scavenger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class ScavengerEventListener implements Listener { 
   
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        RestorationManager.enable(event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {        
        if ((event.getEntity() instanceof Player)) {            
            Player player = (Player)event.getEntity();
            if (ScavengerIgnoreList.isIgnored(player.getName()))
                return;                 
            if (player.hasPermission("scavenger.scavenge") 
                    || !Scavenger.getSConfig().permsEnabled() 
                    || (player.isOp() && Scavenger.getSConfig().opsAllPerms())) {
                    RestorationManager.collect((Player)event.getEntity(), event.getDrops(), event);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        RestorationManager.enable(event.getPlayer());
        //RestorationManager.restore(plugin, event.getPlayer());
    } 
    /*
    @EventHandler (priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent event){
        RestorationManager.enable(event.getPlayer());
    }
    */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent event) {
        //RestorationManager.enable(event.getPlayer());
        RestorationManager.restore(event.getPlayer());
    }
    

}