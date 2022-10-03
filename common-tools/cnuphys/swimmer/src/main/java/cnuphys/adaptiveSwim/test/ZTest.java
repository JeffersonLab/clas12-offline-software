package cnuphys.adaptiveSwim.test;

import java.io.File;
import java.util.Random;

import cnuphys.adaptiveSwim.AdaptiveSwimException;
import cnuphys.adaptiveSwim.AdaptiveSwimResult;
import cnuphys.adaptiveSwim.AdaptiveSwimmer;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.MagneticFields.FieldType;
import cnuphys.rk4.RungeKuttaException;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.Swimmer;
import cnuphys.swimtest.CSVWriter;
import cnuphys.swimtest.RandomData;
import cnuphys.swimtest.SwimTest;

public class ZTest {

	

	//swim to a fixed z
	public static void swimZTest(int n, long seed) {
		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITE);
	    System.err.println("Swim to fixed z test");
	    
	    //for writing results to CSV
		String homeDir = System.getProperty("user.home");
		File file = new File(homeDir, "testResults/swimZ.csv");

	    CSVWriter writer = new CSVWriter(file);
	    writer.writeRow("Swim to Fized Z");
	    writer.newLine();
	    
	    //write the header row
	    writer.writeRow("charge", "zTarg", "xo (m)", "yo (m)", "zo (m)", "p (GeV/c)", "theta (deg)", "phi (deg)", 
	//    		"xf_old", "yf_old", "zf_old", "dZ_old", 
	    		"status", "xf_new", "yf_new", "zf_new", "dZ_new");
	    
	    
		Swimmer swimmer = new Swimmer(); //old
		AdaptiveSwimmer adaptiveSwimmer = new AdaptiveSwimmer(); //new
		
		//results for old and new
		AdaptiveSwimResult result = new AdaptiveSwimResult(true);
		

		double stepsizeAdaptive = 0.01; // starting

		double maxPathLength = 10; // m
		double accuracy = 1e-5; // m
		double eps = 1.0e-6;
		double hdata[] = new double[3];
		
		//generate some random data			
		RandomData data = new RandomData(n, seed);

		//random target z im meters
	    double zTarget[] = new double[n];
	    Random rand = new Random(seed);
	    for (int i= 0; i < n; i++) {
	    	zTarget[i] = 2.5 + 3.5*rand.nextDouble();
	    }
	    
		for (int i = 0; i < n; i++) {
			
			int charge = data.charge[i];
			double xo = data.xo[i];
			double yo = data.yo[i];
			double zo = data.zo[i];
			double p = data.p[i];
			double theta = data.theta[i];
			double phi = data.phi[i];
			double zTarg = zTarget[i];
			
			result.reset();

			writer.writeStartOfRow(charge, 100*zTarg, 100 * xo, 100 * yo, 100 * zo, p, theta, phi);
			
			// interp OLD
			try {
				SwimTrajectory traj = swimmer.swim(charge, xo, yo, zo, p, theta, phi, zTarg, accuracy, maxPathLength, stepsizeAdaptive,
						Swimmer.CLAS_Tolerance, hdata);
				
				swimZSwimResult(writer, zTarg, traj);


			} catch (RungeKuttaException e) {
				System.err.println("OLD Swimmer Failed." + "  final pathlength = " + result.getFinalS());
				e.printStackTrace();
			}

			result.reset();
			
			// NEW
			try {
				adaptiveSwimmer.swimZ(charge, xo, yo, zo, p, 
						theta, phi, zTarg, accuracy,
						maxPathLength, stepsizeAdaptive, eps, result);
				
				swimZSwimResult(writer, zTarg, result);

			} catch (AdaptiveSwimException e) {
				System.err.println("NEW Swimmer Failed." + "  final pathlength = " + result.getS());
				e.printStackTrace();
			}

			
			
			writer.newLine();

		}
		
	    
		writer.close();
		System.err.println("done with main test. Now timing test.");

		//timing test
		long threadId = Thread.currentThread().getId();
		long oldTime;
		long newTime;
		
		long start = SwimTest.cpuTime(threadId);
		
		for (int i = 0; i < n; i++) {
			
			int charge = data.charge[i];
			double xo = data.xo[i];
			double yo = data.yo[i];
			double zo = data.zo[i];
			double p = data.p[i];
			double theta = data.theta[i];
			double phi = data.phi[i];
			double zTarg = zTarget[i];
			
			result.reset();
		
			// NEW
			try {
				adaptiveSwimmer.swimZ(charge, xo, yo, zo, p, 
						theta, phi, zTarg, accuracy,
						maxPathLength, stepsizeAdaptive, eps, result);
				
			} catch (AdaptiveSwimException e) {
				System.err.println("NEW Swimmer Failed." + "  final pathlength = " + result.getS());
				e.printStackTrace();
			}
		}
		
		newTime = SwimTest.cpuTime(threadId) - start;
		
		start = SwimTest.cpuTime(threadId);

		for (int i = 0; i < n; i++) {
			
			int charge = data.charge[i];
			double xo = data.xo[i];
			double yo = data.yo[i];
			double zo = data.zo[i];
			double p = data.p[i];
			double theta = data.theta[i];
			double phi = data.phi[i];
			double zTarg = zTarget[i];
			
			
			// interp OLD
			try {

				SwimTrajectory traj = swimmer.swim(charge, xo, yo, zo, p, theta, phi, zTarg, accuracy, maxPathLength, stepsizeAdaptive,
						Swimmer.CLAS_Tolerance, hdata);

			} catch (RungeKuttaException e) {
				System.err.println("OLD Swimmer Failed." + "  final pathlength = " + result.getS());
				e.printStackTrace();
			}
			
		}
		
		oldTime = SwimTest.cpuTime(threadId) - start;
		
		


		
		System.err.println("old time: " + oldTime);
		System.err.println("new time: " + newTime);
		System.err.println("ratio old/new = " + (double)oldTime/(double)newTime);
		System.err.println("done");

	}
		
	
	
	//swimZ results
	private static void swimZSwimResult(CSVWriter writer, double targetZ, AdaptiveSwimResult result) {
		double NaN = Double.NaN;

		writer.writeStartOfRow(result.statusString()); 

		//uf is NOT the intersection
		if (result.getStatus() == AdaptiveSwimmer.SWIM_SUCCESS) {
			double[] uf = result.getU();
			double dist = Math.abs(uf[2] - targetZ);
			writer.writeStartOfRow(100*uf[0], 100*uf[1], 100*uf[2], 100*dist);


		} else {
			writer.writeStartOfRow(NaN, NaN, NaN, NaN);
		}
	}
	
	//swimZ results
	private static void swimZSwimResult(CSVWriter writer, double targetZ, SwimTrajectory traj) {
		
		double uf[] = traj.lastElement();
		double dist = Math.abs(uf[2] - targetZ);
		writer.writeStartOfRow(100*uf[0], 100*uf[1], 100*uf[2], 100*dist);
	}


}
