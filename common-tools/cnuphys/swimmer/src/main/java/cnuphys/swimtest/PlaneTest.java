package cnuphys.swimtest;

import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.RotatedCompositeProbe;
import cnuphys.magfield.MagneticFields.FieldType;
import cnuphys.rk4.RungeKuttaException;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.Swimmer;
import cnuphys.swim.Swimming;
import cnuphys.swim.util.Plane;
import cnuphys.swimZ.SwimZ;
import cnuphys.swimZ.SwimZException;
import cnuphys.swimZ.SwimZResult;
import cnuphys.swimZ.SwimZStateVector;

public class PlaneTest {

	/**
	 * Test swimming to a plane
	 */
	public static void planeTest() {
		System.out.println("TEST swimming to a plane");
		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITE);

		double xo = 0; // m
		double yo = 0; // m
		double zo = 0; // m
		double p = 2; // GeV
		// double theta = 15;
		double phi = 0;

		// create the plane
		// double normX = 0.312250;
		// double normY = 0;
		// double normZ = .95;
		// double d = 1.75; //m

		// Plane plane = Plane.createPlane(normX, normY, normZ, d);

		double distToPlane = 3.521; // m
		double distToPlaneCM = 100. * distToPlane; // cm
		Plane plane = Plane.createTiltedPlane(distToPlane);

		Swimmer swimmer1 = new Swimmer();
		SwimZ swimZ = new SwimZ(MagneticFields.getInstance().getRotatedCompositeField());

		double accuracy = 1.0e-5; // m
		double stepSize = 0.01; // m
		double stepSizeCM = 100. * stepSize; // cm
		double hdata[] = new double[3];

		try {

			for (int iTheta = 12; iTheta < 30; iTheta++) {
				// public SwimTrajectory swim(int charge, double xo, double yo,
				// double zo, double momentum, double theta, double phi,
				// Plane plane, double accuracy, double sMax, double stepSize,
				// double relTolerance[], double hdata[])

				SwimTrajectory traj = swimmer1.swim(-1, xo, yo, zo, p, iTheta, phi, plane, accuracy, 9, stepSize,
						Swimmer.CLAS_Tolerance, hdata);

				double lastY[] = traj.lastElement();

				SwimTest.printSummary("Last for swimmer 1", traj.size(), p, lastY, hdata);
				traj.computeBDL(swimmer1.getProbe());
				System.out.println(
						"**** BDL for swimmer 1 = " + 100 * traj.lastElement()[SwimTrajectory.BXDL_IDX] + "  kG cm");
				System.out.println("**** PATHLENGTH for swimmer 1 = "
						+ 100 * traj.lastElement()[SwimTrajectory.PATHLEN_IDX] + "  cm");

				System.out.println("Distance from plane: " + plane.distanceToPlane(lastY[0], lastY[1], lastY[2]));

				Swimming.addMCTrajectory(traj);

			} // end of loop

			System.out.println("\n\n\n** Compare to SwimZ");
			System.out.println("\nSwim to Plane");

			double theta = 20;
			SwimTrajectory traj = swimmer1.swim(-1, xo, yo, zo, p, theta, phi, plane, accuracy, 9, stepSize,
					Swimmer.CLAS_Tolerance, hdata);

			double lastY[] = traj.lastElement();
			System.out.println("Distance from plane: " + plane.distanceToPlane(lastY[0], lastY[1], lastY[2]));


			traj.computeBDL(swimmer1.getProbe());
			SwimTest.printSummary("Last for swimmer 1", traj.size(), p, lastY, hdata);
			System.out.println(
					"**** BDL for swimmer 1 = " + 100 * traj.lastElement()[SwimTrajectory.BXDL_IDX] + "  kG cm");
			System.out.println(
					"**** PATHLENGTH for swimmer 1 = " + 100 * traj.lastElement()[SwimTrajectory.PATHLEN_IDX] + "  cm");

			System.out.println("\nSwimZ");
			
			double pperp = p*Math.sin(Math.toRadians(theta));
			double px = pperp*Math.cos(Math.toRadians(phi));
			double py = pperp*Math.sin(Math.toRadians(phi));
			double pz = p*Math.cos(Math.toRadians(theta));
			double tp[] = CompareSwimmers.sectorToTilted(px, pz);
			
			double tpx = tp[0];
			double tpz = tp[1];
			
			double ptheta = Math.toDegrees(Math.acos(tpz/p));
			double pphi = Math.toDegrees(Math.atan2(py, tpx));
			
//			pphi= 0;
//			ptheta = -5;
//			System.out.println("PTHETA = " + ptheta);
//			System.out.println("PPHI = " + pphi);

			// swimZ uses CM
			SwimZStateVector szV = new SwimZStateVector(xo * 100, yo * 100, zo * 100, p, ptheta, pphi);
			SwimZResult szr = swimZ.sectorAdaptiveRK(1, -1, p, szV, distToPlaneCM, stepSizeCM, hdata);
			
		
			SwimZStateVector last = szr.last();
			SwimTest.printSummary("Last for swimZ", szr.size(), p, theta, last, hdata);
			
			double sr[] = CompareSwimmers.tiltedToSector(last.x, last.z);
			System.out.println("R in sector coordinates (" + sr[0]/100 + ", " + last.y/100 + ", " + sr[1]/100 + ") m");

			System.out.println("**** BDL for swimZ = " + szr.sectorGetBDL(1, swimZ.getProbe()) + "  kG cm");
			System.out.println("**** PATHLENGTH for swimZ = " + szr.getPathLength() + "  cm");
			
			System.out.println("\n\nTIMING ");
			int N = 100;
			long time = System.currentTimeMillis();
			for (int i = 0; i < 1000; i++) {
				traj = swimmer1.swim(-1, xo, yo, zo, p, theta, phi, plane, accuracy, 9, stepSize,
						Swimmer.CLAS_Tolerance, hdata);
			}
			System.out.println("Swim to plane: " + (System.currentTimeMillis()-time));
			time = System.currentTimeMillis();
			for (int i = 0; i < 1000; i++) {
				szr = swimZ.sectorAdaptiveRK(1, -1, p, szV, distToPlaneCM, stepSizeCM, hdata);
			}
			System.out.println("SwimZ: " + (System.currentTimeMillis()-time));


		} catch (RungeKuttaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SwimZException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	


}