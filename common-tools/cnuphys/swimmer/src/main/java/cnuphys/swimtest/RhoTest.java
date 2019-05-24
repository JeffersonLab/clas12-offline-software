package cnuphys.swimtest;

import java.util.Random;

import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.MagneticFields.FieldType;
import cnuphys.rk4.RungeKuttaException;
import cnuphys.swim.SwimResult;
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
		double accuracy = 5e-4; //m
		double fixedRho = 0.30;  //m 
		
		int num = 100;
		
		SwimResult uniform = new SwimResult(6);
		SwimResult adaptive = new SwimResult(6);
		
		//generate some random initial conditions
		Random rand = new Random();
		
//		double p[] = new double[num];
//		double theta[] = new double[num];
		
		

		Swimmer swimmer = new Swimmer();
		int ns;
		
		//adaptive step
		try {
			swimmer.swimRho(charge, xo, yo, zo, p, theta, phi, fixedRho, accuracy, maxPathLength, stepsize, Swimmer.CLAS_Tolerance, adaptive);
			SwimTest.printSummary("Fixed Rho,  Adaptive step size", adaptive.getNStep(), p, adaptive.getUf(), null);
			System.out.println("Adaptive Path length = " + adaptive.getFinalS() + " m\n\n");
		} catch (RungeKuttaException e) {
			e.printStackTrace();
		}

		
		//uniform step
		swimmer.swimRho(charge, xo, yo, zo, p, theta, phi, fixedRho, accuracy, maxPathLength, stepsize, uniform);
		
		SwimTest.printSummary("Fixed Rho,  Fixed step size", uniform.getNStep(), p, uniform.getUf(), null);
		System.out.println("Uniform Path length = " + uniform.getFinalS() + " m\n\n");
		
		
		
	}

}
