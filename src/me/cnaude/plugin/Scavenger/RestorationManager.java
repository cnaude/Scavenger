package me.cnaude.plugin.Scavenger;

import java.util.HashMap;
import java.util.List;
import net.milkbowl.vault.economy.EconomyResponse;
import net.slipcor.pvparena.api.PVPArenaAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class RestorationManager {
    private static HashMap<String, Restoration> restorations = new HashMap<String, Restoration>();    
    
    public static boolean hasRestoration(Player _player) {
        return restorations.containsKey(_player.getName());
    }

    public static void collect(Scavenger plug, Player _player, List<ItemStack> _drops, EntityDeathEvent event) {
        //plug.logDebug("collect(): "+_player.getDisplayName());
        if (_drops.isEmpty() && _player.getExp() == 0 && _player.getLevel() == 0) {
            return;
        }
        if (Scavenger.maHandler != null && Scavenger.maHandler.isPlayerInArena(_player)) { 
            if (plug.getSConfig().shouldNotify()) {
                plug.message(_player, "You are inside an arena. Scavenger will not save your inventory.");
                //plug.logDebug("Inside MobArena: "+_player.getDisplayName());
            }
            return;
        }         
        
        if (Scavenger.pvpHandler != null && !PVPArenaAPI.getArenaName(_player).equals("")) { 
            if (plug.getSConfig().shouldNotify()) {
                plug.message(_player, String.format("You are inside PVP arena %s. Scavenger will not save your inventory.",PVPArenaAPI.getArenaName(_player)));
                //plug.logDebug("Inside PVP Arena: "+_player.getDisplayName());                
            }
            return;
        } 
        
        if (hasRestoration(_player)) {
            if (plug.getSConfig().shouldNotify()) {
                plug.error(_player, "Restoration already exists, ignoring.");
                //plug.logDebug("Restoration already exists: "+_player.getDisplayName());                
            }
            return;
        }

        if(plug.getEconomy() != null && !(_player.hasPermission("scavenger.free")) && plug.getSConfig().economyEnabled()) {
            //plug.logDebug("Scavenging for a fee: "+_player.getDisplayName());
            
            double restore_cost = plug.getSConfig().restoreCost(); 
            double withdraw_amount;
            double player_balance = plug.getEconomy().getBalance(_player.getName());
            double percent_cost = plug.getSConfig().percentCost();
            double min_cost = plug.getSConfig().minCost();
            double max_cost = plug.getSConfig().maxCost();
            EconomyResponse er;
            String currency;
            if (plug.getSConfig().percent()) {                                
                withdraw_amount = player_balance * (percent_cost / 100.0);                
                if (plug.getSConfig().addMin()) {
                    withdraw_amount = withdraw_amount + min_cost;
                } else if (withdraw_amount < min_cost) {
                    withdraw_amount = min_cost;
                } 
                if (withdraw_amount > max_cost && max_cost > 0) {
                    withdraw_amount = max_cost;
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
                    plug.message(_player, String.format("Saving your inventory for a small fee of %.2f %s.",withdraw_amount,currency));                    
                    //plug.logDebug("Player has enough money for recovery: "+_player.getDisplayName());                
                }
            } else {
                if (player_balance == 1) {
                   currency = plug.getEconomy().currencyNameSingular();                
                } else {
                    currency = plug.getEconomy().currencyNamePlural();
                }
                if (plug.getSConfig().shouldNotify()) {
                    plug.message(_player, String.format("Item recovery cost is %.2f and you only have %.2f %s.",withdraw_amount,player_balance,currency));
                    //plug.logDebug("Not enough money to recover items: "+_player.getDisplayName());                
                }
                return;
            }
        } else {
            if (plug.getSConfig().shouldNotify()) {
                plug.message(_player, "Saving your inventory.");
                //plug.logDebug("Scavenging for free: "+_player.getDisplayName());                
            }
        }
        Restoration restoration = new Restoration();

        restoration.enabled = false;

        restoration.inventory = _player.getInventory().getContents();
        restoration.armour = _player.getInventory().getArmorContents();
        if (_player.hasPermission("scavenger.level")) {            
            restoration.level = _player.getLevel();
        }
        if (_player.hasPermission("scavenger.exp")) {            
            restoration.exp = _player.getExp();
            event.setDroppedExp(0);
        }

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
                //plug.logDebug("Clearing player inventory: "+_player.getDisplayName());                
                _player.getInventory().clear();

                //plug.logDebug("Recovering player inventory: "+_player.getDisplayName());
                _player.getInventory().setContents(restoration.inventory);
                _player.getInventory().setArmorContents(restoration.armour);
                if (_player.hasPermission("scavenger.level")) {
                    _player.setLevel(restoration.level);
                    //plug.logDebug("Recovering level: "+_player.getDisplayName());                
                }
                if (_player.hasPermission("scavenger.exp")) {
                    _player.setExp(restoration.exp);        
                }                
                if (plug.getSConfig().shouldNotify()) {
                    plug.message(_player, "Your inventory has been restored.");
                    //plug.logDebug("Recovering experience: "+_player.getDisplayName());                
                }
                restorations.remove(_player.getName());
            }
        }
    }
}
