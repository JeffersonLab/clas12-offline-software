package events;

import java.util.Random;
import java.util.ArrayList;
import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H2F;
import org.jlab.groot.ui.TCanvas;

/** Creates multi-particle events w/ semi-realistic phase space
 *
 * @author naharrison
 */
public class RandomEventGenerator {

	ArrayList<Integer> particleID = new ArrayList<>();
	ArrayList<Double> pMin = new ArrayList<>(); // GeV
	ArrayList<Double> pMax = new ArrayList<>();
	ArrayList<Double> thetaMin = new ArrayList<>(); // deg
	ArrayList<Double> thetaMax = new ArrayList<>();
	ArrayList<Double> phiMin = new ArrayList<>(); // deg
	ArrayList<Double> phiMax = new ArrayList<>();

	public RandomEventGenerator() { }

	public void includeParticle(int pid, double pmin, double pmax, double tmin, double tmax, double phimin, double phimax) {
		particleID.add(pid);
		pMin.add(pmin);
		pMax.add(pmax);
		thetaMin.add(tmin);
		thetaMax.add(tmax);
		phiMin.add(phimin);
		phiMax.add(phimax);
	}

	public void clearAllParticles() {
		particleID.clear();
		pMin.clear();
		pMax.clear();
		thetaMin.clear();
		thetaMax.clear();
		phiMin.clear();
		phiMax.clear();
	}

	public PhysicsEvent getPhysicsEvent() {

		 Random r = new Random();
		 PhysicsEvent event = new PhysicsEvent();

		 for(int k = 0; k < particleID.size(); k++)
		 {
			  boolean isGoodParticle = false;
			  while(!isGoodParticle)
			  {
					double pRand = (pMax.get(k) - pMin.get(k))*r.nextDouble() + pMin.get(k);
					double thetaRand = (thetaMax.get(k) - thetaMin.get(k))*r.nextDouble() + thetaMin.get(k);

					double thetaCut = 160.0*Math.exp(-0.4*pRand) + 12.0; // "by-eye" estimate function
					if(thetaCut > thetaMax.get(k)) thetaCut = thetaMax.get(k);

					if(thetaRand < thetaCut)
					{
						 isGoodParticle = true;
						 double phiRand = (phiMax.get(k) - phiMin.get(k))*r.nextDouble() + phiMin.get(k);
						 event.addParticle(getParticle(particleID.get(k), pRand, thetaRand, phiRand));
					}
			  }
		 }
		
		 return event;
	}

	public Particle getParticle(int pid, double p, double th, double phi) {
		 double pt = p*Math.sin(Math.toRadians(th));
		 double px = pt*Math.cos(Math.toRadians(phi));
		 double py = pt*Math.sin(Math.toRadians(phi));
		 double pz = p*Math.cos(Math.toRadians(th));
		 return new Particle(pid, px, py, pz);
	}

	public static void main(String[] args) {
		 RandomEventGenerator gen = new RandomEventGenerator();

		 for(int k = 0; k < 8000; k++)
		 {
		 	  gen.includeParticle(11, 0.5, 10.5, 2.5, 42.5, -180, 180);

			  if(k%4 == 0) {
		 	  		gen.includeParticle(2212, 0.25, 10.25, 2.5, 162.5, -180, 180);
		 	  		gen.includeParticle(22, 0.25, 10.25, 2.5, 162.5, -180, 180);
			  }
			  else if(k%4 == 1) {
		 	  		gen.includeParticle(-13, 0.25, 10.25, 2.5, 162.5, -180, 180);
		 	  		gen.includeParticle(22, 0.25, 10.25, 2.5, 162.5, -180, 180);
			  }
			  else if(k%4 == 2) {
		 	  		gen.includeParticle(13, 0.25, 10.25, 2.5, 162.5, -180, 180);
		 	  		gen.includeParticle(211, 0.25, 10.25, 2.5, 162.5, -180, 180);
			  }
			  else if(k%4 == 3) {
		 	  		gen.includeParticle(211, 0.25, 10.25, 2.5, 162.5, -180, 180);
		 	  		gen.includeParticle(-211, 0.25, 10.25, 2.5, 162.5, -180, 180);
		 	  		gen.includeParticle(2112, 0.25, 10.25, 2.5, 162.5, -180, 180);
			  }

			  System.out.print(gen.getPhysicsEvent().toLundString());

			  gen.clearAllParticles();
		 }
	}

}
