package cnuphys.swimtest;

import java.util.Random;

import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.MagneticFields.FieldType;
import cnuphys.magfield.RotatedCompositeProbe;
import cnuphys.rk4.RungeKutta;
import cnuphys.rk4.RungeKuttaException;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.Swimmer;
import cnuphys.swim.Swimmer2;
import cnuphys.swimZ.SwimZ;
import cnuphys.swimZ.SwimZException;
import cnuphys.swimZ.SwimZResult;
import cnuphys.swimZ.SwimZStateVector;

public class CompareSwimmers {
	
	// the angle in degrees for rotating between tilted and sector CS
	private static final double _angle = 25.0;
	private static final double _sin25 = Math.sin(Math.toRadians(_angle));
	private static final double _cos25 = Math.cos(Math.toRadians(_angle));

	
	//for testing special cases that are failing in swimZ
	public static void specialCaseTest() {
		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITEROTATED);

		double xo = -1.391717; //m
		double yo = 0.049991; //m
		double zo = 5.059315; //m
		double p = 3.904347; //GeV
		double theta = 160.903767;
		double phi =  -0.837846;
		double zTarg = 1.8; //m

		
		Swimmer swimmer1 = new Swimmer(MagneticFields.getInstance().getRotatedCompositeField());

		SwimZ swimZ = new SwimZ(MagneticFields.getInstance().getRotatedCompositeField());
		double accuracy = 1.0e-5; //m
		double stepSize = 0.01; //m
		double stepSizeCM = stepSize*100; //cm

		double hdata[] = new double[3];
		
		//swimZ uses CM
		SwimZStateVector szV = new SwimZStateVector(xo*100, yo*100, zo*100, p, theta, phi);


		try {
			int sect = 2;
			SwimTrajectory traj = swimmer1.sectorSwim(sect, -1, xo, yo, zo, p, theta, phi, zTarg, accuracy, 8, 9, stepSize,
					Swimmer.CLAS_Tolerance, hdata);
			
			SwimTest.printSummary("Last for swimmer 1", traj.size(), p, traj.lastElement(), hdata);
			traj.sectorComputeBDL(sect, (RotatedCompositeProbe) swimmer1.getProbe());
			System.out.println("**** BDL for swimmer 1 = " + 100*traj.lastElement()[SwimTrajectory.BXDL_IDX] + "  kG cm");
			System.out.println("**** PATHLENGTH for swimmer 1 = " + 100*traj.lastElement()[SwimTrajectory.PATHLEN_IDX] + "  cm");
			
			double sc[] = tiltedToSector(xo, zo);
			double sf[] =  tiltedToSector(traj.lastElement()[SwimTrajectory.X_IDX], traj.lastElement()[SwimTrajectory.Z_IDX]);
			
			double poz = p*Math.cos(Math.toRadians(theta));
			double pop = p*Math.sin(Math.toRadians(theta));
			double pox = pop*Math.cos(Math.toRadians(phi));
			double poy = pop*Math.sin(Math.toRadians(phi));
			double sp[] = tiltedToSector(pox, poz);
			pox = sp[0];
			poz = sp[1];
			double sTheta = Math.toDegrees(Math.acos(poz/p));
			double sPhi = Math.toDegrees(Math.atan2(poy, pox));
			
			
			System.out.println("START IN SECTOR CS: (" + 100*sc[0] + ", " +  100*yo + ", " + 100*sc[1] + ")  theta = " + sTheta + "  phi = " + sPhi);
			System.out.println("  END IN SECTOR CS: (" + 100*sf[0] + ", " +  100*traj.lastElement()[SwimTrajectory.Y_IDX] + ", " + 100*sf[1] + ")");
			System.out.println();
			
			try {
				SwimZResult szr = swimZ.sectorAdaptiveRK(sect, -1 ,p, szV, zTarg*100, stepSizeCM, hdata);
				
		        SwimTest.printSummary("Last for swimZ", szr.size(), p, theta, szr.last(), hdata);
		        
		        
				System.out.println("**** BDL for swimZ = " + szr.sectorGetBDL(sect, swimZ.getProbe()) + "  kG cm");
				System.out.println("**** PATHLENGTH for swimZ = " + szr.getPathLength() + "  cm");

			} catch (SwimZException e) {
				e.printStackTrace();
			}

		} catch (RungeKuttaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
	
	/**
	 * Convert tilted x and z to sector x and z
	 * @param tiltedX the tilted x coordinate
	 * @param tiltedZ the tilted z coordinate
	 * @return the sector coordinates, with v[0] = x and v[1] = z
	 */
	public static double[] tiltedToSector(double tiltedX, double tiltedZ) {
		double[] v = new double[2];
		v[0] = tiltedX * _cos25 + tiltedZ * _sin25;
		v[1] = tiltedZ * _cos25 - tiltedX * _sin25;
		return v;
	}
	
	/**
	 * Convert sector x and z to tilted x and z
	 * @param sectorX the sector x coordinate
	 * @param sectorZ the sector z coordinate
	 * @return the tilted coordinates, with v[0] = x and v[1] = z
	 */
	public static double[] sectorToTilted(double sectorX, double sectorZ) {
		double[] v = new double[2];
		v[0] = sectorX * _cos25 - sectorZ * _sin25;
		v[1] = sectorZ * _cos25 + sectorX * _sin25;
		return v;
	}




	/**
	 * Compare swimmer  vs swimmer 2
	 */
	public static void swimmerVswimmer2Test(long seed, int num) {
		
		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITE);
		double zTarg = 5.0; //m
		double zTargCM = zTarg*100; //cm
		double accuracy = 1.0e-5; //m
		double stepSize = 0.01; //m
		double stepSizeCM = stepSize*100; //cm


		Random rand = new Random(seed);
		
		int charge[] = new int[num];
		double pTot[] = new double[num];
		double theta[] = new double[num];
		double phi[] = new double[num];
		double x0[] = new double[num];
		double y0[] = new double[num];
		double z0[] = new double[num];
		
		SwimZStateVector szV[] = new SwimZStateVector[num];
		SwimZResult szTraj[] = new SwimZResult[num];
		
		double hdata[] = new double[3];
		
		SwimTrajectory traj1[] = new SwimTrajectory[num];
		SwimTrajectory traj2[] = new SwimTrajectory[num];
		
		Swimmer swimmer1 = new Swimmer();
		Swimmer2 swimmer2 = new Swimmer2();
		SwimZ swimZ = new SwimZ();
		
		for (int i = 0; i < num; i++) {
			charge[i] = ((rand.nextFloat() < 0.5f) ? -1 : 1);
			pTot[i] = SwimTest.randVal(1., 2., rand);
			theta[i] = SwimTest.randVal(15, 25, rand);
			phi[i] = SwimTest.randVal(-10, 10, rand);
			x0[i] = SwimTest.randVal(-2, 2, rand)/100;
			y0[i] = SwimTest.randVal(-2, 2, rand)/100;
			z0[i] = SwimTest.randVal(-2, 2, rand)/100;
			
			if (i == num-1) {
				charge[i] = -1;
				pTot[i] = 2;
				theta[i] = 15;
				phi[i] = 0;
				x0[i] = 0;
				y0[i] = 0;
				z0[i] = 0;
			}

						
			//swimZ uses CM
			szV[i] = new SwimZStateVector(x0[i]*100, y0[i]*100, z0[i]*100, pTot[i], theta[i], phi[i]);
		}
		
		//prime the pump
		for (int i = 0; i < num/2; i++) {
			try {
				traj1[i] = swimmer1.swim(charge[i], x0[i], y0[i], z0[i], pTot[i], theta[i], phi[i], zTarg, accuracy, 10, 10, stepSize,
						Swimmer.CLAS_Tolerance, hdata);
				traj2[i] = swimmer2.swim(charge[i], x0[i], y0[i], z0[i], pTot[i], theta[i], phi[i], zTarg, accuracy, 10, stepSize,
						Swimmer.CLAS_Tolerance, hdata);
			} catch (RungeKuttaException e) {
				e.printStackTrace();
			}
		}
		
		SwimTrajectory lastTraj = null;
		long time;
		time = System.currentTimeMillis();
	
//		System.out.println("Swimmer 1 UNIFORM");
//		time = System.currentTimeMillis();
//		for (int i = 0; i < num; i++) {
//			try {
//				traj1[i] = swimmer1.swim(charge[i], x0[i], y0[i], z0[i], pTot[i], theta[i], phi[i], zTarg, accuracy, 10, 10, stepSize,
//						0.05);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		time = System.currentTimeMillis() - time;
//		System.out.println("Swimmer 1 UNIFORM time: " + timeString(time, num) + "  max step size = " + hdata[2] + "  numStep: " + (traj1[num-1].size()));
// 
//		lastTraj = traj1[num-1];
//		SwimTest.printSummary("Last for swimmer 1 UNIFORM", lastTraj.size(), pTot[num-1], lastTraj.lastElement(), hdata);
//		lastTraj.computeBDL(swimmer1.getProbe());
//		System.out.println("**** BDL for swimmer 1 UNIFORM = " + 100*lastTraj.lastElement()[SwimTrajectory.BXDL_IDX] + "  kG cm");
//		System.out.println("**** PATHLENGTH for swimmer 1 UNIFORM = " + 100*lastTraj.lastElement()[SwimTrajectory.PATHLEN_IDX] + "  cm");


		
//		public SwimTrajectory swim(int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
//				final double fixedZ, final double accuracy, double maxRad, double maxPathLength, double stepSize,
//				double distanceBetweenSaves) {

		System.out.println("Swimmer 1 ADAPTIVE");
		time = System.currentTimeMillis();
		for (int i = 0; i < num; i++) {
			try {
				traj1[i] = swimmer1.swim(charge[i], x0[i], y0[i], z0[i], pTot[i], theta[i], phi[i], zTarg, accuracy, 10, 10, stepSize,
						Swimmer.CLAS_Tolerance, hdata);
			} catch (RungeKuttaException e) {
				e.printStackTrace();
			}
		}
		time = System.currentTimeMillis() - time;
		System.out.println("Swimmer 1 time: " + timeString(time, num) + "  max step size = " + hdata[2] + "  numStep: " + (traj1[num-1].size()));
 
		lastTraj = traj1[num-1];
		SwimTest.printSummary("Last for swimmer 1", lastTraj.size(), pTot[num-1], lastTraj.lastElement(), hdata);
		lastTraj.computeBDL(swimmer1.getProbe());
		System.out.println("**** BDL for swimmer 1 = " + 100*lastTraj.lastElement()[SwimTrajectory.BXDL_IDX] + "  kG cm");
		System.out.println("**** PATHLENGTH for swimmer 1 = " + 100*lastTraj.lastElement()[SwimTrajectory.PATHLEN_IDX] + "  cm");

		
		System.out.println("Swimmer 2 ADAPTIVE");
		time = System.currentTimeMillis();
		for (int i = 0; i < num; i++) {
			try {
				traj2[i] = swimmer2.swim(charge[i], x0[i], y0[i], z0[i], pTot[i], theta[i], phi[i], 
						zTarg, accuracy, 10, stepSize,
						Swimmer.CLAS_Tolerance, hdata);
			} catch (RungeKuttaException e) {
				e.printStackTrace();
			}
		}
		time = System.currentTimeMillis() - time;
		System.out.println("Swimmer 2 time: " + timeString(time, num) + "  max step size = " + hdata[2] + "  numStep: " + (traj2[num-1].size()));
		lastTraj = traj2[num-1];
        SwimTest.printSummary("Last for swimmer 2", lastTraj.size(), pTot[num-1], lastTraj.lastElement(), hdata);
		lastTraj.computeBDL(swimmer2.getProbe());
		System.out.println("**** BDL for swimmer 2 = " + 100*lastTraj.lastElement()[SwimTrajectory.BXDL_IDX] + "  kG cm");
		System.out.println("**** PATHLENGTH for swimmer 2 = " + 100*lastTraj.lastElement()[SwimTrajectory.PATHLEN_IDX] + "  cm");


		int maxDiffIndex = -1;
		double maxDiff = -1;
		
		System.out.println("Computing Swimmer1-Swimmer2 diffs");
		for (int i = 0; i < num; i++) {
			double diff = SwimTest.locDiff(traj1[i].lastElement(), traj2[i].lastElement());
			if (diff > maxDiff) {
				maxDiff = diff;
				maxDiffIndex = i;
			}
		}
		
		System.out.println("Max Swimmer1-Swimmer2 difference at index " + maxDiffIndex + " = " + maxDiff);
		
		//SWIMMER 2 NO TRAJ
		System.out.println("\nSwimmer 2 No Traj");
		time = System.currentTimeMillis();
		
		int numStep = 0;
		double finalV[] = new double[6];
		for (int i = 0; i < num; i++) {
			try {
				numStep = swimmer2.swim(charge[i], x0[i], y0[i], z0[i], pTot[i], 
						theta[i], phi[i], zTarg, accuracy, 10, stepSize,
						RungeKutta.DEFMAXSTEPSIZE,
						Swimmer.CLAS_Tolerance, hdata, finalV);
			} catch (RungeKuttaException e) {
				e.printStackTrace();
			}
		}
		time = System.currentTimeMillis() - time;
		System.out.println("Swimmer 2 no traj time " + timeString(time, num) + "  max step size = " + hdata[2] + "  numStep: " + numStep);
        SwimTest.printSummary("Last for swimmer 2 no traj", numStep, pTot[num-1], finalV, hdata);

		
		System.out.println("\nSwimZ");
		time = System.currentTimeMillis();
		
		for (int i = 0; i < num; i++) {
			try {
				szTraj[i] = swimZ.adaptiveRK(charge[i],pTot[i], szV[i], zTargCM, stepSizeCM, hdata);
			} catch (SwimZException e) {
				e.printStackTrace();
			}
		}
		time = System.currentTimeMillis() - time;
		SwimZResult lastSZR =  szTraj[num-1];
		System.out.println("SwimZ time: " + timeString(time, num) + "  max step size = " + hdata[2]/100. + "  numStep: " + (lastSZR.size()));
        SwimTest.printSummary("Last for swimZ", lastSZR.size(), pTot[num-1], theta[num-1], lastSZR.last(), hdata);
        
       
		System.out.println("**** BDL for swimZ = " + lastSZR.getBDL(swimZ.getProbe()) + "  kG cm");
		System.out.println("**** PATHLENGTH for swimZ = " + lastSZR.getPathLength() + "  cm");


//		SwimTest.printSwimZ(szTraj[num-1].last(), "Last for swimZ");
		
		maxDiffIndex = -1;
		maxDiff = -1;
		
		System.out.println("Computing Swimmer1-SwimZ diffs");
		for (int i = 0; i < num; i++) {
			double diff = SwimTest.locDiff(traj1[i].lastElement(), szTraj[i].last());
			if (diff > maxDiff) {
				maxDiff = diff;
				maxDiffIndex = i;
			}
		}
		
		System.out.println("Max Swimmer1-SwimZ difference at index " + maxDiffIndex + " = " + maxDiff);		
		
		System.out.println("\nSwimZ no traj");
		time = System.currentTimeMillis();
		
		SwimZStateVector stopSV = new SwimZStateVector();
		for (int i = 0; i < num; i++) {
			try {
				numStep = swimZ.adaptiveRK(charge[i],pTot[i], szV[i], stopSV, zTargCM, stepSizeCM, hdata);
			} catch (SwimZException e) {
				e.printStackTrace();
			}
		}
		time = System.currentTimeMillis() - time;
		System.out.println("SwimZ no traj time: " + timeString(time, num) + "  max step size = " + hdata[2]/100. + "  numStep: " + (szTraj[num-1].size()));
        SwimTest.printSummary("Last for swimZ no traj", numStep, pTot[num-1], theta[num-1], stopSV, hdata);
//		SwimTest.printSwimZ(stopSV, "Last for swimZ no traj");
		
        
        //SWIM BACKWARDS TEST
        
        System.out.println("\n\nSwimmer 1 BACKWARDS");
        int qB = 1;  //have to switch charge sign to swim bw
        double xB = 0.921036;
        double yB = 0;
        double zB = 5.000006;
        double thetaB = 177.893937;
        double phiB = 0;
        double zfB = 0;
        double pB = 2;
		try {
			lastTraj = swimmer1.swim(qB, xB, yB, zB, pB, thetaB, phiB, zfB, accuracy, 10, 10, stepSize,
					Swimmer.CLAS_Tolerance, hdata);
			
	        SwimTest.printSummary("End of Traj for Swimmer 1 Backwards", lastTraj.size(), pB, lastTraj.lastElement(), hdata);
			lastTraj.computeBDL(swimmer2.getProbe());
			System.out.println("**** BDL for swimmer 1 Backwards = " + 100*lastTraj.lastElement()[SwimTrajectory.BXDL_IDX] + "  kG cm");
			System.out.println("**** PATHLENGTH for swimmer 1 backwards = " + 100*lastTraj.lastElement()[SwimTrajectory.PATHLEN_IDX] + "  cm");

		} catch (RungeKuttaException e) {
			e.printStackTrace();
		}
		
        System.out.println("\n\nSwimZ BACKWARDS");
        SwimZStateVector szVB = new SwimZStateVector(xB*100, yB*100, zB*100, pB, thetaB, phiB);

        try {
			lastSZR = swimZ.adaptiveRK(qB, pB, szVB, zfB*100, stepSizeCM, hdata);
	        SwimTest.printSummary("Last for swimZ Backwards", lastSZR.size(), pB, thetaB, lastSZR.last(), hdata);
	        
	       
			System.out.println("**** BDL for swimZ Backwards = " + lastSZR.getBDL(swimZ.getProbe()) + "  kG cm");
			System.out.println("**** PATHLENGTH for swimZ Backwards = " + lastSZR.getPathLength() + "  cm");
		} catch (SwimZException e) {
			e.printStackTrace();
		}

		
	}
	
	private static String timeString(long time, int num) {
		return String.format("%-7.4f ms ", (1.*time)/num);
	}

}
