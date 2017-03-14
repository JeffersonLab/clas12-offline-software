/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.fastmc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.geom.DetectorHit;
import org.jlab.geom.base.Detector;
import org.jlab.geom.prim.Path3D;

/**
 *
 * @author gavalian
 */
public class Clas12FastMC {

	private ParticleSwimmer particleSwimmer = null;
	List<DetectorSensitivity> mcSensitivity = new ArrayList<DetectorSensitivity>();
	Map<String, Detector> mcDetectors = new LinkedHashMap<String, Detector>();
	private double torusScale = 0.0;
	private double solenoidScale = 0.0;
	// For smearing
	private final TreeMap<Integer, IParticleResolution> pResolutions = new TreeMap<Integer, IParticleResolution>();
	public boolean isSmeared = false;
	private int debugMode = 0;

	/**
	 * constructor initialized part Monte-Carlo module module. arguments given are field strengths for torus and solenoid magnets. The scale value -1.0 is for
	 * in-bending configuration and +1.0 for out-bending.
	 * 
	 * @param torusScale
	 *            scale of the torus field relative to nominal
	 * @param solenoidScale
	 *            scale of the solenoid field relative to nominal
	 */
	public Clas12FastMC(double torusScale, double solenoidScale) {
		this.torusScale = torusScale;
		this.solenoidScale = solenoidScale;
		particleSwimmer = new ParticleSwimmer(torusScale, solenoidScale);
		this.addResolutionFuncs();
	}

	private void addResolutionFuncs() {
		if (torusScale < 0) {
			this.addResolutionFunc(11, new ParticleResolutionCentralIn());
			this.addResolutionFunc(-211, new ParticleResolutionCentralIn());
			this.addResolutionFunc(-321, new ParticleResolutionCentralIn());
			this.addResolutionFunc(-2212, new ParticleResolutionCentralIn());
			this.addResolutionFunc(321, new ParticleResolutionCentralOut());
			this.addResolutionFunc(211, new ParticleResolutionCentralOut());
			this.addResolutionFunc(2212, new ParticleResolutionCentralOut());
			this.addResolutionFunc(-11, new ParticleResolutionCentralOut());

		} else {
			this.addResolutionFunc(11, new ParticleResolutionCentralOut());
			this.addResolutionFunc(-211, new ParticleResolutionCentralOut());
			this.addResolutionFunc(-321, new ParticleResolutionCentralOut());
			this.addResolutionFunc(-2212, new ParticleResolutionCentralOut());
			this.addResolutionFunc(321, new ParticleResolutionCentralIn());
			this.addResolutionFunc(211, new ParticleResolutionCentralIn());
			this.addResolutionFunc(2212, new ParticleResolutionCentralIn());
			this.addResolutionFunc(-11, new ParticleResolutionCentralIn());
		}
	}

	public void setSmearing(boolean isSmeared) {
		this.isSmeared = isSmeared;
	}

	public void addResolutionFunc(int pid, IParticleResolution res) {
		this.pResolutions.put(pid, res);
	}

	/**
	 * adds a detector to fast MC check list. with given cuts for each detector.
	 * 
	 * @param name
	 * @param detector
	 *            CLAS12 detector defined in geometry package
	 */
	public void addDetector(String name, Detector detector) {
		// DetectorSensitivity ds = new DetectorSensitivity(detector,rhC,rhN);
		// this.fastMCDetectors.add(ds);
		this.mcDetectors.put(name, detector);
	}

	/**
	 * add a detector filter for accepting particles, the filter will be applied only to particles with specified charge, and the arrays indicate the detector names
	 * and minimum number of hits for given detector. for example: addFilter(-1,new String[]{"DC","EC"},new int[]{36,9}); will require for negative particle to have
	 * minimum of 36 hits in DC and minimum of 9 hits in EC to be accepted. Many filters can be added to the fast MC module, and they work as OR. Particles can be
	 * detected in different detectors with different hit numbers, and as long as one filter works for particle with given charge, the particle will be accepted.
	 * 
	 * @param charge
	 * @param d
	 * @param res
	 */
	public void addFilter(int charge, String[] d, int[] res) {
		DetectorSensitivity sensitivity = new DetectorSensitivity(charge, d, res);
		this.mcSensitivity.add(sensitivity);
	}

	/**
	 * set debug mode to control printout of the fast MC.
	 * 
	 * @param mode
	 */
	public void setDebugMode(int mode) {
		this.debugMode = mode;
	}

	/**
	 * Swims particle through magnetic field and checks if all detector requirements are passed. returns false if any detector cut required is not satisfied.
	 * 
	 * @param part
	 * @return
	 */
	public boolean checkParticle(Particle part) {
		/*
		 * Path3D particlePath = particleSwimmer.getParticlePath(part); if(this.debugMode>0){ System.out.println("[check-particle] --> PART : " +
		 * part.toLundString()); } for(DetectorSensitivity ds : fastMCDetectors){
		 * 
		 * List<DetectorHit> hits = ds.getDetector().getLayerHits(particlePath); int nhits = 0; if(hits!=null){ nhits = hits.size(); } if(this.debugMode>0){
		 * System.out.println( String.format("\t DET : %12s :  N hits = %8d", ds.getDetector().getType(),nhits)); }
		 * 
		 * if(part.charge()==0){ if(nhits<ds.requiredLayersNeutral) return false; } else { if(nhits<ds.requiredLayersCharged) return false; } }
		 */

		Map<String, Integer> hitMap = this.getDetectorResponses(part);

		for (DetectorSensitivity sensitivity : this.mcSensitivity) {
			if (sensitivity.getCharge() == part.charge()) {
				if (sensitivity.isValid(hitMap) == true) {
					return true;
				}
			}
		}
		return false;
	}

	public PhysicsEvent getEvent(PhysicsEvent event) {

		PhysicsEvent recEvent = new PhysicsEvent();
		recEvent.setBeamParticle(event.beamParticle());
		recEvent.setTargetParticle(event.targetParticle());

		int ncount = event.count();
		for (int i = 0; i < ncount; i++) {
			Particle genPart = event.getParticle(i);
			if (this.checkParticle(genPart) == true) {
				Particle recParticle = new Particle();
				recParticle.copy(genPart);
				recEvent.addParticle(recParticle);
			}
		}
		/**
		 * Apply resolution smearing if the flag is set.
		 */
		if (this.isSmeared == true) {
			for (int p = 0; p < recEvent.count(); p++) {
				int pid = recEvent.getParticle(p).pid();
				int charge = recEvent.getParticle(p).charge();
				if (this.pResolutions.containsKey(pid) == true) {
					this.pResolutions.get(pid).apply(recEvent.getParticle(p), this.torusScale, this.solenoidScale);
				}
			}
		}

		return recEvent;
	}

	public Map<String, Integer> getDetectorResponses(Particle part) {
		Map<String, Integer> detectors = new LinkedHashMap<String, Integer>();
		Path3D particlePath = this.particleSwimmer.getParticlePath(part);
		for (Map.Entry<String, Detector> entry : mcDetectors.entrySet()) {
			List<DetectorHit> hits = entry.getValue().getLayerHits(particlePath);
			int nhits = 0;
			if (hits != null) {
				nhits = hits.size();
			}
			detectors.put(entry.getKey(), nhits);
		}
		return detectors;
	}

	public void showDetectorResponses(Particle part) {
		Map<String, Integer> detectors = this.getDetectorResponses(part);
		System.out.println("[RESPONSES] --> PARTICLE : " + part.toLundString());
		for (Map.Entry<String, Integer> entry : detectors.entrySet()) {
			System.out.println(String.format("\t DETECTOR [%8s] :  N hits = %12d", entry.getKey(), entry.getValue()));
		}
	}

	public void show() {
		System.out.println(" DETECTORS LOADED");
		for (Map.Entry<String, Detector> entry : this.mcDetectors.entrySet()) {
			System.out.println(String.format("\t Detector : %8s : SECTORS = %4d", entry.getKey(), entry.getValue().getNumSectors()));
		}
		System.out.println(" PARTICLE SELECTION FILTERS");
		for (DetectorSensitivity filter : this.mcSensitivity) {
			System.out.println("\t" + filter.toString());
		}
	}

	/**
	 * Internal class to keep requirements for given detectors.
	 */
	public static class DetectorSensitivity {

		Map<String, Integer> detectorHits = new LinkedHashMap<String, Integer>();
		int charge = 0;

		public DetectorSensitivity(int pch, String[] dnames, int[] hits) {
			charge = pch;
			if (dnames.length != hits.length) {
				System.out.println("[DetectorSensitivity] ERROR : " + "number of detector names doe not match number of hits"
				        + dnames.length + " " + hits.length);
			} else {
				for (int i = 0; i < dnames.length; i++) {
					detectorHits.put(dnames[i], hits[i]);
				}
			}

		}

		public Map<String, Integer> getMap() {
			return this.detectorHits;
		}

		public int getCharge() {
			return charge;
		}

		public boolean isValid(Map<String, Integer> hitMap) {

			for (Map.Entry<String, Integer> entry : detectorHits.entrySet()) {
				if (hitMap.containsKey(entry.getKey()) == false) {
					return false;
				} else {
					if (hitMap.get(entry.getKey()) < entry.getValue()) {
						return false;
					}
				}
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder str = new StringBuilder();
			str.append(String.format(" CHARGE : %4d : ", charge));
			for (Map.Entry<String, Integer> entry : detectorHits.entrySet()) {
				str.append(String.format("[%8s] %4d,", entry.getKey(), entry.getValue()));
			}
			return str.toString();
		}
	}
}
