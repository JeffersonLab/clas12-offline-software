package org.jlab.rec.rtpc.KalmanFilter.EnergyLoss;

import java.util.Vector;

import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.Material.NTP_Temperature;
import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.PhysicalConstants.*;
import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.State.*;
import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.SystemOfUnits.eV;
import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.SystemOfUnits.pi;

public class IonisParamMat {

    public IonisParamMat(Material material) {
        fMaterial = material;
        twoln10 = 2. * Math.log(10);

        fCdensity = 0.0;
        fD0density = 0.0;
        if (fDensityData == null) { fDensityData = new DensityEffectData(); }

        ComputeMeanParameters();
        ComputeDensityEffectParameters();
        ComputeFluctModel();
        ComputeIonParameters();
    }


    private void ComputeMeanParameters() {

        fMeanExcitationEnergy = 0.;
        fLogMeanExcEnergy = 0.;

        int nElements = fMaterial.GetNumberOfElements();
        Vector<Element> elmVector = fMaterial.GetElementVector();
        double[] nAtomsPerVolume = fMaterial.GetVecNbOfAtomsPerVolume();

        fMeanExcitationEnergy = FindMeanExcitationEnergy(fMaterial);

        if (fMeanExcitationEnergy > 0.0) {
            fLogMeanExcEnergy = Math.log(fMeanExcitationEnergy);
        } else {
            for (int i = 0; i < nElements; i++) {
            Element elm = (elmVector).get(i);
                fLogMeanExcEnergy += nAtomsPerVolume[i] * elm.GetZ()
                        * Math.log(elm.GetIonisation().GetMeanExcitationEnergy());
            }
            fLogMeanExcEnergy /= fMaterial.GetTotNbOfElectPerVolume();
            fMeanExcitationEnergy = Math.exp(fLogMeanExcEnergy);
        }

        fShellCorrectionVector = new double[3];

        for (int j = 0; j <= 2; j++) {
            fShellCorrectionVector[j] = 0.;

            for (int k = 0; k < nElements; k++) {
                fShellCorrectionVector[j] += nAtomsPerVolume[k]
                        * (((elmVector).get(k)).GetIonisation().GetShellCorrectionVector())[j];
            }
            fShellCorrectionVector[j] *= 2.0 / fMaterial.GetTotNbOfElectPerVolume();
        }
    }


    private void ComputeDensityEffectParameters() {
        State State = fMaterial.GetState();

        int idx = fDensityData.GetIndex(fMaterial.GetName());
        int nelm = fMaterial.GetNumberOfElements();
        int Z0 = (((fMaterial.GetElementVector())).get(0)).GetZasInt();
        Material bmat = fMaterial.GetBaseMaterial();
        NistManager nist = NistManager.Instance();

        double corrmax = 1.;
        double massfracmax = 0.9;

        // for simple non-NIST materials
        double corr = 0.0;

        if (idx < 0 && 1 == nelm) {
            idx = fDensityData.GetElementIndex(Z0, fMaterial.GetState());
            if (idx >= 0) {
                double dens = nist.GetNominalDensity(Z0);
                if (dens <= 0.0) { idx = -1; }
                else {
                    corr = Math.log(dens / fMaterial.GetDensity());
                }
                if (Math.abs(corr) > corrmax) { idx = -1; }
            }
        }
        if (idx < 0 && null != bmat) {
            idx = fDensityData.GetIndex(bmat.GetName());
            if (idx >= 0) {
                corr = Math.log(bmat.GetDensity() / fMaterial.GetDensity());
                if (Math.abs(corr) > corrmax) { idx = -1; }
            }
        }

        if (idx < 0 && 1 < nelm) {
        double tot = fMaterial.GetTotNbOfAtomsPerVolume();
            for (int i = 0; i < nelm; ++i) {
            double frac = fMaterial.GetVecNbOfAtomsPerVolume()[i] / tot;
                if (frac > massfracmax) {
                    Z0 = (((fMaterial.GetElementVector())).get(i)).GetZasInt();
                    idx = fDensityData.GetElementIndex(Z0, fMaterial.GetState());
                    double dens = nist.GetNominalDensity(Z0);
                    if (idx >= 0 && dens > 0.0) {
                        corr = Math.log(dens / fMaterial.GetDensity());
                        if (Math.abs(corr) > corrmax) { idx = -1; }
                    else { break; }
                    }
                }
            }
        }

        if (idx >= 0) {

            fCdensity = fDensityData.GetCdensity(idx);
            fMdensity = fDensityData.GetMdensity(idx);
            fAdensity = fDensityData.GetAdensity(idx);
            fX0density = fDensityData.GetX0density(idx);
            fX1density = fDensityData.GetX1density(idx);
            fD0density = fDensityData.GetDelta0density(idx);
            fPlasmaEnergy = fDensityData.GetPlasmaEnergy(idx);
            fDensityData.GetAdjustmentFactor(idx);

            fCdensity += corr;
            fX0density += corr / twoln10;
            fX1density += corr / twoln10;

        } else {

            double Cd2 = 4 * pi * hbarc_squared * classic_electr_radius;
            fPlasmaEnergy = Math.sqrt(Cd2 * fMaterial.GetTotNbOfElectPerVolume());

            int icase;

            fCdensity = 1. + 2 * Math.log(fMeanExcitationEnergy / fPlasmaEnergy);

            if ((State == kStateSolid) || (State == kStateLiquid)) {

                double E100eV = 100. * eV;
                double[] ClimiS = {3.681, 5.215};
                double[] X0valS = {1.0, 1.5};
                double[] X1valS = {2.0, 3.0};

                if (fMeanExcitationEnergy < E100eV) { icase = 0; }
                else { icase = 1; }

                if (fCdensity < ClimiS[icase]) { fX0density = 0.2; }
                else { fX0density = 0.326 * fCdensity - X0valS[icase]; }

                fX1density = X1valS[icase];
                fMdensity = 3.0;

                //special: Hydrogen
                if (1 == nelm && 1 == Z0) {
                    fX0density = 0.425;
                    fX1density = 2.0;
                    fMdensity = 5.949;
                }
            } else {
                //
                // gases
                //
                fMdensity = 3.;
                fX1density = 4.0;

                if (fCdensity <= 10.) {
                    fX0density = 1.6;
                } else if (fCdensity <= 10.5) {
                    fX0density = 1.7;
                } else if (fCdensity <= 11.0) {
                    fX0density = 1.8;
                } else if (fCdensity <= 11.5) {
                    fX0density = 1.9;
                } else if (fCdensity <= 12.25) {
                    fX0density = 2.0;
                } else if (fCdensity <= 13.804) {
                    fX0density = 2.0;
                    fX1density = 5.0;
                } else {
                    fX0density = 0.326 * fCdensity - 2.5;
                    fX1density = 5.0;
                }

                //special: Hydrogen
                if (1 == nelm && 1 == Z0) {
                    fX0density = 1.837;
                    fX1density = 3.0;
                    fMdensity = 4.754;
                }

                //special: Helium
                if (1 == nelm && 2 == Z0) {
                    fX0density = 2.191;
                    fX1density = 3.0;
                    fMdensity = 3.297;
                }
            }
        }

        if (State == kStateGas) {
            double Density = fMaterial.GetDensity();
            double Pressure = fMaterial.GetPressure();
            double Temp = fMaterial.GetTemperature();

            double DensitySTP = Density * STP_Pressure * Temp / (Pressure * NTP_Temperature);

            double ParCorr = Math.log(Density / DensitySTP);

            fCdensity -= ParCorr;
            fX0density -= ParCorr / twoln10;
            fX1density -= ParCorr / twoln10;
        }

        if (0.0 == fD0density) {
            double Xa = fCdensity / twoln10;
            fAdensity = twoln10 * (Xa - fX0density)
                    / Math.pow((fX1density - fX0density), fMdensity);
        }
    }


    private void ComputeFluctModel() {
        double Zeff = 0.;
        for (int i = 0; i < fMaterial.GetNumberOfElements(); ++i) {
            Zeff += (fMaterial.GetFractionVector())[i]
                    * (((fMaterial.GetElementVector())).get(i).GetZ());
        }
        fF2fluct = (Zeff > 2.) ? 2. / Zeff : 0.0;

        fF1fluct = 1. - fF2fluct;
        fEnergy2fluct = 10. * Zeff * Zeff * eV;
        fLogEnergy2fluct = Math.log(fEnergy2fluct);
        fLogEnergy1fluct = (fLogMeanExcEnergy - fF2fluct * fLogEnergy2fluct)
                / fF1fluct;
    }


    private void ComputeIonParameters() {
    Vector<Element> theElementVector = fMaterial.GetElementVector();
    double[] theAtomicNumDensityVector =
                fMaterial.GetAtomicNumDensityVector();
    int NumberOfElements = fMaterial.GetNumberOfElements();

        double z = 0.0, vF = 0.0, lF = 0.0, a23 = 0.0;

        if (1 == NumberOfElements) {
        Element element = (theElementVector).get(0);
            z = element.GetZ();
            vF = element.GetIonisation().GetFermiVelocity();
            lF = element.GetIonisation().GetLFactor();
            a23 = 1.0 / Math.pow(element.GetN(), 2.0 / 3.0);

        } else {
            double norm = (0.0);
            for (int iel = 0; iel < NumberOfElements; ++iel) {
            Element element = (theElementVector).get(iel);
            double weight = theAtomicNumDensityVector[iel];
                norm += weight;
                z += element.GetZ() * weight;
                vF += element.GetIonisation().GetFermiVelocity() * weight;
                lF += element.GetIonisation().GetLFactor() * weight;
                a23 += weight / Math.pow(element.GetN(), 2.0 / 3.0);
            }
            z /= norm;
            vF /= norm;
            lF /= norm;
            a23 /= norm;
        }
    }


    public void SetMeanExcitationEnergy(double value) {
        if (value == fMeanExcitationEnergy || value <= 0.0) { return; }


        fMeanExcitationEnergy = value;

        // add corrections to density effect
        double newlog = Math.log(value);
        double corr = 2 * (newlog - fLogMeanExcEnergy);
        fCdensity += corr;
        fX0density += corr / twoln10;
        fX1density += corr / twoln10;

        // recompute parameters of fluctuation model
        fLogMeanExcEnergy = newlog;
        ComputeFluctModel();
    }


    public static double FindMeanExcitationEnergy(Material mat) {
        double res = 0.0;
        // data from density effect data
        if (fDensityData != null) {
            int idx = fDensityData.GetIndex(mat.GetName());
            if (idx >= 0) {
                res = fDensityData.GetMeanIonisationPotential(idx);
            }
        }

    String chFormula = mat.GetChemicalFormula();
        if (!chFormula.isEmpty()) {

            int numberOfMolecula = 54;
            String[] name = {
                    // gas 0 - 12
                    "NH_3", "C_4H_10", "CO_2", "C_2H_6", "C_7H_16-Gas",
                    // "G4_AMMONIA", "G4_BUTANE","G4_CARBON_DIOXIDE","G4_ETHANE", "G4_N-HEPTANE"
                    "C_6H_14-Gas", "CH_4", "NO", "N_2O", "C_8H_18-Gas",
                    // "G4_N-HEXANE" , "G4_METHANE", "x", "G4_NITROUS_OXIDE", "G4_OCTANE"
                    "C_5H_12-Gas", "C_3H_8", "H_2O-Gas",
                    // "G4_N-PENTANE", "G4_PROPANE", "G4_WATER_VAPOR"

                    // liquid 13 - 39
                    "C_3H_6O", "C_6H_5NH_2", "C_6H_6", "C_4H_9OH", "CCl_4",
                    //"G4_ACETONE","G4_ANILINE","G4_BENZENE","G4_N-BUTYL_ALCOHOL","G4_CARBON_TETRACHLORIDE"
                    "C_6H_5Cl", "CHCl_3", "C_6H_12", "C_6H_4Cl_2", "C_4Cl_2H_8O",
                    //"G4_CHLOROBENZENE","G4_CHLOROFORM","G4_CYCLOHEXANE","G4_1,2-DICHLOROBENZENE",
                    //"G4_DICHLORODIETHYL_ETHER"
                    "C_2Cl_2H_4", "(C_2H_5)_2O", "C_2H_5OH", "C_3H_5(OH)_3", "C_7H_16",
                    //"G4_1,2-DICHLOROETHANE","G4_DIETHYL_ETHER","G4_ETHYL_ALCOHOL","G4_GLYCEROL","G4_N-HEPTANE"
                    "C_6H_14", "CH_3OH", "C_6H_5NO_2", "C_5H_12", "C_3H_7OH",
                    //"G4_N-HEXANE","G4_METHANOL","G4_NITROBENZENE","G4_N-PENTANE","G4_N-PROPYL_ALCOHOL",
                    "C_5H_5N", "C_8H_8", "C_2Cl_4", "C_7H_8", "C_2Cl_3H",
                    //"G4_PYRIDINE","G4_POLYSTYRENE","G4_TETRACHLOROETHYLENE","G4_TOLUENE","G4_TRICHLOROETHYLENE"
                    "H_2O", "C_8H_10",
                    // "G4_WATER", "G4_XYLENE"

                    // solid 40 - 53
                    "C_5H_5N_5", "C_5H_5N_5O", "(C_6H_11NO)-nylon", "C_25H_52",
                    // "G4_ADENINE", "G4_GUANINE", "G4_NYLON-6-6", "G4_PARAFFIN"
                    "(C_2H_4)-Polyethylene", "(C_5H_8O_2)-Polymethil_Methacrylate",
                    // "G4_ETHYLENE", "G4_PLEXIGLASS"
                    "(C_8H_8)-Polystyrene", "A-150-tissue", "Al_2O_3", "CaF_2",
                    // "G4_POLYSTYRENE", "G4_A-150_TISSUE", "G4_ALUMINUM_OXIDE", "G4_CALCIUM_FLUORIDE"
                    "LiF", "Photo_Emulsion", "(C_2F_4)-Teflon", "SiO_2"
                    // "G4_LITHIUM_FLUORIDE", "G4_PHOTO_EMULSION", "G4_TEFLON", "G4_SILICON_DIOXIDE"
            };

            double[] meanExcitation = {

                53.7, 48.3, 85.0, 45.4, 49.2,
                        49.1, 41.7, 87.8, 84.9, 49.5,
                        48.2, 47.1, 71.6,

                        64.2, 66.2, 63.4, 59.9, 166.3,
                        89.1, 156.0, 56.4, 106.5, 103.3,
                        111.9, 60.0, 62.9, 72.6, 54.4,
                        54.0, 67.6, 75.8, 53.6, 61.1,
                        66.2, 64.0, 159.2, 62.5, 148.1,
                        75.0, 61.8,

                        71.4, 75.0, 63.9, 48.3, 57.4,
                        74.0, 68.7, 65.1, 145.2, 166.,
                        94.0, 331.0, 99.1, 139.2
            };

            for (int i = 0; i < numberOfMolecula; i++) {
                if (chFormula.equals(name[i])) {
                    res = meanExcitation[i] * eV;
                    break;
                }
            }
        }
        return res;
    }

    public double GetCdensity() { return fCdensity; }

    public double GetMdensity() { return fMdensity; }

    public double GetAdensity() { return fAdensity; }

    public double GetX0density() { return fX0density; }

    public double GetX1density() { return fX1density; }

    public double GetD0density() { return fD0density; }

    public double GetMeanExcitationEnergy() { return fMeanExcitationEnergy; }


    //
    // data members
    //
    private final Material fMaterial;             // this material

    // calculator of the density effect
    double[] fShellCorrectionVector;        // shell correction coefficients

    // parameters for mean energy loss calculation
    double fMeanExcitationEnergy;         //
    double fLogMeanExcEnergy;             //
    // lower limit of Bethe-Bloch formula

    // parameters of the density correction
    double fCdensity;                      // mat.constant
    double fMdensity;                      // exponent
    double fAdensity;                      //
    double fX0density;                     //
    double fX1density;                     //
    double fD0density;

    double fPlasmaEnergy;

    // parameters of the energy loss fluctuation model
    double fF1fluct;
    double fF2fluct;
    double fLogEnergy1fluct;
    double fEnergy2fluct;
    double fLogEnergy2fluct;

    // static data created only once
    static DensityEffectData fDensityData;
    double twoln10;

}
