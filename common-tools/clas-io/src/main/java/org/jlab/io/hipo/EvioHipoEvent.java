/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.hipo;

import java.util.ArrayList;
import java.util.List;
import org.jlab.hipo.data.HipoEvent;
import org.jlab.hipo.data.HipoNodeBuilder;
import org.jlab.io.base.DataBank;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataDescriptor;
import org.jlab.io.evio.EvioDataDictionary;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioFactory;
import org.jlab.io.evio.EvioSource;
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
        this.fillHipoEventSVT(hipoEvent, event);
        this.fillHipoEventRTPC(hipoEvent, event);
        this.fillHipoEventCTOF(hipoEvent, event);        
        this.fillHipoEventCND(hipoEvent, event);        
        this.fillHipoEventECAL(hipoEvent, event);
        this.fillHipoEventLTCC(hipoEvent, event);
        this.fillHipoEventHTCC(hipoEvent, event);
        this.fillHipoEventRICH(hipoEvent, event);
        this.fillHipoEventGenPart(hipoEvent, event);
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
                    hipoBank.setInt("ADC", i, evioBank.getInt("npheD", i)*100);
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
    
    public void fillHipoEventSVT(HipoDataEvent hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("BST::dgtz")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("BST::dgtz");
            HipoDataBank hipoADC = (HipoDataBank) hipoEvent.createBank("SVT::adc", evioBank.rows());
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
                hipoDOCA.setFloat("sdoca", i, (float) evioBank.getDouble("sdoca", i));
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
            HipoDataBank hipoPOS = (HipoDataBank) hipoEvent.createBank("RTPC::pos", evioBank.rows());            
            for(int i = 0; i < evioBank.rows(); i++){
                hipoADC.setByte("sector", i, (byte) 1);
                hipoADC.setByte("layer",  i, (byte) 1);
                hipoADC.setShort("component",  i, (short) evioBank.getInt("CellID",i));
                hipoADC.setByte("order", i, (byte) 0);
                hipoADC.setInt("ADC", i, (int) evioBank.getDouble("ADC", i));
                double tdc = evioBank.getDouble("TDC", i);
                hipoADC.setFloat("time", i, (float) tdc);
                hipoADC.setShort("ped", i, (short) 0);
                hipoPOS.setInt("step", i, (byte) evioBank.getInt("step", i));
                hipoPOS.setFloat("time", i, (float) tdc);
                hipoPOS.setFloat("energy", i, (float) evioBank.getDouble("EDep", i));
                hipoPOS.setFloat("posx", i, (float) evioBank.getDouble("PosX", i));
                hipoPOS.setFloat("posy", i, (float) evioBank.getDouble("PosY", i));
                hipoPOS.setFloat("posz", i, (float) evioBank.getDouble("PosZ", i));
                hipoPOS.setFloat("phi", i, (float) evioBank.getDouble("phiRad", i));
            }
            hipoEvent.appendBanks(hipoADC,hipoPOS);
        }
    }
    
    public void fillHipoEventCTOF(HipoDataEvent hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("CTOF::dgtz")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("CTOF::dgtz");
            HipoDataBank hipoADC = (HipoDataBank) hipoEvent.createBank("CTOF::adc", evioBank.rows()*2);
            HipoDataBank hipoTDC = (HipoDataBank) hipoEvent.createBank("CTOF::tdc", evioBank.rows()*2);
            for(int i = 0; i < evioBank.rows(); i++){
                int index = i*2;
                
                hipoADC.setByte("sector", index,      (byte)  1);
                hipoADC.setByte("layer",  index,      (byte)  1);
                hipoADC.setShort("component",  index, (short) evioBank.getInt("paddle",i));
                hipoADC.setByte("order", index,(byte) 0);
                hipoADC.setInt("ADC", index, evioBank.getInt("ADCU", i));
                
                hipoADC.setByte("sector", index+1,      (byte)  1);
                hipoADC.setByte("layer",  index+1,      (byte)  1);
                hipoADC.setShort("component",  index+1, (short) evioBank.getInt("paddle",i));
                hipoADC.setByte("order", index+1,(byte) 1);
                hipoADC.setInt("ADC", index+1, evioBank.getInt("ADCD", i));
                
                hipoTDC.setByte("sector", index,      (byte)  1);
                hipoTDC.setByte("layer",  index,      (byte)  1);
                hipoTDC.setShort("component",  index, (short) evioBank.getInt("paddle",i));
                hipoTDC.setByte("order", index,(byte) 2);
                hipoTDC.setInt("TDC", index, evioBank.getInt("TDCU", i));
                
                hipoTDC.setByte("sector", index+1,      (byte)  1);
                hipoTDC.setByte("layer",  index+1,      (byte)  1);
                hipoTDC.setShort("component",  index+1, (short) evioBank.getInt("paddle",i));
                hipoTDC.setByte("order", index+1,(byte) 3);
                hipoTDC.setInt("TDC", index+1, evioBank.getInt("TDCD", i));
                             
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
                hipoADC.setShort("component",  index, (short) evioBank.getInt("paddle",i));
                hipoADC.setByte("order", index,(byte) 0);
                hipoADC.setInt("ADC", index, evioBank.getInt("ADCL", i));
                
                hipoADC.setByte("sector", index+1,      (byte)  evioBank.getInt("sector",i));
                hipoADC.setByte("layer",  index+1,      (byte)  evioBank.getInt("layer",i));
                hipoADC.setShort("component",  index+1, (short) evioBank.getInt("paddle",i));
                hipoADC.setByte("order", index+1,(byte) 1);
                hipoADC.setInt("ADC", index+1, evioBank.getInt("ADCR", i));
                
                hipoTDC.setByte("sector", index,      (byte)  evioBank.getInt("sector",i));
                hipoTDC.setByte("layer",  index,      (byte)  evioBank.getInt("layer",i));
                hipoTDC.setShort("component",  index, (short) evioBank.getInt("paddle",i));
                hipoTDC.setByte("order", index,(byte) 2);
                hipoTDC.setInt("TDC", index, evioBank.getInt("TDCL", i));
                
                hipoTDC.setByte("sector", index+1,      (byte)  evioBank.getInt("sector",i));
                hipoTDC.setByte("layer",  index+1,      (byte)  evioBank.getInt("layer",i));
                hipoTDC.setShort("component",  index+1, (short) evioBank.getInt("paddle",i));
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
            HipoDataBank hipoADC = (HipoDataBank) hipoEvent.createBank("FTOF::adc", rows*2);
            HipoDataBank hipoTDC = (HipoDataBank) hipoEvent.createBank("FTOF::tdc", rows*2);            

            for(int i = 0; i < evioBank.rows(); i++){
                int index = i*2;
                
                hipoADC.setByte("sector", index,      (byte)  evioBank.getInt("sector",i));
                hipoADC.setByte("layer",  index,      (byte)  evioBank.getInt("layer",i));
                hipoADC.setShort("component",  index, (short) evioBank.getInt("paddle",i));
                hipoADC.setByte("order", index,(byte) 0);
                hipoADC.setInt("ADC", index, evioBank.getInt("ADCL", i));
                double tdcl = (double) evioBank.getInt("TDCL", i);
                hipoADC.setFloat("time", i, (float) (tdcl*24.0/1000));
                
                hipoADC.setByte("sector", index+1,      (byte)  evioBank.getInt("sector",i));
                hipoADC.setByte("layer",  index+1,      (byte)  evioBank.getInt("layer",i));
                hipoADC.setShort("component",  index+1, (short) evioBank.getInt("paddle",i));
                hipoADC.setByte("order", index+1,(byte) 1);
                hipoADC.setInt("ADC", index+1, evioBank.getInt("ADCR", i));
                double tdcr = (double) evioBank.getInt("TDCR", i);
                hipoADC.setFloat("time", i, (float) (tdcr*24.0/1000));
                
                hipoTDC.setByte("sector", index,      (byte)  evioBank.getInt("sector",i));
                hipoTDC.setByte("layer",  index,      (byte)  evioBank.getInt("layer",i));
                hipoTDC.setShort("component",  index, (short) evioBank.getInt("paddle",i));
                hipoTDC.setByte("order", index,(byte) 2);
                hipoTDC.setInt("TDC", index, evioBank.getInt("TDCL", i));
                
                hipoTDC.setByte("sector", index+1,      (byte)  evioBank.getInt("sector",i));
                hipoTDC.setByte("layer",  index+1,      (byte)  evioBank.getInt("layer",i));
                hipoTDC.setShort("component",  index+1, (short) evioBank.getInt("paddle",i));
                hipoTDC.setByte("order", index+1,(byte) 3);
                hipoTDC.setInt("TDC", index+1, evioBank.getInt("TDCR", i));
                
                //hipoBank.setInt("ADCL", i, evioBank.getInt("ADCL", i));
                //hipoBank.setInt("ADCR", i, evioBank.getInt("ADCR", i));
                //hipoBank.setInt("TDCL", i, evioBank.getInt("TDCL", i));
                //hipoBank.setInt("TDCR", i, evioBank.getInt("TDCR", i));                
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
        
        int counter = 0;
        if(bankPCAL!=null){
            for(int i = 0; i < bankPCAL.rows(); i++){
                hipoADC.setByte("sector",     counter, (byte)  bankPCAL.getInt("sector",i));
                hipoADC.setByte("layer",      counter, (byte)  bankPCAL.getInt("view",i));
                hipoADC.setShort("component", counter, (short) bankPCAL.getInt("strip",i));
                hipoADC.setByte("order", counter, (byte) 0);
                hipoADC.setInt("ADC", counter, bankPCAL.getInt("ADC", i));
                
                hipoTDC.setByte("sector",     counter, (byte)  bankPCAL.getInt("sector",i));
                hipoTDC.setByte("layer",      counter, (byte)  bankPCAL.getInt("view",i));
                hipoTDC.setShort("component", counter, (short) bankPCAL.getInt("strip",i));
                hipoTDC.setByte("order", counter, (byte) 2);
                hipoTDC.setInt("TDC", counter, bankPCAL.getInt("TDC", i));
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
                hipoADC.setByte("order", counter, (byte) 0);
                hipoADC.setInt("ADC", counter, bankECAL.getInt("ADC", i));
                
                hipoTDC.setByte("sector",     counter, (byte)  bankECAL.getInt("sector",i));
                hipoTDC.setByte("layer",      counter, (byte)  (view+stack*3));
                hipoTDC.setShort("component", counter, (short) bankECAL.getInt("strip",i));
                hipoTDC.setByte("order", counter, (byte) 1);
                hipoTDC.setInt("TDC", counter, bankECAL.getInt("TDC", i));                
                
                counter++;
            }
        }
        if(nrows>0){
            hipoEvent.appendBanks(hipoADC,hipoTDC);
        }
    }
    
    public void fillHipoEventGenPart(HipoDataEvent hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("GenPart::true")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("GenPart::true");
            HipoDataBank hipoBank = (HipoDataBank) hipoEvent.createBank("MC::Particle", evioBank.rows());
            for(int i = 0; i < evioBank.rows(); i++){
                hipoBank.setInt("pid", i, evioBank.getInt("pid", i));
                hipoBank.setFloat("px", i, (float) (evioBank.getDouble("px", i)/1000.0) );
                hipoBank.setFloat("py", i, (float) (evioBank.getDouble("py", i)/1000.0) );
                hipoBank.setFloat("pz", i, (float) (evioBank.getDouble("pz", i)/1000.0) );
                hipoBank.setFloat("vx", i, (float) (evioBank.getDouble("vx", i)) );
                hipoBank.setFloat("vy", i, (float) (evioBank.getDouble("vy", i)) );
                hipoBank.setFloat("vz", i, (float) (evioBank.getDouble("vz", i)) );
            }
            if(evioBank.rows()>0) hipoEvent.appendBanks(hipoBank);
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
