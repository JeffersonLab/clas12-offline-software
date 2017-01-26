/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.physics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jlab.clas.pdg.PDGDatabase;
import org.jlab.clas.pdg.PDGParticle;

/**
 *
 * @author gavalian
 */

public class Particle {

	LorentzVector partVector;
	Vector3 partVertex;
	int particleID;
	int particleGeantID;
	byte particleCharge;
	HashMap<String, Double> particleProperties;

	public Particle() {
		this.initParticleWithMass(0.0, 0., 0., 0., 0., 0., 0.);
	}

	public Particle(Particle p) {
		this.initParticle(p.pid(), p.px(), p.py(), p.pz(), p.vertex().x(), p.vertex().y(), p.vertex().z());
	}

	public Particle(int pid, double px, double py, double pz, double vx, double vy, double vz) {
		this.initParticle(pid, px, py, pz, vx, vy, vz);
	}

	public Particle(int pid, double px, double py, double pz) {
		this.initParticle(pid, px, py, pz, 0.0, 0.0, 0.0);
	}

	public Particle(int pid, double mass, byte charge, double px, double py, double pz, double vx, double vy, double vz) {
		this.initParticleWithMass(mass, px, py, pz, vx, vy, vz);
		particleID = pid;
		particleCharge = (byte) charge;
	}
        
        public static Particle createWithMassCharge(double mass, int charge ,double px, double py, double pz, double vx, double vy, double vz){
            Particle p = new Particle();
            p.initParticleWithMass(mass, px, py, pz, vx, vy, vz);
            p.particleCharge = (byte) charge;
            return p;
        }
        
        public static Particle createWithPid(int pid ,double px, double py, double pz, double vx, double vy, double vz){
            Particle p = new Particle(pid,px,py,pz,vx,vy,vz);
            return p;
        }
        
	public final void initParticleWithMass(double mass, double px, double py, double pz, double vx, double vy, double vz) {
		particleCharge = 0;
		partVector = new LorentzVector();
		partVertex = new Vector3(vx, vy, vz);
		partVector.setPxPyPzM(px, py, pz, mass);
		particleProperties = new HashMap<String, Double>();
	}

	public final void initParticle(int pid, double px, double py, double pz, double vx, double vy, double vz) {
		PDGParticle particle = PDGDatabase.getParticleById(pid);
		if (particle == null) {
			System.out.println("Particle: warning. particle with pid=" + pid + " does not exist.");
			initParticleWithMass(0., px, py, pz, vx, vy, vz);
			particleID = 0;
			particleGeantID = 0;
		} else {
			initParticleWithMass(particle.mass(), px, py, pz, vx, vy, vz);
			particleID = pid;
			particleGeantID = 0;// particle.gid();
			particleCharge = (byte) particle.charge();
		}
	}

	/**
	 * Change the particle momenta from it's original value to new value
	 * 
	 * @param mom
	 *            new particle momenta
	 */
	public void setP(double mom) {
		double mag = this.vector().p();
		double factor = mom / mag;
		this.vector().setPxPyPzM(this.vector().vect().x() * factor, this.vector().vect().y() * factor, this.vector().vect().z() * factor,
		        this.mass());
	}

	public void setTheta(double theta) {
		this.vector().vect().setMagThetaPhi(this.vector().p(), theta, this.vector().phi());
	}

	public void changePid(int pid) {
		PDGParticle part = PDGDatabase.getParticleById(pid);
		if (part == null) {
			System.err.println("[Particle::changePid]  error ---> unknown particle id " + pid);
			return;
		}
		partVector.setPxPyPzM(this.partVector.px(), this.partVector.py(), this.partVector.pz(), part.mass());

		particleID = pid;
	}

	public void setParticleWithMass(double mass, byte charge, double px, double py, double pz, double vx, double vy, double vz) {
		partVector.setPxPyPzM(px, py, pz, mass);
		particleID = 0;
		particleGeantID = 0;
		particleCharge = charge;
	}

	public void setVector(int pid, double px, double py, double pz, double vx, double vy, double vz) {
		PDGParticle particle = PDGDatabase.getParticleById(pid);
		if (particle == null) {
			System.out.println("Particle: warning. particle with pid=" + pid + " does not exist.");
			particleID = 0;
		} else {
			partVector.setPxPyPzM(px, py, pz, particle.mass());
			partVertex.setXYZ(vx, vy, vz);
			particleID = pid;
			particleGeantID = particle.gid();
			particleCharge = (byte) particle.charge();
		}
	}

	public double px() {
		return this.vector().px();
	}

	public double py() {
		return this.vector().py();
	}

	public double pz() {
		return this.vector().pz();
	}

	public double p() {
		return this.vector().p();
	}

	public double theta() {
		return this.vector().theta();
	}

	public double phi() {
		return this.vector().phi();
	}

	public double e() {
		return this.vector().e();
	}

	public double vx() {
		return this.partVertex.x();
	}

	public double vy() {
		return this.partVertex.y();
	}

	public double vz() {
		return this.partVertex.z();
	}

	public void clearProperties() {
		particleProperties.clear();
	}

	public void setVector(int pid, Vector3 nvect, Vector3 nvert) {
		PDGParticle particle = PDGDatabase.getParticleById(pid);
		if (particle == null) {
			System.out.println("Particle: warning. particle with pid=" + pid + " does not exist.");
			particleID = 0;
		} else {
			partVector.setVectM(nvect, particle.mass());
			partVertex.setXYZ(nvert.x(), nvert.y(), nvert.z());
			particleID = pid;
			particleGeantID = particle.gid();
			particleCharge = (byte) particle.charge();
		}
	}

	public double euclideanDistance(Particle part) {
		double xx = (this.vector().px() - part.vector().px());
		double yy = (this.vector().py() - part.vector().py());
		double zz = (this.vector().pz() - part.vector().pz());
		return Math.sqrt(xx * xx + yy * yy + zz * zz);
	}

	public double cosTheta(Particle part) {
		if (part.vector().p() == 0 || this.vector().p() == 0)
			return -1;
		return part.vector().vect().dot(partVector.vect()) / (part.vector().vect().mag() * partVector.vect().mag());
	}

	void initParticleWithMassSquare(double mass2, double px, double py, double pz, double vx, double vy, double vz) {
		particleCharge = 0;
		partVector = new LorentzVector();
		partVertex = new Vector3(vx, vy, vz);
		partVector.setPxPyPzE(px, py, pz, Math.sqrt(px * px + py * py + pz * pz + mass2));
		particleProperties = new HashMap<String, Double>();
	}

	public void initParticleWithPidMassSquare(int pid, int charge, double mass2, double px, double py, double pz, double vx, double vy,
	        double vz) {
		particleID = pid;
		particleCharge = (byte) charge;
		partVector = new LorentzVector();
		partVertex = new Vector3(vx, vy, vz);
		partVector.setPxPyPzE(px, py, pz, Math.sqrt(px * px + py * py + pz * pz + mass2));
		particleProperties = new HashMap<String, Double>();
	}

	public void setVector(LorentzVector nvec, Vector3 nvert) {
		partVector = nvec;
		partVertex = nvert;
	}

	public double mass() {
		return partVector.mass();
	}

	public double mass2() {
		return partVector.mass2();
	}

	public int charge() {
		return (int) particleCharge;
	}

	public int pid() {
		return particleID;
	}

	public int gid() {
		return particleGeantID;
	}

	public LorentzVector vector() {
		return partVector;
	}

	public Vector3 vertex() {
		return partVertex;
	}

	public double getProperty(String pname) {
		if (particleProperties.containsKey(pname) == true)
			return particleProperties.get(pname).doubleValue();

		return 0.0;
	}

	public Particle inFrame(Particle parent) {
		Vector3 boost = parent.vector().boostVector();
		Vector3 boostm = new Vector3(-boost.x(), -boost.y(), -boost.z());
		partVector.boost(boostm);
		return this;
	}

	public double get(String pname) {
		if (pname.compareTo("mass") == 0)
			return partVector.mass();
		if (pname.compareTo("mass2") == 0)
			return partVector.mass2();
		if (pname.compareTo("theta") == 0)
			return partVector.theta();
		if (pname.compareTo("phi") == 0)
			return partVector.phi();
		if (pname.compareTo("p") == 0)
			return partVector.p();
		if (pname.compareTo("mom") == 0)
			return partVector.p();
		if (pname.compareTo("e") == 0)
			return partVector.e();
		if (pname.compareTo("px") == 0)
			return partVector.px();
		if (pname.compareTo("py") == 0)
			return partVector.py();
		if (pname.compareTo("pz") == 0)
			return partVector.pz();
		if (pname.compareTo("vx") == 0)
			return partVertex.x();
		if (pname.compareTo("vy") == 0)
			return partVertex.y();
		if (pname.compareTo("vz") == 0)
			return partVertex.z();
		if (pname.compareTo("vertx") == 0)
			return partVertex.x();
		if (pname.compareTo("verty") == 0)
			return partVertex.y();
		if (pname.compareTo("vertz") == 0)
			return partVertex.z();

		System.out.println("[Particle::get] ERROR ----> variable " + pname + "  is not defined");
		return 0.0;
	}

	public boolean hasProperty(String pname) {
		if (particleProperties.containsKey(pname) == true)
			return true;
		return false;
	}

	public void setProperty(String pname, double value) {
		// if(particleProperties.containsKey(pname)==true)
		particleProperties.put(pname, value);
	}

	public String propertyString() {
		StringBuilder str = new StringBuilder();
		Iterator it = particleProperties.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			str.append(String.format("%12s : %f", pairs.getKey(), pairs.getValue()));
		}
		return str.toString();
	}

	public String toLundString() {
		StringBuilder str = new StringBuilder();
		str.append(String.format("%3.0f. %4d %6d %2d %2d %9.4f %9.4f %9.4f ", (float) particleCharge, (int) 1, particleID, (int) 0, (int) 0,
		        partVector.px(), partVector.py(), partVector.pz()));

		str.append(String.format("%9.4f %9.4f %11.4f %9.4f %9.4f", partVector.e(), partVector.mass(), partVertex.x(), partVertex.y(),
		        partVertex.z()));
		/*
		 * str.append(String.format("%6d %3d %9.5f %9.5f %9.5f %9.5f %9.5f %9.5f %9.5f %9.5f %9.5f %9.5f", particleID,particleCharge,partVector.mass(),
		 * partVector.px(),partVector.py(),partVector.pz(), partVector.p(), Math.toDegrees(partVector.theta()), Math.toDegrees(partVector.phi()),
		 * partVertex.x(),partVertex.y(),partVertex.z() ));
		 */
		return str.toString();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(String.format("%6d %3d %9.5f %9.5f %9.5f %9.5f %9.5f %9.5f %9.5f", particleID, particleCharge, partVector.mass(),
		        partVector.p(), Math.toDegrees(partVector.theta()), Math.toDegrees(partVector.phi()), partVertex.x(), partVertex.y(),
		        partVertex.z()));
		return str.toString();
	}

	public void copyParticle(Particle part) {
		this.partVector.setPxPyPzM(part.vector().px(), part.vector().py(), part.vector().pz(), part.vector().mass());
		this.partVertex.setXYZ(part.vertex().x(), part.vertex().y(), part.vertex().z());
		particleID = part.pid();
		particleGeantID = part.gid();
		particleCharge = (byte) part.charge();
	}

	public void copy(Particle part) {
		this.copyParticle(part);
	}

	public void combine(Particle cpart, int sign) {
		particleID = 0;
		if (sign >= 0) {
			partVector.add(cpart.vector());
		} else {
			partVector.sub(cpart.vector());
		}

		particleCharge += cpart.charge();

		// if(this.mass()==0.0&&this.vector().p()==0.0)
		// {
		// System.err.println(" pid = " + cpart.pid());
		this.partVertex.setXYZ(cpart.vertex().x(), cpart.vertex().y(), cpart.vertex().z());
		// } else {
		/*
		 * Line3D pl = new Line3D(); Line3D pn = new Line3D();
		 * 
		 * pl.setOrigin(partVertex.x(), partVertex.y(), partVertex.z() );
		 * 
		 * pl.setEnd(partVertex.x()+partVector.vect().x(), partVertex.y()+partVector.vect().y(), partVertex.z()+partVector.vect().z());
		 * 
		 * pn.setOrigin( cpart.vertex().x(), cpart.vertex().y(), cpart.vertex().z());
		 * 
		 * pn.setEnd( cpart.vertex().x() + cpart.vector().px(), cpart.vertex().y() + cpart.vector().py(), cpart.vertex().z() + cpart.vector().pz() ); Line3D doca =
		 * pl.distance(pn); Point3D docam = doca.middle(); this.partVertex.setXYZ(docam.x(), docam.y(), docam.z());
		 * 
		 * /* partVertex.setXYZ(cpart.vertex().x(), cpart.vertex().y(),cpart.vertex().z());
		 */
	}

	public Vector3 particleDoca(Particle cpart) {
		/*
		 * Line3D pl = new Line3D(); Line3D pn = new Line3D();
		 * 
		 * pl.setOrigin(partVertex.x(), partVertex.y(), partVertex.z() );
		 * 
		 * pl.setEnd(partVertex.x()+partVector.vect().x(), partVertex.y()+partVector.vect().y(), partVertex.z()+partVector.vect().z());
		 * 
		 * pn.setOrigin( cpart.vertex().x(), cpart.vertex().y(), cpart.vertex().z());
		 * 
		 * pn.setEnd( cpart.vertex().x() + cpart.vector().px(), cpart.vertex().y() + cpart.vector().py(), cpart.vertex().z() + cpart.vector().pz() );
		 * 
		 * Line3D doca = pl.distance(pn); Point3D docam = doca.middle(); //this.partVertex.setXYZ(docam.x(), docam.y(), docam.z()); return new
		 * BasicVector(docam.x(),docam.y(),docam.z());
		 */
		return new Vector3();
	}

}
