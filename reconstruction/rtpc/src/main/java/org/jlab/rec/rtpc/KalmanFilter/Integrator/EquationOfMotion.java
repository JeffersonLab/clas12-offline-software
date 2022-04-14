package org.jlab.rec.rtpc.KalmanFilter.Integrator;

import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.PhysicalConstants.c_light;
import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.SystemOfUnits.eplus;

public class EquationOfMotion {

    /**
     * Equation of motion for a charged particle in a magnetic field going backward in time.
     *
     * @param y     vector compose by x,y,z,px,py,pz
     * @param dydt  derivative
     * @param Field Magnetic field
     */
    public static void BackwardRightHandSide(double[] y, double[] dydt, double[] Field) {
        double pcharge = 1;

        double fElectroMagCof = eplus * pcharge * c_light;

        double pSquared = y[3] * y[3] + y[4] * y[4] + y[5] * y[5];

        double pModuleInverse = 1.0 / Math.sqrt(pSquared);

        double cof1 = fElectroMagCof * pModuleInverse;

        dydt[0] = - y[3] * pModuleInverse;
        dydt[1] = - y[4] * pModuleInverse;
        dydt[2] = - y[5] * pModuleInverse;

        dydt[3] = -cof1 * (y[4] * Field[2] - y[5] * Field[1]);

        dydt[4] = -cof1 * (y[5] * Field[0] - y[3] * Field[2]);

        dydt[5] = -cof1 * (y[3] * Field[1] - y[4] * Field[0]);
    }

    /**
     * Equation of motion for a charged particle in a magnetic field going forward in time.
     *
     * @param y     vector compose by x,y,z,px,py,pz
     * @param dydt  derivative
     * @param Field Magnetic field
     */
    public static void ForwardRightHandSide(double[] y, double[] dydt, double[] Field) {
        double pcharge = 1;

        double fElectroMagCof = eplus * pcharge * c_light;

        double pSquared = y[3] * y[3] + y[4] * y[4] + y[5] * y[5];

        double pModuleInverse = 1.0 / Math.sqrt(pSquared);

        double cof1 = fElectroMagCof * pModuleInverse;

        dydt[0] = y[3] * pModuleInverse;
        dydt[1] = y[4] * pModuleInverse;
        dydt[2] = y[5] * pModuleInverse;

        dydt[3] = cof1 * (y[4] * Field[2] - y[5] * Field[1]);

        dydt[4] = cof1 * (y[5] * Field[0] - y[3] * Field[2]);

        dydt[5] = cof1 * (y[3] * Field[1] - y[4] * Field[0]);

    }

}
