package com.cnaude.scavenger.Hooks;

import com.cnaude.scavenger.Scavenger;
import fr.xephi.authme.api.NewAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 *
 * @author cnaude
 */
public class ScavengerAuthMeReloaded {

    public static NewAPI newAPI;

    public ScavengerAuthMeReloaded(Scavenger plugin) {
        newAPI = NewAPI.getInstance();
    }

    public boolean isAuthenticated(Player player) {
        if (newAPI != null) {
            return newAPI.isAuthenticated(player);
        }
        return false;
    }

}
