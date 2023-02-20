package org.jlab.rec.ahdc.KalmanFilter;

import org.jlab.clas.tracking.kalmanfilter.Material;

public class Indicator {

	public double R;
	public double h;
	public Hit hit;
	public boolean direction;
	public Material material;

	public Indicator(double r, double h, Hit hit, boolean direction, Material material) {
		this.R = r;
		this.h = h;
		this.hit = hit;
		this.direction = direction;
		this.material = material;
	}

	public boolean haveAHit() {
		boolean res = this.hit != null;
		if (this.R == 0 && !direction) res = true;
		return res;
	}
}
