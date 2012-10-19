package me.cnaude.plugin.Scavenger;

import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.garbagemule.MobArena.MobArena;
import com.garbagemule.MobArena.MobArenaHandler;
import com.massivecraft.factions.P;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.orange451.UltimateArena.*;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.api.PVPArenaAPI;
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

@SuppressWarnings("unused")
public class Scavenger extends JavaPlugin {

    public static final String PLUGIN_NAME = "Scavenger";
    public static final String LOG_HEADER = "[" + PLUGIN_NAME + "]";
    private static Scavenger instance = null;
    private static Economy economy = null;
    public static MobArenaHandler maHandler;
    public static PVPArenaAPI pvpHandler;
    public static MultiverseInventories multiverseHandler;
    public static MultiInv multiinvHandler;
    public static RestorationManager rm;
    public static boolean battleArena = false;
    public static ScavengerIgnoreList ignoreList;
    public boolean configLoaded = false;
    static final Logger log = Logger.getLogger("Minecraft");
    private static ScavengerConfig config;
    private final ScavengerEventListener eventListener = new ScavengerEventListener();
    private final ScavengerEventListener_Online eventListenerOnline = new ScavengerEventListener_Online();

    public static Scavenger get() {
        return instance;
    }

    @Override
    public void onEnable() {
        loadConfig();

        setupMobArenaHandler();
        setupPVPArenaHandler();
        checkForUltimateArena();
        checkForBattleArena();
        checkForWorldGuard();
        checkForFactions();
        setupResidence();

        rm = new RestorationManager();
        rm.load();
        ignoreList = new ScavengerIgnoreList();
        ignoreList.load();
        if (getSConfig().offlineMode()) {
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
    }

    private void checkForWorldGuard() {
        if (getWorldGuard() != null && getSConfig().wgPVPIgnore()) {
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
        if (getFactions() != null && getSConfig().factionEnemyDrops()) {
            logInfo("Factions detected. Players will drop items in enemy teritory!");
        }
    }

    @Override
    public void onDisable() {
        rm.save();
        ignoreList.save();
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

    public MultiverseInventories getMultiverseInventories() {
        Plugin plugin = getServer().getPluginManager().getPlugin("Multiverse-Inventories");

        if (plugin == null || !(plugin instanceof MultiverseInventories)) {
            return null;
        }
        return (MultiverseInventories) plugin;
    }

    public MultiInv getMultiInvInventories() {
        Plugin plugin = getServer().getPluginManager().getPlugin("MultiInv");

        if (plugin == null || !(plugin instanceof MultiInv)) {
            return null;
        }
        return (MultiInv) plugin;
    }

    public P getFactions() {
        Plugin plugin = getServer().getPluginManager().getPlugin("Factions");

        if (plugin == null || !(plugin instanceof P)) {
            return null;
        }
        return (P) plugin;
    }

    public void setupResidence() {
        if (getSConfig().residence()) {
            PluginManager pm = getServer().getPluginManager();
            Plugin p = pm.getPlugin("Residence");
            if (p != null) {
                if (!p.isEnabled()) {
                    logInfo("Manually enabling Residence!");
                    pm.enablePlugin(p);
                }
                logInfo("Adding '" + getSConfig().resFlag() + "' flag to Residence.");
                FlagPermissions.addResidenceOnlyFlag(getSConfig().resFlag());
                FlagPermissions.addFlag(getSConfig().resFlag());
            } else {
                logInfo("Residence NOT Installed!");
            }
        } else {
            logDebug("Residence support disabled via config.");
        }
    }

    public WorldGuardPlugin getWorldGuard() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

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
        if (getSConfig().debugEnabled()) {
            log.log(Level.INFO, String.format("%s [DEBUG] %s", LOG_HEADER, _message));
        }
    }

    public void logError(String _message) {
        log.log(Level.SEVERE, String.format("%s %s", LOG_HEADER, _message));
    }

    public static ScavengerConfig getSConfig() {
        return config;
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
                    if (getSConfig().percent()) {
                        if (getSConfig().addMin()) {
                            logInfo("Item recovery fee: " + getSConfig().percentCost()
                                    + "% + " + getSConfig().minCost());
                        } else {
                            logInfo("Item recovery fee: " + getSConfig().percentCost()
                                    + "% (Min: " + getSConfig().minCost()
                                    + ") (Max: " + getSConfig().maxCost() + ")");
                        }
                    } else {
                        logInfo("Item recovery fee: " + getSConfig().restoreCost());
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
        instance = this;
        configLoaded = true;
    }

    public UltimateArenaAPI getUltimateArena() {
        Plugin uaPlugin = getServer().getPluginManager().getPlugin("UltimateArena");

        if (uaPlugin == null) {
            return null;
        }

        return com.orange451.UltimateArena.UltimateArenaAPI.hookIntoUA();
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

        pvpHandler = new PVPArenaAPI();
        logInfo("PVPArena detected. Player inventory restores ignored inside arenas.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandlabel, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (commandlabel.equalsIgnoreCase("scvr") || commandlabel.equalsIgnoreCase("scavengerreload")) {
                if (p.hasPermission("scavenger.reload")
                        || (p.isOp() && getSConfig().opsAllPerms())) {
                    loadConfig();
                    message(p, "Configuration reloaded.");
                } else {
                    message(p, "No permission to reload scavenger config!");
                }
            }
            if (commandlabel.equalsIgnoreCase("scvron")) {
                if (p.hasPermission("scavenger.self.on")
                        || (p.isOp() && getSConfig().opsAllPerms())
                        || !getSConfig().permsEnabled()) {
                    ignoreList.removePlayer(sender.getName());
                    message(p, "You have enabled item recovery for yourself!");
                } else {
                    message(p, "No permission to do this!");
                }
            }
            if (commandlabel.equalsIgnoreCase("scvroff")) {
                if (p.hasPermission("scavenger.self.off")
                        || (p.isOp() && getSConfig().opsAllPerms())
                        || !getSConfig().permsEnabled()) {
                    ignoreList.addPlayer(sender.getName());
                    message(p, "You have disabled item recovery for yourself!");
                } else {
                    message(p, "No permission to do this!");
                }
            }
            if (commandlabel.equalsIgnoreCase("scvrlist")) {
                if (p.hasPermission("scavenger.list")
                        || (p.isOp() && getSConfig().opsAllPerms())
                        || !getSConfig().permsEnabled()) {
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
        ChatColor headerColor = getSConfig().headerColor();
        ChatColor textColor = getSConfig().textColor();
        if (Scavenger.getSConfig().msgHeader().isEmpty()) {
            return textColor + "[" + headerColor + PLUGIN_NAME + textColor + "] " + textColor;
        } else {
            return textColor + "[" + headerColor + Scavenger.getSConfig().msgHeader() + textColor + "] " + textColor;
        }
    }

    public void message(Player p, String msg) {
        if (p instanceof Player) {
            msg = msg.replaceAll("%PLAYER%", p.getName());
            msg = msg.replaceAll("%DPLAYER%", p.getDisplayName());
            if (getSConfig().shouldNotify()) {
                p.sendMessage(headerStr() + msg);
            } else {
                logInfo(msg);
            }
        }
    }

    public void debugMessage(Player _player, String _message) {
        if (getSConfig().debugEnabled()) {
            if (_player != null) {
                _player.sendMessage(headerStr() + _message);
            } else {
                logDebug(_message);
            }
        }
    }

    public void debugMessage(String _message) {
        if (getSConfig().debugEnabled()) {
            logDebug(_message);
        }
    }

    public void error(Player _player, String _message) {
        if (_player != null && getSConfig().shouldNotify()) {
            _player.sendMessage(headerStr() + ChatColor.RED + "Error: " + _message);
        } else {
            logError(_message);
        }
    }
  
}