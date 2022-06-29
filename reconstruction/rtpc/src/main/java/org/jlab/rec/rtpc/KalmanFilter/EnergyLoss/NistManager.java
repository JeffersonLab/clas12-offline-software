package org.jlab.rec.rtpc.KalmanFilter.EnergyLoss;

import java.util.Vector;

public class NistManager {

    private static NistManager instance;
    private NistElementBuilder elmBuilder;
    private NistMaterialBuilder matBuilder;
    private int verbose;

    private NistManager() {
        verbose = 0;

        elmBuilder = new NistElementBuilder();
        matBuilder = new NistMaterialBuilder(elmBuilder, verbose);

    }

    public static NistManager Instance() {
        if (instance == null) {

            instance = new NistManager();
        }
        return instance;
    }

    public Element FindOrBuildElement(int Z, boolean isotopes) {
        return elmBuilder.FindOrBuildElement(Z);
    }


    public int GetZ(String symb) {
        return elmBuilder.GetZ(symb);
    }


    public double GetAtomicMass(int Z, int N) {
        return elmBuilder.GetAtomicMass(Z, N);
    }


    public double GetIsotopeAbundance(int Z, int N) {
        return elmBuilder.GetIsotopeAbundance(Z, N);
    }


    public int GetNistFirstIsotopeN(int Z) {
        return elmBuilder.GetNistFirstIsotopeN(Z);
    }


    public int GetNumberOfNistIsotopes(int Z) {
        return elmBuilder.GetNumberOfNistIsotopes(Z);
    }


    public Vector<String> GetNistElementNames() {
        return elmBuilder.GetElementNames();
    }


    public double GetMeanIonisationEnergy(int Z) {
        return matBuilder.GetMeanIonisationEnergy(Z);
    }


    public double GetNominalDensity(int Z) {
        return matBuilder.GetNominalDensity(Z);
    }


    public int GetVerbose() {
        return verbose;
    }


    public Material FindOrBuildMaterial(String name, boolean temp, boolean warning) {
        return matBuilder.FindOrBuildMaterial(name, warning);
    }


}
