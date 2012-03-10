package me.cnaude.plugin.Scavenger;

import org.bukkit.configuration.Configuration;

public class ScavengerConfig {
    private final Configuration config;
    
    private static final String SHOULD_NOTIFY       = "Global.Notify";
    private static final String ECONOMY_ENABLED     = "Economy.Enabled";
    private static final String ECONOMY_RESTORECOST = "Economy.RestoreCost";
    
    private boolean shouldNotify;
    private double  getFee;
    private boolean economyEnabled;

    public ScavengerConfig(Scavenger plug) {
        config = plug.getConfig();        
        shouldNotify = config.getBoolean(SHOULD_NOTIFY, true);
        getFee = config.getDouble(ECONOMY_RESTORECOST, 10.0);
        economyEnabled = config.getBoolean(ECONOMY_ENABLED, false);
    }
    
    public boolean shouldNotify() {
        return shouldNotify;
    }
    
    public double getFee() {
        return getFee;
    }
    
    public boolean economyEnabled() {
        return economyEnabled;
    }
}
