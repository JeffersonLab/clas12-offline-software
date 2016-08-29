/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2;

import eu.mihosoft.vrl.v3d.Cube;
import eu.mihosoft.vrl.v3d.Primitive;
import org.jlab.detector.units.SystemOfUnits.Length;

/**
 *
 * @author kenjo
 */
public class G4Box extends Geant4Basic {
    public G4Box(String name, double sizex, double sizey, double sizez) {
        super(new Cube(sizex * 2., sizey * 2., sizez * 2.));
        setName(name);
        setType("Box");
        setDimensions(Length.unit(sizex), Length.unit(sizey), Length.unit(sizez));
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
