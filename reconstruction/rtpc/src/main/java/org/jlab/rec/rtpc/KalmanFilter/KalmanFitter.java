package org.jlab.rec.rtpc.KalmanFilter;

import cnuphys.magfield.FastMath;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.jlab.clas.pdg.PDGDatabase;
import org.jlab.clas.pdg.PDGParticle;
import org.jlab.clas.tracking.kalmanfilter.Material;
import org.jlab.rec.rtpc.hit.FinalTrackInfo;
import org.jlab.rec.rtpc.hit.HitParameters;
import org.jlab.rec.rtpc.hit.RecoHitVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class KalmanFitter {
	public KalmanFitter(
			HitParameters params,
			HashMap<Integer, KalmanFitterInfo> KFTrackMap,
			double magfield,
			HashMap<String, Material> _materialMap) {



		// Initialization ---------------------------------------------------------
		final PDGParticle proton = PDGDatabase.getParticleById(2212);
		final int numberOfVariables = 6;
		double tesla = 0.001;
		final double[] B = {0.0, 0.0, magfield / 10 * tesla};

		// Initialize Material List -----------------------------------------------
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
				ArrayList<Indicator> forwardIndicators = forwardIndicators(hitArrayList, _materialMap);
				ArrayList<Indicator> backwardIndicators = backwardIndicators(hitArrayList, _materialMap);

				// Initialization State Vector --------------------------------------------
				double p = params.get_finaltrackinfomap().get(TID).get_p();
				double theta = Math.toRadians(params.get_finaltrackinfomap().get(TID).get_theta());
				double phi = Math.toRadians(params.get_finaltrackinfomap().get(TID).get_phi());

				double p_min = p_correction(theta) * 1000;
				if (p < p_min) p = p_min;

				final double x0 = 0.0;
				final double y0 = 0.0;
				final double z0 = params.get_finaltrackinfomap().get(TID).get_vz();
				final double px0 = p * Math.sin(theta) * Math.cos(phi);
				final double py0 = p * Math.sin(theta) * Math.sin(phi);
				final double pz0 = p * Math.cos(theta);

				double[] y = new double[]{x0, y0, z0, px0, py0, pz0};

				// Start the Kalman Fitter
				Stepper stepper = new Stepper(y);
				RungeKutta4 RK4 = new RungeKutta4(proton, numberOfVariables, B);
				Propagator propagator = new Propagator(RK4);

				// Initialization of the Kalman Fitter
				RealVector initialStateEstimate = new ArrayRealVector(stepper.y);
				RealMatrix initialErrorCovariance = MatrixUtils.createRealMatrix(new double[][]{
						{10.0, 0.0, 0.0, 0.0, 0.0, 0.0},
						{0.0, 10.0, 0.0, 0.0, 0.0, 0.0},
						{0.0, 0.0, 10.0, 0.0, 0.0, 0.0},
						{0.0, 0.0, 0.0, 1000.0, 0.0, 0.0},
						{0.0, 0.0, 0.0, 0.0, 1000.0, 0.0},
						{0.0, 0.0, 0.0, 0.0, 0.0, 1000.0}});

				KFitter kFitter = new KFitter(initialStateEstimate, initialErrorCovariance, stepper, propagator);

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

				KFitter kfitter = new KFitter(initialStateEstimate, initialErrorCovariance, new Stepper(kFitter.getStateEstimationVector().toArray()), new Propagator(RK4));
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

	ArrayList<Indicator> forwardIndicators(
			ArrayList<Hit> hitArrayList,
			HashMap<String, org.jlab.clas.tracking.kalmanfilter.Material> materialHashMap) {
		ArrayList<Indicator> forwardIndicators = new ArrayList<>();
		forwardIndicators.add(new Indicator(3.0, 0.2, null, true, materialHashMap.get("deuteriumGas")));
		forwardIndicators.add(new Indicator(3.063, 0.001, null, true, materialHashMap.get("Kapton")));
		forwardIndicators.add(new Indicator(20.0, 1, null, true, materialHashMap.get("BONuS12Gas")));
		forwardIndicators.add(new Indicator(20.006, 0.001, null, true, materialHashMap.get("Mylar")));
		forwardIndicators.add(new Indicator(30.0, 1, null, true, materialHashMap.get("BONuS12Gas")));
		forwardIndicators.add(new Indicator(30.004, 0.001, null, true, materialHashMap.get("Mylar")));
		for (Hit hit : hitArrayList) {
			forwardIndicators.add(new Indicator(hit.r(), 0.1, hit, true, materialHashMap.get("BONuS12Gas")));
		}
		return forwardIndicators;
	}

	ArrayList<Indicator> backwardIndicators(
			ArrayList<Hit> hitArrayList,
			HashMap<String, org.jlab.clas.tracking.kalmanfilter.Material> materialHashMap) {
		ArrayList<Indicator> backwardIndicators = new ArrayList<>();
		for (int i = hitArrayList.size() - 2; i >= 0; i--) {
			backwardIndicators.add(new Indicator(hitArrayList.get(i).r(), 0.1, hitArrayList.get(i), false, materialHashMap.get("BONuS12Gas")));
		}
		backwardIndicators.add(new Indicator(30.004, 0.1, null, false, materialHashMap.get("BONuS12Gas")));
		backwardIndicators.add(new Indicator(30.0, 0.001, null, false, materialHashMap.get("Mylar")));
		backwardIndicators.add(new Indicator(20.006, 1, null, false, materialHashMap.get("BONuS12Gas")));
		backwardIndicators.add(new Indicator(20.0, 0.001, null, false, materialHashMap.get("Mylar")));
		backwardIndicators.add(new Indicator(3.063, 1, null, false, materialHashMap.get("BONuS12Gas")));
		backwardIndicators.add(new Indicator(3.0, 0.001, null, false, materialHashMap.get("Kapton")));
		Hit hit = new Hit(0.0, 0.0, 0.0, 1, 1);
		backwardIndicators.add(new Indicator(0.0, 0.2, hit, false, materialHashMap.get("deuteriumGas")));
		return backwardIndicators;
	}

	double p_correction(double theta) {

		double[] params = {0.075, 0.024, -0.004232, -0.003898, -0.0004198, 0.001195, 0.001358};

		return params[6] * Math.pow(theta - Math.PI / 2, 12)
				+ params[5] * Math.pow(theta - Math.PI / 2, 10)
				+ params[4] * Math.pow(theta - Math.PI / 2, 8)
				+ params[3] * Math.pow(theta - Math.PI / 2, 6)
				+ params[2] * Math.pow(theta - Math.PI / 2, 4)
				+ params[1] * Math.pow(theta - Math.PI / 2, 2)
				+ params[0];
	}
}
