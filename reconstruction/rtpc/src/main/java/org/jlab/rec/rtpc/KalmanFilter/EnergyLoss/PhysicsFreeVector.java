package org.jlab.rec.rtpc.KalmanFilter.EnergyLoss;

import java.util.Collections;
import java.util.Vector;

public class PhysicsFreeVector extends PhysicsVector {

    public PhysicsFreeVector(int length, boolean spline) {
        super(spline);
        numberOfNodes = length;

        if (0 < length) {
            binVector = new Vector<>(Collections.nCopies(numberOfNodes,0.0));
            dataVector = new Vector<>(Collections.nCopies(numberOfNodes,0.0));
        }
        Initialise();
    }

    void PutValues(int index, double e, double value) {
        if (index >= numberOfNodes) {
            return;
        }
        binVector.set(index, e);
        dataVector.set(index, value);
        if (index == 0) {
            edgeMin = e;
        } else if (numberOfNodes == index + 1) {
            edgeMax = e;
        }
    }


}
