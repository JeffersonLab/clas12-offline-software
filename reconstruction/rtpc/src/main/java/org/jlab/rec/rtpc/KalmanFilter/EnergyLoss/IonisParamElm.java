package org.jlab.rec.rtpc.KalmanFilter.EnergyLoss;

import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.PhysicalConstants.*;
import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.SystemOfUnits.MeV;
import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.SystemOfUnits.eV;

public class IonisParamElm {

    //
    //  data members
    //
    double fZ;                 // effective Z
    double fZ3;                // std::pow (Z,1/3)

    //  ------ ionisation loss ---------------------------------
    double fTau0;                 // 0.1*std::pow(Z,1/3)*MeV/proton_mass_c2
    double fTaul;                 // 2*MeV/proton mass
    double fBetheBlochLow;         // Bethe-Bloch at fTaul*particle mass
    double fClow;      // parameters for the low energy ion.loss
    double fMeanExcitationEnergy;  //
    double[] fShellCorrectionVector; // shell correction coefficients

    // parameters for ion corrections computations
    double fVFermi;
    double fLFactor;

    public IonisParamElm(double AtomNumber) {
        int Z = lrint(AtomNumber);

        fZ = Z;
        fZ3 = Math.pow(Z, 1.0 / 3.0);


        fMeanExcitationEnergy = NistManager.Instance().GetMeanIonisationEnergy(Z);

        int iz = Z - 1;
        if (91 < iz) {
            iz = 91;
        }

        double[] vFermi = {
                1.0309, 0.15976, 0.59782, 1.0781, 1.0486, 1.0, 1.058, 0.93942, 0.74562, 0.3424,
                0.45259, 0.71074, 0.90519, 0.97411, 0.97184, 0.89852, 0.70827, 0.39816, 0.36552, 0.62712,
                0.81707, 0.9943, 1.1423, 1.2381, 1.1222, 0.92705, 1.0047, 1.2, 1.0661, 0.97411,
                0.84912, 0.95, 1.0903, 1.0429, 0.49715, 0.37755, 0.35211, 0.57801, 0.77773, 1.0207,
                1.029, 1.2542, 1.122, 1.1241, 1.0882, 1.2709, 1.2542, 0.90094, 0.74093, 0.86054,
                0.93155, 1.0047, 0.55379, 0.43289, 0.32636, 0.5131, 0.695, 0.72591, 0.71202, 0.67413,
                0.71418, 0.71453, 0.5911, 0.70263, 0.68049, 0.68203, 0.68121, 0.68532, 0.68715, 0.61884,
                0.71801, 0.83048, 1.1222, 1.2381, 1.045, 1.0733, 1.0953, 1.2381, 1.2879, 0.78654,
                0.66401, 0.84912, 0.88433, 0.80746, 0.43357, 0.41923, 0.43638, 0.51464, 0.73087, 0.81065,
                1.9578, 1.0257};

        double[] lFactor = {
                1.0, 1.0, 1.1, 1.06, 1.01, 1.03, 1.04, 0.99, 0.95, 0.9,
                0.82, 0.81, 0.83, 0.88, 1.0, 0.95, 0.97, 0.99, 0.98, 0.97,
                0.98, 0.97, 0.96, 0.93, 0.91, 0.9, 0.88, 0.9, 0.9, 0.9,
                0.9, 0.85, 0.9, 0.9, 0.91, 0.92, 0.9, 0.9, 0.9, 0.9,
                0.9, 0.88, 0.9, 0.88, 0.88, 0.9, 0.9, 0.88, 0.9, 0.9,
                0.9, 0.9, 0.96, 1.2, 0.9, 0.88, 0.88, 0.85, 0.9, 0.9,
                0.92, 0.95, 0.99, 1.03, 1.05, 1.07, 1.08, 1.1, 1.08, 1.08,
                1.08, 1.08, 1.09, 1.09, 1.1, 1.11, 1.12, 1.13, 1.14, 1.15,
                1.17, 1.2, 1.18, 1.17, 1.17, 1.16, 1.16, 1.16, 1.16, 1.16,
                1.16, 1.16};

        fVFermi = vFermi[iz];
        fLFactor = lFactor[iz];

        // obsolete parameters for ionisation
        fTau0 = 0.1 * fZ3 * MeV / proton_mass_c2;
        fTaul = 2. * MeV / proton_mass_c2;

        // compute the Bethe-Bloch formula for energy = fTaul*particle mass
        double rate = fMeanExcitationEnergy / electron_mass_c2;
        double w = fTaul * (fTaul + 2.);
        fBetheBlochLow = (fTaul + 1.) * (fTaul + 1.) * Math.log(2. * w / rate) / w - 1.;
        fBetheBlochLow = 2. * fZ * twopi_mc2_rcl2 * fBetheBlochLow;

        fClow = Math.sqrt(fTaul) * fBetheBlochLow;
        double Taum = 0.035 * fZ3 * MeV / proton_mass_c2;

        // Shell correction parameterization
        fShellCorrectionVector = new double[3]; //[3]
        rate = 0.001 * fMeanExcitationEnergy / eV;
        double rate2 = rate * rate;

        fShellCorrectionVector[0] = (0.422377 + 3.858019 * rate) * rate2;
        fShellCorrectionVector[1] = (0.0304043 - 0.1667989 * rate) * rate2;
        fShellCorrectionVector[2] = (-0.00038106 + 0.00157955 * rate) * rate2;
    }

    private int lrint(double ad) {
        return (ad > 0) ? (int) (ad + .5) : (int) (ad - .5);
    }

    public double GetZ() {
        return fZ;
    }

    public double GetTaul() {
        return fTaul;
    }

    public double GetMeanExcitationEnergy() {
        return fMeanExcitationEnergy;
    }

    public double GetFermiVelocity() {
        return fVFermi;
    }

    ;

    public double GetLFactor() {
        return fLFactor;
    }

    ;

    public double[] GetShellCorrectionVector() {
        return fShellCorrectionVector;
    }
}
