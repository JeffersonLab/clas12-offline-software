/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2.DC;

import org.jlab.geom.component.DriftChamberWire;
import org.jlab.geom.prim.Shape3D;

/**
 *
 * @author kenjo
 */
public interface DCstructure {

    interface DCLayer {
        Shape3D getBoundary();
        DriftChamberWire getComponent(int icomp);
    }

    interface DCSuperlayer {

        DCLayer getLayer(int ilayer);
    }

    interface DCSector {

        DCSuperlayer getSuperlayer(int isuperlayer);
    }

    interface DCDetector {

        DCSector getSector(int isector);
    }

}
