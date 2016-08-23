/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2;

import java.util.HashMap;

/**
 *
 * @author kenjo
 */
public abstract class Geant4Factory {
    protected Geant4Basic motherVolume;

    protected Geant4Factory(){
        motherVolume = new G4Box("fc",0,0,0);
    }
    
    protected final HashMap<String, String> properties = new HashMap<>();

    public Geant4Basic getMother() {
        return motherVolume;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        for (Geant4Basic volume : motherVolume.getChildren()) {
            str.append(volume.gemcStringRecursive());
        }

        return str.toString();
    }

    public String getProperty(String name) {
        return properties.containsKey(name) ? properties.get(name) : "none";
    }
}
