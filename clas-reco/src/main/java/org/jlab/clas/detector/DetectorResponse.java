/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.detector;


import java.util.ArrayList;
import java.util.List;
import org.jlab.clas.physics.Vector3;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Path3D;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author gavalian
 */
public class DetectorResponse {
    
    private DetectorDescriptor  descriptor  = new DetectorDescriptor();
    private Vector3D             hitPosition = new Vector3D();
    //private Point3D             hitPosition = new Vector3();
    private Vector3D             hitPositionMatched = new Vector3D();
    private Double             detectorTime = 0.0;
    private Double           detectorEnergy = 0.0;
    private Double           particlePath   = 0.0;
    private int              association    = -1;
    
    public DetectorResponse(){
        
    }
    
    public DetectorResponse(int sector, int layer, int component){
        descriptor.setSectorLayerComponent(sector, layer, component);
    }
    
    public void   setTime(double time){ this.detectorTime = time;}
    public void   setPosition(double x, double y, double z){this.hitPosition.setXYZ(x, y, z);}
    public void   setMatchPosition(double x, double y, double z){this.hitPositionMatched.setXYZ(x, y, z);}
    public void   setPath(double path){ this.particlePath = path;}
    public void   setEnergy(double energy) { this.detectorEnergy = energy; }
    
    public double getTime(){ return this.detectorTime;}
    public double getEnergy(){ return this.detectorEnergy; }
    public double getPath(){ return this.particlePath;}
    
    public Vector3D getPosition(){ return this.hitPosition;}
    public Vector3D getMatchedPosition(){ return this.hitPositionMatched;}
    
    public DetectorDescriptor getDescriptor(){ return this.descriptor;}
    
    
    public int getAssociation(){ return this.association;}
    public void setAssociation(int asc){ this.association = asc;}
    
    public Line3D  getDistance(DetectorParticle particle){
        Path3D  trajectory = particle.getTrajectory();
        return trajectory.distance(hitPosition.x(),hitPosition.y(),hitPosition.z());
    }
    
    public int  getParticleMatch(DetectorEvent event){
        
        double  distance = 1000.0;
        int     index    = -1;
        
        int nparticles = event.getParticles().size();
        for(int p = 0; p < nparticles; p++){
            DetectorParticle  particle = event.getParticles().get(p);
            Line3D distanceLine = this.getDistance(particle);
            if(distanceLine.length()<distance){
                distance = distanceLine.length();
                index    = p;
            }
        }
        return index;
    }
    
    /**
     * reads DetectorResponse List from DataEvent class. it has to contain
     * branches:
     *   sector and layer (type BYTE), 
     *   x,y,z (type FLOAT)
     *   energy,time (type FLOAT)
     * @param event
     * @param bankName
     * @param type
     * @return 
     */
    public static List<DetectorResponse>  readHipoEvent(DataEvent event, 
            String bankName, DetectorType type){        
        List<DetectorResponse> responseList = new ArrayList<DetectorResponse>();
        if(event.hasBank(bankName)==true){
            DataBank bank = event.getBank(bankName);
            int nrows = bank.rows();
            for(int row = 0; row < nrows; row++){
                int sector = bank.getByte("sector", row);
                int  layer = bank.getByte("layer",  row);
                DetectorResponse  response = new DetectorResponse(sector,layer,0);
                response.getDescriptor().setType(type);
                float x = bank.getFloat("x", row);
                float y = bank.getFloat("y", row);
                float z = bank.getFloat("z", row);
                response.setPosition(x, y, z);
                response.setEnergy(bank.getFloat("energy", row));
                response.setTime(bank.getFloat("time", row));
                responseList.add(response);
            }
        }
        return responseList;
    }
    
    public static List<DetectorResponse>  getListBySector(List<DetectorResponse> list, DetectorType type, int sector){
        List<DetectorResponse> result = new ArrayList<DetectorResponse>();
        for(DetectorResponse res : list){
            if(res.getDescriptor().getType()==type&&res.getDescriptor().getSector()==sector){
                result.add(res);
            }
        }
        return result;
    }
    
    public static List<DetectorResponse>  getListByLayer(List<DetectorResponse> list, DetectorType type, int layer){
        List<DetectorResponse> result = new ArrayList<DetectorResponse>();
        for(DetectorResponse res : list){
            if(res.getDescriptor().getType()==type&&res.getDescriptor().getLayer()==layer){
                result.add(res);
            }
        }
        return result;
    }
        
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("\t [%8s] [%3d %3d %3d] ", 
                this.descriptor.getType().toString(),
                this.descriptor.getSector(),
                this.descriptor.getLayer(),
                this.descriptor.getComponent()
                ));
        str.append(String.format(" PINDX [%3d] ", 
                this.getAssociation()
                ));
        str.append(String.format(" T/P/E %8.4f %8.4f %8.4f", this.detectorTime,
                this.particlePath,
                this.detectorEnergy));
        str.append(String.format(" POS [ %9.3f %9.3f %9.3f ]", 
                this.hitPosition.x(),this.hitPosition.y(),this.hitPosition.z()));
        str.append(String.format(" ACCURACY [ %9.3f %9.3f %9.3f ] ",
                this.hitPosition.x()-this.hitPositionMatched.x(),
                this.hitPosition.y()-this.hitPositionMatched.y(),
                this.hitPosition.z()-this.hitPositionMatched.z()
                ));
        return str.toString();
    }
}
