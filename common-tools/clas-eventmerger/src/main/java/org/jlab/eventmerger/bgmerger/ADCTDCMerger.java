/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.eventmerger.bgmerger;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.jnp.hipo.data.HipoEvent;
/**
 *
 * @author ziegler
 */
public class ADCTDCMerger {
    private HipoDataSync dataSync = new HipoDataSync();
    
    public int[][] ADCbank(DataBank bankDGTZ) {
        int nCol = bankDGTZ.columns();
        int[][] ADCs    = new int[nCol][bankDGTZ.rows()];
        int[] sector    = new int[bankDGTZ.rows()];
        int[] layer     = new int[bankDGTZ.rows()];
        int[] component = new int[bankDGTZ.rows()];
        int[] adc       = new int[bankDGTZ.rows()];
        int[] order     = new int[bankDGTZ.rows()];
        int[] time      = new int[bankDGTZ.rows()];
        int[] ped       = new int[bankDGTZ.rows()];
        int[] integral  = new int[bankDGTZ.rows()];
        int[] timstmp   = new int[bankDGTZ.rows()];

        for (int i = 0; i < bankDGTZ.rows(); i++) {
            sector[i]       = bankDGTZ.getByte("sector", i);
            layer[i]        = bankDGTZ.getByte("layer", i);
            component[i]    = bankDGTZ.getShort("component", i);
            order[i]        = bankDGTZ.getByte("order", i);
            adc[i]          = bankDGTZ.getInt("ADC", i);
            time[i]         = (int)(bankDGTZ.getFloat("time", i)*1000);
            ped[i]          = bankDGTZ.getShort("ped", i);
            if(nCol==8) {
                timstmp[i]  = (int)bankDGTZ.getLong("timestamp", i);
            }
            if(nCol==9) {
                timstmp[i]  = (int)bankDGTZ.getLong("timestamp", i);
                integral[i] = bankDGTZ.getInt("integral", i);
            }
        }
        ADCs[0] = sector;
        ADCs[1] = layer;
        ADCs[2] = component;
        ADCs[3] = adc;
        ADCs[4] = order;
        ADCs[5] = time;
        ADCs[6] = ped;
        if(nCol==8) {
            ADCs[7]  = timstmp;
        }
        if(nCol==9) {
            ADCs[7]  = timstmp;
            ADCs[8]  = integral;
        }
        return ADCs;
    }
     
    public int[][] TDCbank(DataBank bankDGTZ) {
        int[][] TDCs        = new int[5][bankDGTZ.rows()];
        int[] sector        = new int[bankDGTZ.rows()];
        int[] layer         = new int[bankDGTZ.rows()];
        int[] component     = new int[bankDGTZ.rows()];
        int[] tdc           = new int[bankDGTZ.rows()];
        int[] order         = new int[bankDGTZ.rows()];

        for (int i = 0; i < bankDGTZ.rows(); i++) {
            sector[i]       = bankDGTZ.getByte("sector", i);
            layer[i]        = bankDGTZ.getByte("layer", i);
            component[i]    = bankDGTZ.getShort("component", i);
            order[i]        = bankDGTZ.getByte("order", i);
            tdc[i]          = bankDGTZ.getInt("TDC", i);
        }
        TDCs[0] = sector;
        TDCs[1] = layer;
        TDCs[2] = component;
        TDCs[3] = tdc;
        TDCs[4] = order;
        
        return TDCs;
    }
    
    public DataBank getTDCBank(String Det, DataEvent event, DataEvent bg){
        
        String TDCString = Det+"::tdc";
        DataBank bank = null;
        if(event.hasBank(TDCString)==true && bg.hasBank(TDCString)==false) {
            bank = event.getBank(TDCString);
            if(event.hasBank(TDCString)) { 
                HipoDataEvent de = (HipoDataEvent) event;
                HipoEvent dde = de.getHipoEvent();
                dde.removeGroup(TDCString);
            }
        }
        if(event.hasBank(TDCString)==false && bg.hasBank(TDCString)==true) {
            bank = bg.getBank(TDCString);
        }  
        if(event.hasBank(TDCString)==true && bg.hasBank(TDCString)==true) {
            int[][] BgTDCs = TDCbank(bg.getBank(TDCString));
            int[][] TDCs = TDCbank(event.getBank(TDCString));

            int[][] MgTDCs    = new int[5][TDCs[0].length+BgTDCs[0].length];

            for (int i = 0; i < TDCs[0].length; i++) {
                for (int j = 0; j < BgTDCs[0].length; j++) {
                    if(BgTDCs[0][j]==TDCs[0][i] && BgTDCs[1][j]==TDCs[1][i] && BgTDCs[2][j]==TDCs[2][i]
                            && BgTDCs[4][j]==TDCs[4][i] ) {
                        if(BgTDCs[3][j]>TDCs[3][i]) {
                            BgTDCs[3][j]=-999;
                        } else {
                           TDCs[3][i]=-999; 
                        }
                    }
                }
            } 

            for (int i = 0; i < TDCs[0].length; i++) {
                MgTDCs[0][i] = TDCs[0][i];
                MgTDCs[1][i] = TDCs[1][i];
                MgTDCs[2][i] = TDCs[2][i];
                MgTDCs[3][i] = TDCs[3][i];
                MgTDCs[4][i] = TDCs[4][i];
            }
            for (int i = 0; i < BgTDCs[0].length; i++) {
                MgTDCs[0][TDCs[0].length+i] = BgTDCs[0][i];
                MgTDCs[1][TDCs[0].length+i] = BgTDCs[1][i];
                MgTDCs[2][TDCs[0].length+i] = BgTDCs[2][i];
                MgTDCs[3][TDCs[0].length+i] = BgTDCs[3][i];
                MgTDCs[4][TDCs[0].length+i] = BgTDCs[4][i];
            }
            if(event.hasBank(TDCString)) { 
                HipoDataEvent de = (HipoDataEvent) event;
                HipoEvent dde = de.getHipoEvent();
                //HipoGroup group = dde.getGroup(TDCString);
                dde.removeGroup(TDCString);
            }
            bank = event.createBank(TDCString, TDCs[0].length+BgTDCs[0].length);
            for (int i = 0; i < TDCs[0].length; i++) {
                bank.setByte("sector", i, (byte) TDCs[0][i]);
                bank.setByte("layer",  i, (byte) TDCs[1][i]);
                bank.setShort("component",  i, (short) TDCs[2][i]);
                bank.setInt("TDC",  i, (short) TDCs[3][i]);
                bank.setByte("order",  i, (byte) TDCs[4][i]);
            }
            for (int i = 0; i < BgTDCs[0].length; i++) {
                bank.setByte("sector", TDCs[0].length+i, (byte) BgTDCs[0][i]);
                bank.setByte("layer",  TDCs[0].length+i, (byte) BgTDCs[1][i]);
                bank.setShort("component",  TDCs[0].length+i, (short) BgTDCs[2][i]);
                bank.setInt("TDC",  TDCs[0].length+i, (short) BgTDCs[3][i]);
                bank.setByte("order",  TDCs[0].length+i, (byte) BgTDCs[4][i]);
            }
        }
        return bank;
    }
    
    
    public DataBank getADCBank(String Det, DataEvent event, DataEvent bg){
        
        String ADCString = Det+"::adc";
        DataBank bank = null;
        if(event.hasBank(ADCString)==true && bg.hasBank(ADCString)==false) {
            bank = event.getBank(ADCString);
            if(event.hasBank(ADCString)) { 
                HipoDataEvent de = (HipoDataEvent) event;
                HipoEvent dde = de.getHipoEvent();
                dde.removeGroup(ADCString);
            }
        }
        if(event.hasBank(ADCString)==false && bg.hasBank(ADCString)==true) {
            bank = bg.getBank(ADCString);
        }  
        if(event.hasBank(ADCString)==true && bg.hasBank(ADCString)==true) {
            int[][] BgADCs = ADCbank(bg.getBank(ADCString));
            int[][] ADCs = ADCbank(event.getBank(ADCString));

            int[][] MgADCs    = new int[event.getBank(ADCString).columns()][ADCs[0].length+BgADCs[0].length];

            for (int i = 0; i < ADCs[0].length; i++) {
                for (int j = 0; j < BgADCs[0].length; j++) {
                    if(BgADCs[0][j]==ADCs[0][i] && BgADCs[1][j]==ADCs[1][i] && BgADCs[2][j]==ADCs[2][i]
                            && BgADCs[4][j]==ADCs[4][i] ) {
                        if(BgADCs[5][j]>ADCs[5][i]) {
                            BgADCs[3][j]=-999;
                        } else {
                           ADCs[3][i]=-999; 
                        }
                    }
                }
            } 


            for (int i = 0; i < ADCs[0].length; i++) {
                for (int j = 0; j <event.getBank(ADCString).columns(); j++) {
                    MgADCs[j][i] = ADCs[j][i];
                }
            }
            for (int i = 0; i < BgADCs[0].length; i++) {
                for (int j = 0; j <event.getBank(ADCString).columns(); j++) {
                    MgADCs[j][ADCs[0].length+i] = BgADCs[j][i];
                }
            }

            if(event.hasBank(ADCString)) { 
                    HipoDataEvent de = (HipoDataEvent) event;
                    HipoEvent dde = de.getHipoEvent();
                    dde.removeGroup(ADCString);
           }

            bank = event.createBank(ADCString, ADCs[0].length+BgADCs[0].length);

            for (int i = 0; i < ADCs[0].length; i++) {
                bank.setByte("sector", i, (byte) ADCs[0][i]);
                bank.setByte("layer",  i, (byte) ADCs[1][i]);
                bank.setShort("component",  i, (short) ADCs[2][i]);
                bank.setInt("ADC",  i, (int) ADCs[3][i]); 
                bank.setByte("order",  i, (byte) ADCs[4][i]);
                bank.setFloat("time",  i, (float) (((float) ADCs[5][i])/1000.));
                bank.setShort("ped",  i, (short) ADCs[6][i]);

                if(bank.columns()==8) {
                    bank.setLong("timestamp",  i, (long) ADCs[7][i]);
                }
                if(bank.columns()==9) {
                    bank.setLong("timestamp",  i, (long) ADCs[7][i]);
                    bank.setInt("integral",  i, (int) ADCs[8][i]);
                }
            } 

             for (int i = 0; i < BgADCs[0].length; i++) {
                bank.setByte("sector", ADCs[0].length+i, (byte) BgADCs[0][i]);
                bank.setByte("layer",  ADCs[0].length+i, (byte) BgADCs[1][i]);
                bank.setShort("component",  ADCs[0].length+i, (short) BgADCs[2][i]);
                bank.setInt("ADC",  ADCs[0].length+i, (int) BgADCs[3][i]); 
                bank.setByte("order",  ADCs[0].length+i, (byte) BgADCs[4][i]);
                bank.setFloat("time",  ADCs[0].length+i, (float) (((float) BgADCs[5][i])/1000.));
                bank.setShort("ped",  ADCs[0].length+i, (short) BgADCs[6][i]);
                if(bank.columns()==8) {
                    bank.setLong("timestamp",  i, (long) BgADCs[7][i]);
                }
                if(bank.columns()==9) {
                    bank.setLong("timestamp",  i, (long) BgADCs[7][i]);
                    bank.setInt("integral",  i, (int) BgADCs[8][i]);
                }
            }
        }
        //bank.show();
        return bank;
    }
    
    
    public void updateEventWithMergedBanks(DataEvent event, DataEvent bg) {
        event.appendBanks(
        this.getTDCBank("DC", event, bg),
        this.getADCBank("BST", event, bg),
        this.getADCBank("BMT", event, bg),
        this.getADCBank("FMT", event, bg),
        this.getADCBank("FTCAL", event, bg),
        this.getADCBank("FTHODO", event, bg),
        this.getADCBank("HEL", event, bg),
        this.getTDCBank("CND", event, bg),this.getADCBank("CND", event, bg),
        this.getTDCBank("RF", event, bg),this.getADCBank("RF", event, bg),
        this.getTDCBank("HTCC", event, bg),this.getADCBank("HTCC", event, bg),
        this.getTDCBank("LTCC", event, bg),this.getADCBank("LTCC", event, bg),
        this.getTDCBank("RICH", event, bg),this.getADCBank("RICH", event, bg),
        this.getTDCBank("CTOF", event, bg),this.getADCBank("CTOF", event, bg),
        this.getTDCBank("FTOF", event, bg),this.getADCBank("FTOF", event, bg),
        this.getTDCBank("ECAL", event, bg),this.getADCBank("ECAL", event, bg));
    }
}
