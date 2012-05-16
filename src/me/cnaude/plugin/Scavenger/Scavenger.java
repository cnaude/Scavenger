package me.cnaude.plugin.Scavenger;

import com.garbagemule.MobArena.MobArena;
import com.garbagemule.MobArena.MobArenaHandler;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.orange451.UltimateArena.main;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.api.PVPArenaAPI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Scavenger extends JavaPlugin {
    public static final String PLUGIN_NAME = "Scavenger";
    public static final String LOG_HEADER = "[" + PLUGIN_NAME + "]";
    private static Scavenger instance = null;

    private static Vault vault = null;
    private static Economy economy = null;
    public static MobArenaHandler maHandler;
    public static PVPArenaAPI pvpHandler;    
    public static MultiverseInventories multiverseHandler;
    
    public boolean configLoaded = false;
    
    static final Logger log = Logger.getLogger("Minecraft");    
    private ScavengerConfig config;    
    private final ScavengerEventListener eventListener = new ScavengerEventListener(this);

    public static Scavenger get() {
        return instance;
    }

    @Override
    public void onEnable() {   
        loadConfig();
                       
        setupMobArenaHandler();
        setupPVPArenaHandler();
        checkForWorldGuard();
        
        getServer().getPluginManager().registerEvents(eventListener, this);
        
        RestorationManager.load(this);
        ScavengerIgnoreList.load(this);
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
    
    @Override
    public void onDisable() {
        RestorationManager.save(this);
        ScavengerIgnoreList.save(this);
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
    public WorldGuardPlugin getWorldGuard() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null; // Maybe you want throw an exception instead
        }
        return (WorldGuardPlugin) plugin;
    }
    
    public main getUltimateArena() {
        Plugin plugin = getServer().getPluginManager().getPlugin("UltimateArena");
        
        if (plugin == null || !(plugin instanceof main)) {
            return null; 
        }
        return (main) plugin;
    }
    
    public void logInfo(String _message) {
        log.log(Level.INFO,String.format("%s %s",LOG_HEADER,_message));
    }
    
    public void logDebug(String _message) {
        if (getSConfig().debugEnabled()) {
            log.log(Level.INFO,String.format("%s [DEBUG] %s",LOG_HEADER,_message));
        }
    }
    
    public void logError(String _message) {
        log.log(Level.SEVERE,String.format("%s %s",LOG_HEADER,_message));
    }
    
    public ScavengerConfig getSConfig() {
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
            if(x != null && x instanceof Vault) {
                vault = (Vault) x;
                if(setupEconomy()) {
                    logInfo("Scavenger has linked to " + economy.getName() + " through Vault");                    
                    if (this.getSConfig().percent()) {                                                 
                        if (this.getSConfig().addMin()) {
                            logInfo("Item recovery fee: "+this.getSConfig().percentCost()+
                                    "% + "+this.getSConfig().minCost());
                        } else {
                            logInfo("Item recovery fee: "+this.getSConfig().percentCost()+
                                    "% (Min: "+this.getSConfig().minCost()+
                                    ") (Max: "+this.getSConfig().maxCost()+")");                    
                        }
                    } else {
                        logInfo("Item recovery fee: "+this.getSConfig().restoreCost());
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
        instance = this;
    }
    
    public void setupMobArenaHandler() {
        Plugin maPlugin = (MobArena) getServer().getPluginManager().getPlugin("MobArena");

        if (maPlugin == null)
            return;

        maHandler = new MobArenaHandler();
        logInfo("MobArena detected. Player inventory restores ignored inside arenas.");
    }
    
    public void setupPVPArenaHandler() {
        Plugin pvpPlugin = (PVPArena) getServer().getPluginManager().getPlugin("pvparena");

        if (pvpPlugin == null)
            return;

        pvpHandler = new PVPArenaAPI();
        logInfo("PVPArena detected. Player inventory restores ignored inside arenas.");
    }
    
   
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandlabel, String[] args){
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if(commandlabel.equalsIgnoreCase("scvr") || commandlabel.equalsIgnoreCase("scavengerreload")) {
                if (p.hasPermission("scavenger.reload")) {
                
                    this.loadConfig();
                    message(p,"Configuration reloaded.");
                } else {
                    message(p,"No permission to reload scavenger config!");
                }                
            }
            if(commandlabel.equalsIgnoreCase("scvron")) {
                if (p.hasPermission("scavenger.self.on")
                        || (p.isOp() && getSConfig().opsAllPerms())) {                
                    ScavengerIgnoreList.removePlayer(sender.getName(), this);
                    message(p,"You have enabled item recovery for yourself!");
                } else {
                    message(p,"No permission to do this!");
                }
            }
            if(commandlabel.equalsIgnoreCase("scvroff")) {
                if (p.hasPermission("scavenger.self.off")
                        || (p.isOp() && getSConfig().opsAllPerms())) {
                    ScavengerIgnoreList.addPlayer(sender.getName(), this);
                    message(p,"You have disabled item recovery for yourself!");
                } else {
                    message(p,"No permission to do this!");
                }
            }
        } else if (sender instanceof ConsoleCommandSender) {
            if(commandlabel.equalsIgnoreCase("scvr") || commandlabel.equalsIgnoreCase("scavengerreload")) {               
                this.loadConfig();                        
            }
        }
        return true;
    }
    
    private String headerStr() {
        ChatColor headerColor = getSConfig().headerColor();
        ChatColor textColor = getSConfig().textColor();
        return textColor + "[" + headerColor + PLUGIN_NAME + textColor + "] " + textColor;
    }
    
    public void message(Player _player, String _message) {
        if (_player != null && getSConfig().shouldNotify())
            _player.sendMessage(headerStr() + _message);
        else
            logInfo(_message);
    }
    
    public void debugMessage(Player _player, String _message) {
        if (getSConfig().debugEnabled()) {
            if (_player != null)
                _player.sendMessage(headerStr() + _message);
            else
                logDebug(_message);
        }
    }
    
    public void debugMessage(String _message) {
        if (getSConfig().debugEnabled()) 
                logDebug(_message);
    }

    public void error(Player _player, String _message) {
        if (_player != null && getSConfig().shouldNotify())
            _player.sendMessage(headerStr() + ChatColor.RED + "Error: " + _message);            
        else
            logError(_message);
    }

    
    
   
}
