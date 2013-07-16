/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cnaude.scavenger;

import java.io.*;
import java.util.ArrayList;

/**
 *
 * @author naudec
 */
@SuppressWarnings("serial")
public final class ScavengerIgnoreList implements Serializable {

    private static ArrayList<String> ignoreList = new ArrayList<String>();
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
            ObjectInputStream obj_in = new ObjectInputStream(f_in);
            ignoreList = (ArrayList<String>) obj_in.readObject();
            obj_in.close();
            plugin.logInfo("Loaded ignore list. (Count = " + ignoreList.size() + ")");
        } catch (Exception e) {
            plugin.logError(e.getMessage());
        }
    }

    public void save() {
        try {
            File file = new File(IGNORE_FILE);
            FileOutputStream f_out = new FileOutputStream(file);
            ObjectOutputStream obj_out = new ObjectOutputStream(f_out);
            obj_out.writeObject(ignoreList);
            obj_out.close();
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
        if (ignoreList.contains(s)) {
            return true;
        } else {
            return false;
        }
    }
}