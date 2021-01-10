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
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioSource;
import org.jlab.io.evio.EvioTreeBranch;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.SchemaFactory;
import org.jlab.jnp.hipo4.io.HipoWriter;
import org.jlab.utils.benchmark.ProgressPrintout;
import org.jlab.utils.options.OptionParser;
import org.jlab.utils.system.ClasUtilsFile;

/**
 *
 * @author gavalian
 */
public class EvioHipoEvent4 {
    
    
    private SchemaFactory        schemaFactory = new SchemaFactory();

    public EvioHipoEvent4() {
        String dir = ClasUtilsFile.getResourceDir("CLAS12DIR", "etc/bankdefs/hipo4");
        schemaFactory.initFromDirectory(dir);

    }
    
    
    
    public Event getHipoEvent(HipoWriter writer, EvioDataEvent event){        
        Event hipoEvent = new Event();
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
    
    
    public void fillHipoEventRF(Event hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("RF::info")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("RF::info");
            int nrows = evioBank.rows();
            Bank  hipoBank = new Bank(schemaFactory.getSchema("RUN::rf"), nrows);
            for(int i = 0; i < nrows; i++){
                hipoBank.putShort("id", i, (short) evioBank.getInt("id", i));
                double rf_time = evioBank.getDouble("time", i);
                //System.out.println(" RF time = " + rf_time);
                hipoBank.putFloat("time", i, (float) evioBank.getDouble("rf", i));
            }
            hipoEvent.write(hipoBank);
        }
    }

    public void fillHipoEventRICH(Event hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("RICH::dgtz")==true){
            try {
                EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("RICH::dgtz");
                
                int nrows = evioBank.rows();
                Bank  hipoBankADC = new Bank(schemaFactory.getSchema("RICH::adc"), nrows);
                Bank  hipoBankTDC = new Bank(schemaFactory.getSchema("RICH::tdc"), nrows);
                for(int i = 0; i < nrows; i++){
                	// RICH ADC Hipo Bank
                    hipoBankADC.putByte("sector",   i,    (byte) evioBank.getInt("sector", i));
                    hipoBankADC.putShort("pmt",     i,   (short)  evioBank.getInt("pmt",i));
                    hipoBankADC.putShort("pixel",   i,    (short) evioBank.getInt("pixel",i));
                    hipoBankADC.putByte("order",    i,    (byte) 0);
                    hipoBankADC.putInt("ADC",       i,    (int) evioBank.getInt("ADC", i));
                    // At the moment these variables are not in used
                    hipoBankADC.putFloat("time",    i,    (float) (0.0));
                    hipoBankADC.putInt("ped",       i,    (int) (0.0));
                    
                    
                    // RICH TDC Hipo Bank
                    hipoBankTDC.putByte("sector",   i,    (byte) evioBank.getInt("sector", i));
                    hipoBankTDC.putShort("pmt",     i,   (short)  evioBank.getInt("pmt",i));
                    hipoBankTDC.putShort("pixel",   i,    (short) evioBank.getInt("pixel",i));
                    hipoBankTDC.putInt("TDC1",      i,    (int) evioBank.getInt("TDC1",i));
                    hipoBankTDC.putInt("TDC2",      i,    (int) evioBank.getInt("TDC2", i));                                      
                }
                hipoEvent.write(hipoBankADC);
                hipoEvent.write(hipoBankTDC);
            } catch (Exception e) {
                System.out.println("[hipo-decoder]  >>>> error writing RICH bank");
            }
        }
    }

    public void fillHipoEventHTCC(Event hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("HTCC::dgtz")==true){
            //System.out.println("LTCC bank is present");
            try {
                EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("HTCC::dgtz");
                //evioBank.show();
                
                int nrows = evioBank.rows();
                Bank  hipoBank = new Bank(schemaFactory.getSchema("HTCC::adc"), nrows);
                for(int i = 0; i < nrows; i++){
                    hipoBank.putByte("sector", i, (byte) evioBank.getInt("sector",i));
                    hipoBank.putByte("layer", i, (byte) evioBank.getInt("half",i));
                    hipoBank.putShort("component", i, (short) evioBank.getInt("ring",i));
                    hipoBank.putInt("ADC", i, evioBank.getInt("nphe", i)*100);
                    hipoBank.putFloat("time", i, (float) evioBank.getDouble("time", i));
                    hipoBank.putShort("ped", i, (short) 0);
                }
                hipoEvent.write(hipoBank);
            } catch (Exception e) {
                System.out.println("[hipo-decoder]  >>>> error writing LTCC bank");
            }
        }
    }
    
    public void fillHipoEventLTCC(Event hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("LTCC::dgtz")==true){
            //System.out.println("LTCC bank is present");
            try {
                EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("LTCC::dgtz");
                //evioBank.show();
                
                int nrows = evioBank.rows();
                Bank  hipoBank = new Bank(schemaFactory.getSchema("LTCC::adc"), nrows);
                for(int i = 0; i < nrows; i++){
                    hipoBank.putByte("sector", i, (byte) evioBank.getInt("sector",i));
                    hipoBank.putByte("layer", i, (byte) evioBank.getInt("side",i));
                    hipoBank.putShort("component", i, (short) evioBank.getInt("segment",i));
                    hipoBank.putInt("ADC", i, evioBank.getInt("adc", i));
                    hipoBank.putFloat("time", i, (float) evioBank.getDouble("time",i));
                    hipoBank.putShort("ped", i, (short) 0);
                }
                hipoEvent.write(hipoBank);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("[hipo-decoder]  >>>> error writing LTCC bank");
            }
        }
    }
    
    public void fillHipoEventBST(Event hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("BST::dgtz")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("BST::dgtz");
            Bank hipoADC = new Bank(schemaFactory.getSchema("BST::adc"), evioBank.rows());
            for(int i = 0; i < evioBank.rows(); i++){
                hipoADC.putByte("sector", i, (byte) evioBank.getInt("sector",i));
                hipoADC.putByte("layer",  i, (byte) evioBank.getInt("layer",i));
                hipoADC.putShort("component",  i, (short) evioBank.getInt("strip",i));
                hipoADC.putInt("ADC",  i, (byte) evioBank.getInt("ADC",i));
                hipoADC.putFloat("time",  i, (float) evioBank.getInt("bco",i));
                
            }
            hipoEvent.write(hipoADC);
        }
    }
    
    public void fillHipoEventBMT(Event hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("BMT::dgtz")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("BMT::dgtz");
            Bank hipoADC = new Bank(schemaFactory.getSchema("BMT::adc"), evioBank.rows());
            for(int i = 0; i < evioBank.rows(); i++){
                hipoADC.putByte("sector", i, (byte) evioBank.getInt("sector",i));
                hipoADC.putByte("layer",  i, (byte) evioBank.getInt("layer",i));
                hipoADC.putShort("component",  i, (short) evioBank.getInt("strip",i));
                hipoADC.putInt("ADC",  i, (int) evioBank.getInt("ADC",i));
                hipoADC.putFloat("time",  i, (float) 0);
                hipoADC.putShort("ped", i, (short) 0);            
            }
            hipoEvent.write(hipoADC);
        }
    }
    
    public void fillHipoEventFMT(Event hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("FMT::dgtz")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("FMT::dgtz");
            Bank hipoADC = new Bank(schemaFactory.getSchema("FMT::adc"), evioBank.rows());
            for(int i = 0; i < evioBank.rows(); i++){
                hipoADC.putByte("sector", i, (byte) evioBank.getInt("sector",i));
                hipoADC.putByte("layer",  i, (byte) evioBank.getInt("layer",i));
                hipoADC.putShort("component",  i, (short) evioBank.getInt("strip",i));
                hipoADC.putInt("ADC",  i, (int) evioBank.getInt("ADC",i));
                hipoADC.putFloat("time",  i, (float) 0);
                hipoADC.putShort("ped", i, (short) 0);            
            }
            hipoEvent.write(hipoADC);
        }
    }
    
    public void fillHipoEventDC(Event hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("DC::dgtz")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("DC::dgtz");
            Bank hipoTDC = new Bank(schemaFactory.getSchema("DC::tdc"), evioBank.rows());
            Bank hipoDOCA = new Bank(schemaFactory.getSchema("DC::doca"), evioBank.rows());            
            for(int i = 0; i < evioBank.rows(); i++){
                hipoTDC.putByte("sector", i, (byte) evioBank.getInt("sector",i));
                hipoTDC.putByte("layer",  i, (byte) evioBank.getInt("layer",i));
                hipoTDC.putShort("component",  i, (short) evioBank.getInt("wire",i));
                hipoTDC.putByte("order", i, (byte) 2);
                hipoTDC.putInt("TDC", i, evioBank.getInt("TDC", i));
                hipoDOCA.putByte("LR", i, (byte) evioBank.getInt("LR", i));
                hipoDOCA.putFloat("doca", i, (float) evioBank.getDouble("doca", i));
//                hipoDOCA.putFloat("sdoca", i, (float) evioBank.getDouble("sdoca", i));
                hipoDOCA.putFloat("time", i, (float) evioBank.getDouble("time", i));
                hipoDOCA.putFloat("stime", i, (float) evioBank.getDouble("stime", i));
            }
            hipoEvent.write(hipoTDC);
            hipoEvent.write(hipoDOCA);
        }
    }
    
	public void fillHipoEventRTPC(Event hipoEvent, EvioDataEvent evioEvent){
	    if(evioEvent.hasBank("RTPC::dgtz")==true){
			EvioDataBank evioBankdgtz = (EvioDataBank) evioEvent.getBank("RTPC::dgtz");
            Bank hipoADC = new Bank(schemaFactory.getSchema("RTPC::adc"), evioBankdgtz.rows());     
            for(int i = 0; i < evioBankdgtz.rows(); i++){
                hipoADC.putByte("sector", i, (byte) 1);
                hipoADC.putByte("layer",  i, (byte) evioBankdgtz.getInt("Layer",i));
                hipoADC.putShort("component",  i, (short) evioBankdgtz.getInt("Component",i));
                hipoADC.putByte("order", i, (byte) 0);
                hipoADC.putInt("ADC", i, (int) evioBankdgtz.getInt("ADC", i));
                hipoADC.putFloat("time", i, (float) evioBankdgtz.getDouble("Time", i));
                hipoADC.putShort("ped", i, (short) 0);
            }
            hipoEvent.write(hipoADC);
        }
    }
    
    public void fillHipoEventCTOF(Event hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("CTOF::dgtz")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("CTOF::dgtz");
            Bank hipoADC = new Bank(schemaFactory.getSchema("CTOF::adc"), evioBank.rows());
            Bank hipoTDC = new Bank(schemaFactory.getSchema("CTOF::tdc"), evioBank.rows());
            for(int i = 0; i < evioBank.rows(); i++){
                int index = i;
                
                hipoADC.putByte("sector", index,      (byte)  1);
                hipoADC.putByte("layer",  index,      (byte)  1);
                hipoADC.putShort("component",  index, (short) evioBank.getInt("paddle",i));
                hipoADC.putByte("order", index,(byte) evioBank.getInt("side", i));
                hipoADC.putInt("ADC", index, evioBank.getInt("ADC", i));
                double tdc = (double) evioBank.getInt("TDC", i);
                hipoADC.putFloat("time", i, (float) (tdc*24.0/1000));
                
                hipoTDC.putByte("sector", index,      (byte)  1);
                hipoTDC.putByte("layer",  index,      (byte)  1);
                hipoTDC.putShort("component",  index, (short) evioBank.getInt("paddle",i));
                hipoTDC.putByte("order", index,(byte) (byte) (evioBank.getInt("side", i)+2));
                hipoTDC.putInt("TDC", index, evioBank.getInt("TDC", i));
                
            }
            hipoEvent.write(hipoADC);
            hipoEvent.write(hipoTDC);
        }
    }
    
    public void fillHipoEventCND(Event hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("CND::dgtz")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("CND::dgtz");
            Bank hipoADC = new Bank(schemaFactory.getSchema("CND::adc"), evioBank.rows()*2);
            Bank hipoTDC = new Bank(schemaFactory.getSchema("CND::tdc"), evioBank.rows()*2);
            for(int i = 0; i < evioBank.rows(); i++){
                int index = i*2;
                
                hipoADC.putByte("sector", index,      (byte)  evioBank.getInt("sector",i));
                hipoADC.putByte("layer",  index,      (byte)  evioBank.getInt("layer",i));
                hipoADC.putShort("component",  index, (short) evioBank.getInt("component",i));
                hipoADC.putByte("order", index,(byte) 0);
                hipoADC.putInt("ADC", index, evioBank.getInt("ADCL", i));
                double tdcl = (double) evioBank.getInt("TDCL", i);
                hipoADC.putFloat("time", index, (float) (tdcl*24.0/1000));
                
                hipoADC.putByte("sector", index+1,      (byte)  evioBank.getInt("sector",i));
                hipoADC.putByte("layer",  index+1,      (byte)  evioBank.getInt("layer",i));
                hipoADC.putShort("component",  index+1, (short) evioBank.getInt("component",i));
                hipoADC.putByte("order", index+1,(byte) 1);
                hipoADC.putInt("ADC", index+1, evioBank.getInt("ADCR", i));
                double tdcr = (double) evioBank.getInt("TDCR", i);
                hipoADC.putFloat("time", index+1, (float) (tdcr*24.0/1000));
                
                hipoTDC.putByte("sector", index,      (byte)  evioBank.getInt("sector",i));
                hipoTDC.putByte("layer",  index,      (byte)  evioBank.getInt("layer",i));
                hipoTDC.putShort("component",  index, (short) evioBank.getInt("component",i));
                hipoTDC.putByte("order", index,(byte) 2);
                hipoTDC.putInt("TDC", index, evioBank.getInt("TDCL", i));
                
                hipoTDC.putByte("sector", index+1,      (byte)  evioBank.getInt("sector",i));
                hipoTDC.putByte("layer",  index+1,      (byte)  evioBank.getInt("layer",i));
                hipoTDC.putShort("component",  index+1, (short) evioBank.getInt("component",i));
                hipoTDC.putByte("order", index+1,(byte) 3);
                hipoTDC.putInt("TDC", index+1, evioBank.getInt("TDCR", i));
                             
            }
            hipoEvent.write(hipoADC);
            hipoEvent.write(hipoTDC);
        }
    }
    
    public void fillHipoEventFTCAL(Event hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("FTCAL::dgtz")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("FTCAL::dgtz");
            int rows = evioBank.rows();
            Bank hipoADC = new Bank(schemaFactory.getSchema("FTCAL::adc"), rows);
            for(int i = 0; i < rows; i++){
                hipoADC.putByte("sector", i,      (byte)  evioBank.getInt("sector",i));
                hipoADC.putByte("layer",  i,      (byte)  evioBank.getInt("layer",i));
                hipoADC.putShort("component",  i, (short) evioBank.getInt("component",i));
                hipoADC.putByte("order", i,(byte) 0);
                hipoADC.putInt("ADC", i, evioBank.getInt("ADC", i));
                double tdc = (double) evioBank.getInt("TDC", i);
                hipoADC.putFloat("time", i, (float) (tdc/25.0));
            }
            hipoEvent.write(hipoADC);
        }
    }
    
    public void fillHipoEventFTHODO(Event hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("FTHODO::dgtz")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("FTHODO::dgtz");
            int rows = evioBank.rows();
            Bank hipoADC = new Bank(schemaFactory.getSchema("FTHODO::adc"), rows);
            for(int i = 0; i < rows; i++){
                hipoADC.putByte("sector", i,      (byte)  evioBank.getInt("sector",i));
                hipoADC.putByte("layer",  i,      (byte)  evioBank.getInt("layer",i));
                hipoADC.putShort("component",  i, (short) evioBank.getInt("component",i));
                hipoADC.putByte("order", i,(byte) 0);
                hipoADC.putInt("ADC", i, evioBank.getInt("ADC", i));
                double tdc = (double) evioBank.getInt("TDC", i);
                hipoADC.putFloat("time", i, (float) (tdc/25.0));
            }
            hipoEvent.write(hipoADC);
        }
    }
    
   public void fillHipoEventFTTRK(Event hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("FTTRK::dgtz")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("FTTRK::dgtz");
            int rows = evioBank.rows();
            Bank hipoADC = new Bank(schemaFactory.getSchema("FTTRK::adc"), rows);
            for(int i = 0; i < rows; i++){
                hipoADC.putByte("sector", i,      (byte)  evioBank.getInt("sector",i));
                hipoADC.putByte("layer",  i,      (byte)  evioBank.getInt("layer",i));
                hipoADC.putShort("component",  i, (short) evioBank.getInt("component",i));
                hipoADC.putByte("order", i,(byte) 0);
                hipoADC.putInt("ADC", i, evioBank.getInt("ADC", i));
                double tdc = 0;
                hipoADC.putFloat("time", i, (float) (tdc/25.0));
            }
            hipoEvent.write(hipoADC);
        }
    }
    
    public void fillHipoEventFTOF(Event hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("FTOF::dgtz")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("FTOF::dgtz");
            int rows = evioBank.rows();
            Bank hipoADC = new Bank(schemaFactory.getSchema("FTOF::adc"), rows);
            Bank hipoTDC = new Bank(schemaFactory.getSchema("FTOF::tdc"), rows);            

            for(int i = 0; i < evioBank.rows(); i++){
                int index = i;
                
                hipoADC.putByte("sector", index,      (byte)  evioBank.getInt("sector",i));
                hipoADC.putByte("layer",  index,      (byte)  evioBank.getInt("layer",i));
                hipoADC.putShort("component",  index, (short) evioBank.getInt("paddle",i));
                hipoADC.putByte("order", index,(byte) evioBank.getInt("side", i));
                hipoADC.putInt("ADC", index, evioBank.getInt("ADC", i));
                double tdc = (double) evioBank.getInt("TDC", i);
                hipoADC.putFloat("time", i, (float) (tdc*24.0/1000));
                
                hipoTDC.putByte("sector", index,      (byte)  evioBank.getInt("sector",i));
                hipoTDC.putByte("layer",  index,      (byte)  evioBank.getInt("layer",i));
                hipoTDC.putShort("component",  index, (short) evioBank.getInt("paddle",i));
                hipoTDC.putByte("order", index,(byte) (evioBank.getInt("side", i)+2));
                hipoTDC.putInt("TDC", index, evioBank.getInt("TDC", i));
                
            }
            hipoEvent.write(hipoADC);
            hipoEvent.write(hipoTDC);
        }
    }
    /**
     * Fill the ECAL EVENT combines PCAL with EC
     * @param hipoEvent
     * @param evioEvent 
     */
    public void fillHipoEventECAL(Event hipoEvent, EvioDataEvent evioEvent){
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
        
        Bank hipoADC = new Bank(schemaFactory.getSchema("ECAL::adc"),nrows);
        Bank hipoTDC = new Bank(schemaFactory.getSchema("ECAL::tdc"),nrows);
       
        final float tdc2ns = 0.02345f;

        int counter = 0;
        if(bankPCAL!=null){
            for(int i = 0; i < bankPCAL.rows(); i++){
                hipoADC.putByte("sector",     counter, (byte)  bankPCAL.getInt("sector",i));
                hipoADC.putByte("layer",      counter, (byte)  bankPCAL.getInt("view",i));
                hipoADC.putShort("component", counter, (short) bankPCAL.getInt("strip",i));
                hipoADC.putByte("order",      counter, (byte) 0);
                hipoADC.putInt("ADC",         counter, bankPCAL.getInt("ADC", i));
                hipoADC.putFloat("time",      counter, (float) bankPCAL.getInt("TDC",i)*tdc2ns);
                
                hipoTDC.putByte("sector",     counter, (byte)  bankPCAL.getInt("sector",i));
                hipoTDC.putByte("layer",      counter, (byte)  bankPCAL.getInt("view",i));
                hipoTDC.putShort("component", counter, (short) bankPCAL.getInt("strip",i));
                hipoTDC.putByte("order",      counter, (byte) 2);
                hipoTDC.putInt("TDC",         counter, bankPCAL.getInt("TDC", i));
                counter++;
            }
        }
        
        if(bankECAL!=null){
            for(int i = 0; i < bankECAL.rows(); i++){
                int stack = bankECAL.getInt("stack",i);
                int view  = bankECAL.getInt("view",i);           
                hipoADC.putByte("sector",     counter, (byte)  bankECAL.getInt("sector",i));
                hipoADC.putByte("layer",      counter, (byte)  (view+stack*3));
                hipoADC.putShort("component", counter, (short) bankECAL.getInt("strip",i));
                hipoADC.putByte("order",      counter, (byte) 0);
                hipoADC.putInt("ADC",         counter, bankECAL.getInt("ADC", i));
                hipoADC.putFloat("time",      counter, (float) bankECAL.getInt("TDC",i)*tdc2ns);
                
                hipoTDC.putByte("sector",     counter, (byte)  bankECAL.getInt("sector",i));
                hipoTDC.putByte("layer",      counter, (byte)  (view+stack*3));
                hipoTDC.putShort("component", counter, (short) bankECAL.getInt("strip",i));
                hipoTDC.putByte("order",      counter, (byte) 1);
                hipoTDC.putInt("TDC",         counter, bankECAL.getInt("TDC", i));                
                
                counter++;
            }
        }
        if(nrows>0){
            hipoEvent.write(hipoADC);
            hipoEvent.write(hipoTDC);
        }
    }
    
    public void fillHipoEventGenPart(Event hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("GenPart::header")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("GenPart::header");
            Bank hipoBank = new Bank(schemaFactory.getSchema("MC::Header"), evioBank.rows());
            for(int i = 0; i < evioBank.rows(); i++){
                hipoBank.putInt("run",        i, (evioBank.getInt("runNo", i)) );
                hipoBank.putInt("event",      i, (evioBank.getInt("evn", i)) );
                hipoBank.putByte("type",      i, (byte) (evioBank.getInt("evn_type", i)) );
                hipoBank.putFloat("helicity", i, (float) (evioBank.getDouble("helicity", i)) );
            }
            if(evioBank.rows()>0) hipoEvent.write(hipoBank);
        }
        if(evioEvent.hasBank("GenPart::true")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("GenPart::true");
            Bank hipoBank = new Bank(schemaFactory.getSchema("MC::Particle"), evioBank.rows());
            for(int i = 0; i < evioBank.rows(); i++){
                hipoBank.putInt("pid", i, evioBank.getInt("pid", i));
                hipoBank.putFloat("px", i, (float) (evioBank.getDouble("px", i)/1000.0) );
                hipoBank.putFloat("py", i, (float) (evioBank.getDouble("py", i)/1000.0) );
                hipoBank.putFloat("pz", i, (float) (evioBank.getDouble("pz", i)/1000.0) );
                hipoBank.putFloat("vx", i, (float) (evioBank.getDouble("vx", i)/10.0) );
                hipoBank.putFloat("vy", i, (float) (evioBank.getDouble("vy", i)/10.0) );
                hipoBank.putFloat("vz", i, (float) (evioBank.getDouble("vz", i)/10.0) );
                hipoBank.putFloat("vt", i, (float) (evioBank.getDouble("vt", i)) );
            }
            if(evioBank.rows()>0) hipoEvent.write(hipoBank);
        }
        if(evioEvent.hasBank("Lund::header")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("Lund::header");
            Bank hipoBank = new Bank(schemaFactory.getSchema("MC::Event"), evioBank.rows());
            for(int i = 0; i < evioBank.rows(); i++){
                hipoBank.putShort("npart",     i, (short) (evioBank.getDouble("nParticles", i)) );
                hipoBank.putShort("atarget",   i, (short) (evioBank.getDouble("nNucleons", i)) );
                hipoBank.putShort("ztarget",   i, (short) (evioBank.getDouble("nProtons", i)) );
                hipoBank.putFloat("ptarget",   i, (float) (evioBank.getDouble("targetPol", i)) );
                hipoBank.putFloat("pbeam",     i, (float) (evioBank.getDouble("beamPol", i)) );
                hipoBank.putShort("btype",     i, (short) (evioBank.getDouble("beamType", i)) );
                hipoBank.putFloat("ebeam",     i, (float) (evioBank.getDouble("beamEnergy", i)) );
                hipoBank.putShort("targetid",  i, (short) (evioBank.getDouble("targetID", i)) );
                hipoBank.putShort("processid", i, (short) (evioBank.getDouble("processID", i)) );
                hipoBank.putFloat("weight",    i, (float) (evioBank.getDouble("eventWeight", i)) );
            }
            if(evioBank.rows()>0) hipoEvent.write(hipoBank);
        }
        if(evioEvent.hasBank("Lund::true")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("Lund::true");
            Bank hipoBank = new Bank(schemaFactory.getSchema("MC::Lund"), evioBank.rows());
            for(int i = 0; i < evioBank.rows(); i++){
                hipoBank.putByte("index",    i, (byte)  (evioBank.getDouble("index", i)) );
                hipoBank.putFloat("lifetime",i, (float) (evioBank.getDouble("ltime", i)) );
                hipoBank.putByte("type",     i, (byte)  (evioBank.getDouble("type", i)) );
                hipoBank.putInt("pid",       i, (int)   (evioBank.getDouble("pid", i)) );
                hipoBank.putByte("parent",   i, (byte)  (evioBank.getDouble("parentID", i)) );
                hipoBank.putByte("daughter", i, (byte)  (evioBank.getDouble("daughterID", i)) );
                hipoBank.putFloat("px",      i, (float) (evioBank.getDouble("px", i)) );
                hipoBank.putFloat("py",      i, (float) (evioBank.getDouble("py", i)) );
                hipoBank.putFloat("pz",      i, (float) (evioBank.getDouble("pz", i)) );
                hipoBank.putFloat("energy",  i, (float) (evioBank.getDouble("E", i)) );
                hipoBank.putFloat("mass",    i, (float) (evioBank.getDouble("mass", i)) );
                hipoBank.putFloat("vx",      i, (float) (evioBank.getDouble("vx", i)) );
                hipoBank.putFloat("vy",      i, (float) (evioBank.getDouble("vy", i)) );
                hipoBank.putFloat("vz",      i, (float) (evioBank.getDouble("vz", i)) );
            }
            if(evioBank.rows()>0) hipoEvent.write(hipoBank);
        }
    }
    
   public void fillHipoEventTrueInfo(Event hipoEvent, EvioDataEvent evioEvent){
        
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
            Bank hipoBank = new Bank(schemaFactory.getSchema("MC::True"), rows);
            int irow=0;
            for(int k = 0; k < bankNames.length; k++){
                if(evioEvent.hasBank(bankNames[k]+"::true")==true){
                    EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank(bankNames[k]+"::true");
                    for(int i = 0; i < evioBank.rows(); i++){
                        hipoBank.putByte("detector", irow, (byte)  bankTypes[k].getDetectorId());
                        hipoBank.putInt("pid",       irow,         evioBank.getInt("pid",i));
                        hipoBank.putInt("mpid",      irow,         evioBank.getInt("mpid",i));
                        hipoBank.putInt("tid",       irow,         evioBank.getInt("tid",i));
                        hipoBank.putInt("mtid",      irow,         evioBank.getInt("mtid",i));
                        hipoBank.putInt("otid",      irow,         evioBank.getInt("otid",i));
                        hipoBank.putFloat("trackE",  irow, (float) evioBank.getDouble("trackE",i));
                        hipoBank.putFloat("totEdep", irow, (float) evioBank.getDouble("totEdep",i));
                        hipoBank.putFloat("avgX",    irow, (float) evioBank.getDouble("avgX",i));
                        hipoBank.putFloat("avgY",    irow, (float) evioBank.getDouble("avgY",i));
                        hipoBank.putFloat("avgZ",    irow, (float) evioBank.getDouble("avgZ",i));
                        hipoBank.putFloat("avgLx",   irow, (float) evioBank.getDouble("avgLx",i));
                        hipoBank.putFloat("avgLy",   irow, (float) evioBank.getDouble("avgLy",i));
                        hipoBank.putFloat("avgLz",   irow, (float) evioBank.getDouble("avgLz",i));
                        hipoBank.putFloat("px",      irow, (float) evioBank.getDouble("px",i));
                        hipoBank.putFloat("py",      irow, (float) evioBank.getDouble("py",i));
                        hipoBank.putFloat("pz",      irow, (float) evioBank.getDouble("pz",i));
                        hipoBank.putFloat("vx",      irow, (float) evioBank.getDouble("vx",i));
                        hipoBank.putFloat("vy",      irow, (float) evioBank.getDouble("vy",i));
                        hipoBank.putFloat("vz",      irow, (float) evioBank.getDouble("vz",i));
                        hipoBank.putFloat("mvx",     irow, (float) evioBank.getDouble("mvx",i));
                        hipoBank.putFloat("mvy",     irow, (float) evioBank.getDouble("mvy",i));
                        hipoBank.putFloat("mvz",     irow, (float) evioBank.getDouble("mvz",i));
                        hipoBank.putFloat("avgT",    irow, (float) evioBank.getDouble("avgT",i));
                        hipoBank.putInt("nsteps",    irow,         evioBank.getInt("nsteps",i));
                        hipoBank.putInt("procID",    irow,         evioBank.getInt("procID",i));
                        hipoBank.putInt("hitn",      irow,         evioBank.getInt("hitn",i));
                        irow++;
                    }
                }
            }
            hipoEvent.write(hipoBank);
        }
    }
   
   
    public void fillHipoEventTrigger(Event hipoEvent, EvioDataEvent evioEvent){

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
            Logger.getLogger(EvioHipoEvent4.class.getName()).log(Level.SEVERE, null, ex);
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
            Bank hipoVTP = new Bank(schemaFactory.getSchema("RAW::vtp"), rows);
            for(int i = 0; i < rows; i++){
                int crate = crates.get(i);
                hipoVTP.putByte("sector", i,      (byte)  crate);
                hipoVTP.putByte("layer",  i,      (byte)  0);
                hipoVTP.putShort("component",  i, (short) 0);
                hipoVTP.putByte("order",       i,(byte) 0);
                hipoVTP.putInt("word", i, words.get(i));
            }
            hipoEvent.write(hipoVTP);
        }
    }
   
    public Bank createHeaderBank(Event event, int nrun, int nevent, float torus, float solenoid){
        if(schemaFactory.hasSchema("RUN::config")==false) return null;
        
        Bank bank = new Bank(schemaFactory.getSchema("RUN::config"), 1);
        bank.putInt("run",        0, nrun);
        bank.putInt("event",      0, nevent);
        bank.putFloat("torus",    0, torus);
        bank.putFloat("solenoid", 0, solenoid);        
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
        
        
            EvioHipoEvent4 convertor = new EvioHipoEvent4();
            
            HipoWriter  writer = new HipoWriter();
            writer.setCompressionType(2);
            writer.getSchemaFactory().initFromDirectory(ClasUtilsFile.getResourceDir("CLAS12DIR", "etc/bankdefs/hipo4"));
            
            int nrun = parser.getOption("-r").intValue();
            double torus = parser.getOption("-t").doubleValue();
            double solenoid = parser.getOption("-s").doubleValue();
            
            
            writer.open(outputFile);
            ProgressPrintout progress = new ProgressPrintout();
            
            int maximumEvents = parser.getOption("-n").intValue();
            int nevent = 0;
                        
            System.out.println(">>>>>  SIZE OF THE INPUT FILES = " + inputFiles.size());
            
            for(String input : inputFiles){
                System.out.println(">>>>>  appending file : " + input);
                try {
                    EvioSource reader = new EvioSource();
                    reader.open(input);
                    
                    while(reader.hasEvent()==true){
                        EvioDataEvent evioEvent = (EvioDataEvent) reader.getNextEvent();                    
                        Event hipoEvent = convertor.getHipoEvent(writer, evioEvent);
                        
                        
                        Bank   header = convertor.createHeaderBank(hipoEvent, nrun, nevent, (float) torus, (float) solenoid);
                        if(header!=null) hipoEvent.write(header);
                        
                        writer.addEvent(hipoEvent);
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
            System.out.println(">>>>>  processing file  :  success ");
            System.out.println(">>>>>  number of events :  " + nevent);
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
