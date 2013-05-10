package me.cnaude.plugin.Scavenger;

import fr.areku.Authenticator.Authenticator;
import fr.areku.Authenticator.events.PlayerOfflineModeLogin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class ScavengerEventListenerOffline implements Listener {

    Scavenger plugin;
    RestorationManager rm;
    
    public ScavengerEventListenerOffline(Scavenger plugin, RestorationManager restorationManager) {
        this.plugin = plugin;
        this.rm = restorationManager;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        if ((event.getEntity() instanceof Player)) {
            if (isScavengeAllowed(event.getEntity())) {
                rm.collect(event.getEntity(), event.getDrops(), event);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        if (rm.hasRestoration(event.getPlayer())) {
            rm.enable(event.getPlayer());
            plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    rm.restore(player);
                }
            }, plugin.config.restoreDelayTicks());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerOfflineModeLogin(PlayerOfflineModeLogin event) {
        if ((event.getPlayer() instanceof Player)) {
            if (isScavengeAllowed(event.getPlayer())) {
                rm.enable(event.getPlayer());
            }
        }
    }

    /*
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (isScavengeAllowed(event.getPlayer())) {
            rm.restore(event.getPlayer());
        }

    }*/
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {        
        if (rm.hasRestoration(event.getPlayer())) {
            rm.restore(event.getPlayer());
        }
    }

    private boolean isScavengeAllowed(Player player) {
        String dcString = "NULL";
        if (player.getLastDamageCause() != null) {
            if (player.getLastDamageCause().getCause() != null) {
                dcString = player.getLastDamageCause().getCause().toString();
            }
        }
        plugin.logDebug("Player: " + player + "World: " 
                + player.getWorld().getName().toLowerCase() + " DamageCause: " + dcString);        
        if (!Authenticator.isPlayerLoggedIn(player)) {
            return false;
        }
        if (plugin.config.blacklistedWorlds().contains(player.getWorld().getName().toLowerCase())) {
            return false;
        }
        if (ScavengerIgnoreList.isIgnored(player.getName())) {
            return false;
        }
        if (!plugin.config.permsEnabled()) {
            return true;
        }
        if (player.hasPermission("scavenger.scavenge")) {
            return true;
        }
        if (player.hasPermission("scavenger.scavenge." + dcString)) {
            return true;
        }
        if (player.hasPermission("scavenger.inv")) {
            return true;
        }
        if (player.hasPermission("scavenger.armour")) {
            return true;
        }
        if ((player.isOp() && plugin.config.opsAllPerms())) {
            return true;
        }
        return false;
    }
}