package org.jlab.analysis.physics;

import java.util.ArrayList;
import java.util.Arrays;
import org.jlab.clas.physics.Particle;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 * 
 * @author naharrison
 */
public class CLAS12Event {
	
	public ArrayList<CLAS12Particle> c12particles = new ArrayList<>();
	
	public CLAS12Event(DataEvent event) {
		if(event.hasBank("REC::Particle")) {
			processEvent(event);
		}
	}

	private void processEvent(DataEvent event) {
		DataBank recBank = event.getBank("REC::Particle");
		for(int iparticle = 0; iparticle < recBank.rows(); iparticle++) {
			byte charge = recBank.getByte("charge", iparticle);
			float px = recBank.getFloat("px", iparticle);
			float py = recBank.getFloat("py", iparticle);
			float pz = recBank.getFloat("pz", iparticle);
			float vx = recBank.getFloat("vx", iparticle);
			float vy = recBank.getFloat("vy", iparticle);
			float vz = recBank.getFloat("vz", iparticle);
			Particle particle = Particle.createWithMassCharge(0.0, charge, px, py, pz, vx, vy, vz);
			ArrayList<Double> energies = getECalEnergies(event, iparticle);
			double Epreshower = energies.get(0);
			double Einner = energies.get(1);
			double Eouter = energies.get(2);
			ArrayList<Double> tofInfo = getTofInfo(event, iparticle);
			double tofTime = tofInfo.get(0);
			double tofPath = tofInfo.get(1);
			ArrayList<Integer> ccInfo = getCcInfo(event, iparticle);
			int ltccNphe = ccInfo.get(0);
			int htccNphe = ccInfo.get(1);
			c12particles.add(new CLAS12Particle(particle, Epreshower, Einner, Eouter, tofTime, tofPath, ltccNphe, htccNphe));
		}	
	}

	private ArrayList<Double> getECalEnergies(DataEvent event, int iparticle) {
		double ep, ei, eo;
		ep = ei = eo = 0.0;
		if(event.hasBank("REC::Calorimeter")) {
			DataBank calBank = event.getBank("REC::Calorimeter");
			for(int k = 0; k < calBank.rows(); k++) {
				short pindex = calBank.getShort("pindex", k);
				//short detector = calBank.getShort("detector", k);
				byte detector = calBank.getByte("detector", k);
				if(pindex == iparticle && detector == DetectorType.ECAL.getDetectorId()) {
					byte layer = calBank.getByte("layer", k);
					float energy = calBank.getFloat("energy", k);
					if(layer == 1) ep += energy;
					else if(layer == 4) ei += energy;
					else if(layer == 7) eo += energy;
				}
			}
		}
		return new ArrayList<Double>(Arrays.asList(ep, ei, eo));
	}

	private ArrayList<Double> getTofInfo(DataEvent event, int iparticle) {
		double t = -1;
		double p = -1;
		if(event.hasBank("REC::Scintillator")) {
			DataBank sciBank = event.getBank("REC::Scintillator");
			for(int k = 0; k < sciBank.rows(); k++) {
				short pindex = sciBank.getShort("pindex", k);
				//short detector = sciBank.getShort("detector", k);
				byte detector = sciBank.getByte("detector", k);
				if(pindex == iparticle && (detector == DetectorType.FTOF.getDetectorId() || detector == DetectorType.CTOF.getDetectorId())) {
					byte layer = sciBank.getByte("layer", k);
					if(!(detector == DetectorType.FTOF.getDetectorId() && layer == 1)) {
						t = sciBank.getFloat("time", k);
						p = sciBank.getFloat("path", k);
						break; // taking the first tof hit, that isn't in panel 1A
					}
				}
			}
		}
		return new ArrayList<Double>(Arrays.asList(t, p));
	}
	
	private ArrayList<Integer> getCcInfo(DataEvent event, int iparticle) {
		Integer L = 0;
		Integer H = 0;
		if(event.hasBank("REC::Cherenkov")) {
			DataBank ccBank = event.getBank("REC::Cherenkov");
			for(int k = 0; k < ccBank.rows(); k++) {
				short pindex = ccBank.getShort("pindex", k);
				//short detector = ccBank.getShort("detector", k);
				byte detector = ccBank.getByte("detector", k);
				short nphe = ccBank.getShort("nphe", k);
				if(pindex == iparticle && detector == DetectorType.LTCC.getDetectorId()) L = (int) nphe;
				else if(pindex == iparticle && detector == DetectorType.HTCC.getDetectorId()) H = (int) nphe;
				// if multiple entries, will get the last ones
			}
		}
		return new ArrayList<Integer>(Arrays.asList(L, H));
	}

	public ArrayList<CLAS12Particle> getCLAS12Particles() {
		return c12particles;
	}
}
