package org.jlab.rec.ahdc.KalmanFilter;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

public class Hit_beam extends Hit {

	double x,y,z;
	double r,phi;

	public Hit_beam(int superLayer, int layer, int wire, int numWire, double doca, double x, double y , double z) {
		super(0, 0, 0, 0, Math.hypot(x,y), 0);
		this.x = x;
		this.y = y;
		this.z = z;
		this.r = Math.hypot(x,y);
		this.phi = Math.atan2(y,x);
	}

	public RealVector get_Vector_beam() {
		return new ArrayRealVector(new double[] {this.r, this.phi, this.z});
	}
}
