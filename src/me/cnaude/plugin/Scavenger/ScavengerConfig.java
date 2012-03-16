package me.cnaude.plugin.Scavenger;

import org.bukkit.configuration.Configuration;

public class ScavengerConfig {
    private final Configuration config;
    
    private static final String SHOULD_NOTIFY       = "Global.Notify";
    private static final String ECONOMY_ENABLED     = "Economy.Enabled";
    private static final String ECONOMY_RESTORECOST = "Economy.RestoreCost";
    private static final String ECONOMY_PERCENT     = "Economy.Percent";
    private static final String ECONOMY_MINCOST     = "Economy.MinCost";
    private static final String ECONOMY_PERCENTCOST = "Economy.PercentCost";
    private static final String ECONOMY_ADDMIN      = "Economy.AddMin";
    //private static final String ECONOMY_GROUPS      = "Economy.Groups";
    
    private boolean shouldNotify;
    private double  restoreCost;
    private boolean economyEnabled;
    private boolean percent;
    private double  minCost;
    private double  percentCost;
    private boolean addMin;

    public ScavengerConfig(Scavenger plug) {
        config         = plug.getConfig();        
        economyEnabled = config.getBoolean(ECONOMY_ENABLED, false);
        shouldNotify   = config.getBoolean(SHOULD_NOTIFY, true);
        restoreCost    = config.getDouble(ECONOMY_RESTORECOST, 10.0);
        percent        = config.getBoolean(ECONOMY_PERCENT, false);
        minCost        = config.getDouble(ECONOMY_MINCOST, 10.0);
        percentCost    = config.getDouble(ECONOMY_PERCENTCOST, 5.0);
        addMin         = config.getBoolean(ECONOMY_ADDMIN, false);
    }
    
    public boolean shouldNotify() {
        return shouldNotify;
    }
    
    public double restoreCost() {
        return restoreCost;
    }
    
    public boolean economyEnabled() {
        return economyEnabled;
    }
    
    public boolean percent() {
        return percent;
    }
    
    public boolean addMin() {
        return addMin;
    }
    
    public double minCost() {
        return minCost;
    }
        
    public double percentCost() {
        return percentCost;
    }
}
