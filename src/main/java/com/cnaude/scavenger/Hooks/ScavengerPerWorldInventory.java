package com.cnaude.scavenger.Hooks;

import me.ebonjaeger.perworldinventory.PerWorldInventory;
import me.ebonjaeger.perworldinventory.api.PerWorldInventoryAPI;

/**
 *
 * @author cnaude
 */
public class ScavengerPerWorldInventory {

    PerWorldInventory plugin;
    PerWorldInventoryAPI pwiApi;

    public ScavengerPerWorldInventory(PerWorldInventory plugin) {
        this.plugin = plugin;
        pwiApi = plugin.getApi();
    }

    public String getLocationName(String world) {
        return pwiApi.getGroupFromWorld(world).getName();
    }

}
