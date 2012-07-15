package me.cnaude.plugin.Scavenger;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import com.massivecraft.factions.FPlayer;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.api.GroupManager;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import java.io.*;
import java.util.*;
import net.milkbowl.vault.economy.EconomyResponse;
import net.slipcor.pvparena.api.PVPArenaAPI;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class RestorationManager implements Serializable {

    private static HashMap<String, Restoration> restorations = new HashMap<String, Restoration>();

    public void save() {
        HashMap<String, RestorationS> res_s = new HashMap<String, RestorationS>();
        for (Map.Entry<String, Restoration> entry : restorations.entrySet()) {
            String key = entry.getKey();
            Restoration value = entry.getValue();
            RestorationS restoration_s = new RestorationS();
            for (ItemStack i : value.inventory) {
                if (i instanceof ItemStack) {
                    Scavenger.get().debugMessage("Serializing: " + i.toString());
                    restoration_s.inventory.add(i.serialize());
                    Scavenger.get().debugMessage("Done: " + i.toString());
                }
            }
            for (ItemStack i : value.armour) {
                if (i instanceof ItemStack) {
                    Scavenger.get().debugMessage("Serializing: " + i.toString());
                    restoration_s.armour.add(i.serialize());
                    Scavenger.get().debugMessage("Done: " + i.toString());
                }
            }
            restoration_s.enabled = value.enabled;
            restoration_s.level = value.level;
            restoration_s.exp = value.exp;
            res_s.put(key, restoration_s);
            Scavenger.get().logInfo("Saving " + key + "'s inventory to disk.");
        }
        try {
            File file = new File("plugins/Scavenger/inv.ser");
            FileOutputStream f_out = new FileOutputStream(file);
            ObjectOutputStream obj_out = new ObjectOutputStream(f_out);
            obj_out.writeObject(res_s);
            obj_out.close();
        } catch (Exception e) {
            Scavenger.get().logError(e.getMessage());
        }
    }

    public void load() {
        HashMap<String, RestorationS> res_s;
        File file = new File("plugins/Scavenger/inv.ser");
        if (!file.exists()) {
            Scavenger.get().logInfo("Recovery file '" + file.getAbsolutePath() + "' does not exist.");
            return;
        }
        try {
            FileInputStream f_in = new FileInputStream(file);
            ObjectInputStream obj_in = new ObjectInputStream(f_in);
            res_s = (HashMap<String, RestorationS>) obj_in.readObject();
            obj_in.close();
        } catch (Exception e) {
            Scavenger.get().logError(e.getMessage());
            return;
        }

        for (Map.Entry<String, RestorationS> entry : res_s.entrySet()) {
            String key = entry.getKey();
            RestorationS value = entry.getValue();
            Restoration restoration = new Restoration();
            restoration.inventory = new ItemStack[value.inventory.size()];
            restoration.armour = new ItemStack[value.armour.size()];

            for (int i = 0; i < value.inventory.size(); i++) {
                if (value.inventory.get(i) instanceof Map) {
                    Scavenger.get().debugMessage("Deserializing: " + value.inventory.get(i).toString());
                    restoration.inventory[i] = ItemStack.deserialize(value.inventory.get(i));
                    Scavenger.get().debugMessage("Done: " + restoration.inventory[i].toString());
                }
            }
            for (int i = 0; i < value.armour.size(); i++) {
                if (value.armour.get(i) instanceof Map) {
                    Scavenger.get().debugMessage("Deserializing: " + value.armour.get(i).toString());
                    restoration.armour[i] = ItemStack.deserialize(value.armour.get(i));
                    Scavenger.get().debugMessage("Done: " + restoration.armour[i].toString());
                }
            }
            restoration.enabled = value.enabled;
            restoration.level = value.level;
            restoration.exp = value.exp;

            restorations.put(key, restoration);
            Scavenger.get().logInfo("Loading " + key + "'s inventory from disk.");
        }
    }

    public static boolean hasRestoration(Player p) {
        if (Scavenger.get().getMultiverseInventories() != null) {
            String keyName = p.getName() + "." + getWorldGroups(p.getWorld()).get(0);
            if (restorations.containsKey(keyName)) {
                Scavenger.get().logDebug("Has: " + keyName);
                return true;
            }
        }
        return restorations.containsKey(p.getName());
    }

    private static Restoration getRestoration(Player p) {
        Restoration restoration = new Restoration();
        restoration.enabled = false;
        if (Scavenger.get().getMultiverseInventories() != null) { 
            String keyName = p.getName() + "." + getWorldGroups(p.getWorld()).get(0);
            if (restorations.containsKey(keyName)) {
                Scavenger.get().logDebug("Getting: " + keyName);
                restoration = restorations.get(keyName);
            }
        }
        if (!restoration.enabled) {
            if (restorations.containsKey(p.getName())) {
                Scavenger.get().logDebug("Getting: " + p.getName());
                restoration = restorations.get(p.getName());
            }
        }
        return restoration;
    }

    public static void collect(Player p, List<ItemStack> _drops, EntityDeathEvent event) {
        if (_drops.isEmpty() && p.getExp() == 0 && p.getLevel() == 0) {
            return;
        }
        
        if (Scavenger.getSConfig().dropOnPVPDeath()){
            if (p.getKiller() instanceof Player) {
                Scavenger.get().message(p, Scavenger.getSConfig().msgPVPDeath());
                return;
            }
        }

        if (Scavenger.getSConfig().residence()) {
            ClaimedResidence res = Residence.getResidenceManager().getByLoc(p.getLocation());                       
            if(res != null) {
                ResidencePermissions perms = res.getPermissions(); 
                if (perms.playerHas(p.getName(),Scavenger.getSConfig().resFlag(), true)) {                
                    Scavenger.get().logDebug("Player '"+ p.getName() + "' is not allowed to use Scavenger in this residence. Items will be dropped.");
                    Scavenger.get().message(p, Scavenger.getSConfig().msgInsideRes());
                    return;
                } else {
                    Scavenger.get().logDebug("Player '"+ p.getName() + "' is allowed to use Scavenger in this residence.");
                    
                }               
            } 
        }
        
        if (Scavenger.getSConfig().factionEnemyDrops()) {
            if (Scavenger.get().getFactions() != null) {
                Scavenger.get().logDebug("Checking if '" + p.getName() + "' is in enemy territory.");            
                FPlayer fplayer = com.massivecraft.factions.FPlayers.i.get(p);
                Scavenger.get().logDebug("Relation: "+fplayer.getRelationToLocation().name());
                if (fplayer.getRelationToLocation().name().equals("ENEMY")) {
                    Scavenger.get().logDebug("Player '" + p.getName() + "' is inside enemy territory!");
                    Scavenger.get().message(p, Scavenger.getSConfig().msgInsideEnemyFaction());
                    return;
                }
            } else {
                Scavenger.get().logDebug("No Factions detected");
            }
        }
        
        if (Scavenger.get().getWorldGuard() != null) {
            Scavenger.get().logDebug("Checking region support for '" + p.getWorld().getName() + "'");
            if (Scavenger.get().getWorldGuard().getRegionManager(p.getWorld()) != null) {
                RegionManager regionManager = Scavenger.get().getWorldGuard().getRegionManager(p.getWorld());
                ApplicableRegionSet set = regionManager.getApplicableRegions(p.getLocation());
                if (set.allows(DefaultFlag.PVP) && Scavenger.getSConfig().wgPVPIgnore()) {
                    Scavenger.get().logDebug("This is a WorldGuard PVP zone and WorldGuardPVPIgnore is " + Scavenger.getSConfig().wgPVPIgnore());
                    if (!Scavenger.getSConfig().msgInsideWGPVP().isEmpty()) {
                        Scavenger.get().message(p, Scavenger.getSConfig().msgInsideWGPVP());
                    }
                    return;
                }
                if (!set.allows(DefaultFlag.PVP) && Scavenger.getSConfig().wgGuardPVPOnly()) {
                    Scavenger.get().logDebug("This is NOT a WorldGuard PVP zone and WorldGuardPVPOnly is " + Scavenger.getSConfig().wgGuardPVPOnly());
                    if (!Scavenger.getSConfig().msgInsideWGPVP().isEmpty()) {
                        Scavenger.get().message(p, Scavenger.getSConfig().msgInsideWGPVPOnly());
                    }
                    return;
                }
            } else {
                Scavenger.get().logDebug("Region support disabled for '" + p.getWorld().getName() + "'");
            }
        }

        if (Scavenger.get().getUltimateArena() != null) {
            if (Scavenger.get().getUltimateArena().hookIntoUA().isPlayerInArenaLocation(p)) {
                if (!Scavenger.getSConfig().msgInsideUA().isEmpty()) {
                    Scavenger.get().message(p, Scavenger.getSConfig().msgInsideUA());
                }
                return;
            }
        }

        if (Scavenger.maHandler != null && Scavenger.maHandler.isPlayerInArena(p)) {
            if (!Scavenger.getSConfig().msgInsideMA().isEmpty()) {
                Scavenger.get().message(p, Scavenger.getSConfig().msgInsideMA());
            }
            return;
        }

        if (Scavenger.pvpHandler != null && !PVPArenaAPI.getArenaName(p).equals("")) {
            String x = Scavenger.getSConfig().msgInsidePA();
            if (!x.isEmpty()) {
                x = x.replaceAll("%ARENA%", PVPArenaAPI.getArenaName(p));
                Scavenger.get().message(p, x);
            }
            return;
        }

        if (hasRestoration(p)) {
            Scavenger.get().error(p, "Restoration already exists, ignoring.");
            return;
        }

        if (Scavenger.get().getEconomy() != null
                && !(p.hasPermission("scavenger.free")
                || (p.isOp() && Scavenger.getSConfig().opsAllPerms()))
                && Scavenger.getSConfig().economyEnabled()) {
            double restore_cost = Scavenger.getSConfig().restoreCost();
            double withdraw_amount;
            double player_balance = Scavenger.get().getEconomy().getBalance(p.getName());
            double percent_cost = Scavenger.getSConfig().percentCost();
            double min_cost = Scavenger.getSConfig().minCost();
            double max_cost = Scavenger.getSConfig().maxCost();
            EconomyResponse er;
            String currency;
            if (Scavenger.getSConfig().percent()) {
                withdraw_amount = player_balance * (percent_cost / 100.0);
                if (Scavenger.getSConfig().addMin()) {
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
            er = Scavenger.get().getEconomy().withdrawPlayer(p.getName(), withdraw_amount);
            if (er.transactionSuccess()) {
                if (withdraw_amount == 1) {
                    currency = Scavenger.get().getEconomy().currencyNameSingular();
                } else {
                    currency = Scavenger.get().getEconomy().currencyNamePlural();
                }
                String x = Scavenger.getSConfig().msgSaveForFee();
                if (!x.isEmpty()) {
                    x = x.replaceAll("%COST%", String.format("%.2f", withdraw_amount));
                    x = x.replaceAll("%CURRENCY%", currency);
                    Scavenger.get().message(p, x);
                }
            } else {
                if (player_balance == 1) {
                    currency = Scavenger.get().getEconomy().currencyNameSingular();
                } else {
                    currency = Scavenger.get().getEconomy().currencyNamePlural();
                }
                String x = Scavenger.getSConfig().msgNotEnoughMoney();
                if (!x.isEmpty()) {
                    x = x.replaceAll("%BALANCE%", String.format("%.2f", player_balance));
                    x = x.replaceAll("%COST%", String.format("%.2f", withdraw_amount));
                    x = x.replaceAll("%CURRENCY%", currency);
                    Scavenger.get().message(p, x);
                }
                return;
            }
        } else {
            Scavenger.get().message(p, Scavenger.getSConfig().msgSaving());
        }

        Restoration restoration = new Restoration();
        restoration.enabled = false;       
        restoration.inventory = p.getInventory().getContents();    
        restoration.armour = p.getInventory().getArmorContents();

        if (p.hasPermission("scavenger.level")
                || !Scavenger.getSConfig().permsEnabled()
                || (p.isOp() && Scavenger.getSConfig().opsAllPerms())) {
            restoration.level = p.getLevel();
        }
        if (p.hasPermission("scavenger.exp")
                || !Scavenger.getSConfig().permsEnabled()
                || (p.isOp() && Scavenger.getSConfig().opsAllPerms())) {
            restoration.exp = p.getExp();
            event.setDroppedExp(0);
        }
        
        _drops.clear();

        if (Scavenger.getSConfig().singleItemDrops()) {
            ItemStack[][] invAndArmour = {restoration.inventory, restoration.armour};
            for (ItemStack[] a : invAndArmour) {
                for (ItemStack i : a) {
                    boolean dropIt;
                    if (i instanceof ItemStack && !i.getType().equals(Material.AIR)) {
                        if (Scavenger.getSConfig().singleItemDropsOnly() == true) {
                            if (p.hasPermission("scavenger.drop." + i.getTypeId())) {
                                dropIt = false;
                            } else {
                                dropIt = true;
                            }
                        } else {
                            if (!p.hasPermission("scavenger.drop." + i.getTypeId())) {
                                dropIt = false;
                            } else {
                                dropIt = true;
                            }
                        }
                        if (dropIt) {
                            Scavenger.get().debugMessage(p, "Dropping item " + i.getType());
                            _drops.add(i.clone());
                            i.setAmount(0);
                        } else {
                            Scavenger.get().debugMessage(p, "Keeping item " + i.getType());
                        }
                    }
                }
            }
        }
        
        if (Scavenger.getSConfig().chanceToDrop() > 0 
                && !p.hasPermission("scavenger.nochance")) {
            ItemStack[][] invAndArmour = {restoration.inventory, restoration.armour};
            for (ItemStack[] a : invAndArmour) {
                for (ItemStack i : a) {
                    if (i instanceof ItemStack && !i.getType().equals(Material.AIR)) {
                        Random randomGenerator = new Random();
                        int randomInt = randomGenerator.nextInt(Scavenger.getSConfig().chanceToDrop()) + 1;
                        Scavenger.get().debugMessage(p, "Random number is "+randomInt);
                        if (randomInt == Scavenger.getSConfig().chanceToDrop()) {
                            Scavenger.get().debugMessage(p, "Randomly dropping item " + i.getType());
                            _drops.add(i.clone());
                            i.setAmount(0);
                        } else {
                            Scavenger.get().debugMessage(p, "Randomly keeping item " + i.getType());
                        }
                    }
                }
            }
        }
        
        addRestoration(p, restoration);
    }
    
    public void printRestorations(Player p) {
        Scavenger.get().message(p, "Restorations:");
        for (String key : restorations.keySet()) {
            Scavenger.get().message(p, "  " + key);
        }
    }
    
    public void printRestorations() {
        Scavenger.get().logInfo("Restorations:");
        for (String key : restorations.keySet()) {
            Scavenger.get().logInfo("  " + key);
        }
    }

    public static void addRestoration(Player p, Restoration r) {
        if (Scavenger.get().getMultiverseInventories() != null) {
            String keyName = p.getName() + "." + getWorldGroups(p.getWorld()).get(0);
            restorations.put(keyName, r);
            Scavenger.get().debugMessage("Adding: " + keyName);
        } else {
            restorations.put(p.getName(), r);
            Scavenger.get().debugMessage("Adding: " + p.getName());
        }
    }

    public static void enable(Player p) {
        if (hasRestoration(p)) {
            Restoration restoration = getRestoration(p);
            restoration.enabled = true;
            Scavenger.get().debugMessage("Enabling: " + p.getName());
        }
    }

    public static void restore(Player p) {
        Restoration restoration = getRestoration(p);

        if (restoration.enabled) {
            p.getInventory().clear();

            p.getInventory().setContents(restoration.inventory);
            p.getInventory().setArmorContents(restoration.armour);
            if (p.hasPermission("scavenger.level")
                    || !Scavenger.getSConfig().permsEnabled()
                    || (p.isOp() && Scavenger.getSConfig().opsAllPerms())) {
                p.setLevel(restoration.level);
            }
            if (p.hasPermission("scavenger.exp")
                    || !Scavenger.getSConfig().permsEnabled()
                    || (p.isOp() && Scavenger.getSConfig().opsAllPerms())) {
                p.setExp(restoration.exp);
            }
            if (Scavenger.getSConfig().shouldNotify()) {
                Scavenger.get().message(p, Scavenger.getSConfig().msgRecovered());
            }
            removeRestoration(p);
            if (hasRestoration(p)) {
                Scavenger.get().message(p, "Restore exists!!!");
            }
        }

    }

    public static void removeRestoration(Player p) {
        if (Scavenger.get().getMultiverseInventories() != null) {
            String keyName = p.getName() + "." + getWorldGroups(p.getWorld()).get(0);
            if (restorations.containsKey(keyName)) {
                restorations.remove(keyName);
                Scavenger.get().logDebug("Removing: " + keyName);
            }
        }
        if (restorations.containsKey(p.getName())) {
            restorations.remove(p.getName());
            Scavenger.get().logDebug("Removing: " + p.getName());
        }
    }

    public static List<String> getWorldGroups(World world) {
        List<String> returnData = new ArrayList<String>();
        if (Scavenger.get().getMultiverseInventories() != null) {
            MultiverseInventories multiInv = Scavenger.get().getMultiverseInventories();
            if (multiInv.getGroupManager() != null) {
                GroupManager groupManager = multiInv.getGroupManager();
                if (groupManager.getGroupsForWorld(world.getName()) != null) {
                    List<WorldGroupProfile> worldGroupProfiles = groupManager.getGroupsForWorld(world.getName());
                    if (worldGroupProfiles != null) {
                        for (WorldGroupProfile i : worldGroupProfiles) {
                            returnData.add(i.getName());
                        }
                    }
                }

            }
        }
        if (returnData.isEmpty())
            returnData.add("");
        return returnData;
    }
}
