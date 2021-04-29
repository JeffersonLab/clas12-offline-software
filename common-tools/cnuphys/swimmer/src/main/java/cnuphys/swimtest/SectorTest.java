package cnuphys.swimtest;

import java.util.Random;

import cnuphys.adaptiveSwim.AdaptiveSwimException;
import cnuphys.adaptiveSwim.AdaptiveSwimResult;
import cnuphys.adaptiveSwim.AdaptiveSwimmer;
import cnuphys.magfield.FastMath;
import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.RotatedCompositeProbe;
import cnuphys.magfield.MagneticFields.FieldType;
import cnuphys.rk4.RungeKuttaException;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.Swimmer;

public class SectorTest {

	// test the sector swimmer for rotated composite
	public static void testSectorSwim(int num) {

		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITEROTATED);

		double hdata[] = new double[3];

		int charge = -1;

//		double x0 = (-40. + 20*Math.random())/100.;
//		double y0 = (10. + 40.*Math.random())/100.;
//		double z0 = (180 + 40*Math.random())/100.;

		double x0 = -80 / 100.; // m
		double y0 = 0;
		double z0 = 300 / 100.; // m

		double pTot = 1.0;
		double theta = 0;
		double phi = 0;
		// double z = 511.0/100.;
		double z = z0 + 0.56;
		double accuracy = 10 / 1.0e6;
		double stepSize = 0.01;

		System.out.println("=======");
		
		
		//compare old swimmer and adaptive
		AdaptiveSwimmer adaptiveSwimmer = new AdaptiveSwimmer();
		Swimmer swimmer = new Swimmer(MagneticFields.getInstance().getRotatedCompositeField());
		

		swimmer.getProbe().getField().printConfiguration(System.out);

		System.out.println("\n******  OLD Swimmer ");
		for (int sector = 1; sector <= 6; sector++) {

			SwimTrajectory traj;
			try {
				traj = swimmer.sectorSwim(sector, charge, x0, y0, z0, pTot, theta, phi, z, accuracy, 10, 10, stepSize,
						Swimmer.CLAS_Tolerance, hdata);

				if (traj == null) {
					System.err.println("Null trajectory in Sector Test");
					System.exit(1);
				}

				FieldProbe probe = swimmer.getProbe();
				traj.sectorComputeBDL(sector, (RotatedCompositeProbe) probe);
				System.out.println("BDL = " + traj.getComputedBDL() + " kG-m");

				double lastY[] = traj.lastElement();
				System.out.print("Sector: " + sector + "  ");
				SwimTest.printVect(lastY, " last ");
			} catch (RungeKuttaException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("\n******  NEW ADAPTIVE Swimmer ");
		
		AdaptiveSwimResult swimResult = new AdaptiveSwimResult(true);
		stepSize = 0.01;  //initial ss = 1 cm
		double eps = 1.0e-6;
		
		for (int sector = 1; sector <= 6; sector++) {
			try {
				adaptiveSwimmer.sectorSwimZ(sector, charge, x0, y0, z0, pTot, theta, phi, z, accuracy, 10, stepSize, eps, swimResult);
				SwimTrajectory traj = swimResult.getTrajectory();
				
				FieldProbe probe = adaptiveSwimmer.getProbe();
				
				traj.sectorComputeBDL(sector, (RotatedCompositeProbe) probe);
				System.out.println("BDL = " + traj.getComputedBDL() + " kG-m");

				double lastY[] = swimResult.getLastTrajectoryPoint();
				System.out.print("Sector: " + sector + "  ");
				SwimTest.printVect(lastY, " last ");
				System.out.println("Final s: " + swimResult.getFinalS());
			}
			catch (AdaptiveSwimException e) {
				e.printStackTrace();
			}
		}

		
		System.out.println("\nSwim backwards test");
		
		
		
		System.out.println("\nSwim backwards test (OLD)");
		
		try {
			
			x0 = 0;
			y0 = 0;
			z0 = 0;
			pTot = 2;
			theta = 15;
			phi = 5;
			z = 5.75;

			
			SwimTrajectory traj = swimmer.sectorSwim(1, charge, x0, y0, z0, pTot, theta, phi, z, accuracy, 10, 10, stepSize,
					Swimmer.CLAS_Tolerance, hdata);

			if (traj == null) {
				System.err.println("Null trajectory in Sector Test");
				System.exit(1);
			}

			double lastY[] = traj.lastElement();
			SwimTest.printVect(lastY, " last  (OLD, FORWARD) ");
			
			z = z0;
			x0 = lastY[0];
			y0 = lastY[1];
			z0 = lastY[2];
			
			double txf = -lastY[3];
			double tyf = -lastY[4];
			double tzf = -lastY[5];
			
			
			theta = FastMath.acos2Deg(tzf);
			phi = FastMath.atan2Deg(tyf, txf);
			
			traj = swimmer.sectorSwim(1, -charge, x0, y0, z0, pTot, theta, phi, 0, accuracy, 10, 10, stepSize,
					Swimmer.CLAS_Tolerance, hdata);
			
			lastY = traj.lastElement();
			SwimTest.printVect(lastY, " last  (OLD, BACKWARD) ");
						
			
		} catch (RungeKuttaException e) {
			e.printStackTrace();
		}

		
		System.out.println("\nSwim backwards test (NEW");
		
		try {
			
			x0 = 0;
			y0 = 0;
			z0 = 0;
			pTot = 2;
			theta = 15;
			phi = 5;
			z = 5.75;

			adaptiveSwimmer.sectorSwimZ(1, charge, x0, y0, z0, pTot, theta, phi, z, accuracy, 10, stepSize, eps, swimResult);
			double lastY[] = swimResult.getLastTrajectoryPoint();
			SwimTest.printVect(lastY, " last (NEW, FORWARD) ");
			
			
			z = z0;
			x0 = lastY[0];
			y0 = lastY[1];
			z0 = lastY[2];
			
			double txf = -lastY[3];
			double tyf = -lastY[4];
			double tzf = -lastY[5];
			
			
			theta = FastMath.acos2Deg(tzf);
			phi = FastMath.atan2Deg(tyf, txf);

			adaptiveSwimmer.sectorSwimZ(1, -charge, x0, y0, z0, pTot, theta, phi, z, accuracy, 10, stepSize, eps, swimResult);
			lastY = swimResult.getLastTrajectoryPoint();
			SwimTest.printVect(lastY, " last (NEW, BACKWARD) ");

		}
		catch (AdaptiveSwimException e) {
			e.printStackTrace();
		}
	

		if (true)
			return;
//
//		System.out.println("\nSwim v. SwimZ");
//		for (int sector = 1; sector <= 6; sector++) {
//
//			SwimTrajectory traj;
//			try {
//				traj = swimmer.sectorSwim(sector, charge, x0, y0, z0, pTot, theta, phi, z, accuracy, 10, 10, stepSize,
//						Swimmer.CLAS_Tolerance, hdata);
//
////				FieldProbe probe = swimmer.getProbe();
////				if (probe instanceof RotatedCompositeProbe) {
////					traj.sectorComputeBDL(sector, (RotatedCompositeProbe)probe);
////				} else {
////					traj.computeBDL(probe);
////				}
//
//				double lastY[] = traj.lastElement();
//				System.out.print("****    Sector: " + sector + "  ");
//				SwimTest.printSummary("Last for Swimmer", traj.size(), pTot, lastY, hdata);
//
//				// swim same with swimZ
//				SwimZStateVector start = new SwimZStateVector(x0 * 100, y0 * 100, z0 * 100, pTot, theta, phi);
//				SwimZStateVector stop = new SwimZStateVector(x0 * 100, y0 * 100, z0 * 100, pTot, theta, phi);
//
//				SwimZResult szr = null;
//				try {
//					szr = sz.sectorAdaptiveRK(sector, charge, pTot, start, 100 * z, 100 * stepSize, hdata);
//				} catch (SwimZException e) {
//					e.printStackTrace();
//				}
//
//				SwimTest.printSummary("Last for swimZ", szr.size(), pTot, theta, szr.last(), hdata);
//
//			} catch (RungeKuttaException e) {
//				e.printStackTrace();
//			}
//		}
//
//		if (true)
//			return;
		System.out.println("\nSTRESS TEST. Will swim " + num + " random trajectories in rotated system");

		Random rand = new Random(88779911);
		long time = System.currentTimeMillis();
		
		double aX0[] = new double[num];
		double aY0[] = new double[num];
		double aZ0[] = new double[num];
		double aZ[] = new double[num];
		double aTheta[] = new double[num];
		double aPhi[] = new double[num];
		double aP[] = new double[num];
		int aSect[] = new int[num];
		
		for (int i = 0; i < num; i++) {
			aSect[i] = rand.nextInt(6) + 1;
			double rho = 0.1 + 2 * rand.nextDouble(); // meters
			
			aZ0[i] = 0.5 + 4 * rand.nextDouble(); // meters
			aZ[i] = 0.5 + 4 * rand.nextDouble(); // meters
			aP[i] = 1. + 2 * rand.nextDouble();
			aTheta[i] = -10 + 20 * rand.nextDouble();
			aPhi[i] = -10 + 20 * rand.nextDouble();
			double phiLoc = Math.toRadians(30. * rand.nextDouble());
			aX0[i] = rho * Math.cos(phiLoc);
			aY0[i] = rho * Math.sin(phiLoc);
		}
		
		for (int i = 0; i < num; i++) {

			try {
				SwimTrajectory traj = swimmer.sectorSwim(aSect[i], charge, aX0[i], y0, aZ0[i], aP[i],
						aTheta[i], aPhi[i], aZ[i], accuracy, 10,
						stepSize, Swimmer.CLAS_Tolerance, hdata);
			} catch (RungeKuttaException e) {
				e.printStackTrace();
			}

		}

		time = System.currentTimeMillis() - time;
		System.out.println("DONE avg swim time OLD SWIMMER = " + (((double) time) / num) + " ms");
		
		

		time = System.currentTimeMillis();
		for (int i = 0; i < num; i++) {

			try {
				adaptiveSwimmer.sectorSwimZ(aSect[i], charge, aX0[i], y0, aZ0[i], aP[i],
						aTheta[i], aPhi[i], aZ[i], accuracy, 10, stepSize, eps, swimResult);
			} catch (AdaptiveSwimException e) {
				e.printStackTrace();
			}

		}

		time = System.currentTimeMillis() - time;
		System.out.println("DONE avg swim time NEW ADPSWIMMER = " + (((double) time) / num) + " ms");
		
		System.out.println("\nLooking for biggest difference");
		
		int worstIndex = -1;
		double maxDiff = -1;
		double sum = 0;
		for (int i = 0; i < num; i++) {
			try {
				SwimTrajectory traj = swimmer.sectorSwim(aSect[i], charge, aX0[i], y0, aZ0[i], aP[i],
						aTheta[i], aPhi[i], aZ[i], accuracy, 10,
						stepSize, Swimmer.CLAS_Tolerance, hdata);
				
				adaptiveSwimmer.sectorSwimZ(aSect[i], charge, aX0[i], y0, aZ0[i], aP[i],
						aTheta[i], aPhi[i], aZ[i], accuracy, 10, stepSize, eps, swimResult);

				double diff = trajDiff(traj, swimResult.getTrajectory());
				sum += diff;
				
				if (diff > maxDiff) {
					maxDiff = diff;
					worstIndex = i;
				}
				
			} catch (RungeKuttaException e) {
				e.printStackTrace();
			} catch (AdaptiveSwimException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Max diff: " + maxDiff);
		System.out.println("Avg diff: " + sum/num);
		System.out.println("Worst case diff:");
		
		int i = worstIndex;
		try {
			
			System.out.println("\nWorst case last for OLD: ");
			SwimTrajectory traj = swimmer.sectorSwim(aSect[i], charge, aX0[i], y0, aZ0[i], aP[i],
					aTheta[i], aPhi[i], aZ[i], accuracy, 10,
					stepSize, Swimmer.CLAS_Tolerance, hdata);
			
			
			FieldProbe probe = adaptiveSwimmer.getProbe();
			
			
			traj.sectorComputeBDL(aSect[i], (RotatedCompositeProbe) probe);
			System.out.println("BDL = " + traj.getComputedBDL() + " kG-m");

			double lastY[] = traj.lastElement();
			System.out.print("Sector: " + aSect[i] + "  ");
			SwimTest.printVect(lastY, " last ");

			System.out.println("\nWorst case last for NEW: ");
			adaptiveSwimmer.sectorSwimZ(aSect[i], charge, aX0[i], y0, aZ0[i], aP[i],
					aTheta[i], aPhi[i], aZ[i], accuracy, 10, stepSize, eps, swimResult);
			
			traj = swimResult.getTrajectory();
			traj.sectorComputeBDL(aSect[i], (RotatedCompositeProbe) probe);
			System.out.println("BDL = " + traj.getComputedBDL() + " kG-m");

			lastY = traj.lastElement();
			System.out.print("Sector: " + aSect[i] + "  ");
			SwimTest.printVect(lastY, " last ");

			
		} catch (RungeKuttaException e) {
			e.printStackTrace();
		} catch (AdaptiveSwimException e) {
			e.printStackTrace();
		}

		
		
	}
	
	private static double trajDiff(SwimTrajectory traj1, SwimTrajectory traj2) {
		double lastY1[] = traj1.lastElement();
		double lastY2[] = traj2.lastElement();
		
		double diff = 0;
		
		for (int i = 0; i < 6; i++) {
			double del = lastY2[i] - lastY1[i];
			diff += (del*del);
		}
		
		return Math.sqrt(diff);
	}
}
