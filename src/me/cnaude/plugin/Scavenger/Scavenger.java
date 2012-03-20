package me.cnaude.plugin.Scavenger;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import com.garbagemule.MobArena.MobArena;
import com.garbagemule.MobArena.MobArenaHandler;

public class Scavenger extends JavaPlugin {
    public static final String PLUGIN_NAME = "Scavenger";
    public static final String LOG_HEADER = "[" + PLUGIN_NAME + "] ";
    private static Scavenger instance = null;

    private static Vault vault = null;
    private static Economy economy = null;
    public static MobArenaHandler maHandler;
    
    private Logger log;    
    private ScavengerConfig config;    
    private CommunicationManager communicationManager;
    private RestorationManager restorationManager;
    private final ScavengerEventListener eventListener = new ScavengerEventListener(this);

    public static Scavenger get() {
        return instance;
    }

    @Override
    public void onEnable() {
        this.loadConfig();
        
        log = Logger.getLogger("Minecraft");

        Plugin plugin = getServer().getPluginManager().getPlugin(PLUGIN_NAME);           
      
        communicationManager = new CommunicationManager();  
        
        if (this.getSConfig().economyEnabled()) {
            Plugin x = getServer().getPluginManager().getPlugin("Vault");
            if(x != null && x instanceof Vault) {
                vault = (Vault) x;
                if(setupEconomy()) {
                    logInfo("Scavenger has linked to " + economy.getName() + " through Vault");                    
                    if (this.getSConfig().percent()) {                                                 
                        if (this.getSConfig().addMin()) {
                            logInfo("Item recovery fee: "+this.getSConfig().percentCost()+"% + "+this.getSConfig().minCost());
                        } else {
                            logInfo("Item recovery fee: "+this.getSConfig().percentCost()+"% (Min: "+this.getSConfig().minCost()+")");                    
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
        
        setupMobArenaHandler();
        getServer().getPluginManager().registerEvents(eventListener, this);
    }
        
    public CommunicationManager getCommunicationManager() {
        return communicationManager;
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
    
    public void logInfo(String _message) {
        log.log(Level.INFO,LOG_HEADER + _message);
    }

    public void logError(String _message) {
        log.log(Level.SEVERE,LOG_HEADER + _message);
    }
    
    public ScavengerConfig getSConfig() {
        return config;
    }
    
    void loadConfig() {
        getConfig().options().copyDefaults(true);
        saveConfig();
        config = new ScavengerConfig(this);
    }
    
    public void setupMobArenaHandler() {
        Plugin maPlugin = (MobArena) getServer().getPluginManager().getPlugin("MobArena");

        if (maPlugin == null)
            return;

        maHandler = new MobArenaHandler();
        logInfo("MobArena detected. Player inventory restores ignored inside arenas.");
    }
   
}