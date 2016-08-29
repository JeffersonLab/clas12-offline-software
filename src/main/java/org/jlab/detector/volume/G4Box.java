/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.volume;

import org.jlab.detector.units.SystemOfUnits.Length;
import org.jlab.geometry.prim.Box;

/**
 *
 * @author kenjo
 */
public class G4Box extends Geant4Basic {
    public G4Box(String name, double sizex, double sizey, double sizez) {
        super(new Box(sizex, sizey, sizez));
        setName(name);
        setType("Box");
        setDimensions(Length.value(sizex), Length.value(sizey), Length.value(sizez));
    }
    
    public double getXHalfLength() {
        return volumeDimensions.get(0).value;
    }

    public double getYHalfLength() {
        return volumeDimensions.get(1).value;
    }

    public double getZHalfLength() {
        return volumeDimensions.get(2).value;
    }
}
