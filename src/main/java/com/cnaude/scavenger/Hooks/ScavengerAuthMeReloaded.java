package com.cnaude.scavenger.Hooks;

import com.cnaude.scavenger.Scavenger;
import fr.xephi.authme.AuthMe;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 *
 * @author cnaude
 */
public class ScavengerAuthMeReloaded {

    public static AuthMe authMe;

    public ScavengerAuthMeReloaded(Scavenger plugin) {
        authMe = (AuthMe) Bukkit.getServer().getPluginManager().getPlugin("AuthMe");
    }

    public boolean isAuthenticated(Player player) {
        if (authMe != null) {
            return authMe.api.isAuthenticated(player);
        }
        return false;
    }

}
