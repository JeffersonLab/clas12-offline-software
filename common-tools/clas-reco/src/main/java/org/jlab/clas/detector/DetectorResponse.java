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
 * @author baltzell
 */
public class DetectorResponse {
    
    private DetectorDescriptor  descriptor  = new DetectorDescriptor();
    private Vector3D            hitPosition = new Vector3D();
    private Vector3D     hitPositionMatched = new Vector3D();
    private Double             detectorTime = 0.0;
    private Double           detectorEnergy = 0.0;
    private Double           particlePath   = 0.0;
    private int              association    = -1;
    private int              hitIndex       = -1;
    private int                      status = -1;

    public DetectorResponse(){
        super();
    }
    
    public DetectorResponse(int sector, int layer, int component){
        descriptor.setSectorLayerComponent(sector, layer, component);
    }
    
    public void   setTime(double time){ this.detectorTime = time;}
    public void   setPosition(double x, double y, double z){this.hitPosition.setXYZ(x, y, z);}
    public void   setMatchPosition(double x, double y, double z){this.hitPositionMatched.setXYZ(x, y, z);}
    public void   setPath(double path){ this.particlePath = path;}
    public void   setEnergy(double energy) { this.detectorEnergy = energy; }
    public void   setStatus(int status) { this.status = status; }
    
    public double getTime(){ return this.detectorTime;}
    public double getEnergy(){ return this.detectorEnergy; }
    public double getPath(){ return this.particlePath;}
    public int    getSector(){return this.descriptor.getSector();}
    public int    getStatus(){return this.status;}

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
    public int getHitIndex(){return hitIndex;}
    public void setHitIndex(int hitIndex) {this.hitIndex = hitIndex;}
        
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
    
    /**
     * Reads a HIPO event, constructs list of detector responses then returns only
     * entries with energy above given threshold.
     * @param event
     * @param bankName
     * @param type
     * @param minEnergy
     * @return 
     */
    public static List<DetectorResponse>  readHipoEvent(DataEvent event, 
            String bankName, DetectorType type, double minEnergy){ 
        DetectorResponse response = new DetectorResponse();
        List<DetectorResponse> responses = response.readHipoEvent(event, bankName, type);
        return DetectorResponse.getListByEnergy(responses, minEnergy);
    }
    
    /**
     * Returns a list of detectorResponses where all entries have energy above given threshold.
     * @param responses list of detector responses.
     * @param minEnergy minimum energy accepted
     * @return 
     */
    public static List<DetectorResponse>  getListByEnergy(List<DetectorResponse> responses, double minEnergy){
        List<DetectorResponse> responseList = new ArrayList<DetectorResponse>();
        for(DetectorResponse r : responses){
            if(r.getEnergy()>minEnergy){
                responseList.add(r);
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
        
    public static List<DetectorResponse>  getListBySectorLayer(List<DetectorResponse> list, 
            DetectorType type, int sector, int layer){
        List<DetectorResponse> result = new ArrayList<DetectorResponse>();
        for(DetectorResponse res : list){
            if(res.getDescriptor().getType()==type
                    &&res.getDescriptor().getSector()==sector
                    &&res.getDescriptor().getLayer()==layer){
                result.add(res);
            }
        }
        return result;
    }

    public static int getSector(final double phi) {
        // shift in positive-phi direction by 3.5 sectors, result in [0,2*pi):
        final double phiShifted = Math.IEEEremainder(phi+Math.PI/6,2.*Math.PI)+Math.PI;
        // shifted sector number: 
        final int sectorShifted = (int)(phiShifted / (Math.PI/3)) + 1;
        // rotate back to proper sector:
        return sectorShifted<=3 ? sectorShifted-3+6 : sectorShifted-3;
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

