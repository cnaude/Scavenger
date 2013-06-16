/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cnaude.scavenger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author naudec
 */
public class RestorationS1 implements Serializable {
    public boolean enabled;
    public List<String> inventory = new ArrayList<String>();
    public List<String> armour = new ArrayList<String>();
    public int level;
    public float exp;
}