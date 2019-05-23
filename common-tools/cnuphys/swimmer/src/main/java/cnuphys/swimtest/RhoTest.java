package cnuphys.swimtest;

import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.MagneticFields.FieldType;
import cnuphys.swim.Swimmer;

public class RhoTest {

	/** Test swimming to a fixed rho (cylinder) */
	public static void rhoTest() {
		System.out.println("TEST swimming to a fixed rho (cylinder)");
		MagneticFields.getInstance().setActiveField(FieldType.SOLENOID);
		
		int charge = 1;
		double xo = 0; // m
		double yo = 0; // m
		double zo = 0; // m
		double p = 2; // GeV
		double theta = 60; //deg
		double phi = 0; //deg
		double stepsize = 0.5e-03;  //m
		
		double maxPathLength = 2; //m
		double accuracy = 5e-3; //m
		double fixedRho = 0.24;  //m 

		Swimmer swimmer = new Swimmer();
		
		double[] finalY = swimmer.swimRho(charge, xo, yo, zo, p, theta, phi, fixedRho, accuracy, maxPathLength, stepsize);
		
		SwimTest.printSummary("Fixed Rho,  Fixed step size", -1, p, finalY, null);
		System.err.println("Hey man");
	}

}
