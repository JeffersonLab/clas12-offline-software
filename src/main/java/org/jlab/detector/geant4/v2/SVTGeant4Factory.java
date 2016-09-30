/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2;

import org.jlab.detector.volume.G4Box;
import org.jlab.detector.volume.G4World;

/**
 *
 * @author gavalian, kenjo
 */
public final class SVTGeant4Factory extends Geant4Factory {

    private final double radius = 33;
    
    public SVTGeant4Factory() {
        motherVolume = new G4World("root");
        
        for (int iregion = 0; iregion < 10; iregion++) {
            G4Box svtreg = new G4Box(String.format("svtreg%02d", iregion+1),10,1,50);
            svtreg.setMother(motherVolume);
            svtreg.translate(0, radius, 0);
            svtreg.rotate("zyx", Math.toRadians(iregion*36.0),0,0);
        }
    }
}
