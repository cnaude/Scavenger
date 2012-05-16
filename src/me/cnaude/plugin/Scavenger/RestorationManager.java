package me.cnaude.plugin.Scavenger;

import com.onarandombox.multiverseinventories.*;
import com.onarandombox.multiverseinventories.api.GroupManager;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.api.share.Sharables;
import com.onarandombox.multiverseinventories.api.share.Shares;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.milkbowl.vault.economy.EconomyResponse;
import net.slipcor.pvparena.api.PVPArenaAPI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;


public class RestorationManager implements Serializable {
    private static HashMap<String, Restoration> restorations = new HashMap<String, Restoration>();    

    public static void save(Scavenger plug) {
        HashMap<String, RestorationS> res_s = new HashMap<String, RestorationS>(); 
        for (Map.Entry<String, Restoration> entry : restorations.entrySet()) {            
            String key = entry.getKey();
            Restoration value = entry.getValue();
            RestorationS restoration_s = new RestorationS();
            for (ItemStack i : value.inventory) { 
                if (i instanceof ItemStack) {
                    plug.debugMessage("Serializing: "+i.toString());
                    restoration_s.inventory.add(i.serialize());
                    plug.debugMessage("Done: "+i.toString());
                }
            }
            for (ItemStack i : value.armour) { 
                if (i instanceof ItemStack) {
                    plug.debugMessage("Serializing: "+i.toString());                
                    restoration_s.armour.add(i.serialize());
                    plug.debugMessage("Done: "+i.toString());
                }
            }
            restoration_s.enabled = value.enabled;
            restoration_s.level = value.level;
            restoration_s.exp = value.exp;
            res_s.put(key, restoration_s);
            plug.logInfo("Saving "+key+"'s inventory to disk.");
        }
        try {
            File file = new File("plugins/Scavenger/inv.ser"); 
            FileOutputStream f_out = new FileOutputStream (file);
            ObjectOutputStream obj_out = new ObjectOutputStream (f_out);
            obj_out.writeObject (res_s);
            obj_out.close();
        }
        catch(Exception e) {
          plug.logError(e.getMessage());
        }
    }
    
    public static void load(Scavenger plug) {
        HashMap<String, RestorationS> res_s;
        File file = new File("plugins/Scavenger/inv.ser");
        if (!file.exists()) {
            plug.logInfo("Recovery file '"+file.getAbsolutePath()+"' does not exist.");
            return;
        }
        try {                     
            FileInputStream f_in = new FileInputStream(file);
            ObjectInputStream obj_in = new ObjectInputStream (f_in);
            res_s = (HashMap<String, RestorationS>) obj_in.readObject();
            obj_in.close();            
        }
        catch(Exception e) {
          plug.logError(e.getMessage());
          return;
        }
        
        for (Map.Entry<String, RestorationS> entry : res_s.entrySet()) {            
            String key = entry.getKey();
            RestorationS value = entry.getValue();                       
            Restoration restoration = new Restoration();
            restoration.inventory = new ItemStack[value.inventory.size()];
            restoration.armour = new ItemStack[value.armour.size()];
            
            for (int i = 0; i<value.inventory.size(); i++) {                
                if (value.inventory.get(i) instanceof Map) {
                    plug.debugMessage("Deserializing: "+value.inventory.get(i).toString());
                    restoration.inventory[i] = ItemStack.deserialize(value.inventory.get(i));
                    plug.debugMessage("Done: "+restoration.inventory[i].toString());
                }
            }
            for (int i = 0; i<value.armour.size(); i++) {                
                if (value.armour.get(i) instanceof Map) {
                    plug.debugMessage("Deserializing: "+value.armour.get(i).toString()); 
                    restoration.armour[i] = ItemStack.deserialize(value.armour.get(i));
                    plug.debugMessage("Done: "+restoration.armour[i].toString()); 
                }
            }
            restoration.enabled = value.enabled;
            restoration.level = value.level;
            restoration.exp = value.exp;
            restorations.put(key, restoration);
            plug.logInfo("Loading "+key+"'s inventory from disk.");
        }
    }
    
    public static boolean hasRestoration(Player _player) {
        if (Scavenger.get().getMultiverseInventories() != null) {
            List<WorldGroupProfile> groupProfiles = Scavenger.get().getMultiverseInventories().getGroupManager().getGroupsForWorld(_player.getWorld().getName());
            List<String> groups = new ArrayList<String>();
            for (WorldGroupProfile i: groupProfiles) {
                if (restorations.containsKey(_player.getName())) {
                    Restoration restoration = restorations.get(_player.getName());
                    if (restoration != null) {
                        if (restoration.inventoryWorldGroups != null) {
                            if (restoration.inventoryWorldGroups.contains(i.getName()) == true) {
                                return true;
                            }
                        } 
                    }
                }
                
            }
            return false;
            
        }
        else {
            return restorations.containsKey(_player.getName());
        }
    }
    
    public static void collect(Scavenger plug, Player _player, List<ItemStack> _drops, EntityDeathEvent event) {
        if (_drops.isEmpty() && _player.getExp() == 0 && _player.getLevel() == 0) {
            return;
        }
        
        if (plug.getWorldGuard() != null) {
            plug.logDebug("Checking region support for '"+_player.getWorld().getName()+"'");
            if (plug.getWorldGuard().getRegionManager(_player.getWorld()) != null) {
                RegionManager regionManager = plug.getWorldGuard().getRegionManager(_player.getWorld());       
                ApplicableRegionSet set = regionManager.getApplicableRegions(_player.getLocation());
                if (set.allows(DefaultFlag.PVP) && plug.getSConfig().wgPVPIgnore()) {
                    plug.logDebug("This is a WorldGuard PVP zone and WorldGuardPVPIgnore is "+plug.getSConfig().wgPVPIgnore());
                    if (!plug.getSConfig().msgInsideWGPVP().isEmpty()) {
                        plug.message(_player, plug.getSConfig().msgInsideWGPVP());                        
                    }
                    return;
                }
                if (!set.allows(DefaultFlag.PVP) && plug.getSConfig().wgGuardPVPOnly()) {
                    plug.logDebug("This is NOT a WorldGuard PVP zone and WorldGuardPVPOnly is "+plug.getSConfig().wgGuardPVPOnly());
                    if (!plug.getSConfig().msgInsideWGPVP().isEmpty()) {
                        plug.message(_player, plug.getSConfig().msgInsideWGPVPOnly());
                    }
                    return;
                }
            } else {
                plug.logDebug("Region support disabled for '"+_player.getWorld().getName()+"'");
            }
        }
        
        if (plug.getUltimateArena() != null) {
            if (plug.getUltimateArena().isInArena(_player)) {
                if (!plug.getSConfig().msgInsideUA().isEmpty()) {
                    plug.message(_player, plug.getSConfig().msgInsideUA());
                }
                return;
            }
        }
        
        if (Scavenger.maHandler != null && Scavenger.maHandler.isPlayerInArena(_player)) { 
            if (!plug.getSConfig().msgInsideMA().isEmpty()) {
                plug.message(_player, plug.getSConfig().msgInsideMA());
            }
            return;
        }
        
        if (Scavenger.pvpHandler != null && !PVPArenaAPI.getArenaName(_player).equals("")) { 
            String x = plug.getSConfig().msgInsidePA();
            if (!x.isEmpty()) {
                x = x.replaceAll("%ARENA%", PVPArenaAPI.getArenaName(_player));
                plug.message(_player, x);                               
            }
            return;
        } 
        List<String> tempRespawnGroups = new ArrayList<String>();
        if (plug.getMultiverseInventories() != null) {
            
            if (plug.getMultiverseInventories().getGroupManager() != null) {
                GroupManager groupManager = plug.getMultiverseInventories().getGroupManager();
               
                for (WorldGroupProfile i: groupManager.getGroupsForWorld(_player.getWorld().getName())) {
                    if (i.isSharing(Sharables.ARMOR) && i.isSharing(Sharables.INVENTORY)) {
                        tempRespawnGroups.add(i.getName());
                    }
                }
            }
        }
        
        if (hasRestoration(_player)) {
            plug.error(_player, "Restoration already exists, ignoring.");              
            return;
        }

        if(plug.getEconomy() != null 
                && !(_player.hasPermission("scavenger.free") 
                || (_player.isOp() && plug.getSConfig().opsAllPerms())) 
                && plug.getSConfig().economyEnabled()) {          
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
                String x = plug.getSConfig().msgSaveForFee();
                if (!x.isEmpty()) {
                    x = x.replaceAll("%COST%", String.format("%.2f",withdraw_amount));
                    x = x.replaceAll("%CURRENCY%", currency);
                    plug.message(_player, x);                
                }
            } else {
                if (player_balance == 1) {
                   currency = plug.getEconomy().currencyNameSingular();                
                } else {
                    currency = plug.getEconomy().currencyNamePlural();
                }                
                String x = plug.getSConfig().msgNotEnoughMoney();
                if (!x.isEmpty()) {
                    x = x.replaceAll("%BALANCE%", String.format("%.2f",player_balance));
                    x = x.replaceAll("%COST%", String.format("%.2f",withdraw_amount));
                    x = x.replaceAll("%CURRENCY%", currency);
                    plug.message(_player, x); 
                }
                return;
            }
        } else {
                plug.message(_player, plug.getSConfig().msgSaving());            
        }
        Restoration restoration = new Restoration();

        restoration.enabled = false;
        if (tempRespawnGroups != null) {
            restoration.inventoryWorldGroups = tempRespawnGroups;
        }
        restoration.inventory = _player.getInventory().getContents();
        restoration.armour = _player.getInventory().getArmorContents();
        
        
        
        if (_player.hasPermission("scavenger.level") 
                || !plug.getSConfig().permsEnabled()
                || (_player.isOp() && plug.getSConfig().opsAllPerms())) {
            restoration.level = _player.getLevel();
        }
        if (_player.hasPermission("scavenger.exp") 
                || !plug.getSConfig().permsEnabled()
                || (_player.isOp() && plug.getSConfig().opsAllPerms())) {            
            restoration.exp = _player.getExp();
            event.setDroppedExp(0);
        }
           
        _drops.clear();
        
        if (plug.getSConfig().singleItemDrops()) {
            ItemStack[][] invAndArmour = {restoration.inventory,restoration.armour};
            for (ItemStack[] a : invAndArmour) {
                for (ItemStack i : a) {
                    boolean dropIt;
                    if (i instanceof ItemStack && !i.getType().equals(Material.AIR)) {  
                        if (plug.getSConfig().singleItemDropsOnly() == true) {
                            if (_player.hasPermission("scavenger.drop."+i.getTypeId())) { 
                                dropIt = false; 
                            } else {
                                dropIt = true;
                            }    
                        } else {
                            if (!_player.hasPermission("scavenger.drop."+i.getTypeId())) {  
                                dropIt = false;
                            } else {
                                dropIt = true;                            
                            }
                        } 
                        if (dropIt) {                            
                            plug.debugMessage(_player,"Dropping item "+i.getType());  
                            _drops.add(i.clone()); 
                            i.setAmount(0);
                        } else {
                            plug.debugMessage(_player,"Keeping item "+i.getType()); 
                        }
                    }                
                }  
            }
        } 
        restorations.put(_player.getName(), restoration);
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
            
        if (plug.getMultiverseInventories() != null) {
            if (plug.getMultiverseInventories().getGroupManager() != null) {
                if ( plug.getMultiverseInventories().getGroupManager() != null) {
                GroupManager groupManager = plug.getMultiverseInventories().getGroupManager();
                List<WorldGroupProfile> profiles = groupManager.getGroupsForWorld(_player.getWorld().getName());
                List<String> groups = new ArrayList<String>();
                for (WorldGroupProfile i: profiles) {
                    groups.add(i.getName());
                    
                }
                boolean isInGroup = false;
                for (String i: groups) {
                    if (restoration.inventoryWorldGroups.contains(i)) {
                        isInGroup = true;
                        break;
                    }
                    else if (restoration.inventoryWorldGroups == null) {
                        isInGroup = true;
                        break;
                    }
                }
                if  (isInGroup == false) {
                    plug.message(_player, "You have to be in the same inventory group to get your inventory back.");
                   
                    return;
                }
             }   
            }
        }
            if (restoration.enabled ) {                              
                _player.getInventory().clear();          

                _player.getInventory().setContents(restoration.inventory);
                _player.getInventory().setArmorContents(restoration.armour);
                if (_player.hasPermission("scavenger.level") 
                        || !plug.getSConfig().permsEnabled()
                        || (_player.isOp() && plug.getSConfig().opsAllPerms())) {
                    _player.setLevel(restoration.level);                              
                }
                if (_player.hasPermission("scavenger.exp") 
                        || !plug.getSConfig().permsEnabled()
                        || (_player.isOp() && plug.getSConfig().opsAllPerms())) {
                    _player.setExp(restoration.exp);        
                }                
                if (plug.getSConfig().shouldNotify()) {
                    plug.message(_player, plug.getSConfig().msgRecovered());                    
                }
                restorations.remove(_player.getName());
            }
        }
    }
}
