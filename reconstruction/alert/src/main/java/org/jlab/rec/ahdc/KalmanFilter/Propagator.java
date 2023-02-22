package org.jlab.rec.ahdc.KalmanFilter;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.jlab.geom.prim.Point3D;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

public class Propagator {

	private final RungeKutta4 RK4;

	public Propagator(RungeKutta4 rungeKutta4) {
		this.RK4 = rungeKutta4;
	}

	void propagate(Stepper stepper, Indicator indicator) {
		// ------------------------------------------------------------
		final int    maxNbOfStep = 10000;
		final double R           = indicator.R;

		double dMin = Double.MAX_VALUE;
		double d    = 0;

		// System.out.println("R = " + R);
		// stepper.print();

		for (int nbStep = 0; nbStep < maxNbOfStep; nbStep++) {
			double previous_r = stepper.r();
			RK4.doOneStep(stepper);
			double r = stepper.r();
			// stepper.print();


			if (stepper.direction) {
				if (r >= R - 2 - 1.5 * stepper.h) stepper.h = 1e-2;
				if (indicator.hit != null) {
					if (r >= R - 2) {
						d = indicator.hit.distance(new Point3D(stepper.y[0], stepper.y[1], stepper.y[2]));
						if (d < dMin) dMin = d;
					}
					if (r >= R + 2 || d > dMin) {
						// System.out.println("dMin = " + dMin);
						break;
					}
				} else {
					if (r >= R) {
						break;
					}
				}
			} else {
				if (r <= R + 2 + 1.5 * stepper.h) stepper.h = 1e-2;
				if (R < 5) {
					if (stepper.h < 1e-4) stepper.h = 1e-4;
					if ((previous_r - r) < 0) {
						break;
					}
				}
				if (indicator.hit != null) {
					if (r <= R + 2) {
						d = indicator.hit.distance(new Point3D(stepper.y[0], stepper.y[1], stepper.y[2]));
						if (d < dMin) dMin = d;
					}
					if (r <= R - 2 || d > dMin) {
						// System.out.println("dMin = " + dMin);
						break;
					}
				} else {
					if (r <= R) {
						break;
					}
				}


			}



		}
	}

	public RealVector f(Stepper stepper, Indicator indicator) {
		propagate(stepper, indicator);
		return new ArrayRealVector(stepper.y);
	}

	public void propagateAndWrite(Stepper stepper, Indicator indicator, Writer writer) {
		// ------------------------------------------------------------
		final int    maxNbOfStep = 10000;
		final double R           = indicator.R;

		double dMin = Double.MAX_VALUE;
		double d    = 0;

		System.out.println("R = " + R);
		stepper.print();
		try {writer.write("" + Arrays.toString(stepper.y) + '\n');} catch (Exception e) {e.printStackTrace();}

		for (int nbStep = 0; nbStep < maxNbOfStep; nbStep++) {
			double previous_r = stepper.r();
			RK4.doOneStep(stepper);
			double r = stepper.r();
			stepper.print();
			try {writer.write("" + Arrays.toString(stepper.y) + '\n');} catch (Exception e) {e.printStackTrace();}

			if (stepper.direction) {
				if (r >= R - 2 - 1.5 * stepper.h) stepper.h = 1e-2;
				if (indicator.hit != null) {
					if (r >= R - 2) {
						d = indicator.hit.distance(new Point3D(stepper.y[0], stepper.y[1], stepper.y[2]));
						System.out.println("d = " + d);
						if (d < dMin) dMin = d;
					}
					if (r >= R + 2 || d > dMin) {
						System.out.println("dMin = " + dMin);
						break;
					}
				} else {
					if (r >= R) {
						break;
					}
				}
			} else {
				if (r <= R + 2 + 1.5 * stepper.h) stepper.h = 1e-2;
				if (R < 5) {
					if (stepper.h < 1e-4) stepper.h = 1e-4;
					if ((previous_r - r) < 0) {
						break;
					}
				}
				if (indicator.hit != null) {
					if (r <= R + 2) {
						d = indicator.hit.distance(new Point3D(stepper.y[0], stepper.y[1], stepper.y[2]));
						if (d < dMin) dMin = d;
					}
					if (r <= R - 2 || d > dMin) {
						System.out.println("dMin = " + dMin);
						break;
					}
				} else {
					if (r <= R) {
						break;
					}
				}


			}
		}
	}
}
