package me.cnaude.plugin.Scavenger;

import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;

public final class ScavengerConfig {
    private final Configuration config;
    
    private static final String SHOULD_NOTIFY       = "Global.Notify";
    private static final String ECONOMY_ENABLED     = "Economy.Enabled";
    private static final String ECONOMY_RESTORECOST = "Economy.RestoreCost";
    private static final String ECONOMY_PERCENT     = "Economy.Percent";
    private static final String ECONOMY_MINCOST     = "Economy.MinCost";
    private static final String ECONOMY_MAXCOST     = "Economy.MaxCost";
    private static final String ECONOMY_PERCENTCOST = "Economy.PercentCost";
    private static final String ECONOMY_ADDMIN      = "Economy.AddMin";
    private static final String GLOBAL_COLOR        = "Global.Color";
    private static final String GLOBAL_TEXTCOLOR    = "Global.TextColor";
    private static final String DEBUG_ENABLED       = "Global.Debug";
    private static final String GLOBAL_SIDROPS      = "Global.SingleItemDrops";
    private static final String GLOBAL_SIDROPS_ONLY = "Global.SingleItemDropsOnly";
    //private static final String ECONOMY_GROUPS      = "Economy.Groups";
    
    private boolean shouldNotify;
    private double  restoreCost;
    private boolean economyEnabled;
    private boolean debugEnabled;
    private boolean percent;
    private double  minCost;
    private double  maxCost;
    private double  percentCost;
    private boolean addMin;
    private ChatColor headerColor;
    private ChatColor textColor;
    private boolean singleItemDrops;
    private boolean singleItemDropsOnly;

    public ScavengerConfig(Scavenger plug) {
        config = plug.getConfig();       
        loadValues();
    }
    
    public void loadValues() {
        economyEnabled      = config.getBoolean(ECONOMY_ENABLED, false);
        debugEnabled        = config.getBoolean(DEBUG_ENABLED, false);
        shouldNotify        = config.getBoolean(SHOULD_NOTIFY, true);
        restoreCost         = config.getDouble(ECONOMY_RESTORECOST, 10.0);
        percent             = config.getBoolean(ECONOMY_PERCENT, false);
        minCost             = config.getDouble(ECONOMY_MINCOST, 5.0);
        maxCost             = config.getDouble(ECONOMY_MAXCOST, 0.0);
        percentCost         = config.getDouble(ECONOMY_PERCENTCOST, 5.0);
        addMin              = config.getBoolean(ECONOMY_ADDMIN, false);
        headerColor         = ChatColor.valueOf(config.getString(GLOBAL_COLOR, "GOLD").toUpperCase());
        textColor           = ChatColor.valueOf(config.getString(GLOBAL_TEXTCOLOR, "WHITE").toUpperCase());
        singleItemDrops     = config.getBoolean(GLOBAL_SIDROPS, false);
        singleItemDropsOnly = config.getBoolean(GLOBAL_SIDROPS_ONLY, false);
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
    
    public boolean debugEnabled() {
        return debugEnabled;
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
    
    public double maxCost() {
        return maxCost;
    }
        
    public double percentCost() {
        return percentCost;
    }
    
    public ChatColor headerColor() {
        return headerColor;
    }
    
    public ChatColor textColor() {
        return textColor;
    }
    
    public boolean singleItemDrops() {
        return singleItemDrops;
    }
    
    public boolean singleItemDropsOnly() {
        return singleItemDropsOnly;
    }
}
