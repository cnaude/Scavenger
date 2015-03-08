package com.cnaude.scavenger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class ScavengerEventListenerOnline implements Listener {

    Scavenger plugin;
    RestorationManager rm;

    public ScavengerEventListenerOnline(Scavenger plugin, RestorationManager restorationManager) {
        this.plugin = plugin;
        this.rm = restorationManager;
    }

    public void delayedRestore(final Player player) {
        plugin.logDebug("Delayed restore for " + player.getName());
        plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                if (rm.hasRestoration(player)) {
                    plugin.logDebug("Player " + player.getName() + " has a restore. Initiating restore.");
                    rm.enable(player);
                    rm.restore(player);
                } else {
                    plugin.logDebug("Player " + player.getName() + " has NO restore. Nothing to restore.");
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
        Player player = event.getPlayer();
        plugin.logInfo("Player respawn " + player.getName());
        delayedRestore(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        plugin.logDebug("Player teleport " + player.getName());
        if (player.isDead()) {
            plugin.logDebug("Dead teleport! " + player.getName());
            return;
        }
        delayedRestore(event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        World fromWorld = event.getFrom();
        plugin.logInfo("Player " + player.getName() + " changed from " + fromWorld.getName() + " to " + world.getName());
        if (plugin.config.blackListWarn()) {
            if (plugin.config.blacklistedWorlds().contains(world.getName().toLowerCase())) {
                plugin.logInfo("Blacklisted world: " + world.getName());
                player.sendMessage(plugin.config.MsgBlacklistedWorld());
                return;
            } else {
                plugin.logInfo("Non-blacklisted world: " + world.getName());
            }
        }
        delayedRestore(player);
    }

    private boolean isScavengeAllowed(Player player) {
        if (!plugin.config.isScavengerEnabled()) {
            plugin.logDebug("Scavenger is disabled. Not saving inventory for " + player.getDisplayName());
            return false;
        }
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
            try {                
                ApplicableRegionSet set = WGBukkit.getRegionManager(world).getApplicableRegions(location);
                for (ProtectedRegion region : set) {
                    plugin.logDebug("Region ID: " + region.getId());
                    if (plugin.config.blacklistedWGRegions().contains(region.getId())) {
                        plugin.logDebug("Region ID " + region.getId() + " is blacklisted. Dropping items.");
                        return false;
                    }
                }
            } catch (NullPointerException ex) {
                plugin.logDebug(ex.getMessage());
            }
        }
        if (ScavengerIgnoreList.isIgnored(player.getName())) {
            return false;
        }
        if (!plugin.config.permsEnabled()) {
            plugin.logDebug("Permissions are disabled. Enabling restore for " + player.getName());
            return true;
        }
        if (player.hasPermission("scavenger.scavenge")) {
            plugin.logDebug("Player " + player.getName() + " has " + "scavenger.scavenge");
            return true;
        }
        if (player.hasPermission("scavenger.exp")) {
            plugin.logDebug("Player " + player.getName() + " has " + "scavenger.exp");
            return true;
        }
        if (player.hasPermission("scavenger.level")) {
            plugin.logDebug("Player " + player.getName() + " has " + "scavenger.level");
            return true;
        }
        String dcPerm = "scavenger.scavenge." + dcString;
        if (player.hasPermission(dcPerm)) {
            plugin.logDebug("Player " + player.getName() + " has " + dcPerm);
            return true;
        } else {
            plugin.logDebug("Player " + player.getName() + " does NOT have " + dcPerm);
        }
        if (player.hasPermission("scavenger.inv")) {
            plugin.logDebug("Player " + player.getName() + " has " + "scavenger.inv");
            return true;
        }
        if (player.hasPermission("scavenger.armour")) {
            plugin.logDebug("Player " + player.getName() + " has " + "scavenger.armour");
            return true;
        }
        if (player.isOp() && plugin.config.opsAllPerms()) {
            plugin.logDebug("Player " + player.getName() + " is op and ops have all permissions.");
            return true;
        }
        plugin.logDebug("Returning false.");
        return false;
    }

    

}
