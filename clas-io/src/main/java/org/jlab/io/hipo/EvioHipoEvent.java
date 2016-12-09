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
        this.fillHipoEventECAL(hipoEvent, event);
        this.fillHipoEventDC(hipoEvent, event);
        this.fillHipoEventGenPart(hipoEvent, event);
        return hipoEvent;
    }
    
    public void fillHipoEventDC(HipoDataEvent hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("DC::dgtz")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("DC::dgtz");
            HipoDataBank hipoBank = (HipoDataBank) hipoEvent.createBank("DC::dgtz", evioBank.rows());
            for(int i = 0; i < evioBank.rows(); i++){
                hipoBank.setByte("sector", i, (byte) evioBank.getInt("sector",i));
                hipoBank.setByte("layer",  i, (byte) evioBank.getInt("layer",i));
                hipoBank.setShort("component",  i, (short) evioBank.getInt("wire",i));
                hipoBank.setInt("TDC", i, evioBank.getInt("TDC", i));
                hipoBank.setByte("LR", i, (byte) evioBank.getInt("LR", i));                
            }
            hipoEvent.appendBanks(hipoBank);
        }
    }
    
    public void fillHipoEventFTOF(HipoDataEvent hipoEvent, EvioDataEvent evioEvent){
        if(evioEvent.hasBank("FTOF::dgtz")==true){
            EvioDataBank evioBank = (EvioDataBank) evioEvent.getBank("FTOF::dgtz");
            HipoDataBank hipoBank = (HipoDataBank) hipoEvent.createBank("FTOF::dgtz", evioBank.rows());
            for(int i = 0; i < evioBank.rows(); i++){
                hipoBank.setByte("sector", i, (byte) evioBank.getInt("sector",i));
                hipoBank.setByte("layer",  i, (byte) evioBank.getInt("layer",i));
                hipoBank.setShort("component",  i, (short) evioBank.getInt("paddle",i));
                hipoBank.setInt("ADCL", i, evioBank.getInt("ADCL", i));
                hipoBank.setInt("ADCR", i, evioBank.getInt("ADCR", i));
                hipoBank.setInt("TDCL", i, evioBank.getInt("TDCL", i));
                hipoBank.setInt("TDCR", i, evioBank.getInt("TDCR", i));                
            }
            hipoEvent.appendBanks(hipoBank);
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
        
        HipoDataBank hipoECAL = (HipoDataBank) hipoEvent.createBank("ECAL::dgtz",nrows);
        
        int counter = 0;
        if(bankPCAL!=null){
            for(int i = 0; i < bankPCAL.rows(); i++){
                hipoECAL.setByte("sector",     counter, (byte)  bankPCAL.getInt("sector",i));
                hipoECAL.setByte("layer",      counter, (byte)  bankPCAL.getInt("view",i));
                hipoECAL.setShort("component", counter, (short) bankPCAL.getInt("strip",i));
                hipoECAL.setInt("ADC", counter, bankPCAL.getInt("ADC", i));
                hipoECAL.setInt("TDC", counter, bankPCAL.getInt("TDC", i));                
                counter++;
            }
        }
        
        if(bankECAL!=null){
            for(int i = 0; i < bankECAL.rows(); i++){
                int stack = bankECAL.getInt("stack",i);
                int view  = bankECAL.getInt("view",i);                
                hipoECAL.setByte("sector",     counter, (byte)  bankECAL.getInt("sector",i));
                hipoECAL.setByte("layer",      counter, (byte)  (view+stack*3));
                hipoECAL.setShort("component", counter, (short) bankECAL.getInt("strip",i));
                hipoECAL.setInt("ADC", counter, bankECAL.getInt("ADC", i));
                hipoECAL.setInt("TDC", counter, bankECAL.getInt("TDC", i));                
                counter++;
            }
        }
        if(nrows>0){
            hipoEvent.appendBanks(hipoECAL);
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
        //writer.setCompressionType(2);
        
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
