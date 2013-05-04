package me.cnaude.plugin.Scavenger;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import com.comphenix.protocol.utility.StreamSerializer;
import com.massivecraft.factions.FPlayer;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.api.GroupManager;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.orange451.UltimateArena.UltimateArenaAPI;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.timvisee.DungeonMaze.API.DungeonMazeAPI;

import java.io.*;
import java.util.*;
import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.match.Match;
import me.drayshak.WorldInventories.api.WorldInventoriesAPI;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import uk.co.tggl.pluckerpluck.multiinv.MultiInvAPI;

public final class RestorationManager implements Serializable {

    Scavenger plugin;
    private static HashMap<String, Restoration> restorations = new HashMap<String, Restoration>();

    public RestorationManager(Scavenger plugin) {
        this.plugin = plugin;
        this.load();
    }
    
    public void save() {
        StreamSerializer serializer = new StreamSerializer();
        HashMap<String, RestorationS1> restorationsForDisk = new HashMap<String, RestorationS1>();
        for (Map.Entry<String, Restoration> entry : restorations.entrySet()) {
            String key = entry.getKey();
            Restoration value = entry.getValue();
            RestorationS1 tmpRestoration = new RestorationS1();
            for (ItemStack i : value.inventory) {
                boolean error = false;
                if (i instanceof ItemStack) {
                    plugin.debugMessage("Serializing: " + i.toString());
                    try {
                        tmpRestoration.inventory.add(serializer.serializeItemStack(i));
                    } catch (IOException e) {
                        plugin.logError(e.getMessage());
                        error = true;
                    } catch (Exception e) {
                        error = true;
                        plugin.logError(e.getMessage());
                    }
                    if (error) {
                        plugin.logError("Problem serializing item: " + i.toString());
                    }
                    plugin.debugMessage("Done: " + i.toString());
                }
            }
            for (ItemStack i : value.armour) {
                if (i instanceof ItemStack) {
                    plugin.debugMessage("Serializing: " + i.toString());
                    try {
                        tmpRestoration.armour.add(serializer.serializeItemStack(i));
                    } catch (IOException e) {
                        plugin.logError(e.getMessage());
                    } catch (Exception e) {
                        plugin.logError(e.getMessage());
                    }
                    plugin.debugMessage("Done: " + i.toString());
                }
            }
            tmpRestoration.enabled = value.enabled;
            tmpRestoration.level = value.level;
            tmpRestoration.exp = value.exp;
            restorationsForDisk.put(key, tmpRestoration);
            plugin.logInfo("Saving " + key + "'s inventory to disk.");
        }
        try {
            File file = new File("plugins/Scavenger/inv1.ser");
            FileOutputStream f_out = new FileOutputStream(file);
            ObjectOutputStream obj_out = new ObjectOutputStream(f_out);
            obj_out.writeObject(restorationsForDisk);
            obj_out.close();
        } catch (Exception e) {
            plugin.logError(e.getMessage());
        }
    }

    public void load() {
        StreamSerializer serializer = new StreamSerializer();
        HashMap<String, RestorationS1> restorationsFromDisk;
        File file = new File("plugins/Scavenger/inv1.ser");
        if (!file.exists()) {
            plugin.logDebug("Recovery file '" + file.getAbsolutePath() + "' does not exist.");
            return;
        }
        try {
            FileInputStream f_in = new FileInputStream(file);
            ObjectInputStream obj_in = new ObjectInputStream(f_in);
            restorationsFromDisk = (HashMap<String, RestorationS1>) obj_in.readObject();
            obj_in.close();
        } catch (Exception e) {
            plugin.logError(e.getMessage());
            return;
        }

        for (Map.Entry<String, RestorationS1> entry : restorationsFromDisk.entrySet()) {
            String key = entry.getKey();
            RestorationS1 value = entry.getValue();
            Restoration tmpRestoration = new Restoration();
            tmpRestoration.inventory = new ItemStack[value.inventory.size()];
            tmpRestoration.armour = new ItemStack[value.armour.size()];

            for (int i = 0; i < value.inventory.size(); i++) {
                if (value.inventory.get(i) instanceof String) {
                    boolean error = false;
                    ItemStack tmpStack = new ItemStack(Material.AIR);
                    plugin.debugMessage("Deserializing: " + value.inventory.get(i).toString());
                    try {
                        tmpStack = serializer.deserializeItemStack(value.inventory.get(i));
                    } catch (IOException e) {
                        plugin.logError(e.getMessage());
                        error = true;
                    } catch (Exception e) {
                        plugin.logError(e.getMessage());
                        error = true;
                    }
                    if (error) {
                        plugin.logError("Problem deserializing item: " + value.inventory.get(i).toString());
                    }
                    if (tmpStack == null) {
                        tmpRestoration.inventory[i] = new ItemStack(Material.AIR);
                    } else {
                        tmpRestoration.inventory[i] = tmpStack;
                    }
                    plugin.debugMessage("Done: " + tmpRestoration.inventory[i].toString());
                }
            }

            for (int i = 0; i < value.armour.size(); i++) {
                if (value.armour.get(i) instanceof String) {
                    ItemStack tmpStack = new ItemStack(Material.AIR);
                    plugin.debugMessage("Deserializing: " + value.armour.get(i).toString());
                    try {
                        tmpStack = serializer.deserializeItemStack(value.armour.get(i));
                    } catch (IOException e) {
                        plugin.logError(e.getMessage());
                    }
                    if (tmpStack == null) {
                        tmpRestoration.armour[i] = new ItemStack(Material.AIR);
                    } else {
                        tmpRestoration.armour[i] = tmpStack;
                    }
                    plugin.debugMessage("Done: " + tmpRestoration.armour[i].toString());
                }
            }

            tmpRestoration.enabled = value.enabled;
            tmpRestoration.level = value.level;
            tmpRestoration.exp = value.exp;

            restorations.put(key, tmpRestoration);
            plugin.logInfo("Loading " + key + "'s inventory from disk.");
        }
    }

    public boolean multipleInventories() {
        if (plugin.getMultiverseInventories() != null
                || plugin.getMultiInvAPI() != null
                || plugin.getWorldInvAPI()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hasRestoration(Player p) {
        if (multipleInventories()) {
            String keyName = p.getName() + "." + getWorldGroups(p.getWorld()).get(0);
            if (restorations.containsKey(keyName)) {
                plugin.logDebug("Has: " + keyName);
                return true;
            }
        }
        return restorations.containsKey(p.getName());
    }

    public Restoration getRestoration(Player p) {
        Restoration restoration = new Restoration();
        restoration.enabled = false;
        if (multipleInventories()) {
            String keyName = p.getName() + "." + getWorldGroups(p.getWorld()).get(0);
            if (restorations.containsKey(keyName)) {
                plugin.logDebug("Getting: " + keyName);
                restoration = restorations.get(keyName);
            }
        }
        if (!restoration.enabled) {
            if (restorations.containsKey(p.getName())) {
                plugin.logDebug("Getting: " + p.getName());
                restoration = restorations.get(p.getName());
            }
        }
        return restoration;
    }

    public void collect(Player p, List<ItemStack> itemDrops, EntityDeathEvent event) {
        if (itemDrops.isEmpty() && !levelAllow(p) && !expAllow(p)) {
            return;
        }

        if (plugin.config.dropOnPVPDeath()) {
            if (p.getKiller() instanceof Player) {
                plugin.message(p, plugin.config.msgPVPDeath());
                return;
            }
        }

        if (plugin.config.residence()) {
            ClaimedResidence res = Residence.getResidenceManager().getByLoc(p.getLocation());
            if (res != null) {
                ResidencePermissions perms = res.getPermissions();
                if (perms.playerHas(p.getName(), plugin.config.resFlag(), true)) {
                    plugin.logDebug("Player '" + p.getName() + "' is not allowed to use Scavenger in this residence. Items will be dropped.");
                    plugin.message(p, plugin.config.msgInsideRes());
                    return;
                } else {
                    plugin.logDebug("Player '" + p.getName() + "' is allowed to use Scavenger in this residence.");

                }
            }
        }

        if (plugin.config.factionEnemyDrops()) {
            if (plugin.getFactions() != null) {
                try {
                    plugin.logDebug("Checking if '" + p.getName() + "' is in enemy territory.");
                    FPlayer fplayer = com.massivecraft.factions.FPlayers.i.get(p);
                    plugin.logDebug("Relation: " + fplayer.getRelationToLocation().name());
                    if (fplayer.getRelationToLocation().name().equals("ENEMY")) {
                        plugin.logDebug("Player '" + p.getName() + "' is inside enemy territory!");
                        plugin.message(p, plugin.config.msgInsideEnemyFaction());
                        return;
                    }
                } catch (NoSuchMethodError ex) {
                    plugin.logDebug("ERROR: " + ex.getMessage());
                }
            } else {
                plugin.logDebug("No Factions detected");
            }
        }

        if (plugin.config.dungeonMazeDrops()) {
            if (plugin.getDungeonMaze() != null) {
                plugin.logDebug("Checking if '" + p.getName() + "' is in DungeonMaze.");
                if (DungeonMazeAPI.isInDMWorld(p)) {
                    plugin.logDebug("Player '" + p.getName() + "' is in DungeonMaze.");
                    plugin.message(p, plugin.config.msgInsideDungeonMaze());
                    return;
                }
            } else {
                plugin.logDebug("No DungeonMaze plugin detected");
            }
        }

        if (plugin.getWorldGuard() != null) {
            plugin.logDebug("Checking region support for '" + p.getWorld().getName() + "'");
            if (plugin.getWorldGuard().getRegionManager(p.getWorld()) != null) {
                RegionManager regionManager = plugin.getWorldGuard().getRegionManager(p.getWorld());
                ApplicableRegionSet set = regionManager.getApplicableRegions(p.getLocation());
                if (set.allows(DefaultFlag.PVP) && plugin.config.wgPVPIgnore()) {
                    plugin.logDebug("This is a WorldGuard PVP zone and WorldGuardPVPIgnore is " + plugin.config.wgPVPIgnore());
                    if (!plugin.config.msgInsideWGPVP().isEmpty()) {
                        plugin.message(p, plugin.config.msgInsideWGPVP());
                    }
                    return;
                }
                if (!set.allows(DefaultFlag.PVP) && plugin.config.wgGuardPVPOnly()) {
                    plugin.logDebug("This is NOT a WorldGuard PVP zone and WorldGuardPVPOnly is " + plugin.config.wgGuardPVPOnly());
                    if (!plugin.config.msgInsideWGPVP().isEmpty()) {
                        plugin.message(p, plugin.config.msgInsideWGPVPOnly());
                    }
                    return;
                }
            } else {
                plugin.logDebug("Region support disabled for '" + p.getWorld().getName() + "'");
            }
        }

        if (plugin.getUltimateArena() != null) {
            plugin.getUltimateArena();
            if (UltimateArenaAPI.hookIntoUA().isPlayerInArenaLocation(p)) {
                if (!plugin.config.msgInsideUA().isEmpty()) {
                    plugin.message(p, plugin.config.msgInsideUA());
                }
                return;
            }
        }

        if (plugin.maHandler != null && plugin.maHandler.isPlayerInArena(p)) {
            if (!plugin.config.msgInsideMA().isEmpty()) {
                plugin.message(p, plugin.config.msgInsideMA());
            }
            return;
        }

        if (plugin.pvpHandler != null && !net.slipcor.pvparena.api.PVPArenaAPI.getArenaName(p).equals("")) {
            String x = plugin.config.msgInsidePA();
            if (!x.isEmpty()) {
                x = x.replaceAll("%ARENA%", net.slipcor.pvparena.api.PVPArenaAPI.getArenaName(p));
                plugin.message(p, x);
            }
            return;
        }

        if (plugin.battleArena) {
            mc.alk.arena.objects.ArenaPlayer ap = mc.alk.arena.BattleArena.toArenaPlayer(p);
            if (ap != null) {
                Match match = BattleArena.getBAController().getMatch(ap);
                if (match != null && match.insideArena(ap)) {
                    String x = plugin.config.msgInsideBA();
                    if (!x.isEmpty()) {
                        plugin.message(p, x);
                    }
                    return;
                }
            }
        }

        if (plugin.getMinigames() != null) {
            if (plugin.getMinigames().getPlayerData().playerInMinigame(p)) {
                plugin.logInfo("Player '" + p.getName() + "' is in a Minigame. Not recovering items.");
                return;
            }
        }

        if (hasRestoration(p)) {
            plugin.error(p, "Restoration already exists, ignoring.");
            return;
        }

        if (plugin.getEconomy() != null
                && !(p.hasPermission("scavenger.free")
                || (p.isOp() && plugin.config.opsAllPerms()))
                && plugin.config.economyEnabled()) {
            double restoreCost = plugin.config.restoreCost();
            double withdrawAmount;
            double playeBalance = plugin.getEconomy().getBalance(p.getName());
            double percentCost = plugin.config.percentCost();
            double minCost = plugin.config.minCost();
            double maxCost = plugin.config.maxCost();
            EconomyResponse er;
            String currency;
            if (plugin.config.percent()) {
                withdrawAmount = playeBalance * (percentCost / 100.0);
                if (plugin.config.addMin()) {
                    withdrawAmount = withdrawAmount + minCost;
                } else if (withdrawAmount < minCost) {
                    withdrawAmount = minCost;
                }
                if (withdrawAmount > maxCost && maxCost > 0) {
                    withdrawAmount = maxCost;
                }
            } else {
                withdrawAmount = restoreCost;
            }
            er = plugin.getEconomy().withdrawPlayer(p.getName(), withdrawAmount);
            if (er.transactionSuccess()) {
                if (withdrawAmount == 1) {
                    currency = plugin.getEconomy().currencyNameSingular();
                } else {
                    currency = plugin.getEconomy().currencyNamePlural();
                }
                String x = plugin.config.msgSaveForFee();
                if (!x.isEmpty()) {
                    x = x.replaceAll("%COST%", String.format("%.2f", withdrawAmount));
                    x = x.replaceAll("%CURRENCY%", currency);
                    plugin.message(p, x);
                }
            } else {
                if (playeBalance == 1) {
                    currency = plugin.getEconomy().currencyNameSingular();
                } else {
                    currency = plugin.getEconomy().currencyNamePlural();
                }
                String x = plugin.config.msgNotEnoughMoney();
                if (!x.isEmpty()) {
                    x = x.replaceAll("%BALANCE%", String.format("%.2f", playeBalance));
                    x = x.replaceAll("%COST%", String.format("%.2f", withdrawAmount));
                    x = x.replaceAll("%CURRENCY%", currency);
                    plugin.message(p, x);
                }
                return;
            }
        } else {
            plugin.message(p, plugin.config.msgSaving());
        }

        Restoration restoration = new Restoration();
        restoration.enabled = false;
        restoration.inventory = p.getInventory().getContents();
        restoration.armour = p.getInventory().getArmorContents();

        if (levelAllow(p)) {
            restoration.level = p.getLevel();
        }
        if (expAllow(p)) {
            restoration.exp = p.getExp();
            event.setDroppedExp(0);
        }

        itemDrops.clear();

        if (plugin.config.singleItemDrops()) {
            ItemStack[][] invAndArmour = {restoration.inventory, restoration.armour};
            for (ItemStack[] a : invAndArmour) {
                for (ItemStack i : a) {
                    boolean dropIt;
                    if (i instanceof ItemStack && !i.getType().equals(Material.AIR)) {
                        if (plugin.config.singleItemDropsOnly() == true) {
                            if ((p.hasPermission("scavenger.drop." + i.getTypeId())) || (p.hasPermission("scavenger.drop.*"))) {
                                dropIt = false;
                            } else {
                                dropIt = true;
                            }
                        } else {
                            if (!(p.hasPermission("scavenger.drop." + i.getTypeId())) || (p.hasPermission("scavenger.drop.*"))) {
                                dropIt = false;
                            } else {
                                dropIt = true;
                            }
                        }
                        if (dropIt) {
                            plugin.debugMessage(p, "Dropping item " + i.getType());
                            itemDrops.add(i.clone());
                            i.setAmount(0);
                        } else {
                            plugin.debugMessage(p, "Keeping item " + i.getType());
                        }
                    }
                }
            }
        }

        if (plugin.config.chanceToDrop() > 0
                && !p.hasPermission("scavenger.nochance")) {
            ItemStack[][] invAndArmour = {restoration.inventory, restoration.armour};
            for (ItemStack[] a : invAndArmour) {
                for (ItemStack i : a) {
                    if (i instanceof ItemStack && !i.getType().equals(Material.AIR)) {
                        Random randomGenerator = new Random();
                        int randomInt = randomGenerator.nextInt(plugin.config.chanceToDrop()) + 1;
                        plugin.debugMessage(p, "Random number is " + randomInt);
                        if (randomInt == plugin.config.chanceToDrop()) {
                            plugin.debugMessage(p, "Randomly dropping item " + i.getType());
                            itemDrops.add(i.clone());
                            i.setAmount(0);
                        } else {
                            plugin.debugMessage(p, "Randomly keeping item " + i.getType());
                        }
                    }
                }
            }
        }

        if (plugin.config.slotBasedRecovery()) {
            checkSlots(p, "armour", restoration.armour, itemDrops);
            checkSlots(p, "inv", restoration.inventory, itemDrops);
        }

        addRestoration(p, restoration);
    }

    private void checkSlots(Player p, String type, ItemStack[] itemStackArray, List<ItemStack> itemDrops) {
        for (int i = 0; i < itemStackArray.length; i++) {
            String itemType;
            if (itemStackArray[i] != null) {
                itemType = itemStackArray[i].getType().toString();
            } else {
                itemType = "NULL";
            }
            plugin.debugMessage("[type:" + type + "] [p:" + p.getName() + "] [slot:" + i + "] [item:"
                    + itemType + "] [perm:" + p.hasPermission("scavenger." + type + "." + i) + "]");
            if (!p.hasPermission("scavenger." + type + "." + i) && !itemType.equals("NULL")) {
                plugin.debugMessage(p, "Dropping: " + itemType);
                itemDrops.add(itemStackArray[i].clone());
                itemStackArray[i].setAmount(0);
            } else {
                plugin.debugMessage(p, "Keeping: " + itemType);
            }
        }
    }

    public boolean levelAllow(Player p) {
        if ((p.hasPermission("scavenger.level")
                || !plugin.config.permsEnabled()
                || (p.isOp() && plugin.config.opsAllPerms())) && p.getLevel() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean expAllow(Player p) {
        if ((p.hasPermission("scavenger.exp")
                || !plugin.config.permsEnabled()
                || (p.isOp() && plugin.config.opsAllPerms())) && p.getExhaustion() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void printRestorations(Player p) {
        plugin.message(p, "Restorations:");
        for (String key : restorations.keySet()) {
            plugin.message(p, "  " + key);
        }
    }

    public void printRestorations() {
        plugin.logInfo("Restorations:");
        for (String key : restorations.keySet()) {
            plugin.logInfo("  " + key);
        }
    }

    public void addRestoration(Player p, Restoration r) {
        if (multipleInventories()) {
            String keyName = p.getName() + "." + getWorldGroups(p.getWorld()).get(0);
            restorations.put(keyName, r);
            plugin.debugMessage("Adding: " + keyName);
        } else {
            restorations.put(p.getName(), r);
            plugin.debugMessage("Adding: " + p.getName());
        }
    }

    public void enable(Player p) {
        if (hasRestoration(p)) {
            Restoration restoration = getRestoration(p);
            restoration.enabled = true;
            plugin.debugMessage("Enabling: " + p.getName());
        } else {
            plugin.logDebug("Not enabling: " + p.getName());
        }
    }

    public void restore(Player p) {
        Restoration restoration = getRestoration(p);

        if (restoration.enabled) {
            p.getInventory().clear();

            p.getInventory().setContents(restoration.inventory);
            p.getInventory().setArmorContents(restoration.armour);
            if (p.hasPermission("scavenger.level")
                    || !plugin.config.permsEnabled()
                    || (p.isOp() && plugin.config.opsAllPerms())) {
                p.setLevel(restoration.level);
            }
            if (p.hasPermission("scavenger.exp")
                    || !plugin.config.permsEnabled()
                    || (p.isOp() && plugin.config.opsAllPerms())) {
                p.setExp(restoration.exp);
            }
            if (plugin.config.shouldNotify()) {
                plugin.message(p, plugin.config.msgRecovered());
            }
            removeRestoration(p);
            if (hasRestoration(p)) {
                plugin.message(p, "Restore exists!!!");
            }
        }

    }

    public void removeRestoration(Player p) {
        if (multipleInventories()) {
            String keyName = p.getName() + "." + getWorldGroups(p.getWorld()).get(0);
            if (restorations.containsKey(keyName)) {
                restorations.remove(keyName);
                plugin.logDebug("Removing: " + keyName);
            }
        }
        if (restorations.containsKey(p.getName())) {
            restorations.remove(p.getName());
            plugin.logDebug("Removing: " + p.getName());
        }
    }

    public List<String> getWorldGroups(World world) {
        List<String> returnData = new ArrayList<String>();
        if (plugin.getMultiverseInventories() != null) {
            MultiverseInventories multiInv = plugin.getMultiverseInventories();
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
        if (plugin.getMultiInvAPI() != null) {
            String worldname = world.getName();
            MultiInvAPI multiInvAPI = plugin.getMultiInvAPI();
            if (multiInvAPI.getGroups() != null) {
                if (multiInvAPI.getGroups().containsKey(worldname)) {
                    returnData.add(multiInvAPI.getGroups().get(worldname));
                }
            }
        }
        if (plugin.getWorldInvAPI()) {
            String worldname = world.getName();
            try {
                returnData.add(WorldInventoriesAPI.findGroup(worldname).getName());
            } catch (Exception ex) {
            }
        }
        if (returnData.isEmpty()) {
            returnData.add("");
        }
        return returnData;
    }
}
