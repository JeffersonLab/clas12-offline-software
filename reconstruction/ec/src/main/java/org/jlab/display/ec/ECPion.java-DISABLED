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
import org.jlab.clas.physics.GenericKinematicFitter;
import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;
import org.jlab.clas.physics.Vector3;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Line3D;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioSource;

/**
 *
 * @author gavalian
 */
public class ECPion {
    
    public static List<DetectorResponse>  readEC(EvioDataEvent event){
        List<DetectorResponse>  ecResponse = new ArrayList<DetectorResponse>();
        if(event.hasBank("ECDetector::clusters")==true){
            EvioDataBank ecCL = (EvioDataBank) event.getBank("ECDetector::clusters");
            int nrows = ecCL.rows();
            for(int i = 0; i < nrows; i++){
                DetectorResponse response = new DetectorResponse();
                response.getDescriptor().setType(DetectorType.ECAL);
                response.getDescriptor().setSectorLayerComponent(
                        ecCL.getByte("sector", i),ecCL.getByte("layer", i),0
                        );
                response.setPosition(
                        ecCL.getFloat("X", i),
                        ecCL.getFloat("Y", i),
                        ecCL.getFloat("Z", i)
                        );
                response.setEnergy(ecCL.getFloat("energy", i));
                ecResponse.add(response);
            }            
        }
        return ecResponse;
    }
    
    public static List<DetectorResponse>  getResponseForLayer(List<DetectorResponse> res, int layer){
        List<DetectorResponse>  ecr = new ArrayList<DetectorResponse>();
        for(DetectorResponse r : res){
            if(r.getDescriptor().getLayer()==layer){
                ecr.add(r);
            }
        }
        return ecr;        
    }
    
    public static void getPion(EvioDataEvent event){
        List<DetectorResponse>  ecResponses = ECPion.readEC(event);
        
        List<DetectorResponse>  rPCAL = ECPion.getResponseForLayer(ecResponses, 1);
        
        if(rPCAL.size()!=2) return;
        List<DetectorParticle>  particles = new ArrayList<DetectorParticle>();
        
        for(int i = 0; i < rPCAL.size(); i++){
            DetectorParticle p = new DetectorParticle();
            p.setCross(0.0, 0.0, 0.0, 
                    rPCAL.get(i).getPosition().x(),
                    rPCAL.get(i).getPosition().y(),
                    rPCAL.get(i).getPosition().z()
                    );
            p.addResponse(rPCAL.get(i));
            particles.add(p);
            //System.out.println(p);
        }
        
        List<DetectorResponse> rECIN  = ECPion.getResponseForLayer(ecResponses, 4);
        List<DetectorResponse> rECOUT = ECPion.getResponseForLayer(ecResponses, 7);
        
        int index_ecin  = particles.get(0).getDetectorHitIndex(rECIN);
        
        if(index_ecin>=0&&index_ecin<rECIN.size()){
            double distance = particles.get(0).getDistance(rECIN.get(index_ecin)).length();
            if(distance<15.0){
                particles.get(0).addResponse(rECIN.get(index_ecin));
                rECIN.remove(index_ecin);
            }
        }
        
        int index_ecout  = particles.get(0).getDetectorHitIndex(rECOUT);
        
        if(index_ecout>=0&&index_ecout<rECOUT.size()){
            double distance = particles.get(0).getDistance(rECOUT.get(index_ecout)).length();
            if(distance<15.0){
                particles.get(0).addResponse(rECOUT.get(index_ecout));
                rECOUT.remove(index_ecout);
            }
        }
        
        index_ecin  = particles.get(1).getDetectorHitIndex(rECIN);
        
        if(index_ecin>=0&&index_ecin<rECIN.size()){
            double distance = particles.get(1).getDistance(rECIN.get(index_ecin)).length();
            if(distance<15.0){
                particles.get(1).addResponse(rECIN.get(index_ecin));
                rECIN.remove(index_ecin);
            }
        }
        
        index_ecout  = particles.get(1).getDetectorHitIndex(rECOUT);
        
        if(index_ecout>=0&&index_ecout<rECOUT.size()){
            double distance = particles.get(1).getDistance(rECOUT.get(index_ecout)).length();
            if(distance<15.0){
                particles.get(1).addResponse(rECOUT.get(index_ecout));
                rECOUT.remove(index_ecout);
            }
        }
        
        
        //System.out.println("--------------  EVENT -------------");
        for(DetectorParticle p : particles){
            double energy = p.getEnergy(DetectorType.ECAL);
            //p.getPhysicsParticle(22);
            //System.out.println(" energy = " + energy);
            //System.out.println(p);
        }
        
        Vector3 n1 = particles.get(0).getCrossDir();
        Vector3 n2 = particles.get(1).getCrossDir();
        double  e1 = particles.get(0).getEnergy(DetectorType.ECAL);
        double  e2 = particles.get(1).getEnergy(DetectorType.ECAL);
        n1.unit();
        n2.unit();
        
        Particle g1 = new Particle(22,
                n1.x()*e1/0.27,
                n1.y()*e1/0.27,
                n1.z()*e1/0.27
        );
        
        Particle g2 = new Particle(22,
                n2.x()*e2/0.27,
                n2.y()*e2/0.27,
                n2.z()*e2/0.27
        );
        
        GenericKinematicFitter fitter = new GenericKinematicFitter(11);
        PhysicsEvent gen = fitter.getGeneratedEvent(event);
        
        if(particles.get(0).getResponse(DetectorType.ECAL, 1)!=null&&
                particles.get(0).getResponse(DetectorType.ECAL, 4)!=null&&
                particles.get(1).getResponse(DetectorType.ECAL, 1)!=null&&
                particles.get(1).getResponse(DetectorType.ECAL, 4)!=null                
                ){
            /*
            System.out.println("  ENERGIES = " + (e1/0.27) + "  " + (e2/0.27));
            System.out.println(particles.get(0));
            System.out.println(particles.get(1));
            System.out.println(gen);*/
            g1.combine(g2, +1);
            System.out.println(g1.vector().mass2());
        }

        //System.out.println("  ENERGIES = " + (e1/0.27) + "  " + (e2/0.27));
        //System.out.println(particles.get(0));
        //System.out.println(particles.get(1));
        //System.out.println(gen);
    }
    
    
    public static void getPhoton(EvioDataEvent event){
        
        List<DetectorResponse>  ecResponses = ECPion.readEC(event);        
        List<DetectorResponse>  rPCAL  = ECPion.getResponseForLayer(ecResponses, 1);
        List<DetectorResponse>  rECIN  = ECPion.getResponseForLayer(ecResponses, 4);
        List<DetectorResponse>  rECOUT = ECPion.getResponseForLayer(ecResponses, 7);
        
        if(rPCAL.size()!=1) return;
        List<DetectorParticle>  particles = new ArrayList<DetectorParticle>();
        
        DetectorParticle g = new DetectorParticle();
        g.setCross(0.0, 0.0, 0.0, 
                    rPCAL.get(0).getPosition().x(),
                    rPCAL.get(0).getPosition().y(),
                    rPCAL.get(0).getPosition().z()
                    );
        g.addResponse(rPCAL.get(0));
        
        int index_ecin  = g.getDetectorHitIndex(rECIN);
        int index_ecout = g.getDetectorHitIndex(rECOUT);
        
        if(index_ecin>=0&&rECIN.size()>0)  g.addResponse(rECIN.get(index_ecin));
        if(index_ecout>=0&&rECOUT.size()>0) g.addResponse(rECOUT.get(index_ecout));
        double energy = g.getEnergy(DetectorType.ECAL)/0.27;
        
        Vector3  dir = g.getCrossDir();
        dir.unit();
        /*
        System.out.println(" INDEX = " + index_ecin + "  " + index_ecout 
                + "  energy = " + g.getEnergy(DetectorType.ECAL)
                + "  " + energy + "  " + String.format("%8.5f %8.5f", dir.theta()*57.29,dir.phi()*57.29));
        */
        
        //System.out.println(g);
        
        GenericKinematicFitter fitter = new GenericKinematicFitter(11);
        PhysicsEvent gen = fitter.getGeneratedEvent(event);
        //System.out.println(gen);
        Particle gamma = gen.getParticle("[22]");
        System.out.println(String.format("%8.5f %8.5f %8.5f %8.5f %8.5f %8.5f", energy,
                dir.theta()*57.29,dir.phi()*57.29,gamma.vector().p(),
                gamma.vector().theta()*57.29,gamma.vector().phi()*57.29));
        
        
    }
    
    public static void main(String[] args){
        EvioSource reader = new EvioSource();
        reader.open(args[0]);
        
        GenericKinematicFitter fitter = new GenericKinematicFitter(11);
        
        while(reader.hasEvent()){
            EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
            //ECPion.getPion(event);
            ECPion.getPhoton(event);
            
            //PhysicsEvent gen = fitter.getGeneratedEvent(event);
            //Particle pion = gen.getParticle("[22]+[22,1]");
            //System.out.println("---> " + pion.mass());
        }
    }
}
