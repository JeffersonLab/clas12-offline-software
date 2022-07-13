package org.jlab.rec.rtpc.KalmanFilter;

import org.jlab.clas.pdg.PDGParticle;
import org.jlab.clas.pdg.PhysicsConstants;

public class RungeKutta4 {
	private final int numberOfVariables;
	private final PDGParticle particle;
	private final double[] B;

	private Stepper stepper;

	private final double[] k1;
	private final double[] k2;
	private final double[] k3;
	private final double[] k4;

	private final double[] yInTemp;
	private final double[] yTemp;

	public RungeKutta4(PDGParticle particle, int numberOfVariables, double[] B) {

		this.numberOfVariables = numberOfVariables;
		this.particle = particle;
		this.B = B;

		this.k1 = new double[numberOfVariables];
		this.k2 = new double[numberOfVariables];
		this.k3 = new double[numberOfVariables];
		this.k4 = new double[numberOfVariables];

		this.yInTemp = new double[numberOfVariables];
		this.yTemp = new double[numberOfVariables];
	}

	public void doOneStep(Stepper stepper) {

		this.stepper = stepper;

		double[] yIn = stepper.y;
		double h = stepper.h;
		org.jlab.clas.tracking.kalmanfilter.Material material_ = stepper.material;


		stepper.s += h;
		if (stepper.is_in_drift) stepper.s_drift += h;

		if (stepper.direction) stepper.sTot += h;
		else stepper.sTot -= h;

		System.arraycopy(yIn, 0, yInTemp, 0, numberOfVariables);

		double[] dydt = f(yInTemp);
		for (int i = 0; i < numberOfVariables; ++i) {
			k1[i] = h * dydt[i];
		}

		for (int i = 0; i < numberOfVariables; ++i) {
			yTemp[i] = yInTemp[i] + 0.5 * k1[i];
		}
		dydt = f(yTemp);
		for (int i = 0; i < numberOfVariables; ++i) {
			k2[i] = h * dydt[i];
		}

		for (int i = 0; i < numberOfVariables; ++i) {
			yTemp[i] = yInTemp[i] + 0.5 * k2[i];
		}
		dydt = f(yTemp);
		for (int i = 0; i < numberOfVariables; ++i) {
			k3[i] = h * dydt[i];
		}

		for (int i = 0; i < numberOfVariables; ++i) {
			yTemp[i] = yInTemp[i] + k3[i];
		}
		dydt = f(yTemp);
		for (int i = 0; i < numberOfVariables; ++i) {
			k4[i] = h * dydt[i];
		}

		for (int i = 0; i < numberOfVariables; ++i) {
			yIn[i] =
					yInTemp[i]
							+ 1.0 / 6.0 * k1[i]
							+ 1.0 / 3.0 * k2[i]
							+ 1.0 / 3.0 * k3[i]
							+ 1.0 / 6.0 * k4[i];
		}

		energyLoss(yIn, h, material_);
	}

	private double[] f(double[] y) {
		double charge = 1.0;
		double pModuleInverse = 1.0 / Math.sqrt(y[3] * y[3] + y[4] * y[4] + y[5] * y[5]);
		double k = charge * PhysicsConstants.speedOfLight() * 10 * pModuleInverse;

		if (this.stepper.direction) {
			return new double[]{
					y[3] * pModuleInverse,
					y[4] * pModuleInverse,
					y[5] * pModuleInverse,
					k * (y[4] * B[2] - y[5] * B[1]),
					k * (y[5] * B[0] - y[3] * B[2]),
					k * (y[3] * B[1] - y[4] * B[0])
			};
		} else {
			return new double[]{
					-y[3] * pModuleInverse,
					-y[4] * pModuleInverse,
					-y[5] * pModuleInverse,
					-k * (y[4] * B[2] - y[5] * B[1]),
					-k * (y[5] * B[0] - y[3] * B[2]),
					-k * (y[3] * B[1] - y[4] * B[0])
			};
		}
	}

	private void energyLoss(
			double[] yIn, double h, org.jlab.clas.tracking.kalmanfilter.Material material) {
		double mass = particle.mass() * 1000;

		h /= 10; // cm
		double mom = Math.sqrt(yIn[3] * yIn[3] + yIn[4] * yIn[4] + yIn[5] * yIn[5]);
		double E = Math.sqrt(mom * mom + mass * mass);

		double DeltaE = ComputeDEDX(mom, mass, h, material) * 1000; // MeV/cm

		stepper.dEdx += DeltaE;

		double mom_prim;
		if (this.stepper.direction) mom_prim = Math.sqrt((E - DeltaE) * (E - DeltaE) - mass * mass);
		else mom_prim = Math.sqrt((E + DeltaE) * (E + DeltaE) - mass * mass);

		yIn[3] *= mom_prim / mom;
		yIn[4] *= mom_prim / mom;
		yIn[5] *= mom_prim / mom;
	}

	public double ComputeDEDX(
			double p,
			double mass,
			double thickness,
			org.jlab.clas.tracking.kalmanfilter.Material material) {
		if (mass == 0) return 0;
		double beta = p / Math.sqrt(p * p + mass * mass);
		double s = PhysicsConstants.massElectron() / mass;
		double gamma = 1. / Math.sqrt(1 - beta * beta);
		double Wmax =
				2.
						* PhysicsConstants.massElectron()
						* beta
						* beta
						* gamma
						* gamma
						/ (1. + 2. * s * gamma + s * s);
		double K = 0.000307075; //  GeV mol-1 cm2
		double I = material.getIeV() * 1E-9;
		double logterm =
				2. * PhysicsConstants.massElectron() * beta * beta * gamma * gamma * Wmax / (I * I);
		return thickness
				* material.getDensity()
				* K
				* material.getZoverA()
				* (0.5 * Math.log(logterm) - beta * beta)
				/ beta
				/ beta;
	}
}
