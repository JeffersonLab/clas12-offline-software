package org.jlab.service.ec;

import java.util.ArrayList;
import java.util.List;

import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.base.DetectorType;

import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;

/**
 *
 * @author gavalian
 */

public class ECPeak {
    
    private DetectorDescriptor  desc       = new DetectorDescriptor(DetectorType.ECAL);
    private List<ECStrip>       peakStrips = new ArrayList<ECStrip>();
    private Line3D              peakLine   = new Line3D();
    public int                  indexMaxStrip = -1;
    private int                 peakOrder     = -1;
    private byte                peakStatus    = 1;
    private double              peakDistanceEdge = 0.0;
    private double              peakMoment       = 0.0;
    private double              peakMoment2      = 0.0;
    private double              peakMoment3      = 0.0;
    //private int                 peakID        = -1;
    
    public ECPeak(ECStrip strip){
        this.desc.setSectorLayerComponent(strip.getDescriptor().getSector(), 
                                          strip.getDescriptor().getLayer(), 0);
        this.peakStrips.add(strip);
        this.peakLine.copy(strip.getLine());
        this.indexMaxStrip = 0;
    }
    
    public Line3D  getLine() {return this.peakLine;}
    
    public void setOrder(int order) { this.peakOrder = order;}
    
    public int  getOrder() { return this.peakOrder;}
    
    public void setStatus(int val) {this.peakStatus+=val;}
    
    public byte getStatus()  {return peakStatus;}   
    
    public void setPeakId(int id){
        for(ECStrip strip : this.peakStrips){
            strip.setPeakId(id);
        }
    }
    
    public double getEnergy(){
        double energy = 0.0;
        for(ECStrip strip : this.peakStrips){
            energy += strip.getEnergy();
        }
        return energy;
    }
    
    public double getEnergy(Point3D point){
         double energy = 0.0;
        for(ECStrip strip : this.peakStrips){
            energy += strip.getEnergy(point);
        }
        return energy;
    }
        
    public double getTime(){
        if(this.indexMaxStrip >= 0 && this.indexMaxStrip < this.peakStrips.size()){
            return this.peakStrips.get(indexMaxStrip).getTime();
        }
            return 0.0;
    }

    public double getTime(Point3D point) {
		if (this.indexMaxStrip >= 0 && this.indexMaxStrip < this.peakStrips.size()) {
			return this.peakStrips.get(indexMaxStrip).getTime(point);
		}
		return 0.0;
	}
	
    public DetectorDescriptor getDescriptor(){
        return this.desc;
    }
    
    public ECStrip getMaxECStrip() {
    	    return this.peakStrips.get(this.indexMaxStrip);
    }
    
    public int      getMaxStrip(){
        return this.peakStrips.get(this.indexMaxStrip).getDescriptor().getComponent();
    }
    
    public boolean  addStrip(ECStrip strip){
        for(ECStrip s : this.peakStrips){
            if(s.isNeighbour(strip)){
                this.peakStrips.add(strip);
                if(strip.getEnergy()>peakStrips.get(indexMaxStrip).getEnergy()){
                    this.indexMaxStrip = this.peakStrips.size()-1;
                    this.peakLine.copy(strip.getLine());
                }
                return true;
            }
        }
        return false;
    }
    
    public List<ECStrip> getStrips(){
        return this.peakStrips;
    }
    
    public int getADC(){
        int adc = 0;
        for(ECStrip s : this.peakStrips){
            adc+= s.getADC();
        }
        return adc;
    }
    
    public void redoPeakLine(){
        
        Point3D pointOrigin = new Point3D(0.0,0.0,0.0);
        Point3D pointEnd    = new Point3D(0.0,0.0,0.0);
        this.peakDistanceEdge = 0.0;
        this.peakMoment       = 0.0;
        this.peakMoment2      = 0.0;
        this.peakMoment3      = 0.0;
        
        double logSumm = 0.0;
        double summE   = 0.0;
        
        for(int i = 0; i < this.peakStrips.size(); i++){
            Line3D line = this.peakStrips.get(i).getLine();
            
            double energy = this.peakStrips.get(i).getEnergy();
            double energyMev = energy*1000.0;
//            double     le = Math.log(energy);  //ECReconstructionTest fails to find PCAL cluster for this choice
            double     le = Math.log(energyMev);
            
            this.peakDistanceEdge += 
//                    peakStrips.get(i).getDistanceEdge() + 
                    peakStrips.get(i).getDistanceEdge()*le;
            
            pointOrigin.setX(pointOrigin.x()+line.origin().x()*le);
            pointOrigin.setY(pointOrigin.y()+line.origin().y()*le);
            pointOrigin.setZ(pointOrigin.z()+line.origin().z()*le);
            
            pointEnd.setX(pointEnd.x()+line.end().x()*le);
            pointEnd.setY(pointEnd.y()+line.end().y()*le);
            pointEnd.setZ(pointEnd.z()+line.end().z()*le);
            
            logSumm += le;
            
            summE   += energy;
        }
        
        this.peakDistanceEdge = this.peakDistanceEdge/logSumm;
        //System.out.println(" LOG SUMM = " + logSumm);
        
        this.peakLine.set(
                pointOrigin.x()/logSumm,
                pointOrigin.y()/logSumm,
                pointOrigin.z()/logSumm,
                pointEnd.x()/logSumm,
                pointEnd.y()/logSumm,
                pointEnd.z()/logSumm
        );
        
        
        
        
        // Calculating Moments of the shower peak
        for(int i = 0; i < this.peakStrips.size(); i++){            
            double stripDistance = this.peakStrips.get(i).getDistanceEdge();
            double dist = this.peakDistanceEdge - stripDistance;
            double energyMev = this.peakStrips.get(i).getEnergy()*1000.0;
            double energyLog = Math.log(energyMev);
            this.peakMoment  += dist*dist*dist*dist*energyLog;
            this.peakMoment2 += dist*dist*energyLog;
            this.peakMoment3 += energyLog*dist*dist*dist;
        }
        
        this.peakMoment = this.peakMoment/logSumm;
        
        if(this.peakMoment2<0.0000000001){
            this.peakMoment2 = 4.5/12.0;
        } else {
            this.peakMoment2 = this.peakMoment2/logSumm;
        }
        double sigma3    = Math.sqrt(this.peakMoment2);
        this.peakMoment3 = this.peakMoment3/logSumm/(sigma3*sigma3*sigma3);
        this.peakMoment = this.peakMoment/logSumm/(sigma3*sigma3*sigma3*sigma3);
    }
    
    public double getDistanceEdge(){
        return this.peakDistanceEdge;
    }
    
    public double getMoment(){
        return this.peakMoment;
    }
    
    public double getMoment2(){
        return this.peakMoment2;
    }
    
    public double getMoment3(){
        return this.peakMoment3;
    }
    
    public int getMultiplicity(){
        return this.peakStrips.size();
    }
    
    public int getCoord(){
        double energy_summ = 0.0;
        double energy_norm = 0.0;
        for(ECStrip strip : this.peakStrips){
            energy_summ += strip.getEnergy();
	        int str = strip.getDescriptor().getComponent() - 1;
	        str = str*8+4;
            energy_norm += strip.getEnergy()*str;
        }        
        return (int) (energy_norm/energy_summ);
    }
    
    private double integral_old(int strip, boolean left_right){
        int count = 0;
        int intg  = 0;
        int value = this.peakStrips.get(strip).getADC();
        if(!left_right){
            for(int i = strip + 1; i < this.peakStrips.size(); i++){
                count++;
                intg += this.peakStrips.get(i).getADC();
            }
            
        } else {
            for(int i = strip - 1; i >=0; i--){
                count++;
                intg += this.peakStrips.get(i).getADC();
            }
        }
        double norm = ((double) intg) - ((double) value)*count;
        return norm;
    }
    
    private double integral(int strip, boolean pm){ //pm=true/false: adc summed for strip indices to the +/- of input strip
        int count = 0, intg = 0;
        int value = this.peakStrips.get(strip).getADC();
        for(int i = (pm)?0:strip+1; i < ((pm)?strip:this.peakStrips.size());i++) {count++;intg += this.peakStrips.get(i).getADC();}        
        return ((double) intg) - ((double) value)*count;        
    }
    
    public List<ECPeak>  splitPeak(int strip){
        
        List<ECPeak>  twoPeaks = new ArrayList<ECPeak>();
        ECPeak  leftPeak = new ECPeak(this.peakStrips.get(0));
        for(int i = 1; i < strip; i++) { leftPeak.addStrip(this.peakStrips.get(i));}
        ECPeak  rightPeak = new ECPeak(this.peakStrips.get(strip));
        for(int i = strip+1; i < peakStrips.size(); i++) { rightPeak.addStrip(this.peakStrips.get(i));}
        twoPeaks.add(leftPeak);
        twoPeaks.add(rightPeak);
        return twoPeaks;
    }
    
    public int getSplitStrip(){
        int     split = -1;
        double  ratio = 0.0;
        
        if(peakStrips.size()>3){
            for(int i = 1; i < peakStrips.size()-1; i++){
                double left  = this.integral(i, false);
                double right = this.integral(i, true);
                double lf_ratio = left/right;
                if(ECCommon.debugSplit) {
                	int  s = peakStrips.get(i).getDescriptor().getSector();
                	int il = peakStrips.get(i).getDescriptor().getLayer(); 
                    double oleft  = this.integral_old(i, false);
                    double oright = this.integral_old(i, true);
               	    System.out.println("getSplitStrip: "+s+" "+il+" "+i+" "+left+" "+right+" "+oleft+" "+oright);
                }
                if(left>0.0&&right>0.0&&lf_ratio>ratio){
                    split = i;
                    ratio = lf_ratio;
                }
            }
        }
        //System.out.println(" I THINK YOU SHOULD SPLIT IT at " + split);
        return split;
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("----> peak  ( %3d %3d )  ENERGY = %12.5f\n", 
                this.desc.getSector(),this.desc.getLayer(), this.getEnergy()));
        str.append(this.peakLine.toString());
        str.append("\n");
        for(ECStrip strip : this.peakStrips){
            str.append("\t\t");
            str.append(strip.toString());
            str.append("\n");
        }
        
        return str.toString();
    }
}
