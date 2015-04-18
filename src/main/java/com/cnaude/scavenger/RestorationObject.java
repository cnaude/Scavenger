package com.cnaude.scavenger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author naudec
 */
public class RestorationObject implements Serializable {
    public boolean enabled;
    public List<ScavengerItem> inventory = new ArrayList<>();
    public List<ScavengerItem> armour = new ArrayList<>();
    public int level;
    public float exp;
    public String deathCause;
    public String playerName;
}