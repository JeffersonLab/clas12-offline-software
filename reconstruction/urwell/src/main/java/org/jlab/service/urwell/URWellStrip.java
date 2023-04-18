package org.jlab.service.urwell;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.banks.RawDataBank;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.detector.geant4.v2.URWELL.URWellStripFactory;
import org.jlab.geom.prim.Line3D;
import org.jlab.io.base.DataEvent;

/**
 * URWell strip, defined based on ADC bank information and 3D line provided 
 * by the geometry service 
 * 
 * @author bondi, devita
 */


public class URWellStrip implements Comparable {
    
    private DetectorDescriptor  desc = new DetectorDescriptor(DetectorType.URWELL);
    
    private int      chamber = 0;
    
    private int          ADC = 0;
    private int          TDC = 0;
    private int           id = -1;       // ID of the hit. this shows the row number of the corresponding hit in the ADC bank
    private int    clusterId = -1;       // Id (row number) of the cluster that this hit belongs to
    private int       status = 0;
    
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

    public int getSector() {
        return this.desc.getSector();
    }
    
    public int getLayer() {
        return this.desc.getLayer();
    }
    
    public int getChamber() {
        return chamber;
    }

    public void setChamber(int chamber) {
        this.chamber = chamber;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    
    public boolean isNeighbour(URWellStrip strip){
        if(strip.getDescriptor().getSector()==this.desc.getSector()&&
           strip.getDescriptor().getLayer()==this.desc.getLayer()){
            int s1 = strip.getDescriptor().getComponent();
            int s2 = this.desc.getComponent();
            if(Math.abs(s1-s2)<=1 && this.getChamber()==strip.getChamber()) return true;
        }
        return false;
    }
    
    public boolean isInTime(URWellStrip strip) {
        return Math.abs(this.getTime() - strip.getTime()) < URWellConstants.COINCTIME;
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
    
    public static List<URWellStrip> getStrips(DataEvent event, URWellStripFactory factory, ConstantsManager ccdb) {
        
        List<URWellStrip> strips = new ArrayList<>();
        
        if(event.hasBank("URWELL::adc")){
            RawDataBank bank = new RawDataBank("URWELL::adc");
            bank.read(event);
            //DataBank bank = event.getBank("URWELL::adc");
            for(int i = 0; i < bank.rows(); i++){
                int  sector = bank.getByte("sector", i);
                int   layer = bank.getByte("layer", i); 
                int    comp = bank.getShort("component", i);
                int     adc = bank.getInt("ADC", i);
                double time = bank.getFloat("time", i);
                        
                URWellStrip  strip = new URWellStrip(sector,  layer,   comp); 
                
//                strip.setTriggerPhase(triggerPhase);
                strip.setId(i+1);
                strip.setADC(adc);
                strip.setTDC((int) time);
                strip.setEnergy(strip.ADC*URWellConstants.ADCTOENERGY);
                strip.setTime(strip.TDC*URWellConstants.TDCTOTIME);
                strip.setLine(factory.getStrip(sector, layer, comp)); 
                strip.setChamber(factory.getChamberIndex(comp)+1);
                strip.setStatus(0);
                
                if(strip.getEnergy()>URWellConstants.THRESHOLD) strips.add(strip);

            }
        }         
        return strips;
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
