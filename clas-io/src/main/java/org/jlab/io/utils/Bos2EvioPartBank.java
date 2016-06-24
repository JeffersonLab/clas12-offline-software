/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.utils;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import org.jlab.io.bos.BosDataBank;
import org.jlab.io.bos.BosDataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataDictionary;
import org.jlab.io.evio.EvioFactory;

/**
 *
 * @author gavalian
 */

public class Bos2EvioPartBank {
    
    private TreeMap<String,EvioDataBank>  evioDataBanks = new TreeMap<String,EvioDataBank>();
    private TreeMap<String,BosDataBank>    bosDataBanks = new TreeMap<String,BosDataBank>();
    private EvioDataDictionary            dictionary    = null;
    
    public Bos2EvioPartBank(){        
        dictionary = EvioFactory.getDictionary();
    }
    
    public void clear(){
        this.evioDataBanks.clear();
    }
    
    public void initBosBanks(BosDataEvent event){
        bosDataBanks.clear();
        if(event.hasBank("TGPB")){
            //BosDataBank hevt = (BosDataBank) bos_event.getBank("HEVT");
            BosDataBank tagr = (BosDataBank) event.getBank("TAGR:1");
            this.bosDataBanks.put(tagr.getDescriptor().getName(), tagr);
        }
        if(event.hasBank("HEVT")){
            BosDataBank bHEAD = (BosDataBank) event.getBank("HEVT");
            this.bosDataBanks.put(bHEAD.getDescriptor().getName(), bHEAD);
        }
        
        if(event.hasBank("PART:1")){
            BosDataBank bPART = (BosDataBank) event.getBank("PART:1");
            this.bosDataBanks.put(bPART.getDescriptor().getName(), bPART);
        }
                
        if(event.hasBank("TBID:1")){
            BosDataBank bTBID = (BosDataBank) event.getBank("TBID:1");
            this.bosDataBanks.put(bTBID.getDescriptor().getName(), bTBID);
        }
        
        if(event.hasBank("TBER:1")){
            BosDataBank bTBER = (BosDataBank) event.getBank("TBER");
            this.bosDataBanks.put(bTBER.getDescriptor().getName(), bTBER);
        }
    }
    
    public void processBosEvent(BosDataEvent event){
        evioDataBanks.clear();
        this.bosDataBanks.clear();
        this.initBosBanks(event);
        this.show();
        //this.addParticleBank(event);
        //this.addHeaderBank(event);
        //this.addTaggerBank(event);        
    }
    
    public void show(){
        System.out.print("[BOS BANK] ");
        for(Map.Entry<String,BosDataBank> bank : this.bosDataBanks.entrySet()){
            System.out.print(" : " + bank.getKey());
        }
        System.out.println();
    }
    public TreeMap<String,EvioDataBank> bankStore(){
        return this.evioDataBanks;
    }
    
    public void addTaggerBank(BosDataEvent bos_event){
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
                
                if(evioTGPBp.rows()>0){
                    this.evioDataBanks.put(evioTGPBp.getDescriptor().getName(), evioTGPBp);
                }
            //}

        }
    }
    
    
    public void addHeaderBank(BosDataEvent bos_event){
        if(bos_event.hasBank("HEAD")==true){
            if(bos_event.hasBank("HEVT")){
                EvioDataBank evioHEVTp = EvioFactory.createBank("HEADER::info", 1);
                BosDataBank bHEAD = (BosDataBank) bos_event.getBank("HEVT");
                evioHEVTp.setInt("nrun", 0, bHEAD.getInt("NRUN")[0]);
                evioHEVTp.setInt("nevt", 0, bHEAD.getInt("NEVENT")[0]);
                evioHEVTp.setInt("trigger", 0, bHEAD.getInt("TRGPRS")[0]);
                evioHEVTp.setFloat("fc", 0, bHEAD.getFloat("FC")[0]);
                evioHEVTp.setFloat("fcg", 0, bHEAD.getFloat("FCG")[0]);
                evioHEVTp.setFloat("stt", 0, bHEAD.getFloat("STT")[0]);
                this.evioDataBanks.put(evioHEVTp.getDescriptor().getName(), evioHEVTp);
            }
        }
    }
    
    
    
    public void addDetectorBank(BosDataEvent event){
        ArrayList<DetectorBank>  dbstore = new ArrayList<DetectorBank>();
        if(event.hasBank("PART:1")==true&&event.hasBank("TBID:1")){
            BosDataBank  partBank = (BosDataBank) event.getBank("PART:1");
            BosDataBank  tbidBank = (BosDataBank) event.getBank("TBID:1");
            int prows = partBank.rows();
            for(int loop = 0; loop < prows; loop++){
                int tbid    = partBank.getInt("trkid")[loop];
                if(tbid>0&&tbid<=tbidBank.rows()){
                    int ecindex = tbidBank.getInt("ec_id")[tbid-1];

                    BosDataBank  echbBank = (BosDataBank) event.getBank("ECHB");                
                    if(ecindex>0&&ecindex<=echbBank.rows()){
                        DetectorBank dbank = new DetectorBank();
                        dbank.X = echbBank.getFloat("x_hit")[ecindex];
                        dbank.Y = echbBank.getFloat("y_hit")[ecindex];
                        dbank.Z = echbBank.getFloat("z_hit")[ecindex];
                        dbank.time = echbBank.getFloat("t_hit")[ecindex];
                        dbank.energy = echbBank.getFloat("E_hit")[ecindex];
                    } else {
                        System.err.println("ERROR IN ECHB BANK tbid = " + ecindex 
                            + " rows = " + echbBank.rows());
                    }
                } else {
                    System.err.println("ERROR IN TBID BANK tbid = " + tbid 
                            + " rows = " + tbidBank.rows());
                }
            }
        }
    }
    
    public void addTrackBank(BosDataEvent event){
        
    }
    
    public void addParticleBank(BosDataEvent event){        
        if(event.hasBank("PART:1")==true&&event.hasBank("TBID:1")){
            BosDataBank  partBank = (BosDataBank) event.getBank("PART:1");
            BosDataBank  tbidBank = (BosDataBank) event.getBank("TBID:1");
            int nrows = partBank.rows();
            EvioDataBank  evioPART = (EvioDataBank) dictionary.createBank("PART::particle",nrows);
            for(int loop = 0; loop < nrows; loop++){
                //int pid = partBank.getInt("pid")[loop];                
                float q = partBank.getFloat("q")[loop];
                float energy = partBank.getFloat("E")[loop];
                float px     = partBank.getFloat("px")[loop];
                float py     = partBank.getFloat("py")[loop];
                float pz     = partBank.getFloat("pz")[loop];
                float mass2  = energy*energy - (px*px+py*py+pz*pz);
                byte  charge = (byte) 0;
                if(q>0) charge = (byte) 1;
                if(q<0) charge = (byte) -1;
                
                evioPART.setShort("pid", loop, (short) partBank.getInt("pid")[loop]);
                evioPART.setFloat("px", loop,partBank.getFloat("px")[loop]);
                evioPART.setFloat("py", loop,partBank.getFloat("py")[loop]);
                evioPART.setFloat("pz", loop,partBank.getFloat("pz")[loop]);
                evioPART.setFloat("vx", loop,partBank.getFloat("x")[loop]);
                evioPART.setFloat("vy", loop,partBank.getFloat("y")[loop]);
                evioPART.setFloat("vz", loop,partBank.getFloat("z")[loop]);
                evioPART.setFloat("chi2pid", loop,partBank.getFloat("qpid")[loop]);
                evioPART.setFloat("mass", loop,mass2);
                evioPART.setByte("charge", loop, charge);
                evioPART.setByte("status", loop, (byte) 1);
                
            }
            this.evioDataBanks.put(evioPART.getDescriptor().getName(), evioPART);
            //partBank.show();
            //evioPART.show();
            //tbidBank.show();
        }
    }
    
}
