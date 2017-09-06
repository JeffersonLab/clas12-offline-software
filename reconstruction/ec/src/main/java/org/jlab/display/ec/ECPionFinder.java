/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.display.ec;

import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.detector.DetectorParticle;
import org.jlab.clas.detector.DetectorResponse;
import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;

/**
 *
 * @author gavalian
 */
public class ECPionFinder {
    
    List<DetectorResponse>  detectorResponse = null;
    List<DetectorParticle>  particles = new ArrayList<DetectorParticle>();
    List<DetectorParticle>  goodParticles = new ArrayList<DetectorParticle>();
    PhysicsEvent            physicsEvent  = new PhysicsEvent();
    
    public ECPionFinder(){
        
    }
    
    public void processEvent(DataEvent event){
        this.initEvent(event);
        this.doMatching();
        this.assignEnergy();
        //this.show();
    }
    
    public PhysicsEvent getPhysicsEvent(){ return this.physicsEvent;}
    
    public void initEvent(DataEvent event){        
        detectorResponse = DetectorResponse.readHipoEvent(event, "ECAL::clusters", DetectorType.ECAL);
        List<DetectorResponse>  PCAL = DetectorResponse.getListByLayer(detectorResponse, DetectorType.ECAL, 1);
        particles.clear();
        for(DetectorResponse res : PCAL){
            DetectorParticle p = DetectorParticle.createNeutral(res);
            particles.add(p);
        }                
    }
    
    public void show(){
        /*System.out.println("-----> PARTICLES ");
        for(DetectorParticle p : particles){
            System.out.println(p);
        }
        System.out.println("-----> GOOD PARTICLES ");
        for(DetectorParticle p : goodParticles){
            System.out.println(p);
        }*/
        
        System.out.println(physicsEvent.toLundString());
    }
    
    public void assignEnergy(){
        goodParticles.clear();
        for(DetectorParticle p : particles){
            if(p.getDetectorResponses().size()>1){
                double energy = p.getEnergy(DetectorType.ECAL)/0.245;
                p.vector().setMagThetaPhi(energy, p.vector().theta(), p.vector().phi());
                goodParticles.add(p);
            }
        }
        physicsEvent.clear();
        physicsEvent.setBeam(11.0);
        for(DetectorParticle p : goodParticles){
            physicsEvent.addParticle(p.getPhysicsParticle(22));
        }
    }
    
    public double printMass(){
        int nrows = physicsEvent.countByPid(22);
        Particle g1 = new Particle();
        Particle g2 = new Particle();
        double best_distance = 100.0;
        double     best_mass = 100.0;
        for(int i = 0; i < nrows; i++){
            for(int j = i+1; j < nrows; j++){
                g1.copy(physicsEvent.getParticleByPid(22, i));
                double mom = g1.vector().p();
                g2.copy(physicsEvent.getParticleByPid(22, j));
                g1.combine(g2, 1);
                double mass = g1.vector().mass();
                if(Math.abs(mass-0.135)<best_distance){
                    best_mass = mass;
                    best_distance = Math.abs(mass-0.135);
                }
              /*  System.out.println(String.format("%8.3f %8.3f %8.5f", 
                        mom,g2.vector().p(),g1.vector().mass()));
                */        
            }            
        }
        if(best_distance<50.0)
            System.out.println(String.format("%8.3f %8.3f %8.5f", 
                        1.0,1.0,best_mass));
        return best_mass;
    }
    
    public void doMatching(){
        List<DetectorResponse> ECIN = DetectorResponse.getListByLayer(detectorResponse, DetectorType.ECAL, 4);
        //System.out.println("EC INNER SIZE = " + ECIN.size() );
        int counter = 0;
        for(DetectorParticle p : particles){
            int index = p.getDetectorHit(ECIN, DetectorType.ECAL, 4, 15.0);
            //System.out.println( "particle = " + counter +  " index = " + index);
            if(index>=0){
                p.addResponse(ECIN.get(index), true);
            }
            counter++;
        }
        
        List<DetectorResponse> ECOUT = DetectorResponse.getListByLayer(detectorResponse, DetectorType.ECAL, 7);
        for(DetectorParticle p : particles){
            int index = p.getDetectorHit(ECOUT, DetectorType.ECAL, 7, 15.0);
            if(index>=0){
                p.addResponse(ECOUT.get(index), true);
            }
        }
    }
    
    public static void main(String[] args){
        String inputFile = args[0];//"/Users/gavalian/Work/Software/Release-9.0/COATJAVA/bench/dis_pion_rec_full.hipo";
        HipoDataSource reader = new HipoDataSource();        
        reader.open(inputFile);
        
        ECPionFinder  pion = new ECPionFinder();
        
        while(reader.hasEvent()==true){
            DataEvent event = reader.getNextEvent();
            //System.out.println("-----------------");
            pion.processEvent(event);
            pion.printMass();
        }
    }
}
