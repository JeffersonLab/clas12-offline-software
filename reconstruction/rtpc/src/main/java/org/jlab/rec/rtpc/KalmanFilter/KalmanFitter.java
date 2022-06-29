package org.jlab.rec.rtpc.KalmanFilter;

import cnuphys.magfield.FastMath;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.*;
import org.jlab.rec.rtpc.hit.FinalTrackInfo;
import org.jlab.rec.rtpc.hit.HitParameters;
import org.jlab.rec.rtpc.hit.RecoHitVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.Material.NTP_Temperature;
import static org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.State.kStateGas;

public class KalmanFitter {
	public KalmanFitter(
			HitParameters params,
			HashMap<Integer, KalmanFitterInfo> KFTrackMap,
			double magfield) {

		// Initialization ---------------------------------------------------------
		final BetheBlochModel model = new BetheBlochModel();
		final Particle proton = new Proton();
		final int numberOfVariables = 6;
		final double[] B = {0.0, 0.0, magfield / 10 * SystemOfUnits.tesla};

		// Initialize Material List -----------------------------------------------
		final NistManager manager = NistManager.Instance();
		final HashMap<String, Material> materialMap = generateMaterials(manager);

		HashMap<Integer, FinalTrackInfo> FinaltrackinfoMap = params.get_finaltrackinfomap();
		HashMap<Integer, List<RecoHitVector>> RecotrackMap = params.get_recotrackmap();

		for (int TID : FinaltrackinfoMap.keySet()) {

			try {
				// Generate Hit -----------------------------------------------------------
				ArrayList<Hit> hitArrayList = new ArrayList<>();
				FinalTrackInfo finalTrackInfo = params.get_finaltrackinfomap().get(TID);
				double ADCTot = finalTrackInfo.get_ADCsum();
				for (RecoHitVector hit : RecotrackMap.get(TID)) {
					double x = hit.x();
					double y = hit.y();
					double r = FastMath.hypot(x, y);
					double phi = FastMath.atan2(y, x);
					double z = hit.z();
					double adc = hit.adc();
					hitArrayList.add(new Hit(r, phi, z, adc, ADCTot));
				}

				// Initialization Indicators list -----------------------------------------
				ArrayList<Indicator> forwardIndicators = forwardIndicators(materialMap, hitArrayList);
				ArrayList<Indicator> backwardIndicators = backwardIndicators(materialMap, hitArrayList);

				// Initialization State Vector --------------------------------------------
				double p = params.get_finaltrackinfomap().get(TID).get_p();
				double theta = Math.toRadians(params.get_finaltrackinfomap().get(TID).get_theta());
				double phi = Math.toRadians(params.get_finaltrackinfomap().get(TID).get_phi());

				double p_min = p_correction(theta) * 1000;
				if (p < p_min) p = p_min;

				final double x0 = 0.0 * SystemOfUnits.mm;
				final double y0 = 0.0 * SystemOfUnits.mm;
				final double z0 = params.get_finaltrackinfomap().get(TID).get_vz() * SystemOfUnits.mm;
				final double px0 = p * Math.sin(theta) * Math.cos(phi) * SystemOfUnits.MeV;
				final double py0 = p * Math.sin(theta) * Math.sin(phi) * SystemOfUnits.MeV;
				final double pz0 = p * Math.cos(theta) * SystemOfUnits.MeV;

				double[] y = new double[]{x0, y0, z0, px0, py0, pz0};

				// Start the Kalman Fitter
				Stepper stepper = new Stepper(y);
				RungeKutta4 RK4 = new RungeKutta4(proton, model, numberOfVariables, B);
				Propagator propagator = new Propagator(RK4);

				// Initialization of the Kalman Fitter
				RealVector initialStateEstimate = new ArrayRealVector(stepper.y);
				RealMatrix initialErrorCovariance =
						MatrixUtils.createRealMatrix(
								new double[][]{
										{10.0, 0.0, 0.0, 0.0, 0.0, 0.0},
										{0.0, 10.0, 0.0, 0.0, 0.0, 0.0},
										{0.0, 0.0, 10.0, 0.0, 0.0, 0.0},
										{0.0, 0.0, 0.0, 1000.0, 0.0, 0.0},
										{0.0, 0.0, 0.0, 0.0, 1000.0, 0.0},
										{0.0, 0.0, 0.0, 0.0, 0.0, 1000.0}
								});

				KFitter kFitter =
						new KFitter(initialStateEstimate, initialErrorCovariance, stepper, propagator);

				for (int k = 0; k < 1; k++) {

					for (Indicator indicator : forwardIndicators) {
						kFitter.predict(indicator);
						if (indicator.haveAHit()) {
							kFitter.correct(indicator);
						}
					}

					for (Indicator indicator : backwardIndicators) {
						kFitter.predict(indicator);
						if (indicator.haveAHit()) {
							kFitter.correct(indicator);
						}
					}
				}

				KFitter kfitter =
						new KFitter(
								initialStateEstimate,
								initialErrorCovariance,
								new Stepper(kFitter.getStateEstimationVector().toArray()),
								new Propagator(RK4));
				for (Indicator indicator : forwardIndicators) {
					kfitter.predict(indicator);
				}

				double s = kfitter.stepper.s_drift;
				double dEdx = ADCTot / s;
				double p_drift = kfitter.stepper.p();
				// double chi2 = kFitter.chi2;

				double vz = stepper.y[2];
				double px = stepper.y[3];
				double py = stepper.y[4];
				double pz = stepper.y[5];
				KalmanFitterInfo output = new KalmanFitterInfo(px, py, pz, vz, dEdx, p_drift);
				KFTrackMap.put(TID, output);


			} catch (Exception ignored) {
			}
		}
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

		return new HashMap<>() {
			{
				put(deuteriumGas.GetName(), deuteriumGas);
				put("Kapton", Kapton);
				put("Mylar", Mylar);
				put(BONuSGas.GetName(), BONuSGas);
			}
		};
	}

	ArrayList<Indicator> forwardIndicators(
			HashMap<String, Material> materialMap, ArrayList<Hit> hitArrayList) {
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
		return forwardIndicators;
	}

	ArrayList<Indicator> backwardIndicators(
			HashMap<String, Material> materialMap, ArrayList<Hit> hitArrayList) {
		ArrayList<Indicator> backwardIndicators = new ArrayList<>();
		for (int i = hitArrayList.size() - 2; i >= 0; i--) {
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
		return backwardIndicators;
	}

	double p_correction(double theta) {

		double[] params = {0.07909, 0.024, -0.004232, -0.003898, -0.0004198, 0.001195, 0.001358};

		return params[6] * Math.pow(theta - Math.PI / 2, 12)
				+ params[5] * Math.pow(theta - Math.PI / 2, 10)
				+ params[4] * Math.pow(theta - Math.PI / 2, 8)
				+ params[3] * Math.pow(theta - Math.PI / 2, 6)
				+ params[2] * Math.pow(theta - Math.PI / 2, 4)
				+ params[1] * Math.pow(theta - Math.PI / 2, 2)
				+ params[0];
	}
}
