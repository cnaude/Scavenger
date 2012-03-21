package me.cnaude.plugin.Scavenger;

import java.util.HashMap;
import java.util.List;
import net.milkbowl.vault.economy.EconomyResponse;
import net.slipcor.pvparena.api.PVPArenaAPI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RestorationManager {
    private static HashMap<String, Restoration> restorations = new HashMap<String, Restoration>();    
    
    public static boolean hasRestoration(Player _player) {
        return restorations.containsKey(_player.getName());
    }

    public static void collect(Scavenger plug, Player _player, List<ItemStack> _drops) {
        if (Scavenger.maHandler != null && Scavenger.maHandler.isPlayerInArena(_player)) { 
            if (plug.getSConfig().shouldNotify()) {
                plug.getCommunicationManager().message(_player, "You are inside an arena. Scavenger will not save your inventory.");
            }
            return;
        } 
        
        if (Scavenger.pvpHandler != null && !PVPArenaAPI.getArenaName(_player).equals("")) { 
            if (plug.getSConfig().shouldNotify()) {
                plug.getCommunicationManager().message(_player, "You are inside PVP arena "+PVPArenaAPI.getArenaName(_player)+". Scavenger will not save your inventory.");
            }
            return;
        } 
        
        if (hasRestoration(_player)) {
            if (plug.getSConfig().shouldNotify()) {
                plug.getCommunicationManager().error(_player, "Restoration already exists, ignoring.");
            }
            return;
        }

        if(plug.getEconomy() != null && !(_player.hasPermission("scavenger.free"))) {
            double restore_cost = plug.getSConfig().restoreCost(); 
            double withdraw_amount;
            double player_balance = plug.getEconomy().getBalance(_player.getName());
            double percent_cost = plug.getSConfig().percentCost();
            double min_cost = plug.getSConfig().minCost();
            EconomyResponse er;
            String currency;
            if (plug.getSConfig().percent()) {                                
                withdraw_amount = player_balance * (percent_cost / 100.0);                
                if (plug.getSConfig().addMin()) {
                    withdraw_amount = withdraw_amount + min_cost;
                } else if (withdraw_amount < min_cost) {
                    withdraw_amount = min_cost;
                }
            } else {
                withdraw_amount = restore_cost;
            }                        
            er = plug.getEconomy().withdrawPlayer(_player.getName(), withdraw_amount);
            if(er.transactionSuccess()) {
                if (withdraw_amount == 1) {
                   currency = plug.getEconomy().currencyNameSingular();                
                } else {
                    currency = plug.getEconomy().currencyNamePlural();
                }
                if (plug.getSConfig().shouldNotify()) {
                    plug.getCommunicationManager().message(_player, "Saved your inventory for a small fee of "+withdraw_amount+ " "+currency+".");
                }
            } else {
                if (player_balance == 1) {
                   currency = plug.getEconomy().currencyNameSingular();                
                } else {
                    currency = plug.getEconomy().currencyNamePlural();
                }
                if (plug.getSConfig().shouldNotify()) {
                    plug.getCommunicationManager().message(_player, "Item recovery is "+withdraw_amount+" and you only have "+player_balance+" "+currency+".");
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
                
                if (plug.getSConfig().shouldNotify()) {
                    plug.getCommunicationManager().message(_player, "Your inventory has been restored.");
                }
                restorations.remove(_player.getName());
            }
        }
    }
}
