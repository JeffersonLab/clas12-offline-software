/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.analysis.eventmerger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataEvent;

/**
 * Class for merging of ADC and TDC banks from two events
 * Operates on selectable list of detectors (default DC, FTOF)
 * 
 * @author ziegler
 * @author devita
 */

public class ADCTDCMerger {
    
    private boolean debug = false;
    private boolean suppressDoubleHits = true;
    private String[] detectors;
    private EventMergerConstants constants = new EventMergerConstants();
            
    public ADCTDCMerger() {
        detectors = new String[]{"DC","FTOF"};
        printDetectors();
    }
        
    public ADCTDCMerger(String[] dets, boolean dhits) {
        suppressDoubleHits = dhits;
        System.out.println("Double hits suppression flag set to " + suppressDoubleHits);
        detectors = dets;
        printDetectors();
    }
    
    /**
     * Reads  ADC bank and returns corresponding information
     * 
     * @param detector: detector identifier string
     * @param bankDGTZ: selected DataBank
     * @return 
     */
    public List<ADC> ADCbank(String detector,DataBank bankDGTZ) {
        List<ADC> adcStore   = new ArrayList<ADC>();
        
        for (int i = 0; i < bankDGTZ.rows(); i++) {
            ADC adcData = null;
            byte sector     = bankDGTZ.getByte("sector", i);
            byte layer      = bankDGTZ.getByte("layer", i);
            short component = bankDGTZ.getShort("component", i);
            byte order      = bankDGTZ.getByte("order", i);
            int adc         = bankDGTZ.getInt("ADC", i);
            float time      = bankDGTZ.getFloat("time", i);
            short ped       = bankDGTZ.getShort("ped", i);
            if(adc<=0) continue;
            if(detector == DetectorType.BST.getName()) {
                long timestamp = (int)bankDGTZ.getLong("timestamp", i);
                adcData = new ADC(sector,layer,component,order,adc,time,ped,timestamp);
            }
            else if(detector == DetectorType.BMT.getName() || detector==DetectorType.FMT.getName() || detector==DetectorType.FTTRK.getName()) {
                long timestamp = (int)bankDGTZ.getLong("timestamp", i);
                int  integral   = bankDGTZ.getInt("integral", i);
                adcData = new ADC(sector,layer,component,order,adc,time,ped,timestamp,integral);
            }
            else if(detector == DetectorType.BAND.getName()) {
                int  amplitude   = bankDGTZ.getInt("amplitude", i);
                adcData = new ADC(sector,layer,component,order,adc,time,ped,amplitude);
            }
            else {
                adcData = new ADC(sector,layer,component,order,adc,time,ped);
            }
            adcStore.add(adcData);
        }
        
        return adcStore;
    }
     
    /**
     * Read TDC bank and return corresponding information
     * 
     * @param bankDGTZ: selected DataBank
     * @param offset:   offset to be applied to TDC values to compensate for jitter  
     * @return
     */
    public List<TDC> TDCbank(DataBank bankDGTZ, int offset) {
        List<TDC> tdcStore   = new ArrayList<TDC>();
        for (int i = 0; i < bankDGTZ.rows(); i++) {
            byte sector     = bankDGTZ.getByte("sector", i);
            byte layer      = bankDGTZ.getByte("layer", i);
            short component = bankDGTZ.getShort("component", i);
            byte order      = bankDGTZ.getByte("order", i);
            int tdc         = bankDGTZ.getInt("TDC", i);
            if(tdc<=0) continue;
            TDC tdcData = new TDC(sector,layer,component,order,tdc+offset);
            tdcStore.add(tdcData);
        }
        return tdcStore;
    }
    
    /**
     * Merge TDC banks for data (signal) and background events for selected detector
     * In case of multiple hit on same detector element, only first hit in time is kept 
     * unless the double-hit suppression flag, suppressDoubleHits, is set to false
     * 
     * @param Det
     * @param event
     * @param bg
     * @return
     */
    public DataBank getTDCBank(String Det, DataEvent event, DataEvent bg){
        
        int offset = getTDCOffset(Det, event, bg);
        
        String TDCString = Det+"::tdc";
        DataBank bank = null;
        if(event.hasBank(TDCString)==true && (bg==null || bg.hasBank(TDCString)==false)) {
            bank = event.getBank(TDCString);
            if(event.hasBank(TDCString)) { 
                HipoDataEvent de = (HipoDataEvent) event;
                de.removeBank(TDCString);
            }
        }
        else if(event.hasBank(TDCString)==false && bg.hasBank(TDCString)==true) {
            bank = bg.getBank(TDCString);
        }  
        else if(event.hasBank(TDCString)==true && bg.hasBank(TDCString)==true) {
            List<TDC> bgTDCs  = TDCbank(bg.getBank(TDCString), offset);
            List<TDC> TDCs    = TDCbank(event.getBank(TDCString), 0);

            List<TDC> allTDCs = new ArrayList<TDC>();
            for(TDC tdc : TDCs)   allTDCs.add(tdc);
            for(TDC tdc : bgTDCs) allTDCs.add(tdc);
            Collections.sort(allTDCs);
            
            List<TDC> mergedTDCs = new ArrayList<TDC>();
            for(int i = 0; i < allTDCs.size(); i++) {
                TDC tdc = allTDCs.get(i);
                if(mergedTDCs.size()==0) {
                    if(debug) {
                        System.out.println("Keeping TDC " + i);
                        tdc.show();
                    }
                    mergedTDCs.add(tdc);
                }
                else {
                    TDC tdcOld = mergedTDCs.get(mergedTDCs.size()-1);
                    if(!tdc.equalTo(tdcOld) || !suppressDoubleHits) {
                        if(debug) {
                            System.out.println("Keeping TDC " + i);
                            tdc.show();
                        }
                        mergedTDCs.add(tdc);
                    }
                    else {
                        if(debug) {
                            System.out.println("\tSkipping TDC " + i +"\t");
                            tdc.show();
                        }
                    }
                }
            } 

            if(event.hasBank(TDCString)) { 
                HipoDataEvent de = (HipoDataEvent) event;
                de.removeBank(TDCString);
            }
            bank = event.createBank(TDCString, mergedTDCs.size());
            for (int i = 0; i < mergedTDCs.size(); i++) {
                bank.setByte("sector",     i, mergedTDCs.get(i).getSector());
                bank.setByte("layer",      i, mergedTDCs.get(i).getLayer());
                bank.setShort("component", i, mergedTDCs.get(i).getComponent());
                bank.setInt("TDC",         i, mergedTDCs.get(i).getTdc());
                bank.setByte("order",      i, mergedTDCs.get(i).getOrder());
            }
        }
        return bank;
    }
    
    /**
     * Merge TDC banks for data (signal) and background events for selected detector
     * Use two background events shifted in time to extend the time range of the backgrounds
     * Multiple hits on the same components are kept if time distance exceed the holdoff time
     * 
     * @param Det
     * @param event
     * @param bg1: primary background event
     * @param bg2: secondary background event that will be shifted to negative times
     * @return
     */
    public DataBank getTDCBank(String Det, DataEvent event, DataEvent bg1, DataEvent bg2){
        
        int offset1 = getTDCOffset(Det, event, bg1);
        int offset2 = getTDCOffset(Det, event, bg2);
        
        int run = bg2.getBank("RUN::config").getInt("run", 0);

        String TDCString = Det+"::tdc";
        DataBank bank = null;
        // if no detector bank is found in the background events then keep the event bank
        if(event.hasBank(TDCString)==true && bg1.hasBank(TDCString)==false && bg2.hasBank(TDCString)==false) {
            bank = event.getBank(TDCString);
            if(event.hasBank(TDCString)) { 
                HipoDataEvent de = (HipoDataEvent) event;
                de.removeBank(TDCString);
            }
        }
        // if no detector bank is found in the phyics event, then keep the bank from the primary background event
        else if(event.hasBank(TDCString)==false && bg1.hasBank(TDCString)==true) {
            bank = bg1.getBank(TDCString);
        }  
        // if both physics and primary background events have the detector bank, then proceed with merging
        else if(event.hasBank(TDCString)==true && bg1.hasBank(TDCString)==true) {
            List<TDC> bgTDCs  = TDCbank(bg1.getBank(TDCString), offset1);
            // if secondary background event has the relevant detector bank, add the hits shifted in time
            if(bg2.hasBank(TDCString)) {
                List<TDC> bg2TDCs  = TDCbank(bg2.getBank(TDCString), offset2);
                for(TDC tdc : bg2TDCs) {
                    int value  = tdc.getTdc();
                    int layer  = tdc.getLayer();
                    int comp   = tdc.getComponent();
                    int offset = constants.getInt(run, Det, EventMergerEnum.READOUT_WINDOW, 0, layer, comp);
                    tdc.setTdc(value-offset);
                    bgTDCs.add(tdc);
                }
            }
            List<TDC> TDCs    = TDCbank(event.getBank(TDCString), 0);

            List<TDC> allTDCs = new ArrayList<TDC>();
            for(TDC tdc : TDCs)   allTDCs.add(tdc);
            for(TDC tdc : bgTDCs) allTDCs.add(tdc);
            Collections.sort(allTDCs);
            
            List<TDC> mergedTDCs = new ArrayList<TDC>();
            for(int i = 0; i < allTDCs.size(); i++) {
                TDC tdc = allTDCs.get(i);
                if(mergedTDCs.size()==0) {
                    if(debug) {
                        System.out.println("Keeping TDC " + i);
                        tdc.show();
                    }
                    mergedTDCs.add(tdc);
                }
                else {
                    TDC tdcOld = mergedTDCs.get(mergedTDCs.size()-1);
                    if(!tdc.equalTo(tdcOld) || !suppressDoubleHits) {
                        if(debug) {
                            System.out.println("Keeping TDC " + i);
                            tdc.show();
                        }
                        mergedTDCs.add(tdc);
                    }
                    else {
                        double delta = constants.getInt(run, Det, EventMergerEnum.READOUT_HOLDOFF, 0, tdc.getLayer(), tdc.getComponent())
                                     / constants.getDouble(Det, EventMergerEnum.TDC_CONV);
                         if(tdc.getTdc()-tdcOld.getTdc()<delta) {
                            if(debug) {
                                System.out.println("\tSkipping TDC " + i +"\t");
                                tdc.show();
                            }                            
                        }
                        else {
                            if(debug) {
                                System.out.println("Keeping TDC " + i);
                                tdc.show();
                            }
                            mergedTDCs.add(tdc);
                        }
                    }
                }
            } 
                
            List<TDC> filteredTDCs = new ArrayList<TDC>();
            for(int i = 0; i < mergedTDCs.size(); i++) {
                TDC tdc = mergedTDCs.get(i);
                int value = tdc.getTdc();
                int layer  = tdc.getLayer();
                int comp   = tdc.getComponent();
                if(value>0 && value<constants.getInt(run, Det, EventMergerEnum.READOUT_WINDOW, 0, layer, comp)) {
                    filteredTDCs.add(tdc);
                }
            }
            if(event.hasBank(TDCString)) { 
                HipoDataEvent de = (HipoDataEvent) event;
                de.removeBank(TDCString);
            }
            bank = event.createBank(TDCString, filteredTDCs.size());
            for (int i = 0; i < filteredTDCs.size(); i++) {
                bank.setByte("sector",     i, filteredTDCs.get(i).getSector());
                bank.setByte("layer",      i, filteredTDCs.get(i).getLayer());
                bank.setShort("component", i, filteredTDCs.get(i).getComponent());
                bank.setInt("TDC",         i, filteredTDCs.get(i).getTdc());
                bank.setByte("order",      i, filteredTDCs.get(i).getOrder());
            }
        }
        return bank;
    }

    /**
     * Merge ADC banks for data (signal) and background events for selected detector
     * In case of multiple hit on same detector element, only first hit in time is kept 
     * unless the double-hit suppression flag, suppressDoubleHits, is set to false
     *
     * @param detector
     * @param event
     * @param bg
     * @return
     */
    public DataBank getADCBank(String detector, DataEvent event, DataEvent bg){
        
        String ADCString = detector+"::adc";
        DataBank bank = null;
        if(event.hasBank(ADCString)==true && (bg==null || bg.hasBank(ADCString)==false)) {
            bank = event.getBank(ADCString);
            if(event.hasBank(ADCString)) { 
                HipoDataEvent de = (HipoDataEvent) event;
                de.removeBank(ADCString);
            }
        }
        else if(event.hasBank(ADCString)==false && bg.hasBank(ADCString)==true) {
            bank = bg.getBank(ADCString);
        }  
        else if(event.hasBank(ADCString)==true && bg.hasBank(ADCString)==true) {
            List<ADC> bgADCs  = ADCbank(detector,bg.getBank(ADCString));
            List<ADC> ADCs    = ADCbank(detector,event.getBank(ADCString));

            List<ADC> allADCs = new ArrayList<ADC>();
            for(ADC adc : ADCs)   allADCs.add(adc);
            for(ADC adc : bgADCs) allADCs.add(adc);
            Collections.sort(allADCs);
            
            List<ADC> mergedADCs = new ArrayList<>();

            for(int i = 0; i < allADCs.size(); i++) {
                ADC adc = allADCs.get(i);
                if(mergedADCs.isEmpty()) {
                    if(debug) {
                        System.out.println("\tSkipping ADC " + i +"\t");
                        adc.show();
                    }
                    mergedADCs.add(adc);
                }
                else {
                    ADC adcOld = mergedADCs.get(mergedADCs.size()-1);
                    if(!adc.equalTo(adcOld)) {
                        if(debug) {
                            System.out.println("Keeping ADC " + i);
                            adc.show();
                        }
                        mergedADCs.add(adc);
                    }
                    else {
                        if(debug) {
                            System.out.println("\tSkipping ADC " + i +"\t");
                            adc.show();
                        }
                    }
                }
            } 

            if(event.hasBank(ADCString)) { 
                HipoDataEvent de = (HipoDataEvent) event;
                de.removeBank(ADCString);
            }

            bank = event.createBank(ADCString, mergedADCs.size());

            for (int i = 0; i < mergedADCs.size(); i++) {
                bank.setByte("sector",     i, mergedADCs.get(i).getSector());
                bank.setByte("layer",      i, mergedADCs.get(i).getLayer());
                bank.setShort("component", i, mergedADCs.get(i).getComponent());
                bank.setInt("ADC",         i, mergedADCs.get(i).getAdc()); 
                bank.setByte("order",      i, mergedADCs.get(i).getOrder());
                bank.setFloat("time",      i, mergedADCs.get(i).getTime());
                bank.setShort("ped",       i, mergedADCs.get(i).getPedestal());

                if(detector==DetectorType.BST.getName()) {
                    bank.setLong("timestamp", i, mergedADCs.get(i).getTimestamp());
                }
                if(detector == DetectorType.BMT.getName() || detector==DetectorType.FMT.getName() || detector==DetectorType.FTTRK.getName()) {
                    bank.setLong("timestamp", i, mergedADCs.get(i).getTimestamp());
                    bank.setInt("integral",   i, mergedADCs.get(i).getIntegral());
                }
                if(detector == DetectorType.BAND.getName()) {
                    bank.setInt("amplitude",  1, mergedADCs.get(i).getAmplitude());
                }
            } 
        }
        //bank.show();
        return bank;
    }
    
    private int getTDCOffset(String detector, DataEvent event, DataEvent bg) {
        int offset = getJitterCorrection(event, detector)
                   - getJitterCorrection(bg, detector);
        return offset;
    }
    
    private int getJitterCorrection(DataEvent event, String detector) {
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

    private void printDetectors() {
        System.out.print("\nMerging activated for detectors: ");
        for(String det:detectors) System.out.print(det + " ");
        System.out.println("\n");
    }
    
    /**
     * Append merged banks to hipo event
     * 
     * @param event
     * @param bg1
     * @param bg2
     */
    public void updateEventWithMergedBanks(DataEvent event, DataEvent bg1, DataEvent bg2) {
        
        if(!event.hasBank("RUN::config") || !bg1.hasBank("RUN::config") || !bg2.hasBank("RUN::config")) {
            System.out.println("Missing RUN::config bank");
            return;
        }
        
        if(event.hasBank("DC::doca")) event.removeBank("DC::doca");
        
        for(String det:detectors) {
            if("BMT".equals(det) || "BST".equals(det) || "FTCAL".equals(det) || "FTHODO".equals(det) || "FMT".equals(det) || "FTTRK".equals(det) || "HTCC".equals(det) || "LTCC".equals(det)) {
                event.appendBanks(this.getADCBank(det, event, bg1));
            }
            else if("DC".equals(det)) {
                event.appendBanks(this.getTDCBank(det, event, bg1, bg2));
            }
            else if("BAND".equals(det) || "CND".equals(det) || "CTOF".equals(det) || "ECAL".equals(det) || "FTOF".equals(det)) {
                event.appendBanks(this.getADCBank(det, event, bg1),this.getTDCBank(det, event, bg1));
            }
            else {
                System.out.println("Unknown detector:" + det);
            }
        }
    }
    
    private class ADC implements Comparable<ADC> {
        private byte sector;
        private byte layer;
        private short component;
        private byte order;
        private int adc;
        private float time;
        private short pedestal;
        private long timestamp;
        private int integral;
        private int amplitude;
        
        public ADC(byte sector, byte layer, short component, byte order, int adc, float time, short ped) {
            this.sector    = sector;
            this.layer     = layer;
            this.component = component;
            this.order     = order;
            this.adc       = adc;
            this.time      = time;
            this.pedestal  = ped;            
        }
        
        public ADC(byte sector, byte layer, short component, byte order, int adc, float time, short ped, int amplitude) {
            this.sector    = sector;
            this.layer     = layer;
            this.component = component;
            this.order     = order;
            this.adc       = adc;
            this.time      = time;
            this.pedestal  = ped;   
            this.amplitude = amplitude;
        }
        
        public ADC(byte sector, byte layer, short component, byte order, int adc, float time, short ped, long timestamp) {
            this.sector    = sector;
            this.layer     = layer;
            this.component = component;
            this.order     = order;
            this.adc       = adc;
            this.time      = time;
            this.pedestal  = ped;            
            this.timestamp = timestamp;
        }
        
        public ADC(byte sector, byte layer, short component, byte order, int adc, float time, short ped, long timestamp, int integral) {
            this.sector    = sector;
            this.layer     = layer;
            this.component = component;
            this.order     = order;
            this.adc       = adc;
            this.time      = time;
            this.pedestal  = ped;            
            this.timestamp = timestamp;
            this.integral  = integral;
        }  

        public byte getSector() {
            return sector;
        }

        public byte getLayer() {
            return layer;
        }

        public short getComponent() {
            return component;
        }

        public byte getOrder() {
            return order;
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
        
        public boolean equalTo(ADC o){
            if(this.getSector()    == o.getSector() &&
               this.getLayer()     == o.getLayer()&&
               this.getComponent() == o.getComponent() &&
               this.getOrder()     == o.getOrder())
                 return true;
            else return false;
        }

        @Override
        public int compareTo(ADC o) {
            int value = 0;
            int CompSec = this.getSector()    < o.getSector()    ? -1 : this.getSector()   == o.getSector() ? 0 : 1;
            int CompLay = this.getLayer()     < o.getLayer()     ? -1 : this.getLayer()    == o.getLayer() ? 0 : 1;
            int CompCom = this.getComponent() < o.getComponent() ? -1 : this.getComponent()== o.getComponent()? 0 : 1;
            int CompOrd = this.getOrder()     < o.getOrder()     ? -1 : this.getOrder()    == o.getOrder()? 0 : 1;
            int CompTim = this.getTime()      < o.getTime()      ? -1 : this.getTime()     == o.getTime()? 0 : 1;

            int value3 = ((CompOrd == 0) ? CompTim : CompOrd);
            int value2 = ((CompCom == 0) ? value3 : CompCom);
            int value1 = ((CompLay == 0) ? value2 : CompLay);
            value = ((CompSec == 0) ? value1 : CompSec);

            return value;
        }
        
        public void show() {
            System.out.println("Sector/Layer/Component/Order: " + sector + "/"+ layer + "/" + component + "/" + order + " ADC: " + adc + " time: " + time);
        }
    }
    
    private class TDC implements Comparable<TDC>{
        private byte sector;
        private byte layer;
        private short component;
        private byte order;
        private int tdc;
        
        public TDC(byte sector, byte layer, short component, byte order, int tdc) {
            this.sector    = sector;
            this.layer     = layer;
            this.component = component;
            this.order     = order;
            this.tdc       = tdc;          
        }

        public byte getSector() {
            return sector;
        }

        public byte getLayer() {
            return layer;
        }

        public short getComponent() {
            return component;
        }

        public byte getOrder() {
            return order;
        }

        public int getTdc() {
            return tdc;
        }

        public void setTdc(int tdc) {
            this.tdc = tdc;
        }
        
        public boolean equalTo(TDC o){
            if(this.getSector()    == o.getSector() &&
               this.getLayer()     == o.getLayer()&&
               this.getComponent() == o.getComponent() &&
               this.getOrder()     == o.getOrder())
                 return true;
            else return false;
        }

        @Override
        public int compareTo(TDC o) {
            int value = 0;
            int CompSec = this.getSector()    < o.getSector()    ? -1 : this.getSector()   == o.getSector() ? 0 : 1;
            int CompLay = this.getLayer()     < o.getLayer()     ? -1 : this.getLayer()    == o.getLayer() ? 0 : 1;
            int CompCom = this.getComponent() < o.getComponent() ? -1 : this.getComponent()== o.getComponent()? 0 : 1;
            int CompOrd = this.getOrder()     < o.getOrder()     ? -1 : this.getOrder()    == o.getOrder()? 0 : 1;
            int CompTim = this.getTdc()       < o.getTdc()       ? -1 : this.getTdc()      == o.getTdc()? 0 : 1;

            int value3 = ((CompOrd == 0) ? CompTim : CompOrd);
            int value2 = ((CompCom == 0) ? value3 : CompCom);
            int value1 = ((CompLay == 0) ? value2 : CompLay);
            value = ((CompSec == 0) ? value1 : CompSec);

            return value;
        }
        
        public void show() {
            System.out.println("Sector/Layer/Component/Order: " + sector + "/"+ layer + "/" + component + "/" + order + " TDC: " + tdc);
        }
    }
    
}
