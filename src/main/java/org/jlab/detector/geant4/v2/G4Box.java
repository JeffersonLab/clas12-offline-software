/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2;

import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Cube;
import org.jlab.detector.units.SystemOfUnits.Length;

/**
 *
 * @author kenjo
 */
public class G4Box extends Geant4Basic {

    private final double sizex, sizey, sizez;

    public G4Box(String name, double sizex, double sizey, double sizez) {
        super(name, "G4Box", Length.unit(sizex), Length.unit(sizey), Length.unit(sizez));
        this.sizex = sizex;
        this.sizey = sizey;
        this.sizez = sizez;
        
        volumeSolid = new Cube(sizex * 2., sizey * 2., sizez * 2.);
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
