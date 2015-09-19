package com.cnaude.scavenger.Hooks;

import fr.areku.Authenticator.Authenticator;
import org.bukkit.entity.Player;

/**
 *
 * @author cnaude
 */
public class ScavengerAuthenticator {

    public boolean isAuthenticated(Player player) {
        return Authenticator.isPlayerLoggedIn(player);
    }
    
}
