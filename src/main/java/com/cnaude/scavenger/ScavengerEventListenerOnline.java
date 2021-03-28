package com.cnaude.scavenger;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class ScavengerEventListenerOnline implements Listener {

    Scavenger plugin;
    RestorationManager rm;
    CopyOnWriteArrayList list;

    public ScavengerEventListenerOnline(Scavenger plugin, RestorationManager restorationManager) {
        this.plugin = plugin;
        this.rm = restorationManager;
        this.list = new CopyOnWriteArrayList<>();
    }

    public void delayedRestore(final Player player) {
        final String playerName = player.getName();
        if (list.contains(playerName)) {
            plugin.logDebug("Delayed restore for " + playerName + " called before previous completed. Aborting.");
            return;
        }
        plugin.logDebug("Delayed restore for " + playerName);
        list.add(playerName);
        plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                if (rm.hasRestoration(player)) {
                    plugin.logDebug("Player " + playerName + " has a restore. Initiating restore.");
                    rm.enable(player);
                    rm.restore(player);
                } else {
                    plugin.logDebug("Player " + playerName + " has NO restore. Nothing to restore.");
                }
                if (list.contains(playerName)) {
                    list.remove(playerName);
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
        plugin.logDebug("Player respawn " + player.getName());
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
        plugin.logDebug("Player " + player.getName() + " changed from " + fromWorld.getName() + " to " + world.getName());
        if (plugin.config.blackListWarn()) {
            if (plugin.config.blacklistedWorlds().contains(world.getName().toLowerCase())) {
                plugin.logDebug("Blacklisted world: " + world.getName());
                player.sendMessage(plugin.config.MsgBlacklistedWorld());
                return;
            } else {
                plugin.logDebug("Non-blacklisted world: " + world.getName());
            }
        }
        delayedRestore(player);
    }

    private boolean isScavengeAllowed(Player player) {
        if (!plugin.config.isScavengerEnabled()) {
            plugin.logDebug("Scavenger is disabled. Not saving inventory for " + player.getDisplayName());
            return false;
        }
        
        World world = player.getWorld();
        String worldName = world.getName().toLowerCase();
        
        if (plugin.config.whitelistedWorlds().contains(world.getName().toLowerCase())) {
            plugin.logDebug("[WhiteListedWorld]: Player: " + player + " World: " + worldName);
            return true;
        }
        
        String dcString = "NULL";
        if (player.getLastDamageCause() != null) {
            if (player.getLastDamageCause().getCause() != null) {
                dcString = player.getLastDamageCause().getCause().toString();
            }
        }
        
        plugin.logDebug("[isScavengeAllowed]: Player: " + player + " World: " + worldName + " DamageCause: " + dcString);

        Location location = player.getLocation();
        if (plugin.config.blacklistedWorlds().contains(worldName)) {
            plugin.logDebug("[isScavengeAllowed]: Blacklisted world: " + worldName);
            return false;
        }
        if (plugin.getWorldGuard() != null) {
            try {
                RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(player.getWorld()));
                if (regionManager != null) {
                    ApplicableRegionSet set = regionManager.getApplicableRegions(BukkitAdapter.asVector(location).toBlockPoint());
                    for (ProtectedRegion region : set) {
                        plugin.logDebug("[isScavengeAllowed]: Region ID: " + region.getId());
                        if (plugin.config.blacklistedWGRegions().contains(region.getId())) {
                            plugin.logDebug("[isScavengeAllowed]: Region ID " + region.getId() + " is blacklisted. Dropping items.");
                            return false;
                        }
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
            plugin.logDebug("[isScavengeAllowed]: Permissions are disabled. Enabling restore for " + player.getName());
            return true;
        }

        if (hasPermission(player, "scavenger.scavenge")) {
            return true;
        }
        if (hasPermission(player, "scavenger.exp")) {
            return true;
        }
        if (hasPermission(player, "scavenger.level")) {
            return true;
        }
        if (player.hasPermission("scavenger.scavenge." + dcString)) {
            return true;
        }
        if (hasPermission(player, "scavenger.inv")) {
            return true;
        }
        if (hasPermission(player, "scavenger.armour")) {
            return true;
        }
        if (player.isOp() && plugin.config.opsAllPerms()) {
            plugin.logDebug("[isScavengeAllowed]: Player " + player.getName() + " is op and ops have all permissions.");
            return true;
        }
        plugin.logDebug("[isScavengeAllowed]: No scavenge will occur.");
        return false;
    }

    private boolean hasPermission(Player player, String perm) {
        boolean b = player.hasPermission(perm);
        plugin.logDebug("[isScavengeAllowed]: " + player.getName() + " : " + perm + " : " + b);
        return b;
    }

}
