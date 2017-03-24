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
 * @author kenjo
 */
public final class SVTGeant4Factory extends Geant4Factory {

    private final double radius = 33;

    public SVTGeant4Factory() {
        motherVolume = new G4World("root");

        for (int iregion = 0; iregion < 10; iregion++) {
            G4Box svtreg = SVTbuilder.buildRegion(iregion, 20, 1, 100);
            svtreg.setMother(motherVolume);
            svtreg.translate(0, radius, 0);
            svtreg.rotate("zyx", Math.toRadians(iregion * 36.0), 0, 0);
        }
    }

    static private class SVTbuilder {
        static G4Box buildRegion(int iregion, double width, double height, double length) {
            G4Box regionVolume = new G4Box("svtreg"+iregion, width / 2, height / 2, length / 2);

            for (int irow = 0; irow < 3; irow++) {
                for (int icol = 0; icol < 2; icol++) {
                    int isensor = icol * 3 + irow + 1;
                    G4Box sensorVol = new G4Box(String.format("sensor%02d%d",iregion,isensor), width / 2, height / 6, length / 6);
                    sensorVol.translate(0, height * (-1.0 / 3.0 + icol * 2.0 / 3.0), length * (irow - 1) * (1.0 / 3.0+1.0/100.0));
                    sensorVol.setMother(regionVolume);
                }
            }

            return regionVolume;
        }
    }
}
