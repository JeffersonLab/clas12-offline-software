/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.bos.BosDataBank;
import org.jlab.io.bos.BosDataEvent;
import org.jlab.io.bos.BosDataSource;
import org.jlab.io.hipo.HipoDataBank;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.io.hipo.HipoDataSync;

/**
 *
 * @author gavalian, kenjo
 */
public class Bos2HipoEventBank {
    private TreeMap<String,DataBank>  hipoDataBanks = new TreeMap<String,DataBank>();
    private TreeMap<String,BosDataBank>    bosDataBanks = new TreeMap<String,BosDataBank>();

    public Bos2HipoEventBank(){

    }

    public void clear(){
        this.hipoDataBanks.clear();
    }

    public void initHipoBank(DataEvent hipoEvent){
        this.hipoDataBanks.clear();
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
            DataBank  bankHEVT = hipoEvent.createBank("HEADER::info", 1);
            bankHEVT.setInt("estatus", 0, this.bosDataBanks.get("HEVT").getInt("ESTATUS")[0]);
            bankHEVT.setInt("nrun", 0, this.bosDataBanks.get("HEVT").getInt("NRUN")[0]);
            bankHEVT.setInt("nevt", 0, this.bosDataBanks.get("HEVT").getInt("NEVENT")[0]);
            bankHEVT.setInt("type", 0, this.bosDataBanks.get("HEVT").getInt("TYPE")[0]);
            bankHEVT.setInt("npgp", 0, this.bosDataBanks.get("HEVT").getInt("NPGP")[0]);
            bankHEVT.setInt("trgprs", 0, this.bosDataBanks.get("HEVT").getInt("TRGPRS")[0]);
            bankHEVT.setFloat("tg", 0, this.bosDataBanks.get("HEVT").getFloat("TG")[0]);
            bankHEVT.setFloat("stt" , 0, this.bosDataBanks.get("HEVT").getFloat("STT")[0]);
            bankHEVT.setFloat("fc"  , 0, this.bosDataBanks.get("HEVT").getFloat("FC")[0]);
            bankHEVT.setFloat("fcg" , 0, this.bosDataBanks.get("HEVT").getFloat("FCG")[0]);
            bankHEVT.setFloat("rf1", 0, this.bosDataBanks.get("HEVT").getFloat("RF1")[0]);
            bankHEVT.setFloat("rf2", 0, this.bosDataBanks.get("HEVT").getFloat("RF2")[0]);
            bankHEVT.setFloat("con1", 0, this.bosDataBanks.get("HEVT").getFloat("CON1")[0]);
            bankHEVT.setFloat("con2", 0, this.bosDataBanks.get("HEVT").getFloat("CON2")[0]);
            bankHEVT.setFloat("con3", 0, this.bosDataBanks.get("HEVT").getFloat("CON3")[0]);
            int evtclass = this.bosDataBanks.get("HEVT").getInt("TRGPRS")[0];
            byte ihelicity = 0;
            if(evtclass<0) ihelicity = 1;
            bankHEVT.setByte("helicity" , 0, ihelicity);

            // FBPM bank for raster
            if(this.bosDataBanks.containsKey("FBPM")){
                BosDataBank bFBPM = (BosDataBank) this.bosDataBanks.get("FBPM");
                int nrows = bFBPM.rows();

                bankHEVT.setShort("rastr1" , 0, this.bosDataBanks.get("FBPM").getShort("ADC")[0]);
                bankHEVT.setShort("rastr2" , 0, this.bosDataBanks.get("FBPM").getShort("ADC")[1]);
            }
            this.hipoDataBanks.put("HEVT", bankHEVT);
        }

        /*
        * Writing EVNT bank
        */
        if(this.bosDataBanks.containsKey("EVNT")==true){
            BosDataBank bEVNT = (BosDataBank) this.bosDataBanks.get("EVNT");
            int nrows = bEVNT.rows();

            DataBank  hipoEVNTp = hipoEvent.createBank("EVENT::particle", nrows);
            for(int loop = 0; loop < nrows; loop++){
                hipoEVNTp.setByte("status", loop, (byte) bEVNT.getInt("Status")[loop]);
                hipoEVNTp.setByte("charge", loop, (byte) bEVNT.getInt("Charge")[loop]);

                hipoEVNTp.setByte("dcstat", loop, (byte) bEVNT.getInt("DCstat")[loop]);
                hipoEVNTp.setByte("ecstat", loop, (byte) bEVNT.getInt("ECstat")[loop]);
                hipoEVNTp.setByte("lcstat", loop, (byte) bEVNT.getInt("LCstat")[loop]);
                hipoEVNTp.setByte("scstat", loop, (byte) bEVNT.getInt("SCstat")[loop]);
                hipoEVNTp.setByte("ccstat", loop, (byte) bEVNT.getInt("CCstat")[loop]);
                hipoEVNTp.setByte("ststat", loop, (byte) bEVNT.getInt("STstat")[loop]);

                hipoEVNTp.setInt("pid",loop, bEVNT.getInt("ID")[loop]);
                hipoEVNTp.setFloat("beta", loop, bEVNT.getFloat("Betta")[loop]);
                hipoEVNTp.setFloat("mass", loop,bEVNT.getFloat("Mass")[loop]);
                hipoEVNTp.setFloat("px", loop,bEVNT.getFloat("Pmom")[loop]*bEVNT.getFloat("Cx")[loop]);
                hipoEVNTp.setFloat("py", loop,bEVNT.getFloat("Pmom")[loop]*bEVNT.getFloat("cy")[loop]);
                hipoEVNTp.setFloat("pz", loop,bEVNT.getFloat("Pmom")[loop]*bEVNT.getFloat("cz")[loop]);
                hipoEVNTp.setFloat("vx", loop,bEVNT.getFloat("X")[loop]);
                hipoEVNTp.setFloat("vy", loop,bEVNT.getFloat("Y")[loop]);
                hipoEVNTp.setFloat("vz", loop,bEVNT.getFloat("Z")[loop]);
            }
            this.hipoDataBanks.put("EVNT", hipoEVNTp);
        }
        if(this.bosDataBanks.containsKey("TGPB")==true){
        BosDataBank tgpb = (BosDataBank) this.bosDataBanks.get("TGPB");
        DataBank hipoTGPBp = hipoEvent.createBank("TAGGER::tgpb", tgpb.rows());
        int nrows = tgpb.rows();
        for(int loop = 0; loop < nrows; loop++){
            hipoTGPBp.setByte("pointer", loop, (byte) tgpb.getInt("pointer")[loop]);
            hipoTGPBp.setFloat("time", loop, tgpb.getFloat("Time")[loop]);
            hipoTGPBp.setFloat("energy", loop, tgpb.getFloat("Energy")[loop]);
            hipoTGPBp.setFloat("dt", loop, tgpb.getFloat("dt")[loop]);
            }
        this.hipoDataBanks.put("TGPB", hipoTGPBp);
        }
        if(this.bosDataBanks.containsKey("TGBI")==true){
            BosDataBank tgbi = (BosDataBank) this.bosDataBanks.get("TGBI");
            DataBank hipoTGBIp = hipoEvent.createBank("HEADER::tgbi",tgbi.rows());
            hipoTGBIp.setInt("latch1", 0, tgbi.getInt("latch1")[0]);
            hipoTGBIp.setInt("helicity_scaler", 0, tgbi.getInt("helicity_scaler")[0]);
            hipoTGBIp.setInt("interrupt_time", 0, tgbi.getInt("interrupt_time")[0]);
            hipoTGBIp.setInt("latch2", 0, tgbi.getInt("latch2")[0]);
            hipoTGBIp.setInt("level3", 0, tgbi.getInt("level3")[0]);

        this.hipoDataBanks.put("TGBI", hipoTGBIp);
        }
        if(this.bosDataBanks.containsKey("TAGR")==true){
            BosDataBank tagr = (BosDataBank) this.bosDataBanks.get("TAGR");
            DataBank hipoTAGRp = hipoEvent.createBank("TAGGER::tagr",tagr.rows());
            int nrows = tagr.rows();
            for(int loop = 0; loop < nrows; loop++){
                hipoTAGRp.setByte("status", loop , (byte) tagr.getInt("STAT")[loop]);
                hipoTAGRp.setFloat("energy", loop, tagr.getFloat("ERG")[loop]);
                //hipoTGPBp.setFloat("time", loop, tagr.getFloat("TPHO")[loop]-startTime[0]);
                hipoTAGRp.setFloat("time", loop, tagr.getFloat("TPHO")[loop]);
                hipoTAGRp.setShort("tid", loop, (short) tagr.getInt("T_id")[loop]);
                hipoTAGRp.setShort("eid", loop, (short) tagr.getInt("E_id")[loop]);
                hipoTAGRp.setFloat("ttag", loop, tagr.getFloat("TTAG")[loop]);
            }
            this.hipoDataBanks.put("TAGR", hipoTAGRp);
        }


        if(this.bosDataBanks.containsKey("DCPB")){
            BosDataBank bDCPB = (BosDataBank) this.bosDataBanks.get("DCPB");
            int rows = bDCPB.rows();
            DataBank hipoDCPB = hipoEvent.createBank("DETECTOR::dcpb", rows);
            for(int loop = 0; loop < rows; loop++){
                int sctr = bDCPB.getInt("ScTr")[loop];
                int sector = (int) sctr/100;
                int track_ID = (int) (sctr-sector*100);
                hipoDCPB.setByte("sector", loop, (byte) sector);
                hipoDCPB.setByte("track_id", loop, (byte) track_ID);
                hipoDCPB.setFloat("x_sc", loop, bDCPB.getFloat("x_SC")[loop]);
                hipoDCPB.setFloat("y_sc", loop, bDCPB.getFloat("y_SC")[loop]);
                hipoDCPB.setFloat("z_sc", loop, bDCPB.getFloat("z_SC")[loop]);
                hipoDCPB.setFloat("cx_sc", loop, bDCPB.getFloat("CX_SC")[loop]);
                hipoDCPB.setFloat("cy_sc", loop, bDCPB.getFloat("CY_SC")[loop]);
                hipoDCPB.setFloat("cz_sc", loop, bDCPB.getFloat("CZ_SC")[loop]);
                hipoDCPB.setFloat("x_v", loop, bDCPB.getFloat("X_v")[loop]);
                hipoDCPB.setFloat("y_v", loop, bDCPB.getFloat("Y_v")[loop]);
                hipoDCPB.setFloat("z_v", loop, bDCPB.getFloat("Z_v")[loop]);
                hipoDCPB.setFloat("r_v", loop, bDCPB.getFloat("R_v")[loop]);
                hipoDCPB.setFloat("chi2", loop, bDCPB.getFloat("Chi2")[loop]);
                hipoDCPB.setInt("status", loop, bDCPB.getInt("Status")[loop]);
            }
            this.hipoDataBanks.put("DCPB", hipoDCPB);
        }
        if(this.bosDataBanks.containsKey("BEAM")){
            BosDataBank bBEAM = (BosDataBank) this.bosDataBanks.get("BEAM");
            int nrows = bBEAM.rows();
            DataBank hipoBEAMp = hipoEvent.createBank("EVENT::beam", nrows);
            for(int loop =0; loop < nrows; loop++){
                hipoBEAMp.setFloat("energy", loop, bBEAM.getFloat("ENERGY")[loop]);
                hipoBEAMp.setFloat("itorus", loop, bBEAM.getFloat("ITORUS")[loop]);
                hipoBEAMp.setFloat("imini", loop, bBEAM.getFloat("IMINI")[loop]);
                hipoBEAMp.setFloat("itag", loop, bBEAM.getFloat("ITAG")[loop]);
            }
        }

        if(this.bosDataBanks.containsKey("ICPB")){
            BosDataBank bICPB = (BosDataBank) this.bosDataBanks.get("ICPB");
            int rows = bICPB.rows();
            DataBank hipoICPB = hipoEvent.createBank("DETECTOR::icpb", rows);
            for(int loop = 0; loop < rows; loop++){
                hipoICPB.setFloat("etc", loop, bICPB.getFloat("Etot")[loop]);
                hipoICPB.setFloat("ecc", loop, bICPB.getFloat("Ecen")[loop]);
                hipoICPB.setFloat("tc", loop, bICPB.getFloat("Time")[loop]);
                hipoICPB.setFloat("xc", loop, bICPB.getFloat("X")[loop]);
                hipoICPB.setFloat("yc", loop, bICPB.getFloat("Y")[loop]);
                hipoICPB.setFloat("m2_hit", loop, bICPB.getFloat("M2_hit")[loop]);
                hipoICPB.setFloat("m3_hit", loop, bICPB.getFloat("M3_hit")[loop]);
                hipoICPB.setInt("status", loop, bICPB.getInt("Status")[loop]);
                hipoICPB.setFloat("zc", loop, bICPB.getFloat("Z")[loop]);
            }
            this.hipoDataBanks.put("ICPB", hipoICPB);
        }


        if(this.bosDataBanks.containsKey("ECPB")==true){
            BosDataBank bECPB = (BosDataBank) this.bosDataBanks.get("ECPB");
            int rows = bECPB.rows();
            DataBank hipoECPB = hipoEvent.createBank("DETECTOR::ecpb", rows);
            for(int loop = 0; loop < rows; loop++){
                int scht = bECPB.getInt("ScHt")[loop];
                int sector = (int) scht/100;
                int whole_hit_ID = (int) scht-sector*100;
                hipoECPB.setByte("sector", loop, (byte) sector);
                hipoECPB.setByte("clusterid", loop, (byte) whole_hit_ID);
                hipoECPB.setFloat("etot", loop, bECPB.getFloat("Etot")[loop]);
                hipoECPB.setFloat("ein" , loop, bECPB.getFloat("Ein")[loop]);
                hipoECPB.setFloat("eout", loop, bECPB.getFloat("Eout")[loop]);
                hipoECPB.setFloat("time", loop, bECPB.getFloat("Time") [loop]);
                hipoECPB.setFloat("path", loop, bECPB.getFloat("Path") [loop]);
                hipoECPB.setFloat("x", loop, bECPB.getFloat("X") [loop]);
                hipoECPB.setFloat("y", loop, bECPB.getFloat("Y") [loop]);
                hipoECPB.setFloat("z", loop, bECPB.getFloat("Z") [loop]);
                hipoECPB.setFloat("m2_hit", loop, bECPB.getFloat("M2_hit")[loop]);
                hipoECPB.setFloat("m3_hit", loop, bECPB.getFloat("M3_hit")[loop]);
                hipoECPB.setFloat("m4_hit", loop, bECPB.getFloat("M4_hit")[loop]);
                hipoECPB.setFloat("chi2", loop, bECPB.getFloat("Chi2EC") [loop]);
                hipoECPB.setInt("innstr", loop, bECPB.getInt("InnStr")[loop]);
                hipoECPB.setInt("outstr", loop, bECPB.getInt("OutStr")[loop]);
                hipoECPB.setInt("status", loop, bECPB.getInt("Status")[loop]);
            }
            this.hipoDataBanks.put("ECPB", hipoECPB);
            //banklist.add(hipoECPB);
        }

        if(this.bosDataBanks.containsKey("LCPB")==true){
            BosDataBank bLCPB = (BosDataBank) this.bosDataBanks.get("LCPB");
            int rows = bLCPB.rows();
            DataBank hipoLCPB = hipoEvent.createBank("DETECTOR::lcpb", rows);
            for(int loop = 0; loop < rows; loop++){
                int scht = bLCPB.getInt("ScHt")[loop];
                int sector = (int) scht/100;
                int lchit_id = scht - sector;
                hipoLCPB.setByte("sector", loop, (byte) sector);
                hipoLCPB.setByte("hit_id", loop, (byte) lchit_id);
                hipoLCPB.setFloat("etot", loop, bLCPB.getFloat("Etot")[loop]);
                hipoLCPB.setFloat("ein" , loop, bLCPB.getFloat("Ein")[loop]);
                hipoLCPB.setFloat("time", loop, bLCPB.getFloat("Time") [loop]);
                hipoLCPB.setFloat("path", loop, bLCPB.getFloat("Path") [loop]);
                hipoLCPB.setFloat("x", loop, bLCPB.getFloat("X") [loop]);
                hipoLCPB.setFloat("y", loop, bLCPB.getFloat("Y") [loop]);
                hipoLCPB.setFloat("z", loop, bLCPB.getFloat("Z") [loop]);
                hipoLCPB.setFloat("chi2", loop, bLCPB.getFloat("Chi2LC") [loop]);
                hipoLCPB.setInt("status", loop, bLCPB.getInt("Status") [loop]);
            }
            this.hipoDataBanks.put("LCPB", hipoLCPB);
            //banklist.add(hipoECPB);
        }
        if(this.bosDataBanks.containsKey("SCPB")){
            BosDataBank bSCPB = (BosDataBank) this.bosDataBanks.get("SCPB");
            int rows = bSCPB.rows();
            DataBank hipoSCPB = hipoEvent.createBank("DETECTOR::scpb", rows);
            for(int loop = 0; loop < rows; loop++){
                int sector = bSCPB.getInt("ScPdHt")[loop]/10000;
                int paddle = bSCPB.getInt("ScPdHt")[loop]/100 - sector*100;
                int hit_id = bSCPB.getInt("ScPdHt")[loop] - sector - paddle;
                //System.err.println(" " + bSCPB.getInt("ScPdHt")[loop] +
                //        "  sector = " + sector + " " + paddle);
                //int paddle = bSCPB.getInt("ScPdHt")[loop]/10000;
                hipoSCPB.setByte("sector", loop, (byte) sector);
                hipoSCPB.setByte("paddle", loop, (byte) paddle);
                hipoSCPB.setByte("hit_id", loop, (byte) hit_id);
                hipoSCPB.setFloat("edep", loop, bSCPB.getFloat("Edep") [loop]);
                hipoSCPB.setFloat("time", loop, bSCPB.getFloat("Time") [loop]);
                hipoSCPB.setFloat("path", loop, bSCPB.getFloat("Path") [loop]);
                hipoSCPB.setFloat("chi2", loop, bSCPB.getFloat("Chi2SC") [loop]);
                hipoSCPB.setInt("status", loop, bSCPB.getInt("Status") [loop]);
            }
            this.hipoDataBanks.put("SCPB", hipoSCPB);
        }

        if(this.bosDataBanks.containsKey("CCPB")){
            BosDataBank bCCPB = (BosDataBank) this.bosDataBanks.get("CCPB");
            int rows = bCCPB.rows();
            DataBank hipoCCPB = hipoEvent.createBank("DETECTOR::ccpb", rows);
            for(int loop = 0; loop < rows; loop++){
                int sector = bCCPB.getInt("ScSgHt")[loop]/100;
                int cluster = bCCPB.getInt("ScSgHt")[loop] - sector*100;
                //int paddle = bSCPB.getInt("ScPdHt")[loop]/10000;
                hipoCCPB.setByte("sector", loop, (byte) sector);
                hipoCCPB.setInt("clusterid", loop, cluster);
                hipoCCPB.setFloat("nphe", loop, bCCPB.getFloat("Nphe") [loop]);
                hipoCCPB.setInt("status", loop, bCCPB.getInt("Status") [loop]);
                hipoCCPB.setFloat("time", loop, bCCPB.getFloat("Time") [loop]);
                hipoCCPB.setFloat("path", loop, bCCPB.getFloat("Path") [loop]);
                hipoCCPB.setFloat("chi2", loop, bCCPB.getFloat("Chi2CC") [loop]);
            }
            this.hipoDataBanks.put("CCPB", hipoCCPB);
        }

    }

    public TreeMap<String,DataBank>  getHipoBankStore(){
        return this.hipoDataBanks;
    }

    public void initBosBanks(BosDataEvent bosEvent){
        this.bosDataBanks.clear();

	    if(bosEvent.hasBank("TAGR:1")){
            //BosDataBank hevt = (BosDataBank) bos_event.getBank("HEVT")
            BosDataBank tagr = (BosDataBank) bosEvent.getBank("TAGR:1");
            this.bosDataBanks.put(tagr.getDescriptor().getName(), tagr);
        }


        if(bosEvent.hasBank("TGBI")==true){
            BosDataBank bTGBI = (BosDataBank) bosEvent.getBank("TGBI");
            this.bosDataBanks.put(bTGBI.getDescriptor().getName(), bTGBI);
        }
        if(bosEvent.hasBank("FBPM")){
            BosDataBank bFBPM = (BosDataBank) bosEvent.getBank("FBPM");
            this.bosDataBanks.put(bFBPM.getDescriptor().getName(), bFBPM);
        }

        if(bosEvent.hasBank("HEVT")){
            BosDataBank bHEVT = (BosDataBank) bosEvent.getBank("HEVT");
            this.bosDataBanks.put(bHEVT.getDescriptor().getName(), bHEVT);
        }

        if(bosEvent.hasBank("EVNT")){
            BosDataBank bPART = (BosDataBank) bosEvent.getBank("EVNT");
            this.bosDataBanks.put(bPART.getDescriptor().getName(), bPART);
        }

        if(bosEvent.hasBank("ECPB")){
            BosDataBank bECPB = (BosDataBank) bosEvent.getBank("ECPB");
            this.bosDataBanks.put(bECPB.getDescriptor().getName(), bECPB);
        }
        if(bosEvent.hasBank("ICPB")){
            BosDataBank bICPB = (BosDataBank) bosEvent.getBank("ICPB");
            this.bosDataBanks.put(bICPB.getDescriptor().getName(), bICPB);
        }
        if(bosEvent.hasBank("DCPB")){
            BosDataBank bDCPB = ( BosDataBank ) bosEvent.getBank("DCPB");
            this.bosDataBanks.put(bDCPB.getDescriptor().getName(), bDCPB);
        }
        if(bosEvent.hasBank("STPB")){
            BosDataBank bSTPB = (BosDataBank) bosEvent.getBank("STPB");
            this.bosDataBanks.put(bSTPB.getDescriptor().getName(), bSTPB);
        }
        if(bosEvent.hasBank("BEAM")){
            BosDataBank bBEAM = (BosDataBank) bosEvent.getBank("BEAM");
            this.bosDataBanks.put(bBEAM.getDescriptor().getName(), bBEAM);
        }
        if(bosEvent.hasBank("LCPB")){
            BosDataBank bLCPB = (BosDataBank) bosEvent.getBank("LCPB");
            this.bosDataBanks.put(bLCPB.getDescriptor().getName(), bLCPB);
        }

        if(bosEvent.hasBank("SCPB")){
            BosDataBank bSCPB = (BosDataBank) bosEvent.getBank("SCPB");
            this.bosDataBanks.put(bSCPB.getDescriptor().getName(), bSCPB);
        }
        if(bosEvent.hasBank("CCPB")){
            BosDataBank bCCPB = (BosDataBank) bosEvent.getBank("CCPB");
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
            Bos2HipoEventBank.printUsage();
            System.exit(0);
        }

        if(args[0].startsWith("-")==false){
            Bos2HipoEventBank.printUsage();
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
            Bos2HipoEventBank.printUsage();
            System.exit(0);
        }
        String  hipoFileName = args[1];
        String  bosFileName  = args[2];


        Bos2HipoEventBank bos2hipo = new Bos2HipoEventBank();

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

            BosDataEvent bosEvent = (BosDataEvent) reader.getNextEvent();
            //bosEvent.showBankInfo("EVNT");
            try{

		        DataEvent hipoEvent = writer.createEvent();
                bos2hipo.initBosBanks(bosEvent);

                bos2hipo.initHipoBank(hipoEvent);
                TreeMap<String,DataBank>  hipoBanks = bos2hipo.getHipoBankStore();

                if(hipoBanks.containsKey("HEVT")==true){
                    hipoEvent.appendBanks(hipoBanks.get("HEVT"));
                }
                if(hipoBanks.containsKey("EVNT")==true){
                    hipoEvent.appendBanks(hipoBanks.get("EVNT"));
                }

                List<DataBank>  detectorBanks = new ArrayList<DataBank>();
                //if(hipo) //##############################################
                if(hipoBanks.containsKey("ICPB")==true){
                    detectorBanks.add(hipoBanks.get("ICPB"));
                }
                if(hipoBanks.containsKey("TAGR")==true){
                    detectorBanks.add(hipoBanks.get("TAGR"));
                }
                if(hipoBanks.containsKey("ECPB")==true){
                    detectorBanks.add(hipoBanks.get("ECPB"));
                }
                if(hipoBanks.containsKey("SCPB")==true){
                    detectorBanks.add(hipoBanks.get("SCPB"));
                }
                if(hipoBanks.containsKey("CCPB")==true){
                    detectorBanks.add(hipoBanks.get("CCPB"));
                }
                if(hipoBanks.containsKey("DCPB")==true){
                    detectorBanks.add(hipoBanks.get("DCPB"));
                }
                if(hipoBanks.containsKey("STPB")==true){
                    detectorBanks.add(hipoBanks.get("STPB"));
                }
                if(hipoBanks.containsKey("LCPB")==true){
                    detectorBanks.add(hipoBanks.get("LCPB"));

                }
                if(hipoBanks.containsKey("BEAM")==true){
                    detectorBanks.add(hipoBanks.get("BEAM"));
                    System.out.println("Detected BEAM");
                }

                //System.out.println(" DETECTOR BANKS SIZE = " + detectorBanks.size());

                if(detectorBanks.size()>0){
                    DataBank[] banks  = new DataBank[detectorBanks.size()];
                    for(int i = 0; i < detectorBanks.size();i++){
                        banks[i] = detectorBanks.get(i);
                    }
                    hipoEvent.appendBanks(banks);
                }

                if(hipoBanks.containsKey("EVNT")==true && hipoBanks.containsKey("HEVT")==true){
                    writer.writeEvent(hipoEvent);
                }
            } catch (Exception e) {
                System.out.println("---> error at event # " + progressCounter);
            }
        }

        writer.close();
    }
}
