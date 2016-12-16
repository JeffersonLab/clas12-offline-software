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
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataDescriptor;
import org.jlab.io.evio.EvioDataDictionary;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioFactory;
import org.jlab.io.evio.EvioSource;

/**
 *
 * @author gavalian
 */
public class EvioHipoEvent {
    
    
    
    public HipoDataEvent getHipoEvent(HipoDataSync writer, EvioDataEvent event){        
        HipoDataEvent hipoEvent = (HipoDataEvent) writer.createEvent();        
        this.fillHipoEventFTOF(hipoEvent, event);
        this.fillHipoEventFTCAL(hipoEvent, event);
        this.fillHipoEventDC(hipoEvent, event);
        //this.fillHipoEventCTOF(hipoEvent, event);        
        this.fillHipoEventECAL(hipoEvent, event);

        this.fillHipoEventGenPart(hipoEvent, event);
        return hipoEvent;
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
    
    public void fillHipoEventCTOF(HipoDataEvent hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("CTOF::dgtz")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("CTOF::dgtz");
            HipoDataBank hipoBank = (HipoDataBank) hipoEvent.createBank("CTOF::dgtz", evioBank.rows());
            for(int i = 0; i < evioBank.rows(); i++){
                //hipoBank.setByte("sector", i, (byte) evioBank.getInt("sector",i));
                //hipoBank.setByte("layer",  i, (byte) evioBank.getInt("layer",i));
                hipoBank.setShort("component",  i, (short) evioBank.getInt("paddle",i));
                hipoBank.setInt("ADCU", i, evioBank.getInt("ADCU", i));
                hipoBank.setInt("ADCD", i, evioBank.getInt("ADCD", i));
                hipoBank.setInt("TDCU", i, evioBank.getInt("TDCU", i));
                hipoBank.setInt("TDCD", i, evioBank.getInt("TDCD", i));                
            }
            hipoEvent.appendBanks(hipoBank);
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
                hipoTDC.setByte("order", counter, (byte) 1);
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
    
        
    public static void main(String[] args){
        
        String outputFile = args[0];
        List<String> inputFiles = new ArrayList<String>();
        for(int i = 1; i < args.length; i++){
            inputFiles.add(args[i]);
        }
        
        
        EvioHipoEvent convertor = new EvioHipoEvent();
        
        HipoDataSync  writer = new HipoDataSync();
        writer.open(outputFile);
        writer.setCompressionType(2);
        System.out.println(">>>>>  SIZE OF THE INPUT FILES = " + inputFiles.size());
        for(String input : inputFiles){
            System.out.println(">>>>>  appending file : " + input);
            try {
                EvioSource reader = new EvioSource();
                reader.open(input);
                
                while(reader.hasEvent()==true){
                    EvioDataEvent evioEvent = (EvioDataEvent) reader.getNextEvent();                    
                    HipoDataEvent hipoEvent = convertor.getHipoEvent(writer, evioEvent);
                    writer.writeEvent(hipoEvent);
                }
            } catch (Exception e){
                e.printStackTrace();
                System.out.println(">>>>>  processing file :  failed ");
            }
            System.out.println(">>>>>  processing file :  success ");
            System.out.println();            
        }
        writer.close();
        /*
        EvioDataDictionary dictionary = new EvioDataDictionary("/Users/gavalian/Work/Software/Release-9.0/COATJAVA/coatjava/etc/bankdefs/hipo");
        dictionary.show();
        
        EvioDataDescriptor desc = (EvioDataDescriptor) dictionary.getDescriptor("ECAL::dgtz");
        desc.show();*/
    }
}
