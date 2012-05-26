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
    private static final String GLOBAL_PERMS        = "Global.Permissions";
    private static final String GLOBAL_WGPVPIGNORE  = "Global.WorldGuardPVPIgnore";
    private static final String GLOBAL_WGPVPONLY    = "Global.WorldGuardPVPOnly";
    private static final String GLOBAL_OPSALLPERMS  = "Global.OpsAllPerms";
    
    //private static final String ECONOMY_GROUPS      = "Economy.Groups";
    private static final String MSG_RECOVERED       = "Messages.MsgRecovered";
    private static final String MSG_SAVING          = "Messages.MsgSaving";
    private static final String MSG_SAVEFORFEE      = "Messages.MsgSaveForFee";
    private static final String MSG_NOTENOUGHMONEY  = "Messages.MsgNotEnoughMoney";
    private static final String MSG_INSIDEPA        = "Messages.MsgInsidePA";
    private static final String MSG_INSIDEMA        = "Messages.MsgInsideMA";
    private static final String MSG_INSIDEUA        = "Messages.MsgInsideUA";
    private static final String MSG_INSIDEWGPVP     = "Messages.MsgInsideWGPVP";
    private static final String MSG_INSIDEWGPVPONLY = "Messages.MsgInsideWGPVPOnly";
    private static final String MSG_HEADER          = "Messages.MsgHeader";
    
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
    private boolean permsEnabled;
    private String msgRecovered;
    private String msgSaving;
    private String msgSaveForFee;
    private String msgNotEnoughMoney;
    private String msgInsidePA;
    private String msgInsideMA;
    private String msgInsideUA;
    private boolean wgPVPIgnore;
    private boolean wgGuardPVPOnly;
    private String msgInsideWGPVP;
    private String msgInsideWGPVPOnly;
    private boolean opsAllPerms;
    private String msgHeader;
    

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
        permsEnabled        = config.getBoolean(GLOBAL_PERMS, true);
        msgRecovered        = config.getString(MSG_RECOVERED, "Your inventory has been restored.");
        msgSaving           = config.getString(MSG_SAVING, "Saving your inventory.");
        msgSaveForFee       = config.getString(MSG_SAVEFORFEE, "Saving your inventory for a small fee of %COST% %CURRENCY%.");
        msgNotEnoughMoney   = config.getString(MSG_NOTENOUGHMONEY, "Item recovery cost is %COST% and you only have %BALANCE% %CURRENCY%.");
        msgInsidePA         = config.getString(MSG_INSIDEPA, "You are inside PVP Arena %ARENA%. Scavenger will not save your inventory.");
        msgInsideMA         = config.getString(MSG_INSIDEMA, "You are inside a Mob Arena. Scavenger will not save your inventory.");
        msgInsideUA         = config.getString(MSG_INSIDEUA, "You are inside an Ultimate Arena. Scavenger will not save your inventory.");
        msgInsideWGPVP      = config.getString(MSG_INSIDEWGPVP, "You are inside WorldGuard PVP region. Scavenger will not save your inventory.");
        msgInsideWGPVPOnly  = config.getString(MSG_INSIDEWGPVPONLY, "You are not inside a WorldGuard PVP region. Scavenger will not save your inventory.");
        wgPVPIgnore         = config.getBoolean(GLOBAL_WGPVPIGNORE, false);
        wgGuardPVPOnly      = config.getBoolean(GLOBAL_WGPVPONLY, false);
        opsAllPerms         = config.getBoolean(GLOBAL_OPSALLPERMS, true);
        msgHeader           = config.getString(MSG_HEADER, "Scavenger");
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
    
    public boolean permsEnabled() {
        return permsEnabled;
    }
    
    public String msgRecovered() {
        return msgRecovered;
    }
    
    public String msgSaving() {
        return msgSaving;
    }
    
    public String msgSaveForFee() {
        return msgSaveForFee;
    }
        
    public String msgNotEnoughMoney() {
        return msgNotEnoughMoney;
    }
    
    public String msgInsidePA() {
        return msgInsidePA;
    }
    
    public String msgInsideMA() {
        return msgInsideMA;
    }
    
    public String msgInsideUA() {
        return msgInsideUA;
    }
    
    public String msgInsideWGPVP() {
        return msgInsideWGPVP;
    }
    
    public String msgInsideWGPVPOnly() {
        return msgInsideWGPVPOnly;
    }
    
    public String msgHeader() {
        return msgHeader;
    }
    
    public boolean wgPVPIgnore() {
        return wgPVPIgnore;
    }
    
    public boolean wgGuardPVPOnly() {
        return wgGuardPVPOnly;
    }
    
    public boolean opsAllPerms() {
        return opsAllPerms;
    }
}