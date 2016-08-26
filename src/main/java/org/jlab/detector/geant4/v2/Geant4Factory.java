/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2;

import eu.mihosoft.vrl.v3d.Line3d;
import java.util.HashMap;
import java.util.List;
import org.jlab.detector.hits.DetHit;

/**
 *
 * @author kenjo
 */
public abstract class Geant4Factory {
    protected Geant4Basic motherVolume;
    protected final HashMap<String, String> properties = new HashMap<>();

    protected Geant4Factory(){
        motherVolume = new G4Box("fc",0,0,0);
    }

    public Geant4Basic getMother() {
        return motherVolume;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        motherVolume.getChildren().stream()
                .forEach(child -> str.append(child.gemcStringRecursive()));

        return str.toString();
    }

    public String getProperty(String name) {
        return properties.containsKey(name) ? properties.get(name) : "none";
    }

    List<Geant4Basic> getComponents(){
        return motherVolume.getComponents();
    }
    
    List<DetHit> getIntersections(Line3d line){
        return motherVolume.getIntersections(line);
    }
}
