package org.jlab.rec.rtpc.KalmanFilter.EnergyLoss;

import java.util.Collections;
import java.util.Vector;

import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.PhysicalConstants.alpha_rcl2;
import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.PhysicalConstants.fine_structure_const;
import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.SystemOfUnits.g;
import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.SystemOfUnits.mole;

public class Element {

    // Set up the static Table of Elements
    private static final Vector<Element> theElementTable = new Vector<>();
    //
    // Basic data members (which define an Element)
    //
    private String fName;                 // name
    private String fSymbol;               // symbol
    private double fZeff;                 // Effective atomic number
    private double fNeff;                 // Effective number of nucleons
    private double fAeff;                 // Effective mass of a mole
    private int fZ;
    private int fNbOfAtomicShells;        // number  of atomic shells
    private double[] fAtomicShells;        // Pointer to atomic shell binding energies
    private int[] fNbOfShellElectrons;     // Pointer to the number of subshell electrons
    // Isotope vector contains constituent isotopes of the element
    private int fNumberOfIsotopes;     // Number of isotopes added to the element
    private Vector<Isotope> theIsotopeVector;
    private double[] fRelativeAbundanceVector;     // Fraction nb of atomes per volume
    private int fIndexInTable;
    private boolean fNaturalAbundance;
    //
    // Derived data members (computed from the basic data members)
    //
    private double fCoulomb;             // Coulomb correction factor
    private double fRadTsai;             // Tsai formula for the radiation length
    private IonisParamElm fIonisation;  // Pointer to ionisation parameters

    public Element(String name, String symbol, double zeff, double aeff) {

        fName = name;
        fSymbol = symbol;

        int iz = lrint(zeff);

        InitializePointers();

        fZeff = zeff;
        fAeff = aeff;
        fNeff = fAeff / (g / mole);

        if (fNeff < 1.0) fNeff = 1.0;

        fNbOfAtomicShells = AtomicShells.GetNumberOfShells(iz);
        fAtomicShells = new double[fNbOfAtomicShells];
        fNbOfShellElectrons = new int[fNbOfAtomicShells];

        AddNaturalIsotopes();

        for (int i = 0; i < fNbOfAtomicShells; ++i) {
            fAtomicShells[i] = AtomicShells.GetBindingEnergy(iz, i);
            fNbOfShellElectrons[i] = AtomicShells.GetNumberOfElectrons(iz, i);
        }
        ComputeDerivedQuantities();
    }

    public Element(String name, String symbol, int nIsotopes) {
        fName = name;
        fSymbol = symbol;

        InitializePointers();

        theIsotopeVector = new Vector<>(Collections.nCopies(nIsotopes, null));
        fRelativeAbundanceVector = new double[nIsotopes];
    }

    public boolean GetNaturalAbundanceFlag() {
        return fNaturalAbundance;
    }

    public void SetNaturalAbundanceFlag(boolean val) {
        fNaturalAbundance = val;
    }

    public void SetName(String name) {
        fName = name;
    }

    //the index of this element in the Table:
    //
    public int GetIndex() {
        return fIndexInTable;
    }

    //Coulomb correction factor:
    //
    public double GetfCoulomb() {
        return fCoulomb;
    }

    //Tsai formula for the radiation length:
    //
    public double GetfRadTsai() {
        return fRadTsai;
    }

    //pointer to ionisation parameters:
    //
    public IonisParamElm GetIonisation() {
        return fIonisation;
    }

    public static Vector<Element> GetElementTable() {
        return theElementTable;
    }

    //the number of atomic shells in this element:
    //
    public int GetNbOfAtomicShells() {
        return fNbOfAtomicShells;
    }

    //number of isotopes constituing this element:
    //
    public int GetNumberOfIsotopes() {
        return fNumberOfIsotopes;
    }

    //vector of pointers to isotopes constituing this element:
    //
    public Vector<Isotope> GetIsotopeVector() {
        return theIsotopeVector;
    }

    //vector of relative abundance of each isotope:
    //
    public double[] GetRelativeAbundanceVector() {
        return fRelativeAbundanceVector;
    }

    public Isotope GetIsotope(int iso) {
        return theIsotopeVector.get(iso);
    }

    public String GetName() {
        return fName;
    }

    public String GetSymbol() {
        return fSymbol;
    }

    public double GetZ() {
        return fZeff;
    }

    public int GetZasInt() {
        return fZ;
    }

    // Atomic weight in atomic units
    public double GetN() {
        return fNeff;
    }

    public double GetAtomicMassAmu() {
        return fNeff;
    }
    // for each constituent

    // Mass of a mole in Geant4 units for atoms with atomic shell
    public double GetA() {
        return fAeff;
    }

    public void AddIsotope(Isotope isotope, double abundance) {
        if (theIsotopeVector == null) return;

        int iz = isotope.GetZ();

        if (fNumberOfIsotopes < (int) theIsotopeVector.size()) {
            if (fNumberOfIsotopes == 0) { fZeff = (double)(iz); }
            else if ((double)(iz) != fZeff) return;

            fRelativeAbundanceVector[fNumberOfIsotopes] = abundance;
            theIsotopeVector.set(fNumberOfIsotopes, isotope);
            ++fNumberOfIsotopes;

        } else { return; }

        if (fNumberOfIsotopes == theIsotopeVector.size()) {
            double wtSum = 0.0;
            fAeff = 0.0;
            for (int i = 0; i < fNumberOfIsotopes; ++i) {
                fAeff += fRelativeAbundanceVector[i] * (theIsotopeVector).get(i).GetA();
                wtSum += fRelativeAbundanceVector[i];
            }
            if (wtSum > 0.0) { fAeff /= wtSum; }
            fNeff = fAeff / (g / mole);

            if (wtSum != 1.0) {
                for (int i = 0; i < fNumberOfIsotopes; ++i) {
                    fRelativeAbundanceVector[i] /= wtSum;
                }
            }

            fNbOfAtomicShells = AtomicShells.GetNumberOfShells(iz);
            fAtomicShells = new double[fNbOfAtomicShells];
            fNbOfShellElectrons = new int[fNbOfAtomicShells];

            for (int j = 0; j < fNbOfAtomicShells; ++j) {
                fAtomicShells[j] = AtomicShells.GetBindingEnergy(iz, j);
                fNbOfShellElectrons[j] = AtomicShells.GetNumberOfElectrons(iz, j);
            }
            ComputeDerivedQuantities();
        }
    }

    private void InitializePointers() {
        theIsotopeVector = null;
        fRelativeAbundanceVector = null;
        fAtomicShells = null;
        fNbOfShellElectrons = null;
        fIonisation = null;
        fNumberOfIsotopes = 0;
        fNaturalAbundance = false;

        fZeff = 0;
        fNeff = 0;
        fAeff = 0;
        fNbOfAtomicShells = 0;
        fIndexInTable = 0;
        fCoulomb = 0.0;
        fRadTsai = 0.0;
        fZ = 0;
    }

    private void ComputeDerivedQuantities() {

        theElementTable.add(this);
        fIndexInTable = theElementTable.size() - 1;

        ComputeCoulombFactor();
        ComputeLradTsaiFactor();

        fIonisation = new IonisParamElm(fZeff);
        fZ = lrint(fZeff);
    }

    private void ComputeCoulombFactor() {

        double k1 = 0.0083, k2 = 0.20206, k3 = 0.0020, k4 = 0.0369;

        double az2 = (fine_structure_const * fZeff) * (fine_structure_const * fZeff);
        double az4 = az2 * az2;

        fCoulomb = (k1 * az4 + k2 + 1. / (1. + az2)) * az2 - (k3 * az4 + k4) * az4;
    }

    private void ComputeLradTsaiFactor() {

        double Lrad_light[] = {5.31, 4.79, 4.74, 4.71};
        double Lprad_light[] = {6.144, 5.621, 5.805, 5.924};

        double logZ3 = Math.log(fZeff) / 3.;

        double Lrad, Lprad;
        int iz = lrint(fZeff) - 1;
        double log184 = Math.log(184.15);
        double log1194 = Math.log(1194.);
        if (iz <= 3) {
            Lrad = Lrad_light[iz];
            Lprad = Lprad_light[iz];
        } else {
            Lrad = log184 - logZ3;
            Lprad = log1194 - 2 * logZ3;
        }

        fRadTsai = 4 * alpha_rcl2 * fZeff * (fZeff * (Lrad - fCoulomb) + Lprad);
    }

    private void AddNaturalIsotopes() {
        int Z = lrint(fZeff);
        NistManager nist = NistManager.Instance ();
        int n = nist.GetNumberOfNistIsotopes(Z);
        int N0 = nist.GetNistFirstIsotopeN(Z);

        if (fSymbol.isEmpty()) {
            Vector<String> elmnames =
                    NistManager.Instance().GetNistElementNames();
            if (Z < elmnames.size()) {
                fSymbol = elmnames.get(Z);
            } else {
                fSymbol = fName;
            }
        }

        fNumberOfIsotopes = 0;
        for (int i = 0; i < n; ++i) {
            if (nist.GetIsotopeAbundance(Z, N0 + i) > 0.0) {
                ++fNumberOfIsotopes;
            }
        }
        theIsotopeVector = new Vector<>(Collections.nCopies(fNumberOfIsotopes, null));
        fRelativeAbundanceVector = new double[fNumberOfIsotopes];
        int idx = 0;
        double xsum = 0.0;
        for (int i = 0; i < n; ++i) {
            int N = N0 + i;
            double x = nist.GetIsotopeAbundance(Z, N);
            if (x > 0.0) {
                theIsotopeVector.set(idx, new Isotope(fSymbol, Z, N, 0.0, 0));
                fRelativeAbundanceVector[idx] = x;
                xsum += x;
                ++idx;
            }
        }
        if (xsum != 0.0 && xsum != 1.0) {
            for (int i = 0; i < idx; ++i) {
                fRelativeAbundanceVector[i] /= xsum;
            }
        }
        fNaturalAbundance = true;
    }

    private int lrint(double ad) {
        return (ad > 0) ? (int) (ad + .5) : (int) (ad - .5);
    }
}
