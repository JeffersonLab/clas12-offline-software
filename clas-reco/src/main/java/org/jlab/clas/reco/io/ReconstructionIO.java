/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.reco.io;

import java.util.ArrayList;
import java.util.List;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioDataSync;
import org.jlab.io.evio.EvioFactory;
import org.jlab.io.evio.EvioSource;
import org.jlab.io.hipo.HipoDataSync;

/**
 *
 * @author gavalian
 */
public class ReconstructionIO {
    
    public static void createDST(String filename, List<String> inputFiles){
        HipoDataSync  writer = new HipoDataSync();
        writer.setCompressionType(1);
        writer.open(filename);
        
        for(int i = 0; i < inputFiles.size();i++){
            EvioSource reader = new EvioSource();
            reader.open(inputFiles.get(i));
            while(reader.hasEvent()==true){
                EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
                EvioDataEvent dstEvent = EvioFactory.createEvioEvent();
                
                EvioDataBank  bankP = (EvioDataBank) event.getBank("EVENTTB::particle");
                EvioDataBank  bankD = (EvioDataBank) event.getBank("EVENTTB::detector");
                
                EvioDataBank  bankH = (EvioDataBank) event.getBank("RUN::config");
                EvioDataBank  bankG = (EvioDataBank) event.getBank("GenPart::true");
                
                if(bankG!=null){
                    dstEvent.appendGeneratedBank(bankG);
                }
                
                if(bankH!=null) dstEvent.appendBanks(bankH);
                
                if(bankP!=null&&bankD!=null){
                    dstEvent.appendBanks(bankP,bankD);
                } else if(bankP!=null){
                    dstEvent.appendBanks(bankP);
                }
                writer.writeEvent(dstEvent);
            }
        }
        
        writer.close();
    }
    
    
    public static void writeHeader(String inputEvio, String outputEvio, int run, double torus, double solenoid, int type){
        EvioSource reader = new EvioSource();
        reader.open(inputEvio);
        
        EvioDataSync  writer = new EvioDataSync();
        writer.open(outputEvio);
        
        int evtCounter = 1;
        
        while(reader.hasEvent()==true){
            EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
            EvioDataBank  bankHeader = EvioFactory.createBank("RUN::config", 1);
            
            bankHeader.setInt("Run"  ,  0, run);
            bankHeader.setInt("Event",  0, evtCounter);
            bankHeader.setByte("Type"  , 0, (byte) type);
            bankHeader.setFloat("Torus", 0, (float) torus);
            bankHeader.setFloat("Solenoid", 0, (float) solenoid);
            event.appendBanks(bankHeader);
            writer.writeEvent(event);
            evtCounter++;
        }
        writer.close();
    }
    
    public static void main(String[] args){
        
        String command = args[0];
        
        if(command.compareTo("-dst")==0){
            String outputFile = args[1];
            List<String>  inputFiles = new ArrayList<String>();
            for(int i = 2; i < args.length; i++){
                inputFiles.add(args[i]);
            }
            
            ReconstructionIO.createDST(outputFile, inputFiles);
            System.out.println("\n\n -----> conversion done....\n");
            System.exit(0);
        }
        
        
        String input    = args[0];
        
        int    run      = Integer.parseInt(args[1]);
        double torus    = Double.parseDouble(args[2]);
        double solenoid = Double.parseDouble(args[3]);
        int type = 0;
        
        if(args.length>4) type = Integer.parseInt(args[4]);
        ReconstructionIO.writeHeader(input, "output.evio" , run, torus, solenoid,type);
    }
}
