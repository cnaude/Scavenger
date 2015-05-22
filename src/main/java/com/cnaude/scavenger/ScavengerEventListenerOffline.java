package com.cnaude.scavenger;

import fr.areku.Authenticator.Authenticator;
import fr.areku.Authenticator.events.PlayerOfflineModeLogin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.World;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class ScavengerEventListenerOffline implements Listener {

    Scavenger plugin;
    RestorationManager rm;
    CopyOnWriteArrayList list;

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
                    if (Authenticator.isPlayerLoggedIn(player)) {
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
        plugin.logDebug("Offline respawn " + event.getPlayer().getName());
        delayedRestore(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerOfflineModeLogin(PlayerOfflineModeLogin event) {
        if ((event.getPlayer() instanceof Player)) {
            plugin.logDebug("Offline login " + event.getPlayer().getName());
            if (isScavengeAllowed(event.getPlayer())) {
                rm.enable(event.getPlayer());
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (rm.hasRestoration(event.getPlayer())) {
            if (Authenticator.isPlayerLoggedIn(event.getPlayer())) {
                //if (isScavengeAllowed(event.getPlayer())) {
                rm.enable(event.getPlayer());
                rm.restore(event.getPlayer());
                //}
            }
        }
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
        if (!Authenticator.isPlayerLoggedIn(player)) {
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

}
