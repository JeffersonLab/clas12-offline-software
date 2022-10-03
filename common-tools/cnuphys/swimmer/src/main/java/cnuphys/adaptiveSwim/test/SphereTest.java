package cnuphys.adaptiveSwim.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import cnuphys.adaptiveSwim.AdaptiveSwimException;
import cnuphys.adaptiveSwim.AdaptiveSwimResult;
import cnuphys.adaptiveSwim.AdaptiveSwimmer;
import cnuphys.lund.AsciiReader;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.MagneticFields.FieldType;
import cnuphys.rk4.RungeKuttaException;
import cnuphys.swim.DefaultSphereStopper;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.Swimmer;
import cnuphys.swimtest.CSVWriter;
import cnuphys.swimtest.SwimTest;

public class SphereTest {
	
	public static void sphereTest() {
		//get data from csv data file
		SphereTestData testData[] = readDataFile();
		int n = testData.length;

		
		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITE);
	    System.err.println("Swim to sphere test");
	    
	    //for writing results to CSV
		String homeDir = System.getProperty("user.home");
		File file = new File(homeDir, "testResults/swimSphere.csv");
		
	    CSVWriter writer = new CSVWriter(file);
	    writer.writeRow("Swim to sphere");
	    writer.newLine();
	    
	    //write the header row
	    writer.writeRow("charge", "xo (m)", "yo (m)", "zo (m)", "p (GeV/c)", "theta (deg)", "phi (deg)", 
	    		"start", "status", "xf_old", "yf_old", "zf_old", "s_old", "bdl_old", "d_old",
	    		"status", "xf_new", "yf_new", "zf_new", "s_new", "bdl_new", "d_new");

		Swimmer swimmer = new Swimmer(); //old
		AdaptiveSwimmer adaptiveSwimmer = new AdaptiveSwimmer(); //new
		
		//results for old and new
		AdaptiveSwimResult result = new AdaptiveSwimResult(true);
		

		double eps = 1.0e-6;
		
		for (SphereTestData data : testData) {
			
			int charge = data.charge;
			double xo = data.xo;
			double yo = data.yo;
			double zo = data.zo;
			double p = data.p;
			double theta = data.theta;
			double phi = data.phi;
			
			double rsq = data.r * data.r;
			boolean inside = xo*xo + yo*yo + zo*zo < rsq;
			
			writer.writeStartOfRow(charge, 100*xo, 100*yo, 100*zo, p, theta, phi);
			writer.writeStartOfRow(inside ? "inside" : "outside");
			
	        int dir = (inside) ? 1 : -1;
			
			DefaultSphereStopper stopper = new DefaultSphereStopper(data.r, dir);
			SwimTrajectory traj = swimmer.swim(charge, xo, yo, zo, p, theta, phi, stopper, data.sMax, data.stepSize,
			        0.0005);
			
	        if(traj!=null) {
	        	traj.computeBDL(swimmer.getProbe());
	        }
	        else {
	        	System.out.println("Bad sphere swim");
	        }
	        swimCylinderSwimResult(writer, data, traj);

			result.reset();
			
			// NEW
			try {
				adaptiveSwimmer.swimSphere(charge, xo, yo, zo, p, theta, phi,
						data.r,
						data.accuracy, data.sMax, data.stepSize, eps, result);
				
				result.getTrajectory().computeBDL(adaptiveSwimmer.getProbe());

				swimSphereSwimResult(writer, data, result);

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

		for (SphereTestData data : testData) {
			
			int charge = data.charge;
			double xo = data.xo;
			double yo = data.yo;
			double zo = data.zo;
			double p = data.p;
			double theta = data.theta;
			double phi = data.phi;
			
			double rsq = data.r * data.r;
			boolean inside = xo*xo + yo*yo + zo*zo < rsq;
	        int dir = (inside) ? 1 : -1;
						
			DefaultSphereStopper stopper = new DefaultSphereStopper(data.r, dir);
			SwimTrajectory traj = swimmer.swim(charge, xo, yo, zo, p, theta, phi, stopper, data.sMax, data.stepSize,
			        0.0005);
			
	        if(traj!=null) {
	        	traj.computeBDL(swimmer.getProbe());
	        }
	        else {
	        	System.out.println("Bad sphere swim");
	        }
		}
		
		oldTime = SwimTest.cpuTime(threadId) - start;
		
		start = SwimTest.cpuTime(threadId);


		for (SphereTestData data : testData) {

			result.reset();
			
			// NEW
			try {
				
				int charge = data.charge;
				double xo = data.xo;
				double yo = data.yo;
				double zo = data.zo;
				double p = data.p;
				double theta = data.theta;
				double phi = data.phi;
				
				adaptiveSwimmer.swimSphere(charge, xo, yo, zo, p, theta, phi,
						data.r,
						data.accuracy, data.sMax, data.stepSize, eps, result);
				
				result.getTrajectory().computeBDL(adaptiveSwimmer.getProbe());

			} catch (AdaptiveSwimException e) {
				System.err.println("NEW Swimmer Failed." + "  final pathlength = " + result.getS());
				e.printStackTrace();
			}


		}
		
		newTime = SwimTest.cpuTime(threadId) - start;

		System.err.println("old time: " + oldTime);
		System.err.println("new time: " + newTime);
		System.err.println("ratio old/new = " + (double) oldTime / (double) newTime);

		System.err.println("done");

	}
	
	//swimCylinder results
	private static void swimCylinderSwimResult(CSVWriter writer, SphereTestData data, SwimTrajectory traj) {
		double NaN = Double.NaN;

		writer.writeStartOfRow((traj == null) ? "FAIL" : "Traj_NP=" + traj.size());

//	    writer.writeRow("charge", "xo (m)", "yo (m)", "zo (m)", "p (GeV/c)", "theta (deg)", "phi (deg)", 
//	    		"status", "xf_old", "yf_old", "zf_old", "s_old", "bdl_old", "d_old");

		if (traj != null) {
			double[] uf = traj.lastElement();
			double dist = data.sphere.signedDistance(uf[0], uf[1], uf[2]);
			writer.writeStartOfRow(100 * uf[0], 100 * uf[1], 100 * uf[2], 100 * uf[6], uf[7],
					Math.abs(dist));

		} else {
			writer.writeStartOfRow(NaN, NaN, NaN, NaN, NaN, NaN);
		}
	}
	
	//swimCylinder results
	private static void swimSphereSwimResult(CSVWriter writer, SphereTestData data, AdaptiveSwimResult result) {
		double NaN = Double.NaN;

		writer.writeStartOfRow(result.statusString()); 

		//uf is NOT the intersection
		if (result.getStatus() == AdaptiveSwimmer.SWIM_SUCCESS) {
			double[] uf = result.getU();
			double dist = data.sphere.signedDistance(uf[0], uf[1], uf[2]);
			
			double bdl = result.getTrajectory().getComputedBDL();
			writer.writeStartOfRow(100*uf[0], 100*uf[1], 100*uf[2], 100*result.getS(), bdl, Math.abs(dist));


		} else {
			writer.writeStartOfRow(NaN, NaN, NaN, NaN, NaN, NaN);
		}
	}

	
	
	//rear a csv data file
	private static SphereTestData[] readDataFile() {
		
		ArrayList<SphereTestData> data = new ArrayList<>();
		File file = new File("./cnuphys/adaptiveSwim/test/spheredata.csv");
		if (file.exists()) {
			System.out.println("Found sphere data file");
			try {
				AsciiReader reader = new AsciiReader(file) {

					@Override
					protected void processLine(String line) {
						data.add(new SphereTestData(line));
					}

					@Override
					public void done() {
						System.out.println("Done reading data file.");
					}
					
				};
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		else {
			System.out.println("Did not find sphere data file [" + file.getAbsolutePath() + "]");
		}
		
		SphereTestData array[] = new SphereTestData[data.size()];
		return data.toArray(array);
	}

}
