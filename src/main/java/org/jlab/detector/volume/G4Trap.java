/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.volume;

import eu.mihosoft.vrl.v3d.Vector3d;
import org.jlab.detector.units.SystemOfUnits.Length;
import org.jlab.detector.units.SystemOfUnits.Angle;
import org.jlab.geometry.prim.Trap;

/**
 *
 * @author kenjo
 */
public class G4Trap extends Geant4Basic {

    public G4Trap(String name, double pDz, double pTheta, double pPhi,
            double pDy1, double pDx1, double pDx2, double pAlp1,
            double pDy2, double pDx3, double pDx4, double pAlp2) {

        super(new Trap(pDz, pTheta, pPhi, pDy1, pDx1, pDx2, pAlp1, pDy2, pDx3, pDx4, pAlp2));
        setName(name);
        setType("G4Trap");
        setDimensions(Length.value(pDz), Angle.value(pTheta), Angle.value(pPhi),
                Length.value(pDy1), Length.value(pDx1), Length.value(pDx2), Angle.value(pAlp1),
                Length.value(pDy2), Length.value(pDx3), Length.value(pDx4), Angle.value(pAlp2));
    }
    
    public Vector3d getVertex(int ivertex){
        int ipol = ivertex/4;
        int[][] ivert = {{0,3,1,2}, {0,1,3,2}};
        return volumeCSG.getPolygons().get(4+ipol).vertices.get(ivert[ipol][ivertex-ipol*4]).pos;
    }
}
