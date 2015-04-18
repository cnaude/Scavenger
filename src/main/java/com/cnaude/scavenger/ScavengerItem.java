/*
 * https://github.com/mineGeek/ZoneReset/blob/master/src/com/github/mineGeek/ZoneReset/Data/ScavengerItem.java
 */
package com.cnaude.scavenger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

public class ScavengerItem implements Serializable {

    private static final long serialVersionUID = 9218747208794761041L;
    private final int materialId;
    private final byte data;
    private final short durability;
    private final int amount;
    private Map<String, Object> meta;

    public ScavengerItem(ItemStack i) {

        this.materialId = i.getTypeId();
        this.data = i.getData().getData();
        this.durability = i.getDurability();
        this.amount = i.getAmount();

        if (i.getItemMeta() instanceof ConfigurationSerializable) {
            this.meta = this.getNewMap(((ConfigurationSerializable) i.getItemMeta()).serialize());
        }

    }

    @SuppressWarnings("unchecked")
    public ScavengerItem(Map<String, Object> map) {

        materialId = (Integer) map.get("item");
        data = (Byte) map.get("data");
        durability = (Short) map.get("data");
        amount = (Integer) map.get("amount");
        meta = (Map<String, Object>) map.get("meta");
    }

    public Map<String, Object> getMap() {

        Map<String, Object> map = new HashMap<>();
        map.put("item", materialId);
        map.put("data", data);
        map.put("durability", durability);
        map.put("amount", amount);
        map.put("meta", meta);

        return map;
    }

    public ItemStack getItemStack() {
        ItemStack i = new ItemStack(this.materialId);
        i.setAmount(this.amount);
        i.setDurability(this.durability);
        i.setData(new MaterialData(this.data));
        i.setDurability(this.durability);

        if (this.meta != null && !this.meta.isEmpty()) {
            i.setItemMeta((ItemMeta) ConfigurationSerialization.deserializeObject(this.meta, ConfigurationSerialization.getClassByAlias("ItemMeta")));
        }

        return i;

    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getNewMap(Map<String, Object> map) {
        Map<String, Object> newMap = new HashMap<>();
        if (!map.isEmpty()) {
            for (String x : map.keySet()) {
                Object value = map.get(x);
                if (value instanceof Map) {
                    value = getNewMap((Map<String, Object>) value);
                }
                newMap.put(x, value);
            }
        }
        return newMap;
    }

}
