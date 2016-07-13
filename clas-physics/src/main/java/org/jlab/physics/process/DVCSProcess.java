/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.physics.process;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jlab.clas.pdg.PDGDatabase;
import org.jlab.clas.physics.LorentzVector;
import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.clas.physics.Vector3;
import org.jlab.physics.base.PhaseSpace;

/**
 *
 * @author gavalian
 */
public class DVCSProcess implements IPhysicsProcess {

    public PhysicsEvent getEvent(PhaseSpace kinematics) {
        PhysicsEvent event = new PhysicsEvent();
        event.setBeam(kinematics.getDimension("E").getValue());        
        
        Particle  electron = KinematicsFactory.getElectron( 
                kinematics.getDimension("E").getValue(),
                kinematics.getDimension("Q2").getValue(),
                kinematics.getDimension("xb").getValue());
        
        
        double q2 = kinematics.getDimension("Q2").getValue();
        double xb = kinematics.getDimension("xb").getValue();
        double t  = kinematics.getDimension("t").getValue();
        
        double min_t = this.t_min(q2, xb);
        double max_t = this.t_max(q2, xb);
        
        System.out.println(" t = " + t + " t-min = " + this.t_min(q2, xb) + "  t-max = " + this.t_max(q2, xb));
        
        if(t<min_t||t>max_t){
            return event;
        }
        
        event.addParticle(electron);
        
        
        Vector3 norm = new Vector3(0.0,1.0,0.0);
        
        Particle q2p = event.getParticle("[b]-[11]");
        LorentzVector cm = new LorentzVector(q2p.vector());
        LorentzVector pr = new LorentzVector(0.0,0.0,0.0,0.938);
        cm.add(pr);
        System.out.println("*****************************");
        System.out.println(q2p.toLundString());
        cm.print();
        
        return event;
    }
    
    public PhaseSpace getPhaseSpace() {
        PhaseSpace  space = new PhaseSpace();
        space.add("E"   , 6.0, 4.0, 11.0);
        space.add("Q2"  , 2.4, 1.5,  5.0);
        space.add("xb"  , 0.4, 0.0,  1.0);
        space.add("t"   , 0.2, 0.0, 11.0);
        space.add("phi" , 0.0, -Math.PI,Math.PI);
        return space;
    }
    
    public Map<String, Double> getKinematics() {
        Map<String,Double> kinematics = new LinkedHashMap<String,Double>();
        kinematics.put("E" , 6.0);
        kinematics.put("Q2", 2.4);
        kinematics.put("xb", 0.4);
        kinematics.put("t" , 0.2);
        kinematics.put("phi", 0.0);
        return kinematics;
    }
    
    public double getProperty(String key, PhysicsEvent event){
        
        if(key.compareTo("Q2")==0){
            Particle q2p = event.getParticle("[b]-[11]");
            return -q2p.vector().mass2();
        }
        
        if(key.compareTo("W2")==0){
            Particle w2p = event.getParticle("[b]+[t]-[11]");
            return w2p.vector().mass2();
        }
        
        if(key.compareTo("t")==0){
            Particle delta = event.getParticle("[t]-[2212]");
            return delta.vector().mass2();
        }
        
        if(key.compareTo("phi")==0){
            Particle beam     = event.getParticle("[b]");
            Particle electron = event.getParticle("[11]");
            Vector3  normE    = beam.vector().vect().cross(electron.vector().vect());
            normE.unit();
            Particle proton   = event.getParticle("[2212]");
            Particle gamma    = event.getParticle("[22]");
            Vector3  normH    = proton.vector().vect().cross(gamma.vector().vect());
            //return delta.vector().mass2();
        }
        
        return 0.0;
    }
    
    public double t_min(double q2, double xb){
        double eps = KinematicsFactory.getEpsilon(q2, xb);
        double nomin = 2.0*(1.0-xb)*(1.0-Math.sqrt(1+eps*eps)) + eps*eps;
        double denom = 4.0*xb*(1.0-xb) + eps*eps;
        return q2*(nomin/denom);
    }
    
    public double t_max(double q2, double xb){
        double eps = KinematicsFactory.getEpsilon(q2, xb);
        double nomin = 2.0*(1.0-xb)*(1.0+Math.sqrt(1+eps*eps)) + eps*eps;
        double denom = 4.0*xb*(1.0-xb) + eps*eps;
        return q2*(nomin/denom);
    }
    
    public Particle getProton(PhysicsEvent event, double q2, double xb,double t){
        Particle q2p     = event.getParticle("[b]-[11]");
        LorentzVector cm = new LorentzVector(q2p.vector());
        LorentzVector pr = new LorentzVector(0.0,0.0,0.0,0.938);
        cm.add(pr);
        double min_t = this.t_min(q2, xb);
        double max_t = this.t_max(q2, xb);
        double scale = (max_t - min_t)/Math.PI;
        return new Particle(2212,0.0,0.0,0.0,0.0,0.0,0.0);
        //return 1.0;
    }
    
    public static void main(String[] args){
        //PDGDatabase.show();
        DVCSProcess dvcs = new DVCSProcess();
        PhaseSpace  kinematics = dvcs.getPhaseSpace();
        
        PhysicsEvent event = dvcs.getEvent(kinematics);
        Particle q2 = event.getParticle("[b]-[11]");
        Particle e  = event.getParticle("[11]");
        double q2m = 4*6.0*e.vector().e()*Math.sin(e.theta()/2.0)*Math.sin(e.theta()/2.0);
        System.out.println(q2.mass()*q2.mass() + "  q2 mass = " + q2m);
        System.out.println(event.toLundString());
    }

    
}
