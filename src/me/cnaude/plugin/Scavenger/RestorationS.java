/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.cnaude.plugin.Scavenger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author naudec
 */
public class RestorationS implements Serializable {
    public boolean enabled;
    public List<String> worldGroups = new ArrayList<String>();
    public List<Map<String,Object>> inventory = new ArrayList<Map<String,Object>>();
    public List<Map<String,Object>> armour = new ArrayList<Map<String,Object>>();
    public int level;
    public float exp;
}
