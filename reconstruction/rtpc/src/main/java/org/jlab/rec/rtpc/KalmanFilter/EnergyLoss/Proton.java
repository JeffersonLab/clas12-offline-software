package org.jlab.rec.rtpc.KalmanFilter.EnergyLoss;

import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.PhysicalConstants.proton_mass_c2;
import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.SystemOfUnits.eplus;

public class Proton extends Particle {

    public Proton() {
        super("Proton", proton_mass_c2, 1, eplus);
    }

}
