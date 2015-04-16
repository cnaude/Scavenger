package com.cnaude.scavenger;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import com.comphenix.protocol.utility.StreamSerializer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;

import java.io.*;
import java.util.*;
import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.match.Match;
import me.cnaude.plugin.Scavenger.RestorationS1;
import me.drayshak.WorldInventories.api.WorldInventoriesAPI;
import me.x128.xInventories.Main;
import net.dmulloy2.ultimatearena.UltimateArenaAPI;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import uk.co.tggl.pluckerpluck.multiinv.MultiInvAPI;

public final class RestorationManager implements Serializable {

    Scavenger plugin;
    private static final HashMap<String, Restoration> restorations = new HashMap<>();

    final String PERM_PREFIX = "scavenger.";
    final String PERM_DROP_PREFIX = PERM_PREFIX + "drop.";
    final String PERM_DROP_ALL = PERM_PREFIX + "drop.*";
    final String PERM_KEEP_PREFIX = PERM_PREFIX + "keep.";
    final String PERM_KEEP_ALL = PERM_PREFIX + "keep.*";
    final String PERM_SCAVENGE_PREFIX = PERM_PREFIX + "scavenge.";
    final String PERM_SCAVENGE = PERM_PREFIX + "scavenge";
    final String PERM_FREE = PERM_PREFIX + "free";
    final String PERM_NO_CHANCE = PERM_PREFIX + "nochance";
    final String PERM_LEVEL = PERM_PREFIX + "level";
    final String PERM_EXP = PERM_PREFIX + "exp";

    public RestorationManager(Scavenger plugin) {
        this.plugin = plugin;
        this.load();
    }

    public void save() {
        StreamSerializer serializer = new StreamSerializer();
        HashMap<String, RestorationS1> restorationsForDisk = new HashMap<>();
        for (Map.Entry<String, Restoration> entry : restorations.entrySet()) {
            String key = entry.getKey();
            Restoration value = entry.getValue();
            RestorationS1 tmpRestoration = new RestorationS1();
            for (ItemStack i : value.inventory) {
                boolean error = false;
                if (i instanceof ItemStack) {
                    plugin.logDebug("Serializing: " + i.toString());
                    try {
                        tmpRestoration.inventory.add(serializer.serializeItemStack(i.clone()));
                    } catch (IOException e) {
                        plugin.logError(e.getMessage());
                        error = true;
                    }
                    if (error) {
                        plugin.logError("Problem serializing item: " + i.toString());
                    }
                    plugin.logDebug("Done: " + i.toString());
                }
            }
            for (ItemStack i : value.armour) {
                if (i instanceof ItemStack) {
                    plugin.logDebug("Serializing: " + i.toString());
                    try {
                        tmpRestoration.armour.add(serializer.serializeItemStack(i.clone()));
                    } catch (IOException e) {
                        plugin.logError(e.getMessage());
                    }
                    plugin.logDebug("Done: " + i.toString());
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
            try (ObjectOutputStream obj_out = new ObjectOutputStream(f_out)) {
                obj_out.writeObject(restorationsForDisk);
            }
        } catch (IOException e) {
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
            try (ObjectInputStream obj_in = new ObjectInputStream(f_in)) {
                restorationsFromDisk = (HashMap<String, RestorationS1>) obj_in.readObject();
            }
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
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
                    plugin.logDebug("Deserializing: " + value.inventory.get(i));
                    try {
                        tmpStack = serializer.deserializeItemStack(value.inventory.get(i));
                    } catch (Exception e) {
                        plugin.logError(e.getMessage() + " => " + value.inventory.get(i));
                        error = true;
                    } catch (Throwable e) {
                        plugin.logError(e.getMessage() + " => " + value.inventory.get(i));
                        error = true;
                    }
                    if (tmpStack == null || error) {
                        tmpRestoration.inventory[i] = new ItemStack(Material.AIR);
                    } else {
                        tmpRestoration.inventory[i] = tmpStack;
                    }
                    plugin.logDebug("Done: " + tmpRestoration.inventory[i].toString());
                }
            }

            for (int i = 0; i < value.armour.size(); i++) {
                if (value.armour.get(i) instanceof String) {
                    ItemStack tmpStack = new ItemStack(Material.AIR);
                    plugin.logDebug("Deserializing: " + value.armour.get(i));
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
                    plugin.logDebug("Done: " + tmpRestoration.armour[i].toString());
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
        return plugin.getMultiInvAPI() != null
                || plugin.myWorldsHook != null
                || plugin.getWorldInvAPI()
                || plugin.getXInventories() != null;
    }

    public boolean hasRestoration(Player player) {
        UUID uuid = player.getUniqueId();
        if (multipleInventories()) {
            return restorations.containsKey(uuid + "." + getWorldGroups(player));
        }
        return restorations.containsKey(uuid.toString());
    }

    public Restoration getRestoration(Player player) {
        UUID uuid = player.getUniqueId();
        Restoration restoration = new Restoration();
        restoration.enabled = false;
        if (multipleInventories()) {
            String keyName = uuid + "." + getWorldGroups(player);
            if (restorations.containsKey(keyName)) {
                plugin.logDebug("Getting: " + keyName);
                restoration = restorations.get(keyName);
            }
        }
        if (!restoration.enabled) {
            String keyName = player.getUniqueId().toString();
            if (restorations.containsKey(keyName)) {
                plugin.logDebug("Getting: " + keyName + ":" + player.getName());
                restoration = restorations.get(keyName);
            }
        }
        return restoration;
    }

    public void collect(Player player, List<ItemStack> itemDrops, EntityDeathEvent event) {
        if (itemDrops.isEmpty() && !levelAllow(player) && !expAllow(player)) {
            return;
        }

        if (plugin.config.dropOnPVPDeath() && player.getKiller() instanceof Player) {
            plugin.message(player, plugin.config.msgPVPDeath());
            return;
        }

        if (plugin.config.residence()) {
            ClaimedResidence res = Residence.getResidenceManager().getByLoc(player.getLocation());
            if (res != null) {
                ResidencePermissions perms = res.getPermissions();
                if (perms.playerHas(player.getName(), plugin.config.resFlag(), true)) {
                    plugin.logDebug("Player '" + player.getName() + "' is not allowed to use Scavenger in this residence. Items will be dropped.");
                    plugin.message(player, plugin.config.msgInsideRes());
                    return;
                } else {
                    plugin.logDebug("Player '" + player.getName() + "' is allowed to use Scavenger in this residence.");

                }
            }
        }

        if (plugin.config.factionEnemyDrops() && plugin.factionHook != null) {
            if (plugin.factionHook.isPlayerInEnemyFaction(player)) {
                return;
            }
        }

        if (plugin.config.dungeonMazeDrops() && plugin.dmHook != null) {
            plugin.logDebug("Checking if '" + player.getName() + "' is in DungeonMaze.");
            if (plugin.dmHook.isPlayerInDungeon(player)) {
                plugin.logDebug("Player '" + player.getName() + "' is in DungeonMaze.");
                plugin.message(player, plugin.config.msgInsideDungeonMaze());
                return;
            }
        }

        if (plugin.getWorldGuard() != null) {
            plugin.logDebug("Checking region support for '" + player.getWorld().getName() + "'");
            if (plugin.getWorldGuard().getRegionManager(player.getWorld()) != null) {
                try {
                    RegionManager regionManager = plugin.getWorldGuard().getRegionManager(player.getWorld());
                    ApplicableRegionSet set = regionManager.getApplicableRegions(player.getLocation());
                    if (set.allows(DefaultFlag.PVP) && plugin.config.wgPVPIgnore()) {
                        plugin.logDebug("This is a WorldGuard PVP zone and WorldGuardPVPIgnore is " + plugin.config.wgPVPIgnore());
                        if (!plugin.config.msgInsideWGPVP().isEmpty()) {
                            plugin.message(player, plugin.config.msgInsideWGPVP());
                        }
                        return;
                    }
                    if (!set.allows(DefaultFlag.PVP) && plugin.config.wgGuardPVPOnly()) {
                        plugin.logDebug("This is NOT a WorldGuard PVP zone and WorldGuardPVPOnly is " + plugin.config.wgGuardPVPOnly());
                        if (!plugin.config.msgInsideWGPVP().isEmpty()) {
                            plugin.message(player, plugin.config.msgInsideWGPVPOnly());
                        }
                        return;
                    }
                } catch (NullPointerException ex) {
                    plugin.logDebug(ex.getMessage());
                }
            } else {
                plugin.logDebug("Region support disabled for '" + player.getWorld().getName() + "'");
            }
        }

        if (plugin.getUltimateArena() != null) {
            plugin.getUltimateArena();
            if (UltimateArenaAPI.hookIntoUA(plugin).isPlayerInArenaLocation(player)) {
                if (!plugin.config.msgInsideUA().isEmpty()) {
                    plugin.message(player, plugin.config.msgInsideUA());
                }
                return;
            }
        }

        if (plugin.maHandler != null && plugin.maHandler.isPlayerInArena(player)) {
            if (!plugin.config.msgInsideMA().isEmpty()) {
                plugin.message(player, plugin.config.msgInsideMA());
            }
            return;
        }

        if (plugin.pvpHandler != null && !net.slipcor.pvparena.api.PVPArenaAPI.getArenaName(player).equals("")) {
            String x = plugin.config.msgInsidePA();
            if (!x.isEmpty()) {
                x = x.replaceAll("%ARENA%", net.slipcor.pvparena.api.PVPArenaAPI.getArenaName(player));
                plugin.message(player, x);
            }
            return;
        }

        if (plugin.battleArena) {
            mc.alk.arena.objects.ArenaPlayer ap = mc.alk.arena.BattleArena.toArenaPlayer(player);
            if (ap != null) {
                Match match = BattleArena.getBAController().getMatch(ap);
                if (match != null) {
                    if (match.isInMatch(ap)) {
                        String x = plugin.config.msgInsideBA();
                        if (!x.isEmpty()) {
                            plugin.message(player, x);
                        }
                        return;
                    }
                }
            }
        }

        if (plugin.minigames != null) {
            if (plugin.minigames.playerInMinigame(player)) {
                plugin.logInfo("Player '" + player.getName() + "' is in a Minigame. Not recovering items.");
                return;
            }
        }

        if (hasRestoration(player)) {
            plugin.error(player, "Restoration already exists, ignoring.");
            return;
        }

        if (plugin.getEconomy() != null
                && !(player.hasPermission(PERM_FREE)
                || (player.isOp() && plugin.config.opsAllPerms()))
                && plugin.config.economyEnabled()) {
            double restoreCost = plugin.config.restoreCost();
            double withdrawAmount;
            double playeBalance = plugin.getEconomy().getBalance(player.getName());
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
            er = plugin.getEconomy().withdrawPlayer(player.getName(), withdrawAmount);
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
                    plugin.message(player, x);
                }
                if (!plugin.config.depositDestination().isEmpty()) {
                    plugin.logDebug("DepositDesination: " + plugin.config.depositDestination());
                    if (plugin.config.depositType().equalsIgnoreCase("bank")) {
                        plugin.logDebug("DepositType: BANK");
                        if (plugin.getEconomy().hasBankSupport()) {
                            plugin.logDebug("Bank support is enabled");
                            plugin.getEconomy().bankDeposit(plugin.config.depositDestination(), withdrawAmount);
                        } else {
                            plugin.logDebug("Bank support is NOT enabled");
                        }
                    } else if (plugin.config.depositType().equalsIgnoreCase("player")) {
                        plugin.logDebug("DepositType: PLAYER");
                        plugin.logDebug("DepositDestination: " + plugin.config.depositDestination());
                        if (plugin.getEconomy().hasAccount(plugin.config.depositDestination())) {
                            plugin.logDebug("DepositDestination: VALID");
                            plugin.getEconomy().depositPlayer(plugin.config.depositDestination(), withdrawAmount);
                        } else {
                            plugin.logDebug("DepositDestination: INVALID");
                        }
                    } else {
                        plugin.logDebug("DepositType: INVALID");
                    }
                } else {
                    plugin.logDebug("No deposit destination!");
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
                    plugin.message(player, x);
                }
                return;
            }
        } else {
            plugin.message(player, plugin.config.msgSaving());
        }

        Restoration restoration = new Restoration();
        restoration.enabled = false;

        restoration.inventory = player.getInventory().getContents();
        restoration.armour = player.getInventory().getArmorContents();
        itemDrops.clear();

        if (levelAllow(player)) {
            plugin.logDebug("Collecting level " + player.getLevel() + " for " + player.getName());
            restoration.level = player.getLevel();
        }
        if (expAllow(player)) {
            plugin.logDebug("Collecting exp " + player.getExp() + " for " + player.getName());
            restoration.exp = player.getExp();
            event.setDroppedExp(0);
        }

        String deathCause = "NULL";
        if (player.getLastDamageCause() != null) {
            if (player.getLastDamageCause().getCause() != null) {
                deathCause = player.getLastDamageCause().getCause().toString();
            }
        }
        String deathCausePermission = PERM_SCAVENGE_PREFIX + deathCause;
        plugin.logDebug("[p:" + player.getName() + "] [" + PERM_SCAVENGE + ":" + player.hasPermission(PERM_SCAVENGE) + "]"
                + " [" + deathCausePermission + ":" + player.hasPermission(deathCausePermission) + "]");
        if (player.hasPermission(PERM_SCAVENGE) || player.hasPermission(deathCausePermission)) {
            plugin.logDebug("Permissions are okay. Time to scavenge...");
            if (plugin.config.chanceToDrop() > 0 && !player.hasPermission(PERM_NO_CHANCE)) {
                checkChanceToDropItems(restoration.armour, itemDrops);
                checkChanceToDropItems(restoration.inventory, itemDrops);
            }
            if (plugin.config.singleItemDrops()) {
                checkSingleItemDrops(player, restoration.armour, itemDrops);
                checkSingleItemDrops(player, restoration.inventory, itemDrops);
            } else if (plugin.config.singleItemKeeps()) {
                checkSingleItemKeeps(player, "armour", restoration.armour, itemDrops);
                checkSingleItemKeeps(player, "inv", restoration.inventory, itemDrops);
            } else if (plugin.config.slotBasedRecovery()) {
                checkSlots(player, "armour", restoration.armour, itemDrops);
                checkSlots(player, "inv", restoration.inventory, itemDrops);
            }
        } else {
            plugin.logDebug("Permissions are NOT okay. Dropping items...");
            dropItems(restoration.armour, itemDrops);
            dropItems(restoration.inventory, itemDrops);
        }
        addRestoration(player, restoration);
    }

    private void dropItem(ItemStack itemStack, List<ItemStack> itemDrops) {
        if (itemStack == null) {
            plugin.logDebug("Ignoring null item");
            return;
        }
        plugin.logDebug("Dropping item " + itemStack.getType());
        itemDrops.add(itemStack.clone());
        itemStack.setAmount(0);
    }

    private void dropItems(ItemStack[] itemStackArray, List<ItemStack> itemDrops) {
        for (ItemStack itemStack : itemStackArray) {
            if (itemStack instanceof ItemStack && !itemStack.getType().equals(Material.AIR)) {
                dropItem(itemStack, itemDrops);
            }
        }
    }

    private void checkChanceToDropItems(ItemStack[] itemStackArray, List<ItemStack> itemDrops) {
        for (ItemStack itemStack : itemStackArray) {
            if (itemStack instanceof ItemStack && !itemStack.getType().equals(Material.AIR)) {
                Random randomGenerator = new Random();
                int randomInt = randomGenerator.nextInt(plugin.config.chanceToDrop()) + 1;
                plugin.logDebug("Random number is " + randomInt);
                if (randomInt == plugin.config.chanceToDrop()) {
                    plugin.logDebug("Randomly dropping item " + itemStack.getType());
                    dropItem(itemStack, itemDrops);
                } else {
                    plugin.logDebug("Randomly keeping item " + itemStack.getType());
                }
            }
        }
    }

    private boolean checkSlot(Player player, String type, int slot, String itemType) {
        final String PERM = PERM_PREFIX + type + "." + slot;
        plugin.logDebug("[p:" + player.getName() + "] " + "[" + PERM + ":" + player.hasPermission(PERM) + "] " + "[item:" + itemType + "]");
        return (player.hasPermission(PERM));
    }

    private void checkSlots(Player player, String invType, ItemStack[] itemStackArray, List<ItemStack> itemDrops) {
        for (int slot = 0; slot < itemStackArray.length; slot++) {
            String itemType;
            if (itemStackArray[slot] != null) {
                itemType = itemStackArray[slot].getType().name();
            } else {
                itemType = "NULL";
            }
            if (!checkSlot(player, invType, slot, itemType)) {
                plugin.logDebug("[cs]Dropping slot " + slot + ": " + itemType);
                dropItem(itemStackArray[slot], itemDrops);
            } else {
                plugin.logDebug("[cs]Keeping slot " + slot + ": " + itemType);
            }
        }
    }

    private boolean checkPerms(Player player, List<String> perms) {
        String debugMessage = "[p:" + player.getName() + "]";
        boolean bool = false;
        for (String perm : perms) {
            debugMessage += " [" + perm + ":" + player.hasPermission(perm) + "]";
            if (player.hasPermission(perm)) {
                bool = true;
            }
        }
        plugin.logDebug(debugMessage);
        return bool;
    }

    private boolean checkSingleItemDrop(Player player, ItemStack itemStack) {
        String itemType = itemStack.getType().name();
        int itemTypeId = itemStack.getTypeId();
        List<String> perms = new ArrayList<>();
        perms.add(PERM_DROP_PREFIX + itemTypeId);
        perms.add(PERM_DROP_PREFIX + itemType.toLowerCase());
        perms.add(PERM_DROP_PREFIX + itemType);
        perms.add(PERM_DROP_ALL);
        return checkPerms(player, perms);
    }

    private void checkSingleItemDrops(Player player, ItemStack[] itemStackArray, List<ItemStack> itemDrops) {
        plugin.logDebug("checkSingleItemDrops()");
        for (ItemStack itemStack : itemStackArray) {
            if (itemStack instanceof ItemStack && !itemStack.getType().equals(Material.AIR)) {
                String itemType = itemStack.getType().name();
                if (checkSingleItemDrop(player, itemStack)) {
                    plugin.logDebug("[sd]Dropping item: " + itemType);
                    dropItem(itemStack, itemDrops);
                } else {
                    plugin.logDebug("[sd]Keeping item: " + itemType);
                }
            }
        }
    }

    private boolean checkSingleItemKeep(Player player, ItemStack itemStack) {
        String itemType = itemStack.getType().toString();
        int itemTypeId = itemStack.getTypeId();
        List<String> perms = new ArrayList<>();
        perms.add(PERM_KEEP_PREFIX + itemTypeId);
        perms.add(PERM_KEEP_PREFIX + itemType.toLowerCase());
        perms.add(PERM_KEEP_PREFIX + itemType);
        perms.add(PERM_DROP_ALL);
        return checkPerms(player, perms);
    }

    private void checkSingleItemKeeps(Player player, String invType, ItemStack[] itemStackArray, List<ItemStack> itemDrops) {
        plugin.logDebug("checkSingleItemKeeps(" + invType + ")");
        for (int slot = 0; slot < itemStackArray.length; slot++) {
            if (itemStackArray[slot] instanceof ItemStack && !itemStackArray[slot].getType().equals(Material.AIR)) {
                String itemType = itemStackArray[slot].getType().name();
                if (checkSingleItemKeep(player, itemStackArray[slot])) {
                    plugin.logDebug("[sk]Keeping item: " + itemType);
                    continue;
                } 
                if (plugin.config.slotBasedRecovery() && plugin.config.useTheOrMethod()) {
                    if (checkSlot(player, invType, slot, itemType)) {
                        plugin.logDebug("[cs]Keeping item: " + itemType);
                        continue;
                    }
                }
                plugin.logDebug("[sk]Dropping item: " + itemType);
                dropItem(itemStackArray[slot], itemDrops);
            }
        }
    }

    public boolean levelAllow(Player player) {
        return (player.hasPermission(PERM_LEVEL)
                || !plugin.config.permsEnabled()
                || (player.isOp() && plugin.config.opsAllPerms())) && player.getLevel() > 0;
    }

    public boolean expAllow(Player player) {
        return (player.hasPermission(PERM_EXP)
                || !plugin.config.permsEnabled()
                || (player.isOp() && plugin.config.opsAllPerms())) && player.getExhaustion() > 0;
    }

    public void printRestorations(CommandSender sender) {
        plugin.message(sender, "Restorations:");
        for (String key : restorations.keySet()) {
            plugin.message(sender, "  " + key);
        }
    }

    public void addRestoration(Player player, Restoration restoration) {
        UUID uuid = player.getUniqueId();
        if (multipleInventories()) {
            String keyName = uuid + "." + getWorldGroups(player);
            restorations.put(keyName, restoration);
            plugin.logDebug("Adding: " + player.getDisplayName() + ":" + keyName);
        } else {
            restorations.put(uuid.toString(), restoration);
            plugin.logDebug("Adding: " + player.getDisplayName() + ":" + uuid);
        }
    }

    public void enable(Player player) {
        if (hasRestoration(player)) {
            Restoration restoration = getRestoration(player);
            restoration.enabled = true;
            plugin.logDebug("Enabling: " + player.getName());
        } else {
            plugin.logDebug("Not enabling: " + player.getName());
        }
    }

    public void restore(Player player) {
        if (!player.isOnline()) {
            plugin.logDebug("Player " + player.getName() + " is offline. Skipping restore...");
            return;
        }

        Restoration restoration = getRestoration(player);

        if (restoration.enabled) {
            player.getInventory().clear();

            player.getInventory().setContents(restoration.inventory);
            player.getInventory().setArmorContents(restoration.armour);
            if (player.hasPermission(PERM_LEVEL)
                    || !plugin.config.permsEnabled()
                    || (player.isOp() && plugin.config.opsAllPerms())) {
                plugin.logDebug("Player " + player.getName() + " does have " + PERM_LEVEL + " permission.");
                player.setLevel(restoration.level);
                plugin.logDebug("Player " + player.getName() + " gets " + restoration.level + " level.");
            } else {
                plugin.logDebug("Player " + player.getName() + " does NOT have " + PERM_LEVEL + " permission.");
            }
            if (player.hasPermission(PERM_EXP)
                    || !plugin.config.permsEnabled()
                    || (player.isOp() && plugin.config.opsAllPerms())) {
                plugin.logDebug("Player " + player.getName() + " does have " + PERM_EXP + " permission.");
                player.setExp(restoration.exp);
                plugin.logDebug("Player " + player.getName() + " gets " + restoration.exp + " XP.");
            } else {
                plugin.logDebug("Player " + player.getName() + " does NOT have " + PERM_EXP + " permission.");
            }
            if (plugin.config.shouldNotify()) {
                plugin.message(player, plugin.config.msgRecovered());
            }
            removeRestoration(player);
            if (hasRestoration(player)) {
                plugin.message(player, "Restore exists!!!");
            }
        }

    }

    public boolean hasRestoration(UUID uuid) {
        return restorations.containsKey(uuid.toString());
    }

    public void removeRestoration(UUID uuid) {
        if (hasRestoration(uuid)) {
            restorations.remove(uuid.toString());
            plugin.logDebug("Removing: " + uuid);
        }
    }

    public void removeRestoration(Player player) {
        UUID uuid = player.getUniqueId();
        if (multipleInventories()) {
            String keyName = uuid + "." + getWorldGroups(player);
            if (restorations.containsKey(keyName)) {
                restorations.remove(keyName);
                plugin.logDebug("Removing: " + keyName);
            }
        }
        removeRestoration(uuid);
    }

    public String getWorldGroups(Player player) {
        World world = player.getWorld();
        List<String> returnData = new ArrayList<>();
        
        if (plugin.getXInventories() != null) {
            Main xInventories = plugin.getXInventories();
            String xGroup = xInventories.getConfig().getString("worlds." + world.getName());
            plugin.logDebug("xGroup: " + xGroup);
            if (xGroup != null) {
                returnData.add(xGroup);
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
        if (plugin.myWorldsHook != null) {
            if (plugin.myWorldsHook.isEnabled()) {
                returnData.add(plugin.myWorldsHook.getLocationName(player.getLocation()));
            }
        }
        if (returnData.isEmpty()) {
            returnData.add("");
        }
        return returnData.get(0);
    }
}
