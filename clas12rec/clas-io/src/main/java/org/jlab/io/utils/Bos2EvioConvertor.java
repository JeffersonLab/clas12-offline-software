/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.io.utils;

import java.io.File;
import java.util.ArrayList;
import org.jlab.io.bos.BosDataBank;
import org.jlab.io.bos.BosDataEvent;
import org.jlab.io.bos.BosDataSource;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataDictionary;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioDataSync;
import org.jlab.io.evio.EvioFactory;

/**
 *
 * @author gavalian
 */
public class Bos2EvioConvertor {
    private String inputFile  = "";
    private String outputFile = "";
    private EvioFactory  eviofactory = new EvioFactory();
    private EvioDataDictionary dictionary = null;
    private ArrayList<EvioDataBank> bankStore = new ArrayList<EvioDataBank>();
    
    public Bos2EvioConvertor(){
        dictionary = new EvioDataDictionary("CLAS12DIR","lib/bankdefs/clas12");
    }
    
    public Bos2EvioConvertor(String _in, String _out){
        dictionary = new EvioDataDictionary("CLAS12DIR","lib/bankdefs/clas12");
        this.setFiles(_in, _out);
    }
    
    public final void setFiles(String _in, String _out){
        inputFile  = _in;
        outputFile = _out;
    }
    
    public ArrayList<EvioDataBank> store(){
        return bankStore;
    }
    public void clear(){
        bankStore.clear();
    }
    
    public void writeTAGGERBank(BosDataEvent bos_event, EvioDataEvent evio_event){
        
        /*System.err.println("[bos::tagr]---> writing tagger bank.... ");
        System.err.println("[bos::tagr]---> bank HEVT = " + bos_event.hasBank("HEVT"));
        System.err.println("[bos::tagr]---> bank TAGR = " + bos_event.getBank("TAGR:1").rows());
        */
        if(bos_event.hasBank("TGPB")){
            //BosDataBank hevt = (BosDataBank) bos_event.getBank("HEVT");
            BosDataBank tagr = (BosDataBank) bos_event.getBank("TAGR:1");
            //System.err.println("[bos::tagr]---> found banks.... ");
            //float[] startTime  = hevt.getFloat("STT");
            //if(startTime!=null){
                //System.err.println("[bos::tagr]---> start time = " + startTime[0]);
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
                bankStore.add(evioTGPBp);
                evio_event.appendBank(evioTGPBp);
            //}

        }
    }
    
    public void writeHEADDERBank(BosDataEvent bos_event, EvioDataEvent evio_event){
        if(bos_event.hasBank("HEVT")){
            EvioDataBank evioHEVTp = EvioFactory.createBank("HEADER::info", 1);
            BosDataBank bHEAD = (BosDataBank) bos_event.getBank("HEVT");
            evioHEVTp.setInt("nrun", 0, bHEAD.getInt("NRUN")[0]);
            evioHEVTp.setInt("nevt", 0, bHEAD.getInt("NEVENT")[0]);
            evioHEVTp.setInt("trigger", 0, bHEAD.getInt("TRGPRS")[0]);
            evioHEVTp.setFloat("fc", 0, bHEAD.getFloat("FC")[0]);
            evioHEVTp.setFloat("fcg", 0, bHEAD.getFloat("FCG")[0]);
            evioHEVTp.setFloat("stt", 0, bHEAD.getFloat("STT")[0]);
            evio_event.appendBanks(evioHEVTp);
        }
    }
    
    
    public void writeECPBBank(BosDataEvent bos_event, EvioDataEvent evio_event){
        ArrayList<EvioDataBank>  banklist = new ArrayList<EvioDataBank>();
        if(bos_event.hasBank("ECPB")){
            BosDataBank bECPB = (BosDataBank) bos_event.getBank("ECPB");
            int rows = bECPB.rows();
            EvioDataBank evioECPB = EvioFactory.createEvioBank("DETECTOR::ecpb", rows);
            for(int loop = 0; loop < rows; loop++){
                evioECPB.setByte("sector", loop, (byte) 1);
                evioECPB.setFloat("etot", loop, bECPB.getFloat("Etot")[loop]);
                evioECPB.setFloat("ein" , loop, bECPB.getFloat("Ein")[loop]);
                evioECPB.setFloat("eout", loop, bECPB.getFloat("Eout")[loop]);
                evioECPB.setFloat("time", loop, bECPB.getFloat("Time") [loop]);
                evioECPB.setFloat("path", loop, bECPB.getFloat("Path") [loop]);
            }
            banklist.add(evioECPB);
        }
        
        if(bos_event.hasBank("SCPB")){
            BosDataBank bSCPB = (BosDataBank) bos_event.getBank("SCPB");
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
                evioSCPB.setFloat("time", loop, bSCPB.getFloat("Time") [loop]);
                evioSCPB.setFloat("path", loop, bSCPB.getFloat("Path") [loop]);
            }
            banklist.add(evioSCPB);
        }
        
        if(bos_event.hasBank("CCPB")){
            BosDataBank bCCPB = (BosDataBank) bos_event.getBank("CCPB");
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
            banklist.add(evioCCPB);
        }
        
        if(banklist.size()>0){
            EvioDataBank[] eviobanks = new EvioDataBank[banklist.size()];
            for(int loop =0; loop < banklist.size();loop++) eviobanks[loop] = banklist.get(loop);
            evio_event.appendBanks(eviobanks);
        }
    }
    
    
    //public void writeEVENTBank(BosDataEvent bos_event,EvioDataEvent evio_event){
    public void writeEVENTBank(BosDataEvent bos_event, EvioDataEvent evio_event){
        //bankStore.clear();
        if(bos_event.hasBank("EVNT") && bos_event.hasBank("HEVT")){
            
        } else {
            return;
        }
        
        BosDataBank bEVNT = (BosDataBank) bos_event.getBank("EVNT");
        BosDataBank bHEAD = (BosDataBank) bos_event.getBank("HEVT");
        
        int rows = bEVNT.rows();
        
        //System.out.println("step 1 rows = " + rows);
        EvioDataBank evioEVNTp = EvioFactory.createEvioBank("EVENT::particle", rows);
        //dictionary.getDescriptor("EVENT::particle"),rows);
        //System.out.println("step 2");
        /*
        EvioDataBank evioEVNTd = EvioFactory.createEvioBank("CLAS6EVENT::detector", 
              dictionary.getDescriptor("CLAS6EVENT::detector"),rows);*/
        //System.out.println("step 3 rows = " + rows);
        for(int loop = 0; loop < rows; loop++){
            evioEVNTp.setByte("status", loop, (byte) bEVNT.getInt("Status")[loop]);
            evioEVNTp.setByte("charge", loop, (byte) bEVNT.getInt("Charge")[loop]);
            
            evioEVNTp.setByte("dcstat", loop, (byte) bEVNT.getInt("DCstat")[loop]);
            evioEVNTp.setByte("ecstat", loop, (byte) bEVNT.getInt("ECstat")[loop]);
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
            //System.err.println("start scpb");
            /*
            if(bos_event.hasBank("CCPB")==true){
                BosDataBank bCCPB = (BosDataBank) bos_event.getBank("CCPB");
                int cc_row  = bEVNT.getInt("CCstat")[loop] - 1;
                if(cc_row>=0&&cc_row<bCCPB.rows()){
                    evioEVNTd.setFloat("ccnphe",loop, bCCPB.getFloat("Nphe")[cc_row]);
                }
            }
            
            if(bos_event.hasBank("SCPB")==true){
                //System.err.println("start scpb");
                BosDataBank bSCPB = (BosDataBank) bos_event.getBank("SCPB");
                int sc_row  = bEVNT.getInt("SCstat")[loop] - 1;
                if(sc_row>=0&&sc_row<bSCPB.rows()){
                    int scpdht   = bSCPB.getInt("ScPdHt")[sc_row];
                    int scsector = (int) (scpdht/10000);
                    int scpaddle = (int) ((scpdht - scsector*10000)/100);
                    evioEVNTd.setInt("scsector", loop,scsector);
                    evioEVNTd.setInt("scpaddle", loop,scpaddle);
                    evioEVNTd.setFloat("sctime",loop, bSCPB.getFloat("Time")[sc_row]);
                    evioEVNTd.setFloat("scpath",loop, bSCPB.getFloat("Path")[sc_row]);                    
                }
            }
            //System.err.println("start ecpb");
            if(bos_event.hasBank("ECPB")==true){
                BosDataBank bECPB = (BosDataBank) bos_event.getBank("ECPB");
                int ec_row  = bEVNT.getInt("ECstat")[loop] - 1;
                if(ec_row>=0&&ec_row<bECPB.rows()){
                    int scht   = bECPB.getInt("ScHt")[ec_row];
                    int ecsector = (int) (scht/100);
                    evioEVNTd.setInt("ecsector", loop,ecsector);
                    evioEVNTd.setFloat("ectime",loop, bECPB.getFloat("Time")[ec_row]);
                    evioEVNTd.setFloat("ecpath",loop, bECPB.getFloat("Path")[ec_row]);                    
                    evioEVNTd.setFloat("ecin",loop, bECPB.getFloat("Ein")[ec_row]);
                    evioEVNTd.setFloat("ecout",loop, bECPB.getFloat("Eout")[ec_row]);
                    evioEVNTd.setFloat("ectot",loop, bECPB.getFloat("Etot")[ec_row]);
                    evioEVNTd.setFloat("ecu",loop, bECPB.getFloat("X")[ec_row]);
                    evioEVNTd.setFloat("ecv",loop, bECPB.getFloat("Y")[ec_row]);
                    evioEVNTd.setFloat("ecw",loop, bECPB.getFloat("Z")[ec_row]);                    
                }
            }
         */   
        }
        
        bankStore.add(evioEVNTp);
        //bankStore.add(evioEVNTd);
        evio_event.appendBanks(evioEVNTp);
        //evioEVNTp.show();
        //evioEVNTd.show();
    }
    
    public void convertFiles(String outputfile, String[] inputfiles){
        EvioDataSync  writer = new EvioDataSync();
        writer.open(outputfile);
        for(int loop = 0; loop < inputfiles.length; loop++){
            BosDataSource reader = new BosDataSource();                      
            reader.open(inputfiles[loop]);
            while(reader.hasEvent()){
                BosDataEvent event = (BosDataEvent) reader.getNextEvent();
                if(event.hasBank("HEVT")==true&&
                    event.hasBank("EVNT")==true){                    
                
                    this.store().clear();
                    EvioDataEvent outevent = writer.createEvent(EvioFactory.getDictionary());
                    this.writeHEADDERBank(event, outevent);
                    this.writeEVENTBank(event, outevent);
                    this.writeECPBBank(event, outevent);
                    //convertor.writeEVENTBank(event,outevent);
                    this.writeTAGGERBank(event,outevent);
                    writer.writeEvent(outevent);
                }
            }
        }
        writer.close();
    }
    
    
    public static void main(String[] args){

        
        System.err.println("[BOS convertor]----> starting process.....");
        
        if(args.length<2){
            System.err.println("[bos2evio] ----> error. no input files specified.");
            System.err.println("[bos2evio] ----> use : bos2evio [output file] [inputfile1] [inputfile2] ...." );
            System.err.println("[bos2evio] ----> exiting.....\n\n " );
            return;
        }
        
        String outputfile  = args[0];
        int    ninputfiles = args.length -1;
        File ofile = new File(outputfile);
        if(ofile.exists()==true){
            System.err.println("\n\n\n");
            System.err.println("[bos2evio] ----> error. specified output file exists.");
            System.err.println("[bos2evio] ----> remove the file : " + outputfile);
            System.err.println("[bos2evio] ----> and run the program again. " );
            System.err.println("[bos2evio] ----> exiting.....\n\n " );
            return;
        }

        System.err.println("[BOS convertor]----> Initializing convertor with output : " + outputfile);
        String[] inputfiles = new String[ninputfiles];
        
        for(int loop = 0; loop < args.length-1; loop++){
            inputfiles[loop] = args[loop+1];
            System.err.println("[BOS convertor]----> Input : " + inputfiles[loop]);
        }
        
        Bos2EvioConvertor convertor = new Bos2EvioConvertor();
        convertor.convertFiles(outputfile, inputfiles);        
    }
    
    public static void main2(String[] args){
        
        if(args.length<2){
           System.err.println("[bos2evio] ----> error in arguments. please specify input/output files.");
           System.err.println("[bos2evio] ----> exiting.....\n\n " );
           return; 
        }
        
        String outputfile = args[0];
        String inputfile  = args[1];
        
        
        
        File ofile = new File(outputfile);
        
        if(ofile.exists()==true){
            System.err.println("\n\n\n");
            System.err.println("[bos2evio] ----> error. specified output file exists.");
            System.err.println("[bos2evio] ----> remove the file : " + outputfile);
            System.err.println("[bos2evio] ----> and run the program again. " );
            System.err.println("[bos2evio] ----> exiting.....\n\n " );
            return;
        }
        
        int counter = 0;
        int counterConvert = 0;
        System.out.println("[bosConvertor]---> opening file : " + outputfile);
        long starttime = System.currentTimeMillis();
        BosDataSource reader = new BosDataSource();                      
        reader.open(inputfile);
        
        EvioFactory.getDictionary().getDescriptor("HEADER::info").show();
        EvioFactory.getDictionary().getDescriptor("EVENT::particle").show();
        
        EvioDataSync  writer = new EvioDataSync();
        writer.open(outputfile);
        
        Bos2EvioConvertor convertor = new Bos2EvioConvertor();
        
        while(reader.hasEvent()){
            BosDataEvent event = (BosDataEvent) reader.getNextEvent();
            counter++;
            
            if(event.hasBank("HEVT")==true&&
                    event.hasBank("EVNT")==true){
                counterConvert++;
                
                convertor.store().clear();
                EvioDataEvent outevent = writer.createEvent(EvioFactory.getDictionary());
                convertor.writeHEADDERBank(event, outevent);
                convertor.writeEVENTBank(event, outevent);
                convertor.writeECPBBank(event, outevent);
                //convertor.writeEVENTBank(event,outevent);
                convertor.writeTAGGERBank(event,outevent);
                writer.writeEvent(outevent);                
                /*
                //outevent.show();
                //EvioDataEvent  event = EvioFactory.getDictionary().
                //for(EvioDataBank bank: convertor.store()){
                //bank.show();
                //outevent.appendBank(bank);
                //}
                */
            }
        }
        writer.close();
        long endtime = System.currentTimeMillis();
        double processTime = (endtime-starttime)/1000.;
        double processTimePerEvent = processTime/counter;
        System.err.println(String.format("\n[BOSCONVERTOR] --->  %d / %d events processed in %.2f sec. ", 
                counter, counterConvert,processTime));
    }
}
