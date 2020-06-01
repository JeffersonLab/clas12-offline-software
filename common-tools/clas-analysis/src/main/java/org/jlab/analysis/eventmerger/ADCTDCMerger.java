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
     * Reads  ADC bank
     * 
     * @param detector
     * @param bankDGTZ
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
     * Read TDC bank
     * 
     * @param bankDGTZ
     * @return
     */
    public List<TDC> TDCbank(DataBank bankDGTZ) {
        List<TDC> tdcStore   = new ArrayList<TDC>();
        for (int i = 0; i < bankDGTZ.rows(); i++) {
            byte sector     = bankDGTZ.getByte("sector", i);
            byte layer      = bankDGTZ.getByte("layer", i);
            short component = bankDGTZ.getShort("component", i);
            byte order      = bankDGTZ.getByte("order", i);
            int tdc         = bankDGTZ.getInt("TDC", i);
            if(tdc<=0) continue;
            TDC tdcData = new TDC(sector,layer,component,order,tdc);
            tdcStore.add(tdcData);
        }
        return tdcStore;
    }
    
    /**
     * Merge TDC banks for data (signal) and background events for selected detector
     * In case of multiple hit on same detector element, only first hit in time is kept 
     * 
     * @param Det
     * @param event
     * @param bg
     * @return
     */
    public DataBank getTDCBank(String Det, DataEvent event, DataEvent bg){
        
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
            List<TDC> bgTDCs  = TDCbank(bg.getBank(TDCString));
            List<TDC> TDCs    = TDCbank(event.getBank(TDCString));

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
     * Merge ADC banks for data (signal) and background events for selected detector
     * In case of multiple hit on same detector element, only first hit in time is kept 
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
    
    private void printDetectors() {
        System.out.print("\nMerging activated for detectors: ");
        for(String det:detectors) System.out.print(det + " ");
        System.out.println("\n");
    }
    
    /**
     * Append merged banks to hipo event
     * 
     * @param event
     * @param bg
     */
    public void updateEventWithMergedBanks(DataEvent event, DataEvent bg) {
        if(event.hasBank("DC::doca")) event.removeBank("DC::doca");
        
        for(String det:detectors) {
            if("BMT".equals(det) || "BST".equals(det) || "FTCAL".equals(det) || "FTHODO".equals(det) || "FMT".equals(det) || "FTTRK".equals(det) || "HTCC".equals(det) || "LTCC".equals(det)) {
                event.appendBanks(this.getADCBank(det, event, bg));
            }
            else if("DC".equals(det) || "RICH".equals(det)) {
                event.appendBanks(this.getTDCBank(det, event, bg));
            }
            else if("BAND".equals(det) || "CND".equals(det) || "CTOF".equals(det) || "ECAL".equals(det) || "FTOF".equals(det)) {
                event.appendBanks(this.getADCBank(det, event, bg),this.getTDCBank(det, event, bg));
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
