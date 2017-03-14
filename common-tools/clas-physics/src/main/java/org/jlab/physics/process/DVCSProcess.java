/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.physics.process;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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

    private static final double MP = 0.93827;
    
    public PhysicsEvent getEvent(PhaseSpace kinematics) {
        PhysicsEvent event = new PhysicsEvent();
        event.setBeam(kinematics.getDimension("E").getValue());        
        
        Particle  electron = KinematicsFactory.getElectron( 
                kinematics.getDimension("E").getValue(),
                kinematics.getDimension("Q2").getValue(),
                kinematics.getDimension("xb").getValue());
        
        double E   = kinematics.getDimension("E").getValue();
        double q2  = kinematics.getDimension("Q2").getValue();
        double xb  = kinematics.getDimension("xb").getValue();
        double t   = kinematics.getDimension("t").getValue();
        double phi = kinematics.getDimension("phi").getValue();
        
        double min_t = this.t_min(q2, xb);
        double max_t = this.t_max(q2, xb);
        double xmin  = q2/(2.0*MP*E);
        /*System.out.println(" t = " + t + " t-min = " 
                + this.t_min(q2, xb) + "  t-max = " + this.t_max(q2, xb) + 
                "  xmin = " + xmin);
        */
        if(t<min_t||t>max_t){
            return event;
        }
        
        event.addParticle(electron);
        
        List<Vector3>  protonGamma = this.getDvcsPhoton(E, q2, xb, t, 0.0, phi);
        //System.out.println(" PROTON " + protonGamma.get(0));
        //System.out.println(" PHOTON " + protonGamma.get(1));
        if(protonGamma.size()<2){
            event.clear();
            return event;
        }
        
        Particle proton = new Particle(2212,
                protonGamma.get(0).x(),
                protonGamma.get(0).y(),
                protonGamma.get(0).z()
        );
        
        Particle photon = new Particle(22,
                protonGamma.get(1).x(),
                protonGamma.get(1).y(),
                protonGamma.get(1).z()
        );
        
        event.addParticle(proton);
        event.addParticle(photon);
        /*
        Vector3 norm = new Vector3(0.0,1.0,0.0);
        
        Particle q2p = event.getParticle("[b]-[11]");
        LorentzVector cm = new LorentzVector(q2p.vector());
        LorentzVector pr = new LorentzVector(0.0,0.0,0.0,0.938);
        cm.add(pr);
        System.out.println("*****************************");
        System.out.println(q2p.toLundString());
        cm.print();
        */
        return event;
    }
    
    public PhaseSpace getPhaseSpace() {
        PhaseSpace  space = new PhaseSpace();
        space.add("E"   , 6.0, 4.0, 11.0);
        space.add("Q2"  , 2.4, 1.5,  5.0);
        space.add("xb"  , 0.35, 0.0, 1.0);
        space.add("t"   , 0.25, 0.0, 11.0);
        space.add("phi" , 0.0, -Math.PI,Math.PI);
        return space;
    }
    
    public Map<String, Double> getKinematics() {
        Map<String,Double> kinematics = new LinkedHashMap<String,Double>();
        /*kinematics.put("E" , 6.0);
        kinematics.put("Q2", 2.5);
        kinematics.put("xb", 0.35);
        kinematics.put("t" , 0.2);
        kinematics.put("phi", 0.0);*/
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
            return -delta.vector().mass2();
        }
        
        if(key.compareTo("xb")==0){
            Particle q2p   = event.getParticle("[b]-[11]");
            Particle ep    = event.getParticle("[11]");
            Particle beam  = event.getParticle("[b]");
            double nu = beam.vector().e()-ep.vector().e();
            double q2 = -q2p.vector().mass2();
            return q2/(2.0*MP*nu);
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
    
    public List<Vector3>  getDvcsPhoton(double E, double q2, double xb, double t, double phield, double phigd){
        
        double nu = q2/(2.0*MP*xb);
        double qmod = Math.sqrt(nu*nu + q2);
        double esc  = E - nu;
        double costel = 1.0 - q2/(2.0*E*esc);
        double sintel = Math.sqrt(1.0 - costel*costel);
        
        if(esc<0.0) return new ArrayList<Vector3>();
        
        double Ep   = MP + t/(2.0*MP);
        double Egam = nu - t/(2.0*MP);
        
        double cosphe = Math.cos(phield);
        double sinphe = Math.sin(phield);
        Vector3 vecQ  = new Vector3(0.0,0.0,E);
        Vector3 vecEP = new Vector3(esc*sintel*cosphe,esc*sintel*sinphe,esc*costel);
        vecQ.sub(vecEP);
        /*System.out.println(" Q2 VECTOR = " + "ESC =  "  +  nu + " " + vecQ);
        System.out.println(" EP / EGAM  = " + Ep + " "  +  Egam + " VEC " + vecQ);
        System.out.println("******* " + nu + " " + Egam + "  " + q2);*/
        /*double a1 = 2.0*Egam*(MP + nu) + q2;
        double a2 =  - 2.0*MP*nu ;
        double a3 = 2.0*Egam*qmod;
        */
        //System.out.println("a1/a2/a3 = " + a1 + " " + a2 + " " + a3);
        /*System.out.println(" NU = " + nu);*/
        
        double costVq = vecQ.z()/qmod;
        double sintVq = Math.sqrt(1.0 - costVq*costVq);
        double costgg = (2.0*Egam*(MP + nu) + q2 - 2.0*MP*nu )/(2.0*Egam*qmod);
        if(costgg>1.0) return new ArrayList<Vector3>();
        //double costgg = (2.0*Egam*(nu) + q2 - 2.0*MP*nu )/(2.0*Egam*qmod);
        double sintgg = Math.sqrt(1.0 - costgg*costgg);
        double Vgx    = Egam*sintgg*Math.cos(phigd);
        double Vgy    = Egam*sintgg*Math.sin(phigd);
        double Vgz    = Egam*costgg;
        
        Vector3 vecG  = new Vector3(
                Vgx * costVq * cosphe - Vgz*sintVq*cosphe - Vgy*sinphe,
                Vgx * costVq * sinphe - Vgz*sintVq*sinphe + Vgy*cosphe,
                Vgx * sintVq + Vgz*costVq
        );
        /*System.out.println(" cos VQ " + costVq +   " costgg = " + costgg 
                + " qmod = " + qmod + "  Nu = " + nu);*/
        //System.out.println(" PHOTON VECT = " + vecG);
        List<Vector3>  protonGamma = new ArrayList<Vector3>();
        vecQ.sub(vecG);
        
        protonGamma.add(vecQ);
        protonGamma.add(vecG);
        
        return protonGamma;
    }
    
    public static void main(String[] args){
        //PDGDatabase.show();
        DVCSProcess dvcs = new DVCSProcess();
        PhaseSpace  kinematics = dvcs.getPhaseSpace();
        
        //kinematics.show();
        
        kinematics.getDimension("phi").setValue(Math.toRadians(30.0));
        PhysicsEvent event = dvcs.getEvent(kinematics);
                
        /*
        Particle q2 = event.getParticle("[b]-[11]");
        Particle e  = event.getParticle("[11]");
        double q2m = 4*6.0*e.vector().e()*Math.sin(e.theta()/2.0)*Math.sin(e.theta()/2.0);
        System.out.println(q2.mass()*q2.mass() + "  q2 mass = " + q2m);
        */
        
        //System.out.println(event.toLundString());
        
        Particle  mt  = event.getParticle("[t]-[2212]");
        Particle  mtg = event.getParticle("[b]-[11]-[22]");
        System.out.println(" -t  = " + mt.vector().mass2());
        System.out.println(" -tg = " + mtg.vector().mass2());
        
        /*
        for(double xb = 0.1 ; xb < 0.6; xb += 0.05){
            kinematics.getDimension("xb").setValue(xb);
            event = dvcs.getEvent(kinematics);
        }*/
    }    

    public double getWeight(PhaseSpace kinematics) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
