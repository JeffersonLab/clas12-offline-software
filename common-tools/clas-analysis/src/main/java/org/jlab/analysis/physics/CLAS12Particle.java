package org.jlab.analysis.physics;

import org.jlab.clas.physics.Particle;

/**
 * 
 * @author naharrison
 */
public class CLAS12Particle {
	
	public Particle particle;
	public double Epreshower;
	public double Einner;
	public double Eouter;
	public double Etotal;
	public double tofTime;
	public double tofPath;
	public int ltccNphe;
	public int htccNphe;
	
	public CLAS12Particle(Particle particle, double Epreshower, double Einner, double Eouter, double tofTime, double tofPath, int ltccNphe, int htccNphe) {
		this.particle = particle;
		this.Epreshower = Epreshower;
		this.Einner = Einner;
		this.Eouter = Eouter;
		this.Etotal = Epreshower + Einner + Eouter;
		this.tofTime = tofTime;
		this.tofPath = tofPath;
		this.ltccNphe = ltccNphe;
		this.htccNphe = htccNphe;
	}

	public Particle getParticle() {
		return particle;
	}

	public double getEpreshower() {
		return Epreshower;
	}

	public double getEinner() {
		return Einner;
	}

	public double getEouter() {
		return Eouter;
	}

	public double getEtotal() {
		return Etotal;
	}

	public double getTofTime() {
		return tofTime;
	}

	public double getTofPath() {
		return tofPath;
	}

	public int getLtccNphe() {
		return ltccNphe;
	}

	public int getHtccNphe() {
		return htccNphe;
	}

}
