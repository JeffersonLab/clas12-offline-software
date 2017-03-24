/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.volume;

import org.jlab.geometry.prim.Trd;
import org.jlab.detector.units.SystemOfUnits.Length;

/**
 *
 * @author kenjo
 */
public class G4Trd extends Geant4Basic{
    
    public G4Trd(String name, double pdx1, double pdx2, double pdy1, double pdy2, double pdz) {
        super(new Trd(pdx1, pdx2, pdy1, pdy2, pdz));
        setName(name);
        setType("Trd");
        setDimensions(Length.value(pdx1), Length.value(pdx2), Length.value(pdy1), Length.value(pdy2), Length.value(pdz));
    }
}
