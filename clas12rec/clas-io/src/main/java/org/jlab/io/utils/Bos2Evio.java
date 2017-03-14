/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;
import org.jlab.io.bos.BosDataBank;
import org.jlab.io.bos.BosDataEvent;
import org.jlab.io.bos.BosDataSource;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioDataSync;
import org.jlab.io.evio.EvioFactory;

/**
 *
 * @author gavalian
 */
public class Bos2Evio {
    
    public static void printUsage(){
        System.out.println("\n\t Usage: bos2evio [flag] outputfile.evio input1.bos [input2.bos] .... ");
        System.out.println("\n flags:");
        System.out.println("\t -seb : SEB bank output (EVNT, ECPB, SCPB, CCPB, TGPB)");
        System.out.println("\t -a1c : a1c bank output (PART, TBID, TBER)");
        System.out.println("\n\n");
    }
    
    public static void main(String[] args){
        
        if(args.length<3){
            Bos2Evio.printUsage();
            System.exit(0);
        }
        
        EvioFactory.getDictionary().getDescriptor("HEADER::info").show();
        EvioFactory.getDictionary().getDescriptor("PART::particle").show();
        
        String method = args[0];
        
        if(method.compareTo("-seb")==0||method.compareTo("-a1c")==0){
            
        } else {
            System.out.println("\n\n ERROR: unknown flag " + method);
            Bos2Evio.printUsage();
            System.exit(0);
        }
        
        String output = args[1];
        
        File file = new File(output);
        if(file.exists()==true){
            System.out.println("\n\n ERROR : output file " + output + 
                    "  already exists. Can not override.\n\n");
            System.exit(0);
        }
        
        ArrayList<String> inputfiles = new ArrayList<String>();
        for(int loop = 2; loop < args.length; loop++){
            inputfiles.add(args[loop]);
        }
        
        Bos2EvioPartBank   convertPART = new Bos2EvioPartBank();
        Bos2EvioEventBank  convertEVNT = new Bos2EvioEventBank();
        EvioDataSync  writer = new EvioDataSync();
        writer.open(output);
        
        for(String inFile : inputfiles){
            BosDataSource reader = new BosDataSource();                      
            reader.open(inFile);
            int counter = 0;
            while(reader.hasEvent()){
                counter++;
                //System.out.println("------------------> ANALYZING BUFFER # " + counter);
                BosDataEvent bosEvent = (BosDataEvent) reader.getNextEvent();
                if(bosEvent!=null){
                /*if(bosEvent.hasBank("HEAD")==true){
                    BosDataBank header = (BosDataBank) bosEvent.getBank("HEAD");
                    header.show();
                    BosDataBank hevt = (BosDataBank) bosEvent.getBank("HEVT");
                    hevt.show();
                    bosEvent.showBank("HEVT", 0);
                    bosEvent.showBank("EVNT", 0);
                    System.out.println(" EVNT = " + bosEvent.hasBank("EVNT")
                    + "  HEVT = " + bosEvent.hasBank("HEVT"));
                }*/
                //convertPART.processBosEvent(bosEvent);
                EvioDataEvent outevent = writer.createEvent(EvioFactory.getDictionary());
                /*
                for(Map.Entry<String,EvioDataBank> banks : convertPART.bankStore().entrySet()){
                    outevent.appendBank(banks.getValue());
                }*/
                //=============================================================
                //*** FILLING WITH EVNT Schema
                //=============================================================
                if(method.compareTo("-seb")==0){
                    try {
                    convertEVNT.processBosEvent(bosEvent);                   
                    TreeMap<String,EvioDataBank> evioBanks = convertEVNT.getEvioBankStore();
                    if(evioBanks.containsKey("HEVT")==true&&evioBanks.containsKey("EVNT")==true){
                        /*if(bosEvent.hasBank("HEAD")==true){
                            BosDataBank header = (BosDataBank) bosEvent.getBank("HEAD");
                            header.show();
                        }*/
                        
                        outevent.appendBank(evioBanks.get("HEVT"));
                        outevent.appendBank(evioBanks.get("EVNT"));
                        
                        if(evioBanks.containsKey("TAGR")==true){
                            outevent.appendBank(evioBanks.get("TAGR"));
                        }
                        ArrayList<EvioDataBank>  detectorBanks = new ArrayList<EvioDataBank>();
                        if(evioBanks.containsKey("ECPB")==true){
                            //outevent.appendBank(evioBanks.get("ECPB"));
                            detectorBanks.add(evioBanks.get("ECPB"));
                        }
                        if(evioBanks.containsKey("LCPB")==true){
                            //outevent.appendBank(evioBanks.get("ECPB"));
                            detectorBanks.add(evioBanks.get("LCPB"));
                        }
                        if(evioBanks.containsKey("SCPB")==true){
                            //outevent.appendBank(evioBanks.get("ECPB"));
                            detectorBanks.add(evioBanks.get("SCPB"));
                        }
                        if(evioBanks.containsKey("CCPB")==true){
                            //outevent.appendBank(evioBanks.get("ECPB"));
                            detectorBanks.add(evioBanks.get("CCPB"));
                        }
                        if(detectorBanks.size()>0){
                            EvioDataBank[] pb = new EvioDataBank[detectorBanks.size()];
                            for(int loop = 0; loop < detectorBanks.size(); loop++){
                                pb[loop] = detectorBanks.get(loop);
                            }
                            outevent.appendBanks(pb);
                        }
                        writer.writeEvent(outevent);
                    }
                    }catch (Exception e) {
                        System.out.println(" BUFFER IS EMPTY");
                        bosEvent.dumpBufferToFile();
                    }
                }
                }
                //=============================================================
                //*** END OF EVNT Method Fill
                //=============================================================
            }
            reader.close();
        }
        
        writer.close();
    }
    
}
