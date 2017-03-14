/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.clas.physics;

/**
 *
 * @author gavalian
 */
public class ParticleGenerator {
    private int particleID = 11;
    private double pCosThetaMin = -1.0;
    private double pCosThetaMax = 1.0;
    private double pPhiMin = -180.0;
    private double pPhiMax = -180.0;
    private double pMomentumMin = 1.0;
    private double pMomentumMax = 5.0;    
    private final Vector3  vertexConstrains = new Vector3(0.0,0.0,0.0);
    
    public ParticleGenerator(int pid){
        particleID = pid;
    }
    
    public ParticleGenerator(int pid, double pmin, double pmax, double thmin, double thmax,
            double phimin, double phimax){
        particleID = pid;
        this.setRange(pmin, pmax, thmin, thmax, phimin, phimax);
    }
    
    public final void setRange(double pmin, double pmax, double thmin, double thmax,
            double phimin, double phimax){
        this.setMomRange(pmin, pmax);
        this.setThetaRange(thmin, thmax);
        this.setPhiRange(phimin, phimax);
    }
    
    public ParticleGenerator setMomRange(double pmin, double pmax){
        pMomentumMin = pmin;
        pMomentumMax = pmax;
        return this;
    }
    
    public ParticleGenerator setPhiRange(double pmin, double pmax){
        pPhiMin = pmin;
        pPhiMax = pmax;
        return this;
    }
    
    public ParticleGenerator setThetaRange(double tmin, double tmax){
        pCosThetaMin = Math.cos(Math.toRadians(tmin));
        pCosThetaMax = Math.cos(Math.toRadians(tmax));
        return this;
    }
    
    public Particle getParticle(){
        double p     = pMomentumMin + Math.random()*(pMomentumMax-pMomentumMin);
        double theta = Math.acos(pCosThetaMin + Math.random()*(pCosThetaMax-pCosThetaMin));
        double phi   = Math.toRadians(pPhiMin + Math.random()*(pPhiMax-pPhiMin));
        //Vector3  pvector = new Vector3(p*Math.sin(theta)*Math.cos(phi),
        //p*Math.sin(theta)*Math.sin(phi),p*Math.cos(theta));
        Particle part = new Particle(particleID,
                p*Math.sin(theta)*Math.cos(phi),
                p*Math.sin(theta)*Math.sin(phi),
                p*Math.cos(theta),0.0,0.0,0.0);
        return part;
    }
    
}
