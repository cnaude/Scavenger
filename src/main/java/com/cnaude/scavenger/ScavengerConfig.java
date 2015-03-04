package com.cnaude.scavenger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.yaml.snakeyaml.Yaml;

public final class ScavengerConfig {

    private final Configuration config;
    private final Scavenger plugin;
    private static final String langDir = "plugins/Scavenger/Languages";
    private static final String SHOULD_NOTIFY = "Global.Notify";
    private static final String ECONOMY_ENABLED = "Economy.Enabled";
    private static final String ECONOMY_RESTORECOST = "Economy.RestoreCost";
    private static final String ECONOMY_PERCENT = "Economy.Percent";
    private static final String ECONOMY_MINCOST = "Economy.MinCost";
    private static final String ECONOMY_MAXCOST = "Economy.MaxCost";
    private static final String ECONOMY_PERCENTCOST = "Economy.PercentCost";
    private static final String ECONOMY_ADDMIN = "Economy.AddMin";
    private static final String ECONOMY_DROP_CHANCE = "Economy.ChanceToDrop";
    private static final String ECONOMY_DEPOSIT_TYPE = "Economy.DepositType";
    private static final String ECONOMY_DEPOSIT_DESTINATION = "Economy.DepositDestination";
    private static final String GLOBAL_COLOR = "Global.Color";
    private static final String GLOBAL_TEXTCOLOR = "Global.TextColor";
    private static final String DEBUG_ENABLED = "Global.Debug";
    private static final String GLOBAL_SIDROPS = "Global.SingleItemDrops";
    private static final String GLOBAL_SIKEEPS = "Global.SingleItemKeeps";
    private static final String GLOBAL_PERMS = "Global.Permissions";
    private static final String GLOBAL_WGPVPIGNORE = "Global.WorldGuardPVPIgnore";
    private static final String GLOBAL_WGPVPONLY = "Global.WorldGuardPVPOnly";
    private static final String GLOBAL_OPSALLPERMS = "Global.OpsAllPerms";
    private static final String GLOBAL_FACTIONENEMYDROPS = "Global.FactionEnemyDrops";
    private static final String GLOBAL_OFFLINEMODE = "Global.OfflineMode";
    private static final String GLOBAL_DUNGEONMAZE = "Global.DungeonMaze";
    private static final String GLOBAL_RESIDENCE = "Global.Residence";
    private static final String GLOBAL_RESFLAG = "Global.ResidenceFlag";
    private static final String GLOBAL_DROPONPVPDEATH = "Global.DropOnPVPDeath";
    private static final String GLOBAL_LANGUAGE = "Global.LanguageFile";
    private static final String GLOBAL_ENABLED = "Global.Enabled";
    private static final String BLACKLISTED_WORLDS = "BlacklistedWorlds";
    private static final String BLACKLISTED_WGREGIONS = "BlacklistedWGRegions";
    private static final String GLOBAL_SLOT_RECOVERY = "Global.SlotBasedRecovery";
    private static final String GLOBAL_RESTOREDELAY = "Global.RestoreDelayTicks";    
    private static final String GLOBAL_BLACKLISTWARN = "Global.BlackListWarn";   
    //private static final String ECONOMY_GROUPS      = "Economy.Groups";
    private static final String MSG_RECOVERED = "MsgRecovered";
    private static final String MSG_SAVING = "MsgSaving";
    private static final String MSG_SAVEFORFEE = "MsgSaveForFee";
    private static final String MSG_NOTENOUGHMONEY = "MsgNotEnoughMoney";
    private static final String MSG_INSIDEPA = "MsgInsidePA";
    private static final String MSG_INSIDEBA = "MsgInsideBA";
    private static final String MSG_INSIDEMA = "MsgInsideMA";
    private static final String MSG_INSIDEUA = "MsgInsideUA";
    private static final String MSG_INSIDEWGPVP = "MsgInsideWGPVP";
    private static final String MSG_INSIDEWGPVPONLY = "MsgInsideWGPVPOnly";
    private static final String MSG_HEADER = "MsgHeader";
    private static final String MSG_INSIDEENEMYFACTION = "MsgInsideEnemyFaction";
    private static final String MSG_INSIDEDUNGEONMAZE = "MsgInsideDungeonMaze";
    private static final String MSG_INSIDERES = "MsgInsideRes";
    private static final String MSG_PVPDEATH = "PVPDeath";
    private static final String MSG_HEADER_DEF = "Scavenger";
    private static final String MSG_BLACKLISTEDWORLD = "MsgBlacklistedWorld";
    private static final String MSG_RECOVERED_DEF = "Your inventory has been restored.";
    private static final String MSG_SAVING_DEF = "Saving your inventory.";
    private static final String MSG_SAVEFORFEE_DEF = "Saving your inventory for a small fee of %COST% %CURRENCY%.";
    private static final String MSG_NOTENOUGHMONEY_DEF = "Item recovery cost is %COST% and you only have %BALANCE% %CURRENCY%.";
    private static final String MSG_INSIDEPA_DEF = "You are inside PVP Arena %ARENA%. Scavenger will not save your inventory.";
    private static final String MSG_INSIDEBA_DEF = "You are inside a Battle Arena. Scavenger will not save your inventory.";
    private static final String MSG_INSIDEMA_DEF = "You are inside a Mob Arena. Scavenger will not save your inventory.";
    private static final String MSG_INSIDEUA_DEF = "You are inside an Ultimate Arena. Scavenger will not save your inventory.";
    private static final String MSG_INSIDEWGPVP_DEF = "You are inside WorldGuard PVP region. Scavenger will not save your inventory.";
    private static final String MSG_INSIDEWGPVPONLY_DEF = "You are not inside a WorldGuard PVP region. Scavenger will not save your inventory.";
    private static final String MSG_INSIDEENEMYFACTION_DEF = "You died in enemy territory. Your items will be dropped!";
    private static final String MSG_INSIDEDUNGEONMAZE_DEF = "You died in a DungeonMaze. Your items will be dropped!";
    private static final String MSG_INSIDERES_DEF = "This residence does not allow item recovery! Dropping items!";
    private static final String MSG_PVPDEATH_DEF = "Killed by another player! Dropping items.";
    private static final String MSG_BLACKLISTEDWORLD_DEF = "Scavenger is disabled in this world. Be careful.";
    private static final String MSG_NOPERM = "MsgNoPerm";
    private static final String MSG_NOPERM_DEF = "You do not have permissions to do this!";
    private boolean shouldNotify;
    private double restoreCost;
    private boolean economyEnabled;
    private boolean debugEnabled;
    private boolean percent;
    private double minCost;
    private double maxCost;
    private double percentCost;
    private boolean addMin;
    private ChatColor headerColor;
    private ChatColor textColor;
    private boolean singleItemDrops;
    private boolean singleItemKeeps;
    private boolean permsEnabled;
    private String msgRecovered;
    private String msgSaving;
    private String msgSaveForFee;
    private String msgNotEnoughMoney;
    private String msgInsidePA;
    private String msgInsideBA;
    private String msgInsideMA;
    private String msgInsideUA;
    private boolean wgPVPIgnore;
    private boolean wgGuardPVPOnly;
    private String msgInsideWGPVP;
    private String msgInsideWGPVPOnly;
    private String msgBlacklistedWorld;
    private String msgNoPerm;
    private boolean opsAllPerms;
    private String msgHeader;
    private int chanceToDrop;
    private String msgInsideEnemyFaction;
    private String msgInsideDungeonMaze;
    private boolean factionEnemyDrops;
    private boolean offlineMode;
    private boolean dungeonMaze;
    private boolean residence;
    private String msgInsideRes;
    private String resFlag;
    private boolean dropOnPVPDeath;
    private String msgPVPDeath;
    private String languageFileName;
    private final List<String> blacklistedworlds = new ArrayList<>();
    private final List<String> blacklistedWGRegions = new ArrayList<>();
    private boolean slotBasedRecovery;
    private int restoreDelayTicks;
    private String depositType;
    private String depositDestination;
    private boolean blackListWarn;
    private boolean scvrEnabled;

    public ScavengerConfig(Scavenger plugin, Configuration config) {
        this.config = config;
        this.plugin = plugin;
        loadValues();
    }

    public void loadValues() {
        debugEnabled = config.getBoolean(DEBUG_ENABLED, false);
        scvrEnabled = config.getBoolean(GLOBAL_ENABLED, true);

        economyEnabled = config.getBoolean(ECONOMY_ENABLED, false);
        restoreCost = config.getDouble(ECONOMY_RESTORECOST, 10.0);
        percent = config.getBoolean(ECONOMY_PERCENT, false);
        minCost = config.getDouble(ECONOMY_MINCOST, 5.0);
        maxCost = config.getDouble(ECONOMY_MAXCOST, 0.0);
        percentCost = config.getDouble(ECONOMY_PERCENTCOST, 5.0);
        addMin = config.getBoolean(ECONOMY_ADDMIN, false);
        chanceToDrop = config.getInt(ECONOMY_DROP_CHANCE, 0);
        slotBasedRecovery = config.getBoolean(GLOBAL_SLOT_RECOVERY,false);
        depositType = config.getString(ECONOMY_DEPOSIT_TYPE,"none");
        depositDestination = config.getString(ECONOMY_DEPOSIT_DESTINATION,"");

        shouldNotify = config.getBoolean(SHOULD_NOTIFY, true);
        singleItemDrops = config.getBoolean(GLOBAL_SIDROPS, false);
        singleItemKeeps = config.getBoolean(GLOBAL_SIKEEPS, false);
        permsEnabled = config.getBoolean(GLOBAL_PERMS, true);
        wgPVPIgnore = config.getBoolean(GLOBAL_WGPVPIGNORE, false);
        wgGuardPVPOnly = config.getBoolean(GLOBAL_WGPVPONLY, false);
        opsAllPerms = config.getBoolean(GLOBAL_OPSALLPERMS, true);
        factionEnemyDrops = config.getBoolean(GLOBAL_FACTIONENEMYDROPS, false);
        offlineMode = config.getBoolean(GLOBAL_OFFLINEMODE, false);
        dungeonMaze = config.getBoolean(GLOBAL_DUNGEONMAZE, false);
        blackListWarn = config.getBoolean(GLOBAL_BLACKLISTWARN, false);
        residence = config.getBoolean(GLOBAL_RESIDENCE, false);
        resFlag = config.getString(GLOBAL_RESFLAG, "noscv");
        dropOnPVPDeath = config.getBoolean(GLOBAL_DROPONPVPDEATH, false);
        headerColor = ChatColor.valueOf(config.getString(GLOBAL_COLOR, "GOLD").toUpperCase());
        textColor = ChatColor.valueOf(config.getString(GLOBAL_TEXTCOLOR, "WHITE").toUpperCase());
        languageFileName = config.getString(GLOBAL_LANGUAGE, "English.yml");
        for (String s : config.getStringList(BLACKLISTED_WORLDS)) {            
            blacklistedworlds.add(s.toLowerCase());                            
        }
        for (String s : config.getStringList(BLACKLISTED_WGREGIONS)) {            
            blacklistedWGRegions.add(s.toLowerCase());                            
        }        
        restoreDelayTicks = config.getInt(GLOBAL_RESTOREDELAY, 10);
    
        initLangFiles(plugin);
        loadLanguage(plugin);

    }

    private void initLangFiles(Scavenger plug) {
        File dataFolder = new File(langDir);
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        @SuppressWarnings({"rawtypes", "unchecked"})
        ArrayList<String> langFiles = new ArrayList();
        langFiles.add("chinese.yml");
        langFiles.add("dutch.yml");
        langFiles.add("french.yml");
        langFiles.add("german.yml");
        langFiles.add("italian.yml");
        langFiles.add("korean.yml");        
        langFiles.add("portugues.yml");
        langFiles.add("russian.yml");
        langFiles.add("spanish.yml");
        

        for (String fName : langFiles) {
            File file = new File(langDir + "/" + fName);
            if (!file.exists()) {
                try {
                    InputStream in = Scavenger.class.getResourceAsStream("/" + fName);
                    byte[] buf = new byte[1024];
                    int len;
                    try (OutputStream out = new FileOutputStream(file)) {
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                    }
                } catch (IOException ex) {
                    plug.logError(ex.getMessage());
                }
            }
        }
    }

    private void loadLanguage(Scavenger plug) {
        boolean success = false;

        File file = new File(langDir + "/" + languageFileName);
        if (file.exists()) {
            Yaml yaml = new Yaml();
            try {
                plug.logInfo("Loading language file: " + file.getAbsolutePath());
                try (Reader reader = new FileReader(file)) {
                    @SuppressWarnings("unchecked")
                            Map<String, String> map = (Map<String, String>) yaml.load(reader);
                    if (map.containsKey(MSG_HEADER)) {
                        msgHeader = map.get(MSG_HEADER);
                    } else {
                        msgHeader = config.getString("Messages." + MSG_HEADER, MSG_HEADER_DEF);
                    }
                    if (map.containsKey(MSG_INSIDEENEMYFACTION)) {
                        msgInsideEnemyFaction = map.get(MSG_INSIDEENEMYFACTION);
                    } else {
                        msgInsideEnemyFaction = config.getString("Messages." + MSG_INSIDEENEMYFACTION, MSG_INSIDEENEMYFACTION_DEF);
                    }
                    if (map.containsKey(MSG_INSIDEDUNGEONMAZE)) {
                        msgInsideDungeonMaze = map.get(MSG_INSIDEDUNGEONMAZE);
                    } else {
                        msgInsideDungeonMaze = config.getString("Messages." + MSG_INSIDEDUNGEONMAZE, MSG_INSIDEDUNGEONMAZE_DEF);
                    }
                    if (map.containsKey(MSG_INSIDERES)) {
                        msgInsideRes = map.get(MSG_INSIDERES);
                    } else {
                        msgInsideRes = config.getString("Messages." + MSG_INSIDERES, MSG_INSIDERES_DEF);
                    }
                    if (map.containsKey(MSG_PVPDEATH)) {
                        msgPVPDeath = map.get(MSG_PVPDEATH);
                    } else {
                        msgPVPDeath = config.getString("Messages." + MSG_PVPDEATH, MSG_PVPDEATH_DEF);
                    }
                    if (map.containsKey(MSG_RECOVERED)) {
                        msgRecovered = map.get(MSG_RECOVERED);
                    } else {
                        msgRecovered = config.getString("Messages." + MSG_RECOVERED, MSG_RECOVERED_DEF);
                    }
                    if (map.containsKey(MSG_SAVING)) {
                        msgSaving = map.get(MSG_SAVING);
                    } else {
                        msgSaving = config.getString("Messages." + MSG_SAVING, MSG_SAVING_DEF);
                    }
                    if (map.containsKey(MSG_SAVEFORFEE)) {
                        msgSaveForFee = map.get(MSG_SAVEFORFEE);
                    } else {
                        msgSaveForFee = config.getString("Messages." + MSG_SAVEFORFEE, MSG_SAVEFORFEE_DEF);
                    }
                    if (map.containsKey(MSG_NOTENOUGHMONEY)) {
                        msgNotEnoughMoney = map.get(MSG_NOTENOUGHMONEY);
                    } else {
                        msgNotEnoughMoney = config.getString("Messages." + MSG_NOTENOUGHMONEY, MSG_NOTENOUGHMONEY_DEF);
                    }
                    if (map.containsKey(MSG_INSIDEPA)) {
                        msgInsidePA = map.get(MSG_INSIDEPA);
                    } else {
                        msgInsidePA = config.getString("Messages." + MSG_INSIDEPA, MSG_INSIDEPA_DEF);
                    }
                    if (map.containsKey(MSG_INSIDEBA)) {
                        msgInsideBA = map.get(MSG_INSIDEBA);
                    } else {
                        msgInsideBA = config.getString("Messages." + MSG_INSIDEBA, MSG_INSIDEBA_DEF);
                    }
                    if (map.containsKey(MSG_INSIDEMA)) {
                        msgInsideMA = map.get(MSG_INSIDEMA);
                    } else {
                        msgInsideMA = config.getString("Messages." + MSG_INSIDEMA, MSG_INSIDEMA_DEF);
                    }
                    if (map.containsKey(MSG_INSIDEUA)) {
                        msgInsideUA = map.get(MSG_INSIDEUA);
                    } else {
                        msgInsideUA = config.getString("Messages." + MSG_INSIDEUA, MSG_INSIDEUA_DEF);
                    }
                    if (map.containsKey(MSG_INSIDEWGPVP)) {
                        msgInsideWGPVP = map.get(MSG_INSIDEWGPVP);
                    } else {
                        msgInsideWGPVP = config.getString("Messages." + MSG_INSIDEWGPVP, MSG_INSIDEWGPVP_DEF);
                    }
                    if (map.containsKey(MSG_INSIDEWGPVPONLY)) {
                        msgInsideWGPVPOnly = map.get(MSG_INSIDEWGPVPONLY);
                    } else {
                        msgInsideWGPVPOnly = config.getString("Messages." + MSG_INSIDEWGPVPONLY, MSG_INSIDEWGPVPONLY_DEF);
                    }
                    if (map.containsKey(MSG_BLACKLISTEDWORLD)) {
                        msgBlacklistedWorld = map.get(MSG_BLACKLISTEDWORLD);
                    } else {
                        msgBlacklistedWorld = config.getString("Messages." + MSG_BLACKLISTEDWORLD, MSG_BLACKLISTEDWORLD_DEF);
                    }
                    if (map.containsKey(MSG_NOPERM)) {
                        msgNoPerm = map.get(MSG_NOPERM);
                    } else {
                        msgBlacklistedWorld = config.getString("Messages." + MSG_NOPERM, MSG_NOPERM_DEF);
                    }
                }
                success = true;
            } catch (IOException ex) {
                plug.logError("Error reading file: " + ex.getMessage());
                success = false;
            }
        }
        if (!success) {
            // Fall back to our default config.
            msgHeader = config.getString("Messages." + MSG_HEADER, MSG_HEADER_DEF);
            msgInsideEnemyFaction = config.getString("Messages." + MSG_INSIDEENEMYFACTION, MSG_INSIDEENEMYFACTION_DEF);
            msgInsideDungeonMaze = config.getString("Messages." + MSG_INSIDEDUNGEONMAZE, MSG_INSIDEDUNGEONMAZE_DEF);
            msgInsideRes = config.getString("Messages." + MSG_INSIDERES, MSG_INSIDERES_DEF);
            msgPVPDeath = config.getString("Messages." + MSG_PVPDEATH, MSG_PVPDEATH_DEF);
            msgRecovered = config.getString("Messages." + MSG_RECOVERED, MSG_RECOVERED_DEF);
            msgSaving = config.getString("Messages." + MSG_SAVING, MSG_SAVING_DEF);
            msgSaveForFee = config.getString("Messages." + MSG_SAVEFORFEE, MSG_SAVEFORFEE_DEF);
            msgNotEnoughMoney = config.getString("Messages." + MSG_NOTENOUGHMONEY, MSG_NOTENOUGHMONEY_DEF);
            msgInsidePA = config.getString("Messages." + MSG_INSIDEPA, MSG_INSIDEPA_DEF);
            msgInsideBA = config.getString("Messages." + MSG_INSIDEBA, MSG_INSIDEBA_DEF);
            msgInsideMA = config.getString("Messages." + MSG_INSIDEMA, MSG_INSIDEMA_DEF);
            msgInsideUA = config.getString("Messages." + MSG_INSIDEUA, MSG_INSIDEUA_DEF);
            msgInsideWGPVP = config.getString("Messages." + MSG_INSIDEWGPVP, MSG_INSIDEWGPVP_DEF);
            msgInsideWGPVPOnly = config.getString("Messages." + MSG_INSIDEWGPVPONLY, MSG_INSIDEWGPVPONLY_DEF);
            msgBlacklistedWorld = config.getString("Messages." + MSG_BLACKLISTEDWORLD, MSG_BLACKLISTEDWORLD_DEF);
            msgNoPerm = config.getString("Messages." + MSG_NOPERM, MSG_NOPERM_DEF);
        }
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

    public boolean singleItemKeeps() {
        return singleItemKeeps;
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
    
    public String msgInsideBA() {
        return msgInsideBA;
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
    
    public String msgNoPerm() {
        return msgNoPerm;
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

    public int chanceToDrop() {
        return chanceToDrop;
    }

    public String msgInsideEnemyFaction() {
        return msgInsideEnemyFaction;
    }
    
    public String msgInsideDungeonMaze() {
    	return msgInsideDungeonMaze;
    }
    
    public boolean dungeonMazeDrops() {
    	return dungeonMaze;
    }

    public boolean factionEnemyDrops() {
        return factionEnemyDrops;
    }

    public boolean offlineMode() {
        return offlineMode;
    }

    public boolean residence() {
        return residence;
    }

    public String msgInsideRes() {
        return msgInsideRes;
    }

    public String resFlag() {
        return resFlag;
    }

    public boolean dropOnPVPDeath() {
        return dropOnPVPDeath;
    }

    public String msgPVPDeath() {
        return msgPVPDeath;
    }

    public List<String> blacklistedWorlds() {
        return blacklistedworlds;
    }
    
    public List<String> blacklistedWGRegions() {
        return blacklistedWGRegions;
    }    
    
    public boolean slotBasedRecovery() {
        return slotBasedRecovery;
    }
    
    public int restoreDelayTicks() {
        return restoreDelayTicks;
    }
    
    public String depositType() {
        return depositType;
    }
    
    public String depositDestination() {
        return depositDestination;
    }
    
    public String MsgBlacklistedWorld() {
        return msgBlacklistedWorld;
    }
    
    public boolean blackListWarn() {
        return blackListWarn;
    }
    
    public void setEnabled(boolean enabled) {
        scvrEnabled = enabled;
        config.set(GLOBAL_ENABLED, enabled);
        plugin.saveConfig();
    }

    public boolean isScavengerEnabled() {
        return scvrEnabled;
    }
            
}