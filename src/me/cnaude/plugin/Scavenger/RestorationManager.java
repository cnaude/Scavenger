package me.cnaude.plugin.Scavenger;

import java.util.HashMap;
import java.util.List;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RestorationManager {
    private static HashMap<String, Restoration> restorations = new HashMap<String, Restoration>();    
    
    public static boolean hasRestoration(Player _player) {
        return restorations.containsKey(_player.getName());
    }

    public static void collect(Scavenger plug, Player _player, List<ItemStack> _drops) {
        if (hasRestoration(_player)) {
            if (plug.getSConfig().shouldNotify()) {
                plug.getCommunicationManager().error(_player, "Restoration already exists, ignoring.");
            }
            return;
        }

        if(plug.getEconomy() != null && !(_player.hasPermission("scavenger.free"))) {
             double fee = plug.getSConfig().getFee();
                        EconomyResponse er = plug.getEconomy().withdrawPlayer(_player.getName(), fee);
                        if(er.transactionSuccess()) {
                            if (plug.getSConfig().shouldNotify()) {
                                plug.getCommunicationManager().message(_player, "Saved your inventory for a small fee of "+fee);
                            }
                        } else {
                            if (plug.getSConfig().shouldNotify()) {
                                plug.getCommunicationManager().message(_player, "You do not have enough money to save your inventory.");
                            }
                            return;
                        }
        }
        if (plug.getSConfig().shouldNotify()) {
            plug.getCommunicationManager().message(_player, "Saving your inventory.");
        }
        Restoration restoration = new Restoration();

        restoration.enabled = false;

        restoration.inventory = _player.getInventory().getContents();
        restoration.armour = _player.getInventory().getArmorContents();

        restorations.put(_player.getName(), restoration);

        _drops.clear();
    }

    public static void enable(Player _player) {
        if (hasRestoration(_player)) {
            Restoration restoration = restorations.get(_player.getName());
            restoration.enabled = true;
        }
    }

    public static void restore(Scavenger plug, Player _player) {
        if (hasRestoration(_player)) {
            Restoration restoration = restorations.get(_player.getName());
            
            if (restoration.enabled) {
                _player.getInventory().clear();
                
                _player.getInventory().setContents(restoration.inventory);
                _player.getInventory().setArmorContents(restoration.armour);

                //if (Scavenger.get().getSConfig().shouldNotify()) {}
                //    Scavenger.get().getCommunicationManager().message(_player, "Your inventory has been restored.");
                if (plug.getSConfig().shouldNotify()) {}
                    plug.getCommunicationManager().message(_player, "Your inventory has been restored.");

                restorations.remove(_player.getName());
            }
        }
    }
}
