package org.jlab.rec.rtpc.KalmanFilter.EnergyLoss;

import java.util.HashMap;
import java.util.Objects;
import java.util.Vector;

import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.PhysicalConstants.*;
import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.State.*;
import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.SystemOfUnits.*;

public class Material {

    public Material(String name, double z, double a, double density, State state, double temp,
                       double pressure) {

        fName =name;
        InitializePointers();

        if (density < universe_mean_density) density = universe_mean_density;

        fDensity = density;
        fState = state;
        fTemp = temp;
        fPressure = pressure;

        fNbComponents = fNumberOfElements = 1;

        NistManager nist = NistManager.Instance();
        int iz = lrint(z);
        Element elm = nist.FindOrBuildElement(iz, true);
        if (elm == null) { elm = new Element("ELM_" + name, name, z, a); }
        theElementVector.add(elm);

        fMassFractionVector = new double[1];
        fMassFractionVector[0] = 1.;
        fMassOfMolecule = a / Avogadro;

        if (fState == kStateUndefined) {
            if (fDensity > kGasThreshold) { fState = kStateSolid; }
            else { fState = kStateGas; }
        }

        ComputeDerivedQuantities();
    }


    public Material(String name, double density, int nComponents, State state, double temp, double pressure) {
        fName =name;
        InitializePointers();

        if (density < universe_mean_density) density = universe_mean_density;

        fDensity = density;
        fState = state;
        fTemp = temp;
        fPressure = pressure;

        fNbComponents = nComponents;
        theElementVector = new Vector<>(); // Collections.nCopies(fNbComponents,null)

        fAtomsVector = new int[fNbComponents];
        fMassFractionVector = new double[fNbComponents];
        for (int i = 0; i < fNbComponents; ++i) {
            fAtomsVector[i] = 0;
            fMassFractionVector[i] = 0.0;
        }
        fMassFraction = true;

        if (fState == kStateUndefined) {
            if (fDensity > kGasThreshold) { fState = kStateSolid; }
            else { fState = kStateGas; }
        }
    }

    public Material(String name, double density, Material bmat, State state, double temp,
                       double pressure)
        {
            fName = name;
        InitializePointers();

        if (density < universe_mean_density) density = universe_mean_density;

        fDensity = density;
        fState = state;
        fTemp = temp;
        fPressure = pressure;

        fBaseMaterial = bmat;
        fChemicalFormula = fBaseMaterial.GetChemicalFormula();
        fMassOfMolecule = fBaseMaterial.GetMassOfMolecule();

        fNumberOfElements = fBaseMaterial.GetNumberOfElements();
        fNbComponents = fNumberOfElements;

    }

    public void AddElementByNumberOfAtoms(Element elm, int nAtoms) {
        // perform checks consistency
        if (0 == fIdxComponent) { fMassFraction = false; }
        // filling
        if (fIdxComponent < fNbComponents) {
            boolean isAdded = false;
            for (int i = 0; i < fNumberOfElements; ++i) {
                if (elm == (theElementVector).get(i)) {
                    fAtomsVector[i] += nAtoms;
                    break;
                }
            }
            if (!isAdded) {
                theElementVector.add(elm);
                fAtomsVector[fNumberOfElements] = nAtoms;
                ++fNumberOfElements;
            }
        }
        ++fIdxComponent;
        // is filled
        if (fIdxComponent == fNbComponents) {
            // compute proportion by mass
            double Amol = 0.;
            for (int i = 0; i < fNumberOfElements; ++i) {
                double w = fAtomsVector[i] * (theElementVector).get(i).GetA();
                Amol += w;
                fMassFractionVector[i] = w;
            }
            for (int i = 0; i < fNumberOfElements; ++i) {
                fMassFractionVector[i] /= Amol;
            }

            fMassOfMolecule = Amol / Avogadro;
            ComputeDerivedQuantities();
        }
    }
    public void AddElement(Element elm, int nAtoms) { AddElementByNumberOfAtoms(elm, nAtoms); }

    public void AddElementByMassFraction(Element elm, double fraction) {

        Element element = elm;

        // filling
        if (fIdxComponent < fNbComponents) {
            boolean isAdded = false;
            for (int i = 0; i < fNumberOfElements; ++i) {
                if (element == (theElementVector).get(i)) {

                    fMassFractionVector[i] += fraction;
                    isAdded = true;
                    break;
                }
            }
            if (!isAdded) {
                theElementVector.add(element);
                fMassFractionVector[fNumberOfElements] = fraction;
                ++fNumberOfElements;
            }
        }
        ++fIdxComponent;

        // is filled
        if (fIdxComponent == fNbComponents) { FillVectors(); }
    }
    public void AddElement(Element elm, double frac) { AddElementByMassFraction(elm, frac); }

    public void AddMaterial(Material material, double fraction) {

        if (fIdxComponent < fNbComponents) {
            fMatComponents.put(material, fraction);
        }
        ++fIdxComponent;

        if (fIdxComponent == fNbComponents) { FillVectors(); }
    }


    public void SetChemicalFormula(String chF) {

        fChemicalFormula = chF;

    }


    public static Vector<Material> GetMaterialTable() {
        return theMaterialTable;
    }


    public double GetZ() {
        return (theElementVector).get(0).GetZ();
    }

    public int GetIndex() { return fIndexInTable; }


    public String GetName() { return fName; }

    public String GetChemicalFormula() { return fChemicalFormula; }

    public double GetDensity() { return fDensity; }

    public State GetState() { return fState; }

    public double GetTemperature() { return fTemp; }

    public double GetPressure() { return fPressure; }

    public int GetNumberOfElements() { return fNumberOfElements; }

    public Vector<Element> GetElementVector() { return theElementVector; }

    public double[] GetFractionVector() { return fMassFractionVector; }

    public Element GetElement(int iel) { return (theElementVector).get(iel); }

    public double[] GetVecNbOfAtomsPerVolume() { return fVecNbOfAtomsPerVolume; }

    public double GetTotNbOfAtomsPerVolume() { return fTotNbOfAtomsPerVolume; }

    public double GetTotNbOfElectPerVolume() { return fTotNbOfElectPerVolume; }

    public double[] GetAtomicNumDensityVector() { return fVecNbOfAtomsPerVolume; }

    public double GetElectronDensity() { return fTotNbOfElectPerVolume; }

    public IonisParamMat GetIonisation() { return fIonisation; }

    public Material GetBaseMaterial() { return fBaseMaterial; }

    public double GetMassOfMolecule() { return fMassOfMolecule; }

    private void InitializePointers() {
        fBaseMaterial = null;
        theElementVector = null;
        fAtomsVector = null;
        fMassFractionVector = null;
        fVecNbOfAtomsPerVolume = null;

        fIonisation = null;

        fDensity = fFreeElecDensity = fTemp = fPressure = 0.0;
        fTotNbOfAtomsPerVolume = 0.0;
        fTotNbOfElectPerVolume = 0.0;
        fMassOfMolecule = 0.0;

        fState = kStateUndefined;

        fNumberOfElements = fNbComponents = fIdxComponent = 0;

        fMassFraction = true;

        fChemicalFormula = "";

        fIndexInTable = theMaterialTable.size();
        for (int i = 0; i < fIndexInTable; ++i) {
            if (Objects.equals(theMaterialTable.get(i).GetName(), fName)) {
                break;
            }
        }
        theMaterialTable.add(this);
    }


    private void ComputeDerivedQuantities() {
        double Zi, Ai;
        fTotNbOfAtomsPerVolume = 0.;
        fVecNbOfAtomsPerVolume = new double[fNumberOfElements];
        fTotNbOfElectPerVolume = 0.;
        fFreeElecDensity = 0.;
        double elecTh = 15. * eV; // threshold for conductivity e-
        for (int i = 0; i < fNumberOfElements; ++i) {
            Zi = (theElementVector).get(i).GetZ();
            Ai = (theElementVector).get(i).GetA();
            fVecNbOfAtomsPerVolume[i] = Avogadro * fDensity * fMassFractionVector[i] / Ai;
            fTotNbOfAtomsPerVolume += fVecNbOfAtomsPerVolume[i];
            fTotNbOfElectPerVolume += fVecNbOfAtomsPerVolume[i] * Zi;
            if (fState != kStateGas) {
                fFreeElecDensity += fVecNbOfAtomsPerVolume[i] *
                        AtomicShells.GetNumberOfFreeElectrons((int) Zi, elecTh);
            }
        }

        ComputeRadiationLength();

        if (fIonisation == null) { fIonisation = new IonisParamMat(this); }
    }

    private void ComputeRadiationLength() {
        double radinv = 0.0;
        for (int i = 0; i < fNumberOfElements; ++i) {
            radinv += fVecNbOfAtomsPerVolume[i] * ((theElementVector).get(i).GetfRadTsai());
        }
    }

    private void FillVectors() {

        if (!fMatComponents.isEmpty()) {
            int nel = fNumberOfElements;
            // check list of materials
            for (HashMap.Entry<Material,Double> x: fMatComponents.entrySet()) {
            Material mat = x.getKey();
                int nn = mat.GetNumberOfElements();
                for (int j = 0; j < nn; ++j) {
                    boolean yes = true;
                    Element elm = mat.GetElement(j);
                    for (int k = 0; k < fNumberOfElements; ++k) {
                        if (elm == (theElementVector).get(k)) {
                            yes = false;
                            break;
                        }
                    }
                    if (yes) { ++nel; }
                }
            }

            if (nel > fNbComponents) {
                fAtomsVector = new int[nel];
                double[] v = new double[nel];
                for (int i = 0; i < fNumberOfElements; ++i) {
                    fAtomsVector[i] = 0;
                    v[i] = fMassFractionVector[i];
                }
                fMassFractionVector = v;
                for (int i = fNumberOfElements; i < nel; ++i) {
                    fAtomsVector[i] = 0;
                    fMassFractionVector[i] = 0.0;
                }
            }
            // filling
            for (HashMap.Entry<Material,Double> x: fMatComponents.entrySet()) {
            Material mat = x.getKey();
                double frac = x.getValue();
                int nn = mat.GetNumberOfElements();
             double[] elmFrac = mat.GetFractionVector();
                for (int j = 0; j < nn; ++j) {
                    boolean yes = true;
                Element elm = mat.GetElement(j);
                    for (int k = 0; k < fNumberOfElements; ++k) {
                        if (elm == (theElementVector).get(k)) {
                            fMassFractionVector[k] += frac * elmFrac[j];
                            yes = false;
                            break;
                        }
                    }
                    if (yes) {
                        theElementVector.add(elm);
                        fMassFractionVector[fNumberOfElements] = frac * elmFrac[j];
                        ++fNumberOfElements;
                    }
                }
            }
        }

        // check sum of weights -- OK?
        double wtSum = (0.0);
        for (int i = 0; i < fNumberOfElements; ++i) {
            wtSum += fMassFractionVector[i];
        }
        if (Math.abs(1. - wtSum) > perThousand) {

        }
        double coeff = (wtSum > 0.0) ? 1. / wtSum : 1.0;
        double Amol = (0.);
        for (int i = 0; i < fNumberOfElements; ++i) {
            fMassFractionVector[i] *= coeff;
            Amol += fMassFractionVector[i] * (theElementVector).get(i).GetA();
        }
        for (int i = 0; i < fNumberOfElements; ++i) {
            fAtomsVector[i] =
                    lrint(fMassFractionVector[i] * Amol / (theElementVector).get(i).GetA());
        }
        ComputeDerivedQuantities();
    }



    public static double NTP_Temperature = 293.15 * kelvin;

    private static final Vector<Material> theMaterialTable = new Vector<>();

    private Material fBaseMaterial;

    private Vector<Element> theElementVector; // vector of constituent G4Elements
    private int[] fAtomsVector;               // composition by atom count
    private double[] fMassFractionVector;     // composition by fractional mass
    private double[] fVecNbOfAtomsPerVolume;  // number of atoms per volume

    private IonisParamMat fIonisation;    // ionisation parameters

    private double fDensity;               // Material density
    private double fFreeElecDensity;       // Free electron density
    private double fTemp;                  // Temperature (defaults: STP)
    private double fPressure;              // Pressure    (defaults: STP)

    private double fTotNbOfAtomsPerVolume; // Total nb of atoms per volume
    private double fTotNbOfElectPerVolume; // Total nb of electrons per volume
    private double fMassOfMolecule;        // Correct for materials built by atoms count

    private State fState;                  // Material state
    private int fIndexInTable;            // Index in the material table
    private int fNumberOfElements;         // Number of G4Elements in the material

    // Class members used only at initialisation
    private int fNbComponents;             // Number of material components
    private int fIdxComponent;             // Index of a new component
    private boolean fMassFraction;            // Flag of the method to add components

    // For composites built via AddMaterial()
    HashMap<Material, Double> fMatComponents = new HashMap<>();

    private String fName;                  // Material name
    private String fChemicalFormula;       // Material chemical formula

    private int lrint(double ad) {
        return (ad > 0) ? (int) (ad + .5) : (int) (ad - .5);
    }
}
