package org.jlab.rec.rtpc.KalmanFilter;

import org.jlab.rec.rtpc.KalmanFilter.EnergyLoss.Material;

public class Indicator {

	public double R;
	public double h;
	public Material material;
	public Hit hit;
	public boolean direction;

	public Indicator(double r, double h, Material material, Hit hit, boolean direction) {
		this.R = r;
		this.h = h;
		this.material = material;
		this.hit = hit;
		this.direction = direction;
	}

	public boolean haveAHit(){
		boolean res = this.hit != null;
		if (this.R == 0 && !direction) res = true;
		return res;
	}
}
