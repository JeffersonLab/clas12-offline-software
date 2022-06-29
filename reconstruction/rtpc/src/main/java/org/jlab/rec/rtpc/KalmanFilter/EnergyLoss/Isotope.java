package org.jlab.rec.rtpc.KalmanFilter.EnergyLoss;

import java.util.Vector;

import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.PhysicalConstants.amu_c2;
import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.SystemOfUnits.g;
import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.SystemOfUnits.mole;

public class Isotope {

    String fName;              // name of the Isotope
    int fZ;                       // atomic number
    int fN;                       // number of nucleons
    double fA;                    // atomic mass of a mole

    static Vector<Isotope> theIsotopeTable = new Vector<>();

    public Isotope(String name, int z, int n, double a, int mlevel){

        fName = name;
        fZ = z;
        fN = n;
        fA = a;

        if (a <= 0.0) {
            fA = (NistManager.Instance().GetAtomicMass(z, n)) * g / (mole * amu_c2);
        }
            theIsotopeTable.add(this);
    }

    String GetName() { return fName; }
    int GetZ() { return fZ; }
    int GetN() { return fN; }
    double GetA() { return fA; }

    Vector<Isotope> GetIsotopeTable() {
        return theIsotopeTable;
    }

}
