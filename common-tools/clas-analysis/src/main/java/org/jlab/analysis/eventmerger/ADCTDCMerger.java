package org.jlab.analysis.eventmerger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jlab.detector.banks.RawBank;
import org.jlab.detector.banks.RawBank.OrderType;
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
    private boolean preserveHitOrder = true;
    private OrderType[] selectedOrders = {OrderType.NOMINAL, OrderType.BGADDED_NOMINAL, OrderType.BGREMOVED};
    private EventMergerConstants constants;
            
    private DataEvent       event;
    private List<DataEvent> bgEvents;
    private int run;
    
    /**
     * ADC-TDC merger tool: merges raw hits from physics and background events, 
     * accounting for pile-ups
     * @param constants 
     * @param signal physics events
     * @param bgs background events
     */
    public ADCTDCMerger(EventMergerConstants constants, DataEvent signal, DataEvent... bgs) {
        this.constants = constants;
        this.event = signal;
        this.bgEvents = new ArrayList<>();
        this.bgEvents.addAll(Arrays.asList(bgs));
        if(!bgEvents.isEmpty() && bgEvents.get(0).hasBank("RUN::config"))
            run = bgEvents.get(0).getBank("RUN::config").getInt("run", 0);                
    }

    /**
     * Set pile-up operation mode:
     * keep first hit in time if true or both if false
     * @param value
     */
    public void setSuppressDoubleHits(boolean value) {
        this.suppressDoubleHits = value;
    }

    /**
     * Set flag to control the final hit list order
     * @param value
     */
    public void setPreserveHitOrder(boolean value) {
        this.preserveHitOrder = value;
    }

    /**
     * Set list or order types to save
     * @param orders
     */
    public void setSelectedOrders(OrderType[] orders) {
        this.selectedOrders = orders;
    }
              
    /**
     * Reads  ADC bank and returns list of hits
     * 
     * @param detector: detector identifier
     * @param bank: selected DataBank
     * @return list of ADC hits
     */
    public List<DGTZ> readADCs(DetectorType detector,DataBank bank) {
        List<DGTZ> adcStore   = new ArrayList<>();
                        
        if(bank!=null) {
            for (int i = 0; i < bank.rows(); i++) {
                ADC adcData = new ADC(detector);
                adcData.readFromBank(bank, i);

                if(!adcData.isGood()) adcData.remove();
                
                adcStore.add(adcData);
            }
            if(debug) System.out.println("Reading bank " + detector.getName() + "::adc with " + bank.rows() + ", collected " + adcStore.size() + " hits");
        }
        
        return adcStore;
    }
     
    /**
     * Read TDC bank and return list of hit
     * 
     * @param detector: detector identifier
     * @param bank: selected DataBank  
     * @return list of TDC hits
     */
    public List<DGTZ> readTDCs(DetectorType detector, DataBank bank) {
        List<DGTZ> tdcStore   = new ArrayList<>();
                        
        if(bank!=null) {
            for (int i = 0; i < bank.rows(); i++) {
                TDC tdcData = new TDC(detector);
                tdcData.readFromBank(bank, i);
                
                if(!tdcData.isGood()) tdcData.remove();
                
                tdcStore.add(tdcData);
            }
        }
        return tdcStore;
    }
    
    /**
     * Merge ADC banks for data (signal) and background events for selected detector
     * In case of multiple hit on same detector element, only first hit in time is kept 
     * unless the double-hit suppression flag, suppressDoubleHits, is set to false
     *
     * @param detector
     * @return
     */
    public DataBank mergeADCs(DetectorType detector){
        
        DataEvent bg = bgEvents.get(0);

        String ADCString = detector.getName()+"::adc";
        if(!bg.hasBank(ADCString)) {
            return event.getBank(ADCString);
        }
        else {
            List<DGTZ> bgADCs  = readADCs(detector,bg.getBank(ADCString));
            List<DGTZ> ADCs    = readADCs(detector,event.getBank(ADCString));

            DataBank bank = this.writeToBank(event, ADCString, this.merge(ADCs, bgADCs));

            return bank;
        }
    }
    
    /**
     * Merge TDC banks for data (signal) and background events for selected detector
     * Use two background events shifted in time to extend the time range of the backgrounds
     * Multiple hits on the same components are kept if time distance exceed the holdoff time
     * 
     * @param detector
     * @return
     */
    public DataBank mergeTDCs(DetectorType detector){
        
        String TDCString = detector+"::tdc";
        
        // if the primary background event has no detector bank then keep the event bank
        if(!bgEvents.get(0).hasBank(TDCString)) {
            return event.getBank(TDCString);
        }
        // if the primary background events has the detector bank, then proceed with merging
        else {                    
            // get background hits using multiple events dependending on detector
            int bgSize = constants.getInt(detector, EventMergerEnum.MERGE_SIZE);
            if(!event.hasBank(TDCString)) bgSize = 1;
            // collect bg hits
            List<DGTZ> bgTDCs = new ArrayList<>();
            for(int i=0; i<Math.min(bgSize, bgEvents.size()); i++) {
                DataEvent bg = bgEvents.get(i);
                if(bg.hasBank(TDCString)) {
                    // get TDCs, correct them for jitter and shift them in time
                    int jitter = this.getTDCJitter(detector, bg);
                    for(DGTZ dgtz : readTDCs(detector, bg.getBank(TDCString))) {
                        TDC tdc = (TDC) dgtz;
                        int layer  = tdc.getLayer();
                        int comp   = tdc.getComponent();
                        int offset = constants.getInt(run, detector, EventMergerEnum.READOUT_WINDOW, 0, layer, comp);
                        tdc.shift(jitter-i*offset);
                        bgTDCs.add(tdc);
                    } 
                }
            }
            
            // get physics event hits hits
            List<DGTZ> TDCs = readTDCs(detector, event.getBank(TDCString));

            // merge physics and bg hit
            List<DGTZ> mergedTDCs = this.merge(TDCs, bgTDCs);
            
            // create output bank
            return this.writeToBank(event, TDCString, mergedTDCs);
        }
    }    

    /**
     * Merge physics signals hit with background hits
     * @param signal
     * @param background
     * @return merged list
     */
    public final List<DGTZ> merge(List<DGTZ> signal, List<DGTZ> background) {
                
        for(DGTZ b : background) b.markAsBackground();
        
        List<DGTZ> all = new ArrayList<>();
        all.addAll(signal);
        all.addAll(background);
        Collections.sort(all);
        
        List<DGTZ> merged = new ArrayList<>();
        
        for(int i = 0; i < all.size(); i++) {
            DGTZ dgtz = all.get(i);
            if(dgtz.isRemoved()) {
                continue;
            }           
            else if(merged.isEmpty()) {
                if(debug) {
                    System.out.println("\tSkipping DGTZ " + i +"\t");
                    System.out.println(dgtz);
                }
                merged.add(dgtz);
            }
            else {
                DGTZ dgtzOld = merged.get(merged.size()-1);
                if(!dgtz.pilesUp(dgtzOld) || !this.suppressDoubleHits) {
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
        
        if(this.preserveHitOrder || signal.isEmpty()) {
            all.clear();
            all.addAll(signal);
            all.addAll(background);
        }
        return all;
    }

    /**
     * Write list of hits to bank based on hit status
     * @param event hipo event
     * @param name  bank name
     * @param dgtzs list of hits
     * @return hipo bank
     */
    public final DataBank writeToBank(DataEvent event, String name, List<DGTZ> dgtzs) {
        int size = 0;
        for(DGTZ dgtz : dgtzs) {
            if(dgtz.status()) size++;
        }
        DataBank bank = event.createBank(name, size);
        int row = 0;
        for (DGTZ dgtz : dgtzs) {
            if(dgtz.status()) 
                dgtz.addToBank(bank, row++);
        }
        return bank;
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
        public boolean isInTime() {
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

    public class DGTZ extends DetectorDescriptor {

        private boolean background = false;
        private boolean removed = false;

        public DGTZ(DetectorType detector) {
            super(detector);
        }

        public DGTZ(DetectorType detector, byte sector, byte layer, short component, byte order) {
            super(detector);
            this.setSectorLayerComponent(sector, layer, component);
            this.setOrder(order);
            this.removed    = false;
            this.background = false;
        }

        public boolean isGood() {
            return true;
        }

        public boolean isRemoved() {
            return removed;
        }

        public void remove() {
            this.removed = true;
        }

        public boolean isBackground() {
            return background;
        }

        public void markAsBackground() {
            this.background = true;
        }

        public boolean status() {
            if(!this.isInTime())
                return false;
            else if(this.isBackground() && this.isRemoved())
                return false;
            else {
                for(OrderType o : selectedOrders) {
                    if(this.getOrderType()==o)
                        return true;
                }
            }
            return false;
        }

        public RawBank.OrderType getOrderType() {
            if(this.isBackground())
                return RawBank.OrderType.BGADDED_NOMINAL;
            else if(this.isRemoved())
                return RawBank.OrderType.BGREMOVED;
            else
                return RawBank.OrderType.NOMINAL;

        }

        public int getLabeledOrder() {
            return this.getOrder()+this.getOrderType().getTypeId();
        }

        public void addToBank(DataBank bank, int row) {
            bank.setByte("sector",     row, (byte)  this.getSector());
            bank.setByte("layer",      row, (byte)  this.getLayer());
            bank.setShort("component", row, (short) this.getComponent());
            bank.setByte("order",      row, (byte)  this.getLabeledOrder()); 
        }

        public void readFromBank(DataBank bank, int row) {
            byte sector     = bank.getByte("sector",     row);
            byte layer      = bank.getByte("layer",      row);
            short component = bank.getShort("component", row);
            byte order      = bank.getByte("order",      row);
            this.setSectorLayerComponent(sector, layer, component);
            this.setOrder(order);
        }

        public boolean pilesUp(DGTZ o){
            return  this.getSector()    == o.getSector()    &&
                    this.getLayer()     == o.getLayer()     &&
                    this.getComponent() == o.getComponent() &&
                    this.getOrder()     == o.getOrder();
        }

        public boolean isInTime() {
            return true;
        }

        @Override
        public String toString() {
            String s = "Sector/Layer/Component/Order: " + this.getSector() + 
                                                    "/" + this.getLayer() + 
                                                    "/" + this.getComponent() + 
                                                    "/" + this.getOrder();
            s += " Good/Bg/Rm:" +this.isGood() + "/" + this.isBackground() + "/" + this.isRemoved();
            return s;
        }
    }

}
