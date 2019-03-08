package cnuphys.swimtest;

import java.util.Random;

import cnuphys.magfield.MagneticFields;
import cnuphys.magfield.MagneticFields.FieldType;
import cnuphys.rk4.RungeKuttaException;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.Swimmer;

public class ThreadTest {

	// test many points on different threads
	public static void threadTest(final int num, final int numThread) {

		SwimTest.memoryReport("staring thread test");

		MagneticFields.getInstance().setActiveField(FieldType.COMPOSITE);

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				System.err.println("Starting thread " + Thread.currentThread().getName());
				Swimmer swimmer = new Swimmer();
				long seed = 5347632765L;

				Random rand = new Random(seed);

				double hdata[] = new double[3];

				long time = System.currentTimeMillis();
				double pTot = 1.0;
				double theta = 15;
				double phi = 0;
				double z = 411.0 / 100.;
				double accuracy = 10 / 1.0e6;
				double stepSize = 0.01;
				int charge = -1;

				for (int i = 0; i < num; i++) {

					if ((i % 20) == 0) {
						System.err.println(Thread.currentThread().getName() + "[" + i + "]");
					}

					double x0 = (-40. + 20 * rand.nextDouble()) / 100.;
					double y0 = (10. + 40. * rand.nextDouble()) / 100.;
					double z0 = (180 + 40 * rand.nextDouble()) / 100.;

					SwimTrajectory traj;
					try {
						traj = swimmer.swim(charge, x0, y0, z0, pTot, theta, phi, z, accuracy, 10, 10, stepSize,
								Swimmer.CLAS_Tolerance, hdata);

						if (i == 0) {
							double lastY[] = traj.lastElement();
							SwimTest.printVect(lastY, "Thread " + Thread.currentThread().getName() + " first ");
						}
						if (i == (num - 1)) {
							double lastY[] = traj.lastElement();
							SwimTest.printVect(lastY, "Thread " + Thread.currentThread().getName() + " last ");
						}
					} catch (RungeKuttaException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				System.err.println("Thread " + Thread.currentThread().getName() + "  ending millis: "
						+ (System.currentTimeMillis() - time));

			}
		};

		for (int i = 0; i < numThread; i++) {
			Thread thread = new Thread(runnable);
			thread.start();
		}
	}
}
