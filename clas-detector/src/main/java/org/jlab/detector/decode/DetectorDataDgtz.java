/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.detector.decode;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.utils.data.DataUtils;

/**
 *
 * @author gavalian
 */
public class DetectorDataDgtz implements Comparable<DetectorDataDgtz> {
    
    private final List<ADCData>    adcStore   = new ArrayList<ADCData>();
    private final List<TDCData>    tdcStore   = new ArrayList<TDCData>();
    //private final List<ADCPulse>  pulseStore = new ArrayList<ADCPulse>();    
    private Long                 timeStamp = 0L;
    
    private final DetectorDescriptor  descriptor = new DetectorDescriptor();
    
    public DetectorDataDgtz(){
        
    }
    
    public DetectorDataDgtz(int crate, int slot, int channel){
        this.descriptor.setCrateSlotChannel(crate, slot, channel);
    }
    
    public DetectorDataDgtz addPulse(short[] data){
        this.adcStore.add(new ADCData(data));
        return this;
    }
    
    public DetectorDataDgtz addADC(ADCData adc){
        this.adcStore.add(adc);
        return this;
    }
    
    public DetectorDataDgtz addTDC(TDCData tdc){
        this.tdcStore.add(tdc);
        return this;
    }
    
    public void setTimeStamp(long time){
        this.timeStamp = time;
    }
    
    public long getTimeStamp(){
        return this.timeStamp;
    }
    
    public DetectorDescriptor getDescriptor(){
        return this.descriptor;
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(descriptor.toString());
        str.append(" -> ");
        for(ADCData data : this.adcStore){
            str.append(data);
        }
        
        for(TDCData data : this.tdcStore){
            str.append(data);
        }
        
        return str.toString();
    }

    public ADCData getADCData(int index){
        return this.adcStore.get(index);
    }
    
    public TDCData getTDCData(int index){
        return this.tdcStore.get(index);
    }
    
    public int getADCSize(){
        return this.adcStore.size();
    }
    
    public int getTDCSize(){
        return this.tdcStore.size();
    }
    
    public int compareTo(DetectorDataDgtz o) {
        /*if(this.getDescriptor().getType().getDetectorId()
                <o.getDescriptor().getType().getDetectorId()) return -1;
        */
        /*if(this.getDescriptor().getOrder()<o.getDescriptor().getOrder()){
            return -1;
        } else {
            return 1;
        }*/
        return 1;
    }
   
    /**
     * a class to hold ADC values
     */
    
    public static class ADCData implements Comparable<ADCData> {
        
        private int    adcOrder        = 0;
        private int    pulseIntegral   = 0;
        private int    pulseADC        = 0;
        private short  pulsePedestal   = 0;
        private double pulseTime       = 0;
        private int    pulseTimeCourse = 0;
        private short  pulseHeight     = 0;

        
        private List<short[]>   adcPulse = new ArrayList<short[]>();
        
        private boolean isPedistalSubtracted = false;
        
        public ADCData(){
            
        }
        
        public ADCData(short[] pulse){
            this.setPulse(pulse);
        }
        
        public final ADCData setPulse(short[] pulse){
            adcPulse.clear();
            adcPulse.add(pulse);
            return this;
        }
        /**
         * returns number of samples in the pulse
         * @return 
         */
        public int  getPulseSize(){
            if(adcPulse.isEmpty()==true) return 0;
            return adcPulse.get(0).length;
        }
        /**
         * returns adc value from the pulse
         * @param bin pulse bin
         * @return 
         */
        public short getPulseValue(int bin){
            if(adcPulse.isEmpty()==true){
                System.out.println("[ADCData] error --> does not contain a pulse");
                return (short) 0;
            }
            if(bin<0||bin>=adcPulse.get(0).length){
                System.out.println("[ADCData] error --> index out of bounds "
                + " index = " + bin + "  pulse size = " + adcPulse.get(0).length);
                return 0;
            }
            return adcPulse.get(0)[bin];
        }
        /**
         * returns fitted integral of the pulse either set by pulse
         * fitter or initialized from FPGA pulse parameters.
         * @return 
         */
        public int getIntegral(){
            return this.pulseIntegral;
        }
        
        public int getADC(){
            return this.pulseADC;
        }
        
        public int getPedestal(){
            return this.pulsePedestal;
        }
        
        public int getHeight(){
            return this.pulseHeight;
        }
        
        public double getTime(){
            return this.pulseTime;
        }
        
        public int  getTimeCourse(){
            return this.pulseTimeCourse;
        }
        
        
        
        
        
        public int   getOrder() { return adcOrder;}
        
        public ADCData setADC(int nsa, int nsb){
            this.pulseADC = this.pulseIntegral - this.pulsePedestal*(nsa+nsb);
            return this;
        }
        
        public ADCData setPedestal(short ped){
            this.pulsePedestal = ped;
            return this;
        }
        
        public ADCData setHeight(short max){
            this.pulseHeight = max;
            return this;
        }
        
        public ADCData setIntegral(int integral){
            this.pulseIntegral = integral;
            return this;
        }
        
        public ADCData setTimeWord(int timeWord){
            pulseTimeCourse = DataUtils.getInteger(timeWord, 0, 5);
            int timeFine    = DataUtils.getInteger(timeWord, 6, 16);
            pulseTime = pulseTimeCourse*4.0 + timeFine*0.0625;
            return this;
        }
        

        
        public ADCData setOrder(int order){ adcOrder = order; return this;}
        
        @Override
        public String toString(){
            StringBuilder str = new StringBuilder();
            str.append(String.format("ADC (%d) : %5d %5d %5d  time = %5d  %9.4f  max = %5d",
                    getOrder(),
                    getIntegral(),getADC(), getPedestal(), getTimeCourse(), getTime(), getHeight()));
            return str.toString();
        }
                
        public int compareTo(ADCData o) {
            if(getOrder()<o.getOrder()) return -1;
            return 1;
        }
    }
    
   /**
    * a class to hold TDC data
    */
    
    public static class TDCData implements Comparable<TDCData>{
        
        private int   tdcOrder = 0;
        private int tdcTime = 0;
        
        public TDCData() {}
        public TDCData(int time) { this.tdcTime = time;}
        public int getTime() { return this.tdcTime;}
        public int   getOrder() { return tdcOrder;}
        public TDCData  setOrder(int order) { tdcOrder = order;return this;}
        public TDCData  setTime(short time) { tdcTime = time;return this;}
        
        @Override
        public String toString(){
            return String.format("TDC (%d) : %5d", getOrder(),getTime());
        }

        public int compareTo(TDCData o) {
            if(this.getOrder()<o.getOrder()) return -1;
            return 1;
        }        
    }
}
