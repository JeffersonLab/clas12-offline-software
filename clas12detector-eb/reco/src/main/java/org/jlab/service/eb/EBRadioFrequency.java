/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.eb;

import java.util.ArrayList;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author devita
 */
public class EBRadioFrequency {
 
    private int debugMode = 0;
    
    private ArrayList<rfSignal> rfSignals = new ArrayList<rfSignal>();
    private double rfTime = -100;
    
    
    public EBRadioFrequency() {
    }

    
    public double getTime(DataEvent event) {
        this.processEvent(event);
        return rfTime;
    }
    
    public void processEvent(DataEvent event) {
        // if RUN::rf bank does not exist but tdc bnk exist, reconstruct RF signals from TDC hits and save to bank
        if(event.hasBank("RF::tdc") && !event.hasBank("RUN::rf")) {
            double time = 0;
            int    hits = 0;
            DataBank bank = event.getBank("RF::tdc");
            int rows = bank.rows();
            for(int i = 0; i < rows; i++){
                int    sector = bank.getByte("sector",i);
                int     layer = bank.getByte("layer",i);
                int      comp = bank.getShort("component",i);
                int       TDC = bank.getInt("TDC",i);
                int     order = bank.getByte("order",i); 
                if(order == 2) {
                    int ind = this.hasSignal(comp);
                    if(ind>=0) {
                        this.rfSignals.get(ind).add(TDC);
                    }
                    else {
                        rfSignal newSignal = new rfSignal(comp);
                        newSignal.add(TDC);
                        this.rfSignals.add(newSignal);
                    }
                }
            }
            if(debugMode>0) bank.show();
            DataBank bankOut = event.createBank("RUN::rf",this.rfSignals.size());
            for(int i =0; i< this.rfSignals.size(); i++) {
                bankOut.setShort("id",   i, (short) this.rfSignals.get(i).getId());
                bankOut.setFloat("time", i, (float) this.rfSignals.get(i).getTime());
                if(debugMode>0) this.rfSignals.get(i).print();
            }
            event.appendBank(bankOut);
            int index = this.hasSignal(EBConstants.RF_ID);
            if(index>=0) rfTime= this.rfSignals.get(index).getTime();
            if(debugMode>0) bankOut.show();
            
        }  
        else if(event.hasBank("RUN::rf")) {
            DataBank bank = event.getBank("RUN::rf");
            int rows = bank.rows();
            for(int i = 0; i < rows; i++){
                if(bank.getShort("id", i)==EBConstants.RF_ID) rfTime=bank.getFloat("time", i);
            }
        }
        
    }
    
    
    private int hasSignal(int id) {
        int index = -1;
        for(int i=0; i<rfSignals.size(); i++)  {
            if(rfSignals.get(i).getId()==id) index = i;
        }
        return index;
    }
    
    private class rfSignal {
        private int _id;
        private ArrayList<Integer> _tdc = new ArrayList<Integer>();
        private ArrayList<Double> _time = new ArrayList<Double>();

        public rfSignal(int id) {
            this.setId(id);
        }

        public boolean add(int tdc) {
            boolean skip = false;
            double time = (tdc*EBConstants.RF_TDC2TIME) % (EBConstants.RF_CYCLES*EBConstants.RF_BUCKET_LENGTH);
//            time = (tdc % 6840) *EBConstants.RF_TDC2TIME;
            // check if new TDC value compared to previous one is consistent with 80*2.004 ns interval
            if(this._tdc.size()>0) {               
                int deltaTDC = tdc - this._tdc.get(this._tdc.size()-1);
                if(deltaTDC>((double) (EBConstants.RF_CYCLES+1)*EBConstants.RF_BUCKET_LENGTH)/EBConstants.RF_TDC2TIME) { // allow for an extra 2.004 ns
//                    System.out.println("Found missing hits in RF sequence for signal ID " + this._id + ", TDC = " + tdc 
//                            + ", DeltaTDC = " + deltaTDC  + "(" + deltaTDC*EBConstants.RF_TDC2TIME+ ")");
                    skip = true;
                }
            }
            this._tdc.add(tdc);
            this._time.add(time);
            return skip;
        }
        
        public int getId() {
            return _id;
        }

        public void setId(int _id) {
            this._id = _id;
        }

        public double getTime() {
            double time = 0;
            for(int i=0; i<this._time.size(); i++) {
                time += this._time.get(i);
            }
            time /= this._time.size();
            return time;
        }
        
        public double getRMS() {
            double time2 = 0;
            for(int i=0; i<this._time.size(); i++) {
                time2 += Math.pow(this._time.get(i),2.);
            }
            time2 /= this._time.size();
            double time = this.getTime();
            double rms = Math.sqrt(time2-time*time);
            return rms;
        }
        
        public void print(){
            System.out.println("RF signal with ID = " + this._id + " N. Hits = " + this._tdc.size() + " and Time = (" + this.getTime() + "+/-" + this.getRMS() + ") ns");
            for(int i=0; i<this._tdc.size(); i++) {
                int dtdc = 0;
                if(i<this._tdc.size()-1) dtdc = this._tdc.get(i+1)-this._tdc.get(i);
                System.out.println("\t" + i + "\t" + this._tdc.get(i) + "\t" + dtdc + "\t" + (this._tdc.get(i)%6840) + "\t"  + this._time.get(i));
            }
        }
    }
}
