/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import org.jlab.io.bos.BosDataBank;
import org.jlab.io.bos.BosDataEvent;
import org.jlab.io.bos.BosDataSource;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioFactory;
import org.jlab.io.hipo.HipoDataSync;

/**
 *
 * @author gavalian
 */
public class Bos2EvioEventBank {
    //private ArrayList<EvioDataBank>       evioDataBanks = new ArrayList<EvioDataBank>();
    private TreeMap<String,EvioDataBank>  evioDataBanks = new TreeMap<String,EvioDataBank>();
    private TreeMap<String,BosDataBank>    bosDataBanks = new TreeMap<String,BosDataBank>();
    
    public Bos2EvioEventBank(){
        
    }
    
    public void clear(){
        this.evioDataBanks.clear();
    }
    
    public void processBosEvent(BosDataEvent event){
        this.initBosBanks(event);
        this.initEvioBank();
    }
    
    
    public void initEvioBank(){
        this.evioDataBanks.clear();
        byte helicity = (byte) -1;
        if(this.bosDataBanks.containsKey("TGBI")==true){
            int[] harray = this.bosDataBanks.get("TGBI").getInt("latch1");
            if(harray.length>0){
                if((harray[0]&0x00008000)>0){
                    helicity = 1;
                }
            }
            //if()
        }
        
        if(this.bosDataBanks.containsKey("HEVT")==true){
            EvioDataBank  bankHEVT = EvioFactory.createBank("HEADER::info", 1);
            bankHEVT.setInt("nrun", 0, this.bosDataBanks.get("HEVT").getInt("NRUN")[0]);
            bankHEVT.setInt("nevt", 0, this.bosDataBanks.get("HEVT").getInt("NEVENT")[0]);
            bankHEVT.setFloat("stt" , 0, this.bosDataBanks.get("HEVT").getFloat("STT")[0]);
            bankHEVT.setFloat("fc"  , 0, this.bosDataBanks.get("HEVT").getFloat("FC")[0]);
            bankHEVT.setByte("helicity" , 0, helicity);
            bankHEVT.setFloat("fcg" , 0, this.bosDataBanks.get("HEVT").getFloat("FCG")[0]);
            this.evioDataBanks.put("HEVT", bankHEVT);
        }
        /*
        * Writing EVNT bank
        */
        if(this.bosDataBanks.containsKey("EVNT")==true){
            BosDataBank bEVNT = (BosDataBank) this.bosDataBanks.get("EVNT");
            int nrows = bEVNT.rows();
            
            EvioDataBank  evioEVNTp = EvioFactory.createBank("EVENT::particle", nrows);
            for(int loop = 0; loop < nrows; loop++){
                evioEVNTp.setByte("status", loop, (byte) bEVNT.getInt("Status")[loop]);
                evioEVNTp.setByte("charge", loop, (byte) bEVNT.getInt("Charge")[loop]);
                
                evioEVNTp.setByte("dcstat", loop, (byte) bEVNT.getInt("DCstat")[loop]);
                evioEVNTp.setByte("ecstat", loop, (byte) bEVNT.getInt("ECstat")[loop]);
                evioEVNTp.setByte("lcstat", loop, (byte) bEVNT.getInt("LCstat")[loop]);
                evioEVNTp.setByte("scstat", loop, (byte) bEVNT.getInt("SCstat")[loop]);
                evioEVNTp.setByte("ccstat", loop, (byte) bEVNT.getInt("CCstat")[loop]);
                
                evioEVNTp.setInt("pid",loop, bEVNT.getInt("ID")[loop]);
                evioEVNTp.setFloat("mass", loop,bEVNT.getFloat("Mass")[loop]);
                evioEVNTp.setFloat("px", loop,bEVNT.getFloat("Pmom")[loop]*bEVNT.getFloat("Cx")[loop]);
                evioEVNTp.setFloat("py", loop,bEVNT.getFloat("Pmom")[loop]*bEVNT.getFloat("cy")[loop]);
                evioEVNTp.setFloat("pz", loop,bEVNT.getFloat("Pmom")[loop]*bEVNT.getFloat("cz")[loop]);
                evioEVNTp.setFloat("vx", loop,bEVNT.getFloat("X")[loop]);
                evioEVNTp.setFloat("vy", loop,bEVNT.getFloat("Y")[loop]);
                evioEVNTp.setFloat("vz", loop,bEVNT.getFloat("Z")[loop]);
            }
            this.evioDataBanks.put("EVNT", evioEVNTp);
        }
        
        if(this.bosDataBanks.containsKey("TAGR")==true){            
            BosDataBank tagr = (BosDataBank) this.bosDataBanks.get("TAGR");            
            EvioDataBank evioTGPBp = EvioFactory.createEvioBank("TAGGER::tgpb",tagr.rows());
            int nrows = tagr.rows();
            for(int loop = 0; loop < nrows; loop++){
                evioTGPBp.setByte("status", loop , (byte) tagr.getInt("STAT")[loop]);
                evioTGPBp.setFloat("energy", loop, tagr.getFloat("ERG")[loop]);
                //evioTGPBp.setFloat("time", loop, tagr.getFloat("TPHO")[loop]-startTime[0]);
                evioTGPBp.setFloat("time", loop, tagr.getFloat("TPHO")[loop]);
                evioTGPBp.setShort("tid", loop, (short) tagr.getInt("T_id")[loop]);
                evioTGPBp.setShort("eid", loop, (short) tagr.getInt("E_id")[loop]);
            }
            this.evioDataBanks.put("TAGR", evioTGPBp);
        }
        
         if(this.bosDataBanks.containsKey("ECPB")==true){
            BosDataBank bECPB = (BosDataBank) this.bosDataBanks.get("ECPB");
            int rows = bECPB.rows();
            EvioDataBank evioECPB = EvioFactory.createEvioBank("DETECTOR::ecpb", rows);
            for(int loop = 0; loop < rows; loop++){
                int scht = bECPB.getInt("ScHt")[loop];
                int sector = (int) scht/100;
                evioECPB.setByte("sector", loop, (byte) sector);
                evioECPB.setFloat("etot", loop, bECPB.getFloat("Etot")[loop]);
                evioECPB.setFloat("ein" , loop, bECPB.getFloat("Ein")[loop]);
                evioECPB.setFloat("eout", loop, bECPB.getFloat("Eout")[loop]);
                evioECPB.setFloat("time", loop, bECPB.getFloat("Time") [loop]);
                evioECPB.setFloat("path", loop, bECPB.getFloat("Path") [loop]);
                evioECPB.setFloat("x", loop, bECPB.getFloat("X") [loop]);
                evioECPB.setFloat("y", loop, bECPB.getFloat("Y") [loop]);
                evioECPB.setFloat("z", loop, bECPB.getFloat("Z") [loop]);
            }
            this.evioDataBanks.put("ECPB", evioECPB);
            //banklist.add(evioECPB);
        }
         if(this.bosDataBanks.containsKey("LCPB")==true){
            BosDataBank bLCPB = (BosDataBank) this.bosDataBanks.get("LCPB");
            int rows = bLCPB.rows();
            EvioDataBank evioLCPB = EvioFactory.createEvioBank("DETECTOR::lcpb", rows);
            for(int loop = 0; loop < rows; loop++){
                int scht = bLCPB.getInt("ScHt")[loop];
                int sector = (int) scht/100;
                evioLCPB.setByte("sector", loop, (byte) sector);
                evioLCPB.setFloat("etot", loop, bLCPB.getFloat("Etot")[loop]);
                evioLCPB.setFloat("ein" , loop, bLCPB.getFloat("Ein")[loop]);
                evioLCPB.setFloat("time", loop, bLCPB.getFloat("Time") [loop]);
                evioLCPB.setFloat("path", loop, bLCPB.getFloat("Path") [loop]);
                evioLCPB.setFloat("x", loop, bLCPB.getFloat("X") [loop]);
                evioLCPB.setFloat("y", loop, bLCPB.getFloat("Y") [loop]);
                evioLCPB.setFloat("z", loop, bLCPB.getFloat("Z") [loop]);
            }
            this.evioDataBanks.put("LCPB", evioLCPB);
            //banklist.add(evioECPB);
        }
         if(this.bosDataBanks.containsKey("SCPB")){
            BosDataBank bSCPB = (BosDataBank) this.bosDataBanks.get("SCPB");
            int rows = bSCPB.rows();
            EvioDataBank evioSCPB = EvioFactory.createEvioBank("DETECTOR::scpb", rows);
            for(int loop = 0; loop < rows; loop++){
                int sector = bSCPB.getInt("ScPdHt")[loop]/10000;
                int paddle = bSCPB.getInt("ScPdHt")[loop]/100 - sector*100;
                //System.err.println(" " + bSCPB.getInt("ScPdHt")[loop] +
                //        "  sector = " + sector + " " + paddle);
                //int paddle = bSCPB.getInt("ScPdHt")[loop]/10000;
                evioSCPB.setByte("sector", loop, (byte) sector);
                evioSCPB.setByte("paddle", loop, (byte) paddle);
                evioSCPB.setFloat("edep", loop, bSCPB.getFloat("Edep") [loop]);
                evioSCPB.setFloat("time", loop, bSCPB.getFloat("Time") [loop]);
                evioSCPB.setFloat("path", loop, bSCPB.getFloat("Path") [loop]);
            }
            this.evioDataBanks.put("SCPB", evioSCPB);
         }
         
         if(this.bosDataBanks.containsKey("CCPB")){
            BosDataBank bCCPB = (BosDataBank) this.bosDataBanks.get("CCPB");
            int rows = bCCPB.rows();
            EvioDataBank evioCCPB = EvioFactory.createEvioBank("DETECTOR::ccpb", rows);
            for(int loop = 0; loop < rows; loop++){
                int sector = bCCPB.getInt("ScSgHt")[loop]/100;
                //int paddle = bSCPB.getInt("ScPdHt")[loop]/10000;
                evioCCPB.setByte("sector", loop, (byte) sector);
                evioCCPB.setFloat("nphe", loop, bCCPB.getFloat("Nphe") [loop]);
                evioCCPB.setFloat("time", loop, bCCPB.getFloat("Time") [loop]);
                evioCCPB.setFloat("path", loop, bCCPB.getFloat("Path") [loop]);
            }
            this.evioDataBanks.put("CCPB", evioCCPB);
        }
    }
    
    public TreeMap<String,EvioDataBank>  getEvioBankStore(){
        return this.evioDataBanks;
    }
    
    public void initBosBanks(BosDataEvent event){
        this.bosDataBanks.clear();
        if(event.hasBank("TAGR:1")){
            //BosDataBank hevt = (BosDataBank) bos_event.getBank("HEVT");
            BosDataBank tagr = (BosDataBank) event.getBank("TAGR:1");
            this.bosDataBanks.put(tagr.getDescriptor().getName(), tagr);
        }
        
        if(event.hasBank("TGBI")==true){
            BosDataBank bTGBI = (BosDataBank) event.getBank("TGBI");
            this.bosDataBanks.put(bTGBI.getDescriptor().getName(), bTGBI);
        }
        
        if(event.hasBank("HEVT")){
            BosDataBank bHEAD = (BosDataBank) event.getBank("HEVT");
            this.bosDataBanks.put(bHEAD.getDescriptor().getName(), bHEAD);
        }
        
        if(event.hasBank("EVNT")){
            BosDataBank bPART = (BosDataBank) event.getBank("EVNT");
            this.bosDataBanks.put(bPART.getDescriptor().getName(), bPART);
        }
                
        if(event.hasBank("ECPB")){
            BosDataBank bECPB = (BosDataBank) event.getBank("ECPB");
            this.bosDataBanks.put(bECPB.getDescriptor().getName(), bECPB);
        }
        
        if(event.hasBank("LCPB")){
            BosDataBank bLCPB = (BosDataBank) event.getBank("LCPB");
            this.bosDataBanks.put(bLCPB.getDescriptor().getName(), bLCPB);
        }
        
        if(event.hasBank("SCPB")){
            BosDataBank bSCPB = (BosDataBank) event.getBank("SCPB");
            this.bosDataBanks.put(bSCPB.getDescriptor().getName(), bSCPB);
        }
        if(event.hasBank("CCPB")){
            BosDataBank bCCPB = (BosDataBank) event.getBank("CCPB");
            this.bosDataBanks.put(bCCPB.getDescriptor().getName(), bCCPB);
        }
    }
    
    public static void printUsage(){
        System.out.println("\n \t Usage : bos2hipo [options] [output file] [input file]");
        System.out.println("\n\n Options: ");
        System.out.println("\t    -u : uncompressed");
        System.out.println("\t -gzip : gzip compression");
        System.out.println("\t  -lz4 : LZ4 compression\n\n");
    }
    
    public static void main(String[] args){
        int compression = -1;
        
        if(args.length<3){
            Bos2EvioEventBank.printUsage();
            System.exit(0);            
        }
        
        if(args[0].startsWith("-")==false){
            Bos2EvioEventBank.printUsage();
            System.exit(0); 
        } else {
            if(args[0].compareTo("-u")==0){
                compression = 0;
            }
            if(args[0].compareTo("-gzip")==0){
                compression =1;
            }
            if(args[0].compareTo("-lz4")==0){
                compression =2;
            }
        }
        
        
        if(compression<0){
            Bos2EvioEventBank.printUsage();
            System.exit(0); 
        }
        String  hipoFileName = args[1];
        String  bosFileName  = args[2];
        
        
        Bos2EvioEventBank bos2evio = new Bos2EvioEventBank();
        
        HipoDataSync  writer = new HipoDataSync();
        writer.open(hipoFileName);
        writer.setCompressionType(compression);
        
        BosDataSource reader = new BosDataSource();                      
        reader.open(bosFileName);
        int progressCounter = 0;
        
        while(reader.hasEvent()){
            progressCounter++;
            
            
            if(progressCounter%5000==0){
                System.out.println(String.format("-------->  progress : n events processed %12d", progressCounter));
            }
            
            BosDataEvent event = (BosDataEvent) reader.getNextEvent();
            try{
                bos2evio.initBosBanks(event);
                bos2evio.initEvioBank();
                
                TreeMap<String,EvioDataBank>  evioBanks = bos2evio.getEvioBankStore();
                
                EvioDataEvent evioEvent = EvioFactory.createEvioEvent();
                
                if(evioBanks.containsKey("HEVT")==true){
                    evioEvent.appendBanks(evioBanks.get("HEVT"));
                }
                
                if(evioBanks.containsKey("EVNT")==true){
                    evioEvent.appendBanks(evioBanks.get("EVNT"));
                }
                
                List<EvioDataBank>  detectorBanks = new ArrayList<EvioDataBank>();
                
                if(evioBanks.containsKey("ECPB")==true){
                    detectorBanks.add(evioBanks.get("ECPB"));
                }
                if(evioBanks.containsKey("SCPB")==true){
                    detectorBanks.add(evioBanks.get("SCPB"));
                }
                if(evioBanks.containsKey("CCPB")==true){
                    detectorBanks.add(evioBanks.get("CCPB"));
                }
                
                //System.out.println(" DETECTOR BANKS SIZE = " + detectorBanks.size());
                
                if(detectorBanks.size()>0){
                    EvioDataBank[] banks  = new EvioDataBank[detectorBanks.size()];
                    for(int i = 0; i < detectorBanks.size();i++){
                        banks[i] = detectorBanks.get(i);
                    }
                    evioEvent.appendBanks(banks);
                }
                
                if(evioBanks.containsKey("EVNT")==true&&evioBanks.containsKey("HEVT")==true){
                    writer.writeEvent(evioEvent);
                }
            } catch (Exception e) {
                System.out.println("---> error at event # " + progressCounter);
            }
        }
        
        writer.close();
    }
}
