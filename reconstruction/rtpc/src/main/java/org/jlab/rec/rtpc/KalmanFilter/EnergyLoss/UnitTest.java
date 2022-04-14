package org.jlab.rec.rtpc.KalmanFilter.EnergyLoss;

import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.Material.NTP_Temperature;
import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.State.kStateGas;
import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.SystemOfUnits.*;

public class UnitTest {

    public UnitTest(){
        NistManager manager = NistManager.Instance();
        Material CO2 = manager.FindOrBuildMaterial("G4_CARBON_DIOXIDE",false, false);
        Material He = manager.FindOrBuildMaterial("G4_He",false, false);
        Material Kapton = manager.FindOrBuildMaterial("G4_KAPTON",false, false);
        Material Mylar = manager.FindOrBuildMaterial("G4_MYLAR",false, false);

        double He_prop = 0.8;
        double CO2_prop = 0.2;
        double He_dens = 0.0001664;
        double CO2_dens = 0.0018233;
        double He_fractionMass = (He_prop*He_dens)/(He_prop*He_dens + CO2_prop*CO2_dens);
        double CO2_fractionMass = (CO2_prop*CO2_dens)/(He_prop*He_dens + CO2_prop*CO2_dens);
        double bonusGas_Density = He_prop*He_dens+CO2_prop*CO2_dens;
        double density_BONuSGas_GEANT4 = bonusGas_Density * g /  cm3;
        Material BONuSGas = new Material("BONuSGas", density_BONuSGas_GEANT4, 2, kStateGas,NTP_Temperature, PhysicalConstants.STP_Pressure);
        BONuSGas.AddMaterial(CO2, CO2_fractionMass);
        BONuSGas.AddMaterial(He, He_fractionMass);

        Isotope deuteron = new Isotope("deuteron", 1, 2, 2.0141018 * g / SystemOfUnits.mole,0);
        Element deuterium = new Element("deuterium", "deuterium", 1);
        deuterium.AddIsotope(deuteron, 1);
       Material deuteriumGas = new Material("deuteriumGas", 0.000937 * g / cm3, 1, kStateGas,
                294.25 * SystemOfUnits.kelvin, 5.6*SystemOfUnits.atmosphere);
        deuteriumGas.AddElement(deuterium, 1);


        double kineticEnergy = 3.405;
        Particle proton = new Proton();
        BetheBlochModel model = new BetheBlochModel();


        System.out.println("------------------------------------------------------");
        double dedx_Kapton = model.ComputeDEDXPerVolume(proton, Kapton, kineticEnergy, true) / (Kapton.GetDensity() / g * cm3) * 1000;
        System.out.println("density_Kapton = " + Kapton.GetDensity() / g * cm3 + " g/cm^3");
        System.out.println("dedx_Kapton = " + dedx_Kapton + " MeV•g/cm^2");

        System.out.println("------------------------------------------------------");
        double dedx_Deuterium = model.ComputeDEDXPerVolume(proton, deuteriumGas, kineticEnergy, true) / (deuteriumGas.GetDensity() / g * cm3) * 1000;
        System.out.println("density_Kapton = " + Kapton.GetDensity() / g * cm3 + " g/cm^3");
        System.out.println("dedx_Kapton = " + dedx_Deuterium + " MeV•g/cm^2");

    }

}
