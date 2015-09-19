package com.cnaude.scavenger;

import fr.areku.Authenticator.events.PlayerOfflineModeLogin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 *
 * @author cnaude
 */
public class AuthenticatorListener implements Listener {
    
    public Scavenger plugin;
    
    public AuthenticatorListener(Scavenger plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerOfflineModeLogin(PlayerOfflineModeLogin event) {
        plugin.eventListenerOffline.playerLoggedOn(event.getPlayer());        
    }
}
