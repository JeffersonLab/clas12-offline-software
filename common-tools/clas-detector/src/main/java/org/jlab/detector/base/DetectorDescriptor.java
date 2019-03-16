/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.base;


/**
 *
 * @author gavalian
 */
public class DetectorDescriptor implements Comparable<DetectorDescriptor> {

    private DetectorType  detectorType = DetectorType.UNDEFINED;
    private Integer hw_CRATE     = 0;
    private Integer hw_SLOT      = 0;
    private Integer hw_CHANNEL   = 0;
    
    private Integer dt_SECTOR    = 0;
    private Integer dt_LAYER     = 0;
    private Integer dt_COMPONENT = 0;
    
    private Integer dt_ORDER     = 0; // This is the order in the bank
    // defines ADCL,ADCR,TDCL,TDCR (1,2,3,4)
    
    public DetectorDescriptor(){
        
    }
    
    public DetectorDescriptor(DetectorType type){
        this.detectorType = type;
    }
    
    public DetectorDescriptor(String name){
        this.detectorType = DetectorType.getType(name);
    }
    
    public DetectorDescriptor getCopy(){
        DetectorDescriptor newDesc = new DetectorDescriptor(this.detectorType);
        newDesc.setCrateSlotChannel(this.getCrate(), this.getSlot(), this.getChannel());
        newDesc.setSectorLayerComponent(this.getSector(), this.getLayer(), this.getComponent());

        return newDesc;
    }
    
    public int getCrate(){ return this.hw_CRATE;}
    public int getChannel() { return this.hw_CHANNEL;}
    public int getComponent(){ return this.dt_COMPONENT;}
    public int getLayer(){ return this.dt_LAYER;}
    public int getSlot(){ return this.hw_SLOT;}
    public int getSector(){return this.dt_SECTOR;}
    public int getOrder(){ return this.dt_ORDER;}

    public void setSector(int sector){
        this.dt_SECTOR=sector;
    }
    
    public void setOrder(int order){        
        this.dt_ORDER = order;
        if(this.dt_ORDER<0||this.dt_ORDER>3){
            System.err.println("----> error : detector descriptor order must be [1..4]");
            this.dt_ORDER = 0;
        }
    }
    
    public DetectorType getType(){ return this.detectorType;}
    
    public final void setType(DetectorType type){
        this.detectorType = type;
    }
    
    public final void setCrateSlotChannel(int crate, int slot, int channel){
        this.hw_CRATE   = crate;
        this.hw_SLOT    = slot;
        this.hw_CHANNEL = channel;
    }
    
    public final void setSectorLayerComponent(int sector, int layer, int comp){
        this.dt_SECTOR = sector;
        this.dt_LAYER  = layer;
        this.dt_COMPONENT = comp;
    }
    
    
    public static int generateHashCode(int s, int l, int c){
        return  ((s<<24)&0xFF000000)|
                ((l<<16)&0x00FF0000)|(c&0x0000FFFF);
    }
    
    public int getHashCode(){
        int hash = ((this.dt_SECTOR<<24)&0xFF000000)|
                ((this.dt_LAYER<<16)&0x00FF0000)| ((this.dt_ORDER<<12) & 0x0000F000) |
                (this.dt_COMPONENT&0x00000FFF);
        return hash;
    }
    
    
    
    public void copy(DetectorDescriptor desc){
        this.hw_SLOT    = desc.hw_SLOT;
        this.hw_CRATE   = desc.hw_CRATE;
        this.hw_CHANNEL = desc.hw_CHANNEL;
        this.detectorType = desc.detectorType;
        this.dt_SECTOR    = desc.dt_SECTOR;
        this.dt_LAYER     = desc.dt_LAYER;
        this.dt_COMPONENT = desc.dt_COMPONENT;
    }
    
    public boolean compare(DetectorDescriptor desc){
        if(this.detectorType.equals(desc.detectorType)&&
                this.dt_SECTOR.equals(desc.dt_SECTOR)&&
                this.dt_LAYER.equals(desc.dt_LAYER)&&
                this.dt_COMPONENT.equals(desc.dt_COMPONENT)) return true;
        return false;
    }
    
    
    public static String getName(String base, int... ids){
        StringBuilder str = new StringBuilder();
        str.append(base);
        if(ids.length>0) str.append(String.format("_S_%d", ids[0]));
        if(ids.length>1) str.append(String.format("_L_%d", ids[1]));
        if(ids.length>2) str.append(String.format("_C_%d", ids[2]));
        return str.toString();
    }
    
    public static String getTitle(String base, int... ids){
        StringBuilder str = new StringBuilder();
        if(ids.length>0) str.append(String.format(" SECTOR %d", ids[0]));
        if(ids.length>1) str.append(String.format(" LAYER %d", ids[1]));
        if(ids.length>2) str.append(String.format(" UNIT %d", ids[2]));
        return str.toString();
    }
    
    @Override
    public String toString(){
        return String.format("D [%6s ] C/S/C [%4d %4d %4d ]  S/L/C [%4d %4d %4d ] ORDER = %2d", 
                this.detectorType.getName(),
                this.hw_CRATE,this.hw_SLOT,this.hw_CHANNEL,
                this.dt_SECTOR,this.dt_LAYER,this.dt_COMPONENT, this.dt_ORDER);
    }

    @Override
    public int compareTo(DetectorDescriptor o) {
        if(this.getType().getDetectorId()<o.getType().getDetectorId()){
            return -1;
        } else {
            return 1;
        }
        //return 0;
        //return 1;
    }
}
