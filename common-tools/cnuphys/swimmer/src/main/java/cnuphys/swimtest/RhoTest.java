package cnuphys.swimtest;

import java.util.Random;

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
		
		int num = 100;
		
		double uf[] = new double[6];
		
		//generate some random initial conditions
		Random rand = new Random();
		
//		double p[] = new double[num];
//		double theta[] = new double[num];
		
		

		Swimmer swimmer = new Swimmer();
		
		int ns = swimmer.swimRho(charge, xo, yo, zo, p, theta, phi, fixedRho, accuracy, maxPathLength, stepsize, uf);
		
		SwimTest.printSummary("Fixed Rho,  Fixed step size", ns, p, uf, null);
	}

}
