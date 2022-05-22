/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.detector.decode;

import java.util.ArrayList;
import java.util.List;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.helicity.HelicityBit;
import org.jlab.detector.helicity.HelicityState;
import org.jlab.utils.data.DataUtils;

/**
 *
 * @author gavalian
 */
public class DetectorDataDgtz implements Comparable<DetectorDataDgtz> {
    
    private final List<ADCData>       adcStore   = new ArrayList<ADCData>();
    private final List<TDCData>       tdcStore   = new ArrayList<TDCData>();
    private final List<VTPData>       vtpStore   = new ArrayList<VTPData>();
    private final List<SCALERData> scalerStore   = new ArrayList<SCALERData>();
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
    
    public DetectorDataDgtz addVTP(VTPData vtp){
        this.vtpStore.add(vtp);
        return this;
    }
    
    public DetectorDataDgtz addSCALER(SCALERData scaler){
        this.scalerStore.add(scaler);
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
        
        for(VTPData data : this.vtpStore){
            str.append(data);
        }
        
        for(SCALERData data : this.scalerStore){
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
    
    public VTPData getVTPData(int index){
        return this.vtpStore.get(index);
    }
    
    public SCALERData getSCALERData(int index){
        return this.scalerStore.get(index);
    }
    
    public int getADCSize(){
        return this.adcStore.size();
    }
    
    public int getTDCSize(){
        return this.tdcStore.size();
    }
    
    public int getVTPSize(){
        return this.vtpStore.size();
    }
    
    public int getSCALERSize(){
        return this.scalerStore.size();
    }
    
    @Override
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
   
    public static List<DetectorDataDgtz>  getDataADC(List<DetectorDataDgtz> list,DetectorType type, int sector){
        List<DetectorDataDgtz> filtered = new ArrayList<DetectorDataDgtz>();
        for(DetectorDataDgtz dgtz : list){
            if(dgtz.getDescriptor().getType()==type&&dgtz.getDescriptor().getSector()==sector){
                filtered.add(dgtz);
                if(dgtz.getADCSize()>0){
                    filtered.add(dgtz);
                }
            }
        }
        return filtered;        
    }
    
    public static List<DetectorDataDgtz>  getDataADC(List<DetectorDataDgtz> list,DetectorType type, int sector, int layer){
        List<DetectorDataDgtz> filtered = new ArrayList<DetectorDataDgtz>();
        for(DetectorDataDgtz dgtz : list){
            if(dgtz.getDescriptor().getType()==type&&dgtz.getDescriptor().getSector()==sector&&
                    dgtz.getDescriptor().getLayer()==layer){
                if(dgtz.getADCSize()>0){
                    filtered.add(dgtz);
                }
            }
        }
        return filtered;        
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
        private long   pulseTimeStamp  = 0L;

        
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
         * returns array of the ADC pulse
         * @return 
         */
        public short[] getPulseArray(){
            return this.adcPulse.get(0);
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
        
        public long  getTimeStamp(){
            return this.pulseTimeStamp;
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
        
        public ADCData setTimeStamp(long timeStamp){
            this.pulseTimeStamp = timeStamp;
            return this;
        }
        
        public ADCData setTimeWord(int timeWord){
//            pulseTimeCourse = DataUtils.getInteger(timeWord, 0, 5);
//            int timeFine    = DataUtils.getInteger(timeWord, 6, 16);
            int timeFine    = DataUtils.getInteger(timeWord, 0, 5);
            pulseTimeCourse = DataUtils.getInteger(timeWord, 6, 15);
            pulseTime = pulseTimeCourse*4.0 + timeFine*0.0625;
            return this;
        }
        

        public ADCData setTime(int time){
            this.pulseTime = ((double) time);
            this.pulseTimeCourse = time;
            return this;
        }
        
        public ADCData setOrder(int order){ adcOrder = order; return this;}
        
        @Override
        public String toString(){
            int nsamples = 0;
            if(this.adcPulse.isEmpty()==false) nsamples = adcPulse.get(0).length;
            StringBuilder str = new StringBuilder();
            if(nsamples>0){
                str.append("PULSE : ");
                for(int i = 0; i < nsamples; i++){
                    str.append(String.format("%5d ", adcPulse.get(0)[i]));
                }
            } else {
            str.append(String.format("ADC (%d) (L=%6d) : %5d %5d %5d  time = %5d  %9.4f  max = %5d",
                    getOrder(), nsamples,
                    getIntegral(),getADC(), getPedestal(), getTimeCourse(), getTime(), getHeight()));
            }
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
    
    /**
    * a class to hold Helicity decoder data data
    */
    
    public static class HelicityDecoderData implements Comparable<HelicityDecoderData>{
        
        private HelicityState helicityState   = null;              // helicity, pair and pattern
        private HelicityBit   tSettle         = HelicityBit.UDF;   // tSettle state
        private HelicityBit   helicityPattern = HelicityBit.UDF;   // helicity at start of patterm
        private byte polarity = 0;                                 // pattern polarity (XOR of 3,4) 
        private byte patternPhaseCount = 0;                        // state position within pattern         
        private long timestamp = 0;                                // event timestamp in 4 ns units
        private int  helicitySeed = 0;                             // helicity random seed
        private int  nTStableRisingEdge = 0;                       // count of tStable rising edges
        private int  nTStableFallingEdge = 0;                      // count of tStable falling edges
        private int  nPattern = 0;                                 // count of Pattern 
        private int  nPair = 0;                                    // count of Pair 
        private int  tStableStart = 0;                             // time since last tStable start 
        private int  tStableEnd = 0;                               // time since last tStable end 
        private int  tStableTime = 0;                              // duration of last tStable
        private int  tSettleTime = 0;                              // duration of last tSettle 
        private int  patternWindows = 0;                           // last 32 pattern windows 
        private int  pairWindows = 0;                              // last 32 pair windows 
        private int  helicityWindows = 0;                          // last 32 helicity windows
        private int  helicityPatternWindows = 0;                   // last 32 helicity at start of pattern windows
              
        public HelicityDecoderData() {}

        public HelicityDecoderData(byte helicity, byte pair, byte pattern) {
            this.setHelicityState(new HelicityState(HelicityBit.create(helicity), 
                                                    HelicityBit.create(pair), 
                                                    HelicityBit.create(pattern)));
        }

        public HelicityState getHelicityState() {
            return helicityState;
        }

        public void setHelicityState(HelicityState helicityState) {
            this.helicityState = helicityState;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public int getHelicitySeed() {
            return helicitySeed;
        }

        public void setHelicitySeed(int helicitySeed) {
            this.helicitySeed = helicitySeed;
        }

        public int getNTStableRisingEdge() {
            return nTStableRisingEdge;
        }

        public void setNTStableRisingEdge(int nTStableRisingEdge) {
            this.nTStableRisingEdge = nTStableRisingEdge;
        }

        public int getNTStableFallingEdge() {
            return nTStableFallingEdge;
        }

        public void setNTStableFallingEdge(int nTStableFallingEdge) {
            this.nTStableFallingEdge = nTStableFallingEdge;
        }

        public int getNPattern() {
            return nPattern;
        }

        public void setNPattern(int nPattern) {
            this.nPattern = nPattern;
        }

        public int getNPair() {
            return nPair;
        }

        public void setNPair(int nPair) {
            this.nPair = nPair;
        }

        public int getTStableStart() {
            return tStableStart;
        }

        public void setTStableStart(int tStableStart) {
            this.tStableStart = tStableStart;
        }

        public int getTStableEnd() {
            return tStableEnd;
        }

        public void setTStableEnd(int tStableEnd) {
            this.tStableEnd = tStableEnd;
        }

        public int getTStableTime() {
            return tStableTime;
        }

        public void setTStableTime(int tStableTime) {
            this.tStableTime = tStableTime;
        }

        public int getTSettleTime() {
            return tSettleTime;
        }

        public void setTSettleTime(int tSettleTime) {
            this.tSettleTime = tSettleTime;
        }

        public HelicityBit getTSettle() {
            return tSettle;
        }

        public void setTSettle(byte tSettle) {
            this.tSettle = HelicityBit.create(tSettle);
        }

        public HelicityBit getHelicityPattern() {
            return helicityPattern;
        }

        public void setHelicityPattern(byte helicityPattern) {
            this.helicityPattern = HelicityBit.create(helicityPattern);
        }

        public byte getPolarity() {
            return polarity;
        }

        public void setPolarity(byte polarity) {
            this.polarity = polarity;
        }

        public byte getPatternPhaseCount() {
            return patternPhaseCount;
        }

        public void setPatternPhaseCount(byte patternPhaseCount) {
            this.patternPhaseCount = patternPhaseCount;
        }

        public int getPatternWindows() {
            return patternWindows;
        }

        public void setPatternWindows(int patternWindows) {
            this.patternWindows = patternWindows;
        }

        public int getPairWindows() {
            return pairWindows;
        }

        public void setPairWindows(int pairWindows) {
            this.pairWindows = pairWindows;
        }

        public int getHelicityWindows() {
            return helicityWindows;
        }

        public void setHelicityWindows(int helicityWindows) {
            this.helicityWindows = helicityWindows;
        }

        public int getHelicityPatternWindows() {
            return helicityPatternWindows;
        }

        public void setHelicityPatternWindows(int helicityPatternWindows) {
            this.helicityPatternWindows = helicityPatternWindows;
        }
        
        
        @Override
        public String toString(){
            String s = "HelicityDecoder : " + getHelicityState().toString();
            return s;
        }

        public int compareTo(HelicityDecoderData o) {
            if(this.getTimestamp()<o.getTimestamp()) return -1;
            return 1;
        }     
    }
    
    
    /**
    * a class to hold VTP data
    */
    
    public static class VTPData implements Comparable<VTPData>{
        
        private int vtpOrder = 0;
        private int  vtpWord = 0;
        
        public VTPData() {}
        public VTPData(int word) { this.vtpWord = word;}
        public int  getWord() { return this.vtpWord;}
        public int getOrder() { return vtpOrder;}
        public VTPData  setOrder(int order) { vtpOrder = order;return this;}
        public VTPData  setWord(int   word) {  vtpWord =  word;return this;}
        
        @Override
        public String toString(){
            return String.format("VTP (%d) : %d", getOrder(),getWord());
        }

        public int compareTo(VTPData o) {
            if(this.getOrder()<o.getOrder()) return -1;
            return 1;
        }     
    }
    
    public static class SCALERData implements Comparable<SCALERData>{
        
        private int  scalerOrder = 0;
        private byte helicity    = -1;
        private byte quartet     = -1;
        private long value       = 0;

        
        
        public SCALERData() {}

        public byte getHelicity() {
            return helicity;
        }

        public byte getQuartet() {
            return quartet;
        }

        public long getValue() {
            return value;        
        }
        
        public int getOrder() { return scalerOrder;}
        
        public SCALERData  setOrder(int order) { scalerOrder = order; return this;}

        public SCALERData setHelicity(byte scaler) {
            this.helicity = scaler;
            return this;
        }

        public SCALERData setQuartet(byte quartet) {
            this.quartet = quartet;
            return this;
        }

        public SCALERData setValue(long value) {
            this.value = value;
            return this;
        }
        
        @Override
        public String toString(){
            return String.format("SCALER (%d): %d \t %d \t %d", getOrder(),getHelicity(),getQuartet(),getValue());
        }

        public int compareTo(SCALERData o) {
            if(this.getOrder()<o.getOrder()) return -1;
            return 1;
        }        
    }
}
