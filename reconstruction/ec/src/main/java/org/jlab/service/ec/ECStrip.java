package org.jlab.service.ec;

import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.base.DetectorType;
import org.jlab.geom.prim.Line3D;
import org.jlab.geom.prim.Point3D;

/**
 *
 * @author gavalian
 */

public class ECStrip implements Comparable {
    
	/*This is the time to consider two nearby strips (same detector view!) in time. 
	 * There's no correction (yet) for hit position along the bar
	 * However, two bars are near-by and, if the hit is really a matching hit, it is basically in the same position
	 * in the two bars. Hence, the time difference should be very small
	 */
	
	private DetectorDescriptor  desc = new DetectorDescriptor(DetectorType.ECAL);
    
    private int                iADC = 0;
    private int                iTDC = 0;
    private double            iGain = 1.0;
    private double     iADC_to_MEV  = 1.0/10000.0;
    private double    iAttenLengthA = 1.0;
    private double    iAttenLengthB = 50000.0;
    private double    iAttenLengthC = 0.0;
    private double        iTiming00 = 0; // Global TDC offset
	private double        iTimingA0 = 0; // Offset in ns (before applying a1)
	private double        iTimingA1 = 1; // ns -> TDC conv. factor (TDC = ns/a1)
	private double        iTimingA2 = 0; // time-walk factor (time_ns = time_ns + a2/sqrt(adc))
	private double        iTimingA3 = 0; // 0
	private double        iTimingA4 = 0; // 0
	private int        triggerPhase = 0;
	private double             veff = 18.1; // Effective velocity of scintillator light (cm/ns)
	private int              peakID = -1;
    private int                  id = -1;       // ID of the hit. this shows the row number of the corresponding hit in the ADC bank
    private int           clusterId = -1;       // Id (row number) of the cluster that this hit belongs to
	
	private double              tdist=0;
	private double              edist=0;
    
    private Line3D              stripLine = new Line3D();
    private double              stripDistanceEdge = 0.0;
    
	private static final double coincTIME = 25.; //ns. 	
    private double              time = 0;
    private double               gtw = 0; //global time walk correction
    private TimeCorrection        tc = null; 
    
    public ECStrip(int sector, int layer, int component){
        this.desc.setSectorLayerComponent(sector, layer, component);
        if( ECCommon.useNewTimeCal) tc = new ExtendedTWCTime();
        if(!ECCommon.useNewTimeCal) tc = new SimpleTWCTime();
    }
	
    public DetectorDescriptor getDescriptor(){
    	return this.desc;
    }
    
    public ECStrip setADC(int adc){
        this.iADC = adc;
        return this;
    }
    
    public ECStrip setTDC(int tdc){
        this.iTDC = tdc;
        return this;
    }
	
    public int getADC(){
        return this.iADC;
    }

    public int getTDC(){
        return this.iTDC;
    }
    
    public double getRawTime(){
       	return tc.getRawTime();
    }
    
    public double getPhaseCorrectedTime() { 
        return tc.getPhaseCorrectedTime();
    }
    
    public double getRawTime(boolean phaseCorrection) {
 	    return phaseCorrection ? tc.getPhaseCorrectedTime():tc.getRawTime();
    }
    
    public void setGlobalTimeWalk(double gtw) {
    	this.gtw = gtw;
    }
    
    public double getTWCTime() {
    	return tc.getTWCTime();    	
    }
    
    public double getTime() {
    	return tc.getTime();
    }
    
    abstract class TimeCorrection {
    	public abstract double getRawTime();
    	public abstract double getPhaseCorrectedTime();
    	public abstract double getTWCTime();    	
    	public abstract double getTime();
    }
    
    public class SimpleTWCTime extends TimeCorrection {    	
        public double getRawTime(){
           	return iTDC * iTimingA1;
        }
        
        public double getPhaseCorrectedTime() { 
            return iTDC * iTimingA1 - triggerPhase;
        } 
         	
        public double getTWCTime() {
        	double radc = Math.sqrt(iADC);
          	return  getPhaseCorrectedTime() - iTimingA2/radc;
        }  
        
    	public double getTime() {
        	double radc = Math.sqrt(iADC);
    		return getPhaseCorrectedTime() - iTimingA2/radc - iTimingA0;
    	}
    }
    
    public class ExtendedTWCTime extends TimeCorrection {    	
        public double getRawTime(){
           	return iTDC * iTimingA1;
        }
        
        public double getPhaseCorrectedTime() { 
            return iTDC * iTimingA1 - triggerPhase;
        } 
        
        public double getTWCTime() {
        	double radc = Math.sqrt(iADC);
          	return getPhaseCorrectedTime() - gtw/radc - iTimingA2/radc - iTimingA3 - iTimingA4/Math.sqrt(radc) - iTiming00;          	
        } 
        
    	public double getTime() {
        	double radc = Math.sqrt(iADC);
          	return getPhaseCorrectedTime() - gtw/radc - iTimingA2/radc - iTimingA3 - iTimingA4/Math.sqrt(radc) - iTimingA0 - iTiming00;          	
        }         
    } 
    
    public double getEnergy(){
        return this.iADC*this.iGain*this.iADC_to_MEV;
    }
    
    public void setDistanceEdge(double dist){
        this.stripDistanceEdge = dist;
    }
    
    public double getDistanceEdge(){
        return this.stripDistanceEdge;
    }
  
    public void setPeakId(int id){ 
    	    this.peakID = id;
    }
    
    public int getPeakId(){
      	return this.peakID;
    	}
    
    public void setID(int id){
            this.id = id;
    }
    
    public int getID(){
            return this.id;
    }
    
    public void setClusterId(int id){
            this.clusterId = id;
    }
    
    public int getClusterId(){
            return this.clusterId;
    }
        
    
    public void setAttenuation(double a, double b, double c){
        this.iAttenLengthA = a;
        this.iAttenLengthB = b;
        this.iAttenLengthC = c;
    }
    
    public void setTriggerPhase(int num) {
    	    this.triggerPhase = num;
    } 
    
    public void setVeff(double veff) {
    	   this.veff = veff;
    }
    
    public double getVeff(double veff) {
 	   return this.veff;
    }  
    
    public void setGain(double gain){
        this.iGain = gain;
    }
    
	public void setTiming(double a0, double a1, double a2, double a3, double a4) {
		this.iTimingA0 = a0;
		this.iTimingA1 = a1;
		this.iTimingA2 = a2;
		this.iTimingA3 = a3;
		this.iTimingA4 = a4;
	}  
	
	public void setGlobalTimingOffset(double val) {
		this.iTiming00 = val;
	}
	
	public double[] getTiming() {
		double[] array = new double[5];
		array[0] = this.iTimingA0;
		array[1] = this.iTimingA1;
		array[2] = this.iTimingA2;
		array[3] = this.iTimingA3;
		array[4] = this.iTimingA4;
        return array;
	}
	
    public double getEnergy(Point3D point){
        edist = point.distance(this.stripLine.end());
        return this.iADC*this.iGain*this.iADC_to_MEV/getEcorr(-edist);
    }
    
    public double getEcorr(double dist) {
        return this.iAttenLengthA*Math.exp(dist/this.iAttenLengthB) + this.iAttenLengthC;    	
    }
    
	public double getTime(Point3D point) {		
		tdist = point.distance(this.stripLine.end());
		time =  getTime() - tdist/veff;
		return time;
	} 
	
	public double getPointTime() {
		return this.time;
	}
	
	public double getEdist() {return this.edist;}
	
	public double getTdist() {return this.tdist;}
    
    public Line3D getLine()  {return this.stripLine;}
    
    public boolean isNeighbour(ECStrip strip){
        if(strip.getDescriptor().getSector()==this.desc.getSector()&&
           strip.getDescriptor().getLayer()==this.desc.getLayer()){
           if(Math.abs(strip.getDescriptor().getComponent()-this.desc.getComponent())<=1) return true;
        }
        return false;
    }
    
	public boolean isInTime(ECStrip strip) {
		if (Math.abs(this.getTime() - strip.getTime()) < ECStrip.coincTIME) return true;
		return false;
	} 
    
    public int compareTo(Object o) {
        ECStrip ob = (ECStrip) o;
        if(ob.getDescriptor().getSector()     < this.desc.getSector())    return  1;
        if(ob.getDescriptor().getSector()     > this.desc.getSector())    return -1;
        if(ob.getDescriptor().getLayer()      < this.desc.getLayer())     return  1;
        if(ob.getDescriptor().getLayer()      > this.desc.getLayer())     return -1;
        if(ob.getDescriptor().getComponent() <  this.desc.getComponent()) return  1;
        if(ob.getDescriptor().getComponent() == this.desc.getComponent()) return  0;
        return -1;
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("----> strip (%3d %3d %3d) ADC/TDC  %5d %5d  ENERGY = %8.5f TIME = %8.5f DIST = %8.5f", 
                this.desc.getSector(),this.desc.getLayer(),this.desc.getComponent(),
                this.iADC,this.iTDC,this.getEnergy(),this.getTime(),this.getTdist()));
        str.append(String.format("  GAIN (%5.3f) ATT (%12.5f %12.5f %12.5f)", 
                this.iGain,this.iAttenLengthA,this.iAttenLengthB,this.iAttenLengthC));
        return str.toString();
    }
}
