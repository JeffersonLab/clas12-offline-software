/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2;

import eu.mihosoft.vrl.v3d.Plane;
import java.io.IOException;
import org.jlab.detector.volume.G4Stl;
import org.jlab.detector.volume.G4World;

/**
 *
 * @author kenjo
 */
public final class CTOFGeant4Factory extends Geant4Factory {

    private final int nscintillators = 48;

    public CTOFGeant4Factory() throws IOException {
        motherVolume = new G4World("fc");

        ClassLoader cloader = getClass().getClassLoader();

        for (String name : new String[]{"sc", "lgu", "lgd"}) {
            for (int iscint = 1; iscint <= nscintillators; iscint++) {
                G4Stl component = new G4Stl(String.format("%s%02d", name, iscint),
                        cloader.getResourceAsStream(String.format("ctof/cad/%s%02d.stl", name, iscint)));
                component.scale(0.1);
//                component.mirror(Plane.XY_PLANE);
//                component.translate(0,0,2720.0);
                component.setMother(motherVolume);
            }
        }
    }
}
