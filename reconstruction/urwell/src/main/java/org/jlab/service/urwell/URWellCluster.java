package org.jlab.service.urwell;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;

/**
 * Add description
 * 
 * @author bondi, devita
 */
public class URWellCluster {
   
    
    private DetectorDescriptor  desc          = new DetectorDescriptor(DetectorType.URWELL);
    private List<URWellStrip>   clusterStrips = new ArrayList<URWellStrip>();
    private Line3D              clusterLine   = new Line3D();
    public int                  indexMaxStrip = -1;
    private byte                clusterStatus    = 1;
    //private int                 clusterID        = -1;
    
    public URWellCluster(URWellStrip strip){
        this.desc.setSectorLayerComponent(strip.getDescriptor().getSector(), 
                                          strip.getDescriptor().getLayer(), 0);
        this.clusterStrips.add(strip);
        this.clusterLine.copy(strip.getLine());
        this.indexMaxStrip = 0;
    }
    
    public Line3D  getLine() {return this.clusterLine;}
    
    
    public void setStatus(int val) {this.clusterStatus+=val;}
    
    public byte getStatus()  {return clusterStatus;}   
    
    public void setPeakId(int id){
        for(URWellStrip strip : this.clusterStrips){
            strip.setClusterId(id);
        }
    }
    
    public double getEnergy(){
        double energy = 0.0;
        for(URWellStrip strip : this.clusterStrips){
            energy += strip.getEnergy();
        }
        return energy;
    }
    
    public double getEnergy(Point3D point){
        double energy = 0.0;
        for(URWellStrip strip : this.clusterStrips){
            energy += strip.getEnergy();
        }
        return energy;
    }
        
    public double getTime(){
        if(this.indexMaxStrip >= 0 && this.indexMaxStrip < this.clusterStrips.size()){
            return this.clusterStrips.get(indexMaxStrip).getTime();
        }
            return 0.0;
    }

    public double getTime(Point3D point) {
        if (this.indexMaxStrip >= 0 && this.indexMaxStrip < this.clusterStrips.size()) {
                return this.clusterStrips.get(indexMaxStrip).getTime();
        }
        return 0.0;
    }
	
    public DetectorDescriptor getDescriptor(){
        return this.desc;
    }
    
    public URWellStrip getMaxURWellStrip() {
    	    return this.clusterStrips.get(this.indexMaxStrip);
    }
    
    public int      getMaxStrip(){
        return this.clusterStrips.get(this.indexMaxStrip).getDescriptor().getComponent();
    }
    
    public boolean  addStrip(URWellStrip strip){
        for(URWellStrip s : this.clusterStrips){
            if(s.isNeighbour(strip)){
                this.clusterStrips.add(strip);
                if(strip.getEnergy()>clusterStrips.get(indexMaxStrip).getEnergy()){
                    this.indexMaxStrip = this.clusterStrips.size()-1;
                    this.clusterLine.copy(strip.getLine());
                }
                return true;
            }
        }
        return false;
    }
    
    public List<URWellStrip> getStrips(){
        return this.clusterStrips;
    }
    
    public int getADC(){
        int adc = 0;
        for(URWellStrip s : this.clusterStrips){
            adc+= s.getADC();
        }
        return adc;
    }
    
    public void redoPeakLine(){
        
        Point3D pointOrigin = new Point3D(0.0,0.0,0.0);
        Point3D pointEnd    = new Point3D(0.0,0.0,0.0);
        
        double logSumm = 0.0;
        double summE   = 0.0;
        
        for(int i = 0; i < this.clusterStrips.size(); i++){
            Line3D line = this.clusterStrips.get(i).getLine();
            
            double energy = this.clusterStrips.get(i).getEnergy();
            double energyMev = energy*1000.0;
//            double     le = Math.log(energy);  //ECReconstructionTest fails to find PCAL cluster for this choice
            double     le = Math.log(energyMev);
            
            pointOrigin.setX(pointOrigin.x()+line.origin().x()*le);
            pointOrigin.setY(pointOrigin.y()+line.origin().y()*le);
            pointOrigin.setZ(pointOrigin.z()+line.origin().z()*le);
            
            pointEnd.setX(pointEnd.x()+line.end().x()*le);
            pointEnd.setY(pointEnd.y()+line.end().y()*le);
            pointEnd.setZ(pointEnd.z()+line.end().z()*le);
            
            logSumm += le;
            
            summE   += energy;
        }
                
        this.clusterLine.set(
                pointOrigin.x()/logSumm,
                pointOrigin.y()/logSumm,
                pointOrigin.z()/logSumm,
                pointEnd.x()/logSumm,
                pointEnd.y()/logSumm,
                pointEnd.z()/logSumm
        );
        
        
        
        
        // Calculating Moments of the shower cluster
        for(int i = 0; i < this.clusterStrips.size(); i++){            
            double energyMev = this.clusterStrips.get(i).getEnergy()*1000.0;
            double energyLog = Math.log(energyMev);
        }
        
    }
    
    
    public int getMultiplicity(){
        return this.clusterStrips.size();
    }
    
    
    public static List<URWellCluster> createClusters(List<URWellStrip> stripList){
    	
        List<URWellCluster>  clusterList = new ArrayList<>();
        
        if(stripList.size()>0){
            for(int loop = 0; loop < stripList.size(); loop++){ //Loop over all strips 
                boolean stripAdded = false;                
                for(URWellCluster  cluster : clusterList) {
                    if(cluster.addStrip(stripList.get(loop))){ //Add adjacent strip to newly seeded peak
                        stripAdded = true;
                    }
                }
                if(!stripAdded){
                    URWellCluster  newPeak = new URWellCluster(stripList.get(loop)); //Non-adjacent strip seeds new peak
                    clusterList.add(newPeak);
                }
            }
        }
        for(int loop = 0; loop < clusterList.size(); loop++){
            clusterList.get(loop).setPeakId(loop+1);
        }
        return clusterList;
    }   
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("----> cluster  ( %3d %3d )  ENERGY = %12.5f\n", 
                this.desc.getSector(),this.desc.getLayer(), this.getEnergy()));
        str.append(this.clusterLine.toString());
        str.append("\n");
        for(URWellStrip strip : this.clusterStrips){
            str.append("\t\t");
            str.append(strip.toString());
            str.append("\n");
        }
        
        return str.toString();
    }

    
    


}
