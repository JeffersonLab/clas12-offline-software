package cnuphys.swimtest;

import java.util.Random;

import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.RotatedCompositeProbe;
import cnuphys.magfield.MagneticFields.FieldType;
import cnuphys.rk4.RungeKuttaException;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.Swimmer;
import cnuphys.swimZ.SwimZ;
import cnuphys.swimZ.SwimZException;
import cnuphys.swimZ.SwimZResult;
import cnuphys.swimZ.SwimZStateVector;

public class SectorTest {

	
	//test the sector swimmer for rotated composite
	public static void testSectorSwim(int num) {

		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITEROTATED);
//		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITE);

		double hdata[] = new double[3];
		
		int charge = -1;
		
//		double x0 = (-40. + 20*Math.random())/100.;
//		double y0 = (10. + 40.*Math.random())/100.;
//		double z0 = (180 + 40*Math.random())/100.;
		
		
		double x0 = -80/100.; //m
		double y0 = 0;
		double z0 = 300/100.; //m
		
		
		
		double pTot = 1.0;
		double theta = 0;
		double phi = 0;
	//	double z = 511.0/100.;
		double z = z0 + 0.56;
		double accuracy = 10/1.0e6;
		double stepSize = 0.01;
		
		System.out.println("=======");
		Swimmer swimmer = new Swimmer();
		SwimZ sz = new SwimZ();

		swimmer.getProbe().getField().printConfiguration(System.out);
		
		for (int sector = 1; sector <= 6; sector ++) {
			
			SwimTrajectory traj;
			try {
				traj = swimmer.sectorSwim(sector, charge, x0, y0, z0, pTot,
				            theta, phi, z, accuracy, 10,
				            10, stepSize, Swimmer.CLAS_Tolerance, hdata);
				
				if (traj == null) {
					System.err.println("Null trajectory in Sector Test");
					System.exit(1);;
				}
				
				
				FieldProbe probe = swimmer.getProbe();
				if (probe instanceof RotatedCompositeProbe) {
					traj.sectorComputeBDL(sector, (RotatedCompositeProbe)probe);
	//				System.out.println("BDL = " + traj.getComputedBDL() + " kG-m");
				} else {
					traj.computeBDL(probe);
				}
				
	            
	            double lastY[] = traj.lastElement();
				System.out.print("Sector: " + sector + "  ");
				SwimTest.printVect(lastY, " last ");
			} catch (RungeKuttaException e) {
				e.printStackTrace();
			}
		}

		if (true) return;

		System.out.println("\nSwim v. SwimZ");
		for (int sector = 1; sector <= 6; sector ++) {
			
			SwimTrajectory traj;
			try {
				traj = swimmer.sectorSwim(sector, charge, x0, y0, z0, pTot,
				            theta, phi, z, accuracy, 10,
				            10, stepSize, Swimmer.CLAS_Tolerance, hdata);
				
				
//				FieldProbe probe = swimmer.getProbe();
//				if (probe instanceof RotatedCompositeProbe) {
//					traj.sectorComputeBDL(sector, (RotatedCompositeProbe)probe);
//				} else {
//					traj.computeBDL(probe);
//				}
	            
	            double lastY[] = traj.lastElement();
				System.out.print("****    Sector: " + sector + "  ");
		        SwimTest.printSummary("Last for Swimmer", traj.size(), pTot, lastY, hdata);
				
				//swim same with swimZ
				SwimZStateVector start = new SwimZStateVector(x0*100, y0*100, z0*100, pTot, theta, phi);
				SwimZStateVector stop = new SwimZStateVector(x0*100, y0*100, z0*100, pTot, theta, phi);

				SwimZResult szr = null;
				try {
					szr = sz.sectorAdaptiveRK(sector, charge, pTot, start, 100*z, 100*stepSize, hdata);
				} catch (SwimZException e) {
					e.printStackTrace();
				}

			    SwimTest.printSummary("Last for swimZ", szr.size(), pTot, theta, szr.last(), hdata);


				
			} catch (RungeKuttaException e) {
				e.printStackTrace();
			}
		}

		
		if (true) return;
			System.out.println("\nSTRESS TEST. Will swim " + num + " random trajectories in rotated system");
			
			Random rand = new Random();
			
			long time = System.currentTimeMillis();
			for (int i = 0; i < num; i++) {
				int sect = rand.nextInt(6) + 1;
				if ((sect < 1) || (sect > 6)) {
					System.out.println("bad sector " + sect);
					System.exit(1);
				}
				
				double rho = 0.1 + 2*rand.nextDouble(); //meters
				z0 = 0.5 + 4*rand.nextDouble(); //meters
				z = 0.5 + 4*rand.nextDouble(); //meters
				pTot = 1. + 2*rand.nextDouble();
				theta = -10 + 20*rand.nextDouble();
				phi = -10 + 20*rand.nextDouble();
				
				double phiLoc = Math.toRadians(30.*rand.nextDouble());
				
				x0 = rho*Math.cos(phiLoc);
				y0 = rho*Math.sin(phiLoc);
				
				try {
					SwimTrajectory traj = swimmer.sectorSwim(sect, charge, x0, y0, z0, pTot,
					        theta, phi, z, accuracy,
					        10, stepSize, Swimmer.CLAS_Tolerance, hdata);
				} catch (RungeKuttaException e) {
					e.printStackTrace();
				}


			}
			
			time = System.currentTimeMillis()-time;
			System.out.println("DONE avg swim time = " + (((double)time)/num) + " ms");
			

	}
}
