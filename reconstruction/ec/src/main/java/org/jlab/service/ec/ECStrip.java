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
    
    private DetectorDescriptor  desc = new DetectorDescriptor(DetectorType.ECAL);
    
    private int                 iADC  = 0;
    private int                 iTDC  = 0;
    private double              iGain = 1.0;
    private double              iADC_to_MEV  = 1.0/10000.0;
    private double              iTDC_to_NSEC = 1.0;
    private double              iAttenLengthA = 1.0;
    private double              iAttenLengthB = 50000.0;
    private double              iAttenLengthC = 0.0;
    private int                 peakID        = -1;
    
    private Line3D              stripLine = new Line3D();
    private double              stripDistanceEdge = 0.0;
    
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
    
    public void setDistanceEdge(double dist){
        this.stripDistanceEdge = dist;
    }
    
    public double getDistanceEdge(){
        return this.stripDistanceEdge;
    }
    
    public double getEnergy(){
        return this.iADC*this.iGain*this.iADC_to_MEV;
    }
    
    public double getTime(){
        return this.iTDC*this.iTDC_to_NSEC;
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
    
    public double getEnergy(Point3D point){
        double dist = point.distance(this.stripLine.end());
        double ecorr  =  this.iAttenLengthA*Math.exp(-dist/this.iAttenLengthB) + this.iAttenLengthC;
        double energy = this.iADC*this.iGain*this.iADC_to_MEV/ecorr;
        return energy;
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
