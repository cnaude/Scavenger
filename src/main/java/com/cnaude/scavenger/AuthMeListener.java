package com.cnaude.scavenger;

import fr.xephi.authme.events.LoginEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 *
 * @author cnaude
 */
public class AuthMeListener implements Listener {
    
    public Scavenger plugin;
    
    public AuthMeListener(Scavenger plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerOfflineModeLogin(LoginEvent event) {
        plugin.eventListenerOffline.playerLoggedOn(event.getPlayer());        
    }
}
