package org.jlab.rec.ahdc.KalmanFilter;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.jlab.clas.pdg.PDGDatabase;
import org.jlab.clas.pdg.PDGParticle;
import org.jlab.clas.tracking.kalmanfilter.Material;
import org.jlab.clas.tracking.kalmanfilter.Units;
import org.jlab.geom.prim.Point3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.ahdc.Track.Track;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * TODO : - Fix multi hit on the same layer
 *        - Optimize measurement noise and probably use doca as weight
 *        - Fix the wire number (-1)
 *        - Iterate thought multiple tracks per event
 *        - use a flag for simulation
 *        - add target in the map
 *        - move map to initialization engine
 *        - flag for target material
 *        - error px0 use MC !! Bad !! FIX IT FAST
 */

public class KalmanFilter {

	public KalmanFilter(ArrayList<Track> tracks, DataEvent event) {propagation(tracks, event);}

	private void propagation(ArrayList<Track> tracks, DataEvent event) {

		try {
			//If simulation read MC::Particle Bank ------------------------------------------------
			DataBank bankParticle = event.getBank("MC::Particle");
			double   vzmc         = bankParticle.getFloat("vz", 0);
			double   pxmc         = bankParticle.getFloat("px", 0);
			double   pymc         = bankParticle.getFloat("py", 0);
			double   pzmc         = bankParticle.getFloat("pz", 0);

			ArrayList<Point3D> sim_hits = new ArrayList<>();
			sim_hits.add(new Point3D(0, 0, vzmc));

			DataBank bankMC = event.getBank("MC::True");
			for (int i = 0; i < bankMC.rows(); i++) {
				if (bankMC.getInt("pid", i) == 2212) {
					float x = bankMC.getFloat("avgX", i);
					float y = bankMC.getFloat("avgY", i);
					float z = bankMC.getFloat("avgZ", i);
					// System.out.println("r_sim = " + Math.hypot(x, y));
					sim_hits.add(new Point3D(x, y, z));
				}
			}

			/*
			Writer hitsWriter = new FileWriter("hits.dat");
			for (Point3D p : sim_hits) {
				hitsWriter.write("" + p.x() + ", " + p.y() + ", " + p.z() + '\n');
			}
			hitsWriter.close();
			 */


			// Initialization ---------------------------------------------------------------------
			final double      magfield          = -50;
			final PDGParticle proton            = PDGDatabase.getParticleById(2212);
			final int         numberOfVariables = 6;
			final double      tesla             = 0.001;
			final double[]    B                 = {0.0, 0.0, magfield / 10 * tesla};

			// Initialization material map
			HashMap<String, Material> materialHashMap = materialGeneration();

			// Initialization State Vector
			final double x0  = 0.0;
			final double y0  = 0.0;
			final double z0  = tracks.get(0).get_Z0();
			final double px0 = tracks.get(0).get_px();
			final double py0 = tracks.get(0).get_py();
			final double pz0 = tracks.get(0).get_pz();
			double[]     y   = new double[]{x0, y0, z0, px0, py0, pz0};
			// System.out.println("y = " + Arrays.toString(y));

			// Initialization hit
			// System.out.println("tracks = " + tracks);
			ArrayList<org.jlab.rec.ahdc.Hit.Hit> AHDC_hits = tracks.get(0).getHits();
			ArrayList<Hit>                       KF_hits   = new ArrayList<>();
			for (org.jlab.rec.ahdc.Hit.Hit AHDC_hit : AHDC_hits) {
				Hit hit = new Hit(AHDC_hit.getSuperLayerId(), AHDC_hit.getLayerId(), AHDC_hit.getWireId(), AHDC_hit.getNbOfWires(), AHDC_hit.getRadius(), AHDC_hit.getDoca());

				// Do delete hit with same radius
				// boolean aleardyHaveR = false;
				// for (Hit o: KF_hits){
				// 	if (o.r() == hit.r()){
				// 		aleardyHaveR = true;
				// 	}
				// }
				// if (!aleardyHaveR)
				KF_hits.add(hit);
			}

			/*
			Writer hitsWiresWriter = new FileWriter("hits_wires.dat");
			for (Hit h : KF_hits) {
				hitsWiresWriter.write("" + h.getSuperLayer() + ", " + h.getLayer() + ", " + h.getWire() + ", " + h.getDoca() + ", " + h.getNumWires() + ", " + h.getR() + '\n');
			}
			hitsWiresWriter.close();
			 */

			// System.out.println("KF_hits = " + KF_hits);

			final ArrayList<Indicator> forwardIndicators  = forwardIndicators(KF_hits, materialHashMap);
			final ArrayList<Indicator> backwardIndicators = backwardIndicators(KF_hits, materialHashMap);

			// Start propagation
			Stepper     stepper    = new Stepper(y);
			RungeKutta4 RK4        = new RungeKutta4(proton, numberOfVariables, B);
			Propagator  propagator = new Propagator(RK4);

			// ----------------------------------------------------------------------------------------

			// Initialization of the Kalman Fitter
			RealVector initialStateEstimate   = new ArrayRealVector(stepper.y);
			RealMatrix initialErrorCovariance = MatrixUtils.createRealMatrix(new double[][]{{10.0, 0.0, 0.0, 0.0, 0.0, 0.0}, {0.0, 10.0, 0.0, 0.0, 0.0, 0.0}, {0.0, 0.0, 10.0, 0.0, 0.0, 0.0}, {0.0, 0.0, 0.0, 1000.0, 0.0, 0.0}, {0.0, 0.0, 0.0, 0.0, 1000.0, 0.0}, {0.0, 0.0, 0.0, 0.0, 0.0, 1000.0}});

			KFitter kFitter = new KFitter(initialStateEstimate, initialErrorCovariance, stepper, propagator);

			/*
			Stepper stepper_fisrt = new Stepper(y);
			Writer  writer_first  = new FileWriter("track_first.dat");
			for (Indicator indicator : forwardIndicators) {
				stepper_fisrt.initialize(indicator);
				propagator.propagateAndWrite(stepper_fisrt, indicator, writer_first);
			}
			writer_first.close();



			System.out.println("--------- BackWard propagation !! ---------");

			Writer writer_back = new FileWriter("track_back.dat");
			for (Indicator indicator : backwardIndicators) {
				stepper.initialize(indicator);
				propagator.propagateAndWrite(stepper, indicator, writer_back);
			}
			writer_back.close();
			 */


			for (int k = 0; k < 1; k++) {

				// System.out.println("--------- ForWard propagation !! ---------");

				for (Indicator indicator : forwardIndicators) {
					kFitter.predict(indicator);
					if (indicator.haveAHit()) {
						kFitter.correct(indicator);
					}
					// System.out.println("y = " + kFitter.getStateEstimationVector() + " p = " + kFitter.getMomentum());
				}

				// System.out.println("--------- BackWard propagation !! ---------");

				for (Indicator indicator : backwardIndicators) {
					kFitter.predict(indicator);
					if (indicator.haveAHit()) {
						kFitter.correct(indicator);
					}
					// System.out.println("y = " + kFitter.getStateEstimationVector() + " p = " + kFitter.getMomentum());
				}
			}

			/*
			Writer writer_last = new FileWriter("track_last.dat");
			for (Indicator indicator : forwardIndicators) {
				stepper.initialize(indicator);
				propagator.propagateAndWrite(stepper, indicator, writer_last);
			}
			writer_last.close();
			 */


			RealVector x_out = kFitter.getStateEstimationVector();
			tracks.get(0).setPositionAndMomentumForKF(x_out);


		} catch (Exception e) {
			// e.printStackTrace();
		}


	}

	private HashMap<String, Material> materialGeneration() {
		Units units = Units.CM;

		String name_De      = "deuteriumGas";
		double thickness_De = 1;
		double density_De   = 0.0009; // 9.37E-4;
		double ZoverA_De    = 0.496499;
		double X0_De        = 0;
		double IeV_De       = 19.2;

		org.jlab.clas.tracking.kalmanfilter.Material deuteriumGas = new org.jlab.clas.tracking.kalmanfilter.Material(name_De, thickness_De, density_De, ZoverA_De, X0_De, IeV_De, units);

		String name_Bo      = "BONuS12Gas";
		double thickness_Bo = 1;
		double density_Bo   = 4.9778E-4;
		double ZoverA_Bo    = 0.49989;
		double X0_Bo        = 0;
		double IeV_Bo       = 73.8871;

		org.jlab.clas.tracking.kalmanfilter.Material BONuS12 = new org.jlab.clas.tracking.kalmanfilter.Material(name_Bo, thickness_Bo, density_Bo, ZoverA_Bo, X0_Bo, IeV_Bo, units);

		String name_My      = "Mylar";
		double thickness_My = 1;
		double density_My   = 1.4;
		double ZoverA_My    = 0.501363;
		double X0_My        = 0;
		double IeV_My       = 78.7;

		org.jlab.clas.tracking.kalmanfilter.Material Mylar = new org.jlab.clas.tracking.kalmanfilter.Material(name_My, thickness_My, density_My, ZoverA_My, X0_My, IeV_My, units);

		String name_Ka      = "Kapton";
		double thickness_Ka = 1;
		double density_Ka   = 1.42;
		double ZoverA_Ka    = 0.500722;
		double X0_Ka        = 0;
		double IeV_Ka       = 79.6;

		org.jlab.clas.tracking.kalmanfilter.Material Kapton = new org.jlab.clas.tracking.kalmanfilter.Material(name_Ka, thickness_Ka, density_Ka, ZoverA_Ka, X0_Ka, IeV_Ka, units);

		return new HashMap<String, Material>() {
			{
				put("deuteriumGas", deuteriumGas);
				put("Kapton", Kapton);
				put("Mylar", Mylar);
				put("BONuS12Gas", BONuS12);
			}
		};
	}

	ArrayList<Indicator> forwardIndicators(ArrayList<Hit> hitArrayList, HashMap<String, org.jlab.clas.tracking.kalmanfilter.Material> materialHashMap) {
		ArrayList<Indicator> forwardIndicators = new ArrayList<>();
		forwardIndicators.add(new Indicator(3.0, 0.2, null, true, materialHashMap.get("deuteriumGas")));
		forwardIndicators.add(new Indicator(3.063, 0.001, null, true, materialHashMap.get("Kapton")));
		for (Hit hit : hitArrayList) {
			forwardIndicators.add(new Indicator(hit.r(), 0.1, hit, true, materialHashMap.get("BONuS12Gas")));
		}
		return forwardIndicators;
	}

	ArrayList<Indicator> backwardIndicators(ArrayList<Hit> hitArrayList, HashMap<String, org.jlab.clas.tracking.kalmanfilter.Material> materialHashMap) {
		ArrayList<Indicator> backwardIndicators = new ArrayList<>();
		for (int i = hitArrayList.size() - 2; i >= 0; i--) {
			backwardIndicators.add(new Indicator(hitArrayList.get(i).r(), 0.1, hitArrayList.get(i), false, materialHashMap.get("BONuS12Gas")));
		}
		backwardIndicators.add(new Indicator(3.063, 1, null, false, materialHashMap.get("BONuS12Gas")));
		backwardIndicators.add(new Indicator(3.0, 0.001, null, false, materialHashMap.get("Kapton")));
		Hit hit = new Hit_beam(0, 0, 0, 0, 0, 0, 0, 0);
		backwardIndicators.add(new Indicator(0.0, 0.2, hit, false, materialHashMap.get("deuteriumGas")));
		return backwardIndicators;
	}
}
