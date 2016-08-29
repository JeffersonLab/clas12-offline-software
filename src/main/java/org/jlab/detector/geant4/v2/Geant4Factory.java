/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2;

import eu.mihosoft.vrl.v3d.Straight;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.jlab.detector.hits.DetHit;

/**
 *
 * @author kenjo
 */
public abstract class Geant4Factory {
    protected Geant4Basic motherVolume = new G4Box("fc",0,0,0);
    protected final HashMap<String, String> properties = new HashMap<>();

    @Override
    public String toString() {
        return motherVolume.getChildren().stream()
                .map(child -> child.gemcStringRecursive())
                .collect(Collectors.joining());
    }

    public String getProperty(String name) {
        return properties.containsKey(name) ? properties.get(name) : "none";
    }

    List<Geant4Basic> getComponents(){
        return motherVolume.getChildren().stream()
                .flatMap(child -> child.getComponents().stream())
                .collect(Collectors.toList());
    }
    
    List<DetHit> getIntersections(Straight line){
        return motherVolume.getChildren().stream()
                .flatMap(child -> child.getIntersections(line).stream())
                .collect(Collectors.toList());
    }
}
