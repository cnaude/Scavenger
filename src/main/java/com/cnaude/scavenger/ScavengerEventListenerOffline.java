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

public class ScavengerEventListenerOffline implements Listener {

    Scavenger plugin;
    RestorationManager rm;

    public ScavengerEventListenerOffline(Scavenger plugin, RestorationManager restorationManager) {
        this.plugin = plugin;
        this.rm = restorationManager;
    }

    public void delayedRestore(final Player player) {
        plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                if (rm.hasRestoration(player)) {
                    if (Authenticator.isPlayerLoggedIn(player)) {
                        //rm.enable(player);
                        if (isScavengeAllowed(player)) {
                            rm.enable(player);
                            rm.restore(player);
                        }
                    }
                }
            }
        }, plugin.config.restoreDelayTicks());
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
        if (player.hasPermission("scavenger.exp")) {
            return true;
        }
        if (player.hasPermission("scavenger.level")) {
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
        return (player.isOp() && plugin.config.opsAllPerms());
    }
    
}
