package com.cnaude.scavenger.Hooks;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.groups.GroupManager;

/**
 *
 * @author cnaude
 */
public class ScavengerPerWorldInventory {
    private final GroupManager manager;
    PerWorldInventory plugin;
    
    public ScavengerPerWorldInventory(PerWorldInventory plugin) {
        this.plugin = plugin;
        this.manager = plugin.getGroupManager();
    }
    
    public String getLocationName(String world) {
        return manager.getGroupFromWorld(world).getName();
    }
    
}
