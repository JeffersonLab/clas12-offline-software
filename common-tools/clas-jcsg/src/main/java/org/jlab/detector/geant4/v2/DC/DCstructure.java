/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.geant4.v2.DC;

import java.util.List;
import org.jlab.geom.component.DriftChamberWire;

/**
 *
 * @author kenjo
 */
public interface DCstructure {
    interface DCwire {
        void print();
    }

    interface DClayer {
        DCwire getComponent(int iwire);
    }

    interface DCsuperlayer {
        DClayer getLayer(int ilayer);
    }

    interface DCsector {
        DCsuperlayer getSuperlayer(int isuperlayer);
    }
    
    interface DCdetector {
        DCsector getSector(int isector);
    }
    
}
