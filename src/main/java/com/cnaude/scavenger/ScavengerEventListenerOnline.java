package com.cnaude.scavenger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;

public class ScavengerEventListenerOnline implements Listener {

    Scavenger plugin;
    RestorationManager rm;

    public ScavengerEventListenerOnline(Scavenger plugin, RestorationManager restorationManager) {
        this.plugin = plugin;
        this.rm = restorationManager;
    }

    public void delayedRestore(final Player player) {
        plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                if (rm.hasRestoration(player)) {
                    rm.enable(player);
                    rm.restore(player);
                }
            }
        }, plugin.config.restoreDelayTicks());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        if ((event.getEntity() instanceof Player)) {
            if (isScavengeAllowed(event.getEntity())) {
                rm.collect(event.getEntity(), event.getDrops(), event);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        delayedRestore(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        delayedRestore(event.getPlayer());
    }

    /*
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerMove(PlayerMoveEvent event) {
     if (event.getFrom().distance(event.getTo()) >= 0.5f) {
     if (restorationManager.hasRestoration(event.getPlayer())) {
     restorationManager.restore(event.getPlayer());
     }
     }
     }*/
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        delayedRestore(event.getPlayer());
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
        
        World world = player.getWorld();
        Location location = player.getLocation();
        if (plugin.config.blacklistedWorlds().contains(player.getWorld().getName().toLowerCase())) {
            return false;
        }
        if (plugin.getWorldGuard() != null) {            
            ApplicableRegionSet set = WGBukkit.getRegionManager(world).getApplicableRegions(location);
            for (ProtectedRegion region : set) {
                plugin.logDebug("Region ID: " + region.getId());
                if (plugin.config.blacklistedWGRegions().contains(region.getId())) {
                    plugin.logDebug("Region ID " + region.getId() + " is blacklisted. Dropping items.");
                    return false;
                }
            }
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