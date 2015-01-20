package com.cnaude.scavenger;

import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.cnaude.scavenger.Hooks.ScavengerDungeonMaze;
import com.cnaude.scavenger.Hooks.ScavengerFactions;
import com.cnaude.scavenger.Hooks.ScavengerMyWorlds;
import com.garbagemule.MobArena.MobArena;
import com.garbagemule.MobArena.MobArenaHandler;
import com.pauldavdesign.mineauz.minigames.Minigames;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.dmulloy2.ultimatearena.UltimateArenaAPI;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import net.slipcor.pvparena.PVPArena;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;
import uk.co.tggl.pluckerpluck.multiinv.MultiInvAPI;

public class Scavenger extends JavaPlugin {

    public static final String PLUGIN_NAME = "Scavenger";
    public static final String LOG_HEADER = "[" + PLUGIN_NAME + "]";
    private Economy economy = null;
    public MobArenaHandler maHandler;
    public PVPArena pvpHandler;
    public MultiInv multiinvHandler;
    public RestorationManager rm;
    public boolean battleArena = false;
    public boolean minigames = false;
    public ScavengerIgnoreList ignoreList;
    public boolean configLoaded = false;
    static final Logger log = Logger.getLogger("Minecraft");
    public ScavengerConfig config;
    private ScavengerEventListenerOffline eventListener;
    private ScavengerEventListenerOnline eventListenerOnline;
    public ScavengerFactions factionHook = null;
    public ScavengerDungeonMaze dmHook = null;
    public ScavengerMyWorlds myWorldsHook = null;

    @Override
    public void onEnable() {
        if (!checkForProtocolLib()) {
            logError("This plugin requires ProtocolLib. Please download the latest: http://dev.bukkit.org/server-mods/protocollib/");
            Bukkit.getServer().getPluginManager().disablePlugin(this);
        } else {

            loadConfig();

            for (String s : config.blacklistedWorlds()) {
                logDebug("BlackListedWorld: " + s);
            }

            setupMobArenaHandler();
            setupPVPArenaHandler();
            checkForUltimateArena();
            checkForBattleArena();
            checkForWorldGuard();
            checkForFactions();
            checkForDungeonMaze();
            checkForMyWorlds();
            setupResidence();

            rm = new RestorationManager(this);
            eventListener = new ScavengerEventListenerOffline(this, rm);
            eventListenerOnline = new ScavengerEventListenerOnline(this, rm);
            ignoreList = new ScavengerIgnoreList(this);

            if (config.offlineMode()) {
                Plugin p = Bukkit.getServer().getPluginManager().getPlugin("Authenticator");
                {
                    if (p != null) { //if Authenticator is present..
                        if (fr.areku.Authenticator.Authenticator.isUsingOfflineModePlugin()) { // .. and has detected a auth plugin ..
                            getServer().getPluginManager().registerEvents(eventListener, this); // ..register the listener
                            logInfo("Hook to Authenticator's API and your auth plugin.");
                        } else {
                            logInfo("No Auth plugin detected. Set offline-mode to false or add an auth plugin.");
                            getServer().getPluginManager().registerEvents(eventListenerOnline, this);
                        }
                    } else {
                        logInfo("Authenticator not detected. Set offline-mode to false or add Authenticator.");
                        getServer().getPluginManager().registerEvents(eventListenerOnline, this);
                    }
                }
            } else {
                getServer().getPluginManager().registerEvents(eventListenerOnline, this);
                logInfo("Offline-mode is set to false, no Authenticator Hook");
            }
            getServer().getPluginManager().registerEvents(new ScavengerEvents(this, rm), this);
        }
    }

    private void checkForWorldGuard() {
        if (getWorldGuard() != null && config.wgPVPIgnore()) {
            logInfo("WorldGuard detected. Scavenger will not recover items in PVP regions.");
        }
    }

    private void checkForUltimateArena() {
        if (getUltimateArena() != null) {
            logInfo("UltimateArena detected. Scavenger will not recover items in an arena.");
        }
    }

    private void checkForBattleArena() {
        Plugin baPlugin = getServer().getPluginManager().getPlugin("BattleArena");

        if (baPlugin != null) {
            battleArena = true;
            logInfo("BattleArena detected. Scavenger will not recover items in an arena.");
        }
    }

    private void checkForFactions() {
        if (isFactionsLoaded() && config.factionEnemyDrops()) {
            factionHook = new ScavengerFactions(this);
            logInfo("Factions detected. Players will drop items in enemy teritory!");
        }
    }

    private void checkForDungeonMaze() {
        if (isDungeonMazeLoaded() && config.dungeonMazeDrops()) {
            dmHook = new ScavengerDungeonMaze();
            logInfo("DungeonMaze detected, drop item in the dungeon maze worlds configs.");
        }
    }

    @Override
    public void onDisable() {
        if (checkForProtocolLib()) {
            rm.save();
            ignoreList.save();
        }
    }

    public Economy getEconomy() {
        return economy;
    }

    private Boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }

    public MultiInvAPI getMultiInvAPI() {
        Plugin plugin = getServer().getPluginManager().getPlugin("MultiInv");
        MultiInvAPI multiInvAPI = null;

        try {
            if (plugin instanceof MultiInv) {
                multiInvAPI = ((MultiInv) plugin).getAPI();
            }
        } catch (NoClassDefFoundError ex) {
        }
        return multiInvAPI;
    }

    public boolean getWorldInvAPI() {
        //Plugin plugin = getServer().getPluginManager().getPlugin("WorldInventories");
        try {
            Class.forName("me.drayshak.WorldInventories.api.WorldInventoriesAPI");
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    public Minigames getMinigames() {
        Plugin plugin = getServer().getPluginManager().getPlugin("Minigames");
        Minigames mg = null;
        if (plugin != null) {
            mg = (Minigames) Bukkit.getServer().getPluginManager().getPlugin("Minigames");
        }
        return mg;
    }

    public boolean isFactionsLoaded() {
        return (getServer().getPluginManager().getPlugin("Factions") != null);
    }

    public boolean isDungeonMazeLoaded() {
        return (getServer().getPluginManager().getPlugin("DungeonMaze") != null);
    }

    public boolean checkForProtocolLib() {
        Plugin plugin = getServer().getPluginManager().getPlugin("ProtocolLib");
        return plugin != null;
    }

    public void checkForMyWorlds() {
        Plugin plugin = getServer().getPluginManager().getPlugin("MyWorlds");
        if (plugin != null) {
            myWorldsHook = new ScavengerMyWorlds();
            logInfo("MyWorlds detected.");
        }
    }

    public void setupResidence() {
        if (config.residence()) {
            PluginManager pm = getServer().getPluginManager();
            Plugin p = pm.getPlugin("Residence");
            if (p != null) {
                if (!p.isEnabled()) {
                    logInfo("Manually enabling Residence!");
                    pm.enablePlugin(p);
                }
                logInfo("Adding '" + config.resFlag() + "' flag to Residence.");
                FlagPermissions.addResidenceOnlyFlag(config.resFlag());
                FlagPermissions.addFlag(config.resFlag());
            } else {
                logInfo("Residence NOT Installed!");
            }
        } else {
            logDebug("Residence support disabled via config.");
        }
    }

    public WorldGuardPlugin getWorldGuard() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if (plugin != null)  {
            if (!plugin.getDescription().getVersion().startsWith("6.")) {
                logInfo("Invalid version of WorldGuard detected. Please use 6.0.0 or newer.");
                plugin = null;
            }
        }

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null; // Maybe you want throw an exception instead
        }
        return (WorldGuardPlugin) plugin;
    }

    public void logInfo(String _message) {
        log.log(Level.INFO, String.format("%s %s", LOG_HEADER, _message));
    }

    public void logDebug(String _message) {
        if (config.debugEnabled()) {
            log.log(Level.INFO, String.format("%s [DEBUG] %s", LOG_HEADER, _message));
        }
    }

    public void logError(String _message) {
        log.log(Level.SEVERE, String.format("%s %s", LOG_HEADER, _message));
    }

    void loadConfig() {
        if (!this.configLoaded) {
            getConfig().options().copyDefaults(true);
            saveConfig();
            logInfo("Configuration loaded.");
            config = new ScavengerConfig(this);
        } else {
            reloadConfig();
            getConfig().options().copyDefaults(false);
            config = new ScavengerConfig(this);
            logInfo("Configuration reloaded.");
        }

        if (config.economyEnabled()) {
            Plugin x = getServer().getPluginManager().getPlugin("Vault");
            if (x != null && x instanceof Vault) {
                if (setupEconomy()) {
                    logInfo("Scavenger has linked to " + economy.getName() + " through Vault");
                    if (config.percent()) {
                        if (config.addMin()) {
                            logInfo("Item recovery fee: " + config.percentCost()
                                    + "% + " + config.minCost());
                        } else {
                            logInfo("Item recovery fee: " + config.percentCost()
                                    + "% (Min: " + config.minCost()
                                    + ") (Max: " + config.maxCost() + ")");
                        }
                    } else {
                        logInfo("Item recovery fee: " + config.restoreCost());
                    }
                } else {
                    logError("Vault could not find an Economy plugin installed!");
                }
            } else {
                logInfo("Scavenger relies on Vault for economy support and Vault isn't installed!");
                logInfo("See http://dev.bukkit.org/server-mods/vault/");
                logInfo("If you don't want economy support, set 'Economy: Enabled' to false in Scavenger config.");
            }
        } else {
            logInfo("Economy disabled. Item recovery will be free.");
        }
        configLoaded = true;
    }

    public UltimateArenaAPI getUltimateArena() {
        Plugin uaPlugin = getServer().getPluginManager().getPlugin("UltimateArena");

        if (uaPlugin == null) {
            return null;
        }

        return UltimateArenaAPI.hookIntoUA(this);
    }

    public void setupMobArenaHandler() {
        Plugin maPlugin = (MobArena) getServer().getPluginManager().getPlugin("MobArena");

        if (maPlugin == null) {
            return;
        }

        maHandler = new MobArenaHandler();
        logInfo("MobArena detected. Player inventory restores ignored inside arenas.");
    }

    public void setupPVPArenaHandler() {
        Plugin pvpPlugin = (PVPArena) getServer().getPluginManager().getPlugin("pvparena");

        if (pvpPlugin == null) {
            return;
        }

        pvpHandler = net.slipcor.pvparena.PVPArena.instance;

        logInfo("PVPArena detected. Player inventory restores ignored inside arenas.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandlabel, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (commandlabel.equalsIgnoreCase("scvr") || commandlabel.equalsIgnoreCase("scavengerreload")) {
                if (p.hasPermission("scavenger.reload")
                        || (p.isOp() && config.opsAllPerms())) {
                    loadConfig();
                    message(p, "Configuration reloaded.");
                } else {
                    message(p, "No permission to reload scavenger config!");
                }
            }
            if (commandlabel.equalsIgnoreCase("scvron")) {
                if (p.hasPermission("scavenger.self.on")
                        || (p.isOp() && config.opsAllPerms())
                        || !config.permsEnabled()) {
                    ignoreList.removePlayer(sender.getName());
                    message(p, "You have enabled item recovery for yourself!");
                } else {
                    message(p, "No permission to do this!");
                }
            }
            if (commandlabel.equalsIgnoreCase("scvroff")) {
                if (p.hasPermission("scavenger.self.off")
                        || (p.isOp() && config.opsAllPerms())
                        || !config.permsEnabled()) {
                    ignoreList.addPlayer(sender.getName());
                    message(p, "You have disabled item recovery for yourself!");
                } else {
                    message(p, "No permission to do this!");
                }
            }
            if (commandlabel.equalsIgnoreCase("scvrlist")) {
                if (p.hasPermission("scavenger.list")
                        || (p.isOp() && config.opsAllPerms())
                        || !config.permsEnabled()) {
                    rm.printRestorations(p);
                } else {
                    message(p, "No permission to do this!");
                }
            }
        } else if (sender instanceof ConsoleCommandSender) {
            if (commandlabel.equalsIgnoreCase("scvr") || commandlabel.equalsIgnoreCase("scavengerreload")) {
                loadConfig();
            }
            if (commandlabel.equalsIgnoreCase("scvrlist")) {
                rm.printRestorations();
            }
        }
        return true;
    }

    private String headerStr() {
        ChatColor headerColor = config.headerColor();
        ChatColor textColor = config.textColor();
        if (config.msgHeader().isEmpty()) {
            return textColor + "[" + headerColor + PLUGIN_NAME + textColor + "] " + textColor;
        } else {
            return textColor + "[" + headerColor + config.msgHeader() + textColor + "] " + textColor;
        }
    }

    public void message(Player p, String msg) {
        if (p instanceof Player) {
            msg = msg.replaceAll("%PLAYER%", p.getName());
            msg = msg.replaceAll("%DPLAYER%", p.getDisplayName());
            if (config.shouldNotify()) {
                p.sendMessage(headerStr() + msg);
            } else {
                logInfo(msg);
            }
        }
    }

    public void debugMessage(Player _player, String _message) {
        if (config.debugEnabled()) {
            if (_player != null) {
                _player.sendMessage(headerStr() + _message);
            } else {
                logDebug(_message);
            }
        }
    }

    public void debugMessage(String _message) {
        if (config.debugEnabled()) {
            logDebug(_message);
        }
    }

    public void error(Player _player, String _message) {
        if (_player != null && config.shouldNotify()) {
            _player.sendMessage(headerStr() + ChatColor.RED + "Error: " + _message);
        } else {
            logError(_message);
        }
    }
}
