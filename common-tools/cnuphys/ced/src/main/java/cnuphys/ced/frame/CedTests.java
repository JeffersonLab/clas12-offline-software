package cnuphys.ced.frame;

import java.util.Random;

import cnuphys.bCNU.component.TextAreaWriter;
import cnuphys.bCNU.dialog.TextDisplayDialog;
import cnuphys.bCNU.util.UnicodeSupport;
import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.MagneticField;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.TorusMap;
import cnuphys.rk4.RungeKuttaException;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.Swimmer;

public class CedTests {

	
	protected static void swimTest(boolean probeCache) {
		
		FieldProbe.cache(false);
		
		TextDisplayDialog dialog = new TextDisplayDialog("Test Results");
		dialog.setVisible(true);
		TextAreaWriter writer = dialog.getWriter();
		
		int n = 1000;
		int micronAccuracy = 50;
        double accuracy = micronAccuracy/ 1.0e6;  //micons converted to meters

        writer.writeln("Some test results for the swimmer.");
		writer.writeln("Test: Fixed Z swimming, 1 GeV electron, Random " +
		UnicodeSupport.SMALL_PHI + " (0-360), " + UnicodeSupport.SMALL_THETA + " (20-40)");
		writer.writeln("Compare final |" + UnicodeSupport.CAPITAL_DELTA + UnicodeSupport.SMALL_RHO + "| for different full maps compared to symmetric map.");
		writer.writeln("Final Z accuraccy: " + micronAccuracy + " microns");
		writer.writeln("Number of swims: " + n);
		writer.writeln("-------------------------------------------------");

		
		double xo = 0;  //meters
		double yo = 0;
		double zo = 0;
		double rMax = 7;  //max radial coordinate meters
		double momentum = 1;  //GeV/c
		double stepSize = 5e-3; // m
		double maxPathLen = 8.0; // m
		double hdata[] = new double[3];
		double ztarget = 5.0; //meters
		int charge = -1;  //electron
		SwimTrajectory traj = null;

		

		//get the random values
		double phi[] = new double[n];
		double theta[] = new double[n];
		
		//test results
		double xf[] = new double[n];
		double yf[] = new double[n];
		double zf[] = new double[n];
		
		double Q[][] = new double[n][6];
		double Qf[] = new double[7];
		
		
		//stay away from coils
		long seed = 2343453249L;
		Random rand = new Random(seed);
		for (int i = 0; i < n; i++) {
			
			double base = 60*rand.nextInt(6);
			phi[i] = -30 + base + (5 + 50*rand.nextDouble());
			while(phi[i] < 360) {
				phi[i] += 360;
			}
			while(phi[i] > 360) {
				phi[i] -= 360;
			}

			theta[i] = 20.0 + 20.0*rand.nextDouble();
		}
		
		//set to the symmetric map
		MagneticFields.getInstance().setTorus(TorusMap.SYMMETRIC);
		Swimmer swimmer = new Swimmer(MagneticFields.getInstance().getActiveField());
		
		long time = System.nanoTime();

		for (int i = 0; i < n; i++) {
			try {
				traj = swimmer.swim(charge, xo, yo, zo,
						momentum, theta[i], phi[i], ztarget, accuracy,
						rMax, maxPathLen, stepSize,
						Swimmer.CLAS_Tolerance, hdata);
			} catch (RungeKuttaException e) {
				e.printStackTrace();
				return;
			}
			
			System.arraycopy(traj.lastElement(), 0, Q[i], 0, 6);
		}
		timeReport(writer, "Priming pump time", time, System.nanoTime());
		
//		for (int i = 0; i < 5; i++){
//			cleanTrajPoint(momentum, Q[i], Qf);
//			printVect(writer, momentum, Qf);
//		}
		
		//should produce perfect results
		compareField(writer, Q, TorusMap.SYMMETRIC, charge, xo, yo, zo,
				momentum, theta, phi, ztarget, accuracy,
				rMax, maxPathLen, stepSize,
				hdata);
		
		compareField(writer, Q, TorusMap.FULL_200, charge, xo, yo, zo,
				momentum, theta, phi, ztarget, accuracy,
				rMax, maxPathLen, stepSize,
				hdata);

		compareField(writer, Q, TorusMap.FULL_150, charge, xo, yo, zo,
				momentum, theta, phi, ztarget, accuracy,
				rMax, maxPathLen, stepSize,
				hdata);
	
		compareField(writer, Q, TorusMap.FULL_125, charge, xo, yo, zo,
				momentum, theta, phi, ztarget, accuracy,
				rMax, maxPathLen, stepSize,
				hdata);
		
		compareField(writer, Q, TorusMap.FULL_100, charge, xo, yo, zo,
				momentum, theta, phi, ztarget, accuracy,
				rMax, maxPathLen, stepSize,
				hdata);
		
		compareField(writer, Q, TorusMap.FULL_075, charge, xo, yo, zo,
				momentum, theta, phi, ztarget, accuracy,
				rMax, maxPathLen, stepSize,
				hdata);

		compareField(writer, Q, TorusMap.FULL_050, charge, xo, yo, zo,
				momentum, theta, phi, ztarget, accuracy,
				rMax, maxPathLen, stepSize,
				hdata);
		
		compareField(writer, Q, TorusMap.FULL_025, charge, xo, yo, zo,
				momentum, theta, phi, ztarget, accuracy,
				rMax, maxPathLen, stepSize,
				hdata);
		
		FieldProbe.cache(true);

	}
	
	private static void compareField(TextAreaWriter writer, double[][] results, TorusMap tmap, int charge, double xo, double yo, double zo, 
			double momentum, double theta[], double phi[], double ztarget, double accuracy, double rMax,
			double maxPathLen, double stepSize, double hdata[]) {
		MagneticFields.getInstance().setTorus(tmap);
		Swimmer swimmer = new Swimmer(MagneticFields.getInstance().getActiveField());
		SwimTrajectory traj = null;
		
		int n = theta.length;
		double Q[][] = new double[n][6];
		
		double resultQf[] = new double[7];
		double Qf[] = new double[7];


		writer.writeln("\nResults for torus map: " + tmap.getName());
		
		
		//once to prime the pump
		for (int i = 0; i < n; i++) {
			try {
				traj = swimmer.swim(charge, xo, yo, zo,
						momentum, theta[i], phi[i], ztarget, accuracy,
						rMax, maxPathLen, stepSize,
						Swimmer.CLAS_Tolerance, hdata);
			} catch (RungeKuttaException e) {
				e.printStackTrace();
				return;
			}
		}
		
		
		long time = System.nanoTime();

		for (int i = 0; i < n; i++) {
			try {
				traj = swimmer.swim(charge, xo, yo, zo,
						momentum, theta[i], phi[i], ztarget, accuracy,
						rMax, maxPathLen, stepSize,
						Swimmer.CLAS_Tolerance, hdata);
			} catch (RungeKuttaException e) {
				e.printStackTrace();
				return;
			}
			
			System.arraycopy(traj.lastElement(), 0, Q[i], 0, 6);
		}
		timeReport(writer, "Swim time", time, System.nanoTime());

//		for (int i = 0; i < 5; i++){
//			cleanTrajPoint(momentum, Q[i], Qf);
//			printVect(writer, momentum, Qf);
//		}
		
		double rhosum = 0;
		double zsum = 0;
		double rhomax = -1;
		double zmax = -1;
		int worstIndex = 0;
		
		for (int i = 0;i < n; i++) {
			cleanTrajPoint(momentum, results[i], resultQf);
			cleanTrajPoint(momentum, Q[i], Qf);
			double rhodiff = Math.abs(resultQf[3]-Qf[3]);
			double zdiff= Math.abs(resultQf[2]-Qf[2]);
			
			if (rhodiff > rhomax) {
				worstIndex = i;
				rhomax = rhodiff;
			}
			
			zmax = Math.max(zmax,  zdiff);
			
			rhosum += rhodiff;
			zsum += zdiff;
		}
		
		double rhoavg = rhosum/n;
		double zavg = zsum/n;
		
		writer.writeln(String.format("Avg |z diff| = %-10.5f   Max |z diff| =  %-10.5f cm", zavg, zmax));
		writer.writeln(String.format("Avg |" + UnicodeSupport.SMALL_RHO + " diff| = %-10.5f   Max |" + UnicodeSupport.SMALL_RHO + " diff| =  %-10.5f cm", rhoavg, rhomax));
		writer.writeln(String.format("Worst case: INDEX = %d   theta = %-8.3f  phi = %-8.3f", worstIndex, theta[worstIndex], phi[worstIndex]));

		
//		cleanTrajPoint(momentum, results[worstIndex], resultQf);
//		cleanTrajPoint(momentum, Q[worstIndex], Qf);
//		
//		writer.writeln("WORST CASE RHO DIFFERENCE"); 
//		writer.writeln("SYMMETRIC RESULT");
//		printVect(writer, momentum, resultQf);
//		writer.writeln(tmap.getName() + " RESULT");
//		printVect(writer, momentum, Qf);

	}
	
	private static void timeReport(TextAreaWriter writer, String message, long startNano, long stopNano) {
		long del = stopNano - startNano;
		
		if (del > 1000000000L) {
			double sec = del/1.0e9;
			writer.writeln(String.format("[%s] %-8.3f sec", message, sec));
		}
		else if (del > 1000000L) {
			double millisec = del/1.0e6;
			writer.writeln(String.format("[%s] %-8.3f millis", message, millisec));
		}
		else {
			double microsec = del/1.0e3;
			writer.writeln(String.format("[%s] %-8.3f " + UnicodeSupport.SMALL_MU+"s", message, microsec));
		}

	}
	
	private static void cleanTrajPoint(double momentum, double Q[], double Qf[]) {
		
		//Qf[0] = x in cm
		//Qf[1] = y in cm
		//Qf[2] = z in cm
		//Qf[3] = rho in cm
		//Qf[4] = norm (should be one)
		//Qf[5] = theta in deg
		//Qf[6] = phi in deg
		
		Qf[0] = Q[0]*100;
		Qf[1] = Q[1]*100;
		Qf[2] = Q[2]*100;
		Qf[3] = Math.hypot(Qf[0], Qf[1]);
		
		
		double norm = Math.sqrt(Q[3] * Q[3] + Q[4] * Q[4] + Q[5] * Q[5]);
		double px = Q[3];
		double py = Q[4];
		double pz = Q[5];
		
		Qf[4] = norm;
		
		Qf[5] = MagneticField.acos2Deg(pz);
		Qf[6] = MagneticField.atan2Deg(py, px);
		
	}
	
	
	
	private static void printVect(TextAreaWriter writer, double P, double Qf[]) {
		
		//Qf[0] = x in cm
		//Qf[1] = y in cm
		//Qf[2] = z in cm
		//Qf[3] = rho in cm
		//Qf[4] = norm should be 1
		//Qf[5] = theta in deg
		//Qf[6] = phi in deg
	
		
		writer.write(String.format("R = [%-9.4f, %-9.4f, %-9.4f]  ", Qf[0], Qf[1], Qf[2]));
		writer.write(String.format(UnicodeSupport.SMALL_RHO + " = %9.4f  ", Qf[3]));
		writer.write(String.format("norm = %-8.4f  ", Qf[4]));
		writer.write(String.format(UnicodeSupport.SMALL_THETA + " = %-8.3f  ", Qf[5]));
		writer.writeln(String.format(UnicodeSupport.SMALL_PHI + " = %-8.3f  ", Qf[6]));
	}
}
