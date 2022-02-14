package cnuphys.adaptiveSwim.test;

import java.util.Random;

import cnuphys.adaptiveSwim.AdaptiveSwimException;
import cnuphys.adaptiveSwim.AdaptiveSwimResult;
import cnuphys.adaptiveSwim.AdaptiveSwimmer;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.MagneticFields.FieldType;
import cnuphys.rk4.RungeKuttaException;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.Swimmer;

public class AdaptiveBeamlineSwimTest {
	
	//usual small number cutoff
		private static final double SMALL = 1.0e-8;
		
		public static void beamLineTest() {
			double accuracy = 5e-3; // m
			double eps = 1.0e-5;
			double zTarg = 5; // m
			
			LineTestPlotGrid plotGrid = new LineTestPlotGrid(zTarg, accuracy, eps);
			plotGrid.setVisible(true);
			
			long seed = 37552875;
			Random rand = new Random(seed);
	//		int num = 62500;
			int num = 100;
			int n0 = 0;
			

			System.out.println("TEST swimming to the beam line");
			MagneticFields.getInstance().setActiveField(FieldType.COMPOSITE);

			double maxPathLength = 8; // m
			

			double stepsizeAdaptive = 0.01; // starting
			
			Swimmer swimmer = new Swimmer();
			AdaptiveSwimmer adaptiveSwimmer = new AdaptiveSwimmer();
			AdaptiveSwimResult oldResult = new AdaptiveSwimResult(false);
			AdaptiveSwimResult newResult = new AdaptiveSwimResult(true);

	//ranges of variables
			
			double xoMin = -0.1;
			double xoMax = 0.1;
			double yoMin = -0.1;
			double yoMax = 0.1;
			double zoMin = 0;
			double zoMax = 0;
			double pMin = 5.0;
			double pMax = 8.0;
			double thetaMin = 20.0;
			double thetaMax = 35.0;
			
			double phiMin = 0;
			double phiMax = 360;
			
			
	        double hdata[] = new double[3];
	        
	        //for the path length differences
	        double sDiffSum = 0;
	        double worstSDiff = 0;
	        int worstSIndex = -1;
	        
	        //final z diff
	        double zDiffSum = 0;
	        double worstZDiff = 0;
	        int worstZIndex = -1;
	        
	        //final pos diff
	        double rDiffSum = 0;
	        double worstRDiff = 0;
	        int worstRIndex = -1;
	        
	        //final phi diff
	        double phiDiffSum = 0;
	        double worstPhiDiff = 0;
	        int worstPhiIndex = -1;
	        
	      //final bdl diff
	        double bdlDiffSum = 0;
	        double worstBdlDiff = 0;
	        int worstBdlIndex = -1;

					
			SwimTrajectory traj = null;
					
			for (int i = n0; i < num; i++) {
				
				if (((i+1) % 1000) == 0) {
					System.out.println((i+1) + "/" + num);
				}
				
				double xo = randVal(rand, xoMin, xoMax);
				double yo = randVal(rand, yoMin, yoMax);
				double zo = randVal(rand, zoMin, zoMax);
				double p = randVal(rand, pMin, pMax);
				double theta = randVal(rand, thetaMin, thetaMax);
				double phi = randVal(rand, phiMin, phiMax);
				
				int charge = randCharge(rand);
				
				//use each swimmer to swim forwad, the reverse and swim backward
				
				//old swimmer
				try {
					traj = swimmer.swim(charge, xo, yo, zo, p, theta, phi, zTarg, accuracy, maxPathLength, stepsizeAdaptive, Swimmer.CLAS_Tolerance, hdata);
					traj.computeBDL(swimmer.getProbe());
					oldResult.setTrajectory(traj);
					//cause this old swimmer does not call init
					oldResult.setInitialValues(charge, xo, yo, zo, p, theta, phi);
				} catch (RungeKuttaException e) {
					e.printStackTrace();
				}
	            
				
				//new swimmer
				try {
					adaptiveSwimmer.swimZ(charge, xo, yo, zo, p, theta, phi, zTarg, accuracy, maxPathLength, stepsizeAdaptive, eps, newResult);
					newResult.getTrajectory().computeBDL(swimmer.getProbe());
				} catch (AdaptiveSwimException e) {
					e.printStackTrace();
				}
				
				AdaptiveResultDiff diff = new AdaptiveResultDiff(oldResult, newResult);
				
				plotGrid.update(diff);
				
				boolean newWorst = false;
				
				//path length diff
				double sdiff = diff.getFinalAbsSDiff();
				sDiffSum += sdiff;
				
				if (sdiff > worstSDiff) {
					newWorst = true;
					worstSDiff = sdiff;
					worstSIndex = i;
				}
				
				//final z diff
				double zdiff = diff.getFinalAbsZDiff();
				zDiffSum += zdiff;
				
				if (zdiff > worstZDiff) {
					newWorst = true;
					worstZDiff = zdiff;
					worstZIndex = i;
				}

				//final position diff
				double rdiff = diff.getFinalAbsPositionDiff();
				rDiffSum += rdiff;
				
				if (rdiff > worstRDiff) {
					newWorst = true;
					worstRDiff = rdiff;
					worstRIndex = i;
					
				}
				
				//phi diff
				double phiDiff = Math.abs(diff.getFinalPhiDiff());
				phiDiffSum += phiDiff;
				
				if (phiDiff > worstPhiDiff) {
					newWorst = true;
					worstPhiDiff = phiDiff;
					worstPhiIndex = i;
					
				}
				
				//bdl diff
				double bdlDiff = Math.abs(diff.getBDLDiff());
				bdlDiffSum += bdlDiff;
				
				if (bdlDiff > worstBdlDiff) {
					newWorst = true;
					worstBdlDiff = bdlDiff;
					worstBdlIndex = i;
					
				}
				
				
				if (newWorst) {
					System.out.println("INDEX: " + i);
					oldResult.printOut(System.out, " new worst sector Z test (old)");
					newResult.printOut(System.out, " new worst sector Z test (new)");
				}

			}
			
			
			//print last
			
			System.out.println("Avg sdiff: " + (sDiffSum/num) + "  worst: " + worstSDiff + "  at index: " + worstSIndex);
			System.out.println("Avg zdiff: " + (zDiffSum/num) + "  worst: " + worstZDiff + "  at index: " + worstZIndex);
			System.out.println("Avg rdiff: " + (rDiffSum/num) + "  worst: " + worstRDiff + "  at index: " + worstRIndex);
			System.out.println("Avg phidiff: " + (phiDiffSum/num) + "  worst: " + worstPhiDiff + "  at index: " + worstPhiIndex);
			System.out.println("Avg bdldiff: " + (bdlDiffSum/num) + "  worst: " + worstBdlDiff + "  at index: " + worstBdlIndex);

			System.out.println("Done with z-test");
		}
		
		
		//used to generate a random number in a range
			private static double randVal(Random rand, double vmin, double vmax) {
				double del = vmax - vmin;

				if (Math.abs(del) < SMALL) {
					return vmin;
				} else {
					return vmin + del * rand.nextDouble();
				}
			}
			
			private static int randCharge(Random rand) {
				double v =  rand.nextDouble();
				return (v < 0.5) ? -1 : 1;
			}

		
		//get a random sector 1..6
		private static int randSector(Random rand) {
			return rand.nextInt(6) + 1;
		}

}
