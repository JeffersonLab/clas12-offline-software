/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
	
	private static final double coincTIME = 25.; //ns. 	
	private DetectorDescriptor  desc = new DetectorDescriptor(DetectorType.ECAL);
    
    private int                 iADC  = 0;
    private int                 iTDC  = 0;
    private double              iGain = 1.0;
    private double              iADC_to_MEV  = 1.0/10000.0;
    private double              iTDC_to_NSEC = 1.0;
    private double              iAttenLengthA = 1.0;
    private double              iAttenLengthB = 50000.0;
    private double              iAttenLengthC = 0.0;
	private double              iTimingA0 = 0; // this is an offset in ns (before applying a1)
	private double              iTimingA1 = 1; // this is the ns -> TDC conv. factor (TDC = ns/a1)
	private double              iTimingA2 = 0; // this is the time-walk factor (time_ns = time_ns + a2/sqrt(adc))
	private double              iTimingA3 = 0; // 0
	private double              iTimingA4 = 0; // 0
	private double              veff = 160.; // Effective velocity of scintillator light (mm/ns)
	private int                 peakID        = -1;
    
    private Line3D              stripLine = new Line3D();
    
    public ECStrip(int sector, int layer, int component){
        this.desc.setSectorLayerComponent(sector, layer, component);
    }
    
    public ECStrip setADC(int adc){
        this.iADC = adc;
        return this;
    }
    
    public ECStrip setTDC(int tdc){
        this.iTDC = tdc;
        return this;
    }
    
    public double getEnergy(){
        return this.iADC*this.iGain*this.iADC_to_MEV;
    }
    
	public double getTime() {
		return this.iTDC * iTimingA1 - iTimingA0 - iTimingA2 / Math.sqrt(this.iADC);
	}  
    
    public void setPeakId(int id){
        this.peakID = id;
    }
    
    public int getPeakId(){ return this.peakID;}
    
    public void setAttenuation(double a, double b, double c){
        this.iAttenLengthA = a;
        this.iAttenLengthB = b;
        this.iAttenLengthC = c;
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
	
    public double getEnergy(Point3D point){
        double   dist = point.distance(this.stripLine.end());
        double  ecorr = this.iAttenLengthA*Math.exp(-dist/this.iAttenLengthB) + this.iAttenLengthC;
        return this.iADC*this.iGain*this.iADC_to_MEV/ecorr;
    }
    
	public double getTime(Point3D point) {
		double dist = point.distance(this.stripLine.end());
		return this.getTime() - dist / veff;
	}  
	
    public int getADC(){
        return this.iADC;
    }

    public int getTDC(){
        return this.iTDC;
    }
    
    public Line3D  getLine(){ return this.stripLine;}
    
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
	
    public DetectorDescriptor  getDescriptor(){return this.desc;}
    
    public int compareTo(Object o) {
        ECStrip ob = (ECStrip) o;
        if(ob.getDescriptor().getSector() < this.desc.getSector()) return  1;
        if(ob.getDescriptor().getSector() > this.desc.getSector()) return -1;
        if(ob.getDescriptor().getLayer()  < this.desc.getLayer()) return   1;
        if(ob.getDescriptor().getLayer()  > this.desc.getLayer()) return  -1;
        if(ob.getDescriptor().getComponent() <  this.desc.getComponent()) return  1;
        if(ob.getDescriptor().getComponent() == this.desc.getComponent()) return  0;
        return -1;
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("----> strip (%3d %3d %3d) ADC/TDC  %5d %5d  ENERGY = %12.5f", 
                this.desc.getSector(),this.desc.getLayer(),this.desc.getComponent(),
                this.iADC,this.iTDC,this.getEnergy()));
        str.append(String.format("  GAIN (%5.3f) ATT (%12.5f %12.5f %12.5f)", 
                this.iGain,this.iAttenLengthA,this.iAttenLengthB,this.iAttenLengthC));
        return str.toString();
    }
}
