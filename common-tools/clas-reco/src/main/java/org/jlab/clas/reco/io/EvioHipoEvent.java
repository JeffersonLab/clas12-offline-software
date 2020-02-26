/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.reco.io;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.coda.jevio.ByteDataTransformer;
import org.jlab.coda.jevio.EvioException;
import org.jlab.coda.jevio.EvioNode;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioSource;
import org.jlab.io.evio.EvioTreeBranch;
import org.jlab.io.hipo.HipoDataBank;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.utils.options.OptionParser;

/**
 *
 * @author gavalian
 */
public class EvioHipoEvent {
    
    
    
    public HipoDataEvent getHipoEvent(HipoDataSync writer, EvioDataEvent event){        
        HipoDataEvent hipoEvent = (HipoDataEvent) writer.createEvent();
        this.fillHipoEventRF(hipoEvent, event);
        this.fillHipoEventFTOF(hipoEvent, event);
        this.fillHipoEventFTCAL(hipoEvent, event);
        this.fillHipoEventFTHODO(hipoEvent, event);
        this.fillHipoEventFTTRK(hipoEvent, event);
        this.fillHipoEventDC(hipoEvent, event);
        this.fillHipoEventFMT(hipoEvent, event);
        this.fillHipoEventBMT(hipoEvent, event);
        this.fillHipoEventBST(hipoEvent, event);
        this.fillHipoEventRTPC(hipoEvent, event);
        this.fillHipoEventCTOF(hipoEvent, event);        
        this.fillHipoEventCND(hipoEvent, event);        
        this.fillHipoEventECAL(hipoEvent, event);
        this.fillHipoEventLTCC(hipoEvent, event);
        this.fillHipoEventHTCC(hipoEvent, event);
        this.fillHipoEventRICH(hipoEvent, event);
        this.fillHipoEventGenPart(hipoEvent, event);
        this.fillHipoEventTrueInfo(hipoEvent, event);
        this.fillHipoEventTrigger(hipoEvent, event);
        return hipoEvent;
    }
    
    
    public void fillHipoEventRF(HipoDataEvent hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("RF::info")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("RF::info");
            int nrows = evioBank.rows();
            DataBank  hipoBank = hipoEvent.createBank("RUN::rf", nrows);
            for(int i = 0; i < nrows; i++){
                hipoBank.setShort("id", i, (short) evioBank.getInt("id", i));
                double rf_time = evioBank.getDouble("time", i);
                //System.out.println(" RF time = " + rf_time);
                hipoBank.setFloat("time", i, (float) evioBank.getDouble("rf", i));
            }
            hipoEvent.appendBanks(hipoBank);
        }
    }

    public void fillHipoEventRICH(HipoDataEvent hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("RICH::dgtz")==true){
            try {
                EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("RICH::dgtz");
                
                int nrows = evioBank.rows();
                DataBank  hipoBankADC = hipoEvent.createBank("RICH::adc", nrows);
                DataBank  hipoBankTDC = hipoEvent.createBank("RICH::tdc", nrows);
                for(int i = 0; i < nrows; i++){
                	// RICH ADC Hipo Bank
                    hipoBankADC.setByte("sector",   i,    (byte) evioBank.getInt("sector", i));
                    hipoBankADC.setShort("pmt",     i,   (short)  evioBank.getInt("pmt",i));
                    hipoBankADC.setShort("pixel",   i,    (short) evioBank.getInt("pixel",i));
                    hipoBankADC.setByte("order",    i,    (byte) 0);
                    hipoBankADC.setInt("ADC",       i,    (int) evioBank.getInt("ADC", i));
                    // At the moment these variables are not in used
                    hipoBankADC.setFloat("time",    i,    (float) (0.0));
                    hipoBankADC.setInt("ped",       i,    (int) (0.0));
                    
                    
                    // RICH TDC Hipo Bank
                    hipoBankTDC.setByte("sector",   i,    (byte) evioBank.getInt("sector", i));
                    hipoBankTDC.setShort("pmt",     i,   (short)  evioBank.getInt("pmt",i));
                    hipoBankTDC.setShort("pixel",   i,    (short) evioBank.getInt("pixel",i));
                    hipoBankTDC.setInt("TDC1",      i,    (int) evioBank.getInt("TDC1",i));
                    hipoBankTDC.setInt("TDC2",      i,    (int) evioBank.getInt("TDC2", i));                                      
                }
                hipoEvent.appendBanks(hipoBankADC,hipoBankTDC);
            } catch (Exception e) {
                System.out.println("[hipo-decoder]  >>>> error writing RICH bank");
            }
        }
    }

    public void fillHipoEventHTCC(HipoDataEvent hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("HTCC::dgtz")==true){
            //System.out.println("LTCC bank is present");
            try {
                EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("HTCC::dgtz");
                //evioBank.show();
                
                int nrows = evioBank.rows();
                DataBank  hipoBank = hipoEvent.createBank("HTCC::adc", nrows);
                for(int i = 0; i < nrows; i++){
                    hipoBank.setByte("sector", i, (byte) evioBank.getInt("sector",i));
                    hipoBank.setByte("layer", i, (byte) evioBank.getInt("half",i));
                    hipoBank.setShort("component", i, (short) evioBank.getInt("ring",i));
                    hipoBank.setInt("ADC", i, evioBank.getInt("nphe", i)*100);
                    hipoBank.setFloat("time", i, (float) evioBank.getDouble("time", i));
                    hipoBank.setShort("ped", i, (short) 0);
                }
                hipoEvent.appendBanks(hipoBank);
            } catch (Exception e) {
                System.out.println("[hipo-decoder]  >>>> error writing LTCC bank");
            }
        }
    }
    
    public void fillHipoEventLTCC(HipoDataEvent hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("LTCC::dgtz")==true){
            //System.out.println("LTCC bank is present");
            try {
                EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("LTCC::dgtz");
                //evioBank.show();
                
                int nrows = evioBank.rows();
                DataBank  hipoBank = hipoEvent.createBank("LTCC::adc", nrows);
                for(int i = 0; i < nrows; i++){
                    hipoBank.setByte("sector", i, (byte) evioBank.getInt("sector",i));
                    hipoBank.setByte("layer", i, (byte) evioBank.getInt("side",i));
                    hipoBank.setShort("component", i, (short) evioBank.getInt("segment",i));
                    hipoBank.setInt("ADC", i, evioBank.getInt("adc", i));
                    hipoBank.setFloat("time", i, (float) evioBank.getDouble("time",i));
                    hipoBank.setShort("ped", i, (short) 0);
                }
                hipoEvent.appendBanks(hipoBank);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("[hipo-decoder]  >>>> error writing LTCC bank");
            }
        }
    }
    
    public void fillHipoEventBST(HipoDataEvent hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("BST::dgtz")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("BST::dgtz");
            HipoDataBank hipoADC = (HipoDataBank) hipoEvent.createBank("BST::adc", evioBank.rows());
            for(int i = 0; i < evioBank.rows(); i++){
                hipoADC.setByte("sector", i, (byte) evioBank.getInt("sector",i));
                hipoADC.setByte("layer",  i, (byte) evioBank.getInt("layer",i));
                hipoADC.setShort("component",  i, (short) evioBank.getInt("strip",i));
                hipoADC.setInt("ADC",  i, (byte) evioBank.getInt("ADC",i));
                hipoADC.setFloat("time",  i, (float) evioBank.getInt("bco",i));
                
            }
            hipoEvent.appendBanks(hipoADC);
        }
    }
    
    public void fillHipoEventBMT(HipoDataEvent hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("BMT::dgtz")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("BMT::dgtz");
            HipoDataBank hipoADC = (HipoDataBank) hipoEvent.createBank("BMT::adc", evioBank.rows());
            for(int i = 0; i < evioBank.rows(); i++){
                hipoADC.setByte("sector", i, (byte) evioBank.getInt("sector",i));
                hipoADC.setByte("layer",  i, (byte) evioBank.getInt("layer",i));
                hipoADC.setShort("component",  i, (short) evioBank.getInt("strip",i));
                hipoADC.setInt("ADC",  i, (int) evioBank.getInt("ADC",i));
                hipoADC.setFloat("time",  i, (float) 0);
                hipoADC.setShort("ped", i, (short) 0);            
            }
            hipoEvent.appendBanks(hipoADC);
        }
    }
    
    public void fillHipoEventFMT(HipoDataEvent hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("FMT::dgtz")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("FMT::dgtz");
            HipoDataBank hipoADC = (HipoDataBank) hipoEvent.createBank("FMT::adc", evioBank.rows());
            for(int i = 0; i < evioBank.rows(); i++){
                hipoADC.setByte("sector", i, (byte) evioBank.getInt("sector",i));
                hipoADC.setByte("layer",  i, (byte) evioBank.getInt("layer",i));
                hipoADC.setShort("component",  i, (short) evioBank.getInt("strip",i));
                hipoADC.setInt("ADC",  i, (int) evioBank.getInt("ADC",i));
                hipoADC.setFloat("time",  i, (float) 0);
                hipoADC.setShort("ped", i, (short) 0);            
            }
            hipoEvent.appendBanks(hipoADC);
        }
    }
    
    public void fillHipoEventDC(HipoDataEvent hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("DC::dgtz")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("DC::dgtz");
            HipoDataBank hipoTDC = (HipoDataBank) hipoEvent.createBank("DC::tdc", evioBank.rows());
            HipoDataBank hipoDOCA = (HipoDataBank) hipoEvent.createBank("DC::doca", evioBank.rows());            
            for(int i = 0; i < evioBank.rows(); i++){
                hipoTDC.setByte("sector", i, (byte) evioBank.getInt("sector",i));
                hipoTDC.setByte("layer",  i, (byte) evioBank.getInt("layer",i));
                hipoTDC.setShort("component",  i, (short) evioBank.getInt("wire",i));
                hipoTDC.setByte("order", i, (byte) 2);
                hipoTDC.setInt("TDC", i, evioBank.getInt("TDC", i));
                hipoDOCA.setByte("LR", i, (byte) evioBank.getInt("LR", i));
                hipoDOCA.setFloat("doca", i, (float) evioBank.getDouble("doca", i));
//                hipoDOCA.setFloat("sdoca", i, (float) evioBank.getDouble("sdoca", i));
                hipoDOCA.setFloat("time", i, (float) evioBank.getDouble("time", i));
                hipoDOCA.setFloat("stime", i, (float) evioBank.getDouble("stime", i));
            }
            hipoEvent.appendBanks(hipoTDC,hipoDOCA);
        }
    }
    
    public void fillHipoEventRTPC(HipoDataEvent hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("RTPC::dgtz")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("RTPC::dgtz");
            HipoDataBank hipoADC = (HipoDataBank) hipoEvent.createBank("RTPC::adc", evioBank.rows());
//            HipoDataBank hipoPOS = (HipoDataBank) hipoEvent.createBank("RTPC::pos", evioBank.rows());            
            for(int i = 0; i < evioBank.rows(); i++){
                hipoADC.setByte("sector", i, (byte) 1);
                hipoADC.setByte("layer",  i, (byte) 1);
                hipoADC.setShort("component",  i, (short) evioBank.getInt("CellID",i));
                hipoADC.setByte("order", i, (byte) 0);
                hipoADC.setInt("ADC", i, (int) evioBank.getDouble("ADC", i));
                hipoADC.setFloat("time", i, (float) evioBank.getDouble("Time", i));
//                double tdc = evioBank.getDouble("TDC", i);
//                hipoADC.setFloat("time", i, (float) tdc);
//                hipoADC.setShort("ped", i, (short) 0);
//                hipoPOS.setInt("step", i, (byte) evioBank.getInt("step", i));
//                hipoPOS.setFloat("time", i, (float) tdc);
//                hipoPOS.setFloat("energy", i, (float) evioBank.getDouble("EDep", i));
//                hipoPOS.setFloat("posx", i, (float) evioBank.getDouble("PosX", i));
//                hipoPOS.setFloat("posy", i, (float) evioBank.getDouble("PosY", i));
//                hipoPOS.setFloat("posz", i, (float) evioBank.getDouble("PosZ", i));
//                hipoPOS.setFloat("phi", i, (float) evioBank.getDouble("phiRad", i));
            }
//            hipoEvent.appendBanks(hipoADC,hipoPOS);
            hipoEvent.appendBanks(hipoADC);
        }
    }
    
    public void fillHipoEventCTOF(HipoDataEvent hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("CTOF::dgtz")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("CTOF::dgtz");
            HipoDataBank hipoADC = (HipoDataBank) hipoEvent.createBank("CTOF::adc", evioBank.rows());
            HipoDataBank hipoTDC = (HipoDataBank) hipoEvent.createBank("CTOF::tdc", evioBank.rows());
            for(int i = 0; i < evioBank.rows(); i++){
                int index = i;
                
                hipoADC.setByte("sector", index,      (byte)  1);
                hipoADC.setByte("layer",  index,      (byte)  1);
                hipoADC.setShort("component",  index, (short) evioBank.getInt("paddle",i));
                hipoADC.setByte("order", index,(byte) evioBank.getInt("side", i));
                hipoADC.setInt("ADC", index, evioBank.getInt("ADC", i));
                double tdc = (double) evioBank.getInt("TDC", i);
                hipoADC.setFloat("time", i, (float) (tdc*24.0/1000));
                
                hipoTDC.setByte("sector", index,      (byte)  1);
                hipoTDC.setByte("layer",  index,      (byte)  1);
                hipoTDC.setShort("component",  index, (short) evioBank.getInt("paddle",i));
                hipoTDC.setByte("order", index,(byte) (byte) (evioBank.getInt("side", i)+2));
                hipoTDC.setInt("TDC", index, evioBank.getInt("TDC", i));
                
            }
            hipoEvent.appendBanks(hipoADC,hipoTDC);
        }
    }
    
    public void fillHipoEventCND(HipoDataEvent hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("CND::dgtz")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("CND::dgtz");
            HipoDataBank hipoADC = (HipoDataBank) hipoEvent.createBank("CND::adc", evioBank.rows()*2);
            HipoDataBank hipoTDC = (HipoDataBank) hipoEvent.createBank("CND::tdc", evioBank.rows()*2);
            for(int i = 0; i < evioBank.rows(); i++){
                int index = i*2;
                
                hipoADC.setByte("sector", index,      (byte)  evioBank.getInt("sector",i));
                hipoADC.setByte("layer",  index,      (byte)  evioBank.getInt("layer",i));
                hipoADC.setShort("component",  index, (short) evioBank.getInt("component",i));
                hipoADC.setByte("order", index,(byte) 0);
                hipoADC.setInt("ADC", index, evioBank.getInt("ADCL", i));
                
                hipoADC.setByte("sector", index+1,      (byte)  evioBank.getInt("sector",i));
                hipoADC.setByte("layer",  index+1,      (byte)  evioBank.getInt("layer",i));
                hipoADC.setShort("component",  index+1, (short) evioBank.getInt("component",i));
                hipoADC.setByte("order", index+1,(byte) 1);
                hipoADC.setInt("ADC", index+1, evioBank.getInt("ADCR", i));
                
                hipoTDC.setByte("sector", index,      (byte)  evioBank.getInt("sector",i));
                hipoTDC.setByte("layer",  index,      (byte)  evioBank.getInt("layer",i));
                hipoTDC.setShort("component",  index, (short) evioBank.getInt("component",i));
                hipoTDC.setByte("order", index,(byte) 2);
                hipoTDC.setInt("TDC", index, evioBank.getInt("TDCL", i));
                
                hipoTDC.setByte("sector", index+1,      (byte)  evioBank.getInt("sector",i));
                hipoTDC.setByte("layer",  index+1,      (byte)  evioBank.getInt("layer",i));
                hipoTDC.setShort("component",  index+1, (short) evioBank.getInt("component",i));
                hipoTDC.setByte("order", index+1,(byte) 3);
                hipoTDC.setInt("TDC", index+1, evioBank.getInt("TDCR", i));
                             
            }
            hipoEvent.appendBanks(hipoADC,hipoTDC);
        }
    }
    
    public void fillHipoEventFTCAL(HipoDataEvent hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("FTCAL::dgtz")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("FTCAL::dgtz");
            int rows = evioBank.rows();
            HipoDataBank hipoADC = (HipoDataBank) hipoEvent.createBank("FTCAL::adc", rows);
            for(int i = 0; i < rows; i++){
                hipoADC.setByte("sector", i,      (byte)  evioBank.getInt("sector",i));
                hipoADC.setByte("layer",  i,      (byte)  evioBank.getInt("layer",i));
                hipoADC.setShort("component",  i, (short) evioBank.getInt("component",i));
                hipoADC.setByte("order", i,(byte) 0);
                hipoADC.setInt("ADC", i, evioBank.getInt("ADC", i));
                double tdc = (double) evioBank.getInt("TDC", i);
                hipoADC.setFloat("time", i, (float) (tdc/25.0));
            }
            hipoEvent.appendBanks(hipoADC);
        }
    }
    
    public void fillHipoEventFTHODO(HipoDataEvent hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("FTHODO::dgtz")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("FTHODO::dgtz");
            int rows = evioBank.rows();
            HipoDataBank hipoADC = (HipoDataBank) hipoEvent.createBank("FTHODO::adc", rows);
            for(int i = 0; i < rows; i++){
                hipoADC.setByte("sector", i,      (byte)  evioBank.getInt("sector",i));
                hipoADC.setByte("layer",  i,      (byte)  evioBank.getInt("layer",i));
                hipoADC.setShort("component",  i, (short) evioBank.getInt("component",i));
                hipoADC.setByte("order", i,(byte) 0);
                hipoADC.setInt("ADC", i, evioBank.getInt("ADC", i));
                double tdc = (double) evioBank.getInt("TDC", i);
                hipoADC.setFloat("time", i, (float) (tdc/25.0));
            }
            hipoEvent.appendBanks(hipoADC);
        }
    }
    
   public void fillHipoEventFTTRK(HipoDataEvent hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("FTTRK::dgtz")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("FTTRK::dgtz");
            int rows = evioBank.rows();
            HipoDataBank hipoADC = (HipoDataBank) hipoEvent.createBank("FTTRK::adc", rows);
            for(int i = 0; i < rows; i++){
                hipoADC.setByte("sector", i,      (byte)  evioBank.getInt("sector",i));
                hipoADC.setByte("layer",  i,      (byte)  evioBank.getInt("layer",i));
                hipoADC.setShort("component",  i, (short) evioBank.getInt("component",i));
                hipoADC.setByte("order", i,(byte) 0);
                hipoADC.setInt("ADC", i, evioBank.getInt("ADC", i));
                double tdc = 0;
                hipoADC.setFloat("time", i, (float) (tdc/25.0));
            }
            hipoEvent.appendBanks(hipoADC);
        }
    }
    
    public void fillHipoEventFTOF(HipoDataEvent hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("FTOF::dgtz")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("FTOF::dgtz");
            int rows = evioBank.rows();
            HipoDataBank hipoADC = (HipoDataBank) hipoEvent.createBank("FTOF::adc", rows);
            HipoDataBank hipoTDC = (HipoDataBank) hipoEvent.createBank("FTOF::tdc", rows);            

            for(int i = 0; i < evioBank.rows(); i++){
                int index = i;
                
                hipoADC.setByte("sector", index,      (byte)  evioBank.getInt("sector",i));
                hipoADC.setByte("layer",  index,      (byte)  evioBank.getInt("layer",i));
                hipoADC.setShort("component",  index, (short) evioBank.getInt("paddle",i));
                hipoADC.setByte("order", index,(byte) evioBank.getInt("side", i));
                hipoADC.setInt("ADC", index, evioBank.getInt("ADC", i));
                double tdc = (double) evioBank.getInt("TDC", i);
                hipoADC.setFloat("time", i, (float) (tdc*24.0/1000));
                
                hipoTDC.setByte("sector", index,      (byte)  evioBank.getInt("sector",i));
                hipoTDC.setByte("layer",  index,      (byte)  evioBank.getInt("layer",i));
                hipoTDC.setShort("component",  index, (short) evioBank.getInt("paddle",i));
                hipoTDC.setByte("order", index,(byte) (evioBank.getInt("side", i)+2));
                hipoTDC.setInt("TDC", index, evioBank.getInt("TDC", i));
                
            }
            hipoEvent.appendBanks(hipoADC,hipoTDC);
        }
    }
    /**
     * Fill the ECAL EVENT combines PCAL with EC
     * @param hipoEvent
     * @param evioEvent 
     */
    public void fillHipoEventECAL(HipoDataEvent hipoEvent, EvioDataEvent evioEvent){
        int nrows = 0;
        EvioDataBank bankPCAL = null;
        EvioDataBank bankECAL = null;        
        
        if(evioEvent.hasBank("PCAL::dgtz")==true){
            bankPCAL = (EvioDataBank) evioEvent.getBank("PCAL::dgtz");
            nrows += bankPCAL.rows();
        }
        if(evioEvent.hasBank("EC::dgtz")==true){
            bankECAL = (EvioDataBank) evioEvent.getBank("EC::dgtz");
            nrows += bankECAL.rows();
        }
        
        HipoDataBank hipoADC = (HipoDataBank) hipoEvent.createBank("ECAL::adc",nrows);
        HipoDataBank hipoTDC = (HipoDataBank) hipoEvent.createBank("ECAL::tdc",nrows);
       
        final float tdc2ns = 0.02345f;

        int counter = 0;
        if(bankPCAL!=null){
            for(int i = 0; i < bankPCAL.rows(); i++){
                hipoADC.setByte("sector",     counter, (byte)  bankPCAL.getInt("sector",i));
                hipoADC.setByte("layer",      counter, (byte)  bankPCAL.getInt("view",i));
                hipoADC.setShort("component", counter, (short) bankPCAL.getInt("strip",i));
                hipoADC.setByte("order",      counter, (byte) 0);
                hipoADC.setInt("ADC",         counter, bankPCAL.getInt("ADC", i));
                hipoADC.setFloat("time",      counter, (float) bankPCAL.getInt("TDC",i)*tdc2ns);
                
                hipoTDC.setByte("sector",     counter, (byte)  bankPCAL.getInt("sector",i));
                hipoTDC.setByte("layer",      counter, (byte)  bankPCAL.getInt("view",i));
                hipoTDC.setShort("component", counter, (short) bankPCAL.getInt("strip",i));
                hipoTDC.setByte("order",      counter, (byte) 2);
                hipoTDC.setInt("TDC",         counter, bankPCAL.getInt("TDC", i));
                counter++;
            }
        }
        
        if(bankECAL!=null){
            for(int i = 0; i < bankECAL.rows(); i++){
                int stack = bankECAL.getInt("stack",i);
                int view  = bankECAL.getInt("view",i);           
                hipoADC.setByte("sector",     counter, (byte)  bankECAL.getInt("sector",i));
                hipoADC.setByte("layer",      counter, (byte)  (view+stack*3));
                hipoADC.setShort("component", counter, (short) bankECAL.getInt("strip",i));
                hipoADC.setByte("order",      counter, (byte) 0);
                hipoADC.setInt("ADC",         counter, bankECAL.getInt("ADC", i));
                hipoADC.setFloat("time",      counter, (float) bankECAL.getInt("TDC",i)*tdc2ns);
                
                hipoTDC.setByte("sector",     counter, (byte)  bankECAL.getInt("sector",i));
                hipoTDC.setByte("layer",      counter, (byte)  (view+stack*3));
                hipoTDC.setShort("component", counter, (short) bankECAL.getInt("strip",i));
                hipoTDC.setByte("order",      counter, (byte) 1);
                hipoTDC.setInt("TDC",         counter, bankECAL.getInt("TDC", i));                
                
                counter++;
            }
        }
        if(nrows>0){
            hipoEvent.appendBanks(hipoADC,hipoTDC);
        }
    }
    
    public void fillHipoEventGenPart(HipoDataEvent hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("GenPart::header")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("GenPart::header");
            HipoDataBank hipoBank = (HipoDataBank) hipoEvent.createBank("MC::Header", evioBank.rows());
            for(int i = 0; i < evioBank.rows(); i++){
                hipoBank.setInt("run",        i, (evioBank.getInt("runNo", i)) );
                hipoBank.setInt("event",      i, (evioBank.getInt("evn", i)) );
                hipoBank.setByte("type",      i, (byte) (evioBank.getInt("evn_type", i)) );
                hipoBank.setFloat("helicity", i, (float) (evioBank.getDouble("helicity", i)) );
            }
            if(evioBank.rows()>0) hipoEvent.appendBanks(hipoBank);
        }
        if(evioEvent.hasBank("GenPart::true")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("GenPart::true");
            HipoDataBank hipoBank = (HipoDataBank) hipoEvent.createBank("MC::Particle", evioBank.rows());
            for(int i = 0; i < evioBank.rows(); i++){
                hipoBank.setInt("pid", i, evioBank.getInt("pid", i));
                hipoBank.setFloat("px", i, (float) (evioBank.getDouble("px", i)/1000.0) );
                hipoBank.setFloat("py", i, (float) (evioBank.getDouble("py", i)/1000.0) );
                hipoBank.setFloat("pz", i, (float) (evioBank.getDouble("pz", i)/1000.0) );
                hipoBank.setFloat("vx", i, (float) (evioBank.getDouble("vx", i)/10.0) );
                hipoBank.setFloat("vy", i, (float) (evioBank.getDouble("vy", i)/10.0) );
                hipoBank.setFloat("vz", i, (float) (evioBank.getDouble("vz", i)/10.0) );
                hipoBank.setFloat("vt", i, (float) (evioBank.getDouble("vt", i)) );
            }
            if(evioBank.rows()>0) hipoEvent.appendBanks(hipoBank);
        }
        if(evioEvent.hasBank("Lund::header")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("Lund::header");
            HipoDataBank hipoBank = (HipoDataBank) hipoEvent.createBank("MC::Event", evioBank.rows());
            for(int i = 0; i < evioBank.rows(); i++){
                hipoBank.setShort("npart",     i, (short) (evioBank.getDouble("nParticles", i)) );
                hipoBank.setShort("atarget",   i, (short) (evioBank.getDouble("nNucleons", i)) );
                hipoBank.setShort("ztarget",   i, (short) (evioBank.getDouble("nProtons", i)) );
                hipoBank.setFloat("ptarget",   i, (float) (evioBank.getDouble("targetPol", i)) );
                hipoBank.setFloat("pbeam",     i, (float) (evioBank.getDouble("beamPol", i)) );
                hipoBank.setShort("btype",     i, (short) (evioBank.getDouble("beamType", i)) );
                hipoBank.setFloat("ebeam",     i, (float) (evioBank.getDouble("beamEnergy", i)) );
                hipoBank.setShort("targetid",  i, (short) (evioBank.getDouble("targetID", i)) );
                hipoBank.setShort("processid", i, (short) (evioBank.getDouble("processID", i)) );
                hipoBank.setFloat("weight",    i, (float) (evioBank.getDouble("eventWeight", i)) );
            }
            if(evioBank.rows()>0) hipoEvent.appendBanks(hipoBank);
        }
        if(evioEvent.hasBank("Lund::true")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("Lund::true");
            HipoDataBank hipoBank = (HipoDataBank) hipoEvent.createBank("MC::Lund", evioBank.rows());
            for(int i = 0; i < evioBank.rows(); i++){
                hipoBank.setByte("index",    i, (byte)  (evioBank.getDouble("index", i)) );
                hipoBank.setFloat("lifetime",i, (float) (evioBank.getDouble("ltime", i)) );
                hipoBank.setByte("type",     i, (byte)  (evioBank.getDouble("type", i)) );
                hipoBank.setInt("pid",       i, (int)   (evioBank.getDouble("pid", i)) );
                hipoBank.setByte("parent",   i, (byte)  (evioBank.getDouble("parentID", i)) );
                hipoBank.setByte("daughter", i, (byte)  (evioBank.getDouble("daughterID", i)) );
                hipoBank.setFloat("px",      i, (float) (evioBank.getDouble("px", i)) );
                hipoBank.setFloat("py",      i, (float) (evioBank.getDouble("py", i)) );
                hipoBank.setFloat("pz",      i, (float) (evioBank.getDouble("pz", i)) );
                hipoBank.setFloat("energy",  i, (float) (evioBank.getDouble("E", i)) );
                hipoBank.setFloat("mass",    i, (float) (evioBank.getDouble("mass", i)) );
                hipoBank.setFloat("vx",      i, (float) (evioBank.getDouble("vx", i)) );
                hipoBank.setFloat("vy",      i, (float) (evioBank.getDouble("vy", i)) );
                hipoBank.setFloat("vz",      i, (float) (evioBank.getDouble("vz", i)) );
            }
            if(evioBank.rows()>0) hipoEvent.appendBanks(hipoBank);
        }
    }
    
   public void fillHipoEventTrueInfo(HipoDataEvent hipoEvent, EvioDataEvent evioEvent){
        
        String[]        bankNames = new String[]{"BMT","BST","CND","CTOF","DC","EC","FMT","FTCAL","FTHODO","FTOF","FTTRK","HTCC","LTCC","PCAL","RICH","RTPC"};
        DetectorType[]  bankTypes = new DetectorType[]{DetectorType.BMT,
                                                       DetectorType.BST,
                                                       DetectorType.CND,
                                                       DetectorType.CTOF,
                                                       DetectorType.DC,
                                                       DetectorType.ECAL,
                                                       DetectorType.FMT,
                                                       DetectorType.FTCAL,
                                                       DetectorType.FTHODO,
                                                       DetectorType.FTOF,
                                                       DetectorType.FTTRK,
                                                       DetectorType.HTCC,
                                                       DetectorType.LTCC,
                                                       DetectorType.ECAL,
                                                       DetectorType.RICH,
                                                       DetectorType.RTPC};
        int rows = 0;
        for(int k = 0; k < bankNames.length; k++){
            if(evioEvent.hasBank(bankNames[k]+"::true")==true){
                EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank(bankNames[k]+"::true");
                rows += evioBank.rows();
            }
        }
        if(rows!=0) {
            HipoDataBank hipoBank = (HipoDataBank) hipoEvent.createBank("MC::True", rows);
            int irow=0;
            for(int k = 0; k < bankNames.length; k++){
                if(evioEvent.hasBank(bankNames[k]+"::true")==true){
                    EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank(bankNames[k]+"::true");
                    for(int i = 0; i < evioBank.rows(); i++){
                        hipoBank.setByte("detector", irow, (byte)  bankTypes[k].getDetectorId());
                        hipoBank.setInt("pid",       irow,         evioBank.getInt("pid",i));
                        hipoBank.setInt("mpid",      irow,         evioBank.getInt("mpid",i));
                        hipoBank.setInt("tid",       irow,         evioBank.getInt("tid",i));
                        hipoBank.setInt("mtid",      irow,         evioBank.getInt("mtid",i));
                        hipoBank.setInt("otid",      irow,         evioBank.getInt("otid",i));
                        hipoBank.setFloat("trackE",  irow, (float) evioBank.getDouble("trackE",i));
                        hipoBank.setFloat("totEdep", irow, (float) evioBank.getDouble("totEdep",i));
                        hipoBank.setFloat("avgX",    irow, (float) evioBank.getDouble("avgX",i));
                        hipoBank.setFloat("avgY",    irow, (float) evioBank.getDouble("avgY",i));
                        hipoBank.setFloat("avgZ",    irow, (float) evioBank.getDouble("avgZ",i));
                        hipoBank.setFloat("avgLx",   irow, (float) evioBank.getDouble("avgLx",i));
                        hipoBank.setFloat("avgLy",   irow, (float) evioBank.getDouble("avgLy",i));
                        hipoBank.setFloat("avgLz",   irow, (float) evioBank.getDouble("avgLz",i));
                        hipoBank.setFloat("px",      irow, (float) evioBank.getDouble("px",i));
                        hipoBank.setFloat("py",      irow, (float) evioBank.getDouble("py",i));
                        hipoBank.setFloat("pz",      irow, (float) evioBank.getDouble("pz",i));
                        hipoBank.setFloat("vx",      irow, (float) evioBank.getDouble("vx",i));
                        hipoBank.setFloat("vy",      irow, (float) evioBank.getDouble("vy",i));
                        hipoBank.setFloat("vz",      irow, (float) evioBank.getDouble("vz",i));
                        hipoBank.setFloat("mvx",     irow, (float) evioBank.getDouble("mvx",i));
                        hipoBank.setFloat("mvy",     irow, (float) evioBank.getDouble("mvy",i));
                        hipoBank.setFloat("mvz",     irow, (float) evioBank.getDouble("mvz",i));
                        hipoBank.setFloat("avgT",    irow, (float) evioBank.getDouble("avgT",i));
                        hipoBank.setInt("nsteps",    irow,         evioBank.getInt("nsteps",i));
                        hipoBank.setInt("procID",    irow,         evioBank.getInt("procID",i));
                        hipoBank.setInt("hitn",      irow,         evioBank.getInt("hitn",i));
                        irow++;
                    }
                }
            }
            hipoEvent.appendBanks(hipoBank);
        }
    }
   
   
    public void fillHipoEventTrigger(HipoDataEvent hipoEvent, EvioDataEvent evioEvent){

        ArrayList<Integer> crates = new ArrayList();
        ArrayList<Integer> words  = new ArrayList();
        //System.out.println(" READING TRIGGER BANK");
        ArrayList<EvioTreeBranch>  branches = new ArrayList<EvioTreeBranch>();
        try {
            List<EvioNode>  eventNodes = evioEvent.getStructureHandler().getNodes();
            if(eventNodes==null){
                return;
            }
            for(EvioNode node : eventNodes){
                EvioTreeBranch eBranch = new EvioTreeBranch(node.getTag(),node.getNum());
                //branches.add(eBranch);
                //System.out.println("  FOR DROP : " + node.getTag() + "  " + node.getNum());
                List<EvioNode>  childNodes = node.getChildNodes();
                if(childNodes!=null){
                    for(EvioNode child : childNodes){
                        eBranch.addNode(child);
                    }
                    branches.add(eBranch);
                }
            }

        } catch (EvioException ex) {
            Logger.getLogger(EvioHipoEvent.class.getName()).log(Level.SEVERE, null, ex);
        }
        for(EvioTreeBranch branch : branches){
            int  crate = branch.getTag();
            for(EvioNode node : branch.getNodes()){
                if(node.getTag()==57634/*&&crate==125*/){
//                    System.out.println("TRIGGER BANK FOUND ");
                    int[] intData =  ByteDataTransformer.toIntArray(node.getStructureBuffer(true));
//                    if(intData.length!=0) System.out.println(" TRIGGER BANK LENGTH = " + intData.length);
                    for(int loop = 0; loop < intData.length; loop++){
                        crates.add(crate);
                        words.add(intData[loop]);
//                        System.out.println(entry.toString());
                    }
                }
            }
        }
        if(words.size()>0) {
            int rows = words.size();
            HipoDataBank hipoVTP = (HipoDataBank) hipoEvent.createBank("RAW::vtp", rows);
            for(int i = 0; i < rows; i++){
                int crate = crates.get(i);
                hipoVTP.setByte("sector", i,      (byte)  crate);
                hipoVTP.setByte("layer",  i,      (byte)  0);
                hipoVTP.setShort("component",  i, (short) 0);
                hipoVTP.setByte("order",       i,(byte) 0);
                hipoVTP.setInt("word", i, words.get(i));
            }
            hipoEvent.appendBanks(hipoVTP);
        }
    }
   
    public HipoDataBank createHeaderBank(HipoDataEvent event, int nrun, int nevent, float torus, float solenoid){
        HipoDataBank bank = (HipoDataBank) event.createBank("RUN::config", 1);        
        bank.setInt("run",        0, nrun);
        bank.setInt("event",      0, nevent);
        bank.setFloat("torus",    0, torus);
        bank.setFloat("solenoid", 0, solenoid);        
        return bank;
    }
    
    public static void main(String[] args){
        
        OptionParser parser = new OptionParser();
        parser.addRequired("-o");
        parser.addOption("-r","10");
        parser.addOption("-t","-1.0");
        parser.addOption("-s","1.0");
        parser.addOption("-n", "-1");
        
        parser.parse(args);
        
        if(parser.hasOption("-o")==true){
        
            String outputFile = parser.getOption("-o").stringValue();
        
            List<String> inputFiles = parser.getInputList();
            
            /*for(int i = 1; i < args.length; i++){
                inputFiles.add(args[i]);
            }*/
        
        
            EvioHipoEvent convertor = new EvioHipoEvent();
            
            HipoDataSync  writer = new HipoDataSync();
            writer.open(outputFile);
            writer.setCompressionType(2);
            System.out.println(">>>>>  SIZE OF THE INPUT FILES = " + inputFiles.size());
            int nevent = 1;
            int maximumEvents = parser.getOption("-n").intValue();
            
            for(String input : inputFiles){
                System.out.println(">>>>>  appending file : " + input);
                try {
                    EvioSource reader = new EvioSource();
                    reader.open(input);
                    
                    while(reader.hasEvent()==true){
                        EvioDataEvent evioEvent = (EvioDataEvent) reader.getNextEvent();                    
                        HipoDataEvent hipoEvent = convertor.getHipoEvent(writer, evioEvent);
                        int nrun = parser.getOption("-r").intValue();
                        float torus    = (float) parser.getOption("-t").doubleValue();
                        float solenoid = (float) parser.getOption("-s").doubleValue();
                        HipoDataBank header = convertor.createHeaderBank(hipoEvent, nrun, nevent, torus, solenoid);
                        hipoEvent.appendBanks(header);
                        writer.writeEvent(hipoEvent);
                        nevent++;
                        if(maximumEvents>0&&nevent>=maximumEvents) {
                            reader.close();
                            writer.close();
                            System.out.println("\n\n\n Finished output file at event count = " + nevent);
                            System.exit(0);
                        }
                }
            } catch (Exception e){
                e.printStackTrace();
                System.out.println(">>>>>  processing file :  failed ");
            }
            System.out.println(">>>>>  processing file :  success ");
            System.out.println();            
        }
        writer.close();
        }
        /*
        EvioDataDictionary dictionary = new EvioDataDictionary("/Users/gavalian/Work/Software/Release-9.0/COATJAVA/coatjava/etc/bankdefs/hipo");
        dictionary.show();
        
        EvioDataDescriptor desc = (EvioDataDescriptor) dictionary.getDescriptor("ECAL::dgtz");
        desc.show();*/
    }
}
