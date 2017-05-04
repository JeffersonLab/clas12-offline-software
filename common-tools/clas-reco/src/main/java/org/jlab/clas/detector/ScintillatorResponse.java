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
public class ScintillatorResponse {
    
    private DetectorDescriptor  descriptor  = new DetectorDescriptor();
    private Vector3D             hitPosition = new Vector3D();
    //private Point3D             hitPosition = new Vector3();
    private Vector3D             hitPositionMatched = new Vector3D();
    private Double             detectorTime = 0.0;
    private Double           detectorEnergy = 0.0;
    private Double           particlePath   = 0.0;
    private int              association    = -1;
    private int             component = -1;
    private double              hitQuality = 0.0;
    
    public ScintillatorResponse(){
        
    }
    
    public ScintillatorResponse(int sector, int layer, int component){
        descriptor.setSectorLayerComponent(sector, layer, component);
    }
    
    public void   setTime(double time){ this.detectorTime = time;}
    public void   setPosition(double x, double y, double z){this.hitPosition.setXYZ(x, y, z);}
    public void   setMatchPosition(double x, double y, double z){this.hitPositionMatched.setXYZ(x, y, z);}
    public void   setPath(double path){ this.particlePath = path;}
    public void   setEnergy(double energy) { this.detectorEnergy = energy; }
    public void   setComponent(int paddle) {this.component = paddle;}
    public void   setHitQuality(double q) {this.hitQuality = q;}
    
    public int    getComponent() {return this.component;}
    public double getTime(){ return this.detectorTime;}
    public double getEnergy(){ return this.detectorEnergy; }
    public double getPath(){ return this.particlePath;}
    public double getHitQuality() {return this.hitQuality;}
    
    public Vector3D getPosition(){ return this.hitPosition;}
    public Vector3D getMatchedPosition(){ return this.hitPositionMatched;}
    
    public double   getMatchedDistance(){ 
        return Math.sqrt( 
                (this.hitPosition.x()-this.hitPositionMatched.x()) * (this.hitPosition.x()-this.hitPositionMatched.x())
                        + (this.hitPosition.y()-this.hitPositionMatched.y()) * (this.hitPosition.y()-this.hitPositionMatched.y())
                        + (this.hitPosition.z()-this.hitPositionMatched.z()) * (this.hitPosition.z()-this.hitPositionMatched.z())
        );
    }
    
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
     * reads ScintillatorResponse List from DataEvent class. it has to contain
     * branches:
     *   sector and layer (type BYTE), 
     *   x,y,z (type FLOAT)
     *   energy,time (type FLOAT)
     * @param event
     * @param bankName
     * @param type
     * @return 
     */
    public static List<ScintillatorResponse>  readHipoEvent(DataEvent event, 
            String bankName, DetectorType type){        
        List<ScintillatorResponse> responseList = new ArrayList<ScintillatorResponse>();
        if(event.hasBank(bankName)==true){
            DataBank bank = event.getBank(bankName);
            int nrows = bank.rows();
            for(int row = 0; row < nrows; row++){
                int sector = bank.getByte("sector", row);
                int  layer = bank.getByte("layer",  row);

                
                ScintillatorResponse  response = new ScintillatorResponse(sector,layer,0);
                response.getDescriptor().setType(type);
                float x = bank.getFloat("x", row);
                float y = bank.getFloat("y", row);
                float z = bank.getFloat("z", row);
                if(type==DetectorType.FTOF){
                int component = bank.getShort("component",row);
                    response.setComponent(component);
                }
                response.setPosition(x, y, z);
                response.setEnergy(bank.getFloat("energy", row));
                response.setTime(bank.getFloat("time", row));
                responseList.add(response);
            }
        }
        return responseList;
}
    /*
    public static List<ScintillatorResponse>  readHipoEvent(DataEvent event, 
            String bankName, DetectorType type, double energyThreshold){
        
        List<ScintillatorResponse> responseList = new ArrayList<ScintillatorResponse>();
        if(event.hasBank(bankName)==true){
            DataBank bank = event.getBank(bankName);
            int nrows = bank.rows();
            for(int row = 0; row < nrows; row++){
                int sector = bank.getByte("sector", row);
                int  layer = bank.getByte("layer",  row);
                ScintillatorResponse  response = new ScintillatorResponse(sector,layer,0);
                response.getDescriptor().setType(type);
                float x = bank.getFloat("x", row);
                float y = bank.getFloat("y", row);
                float z = bank.getFloat("z", row);
                response.setPosition(x, y, z);
                response.setEnergy(bank.getFloat("energy", row));
                response.setTime(bank.getFloat("time", row));
                if(response.getEnergy()>energyThreshold){
                    responseList.add(response);
                }
            }
        }
        return responseList;
    }*/
    /**
     * Reads a HIPO event, constructs list of detector responses then returns only
     * entries with energy above given threshold.
     * @param event
     * @param bankName
     * @param type
     * @param minEnergy
     * @return 
     */
    public static List<ScintillatorResponse>  readHipoEvent(DataEvent event, 
            String bankName, DetectorType type, double minEnergy){ 
        List<ScintillatorResponse> responses = ScintillatorResponse.readHipoEvent(event, bankName, type);
        return ScintillatorResponse.getListByEnergy(responses, minEnergy);
    }
    
    /**
     * Returns a list of detectorResponses where all entries have energy above given threshold.
     * @param responses list of detector responses.
     * @param minEnergy minimum energy accepted
     * @return 
     */
    public static List<ScintillatorResponse>  getListByEnergy(List<ScintillatorResponse> responses, double minEnergy){
        List<ScintillatorResponse> responseList = new ArrayList<ScintillatorResponse>();
        for(ScintillatorResponse r : responses){
            if(r.getEnergy()>minEnergy){
                responseList.add(r);
            }
        }
        return responseList;
    }
    
    public static List<ScintillatorResponse>  getListBySector(List<ScintillatorResponse> list, DetectorType type, int sector){
        List<ScintillatorResponse> result = new ArrayList<ScintillatorResponse>();
        for(ScintillatorResponse res : list){
            if(res.getDescriptor().getType()==type&&res.getDescriptor().getSector()==sector){
                result.add(res);
            }
        }
        return result;
    }
    
    public static List<ScintillatorResponse>  getListByLayer(List<ScintillatorResponse> list, DetectorType type, int layer){
        List<ScintillatorResponse> result = new ArrayList<ScintillatorResponse>();
        for(ScintillatorResponse res : list){
            if(res.getDescriptor().getType()==type&&res.getDescriptor().getLayer()==layer){
                result.add(res);
            }
        }
        return result;
    }
        
    public static List<ScintillatorResponse>  getListBySectorLayer(List<ScintillatorResponse> list, 
            DetectorType type, int sector, int layer){
        List<ScintillatorResponse> result = new ArrayList<ScintillatorResponse>();
        for(ScintillatorResponse res : list){
            if(res.getDescriptor().getType()==type
                    &&res.getDescriptor().getSector()==sector
                    &&res.getDescriptor().getLayer()==layer){
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
        str.append(String.format(" T/P/E %6.2f %6.2f %6.2f", this.detectorTime,
                this.particlePath,
                this.detectorEnergy));
        str.append(String.format(" POS [ %8.2f %8.2f %8.2f ]", 
                this.hitPosition.x(),this.hitPosition.y(),this.hitPosition.z()));
        str.append(String.format(" ACCURACY [ %8.3f %8.3f %8.3f ] ",
                this.hitPosition.x()-this.hitPositionMatched.x(),
                this.hitPosition.y()-this.hitPositionMatched.y(),
                this.hitPosition.z()-this.hitPositionMatched.z()
                ));
        str.append(String.format(" %8.4f", this.getMatchedDistance()));
        return str.toString();
    }
}
