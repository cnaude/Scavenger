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

public class ScavengerEventListenerOnline implements Listener {

    Scavenger plugin;
    RestorationManager rm;

    public ScavengerEventListenerOnline(Scavenger plugin, RestorationManager restorationManager) {
        this.plugin = plugin;
        this.rm = restorationManager;
    }

    public void delayedRestore(final Player player) {
        plugin.debugMessage("Delayed restore for " + player.getName());
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
            return true;
        }
        if (player.hasPermission("scavenger.scavenge")) {
            return true;
        }
        if (player.hasPermission("scavenger.exp")) {
            return true;
        }
        if (player.hasPermission("scavenger.level")) {
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
            return true;
        }
        if (player.hasPermission("scavenger.armour")) {
            return true;
        }
        return (player.isOp() && plugin.config.opsAllPerms());
    }
}
