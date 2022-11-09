package org.jlab.rec.rtpc.KalmanFilter;

import org.jlab.clas.tracking.kalmanfilter.Material;

import java.util.Arrays;

public class Stepper {
	public double[] y;
	public double h;
	public Material material;
	public double s;
	public double sTot = 0;
	public double s_drift = 0;
	public boolean is_in_drift = false;
	public double dEdx;
	public boolean direction;

	public Stepper(double[] y) {
		this.y = Arrays.copyOf(y, y.length);
	}

	public void initialize(Indicator indicator) {
		this.s = 0;
		this.dEdx = 0;
		this.h = indicator.h;
		this.material = indicator.material;
		this.direction = indicator.direction;
		this.is_in_drift = false;

	}

	public boolean isStepCorrect() {
		return !Double.isNaN(y[0])
				&& !Double.isNaN(y[1])
				&& !Double.isNaN(y[2])
				&& !Double.isNaN(y[3])
				&& !Double.isNaN(y[4])
				&& !Double.isNaN(y[5]);
	}

	public double r() {
		return Math.hypot(y[0], y[1]);
	}

	public double p() {
		return Math.sqrt(y[3] * y[3] + y[4] * y[4] + y[5] * y[5]);
	}

	@Override
	public String toString() {
		return "" + sTot + ' ' + y[0] + ' ' + y[1] + ' ' + y[2] + ' ' + y[3] + ' ' + y[4] + ' ' + y[5];
	}
}
