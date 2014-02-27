/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cnaude.scavenger.Hooks;

import com.bergerkiller.bukkit.mw.MyWorlds;
import com.bergerkiller.bukkit.mw.WorldConfig;
import org.bukkit.Location;

/**
 *
 * @author cnaude
 */
public class ScavengerMyWorlds {

    public boolean isEnabled() {
        try {
            return MyWorlds.useWorldInventories;
        } catch (Throwable t) {
            // Nothing (not enabled)
        }
        return false;
    }

    public String getLocationName(Location location) {
        if (location == null) {
            // Null check (why can the location be null? Oh well.)
            return null;
        } else if (this.isEnabled()) {
            // Get the world configuration for that world, and the inventory container world name of that
            return WorldConfig.get(location).inventory.getSharedWorldName();
        } else {
            // Fall back to using the world name
            return location.getWorld().getName();
        }
    }
}
