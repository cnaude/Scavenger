package com.cnaude.scavenger.Hooks;

import me.gnat008.perworldinventory.PerWorldInventory;
import me.gnat008.perworldinventory.api.PerWorldInventoryAPI;

/**
 *
 * @author cnaude
 */
public class ScavengerPerWorldInventory {

    PerWorldInventory plugin;
    PerWorldInventoryAPI pwiApi;

    public ScavengerPerWorldInventory(PerWorldInventory plugin) {
        this.plugin = plugin;
        pwiApi = plugin.getAPI();
    }

    public String getLocationName(String world) {
        return pwiApi.getGroupFromWorld(world).getName();
    }

}
