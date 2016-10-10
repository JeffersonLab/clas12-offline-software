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
public class RecEvent {
    
    private PhysicsEvent  recEvent = new PhysicsEvent();
    private PhysicsEvent  genEvent = new PhysicsEvent();
    
    private double eucledianDistanceThreshold = 0.35;
    
    
    public RecEvent(double beame){
        recEvent.setBeam(beame);
        genEvent.setBeam(beame);
    }
    
    public void setEuclidianThreshold(double ed){
        this.eucledianDistanceThreshold = ed;
    }
    
    public void addParticle(Particle p){
        recEvent.addParticle(p);
    }
    
    public void addGeneratedParticle(Particle p){
        genEvent.addParticle(p);
    }
    
    public PhysicsEvent getGenerated(){ return genEvent;}
    public PhysicsEvent getReconstructed(){ return recEvent;}
    
    public Particle getMatched(int gindex){
        Particle p = this.genEvent.getParticle(gindex);
        double maxDistance = 1000;
        int    maxIndex    = -1;
        for(int i = 0; i < recEvent.count();i++){
            double distance = recEvent.getParticle(i).euclideanDistance(p);
            if(distance<maxDistance){
                maxIndex = i;
                maxDistance = distance;
            }
            //System.out.println(" INDEX = " +  gindex +  " particle " + i + "  distance =  " + distance);
        }
        //System.out.println(" INDEX = " +  gindex +  " particle " + maxIndex + "  distance =  " + maxDistance);
        if(maxDistance<0.08) return recEvent.getParticle(maxIndex);
        return new Particle();
    }
    
    public void doPidMatch(){
        int nGen = this.genEvent.count();
        
        for(int ig = 0; ig < nGen; ig++){
            double edMin      = 100.0;
            int    edMinIndex = -1;
            Particle p = genEvent.getParticle(ig);
            
            for(int ir = 0 ; ir < recEvent.count();ir++){
                double ed = recEvent.getParticle(ir).euclideanDistance(p);
                if(ed<edMin){
                    edMin = ed; edMinIndex = ir;
                }
            }
            
            if(edMinIndex>=0&&edMin<this.eucledianDistanceThreshold){
                recEvent.getParticle(edMinIndex).changePid(p.pid());
            }
        }
    }
    
    public RecResolution  getResolution(int index){
        
        Particle pg = this.genEvent.getParticle(index);
        Particle pr = recEvent.closestParticle(pg);
        RecResolution res = new RecResolution();
        res.momentum = (pg.p() - pr.p())/pg.p();
        res.theta    = (pg.theta()-pr.theta());
        res.phi      = (pg.phi()-pr.phi());
        
        return res;
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(genEvent.toString());
        str.append(recEvent.toString());
        return str.toString();
    }
    
    public static class RecResolution {
        double momentum = 0.0;
        double theta    = 0.0;
        double phi      = 0.0;
    }
}
