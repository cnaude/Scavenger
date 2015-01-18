package com.cnaude.scavenger;

import java.io.*;
import java.util.ArrayList;

/**
 *
 * @author naudec
 */
@SuppressWarnings("serial")
public final class ScavengerIgnoreList implements Serializable {

    private static ArrayList<String> ignoreList = new ArrayList<>();
    private static final String IGNORE_FILE = "plugins/Scavenger/ignores.ser";
    
    Scavenger plugin;
    
    public ScavengerIgnoreList(Scavenger plugin) {
        this.plugin = plugin;
        this.load();
    }

    @SuppressWarnings("unchecked")
    public void load() {
        File file = new File(IGNORE_FILE);
        if (!file.exists()) {
            plugin.logDebug("Ignore file '" + file.getAbsolutePath() + "' does not exist.");
            return;
        }
        try {
            FileInputStream f_in = new FileInputStream(file);
            try (ObjectInputStream obj_in = new ObjectInputStream(f_in)) {
                ignoreList = (ArrayList<String>) obj_in.readObject();
            }
            plugin.logInfo("Loaded ignore list. (Count = " + ignoreList.size() + ")");
        } catch (IOException | ClassNotFoundException e) {
            plugin.logError(e.getMessage());
        }
    }

    public void save() {
        try {
            File file = new File(IGNORE_FILE);
            FileOutputStream f_out = new FileOutputStream(file);
            try (ObjectOutputStream obj_out = new ObjectOutputStream(f_out)) {
                obj_out.writeObject(ignoreList);
            }
            plugin.logInfo("Saved ignore list. (Count = " + ignoreList.size() + ")");
        } catch (Exception e) {
            plugin.logError(e.getMessage());
        }
    }

    public void addPlayer(String s) {
        if (ignoreList.contains(s)) {
            return;
        }
        plugin.logInfo("Adding " + s + " to ignore list.");
        ignoreList.add(s);
    }

    public void removePlayer(String s) {
        plugin.logInfo("Removing " + s + " from ignore list.");
        ignoreList.remove(s);
    }

    public static boolean isIgnored(String s) {
        return ignoreList.contains(s);
    }
}