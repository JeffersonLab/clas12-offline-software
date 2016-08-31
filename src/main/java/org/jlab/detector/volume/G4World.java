/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.volume;

import org.jlab.geometry.prim.Box;

/**
 *
 * @author kenjo
 */
public final class G4World extends Geant4Basic{
    public G4World(String name){
        super(new Box(1,1,1));
        setName(name);
        setType("World");
    }
}
