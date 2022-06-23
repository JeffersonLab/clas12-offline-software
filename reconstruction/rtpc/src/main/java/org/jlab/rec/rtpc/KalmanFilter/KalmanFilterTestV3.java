package org.jlab.rec.rtpc.KalmanFilter;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.*;
import org.jlab.rec.rtpc.hit.FinalTrackInfo;
import org.jlab.rec.rtpc.hit.HitParameters;
import org.jlab.rec.rtpc.hit.RecoHitVector;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.Material.NTP_Temperature;
import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.State.kStateGas;

public class KalmanFilterTestV3 {

  public KalmanFilterTestV3(
      HitParameters params, DataEvent event, HashMap<Integer, List<RecoHitVector>> reconTrackMap)
      throws Exception {

    // Initialization ---------------------------------------------------------
    final BetheBlochModel model = new BetheBlochModel();
    final Particle proton = new Proton();
    final int numberOfVariables = 6;
    final double[] B = {0.0, 0.0, -0.745 * 5 * SystemOfUnits.tesla};

    // Initialize Material List -----------------------------------------------
    final NistManager manager = NistManager.Instance();
    final HashMap<String, Material> materialMap = generateMaterials(manager);

    // Initialization State Vector --------------------------------------------
    int tid = 0;
    for (int TID : reconTrackMap.keySet()) {
      tid = TID;
    }

    double p = params.get_finaltrackinfomap().get(tid).get_p() + 50;
    double theta = Math.toRadians(params.get_finaltrackinfomap().get(tid).get_theta()) + 0.1;
    double phii = Math.toRadians(params.get_finaltrackinfomap().get(tid).get_phi())  + 0.3;

    System.out.println("p = " + p + " theta = " + theta + " phi = " + phii);

    final double x0 = 0.0 * SystemOfUnits.mm;
    final double y0 = 0.0 * SystemOfUnits.mm;
    final double z0 = params.get_finaltrackinfomap().get(tid).get_vz() * SystemOfUnits.mm;
    final double px0 = p * Math.sin(theta) * Math.cos(phii) * SystemOfUnits.MeV;
    final double py0 = p * Math.sin(theta) * Math.sin(phii) * SystemOfUnits.MeV;
    final double pz0 = p * Math.cos(theta) * SystemOfUnits.MeV;
    double[] y = new double[] {x0, y0, z0, px0, py0, pz0};

    // Generate Hit -----------------------------------------------------------
    ArrayList<Hit> hitArrayList = new ArrayList<>();
    for (int TID : reconTrackMap.keySet()) {
      FinalTrackInfo finalTrackInfo = params.get_finaltrackinfomap().get(TID);
      double ADCTot = finalTrackInfo.get_ADCsum();
      for (RecoHitVector hit : reconTrackMap.get(TID)) {
        double r = hit.r();
        double x = hit.x();
        double yy = hit.y();
        double phi = Math.atan2(yy, x);
        double z = hit.z();
        double adc = hit.adc();
        hitArrayList.add(new Hit(r, phi, z, adc, ADCTot));
      }
    }

    // Generate Indicators List -------------------------------------------------------------------
    ArrayList<Indicator> forwardIndicators = new ArrayList<>();
    forwardIndicators.add(new Indicator(3.0, 0.2, materialMap.get("deuteriumGas"), null, true));
    forwardIndicators.add(new Indicator(3.063, 0.001, materialMap.get("Kapton"), null, true));
    forwardIndicators.add(new Indicator(20.0, 1, materialMap.get("BONuSGas"), null, true));
    forwardIndicators.add(new Indicator(20.006, 0.001, materialMap.get("Mylar"), null, true));
    forwardIndicators.add(new Indicator(30.0, 1, materialMap.get("BONuSGas"), null, true));
    forwardIndicators.add(new Indicator(30.004, 0.001, materialMap.get("Mylar"), null, true));
    for (Hit hit : hitArrayList) {
      forwardIndicators.add(new Indicator(hit.r(), 0.1, materialMap.get("BONuSGas"), hit, true));
    }

    ArrayList<Indicator> backwardIndicators = new ArrayList<>();
    for (int i = hitArrayList.size() - 1; i >= 0; i--) {
      backwardIndicators.add(
          new Indicator(
              hitArrayList.get(i).r(),
              0.1,
              materialMap.get("BONuSGas"),
              hitArrayList.get(i),
              false));
    }
    backwardIndicators.add(new Indicator(30.004, 0.1, materialMap.get("BONuSGas"), null, false));
    backwardIndicators.add(new Indicator(30.0, 0.001, materialMap.get("Mylar"), null, false));
    backwardIndicators.add(new Indicator(20.006, 1, materialMap.get("BONuSGas"), null, false));
    backwardIndicators.add(new Indicator(20.0, 0.001, materialMap.get("Mylar"), null, false));
    backwardIndicators.add(new Indicator(3.063, 1, materialMap.get("BONuSGas"), null, false));
    backwardIndicators.add(new Indicator(3.0, 0.001, materialMap.get("Kapton"), null, false));
    Hit hit = new Hit(0.0, 0.0, 0.0, 1, 1);
    backwardIndicators.add(new Indicator(0.0, 0.2, materialMap.get("deuteriumGas"), hit, false));

    // Start the Kalman Fitter --------------------------------------------------------------------
    Stepper stepper = new Stepper(y);
    RungeKutta4 RK4 = new RungeKutta4(proton, model, numberOfVariables, B);
    Propagator propagator = new Propagator(RK4);

    // Initialization of the Kalman Fitter --------------------------------------------------------
    RealVector initialStateEstimate = new ArrayRealVector(stepper.y);
    RealMatrix initialErrorCovariance =
        MatrixUtils.createRealIdentityMatrix(6).scalarMultiply(1000);

    KFitter kFitter =
        new KFitter(initialStateEstimate, initialErrorCovariance, stepper, propagator);

    forwardPropagationTest(
        forwardIndicators, new Stepper(y), propagator, "output_forward_track_first.dat");

    FileWriter fWriter = new FileWriter("PlotTrack/" + "iteration.dat", true);

    try {
    for (int k = 0; k < 100; k++) {


      for (Indicator indicator : forwardIndicators) {
        kFitter.predict(indicator, k);
        if (indicator.haveAHit()) {
          kFitter.correct(indicator);
        }
      }

      for (Indicator indicator : backwardIndicators) {
        kFitter.predict(indicator, k);
        if (indicator.haveAHit()) {
          kFitter.correct(indicator);
        }
      }


        stepper.print();

        if (k == 0) fWriter.write("New track : " + '\n');
        DataBank particle = event.getBank("MC::Particle");
        double px_mc = particle.getFloat("px", 0) * 1000;
        double py_mc = particle.getFloat("py", 0) * 1000;
        double pz_mc = particle.getFloat("pz", 0) * 1000;
        double p_mc = Math.sqrt(px_mc * px_mc + py_mc * py_mc + pz_mc * pz_mc);
        double pp =
            Math.sqrt(
                stepper.y[3] * stepper.y[3]
                    + stepper.y[4] * stepper.y[4]
                    + stepper.y[5] * stepper.y[5]);
        fWriter.write("" + k + ' ' + pp + ' ' + p_mc + '\n');


    }
    } catch (Exception e) {
      e.printStackTrace();
    }
    fWriter.close();

    forwardPropagationTest(
        forwardIndicators,
        new Stepper(kFitter.getStateEstimationVector().toArray()),
        propagator,
        "output_forward_track_last.dat");

    saveHits(hitArrayList);
  }

  HashMap<String, Material> generateMaterials(NistManager manager) {
    Material CO2 = manager.FindOrBuildMaterial("G4_CARBON_DIOXIDE", false, false);
    Material He = manager.FindOrBuildMaterial("G4_He", false, false);
    Material Kapton = manager.FindOrBuildMaterial("G4_KAPTON", false, false);
    Material Mylar = manager.FindOrBuildMaterial("G4_MYLAR", false, false);

    double He_prop = 0.8;
    double CO2_prop = 0.2;
    double He_dens = 0.0001664;
    double CO2_dens = 0.0018233;
    double He_fractionMass = (He_prop * He_dens) / (He_prop * He_dens + CO2_prop * CO2_dens);
    double CO2_fractionMass = (CO2_prop * CO2_dens) / (He_prop * He_dens + CO2_prop * CO2_dens);
    double bonusGas_Density = He_prop * He_dens + CO2_prop * CO2_dens;
    double density_BONuSGas_GEANT4 = bonusGas_Density * SystemOfUnits.g / SystemOfUnits.cm3;
    Material BONuSGas =
        new Material(
            "BONuSGas",
            density_BONuSGas_GEANT4,
            2,
            kStateGas,
            NTP_Temperature,
            PhysicalConstants.STP_Pressure);
    BONuSGas.AddMaterial(CO2, CO2_fractionMass);
    BONuSGas.AddMaterial(He, He_fractionMass);

    Isotope deuteron =
        new Isotope("deuteron", 1, 2, 2.0141018 * SystemOfUnits.g / SystemOfUnits.mole, 0);
    Element deuterium = new Element("deuterium", "deuterium", 1);
    deuterium.AddIsotope(deuteron, 1);
    Material deuteriumGas =
        new Material(
            "deuteriumGas",
            0.000937 * SystemOfUnits.g / SystemOfUnits.cm3,
            1,
            kStateGas,
            NTP_Temperature,
            5.6 * SystemOfUnits.atmosphere);
    deuteriumGas.AddElement(deuterium, 1);

    return new HashMap<String, Material>() {
      {
        put(deuteriumGas.GetName(), deuteriumGas);
        put("Kapton", Kapton);
        put("Mylar", Mylar);
        put(BONuSGas.GetName(), BONuSGas);
      }
    };
  }

  void forwardPropagationTest(
      ArrayList<Indicator> forwardIndicators,
      Stepper stepper,
      Propagator propagator,
      String fileName) {
    FileWriter fWriter = null;
    try {
      fWriter = new FileWriter("PlotTrack/" + fileName, true);
      fWriter.write("New track : " + '\n');
      for (Indicator indicator : forwardIndicators) {
        stepper.initialize(indicator);
        propagator.propagate(stepper, indicator.R, fWriter);
      }
      fWriter.close();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (fWriter != null) fWriter.close();
      } catch (IOException io) {
        io.printStackTrace();
      }
    }
  }

  void fullPropagationTest(
      ArrayList<Indicator> forwardIndicators,
      ArrayList<Indicator> backwardIndicators,
      Stepper stepper,
      Propagator propagator) {
    try {
      FileWriter fWriter = new FileWriter("PlotTrack/output_forward_track.dat");
      for (Indicator indicator : forwardIndicators) {
        stepper.initialize(indicator);
        propagator.propagate(stepper, indicator.R, fWriter);
      }
      fWriter.close();

      System.out.println("            Going Backward !! ");
      FileWriter fWriter1 = new FileWriter("PlotTrack/output_backward_track.dat");
      for (Indicator indicator : backwardIndicators) {
        stepper.initialize(indicator);
        propagator.propagate(stepper, indicator.R, fWriter1);
      }
      fWriter1.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  void saveHits(ArrayList<Hit> hitArrayList) {
    try {
      FileWriter fWriter = new FileWriter("PlotTrack/Hits.dat", true);
      fWriter.write("New track : " + '\n');
      for (Hit hit : hitArrayList) {
        fWriter.write("" + hit.x() + ' ' + hit.y() + ' ' + hit.z() + '\n');
      }
      fWriter.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
