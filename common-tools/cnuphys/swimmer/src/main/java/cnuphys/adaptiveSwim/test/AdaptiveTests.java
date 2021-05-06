package cnuphys.adaptiveSwim.test;

import java.util.Random;

import cnuphys.adaptiveSwim.AdaptiveSwimException;
import cnuphys.adaptiveSwim.AdaptiveSwimResult;
import cnuphys.adaptiveSwim.AdaptiveSwimmer;
import cnuphys.adaptiveSwim.geometry.Cylinder;
import cnuphys.adaptiveSwim.geometry.Line;
import cnuphys.adaptiveSwim.geometry.Plane;
import cnuphys.adaptiveSwim.geometry.Point;
import cnuphys.adaptiveSwim.geometry.Vector;
import cnuphys.magfield.FastMath;
import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.MagneticFields.FieldType;
import cnuphys.rk4.RungeKuttaException;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.Swimmer;

public class AdaptiveTests {
	
	/** Test the basic swim to a final pathlength */
	public static void noStopperTest() {

		// test basic pathlength swimmer to be used by ced

		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITE);
		AdaptiveSwimmer adaptiveSwimmer = new AdaptiveSwimmer();

		double stepsizeAdaptive = 0.01; // starting
		double xo = 0;
		double yo = 0;
		double zo = 0;
		int Q = 1;
		double maxPathLength = 5.;
		double theta = 15;
		double phi = 0;
		double p = 2;
		double eps = 1.0e-6;

		AdaptiveSwimResult result = new AdaptiveSwimResult(true);

		try {
			adaptiveSwimmer.swim(Q, xo, yo, zo, p, theta, phi, maxPathLength, stepsizeAdaptive, eps, result);
			result.printOut(System.out, "Base S Swimmer");
		} catch (AdaptiveSwimException e) {
			e.printStackTrace();
		}

	}
	

	/** Test the swim to a line */
	public static void lineTest() {
		Line targetLine = new Line(new Point(1, 0, 0), new Point(1, 0, 1));
		
		MagneticFields.getInstance().setActiveField(FieldType.TORUS);
		AdaptiveSwimmer adaptiveSwimmer = new AdaptiveSwimmer();

		double stepsizeAdaptive = 0.01; // starting
		double xo = 0;
		double yo = 0;
		double zo = 0;
		int Q = 1;
		double maxPathLength = 8.;
		double theta = 25;
		double phi = 0;
		double p = 1;
		double eps = 1.0e-6;
		double accuracy = 1.0e-5; //m

		AdaptiveSwimResult result = new AdaptiveSwimResult(true);

		try {
			adaptiveSwimmer.swimLine(Q, xo, yo, zo, p, theta, phi, targetLine, accuracy, maxPathLength, stepsizeAdaptive, eps, result);
			result.printOut(System.out, "Line Test");
		} catch (AdaptiveSwimException e) {
			e.printStackTrace();
		}

	}


	/** Test retracing a swim */
	public static void retraceTest() {
		
		long seed = 9459363;
		Random rand = new Random(seed);
		int num = 10000;
//		num = 1;
		int n0 = 0;

		int status[] = new int[num];

		InitialValues[] ivals = InitialValues.getInitialValues(rand, num, -1, false, 0., 0., 0., 0., 0., 0., 0.5, 5.0, 10., 25., -30., 30.);

		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITE);
		AdaptiveSwimmer adaptiveSwimmer = new AdaptiveSwimmer();
		AdaptiveSwimResult result = new AdaptiveSwimResult(true);

		double stepsizeAdaptive = 0.01; // starting

		double maxPathLength = 8; // m
		double accuracy = 1e-5; // m
		double eps = 1.0e-6;


		// create a plane last layer reg 3
		double r = 5.3092; // m
		double x1 = r * Math.sin(Math.toRadians(25));
		double y1 = 0;
		double z1 = r * Math.cos(Math.toRadians(25));
		Point p1 = new Point(x1, y1, z1);
		Vector v = new Vector(-x1, 0, -z1);
		Plane plane = new Plane(v, p1);
		
		int goodCount = 0;

		try {
			
			InitialValues iv = null;
			double[] uf = null;
			
			double sum = 0;
			double drMax = 0;
			int iMax = -1;
			
			for (int i = n0; i < num; i++) {
				iv = ivals[i];
				result.setInitialValies(iv);
				
				adaptiveSwimmer.swimPlane(iv.charge, iv.xo, iv.yo, iv.zo, iv.p, iv.theta, iv.phi, plane, accuracy,
						maxPathLength, stepsizeAdaptive, eps, result);
				
				
				uf = result.getUf();
				status[i] = result.getStatus();
				if (status[i] == AdaptiveSwimmer.SWIM_SUCCESS) {
					goodCount++;
					//try to swim back
					
					InitialValues revIv = result.retrace();
				
					
//					adaptiveSwimmer.swimZ(revIv.charge, revIv.xo, revIv.yo, revIv.zo, revIv.p, revIv.theta, revIv.phi, zTarg, accuracy, maxPathLength, stepsizeAdaptive, eps, result);
//					adaptiveSwimmer.swim(revIv.charge, revIv.xo, revIv.yo, revIv.zo, revIv.p, revIv.theta, revIv.phi, result.getFinalS(), stepsizeAdaptive, eps, result);
					adaptiveSwimmer.swimS(revIv.charge, revIv.xo, revIv.yo, revIv.zo, revIv.p, revIv.theta, revIv.phi, accuracy, result.getFinalS(), stepsizeAdaptive, eps, result);

					double dr = FastMath.sqrt(uf[0]*uf[0] + uf[1]*uf[1] + uf[2]*uf[2]);
					
					if (i == 711) {
						System.out.println("BACKWARD " + revIv);
						result.printOut(System.out, "Retrace swim", true);
						System.out.println("dr = " + dr);
					}

					sum += dr;
					
					if (dr > drMax) {
						drMax = dr;
						iMax = i;
					}
				}
				else {
					// System.out.println("Bad swim to plane for i = " + i + "  final pathlength = " + result.getFinalS());
				}
			}  //for

			System.out.println("average dr = " + (sum/goodCount) +   "     max dr: " + drMax + "   at i = " + iMax );

		} catch (AdaptiveSwimException e) {
			e.printStackTrace();
		}		
		
		
	
		
	}
	
	//ztest for time
	public static void TIMEzTest() {
		long seed = 9479365;
		Random rand = new Random(seed);
		int num = 100000;
		int n0 = 0;
		
		//the initial values
		InitialValues[] ivals = InitialValues.getInitialValues(rand, num, 1, true, 
				-0.05, -0.05, -0.05,  //vertex mins
				0.05, 0.05, 0.05,  //vertex max
				5, 8.0,      //momentum range
				20., 35.,    //theta range
				0., 360.     //phi range
				);

		System.out.println("TEST swimming to a fixed z");
		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITE);

		double maxPathLength = 8; // m
		double accuracy = 5e-3; // m
		double zTarg = 5; // m
		double eps = 1.0e-5;
		long time;

		double stepsizeAdaptive = 0.01; // starting
		
		Swimmer swimmer = new Swimmer();
		AdaptiveSwimmer adaptiveSwimmer = new AdaptiveSwimmer();
		AdaptiveSwimResult oldResult = new AdaptiveSwimResult(false);
		AdaptiveSwimResult newResult = new AdaptiveSwimResult(false);


        double hdata[] = new double[3];
		
		InitialValues iv = null;
		time = System.currentTimeMillis();
		
		SwimTrajectory traj = null;
		
		//old swimmer
		for (int i = n0; i < num; i++) {
			iv = ivals[i];
			
			try {
				traj = swimmer.swim(iv.charge, iv.xo, iv.yo, iv.zo, iv.p, iv.theta, iv.phi, zTarg, accuracy, maxPathLength, stepsizeAdaptive,
						Swimmer.CLAS_Tolerance, hdata);
			} catch (RungeKuttaException e) {
				e.printStackTrace();
			}
            
			//cause this old swimmer does not call init
			oldResult.setInitialValues(iv.charge, iv.xo, iv.yo, iv.zo, iv.p, iv.theta, iv.phi);

		}
		
		traj.computeBDL(swimmer.getProbe());
		oldResult.setTrajectory(traj);
		time = System.currentTimeMillis() - time;
		oldResult.printOut(System.out, "Z test (old)");
		System.out.println(
				String.format("[OLD] Adaptive time: %-7.3f",
						(time) / 1000.));
		
		time = System.currentTimeMillis();
		//new swimmer
		for (int i = n0; i < num; i++) {
			iv = ivals[i];
			
			try {
				adaptiveSwimmer.swimZ(iv.charge, iv.xo, iv.yo, iv.zo, iv.p, iv.theta, iv.phi, zTarg, accuracy,
						maxPathLength, stepsizeAdaptive, eps, newResult);
			} catch (AdaptiveSwimException e) {
				e.printStackTrace();
			}

		}
		
		time = System.currentTimeMillis() - time;
		newResult.printOut(System.out, "Z test (new)");
		System.out.println(
				String.format("[NEW] Adaptive time: %-7.3f",
						(time) / 1000.));

		System.out.println("Done with z-test");
	}


	//test swim to fixed z
	public static void xxxxxxzTest() {
		long seed = 9479365;
		Random rand = new Random(seed);
		int num = 100;
		int n0 = 0;
		

		System.out.println("TEST swimming to a fixed z");
		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITE);

		double maxPathLength = 8; // m
		double accuracy = 5e-3; // m
		double zTarg = 5; // m
		double eps = 1.0e-5;

		double stepsizeAdaptive = 0.01; // starting
		
		Swimmer swimmer = new Swimmer();
		AdaptiveSwimmer adaptiveSwimmer = new AdaptiveSwimmer();
		AdaptiveSwimResult oldResult = new AdaptiveSwimResult(false);
		AdaptiveSwimResult newResult = new AdaptiveSwimResult(false);


        double hdata[] = new double[3];
				
		SwimTrajectory traj = null;
		
		InitialValues iv = new InitialValues();
		
		for (int i = n0; i < num; i++) {
			InitialValues.randomInitVal(rand, iv, 1, true, 
					-0.05, -0.05, -0.05,  //vertex mins
					0.05, 0.05, 0.05,  //vertex max
					5, 8.0,      //momentum range
					20., 35.,    //theta range
					0., 360.     //phi range
					);
			
			//old swimmer
			try {
				traj = swimmer.swim(iv.charge, iv.xo, iv.yo, iv.zo, iv.p, iv.theta, iv.phi, zTarg, accuracy, maxPathLength, stepsizeAdaptive,
						Swimmer.CLAS_Tolerance, hdata);
			} catch (RungeKuttaException e) {
				e.printStackTrace();
			}
            
			//cause this old swimmer does not call init
			oldResult.setInitialValues(iv.charge, iv.xo, iv.yo, iv.zo, iv.p, iv.theta, iv.phi);
			
			//new swimmer
			try {
				adaptiveSwimmer.swimZ(iv.charge, iv.xo, iv.yo, iv.zo, iv.p, iv.theta, iv.phi, zTarg, accuracy,
						maxPathLength, stepsizeAdaptive, eps, newResult);
			} catch (AdaptiveSwimException e) {
				e.printStackTrace();
			}

		}
		
		
		//print last
		traj.computeBDL(swimmer.getProbe());
		oldResult.setTrajectory(traj);
		oldResult.printOut(System.out, "Z test (old)");
		
		
		newResult.printOut(System.out, "Z test (new)");

		System.out.println("Done with z-test");
	}

	/** Test swimming to a fixed rho */
	public static void rhoTest() {

		long seed = 9459363;
		Random rand = new Random(seed);
		int num = 100000;
//		num = 1;
		int n0 = 0;

		InitialValues[] ivals = InitialValues.getInitialValues(rand, num, 1, true, 0., 0., 0., 0., 0., 0., 0.25, 1.0, 40., 70., 0., 360.);

		System.out.println("TEST swimming to a fixed rho");
		MagneticFields.getInstance().setActiveField(FieldType.SOLENOID);

		double stepsizeAdaptive = 0.01; // starting

		double maxPathLength = 3; // m
		double accuracy = 5e-3; // m
		double rhoTarg = 0.30; // m
		double eps = 1.0e-6;

		AdaptiveSwimResult oldResult = new AdaptiveSwimResult(false);
		AdaptiveSwimResult newResult = new AdaptiveSwimResult(false);

		// generate some random initial conditions

		int adaptStatus[] = new int[num];

		long time;
		double rhof;
		double sum;
		double delMax;
		Swimmer swimmer = new Swimmer();
		AdaptiveSwimmer adaptiveSwimmer = new AdaptiveSwimmer();
		int badStatusCount;

		int nsMax = 0;

		long nStepTotal = 0;

//		 adaptive step
		try {

			sum = 0;
			badStatusCount = 0;
			delMax = Double.NEGATIVE_INFINITY;
			time = System.currentTimeMillis();

			InitialValues iv = null;
			for (int i = n0; i < num; i++) {
				iv = ivals[i];
                
				swimmer.swimRho(iv.charge, iv.xo, iv.yo, iv.zo, iv.p, iv.theta, iv.phi, rhoTarg, accuracy,
						maxPathLength, stepsizeAdaptive, Swimmer.CLAS_Tolerance, oldResult);
				
				//cause this old swimmer does not call init
				oldResult.setInitialValues(iv.charge, iv.xo, iv.yo, iv.zo, iv.p, iv.theta, iv.phi);

				rhof = Math.hypot(oldResult.getUf()[0], oldResult.getUf()[1]);
				double dd = Math.abs(rhoTarg - rhof);
				delMax = Math.max(delMax, dd);

				adaptStatus[i] = oldResult.getStatus();
				nStepTotal += oldResult.getNStep();
				

				nsMax = Math.max(nsMax, oldResult.getNStep());

				if (oldResult.getStatus() != AdaptiveSwimmer.SWIM_SUCCESS) {
					badStatusCount += 1;
				}
				else {
					sum += dd;
				}
			}

			time = System.currentTimeMillis() - time;
			oldResult.printOut(System.out, "Rho test (old)");
			System.out.println(
					String.format("Adaptive time: %-7.3f   avg good delta = %-9.5f  max delta = %-9.5f  badStatCnt = %d",
							(time) / 1000., sum / (num - badStatusCount), delMax, badStatusCount));
			System.out.println("Adaptive Avg NS = " + (int) (((double) nStepTotal) / num) + "   MAX NS: " + nsMax + "\n");

		} catch (RungeKuttaException e) {
			e.printStackTrace();
			System.exit(1);
		}

		// NEW adaptive step no traj
		try {
			nsMax = 0;
			sum = 0;
			badStatusCount = 0;
			delMax = Double.NEGATIVE_INFINITY;
			nStepTotal = 0;

			time = System.currentTimeMillis();

			InitialValues iv = null;
			for (int i = n0; i < num; i++) {
				iv = ivals[i];

				adaptiveSwimmer.swimRho(iv.charge, iv.xo, iv.yo, iv.zo, iv.p, iv.theta, iv.phi, rhoTarg, accuracy,
						maxPathLength, stepsizeAdaptive, eps, newResult);

				rhof = Math.hypot(newResult.getUf()[0], newResult.getUf()[1]);
				double dd = Math.abs(rhoTarg - rhof);
				delMax = Math.max(delMax, dd);
				

				if (newResult.getStatus() != adaptStatus[i]) {
					System.out.println("Adaptive v. NEW Adaptive Status differs for i = " + i + "     adaptiveStat = "
							+ adaptStatus[i] + "    NEW adaptive status = " + newResult.getStatus());
				}

				nStepTotal += newResult.getNStep();
				nsMax = Math.max(nsMax, newResult.getNStep());

				if (newResult.getStatus() != AdaptiveSwimmer.SWIM_SUCCESS) {
					badStatusCount += 1;
				}
				else {
					sum += dd;
				}
			}

			time = System.currentTimeMillis() - time;
			newResult.printOut(System.out, "Rho test (new)");
			System.out.println(
					String.format("NEW Adaptive time: %-7.3f   avg good delta = %-9.5f  max delta = %-9.5f  badStatCnt = %d",
							(time) / 1000., sum / (num - badStatusCount), delMax, badStatusCount));
			System.out.println("NEW Adaptive Avg NS = " + (int) (((double) nStepTotal) / num) + "   MAX NS: " + nsMax + "\n");

		} catch (AdaptiveSwimException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Test swim to a plane
	 */
	public static void planeTest() {

		System.out.println("swim to a plane");
		
		long seed = 9459363;
		Random rand = new Random(seed);
		int num = 1000;
//		num = 1;
		int n0 = 0;

		int status[] = new int[num];
		InitialValues[] ivals = InitialValues.getInitialValues(rand, num, -1, false, 0., 0., 0., 0., 0., 0., 0.5, 5.0, 10., 25., -30., 30.);

		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITE);
		AdaptiveSwimmer adaptiveSwimmer = new AdaptiveSwimmer();
		AdaptiveSwimResult result = new AdaptiveSwimResult(true);

		double stepsizeAdaptive = 0.01; // starting

		double maxPathLength = 8; // m
		double accuracy = 1e-4; // m
		double eps = 1.0e-6;


		// create a plane last layer reg 3
		double r = 5.3092; // m
		double x1 = r * Math.sin(Math.toRadians(25));
		double y1 = 0;
		double z1 = r * Math.cos(Math.toRadians(25));
		Point p1 = new Point(x1, y1, z1);
		Vector v = new Vector(-x1, 0, -z1);
		Plane plane = new Plane(v, p1);
		

		try {
			
			InitialValues iv = null;
			double[] uf = null;
			
			for (int i = n0; i < num; i++) {
				iv = ivals[i];

//				if (i == 54) {
//					System.out.println();
//					System.out.println(iv);
//				}
				
				adaptiveSwimmer.swimPlane(iv.charge, iv.xo, iv.yo, iv.zo, iv.p, iv.theta, iv.phi, plane, accuracy,
						maxPathLength, stepsizeAdaptive, eps, result);
				
				uf = result.getUf();
				status[i] = result.getStatus();
				if (status[i] == AdaptiveSwimmer.SWIM_SUCCESS) {
					
				}
				else {
					System.out.println("Bad swim to plane for i = " + i + "  final pathlength = " + result.getFinalS());
				}


			}

			result.printOut(System.out, "Swim to plane");
			System.out.println(String.format("Distance to plane: %-8.6f m" , Math.abs(plane.distance(uf[0], uf[1], uf[2]))));
		} catch (AdaptiveSwimException e) {
			e.printStackTrace();
		}
	}
	
	/** Test swimming to a cylinder */
	public static void sphereTest() {

	}

	/** Test swimming to a cylinder */
	public static void cylinderTest() {

		System.out.println("Cylinder around z axis should give us same result as rho swim.");
		

		long seed = 9459363;
		Random rand = new Random(seed);
		int num = 10000;
	//	num = 1;
		int n0 = 0;

		InitialValues[] ivals = InitialValues.getInitialValues(rand, num, 1, true, 0., 0., 0., 0., 0., 0., 0.25, 1.0, 40., 70., 0., 360.);

		System.out.println("TEST swimming to a fixed rho");
		MagneticFields.getInstance().setActiveField(FieldType.SOLENOID);

		double stepsizeAdaptive = 0.01; // starting

		double maxPathLength = 3; // m
		double accuracy = 5e-3; // m
		double rhoTarg = 0.30; // m
		double eps = 1.0e-6;
		
		Cylinder targCyl = new Cylinder(new Line(new Point(0, 0, 0), new Point(0, 0, 1)), rhoTarg);

		AdaptiveSwimResult rResult = new AdaptiveSwimResult(true);
		AdaptiveSwimResult cResult = new AdaptiveSwimResult(true);

		// generate some random initial conditions

		int adaptStatus[] = new int[num];

		long time;
		double rhof;
		double sum;
		double delMax;
		AdaptiveSwimmer adaptiveSwimmer = new AdaptiveSwimmer();
		int badStatusCount;

		int nsMax = 0;

		long nStepTotal = 0;


		// rho swim
		try {
			nsMax = 0;
			sum = 0;
			badStatusCount = 0;
			delMax = Double.NEGATIVE_INFINITY;
			nStepTotal = 0;

			time = System.currentTimeMillis();

			InitialValues iv = null;
			for (int i = n0; i < num; i++) {
				iv = ivals[i];

				adaptiveSwimmer.swimRho(iv.charge, iv.xo, iv.yo, iv.zo, iv.p, iv.theta, iv.phi, rhoTarg, accuracy,
						maxPathLength, stepsizeAdaptive, eps, rResult);

				rhof = Math.hypot(rResult.getUf()[0], rResult.getUf()[1]);
				double dd = Math.abs(rhoTarg - rhof);
				delMax = Math.max(delMax, dd);
				sum += dd;
				
				adaptStatus[i] = rResult.getStatus();

				nStepTotal += rResult.getNStep();
				nsMax = Math.max(nsMax, rResult.getNStep());

				if (rResult.getStatus() != 0) {
					badStatusCount += 1;
				}
			}

			time = System.currentTimeMillis() - time;
			
			rResult.printOut(System.out, "Fixed rho in cylinder test");
			
			rResult.getTrajectory().print(System.out);
			System.out.println(
					String.format("Rho time: %-7.3f   avg delta = %-9.5f  max delta = %-9.5f  badStatCnt = %d",
							(time) / 1000., sum / num, delMax, badStatusCount));
			System.out.println("Rho Avg NS = " + (int) (((double) nStepTotal) / num) + "   MAX NS: " + nsMax + "\n");

		} catch (AdaptiveSwimException e) {
			e.printStackTrace();
		}
		
		
		//cylinder swim
		try {
			nsMax = 0;
			sum = 0;
			badStatusCount = 0;
			delMax = Double.NEGATIVE_INFINITY;
			nStepTotal = 0;

			time = System.currentTimeMillis();

			InitialValues iv = null;
			for (int i = n0; i < num; i++) {
				iv = ivals[i];

				adaptiveSwimmer.swimCylinder(iv.charge, iv.xo, iv.yo, iv.zo, iv.p, iv.theta, iv.phi, targCyl, accuracy,
						maxPathLength, stepsizeAdaptive, eps, cResult);

				double dd = targCyl.distance(cResult.getUf()[0], cResult.getUf()[1], cResult.getUf()[2]);
				dd = Math.abs(dd); //cyl dist can be neag if inside
				delMax = Math.max(delMax, dd);
				sum += dd;

				if (cResult.getStatus() != adaptStatus[i]) {
					System.out.println("Rho v. Cylinder Status differs for i = " + i + "     rho statust = "
							+ adaptStatus[i] + "   cylinder status = " + cResult.getStatus());
				}

				nStepTotal += cResult.getNStep();
				nsMax = Math.max(nsMax, cResult.getNStep());

				if (cResult.getStatus() != 0) {
					badStatusCount += 1;
				}
			}

			time = System.currentTimeMillis() - time;
			
			cResult.printOut(System.out, "Fixed rho in cylinder test");
			cResult.getTrajectory().print(System.out);

			System.out.println(
					String.format("Cylinder time: %-7.3f   avg delta = %-9.5f  max delta = %-9.5f  badStatCnt = %d",
							(time) / 1000., sum / num, delMax, badStatusCount));
			System.out.println("Cylinder Avg NS = " + (int) (((double) nStepTotal) / num) + "   MAX NS: " + nsMax + "\n");

		} catch (AdaptiveSwimException e) {
			e.printStackTrace();
		}

	}

}
