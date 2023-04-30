package org.jlab.analysis.eventmerger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 * Class for merging of ADC and TDC banks from two events
 * Operates on selectable list of detectors (default DC, FTOF)
 * 
 * @author ziegler
 * @author devita
 */

public class ADCTDCMerger {
    
    private final boolean debug = false;
    
    private boolean suppressDoubleHits = true;
    private EventMergerConstants constants;
            
    private DataEvent       event;
    private List<DataEvent> bgEvents;
    private int run;
    
    public ADCTDCMerger(EventMergerConstants constants, DataEvent signal, DataEvent... bgs) {
        this.constants = constants;
        this.event = signal;
        this.bgEvents = new ArrayList<>();
        this.bgEvents.addAll(Arrays.asList(bgs));
        if(!bgEvents.isEmpty() && bgEvents.get(0).hasBank("RUN::config"))
            run = bgEvents.get(0).getBank("RUN::config").getInt("run", 0);                
    }

    public void setSuppressDoubleHits(boolean value) {
        this.suppressDoubleHits = value;
    }
            
      
    /**
     * Reads  ADC bank and returns corresponding information
     * 
     * @param detector: detector identifier
     * @param bankDGTZ: selected DataBank
     * @return 
     */
    public List<DGTZ> ADCbank(DetectorType detector,DataBank bankDGTZ) {
        List<DGTZ> adcStore   = new ArrayList<>();
                        
        for (int i = 0; i < bankDGTZ.rows(); i++) {
            ADC adcData = new ADC(detector);
            adcData.readFromBank(bankDGTZ, i);
            
            if(adcData.isGood()) adcStore.add(adcData);
        }
        if(debug) System.out.println("Reading bank " + detector.getName() + "::adc with " + bankDGTZ.rows() + ", collected " + adcStore.size() + " hits");
        
        return adcStore;
    }
     
    /**
     * Read TDC bank and return corresponding information
     * 
     * @param detector: detector identifier
     * @param bankDGTZ: selected DataBank  
     * @return
     */
    public List<DGTZ> TDCbank(DetectorType detector, DataBank bankDGTZ) {
        List<DGTZ> tdcStore   = new ArrayList<>();
                        
        for (int i = 0; i < bankDGTZ.rows(); i++) {
            TDC tdcData = new TDC(detector);
            tdcData.readFromBank(bankDGTZ, i);
        
            if(tdcData.isGood()) tdcStore.add(tdcData);
        }
        return tdcStore;
    }
    
    /**
     * Merge TDC banks for data (signal) and background events for selected detector
     * Use two background events shifted in time to extend the time range of the backgrounds
     * Multiple hits on the same components are kept if time distance exceed the holdoff time
     * 
     * @param detector
     * @return
     */
    public DataBank getTDCBank(DetectorType detector){
        
        String TDCString = detector+"::tdc";
        
        // if the primary background event has no detector bank then keep the event bank
        if(!bgEvents.get(0).hasBank(TDCString)) {
            return event.getBank(TDCString);
        }
        // if no detector bank is found in the physics event, then keep the bank from the primary background event
        else if(!event.hasBank(TDCString)) {
            return bgEvents.get(0).getBank(TDCString);
        }  
        // if both physics and primary background events have the detector bank, then proceed with merging
        else {
                    
            // get background hits
            List<DGTZ> bgTDCs = new ArrayList<>();
            // use multiple events depending on detector 
            int bgSize = constants.getInt(detector, EventMergerEnum.MERGE_SIZE);

            for(int i=0; i<Math.min(bgSize, bgEvents.size()); i++) {

                DataEvent bg = bgEvents.get(i);

                if(bg.hasBank(TDCString)) {

                    int jitter = this.getTDCJitter(detector, bg);

                    // get TDCs, correct them for jitter and shift them in time
                    for(DGTZ dgtz : TDCbank(detector, bg.getBank(TDCString))) {
                        TDC tdc = (TDC) dgtz;
                        int value  = tdc.getTdc();
                        int layer  = tdc.getLayer();
                        int comp   = tdc.getComponent();
                        int offset = constants.getInt(run, detector, EventMergerEnum.READOUT_WINDOW, 0, layer, comp);
                        tdc.setTdc(value+jitter-i*offset);
                        bgTDCs.add(tdc);
                    } 
                }
            }
        
            List<DGTZ> TDCs    = TDCbank(detector, event.getBank(TDCString));

            List<DGTZ> allTDCs = new ArrayList<>();
            allTDCs.addAll(TDCs);
            allTDCs.addAll(bgTDCs);
            Collections.sort(allTDCs);
            
            return DGTZ.writeBank(event, TDCString, this.merge(TDCs, bgTDCs));
        }
    }    

    /**
     * Merge ADC banks for data (signal) and background events for selected detector
     * In case of multiple hit on same detector element, only first hit in time is kept 
     * unless the double-hit suppression flag, suppressDoubleHits, is set to false
     *
     * @param detector
     * @return
     */
    public DataBank getADCBank(DetectorType detector){
        
        DataEvent bg = bgEvents.get(0);

        String ADCString = detector.getName()+"::adc";
        if(!bg.hasBank(ADCString)) {
            return event.getBank(ADCString);
        }
        else if(!event.hasBank(ADCString)) {
            return bg.getBank(ADCString);
        }  
        else if(event.hasBank(ADCString) && bg.hasBank(ADCString)) {
            List<DGTZ> bgADCs  = ADCbank(detector,bg.getBank(ADCString));
            List<DGTZ> ADCs    = ADCbank(detector,event.getBank(ADCString));

            DataBank bank = DGTZ.writeBank(event, ADCString, this.merge(ADCs, bgADCs));

            return bank;
        }
        return null;
    }
    
    public final List<DGTZ> merge(List<DGTZ> signal, List<DGTZ> background) {
        
        for(DGTZ b : background) b.markAsBackground();
        
        List<DGTZ> all = new ArrayList<>();
        all.addAll(signal);
        all.addAll(background);
        Collections.sort(all);
        
        List<DGTZ> merged = new ArrayList<>();
        
        for(int i = 0; i < all.size(); i++) {
            DGTZ dgtz = all.get(i);
            if(merged.isEmpty()) {
                if(debug) {
                    System.out.println("\tSkipping DGTZ " + i +"\t");
                    System.out.println(dgtz);
                }
                merged.add(dgtz);
            }
            else {
                DGTZ dgtzOld = merged.get(merged.size()-1);
                if(!dgtz.pilesUp(dgtzOld)) {
                    if(debug) {
                        System.out.println("Keeping DGTZ " + i);
                        System.out.println(dgtz);
                    }
                    merged.add(dgtz);
                }
                else {
                    if(debug) {
                        System.out.println("\tSkipping DGTZ " + i +"\t");
                        System.out.println(dgtz);
                    }
                    dgtz.remove();
                }
            }
        }
        
        all.clear();
        all.addAll(signal);
        all.addAll(background);
        for(DGTZ dgtz : all) {
            if(!dgtz.inWindow()) dgtz.markAsOutOfTime();
        }
        return all;
    }

    private int getTDCJitter(DetectorType detector, DataEvent bg) {
        int offset = getJitterCorrection(event, detector)
                   - getJitterCorrection(bg, detector);
        return offset;
    }
    
    private int getJitterCorrection(DataEvent event, DetectorType detector) {
        // calculate the trigger time jitter correction
        int run = event.getBank("RUN::config").getInt("run", 0);        
        double tdcconv     = constants.getDouble(detector, EventMergerEnum.TDC_CONV);
        double period      = constants.getDouble(run, detector, EventMergerEnum.JITTER_PERIOD);
        int    phase       = constants.getInt(run, detector, EventMergerEnum.JITTER_PHASE);
        int    cycles      = constants.getInt(run, detector, EventMergerEnum.JITTER_CYCLES);
        
        double triggerphase=0;
        long   timestamp = event.getBank("RUN::config").getLong("timestamp", 0);
        if(cycles > 0) triggerphase=period*((timestamp+phase)%cycles);
        int    tdcjitter = (int) (triggerphase/tdcconv);
        return tdcjitter;
    }

    public class ADC extends DGTZ {
 
        private int adc;
        private float time;
        private short pedestal;
        private long timestamp;
        private int integral;
        private int amplitude;
        
        public ADC(DetectorType detector) {
            super(detector);
        }
        
        public int getAdc() {
            return adc;
        }

        public float getTime() {
            return time;
        }

        public short getPedestal() {
            return pedestal;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public int getIntegral() {
            return integral;
        }

        public int getAmplitude() {
            return amplitude;
        }
        
        @Override
        public boolean isGood() {
            if(this.getType()==DetectorType.BST || this.getType()==DetectorType.BMT) 
                return true;
            else
                return this.adc>0;          
        }

        @Override
        public void addToBank(DataBank bank, int row) {
            super.addToBank(bank, row);
            bank.setInt("ADC",    row, adc);
            bank.setFloat("time", row, time);
            bank.setShort("ped",  row, pedestal);
            if(this.getType()==DetectorType.BST)
                bank.setLong("timestamp", row, timestamp);
            else if(this.getType()==DetectorType.BMT || this.getType()==DetectorType.FMT || this.getType()==DetectorType.FTTRK) {
                bank.setLong("timestamp", row, timestamp);
                bank.setInt("integral",   row, integral);
            }
            else if(this.getType()==DetectorType.BAND)
                bank.setInt("amplitude",  row, amplitude);
        }

        @Override
        public void readFromBank(DataBank bank, int row) {
            super.readFromBank(bank, row);
            this.adc      = bank.getInt("ADC",    row);
            this.time     = bank.getFloat("time", row);
            this.pedestal = bank.getShort("ped",  row);  
            if(this.getType()==DetectorType.BST)
                this.timestamp = bank.getLong("timestamp", row);
            else if(this.getType()==DetectorType.BMT || this.getType()==DetectorType.FMT || this.getType()==DetectorType.FTTRK) {
                this.timestamp = bank.getLong("timestamp", row);
                this.integral  = bank.getInt("integral", row);
            }
            else if(this.getType()==DetectorType.BAND)
                this.amplitude = bank.getInt("amplitude", row);            
        }

        @Override
        public int compareTo(DetectorDescriptor other) {
            ADC o = (ADC) other;
            int CompSec = this.getSector()    < o.getSector()    ? -1 : this.getSector()   == o.getSector() ? 0 : 1;
            int CompLay = this.getLayer()     < o.getLayer()     ? -1 : this.getLayer()    == o.getLayer() ? 0 : 1;
            int CompCom = this.getComponent() < o.getComponent() ? -1 : this.getComponent()== o.getComponent()? 0 : 1;
            int CompOrd = this.getOrder()     < o.getOrder()     ? -1 : this.getOrder()    == o.getOrder()? 0 : 1;
            int CompTim = this.getTime()      < o.getTime()      ? -1 : this.getTime()     == o.getTime()? 0 : 1;

            int value3 = ((CompOrd == 0) ? CompTim : CompOrd);
            int value2 = ((CompCom == 0) ? value3 : CompCom);
            int value1 = ((CompLay == 0) ? value2 : CompLay);
            int value = ((CompSec == 0) ? value1 : CompSec);

            return value;
        }
        
        public void show() {
            System.out.println(this.toString() + " ADC: " + adc + " time: " + time);
        }
    }
    
    public class TDC extends DGTZ {
        
        private int tdc;
        
        public TDC(DetectorType detector) {
            super(detector);
        }

        public int getTdc() {
            return tdc;
        }

        public void setTdc(int tdc) {
            this.tdc = tdc;
        }
        
        public void shift(int offset) {
            this.tdc += offset;
        }
        
        @Override
        public boolean isGood() {
            return this.tdc>0;
        }

        @Override
        public void addToBank(DataBank bank, int row) {
            super.addToBank(bank, row);
            bank.setInt("TDC", row, tdc);
        }
        
        @Override
        public void readFromBank(DataBank bank, int row) {
            super.readFromBank(bank, row);
            this.tdc = bank.getInt("TDC", row);
        }
        
        
        @Override
        public boolean pilesUp(DGTZ other){
            TDC o = (TDC) other;
            double delta = constants.getInt(run, this.getType(), EventMergerEnum.READOUT_HOLDOFF, 0, this.getLayer(), this.getComponent())
                         / constants.getDouble(this.getType(), EventMergerEnum.TDC_CONV);                                   
            if(delta==0) delta = Double.MAX_VALUE;
                
            return  this.getSector()    == o.getSector()    &&
                    this.getLayer()     == o.getLayer()     &&
                    this.getComponent() == o.getComponent() &&
                    this.getOrder()     == o.getOrder()     &&
                    this.tdc-o.tdc<delta;
        }
        
        @Override
        public boolean inWindow() {
            double delta = constants.getInt(run, this.getType(), EventMergerEnum.READOUT_WINDOW, 0, this.getLayer(), this.getComponent());
            return (delta==0 || (this.tdc>0 && this.tdc<delta));
        }
        
        @Override
        public int compareTo(DetectorDescriptor other) {
            TDC o = (TDC) other;
            int CompSec = this.getSector()    < o.getSector()    ? -1 : this.getSector()   == o.getSector() ? 0 : 1;
            int CompLay = this.getLayer()     < o.getLayer()     ? -1 : this.getLayer()    == o.getLayer() ? 0 : 1;
            int CompCom = this.getComponent() < o.getComponent() ? -1 : this.getComponent()== o.getComponent()? 0 : 1;
            int CompOrd = this.getOrder()     < o.getOrder()     ? -1 : this.getOrder()    == o.getOrder()? 0 : 1;
            int CompTim = this.getTdc()       < o.getTdc()       ? -1 : this.getTdc()      == o.getTdc()? 0 : 1;

            int value3 = ((CompOrd == 0) ? CompTim : CompOrd);
            int value2 = ((CompCom == 0) ? value3 : CompCom);
            int value1 = ((CompLay == 0) ? value2 : CompLay);
            int value = ((CompSec == 0) ? value1 : CompSec);

            return value;
        }
        
        public void show() {
            System.out.println(this.toString() + " TDC: " + tdc);
        }
    }
 
}
