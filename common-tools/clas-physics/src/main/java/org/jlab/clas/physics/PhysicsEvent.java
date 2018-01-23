/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.physics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jlab.clas.pdg.PDGDatabase;
import org.jlab.clas.pdg.PDGParticle;
import org.jlab.physics.base.EventSelector;

/**
 *
 * @author gavalian
 */
public class PhysicsEvent {

	Particle eventBeam;
	Particle eventTarget;
	private EventSelector eventSelector = new EventSelector();
	List<Particle> eventParticles;
	List<Particle> generatedParticles;
	HashMap<String, Double> eventProperties;

	private ParticleList mcEvent = new ParticleList();
	private double matchThresholdResolution = 0.02;
	private double matchThresholdAngle = 0.998;

	public PhysicsEvent() {
		eventBeam = new Particle(11, 0., 0., 5.017, 0., 0., 0.);
		eventTarget = new Particle(2212, 0., 0., 0., 0., 0., 0.);
		eventParticles = new ArrayList<Particle>();
		generatedParticles = new ArrayList<Particle>();
		eventProperties = new HashMap<String, Double>();
	}

	public PhysicsEvent(double be) {
		eventBeam = new Particle(11, 0., 0., be, 0., 0., 0.);
		eventTarget = new Particle(2212, 0., 0., 0., 0., 0., 0.);
		eventParticles = new ArrayList<Particle>();
		generatedParticles = new ArrayList<Particle>();
		eventProperties = new HashMap<String, Double>();
	}

	public void addProperty(String name, double value) {
		eventProperties.put(name, value);
	}

	public boolean hasProperty(String name) {
		return eventProperties.containsKey(name);
	}

	/**
	 * returns generated event
	 * 
	 * @return
	 */
	public ParticleList mc() {
		return this.mcEvent;
	}

	/**
	 * returns particle from reconstructed event that matches particle selected from generated event.
	 * 
	 * @param pid
	 *            pid of the particle in generated event
	 * @param skip
	 *            order of particle in generated event
	 * @return particle from reconstructed event
	 */
	public Particle getParticleMatchByPid(int pid, int skip) {
		Particle p = this.mcEvent.getByPid(pid, skip);
		Particle result = new Particle();
		double bestCos = 0.5;
		double bestRes = 0.5;
		// System.out.println(" LOOKING FOR MATCH WITH = " + p.toLundString());
		for (int i = 0; i < this.eventParticles.size(); i++) {
			if (p.charge() == this.eventParticles.get(i).charge()) {
				double cosTheta = p.cosTheta(eventParticles.get(i));
				double resolution = Math.abs((p.p() - eventParticles.get(i).p()) / p.p());
				// System.out.println(i + " " + " cos = " + cosTheta + " res = " + resolution);
				if (cosTheta > bestCos && resolution < bestRes) {
					if (cosTheta >= this.matchThresholdAngle && resolution < this.matchThresholdResolution) {
						bestCos = cosTheta;
						bestRes = resolution;
						// System.out.println(" THIS IS A GOOD PARTICLE = " + eventParticles.get(i).toLundString());
						result.copy(eventParticles.get(i));
					}
				}
			}
		}
		return result;
	}

	public double getProperty(String name) {
		return eventProperties.get(name);
	}

	public void clear() {
		eventParticles.clear();
		eventProperties.clear();
		generatedParticles.clear();
	}

	public int count() {
		return eventParticles.size();
	}

	public int countGenerated() {
		return generatedParticles.size();
	}

	public int countByCharge(int charge) {
		int icount = 0;
		// System.out.println("countainer size = " + eventParticles.size());
		for (int loop = 0; loop < eventParticles.size(); loop++) {
			// System.out.println("Particle pid = " + eventParticles.get(loop).pid()
			// + " charge = " + eventParticles.get(loop).charge());
			if (eventParticles.get(loop).charge() == charge)
				icount++;
		}
		// System.out.println("particles with charge " + charge + " = " + icount);
		return icount;
	}

	/**
	 * returns number of particles with given PID (Lund id), if generated flag==true the count in generated particles is returned.
	 * 
	 * @param pid
	 * @param generated
	 * @return
	 */
	public int countByPid(int pid, boolean generated) {
		if (generated == false)
			return countByPid(pid);
		int icount = 0;
		for (int loop = 0; loop < generatedParticles.size(); loop++)
			if (generatedParticles.get(loop).pid() == pid)
				icount++;
		return icount;
	}

	public int countByPid(int pid) {
		int icount = 0;
		for (int loop = 0; loop < eventParticles.size(); loop++)
			if (eventParticles.get(loop).pid() == pid)
				icount++;
		return icount;
	}

	public void addGeneratedParticle(Particle part) {
		generatedParticles.add(part);
	}

	public void addGeneratedParticle(int pid, double px, double py, double pz, double vx, double vy, double vz) {
		generatedParticles.add(new Particle(pid, px, py, pz, vx, vy, vz));
	}

	public void addParticle(Particle part) {
		eventParticles.add(part);
	}

	public void addParticle(int pid, double px, double py, double pz, double vx, double vy, double vz) {
		eventParticles.add(new Particle(pid, px, py, pz, vx, vy, vz));
	}

	public void setBeam(String particle, double mom_z) {
		PDGParticle partInfo = PDGDatabase.getParticleByName(particle);
		if (partInfo != null) {
			this.eventBeam.setVector(partInfo.pid(), 0.0, 0.0, mom_z, 0.0, 0.0, -100.0);
		}
	}

	public void setBeam(double mom_z) {
		this.eventBeam = new Particle(11, 0.0, 0.0, mom_z, 0.0, 0.0, -100.0);
	}

	public void setBeamParticle(Particle p) {
		eventBeam.copyParticle(p);
	}

	public void setTargetParticle(Particle p) {
		eventTarget.copyParticle(p);
	}

	public Particle beamParticle() {
		return eventBeam;
	}

	public Particle targetParticle() {
		return eventTarget;
	}

	public int getParticleIndex(int pid, int skip) {
		int skiped = 0;
		for (int loop = 0; loop < eventParticles.size(); loop++) {
			// System.err.println("searching ----> " + CLASParticles.get(loop).getPid()
			// + " " + skip + " " + skiped);
			if (eventParticles.get(loop).pid() == pid) {
				if (skip == skiped)
					return loop;
				else
					skiped++;
			}
		}
		return -1;
	}

	public void removeParticleByPid(int pid, int skip) {
		int index = getParticleIndex(pid, skip);
		if (index < 0 || index >= this.count()) {
			System.out.println("----> error. paritcle does not exist pid=" + pid + " skip=" + skip);
		} else {
			this.removeParticle(index);
		}
	}

	public void removeParticle(int index) {
		eventParticles.remove(index);
	}

	public Particle getParticleByCharge(int charge, int skip) {
		int skiped = 0;
		for (int loop = 0; loop < eventParticles.size(); loop++) {
			// System.err.println("searching ----> " + CLASParticles.get(loop).getPid()
			// + " " + skip + " " + skiped);
			if (eventParticles.get(loop).charge() == charge) {
				if (skip == skiped)
					return eventParticles.get(loop);
				else
					skiped++;
			}
		}
		return null;
	}

	public Particle getParticleByCharge(int charge, int skip, int pid) {
		int skiped = 0;
		for (int loop = 0; loop < eventParticles.size(); loop++) {
			// System.err.println("searching ----> " + CLASParticles.get(loop).getPid()
			// + " " + skip + " " + skiped);
			if (eventParticles.get(loop).charge() == charge) {
				if (skip == skiped) {
					Particle ref = eventParticles.get(loop);
					Particle part = new Particle(pid, ref.vector().px(), ref.vector().py(), ref.vector().pz(), ref.vertex().x(),
					        ref.vertex().y(), ref.vertex().z());
					return part;
				} else
					skiped++;
			}
		}
		return null;
	}

	public Particle getParticleByPid(int pid, int skip) {
		if (pid == 5000)
			return this.beamParticle();
		if (pid == 5001)
			return this.targetParticle();

		int index = this.getParticleIndex(pid, skip);
		if (index < 0 || index >= this.count()) {
			return new Particle(pid, 0., 0., 0., 0., 0., 0.);
		}
		return eventParticles.get(index);
	}

	public Particle getGeneratedParticle(int index) {
		if (index < 0 || index >= generatedParticles.size())
			return null;
		return generatedParticles.get(index);
	}

	public Particle getParticle(int index) {
		if (index < 0 || index >= eventParticles.size())
			return null;
		return eventParticles.get(index);
	}

	public Particle getParticle(String selector) {
		EventSelector evt_selector = new EventSelector(selector);
		return evt_selector.get(this);
		/*
		 * eventSelector.parse(selector); return eventSelector.get(this);
		 */
	}

       /* public Particle getParticleByPid(int pid, int skip) {
                if (pid == 5000)
                        return this.beamParticle();
                if (pid == 5001)
                        return this.targetParticle();

                int index = this.getParticleIndex(pid, skip);
                if (index < 0 || index >= this.count()) {
                        return new Particle(pid, 0., 0., 0., 0., 0., 0.);
                }
                return eventParticles.get(index);
        }*/
        
	public String toLundStringGenerated() {
		StringBuilder str = new StringBuilder();
		str.append(String.format("%12d %2d. %2d. %2d %2d %5.3f %7.3f %7.3f %7.3f %7.3f\n", generatedParticles.size(), (int) 1, (int) 1,
		        (int) 1, (int) 1, (float) 0.0, (float) 0.0, eventBeam.vector().e(), (float) 0.0, (float) 0.0));
		for (int loop = 0; loop < generatedParticles.size(); loop++) {
			str.append(String.format("%5d", loop + 1));
			str.append(generatedParticles.get(loop).toLundString());
			str.append("\n");
		}
		return str.toString();
	}

	public String toLundString() {
		StringBuilder str = new StringBuilder();
		str.append(String.format("%12d %2d. %2d. %2d %2d %5.3f %7.3f %7.3f %7.3f %7.3f\n", eventParticles.size(), (int) 1, (int) 1, (int) 1,
		        (int) 1, (float) 0.0, (float) 0.0, eventBeam.vector().e(), (float) 0.0, (float) 0.0));
		for (int loop = 0; loop < eventParticles.size(); loop++) {
			str.append(String.format("%5d", loop + 1));
			str.append(eventParticles.get(loop).toLundString());
			str.append("\n");
		}
		/*
		 * str.append(String.format("%10d %9.5f %9.5f\n",eventParticles.size(),eventBeam.vector().e(), eventTarget.vector().mass())); for(int loop = 0; loop <
		 * eventParticles.size(); loop++){ str.append(eventParticles.get(loop).toString()); str.append("\n"); }
		 */
		return str.toString();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(String.format("%12d %2d. %2d. %2d %2d %5.3f %7.3f %7.3f %7.3f %7.3f\n", eventParticles.size(), (int) 1, (int) 1, (int) 1,
		        (int) 1, (float) 0.0, (float) 0.0, eventBeam.vector().e(), (float) 0.0, (float) 0.0));
		for (int loop = 0; loop < eventParticles.size(); loop++) {
			str.append(String.format("%5d", loop + 1));
			str.append(eventParticles.get(loop).toString());
			str.append("\n");
		}
		return str.toString();
	}

	public Particle closestParticle(Particle child) {
		Particle part = new Particle();
		double minCos = -1.0;
		int index = -2;
		int icounter = 0;
		for (Particle p : eventParticles) {
			double cth = p.cosTheta(child);
			if (cth > minCos) {
				minCos = cth;
				index = icounter;
			}
			icounter++;
		}
		if (index >= 0)
			part.copyParticle(eventParticles.get(index));
		return part;
	}

	public Vector3 primaryVertex() {
		Particle proton = this.getParticleByPid(2212, 0);
		if (proton.vector().p() > 0) {
			return this.beamParticle().particleDoca(proton);
		}
		return new Vector3();
	}

	Vector3 reactionVertexThreeParticles(int index[]) {
		if (index.length == 2) {

		}
		return new Vector3();
	}

	Vector3 reactionVertexTwoParticles(int index[]) {
		if (index.length == 2) {

		}
		return new Vector3();
	}

	public List getParticleListByPid(int... pids_order) {
		ArrayList<Particle> plist = new ArrayList<Particle>();
		for (int loop = 0; loop < pids_order.length; loop += 2) {
			Particle part = this.getParticleByPid(pids_order[loop], pids_order[loop + 1]);
			if (part != null) {
				plist.add(part);
			}
		}
		return plist;
	}

	public List<Particle> getParticlesByPid(int pid){
		List<Particle> plist = new ArrayList<Particle>();
		for(Particle part: eventParticles){
			if(part.pid()==pid){
				plist.add(part);
			}
		}
		return plist;
	}
}
