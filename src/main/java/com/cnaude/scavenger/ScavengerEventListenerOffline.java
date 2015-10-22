package com.cnaude.scavenger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.World;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class ScavengerEventListenerOffline implements Listener {

    Scavenger plugin;
    RestorationManager rm;
    CopyOnWriteArrayList list;
    int onMoveCount = 0;

    public ScavengerEventListenerOffline(Scavenger plugin, RestorationManager restorationManager) {
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
                    if (plugin.isAuthenticated(player)) {
                        if (isScavengeAllowed(player)) {
                            rm.enable(player);
                            rm.restore(player);
                        } else {
                            plugin.logDebug("Player " + playerName + " has NO restore. Nothing to restore.");
                        }
                        if (list.contains(playerName)) {
                            list.remove(playerName);
                        }
                    }
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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (onMoveCount >= 20) {
            if (rm.hasRestoration(event.getPlayer())) {
                if (plugin.isAuthenticated(event.getPlayer())) {
                    rm.enable(event.getPlayer());
                    rm.restore(event.getPlayer());
                }
            }
            onMoveCount = 0;
        } else {
            onMoveCount++;
        }
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
        if (!plugin.isAuthenticated(player)) {
            plugin.logDebug("[isScavengeAllowed]: Player is not logged in " + player.getName());
            return false;
        }
        if (plugin.config.blacklistedWorlds().contains(player.getWorld().getName().toLowerCase())) {
            return false;
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

    public void playerLoggedOn(Player player) {
        plugin.logDebug("Player logged on " + player.getName());
        if (isScavengeAllowed(player)) {
            rm.enable(player);
        }
    }

}
