package me.cnaude.plugin.Scavenger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author naudec
 */
public class RestorationS1 implements Serializable {
    public boolean enabled;
    public List<String> inventory = new ArrayList<>();
    public List<String> armour = new ArrayList<>();
    public int level;
    public float exp;
}