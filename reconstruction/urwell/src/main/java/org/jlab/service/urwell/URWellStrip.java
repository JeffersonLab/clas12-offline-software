package org.jlab.service.urwell;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.geom.prim.Line3D;
import org.jlab.io.base.DataEvent;

/**
 * Add description
 * 
 * @author bondi, devita
 */

// remove add methods as needed
// read ADC bank
public class URWellStrip implements Comparable {
    
    private DetectorDescriptor  desc = new DetectorDescriptor(DetectorType.URWELL);

    private int          ADC = 0;
    private int          TDC = 0;
    private int           id = -1;       // ID of the hit. this shows the row number of the corresponding hit in the ADC bank
    private int    clusterId = -1;       // Id (row number) of the cluster that this hit belongs to
    
    private Line3D stripLine = new Line3D();    
    
    private double    energy = 0;   // decide whether is necesary to have these variables or if they can just be calculated
    private double      time = 0;
    
    
    public URWellStrip(int sector, int layer, int component){
        this.desc.setSectorLayerComponent(sector, layer, component);
    }

    public URWellStrip(int sector, int layer, int component, int ADC, int TDC){
        this.desc.setSectorLayerComponent(sector, layer, component);
        this.ADC = ADC;
        this.TDC = TDC;
        // calculate energy and time
    }

    public DetectorDescriptor getDescriptor() {
        return desc;
    }

    public void setDescriptor(DetectorDescriptor desc) {
        this.desc = desc;
    }

    public int getADC() {
        return ADC;
    }

    public void setADC(int ADC) {
        this.ADC = ADC;
    }

    public int getTDC() {
        return TDC;
    }

    public void setTDC(int TDC) {
        this.TDC = TDC;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClusterId() {
        return clusterId;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    public Line3D getLine() {
        return stripLine;
    }

    public void setLine(Line3D stripLine) {
        this.stripLine = stripLine;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }
    
    public boolean isNeighbour(URWellStrip strip){
        if(strip.getDescriptor().getSector()==this.desc.getSector()&&
           strip.getDescriptor().getLayer()==this.desc.getLayer()){
           if(Math.abs(strip.getDescriptor().getComponent()-this.desc.getComponent())<=1) return true;
        }
        return false;
    }
    
    public boolean isInTime(URWellStrip strip) {
        if (Math.abs(this.getTime() - strip.getTime()) < URWellConstants.COINCTIME) return true;
        return false;
    }     
    
    @Override
    public int compareTo(Object o) {
        URWellStrip ob = (URWellStrip) o;
        if(ob.getDescriptor().getSector()     < this.desc.getSector())    return  1;
        if(ob.getDescriptor().getSector()     > this.desc.getSector())    return -1;
        if(ob.getDescriptor().getLayer()      < this.desc.getLayer())     return  1;
        if(ob.getDescriptor().getLayer()      > this.desc.getLayer())     return -1;
        if(ob.getDescriptor().getComponent() <  this.desc.getComponent()) return  1;
        if(ob.getDescriptor().getComponent() == this.desc.getComponent()) return  0;
        return -1;
    }
    
    public static List<URWellStrip> getStrips(DataEvent event, ConstantsManager ccdb) {
        List<URWellStrip> hits = new ArrayList<>();
//        if(event.hasBank("ECAL::adc")==true){
//            DataBank bank = event.getBank("ECAL::adc");
//            for(int i = 0; i < bank.rows(); i++){
//                int  is = bank.getByte("sector", i);
//                int  il = bank.getByte("layer", i); 
//                int  ip = bank.getShort("component", i);
//                int adc = bank.getInt("ADC", i);
//                float t = bank.getFloat("time", i) + (float) tmf.getDoubleValue("offset",is,il,ip) // FADC-TDC offset (sector, layer, PMT)
//                                                   + (float)  fo.getDoubleValue("offset",is,il,0); // FADC-TDC offset (sector, layer) 
//                
//		        if (status.getIntValue("status",is,il,ip)==3) continue;    
//		    
//                ECStrip  strip = new ECStrip(is, il, ip); 
//                
//                strip.setADC(adc);
//                strip.setTriggerPhase(triggerPhase);
//                strip.setID(i+1);
//                
//                double sca = (is==5)?AtoE5[ind[il-1]]:AtoE[ind[il-1]]; 
//                if (variation=="clas6") sca = 1.0;  
//                
//                if(strip.getADC()>sca*ECCommon.stripThreshold[ind[il-1]]) strips.add(strip); 
//                
//                float  tmax = 1000; int tdc = 0;
//                
//                if (tdcs.hasItem(is,il,ip)) {
//                    float radc = (float)Math.sqrt(adc);
//                    for (float tdcc : tdcs.getItem(is,il,ip)) {
//                         float tdif = tps*tdcc - (float)gtw.getDoubleValue("time_walk",is,il,0)/radc - triggerPhase - FTOFFSET - t; 
//                        if (Math.abs(tdif)<TMFCUT&&tdif<tmax) {tmax = tdif; tdc = (int)tdcc;}
//                    }
//                    strip.setTDC(tdc); 
//                }              
//            }
//        }         
        return hits;
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("----> strip (%3d %3d %3d) ADC/TDC  %5d %5d  ENERGY = %8.5f TIME = %8.5f ", 
                this.desc.getSector(),this.desc.getLayer(),this.desc.getComponent(),
                this.ADC,this.TDC,this.getEnergy(),this.getTime()));
        return str.toString();
    }
}
