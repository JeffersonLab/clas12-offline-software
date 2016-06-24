/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.utils;

import java.util.ArrayList;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataDictionary;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioDataSync;
import org.jlab.io.evio.EvioSource;

/**
 *
 * @author gavalian
 */
public class BoardDecoderVSCM {
    //ArrayList< ArrayList<BoardRecordVSCM> >  boardEvents =  new ArrayList< ArrayList<BoardRecordVSCM> >();
    ArrayList<BoardRecordVSCM> boardEvent = new ArrayList<BoardRecordVSCM>();
    
    final static int DATA_TYPE_BLKHDR  = 0x00;
    final static int DATA_TYPE_BLKTLR  = 0x01;
    final static int DATA_TYPE_EVTHDR  = 0x02;
    final static int DATA_TYPE_TRGTIME = 0x03;
    final static int DATA_TYPE_BCOTIME = 0x04;
    final static int DATA_TYPE_FSSREVT = 0x08;
    final static int DATA_TYPE_DNV     = 0x0E;
    final static int DATA_TYPE_FILLER  = 0x0F;
    final static int UNIX_TYPE_FILLER  = 0x0D;
    
    public  BoardDecoderVSCM(){
        
    }
    
    
    public int getLayer(int slotid, int hfcbid,int chipid){
        if(slotid==10&&hfcbid==0){
            if(chipid==1||chipid==2){
                return 5;
            } 
            if(chipid==3||chipid==4){
                return 6;
            }
        }
        
        if(slotid==9&&hfcbid==1){
           if(chipid==1||chipid==2){
                return 1;
            } 
            if(chipid==3||chipid==4){
                return 2;
            } 
        }
        
        if(slotid==9&&hfcbid==0){
            if(chipid==1||chipid==2){
                return 1;
            } 
            if(chipid==3||chipid==4){
                return 2;
            }
        }
        
        if(slotid==8&&hfcbid==1){
            if(chipid==1||chipid==2){
                return 5;
            } 
            if(chipid==3||chipid==4){
                return 6;
            }
        }
        return 0;
        /*
        int ring = this.getRing(slotid,hfcbid);
        int locallayer = 2;
        if(chipid<=2) locallayer = 1;
        return (ring-1)*2 + locallayer;*/
    }
    
    public int getStrip(int chipid, int chan){
        if(chipid==1){
            return chan+1;
        }
        
        if(chipid==2){
            return chan + 129;
        }
        
        if(chipid==3){
            return chan + 1;
        }
        
        if(chipid==4){
            return chan + 129;
        }
        return 0;
        /*
        int coef = chipid;
        if(coef>2) coef = coef - 1;
        return chan*coef + 1;*/
    }
    
    public int getRing(int slotid, int hfcbid){
        int sector = (slotid-3)*2 + hfcbid + 1;
        if(sector>10 && sector <=24){
            return 2;
        }
        if(sector>24 && sector <=42){
            return 3;
        }
        if(sector > 42) return 4;
        return 1;       
    }
    
    public int getSector(int slotid, int hfcbid){
        
        if(slotid==10&&hfcbid==0){
            return 1;
        }
        
        if(slotid==9&&hfcbid==1){
            return 1;
        }
        
        if(slotid==9&&hfcbid==0){
            return 6;
        }
        
        if(slotid==8&&hfcbid==1){
            return 10;
        }
        
        return 0;
        /*
        int sector = (slotid-3)*2 + hfcbid + 1;
        if(sector>10 && sector <=24){
            return sector-10;
        }
        
        if(sector>24 && sector <=42){
            return sector-24;
        }
        if(sector > 42){
            return sector - 42;
        }
        return sector;*/
    }
    
    
    public ArrayList<BoardRecordVSCM> getRecords(){ return this.boardEvent;}
    
    public void decode(int[] array){
        int unixtime = 0;
        int totalLength = array.length;
        int icount = 0;
        int ievent = 0;
        int bcostart = 0;
        int bcostop  = 0;
        boardEvent.clear();
        //ArrayList<BoardRecordVSCM> boardevent = null;
        int  slotid = 0;        
        while(icount<=totalLength){
            if(icount>=totalLength) break;
            long word = this.getWord(array[icount]);
            if ((word & 0x80000000) != -1){
                int type = (int) ((word >> 27) & 0xF);               
                switch(type){
                    case DATA_TYPE_BLKHDR:
                        int nevents = (int) ((word>>11) & 0x3FF);
                        slotid = (int) ((word >> 22) & 0x1f);
                        //System.err.println("-> BLOCK HEADER in position = " + icount 
                        //+ "  N EVENTS = " + nevents + "  SLOT = " + slotid);
                        break;
                    case DATA_TYPE_BLKTLR:
                        int nwords = (int) ((word) & 0xFFFFF);
                        /*
                        if(boardevent!=null){
                            for(BoardRecordVSCM rec : boardevent){
                                rec.slotid = slotid;
                            }
                        }*/
                        //System.err.println("-> BLOCK TRAILER in position = " + icount 
                        //+ " N WORDS = " + nwords + "  slot id = " + slotid);
                        break;
                    case DATA_TYPE_BCOTIME:
                        //System.out.println("FOUND BCO TIME " + String.format("%X", word));
                        bcostop   = (int) ((word>>16) & 0x00FF);
                        bcostart  = (int) ((word) & 0x00FF);
                        break;
                    case DATA_TYPE_EVTHDR:
                        ievent = 0;
                        //System.out.println("FOUND EVENT HEADER " + String.format("%X", word));
                        //System.err.println("---------> EVENT HEADER in position = " + icount);                                                
                        break;
                    case DATA_TYPE_TRGTIME:                       
                        //System.err.println("----> TRIGGER TIME found at position = " + icount);
                        icount++;
                        break;
                    case DATA_TYPE_FSSREVT:
                    {
                        int chipID = (int) (word >> 19) & 0x7; // get chip id
                        int channel = (int) (word >> 12) & 0x7F; // get channel
                        int bco = (int) (word >> 4) & 0xFF; // get bco time
                        int adc = (int) (word >> 0) & 0x7; // get adc -- value starts at 0
                        int hfcbID = (int) (word >> 22) & 0x1;
                        BoardRecordVSCM record = new BoardRecordVSCM();
                        record.slotid = slotid;
                        //if(record.slotid>=11) record.slotid = slotid - 2;
                        record.adc = adc;
                        record.channel = channel;
                        record.bco = bco;
                        record.chipid = chipID;
                        record.hfcbid = hfcbID;
                        record.bcostart = bcostart;
                        record.bcostop = bcostop;
                        if(boardEvent!=null) {
                            //System.err.println(record);
                            boardEvent.add(record);
                        }
                        //System.err.println("----------------> EVENT " + ievent + " slot = " + "  chip = " + chipID + " chan = " + channel 
                        //+ " time = " + bco + " adc = " + adc + " hfcID = "
                        //+ hfcbID);
                        ievent++;
                    }
                    break;
                    default:
                        break;
                }
            }
            icount++;
        }
    }
    
    
    public EvioDataBank getConvertedBanks(EvioDataEvent event){
        EvioDataBank bstBank = (EvioDataBank) event.getDictionary().createBank("BST::dgtz", boardEvent.size());
        //System.err.println(" CREATE BST BANK with size = " + boardEvent.size());
        int counter = 0;
        for(BoardRecordVSCM rec : boardEvent){
            int sector = this.getSector(rec.slotid, rec.hfcbid);
            int layer  = this.getLayer(rec.slotid, rec.hfcbid,rec.chipid);
            int strip  = this.getStrip(rec.chipid,rec.channel);
            //System.err.println(" slot ID = " + rec.slotid + "  HFCBID =  " + rec.hfcbid
            //        + "  CHIPID = " + rec.chipid + " CHANNEL = " + rec.channel + "  SECTOR = " + sector +
             //               "  LAYAR = " + layer + " STRIP = " + strip);
            //System.err.println("entry " + counter + "  SLOT = " +
            //rec.slotid + "  HFCBID = "  + rec.hfcbid + " CHIP = " + rec.chipid + " CHANNEL = " + rec.channel
            //+ " SECTOR = " + sector + " LAYER = " + layer );
            bstBank.setInt("layer",counter,layer);
            bstBank.setInt("strip",counter,strip);
            bstBank.setInt("sector",counter,sector);
            bstBank.setInt("ADC", counter,rec.adc);
            counter++;
        }
        return bstBank;
        //return null;
    }
    
    public void decodeold(int[] array){
        int unixtime=0;
       for(int loop = 3; loop < array.length; loop++){
           if(unixtime==1)
           {
               unixtime = 0;
               continue;
           }
           long word = getWord(array[loop]);
           if ((word & 0x80000000) != -1) {
               int type = (int) ((word >> 27) & 0xF);
               
               int slotID = 0;
               int hfcbID = 0;
               int chipID = 0;
               int channel = 0;
               int bco = 0;
               int adc = 0;
               /* HEADER */
               if(type==DATA_TYPE_BLKHDR) {
                   
                   System.err.println("-------> block started here = " + loop);
               }

               if(type==DATA_TYPE_BLKTLR) {
                   slotID = (int) ((word >> 22) & 0x1f);
                   //System.err.println("-------> block  ends here = " + loop);
               }
               if(type==DATA_TYPE_EVTHDR) {
                   //System.err.println("-------> event started here = " + loop);
               }
               if(type==DATA_TYPE_TRGTIME) {
                   //System.err.println("-------> TRIG TIME = " + loop);
               }
               
               if(type==UNIX_TYPE_FILLER ) {
                   unixtime = 1;
               }
               if(type==DATA_TYPE_BCOTIME) {
                   
               }
               if(type==DATA_TYPE_FSSREVT) {
                   //	// System.out.printf(" {FSSREVT}");
                   //	// System.out.printf(" HFCBID: %1d", (word >> 22) & 0x1);
                   //	// System.out.printf(" CHIPID: %1d", (word >> 19) & 0x7);
                   //	// System.out.printf(" CH: %3d", (word >> 12) & 0x7F);
                   //	// System.out.printf(" BCO: %3d", (word >> 4) & 0xFF);
                   //	// System.out.printf(" \n");
                   //// System.out.printf(" ADC: %1d\n", (word >> 0) & 0x7);
                   chipID = (int) (word >> 19) & 0x7; // get chip id
                   channel = (int) (word >> 12) & 0x7F; // get channel
                   bco = (int) (word >> 4) & 0xFF; // get bco time
                   adc = (int) (word >> 0) & 0x7; // get adc -- value starts at 0
                   hfcbID = (int) (word >> 22) & 0x1;
                   System.err.println("slot = " + slotID + "  chip = " + chipID + " chan = " + channel 
                           + " time = " + bco + " adc = " + adc + " hfcID = "
                   + hfcbID);
               }
               if(type==DATA_TYPE_DNV) {               
                   System.exit(0);
               }
               if(type==DATA_TYPE_FILLER) {
                   
               }
               
               if(slotID==3 || slotID==4) {
                   //	// System.out.println(" stored hits " );                   
               }
               
           }

       }
   }
   
   public long getWord(int value){
       return (long) value;
   }
   
   public static void main(String[] args){
       
       String inputFileName = args[0];
       
       BoardDecoderVSCM decoder = new BoardDecoderVSCM();
       EvioSource reader = new EvioSource();
       //reader.open("/Users/gavalian/Work/DataSpace/svt2test.dat_000819.evio");
       reader.open(inputFileName);
       EvioDataSync writer = new EvioDataSync();
       writer.open("BST_decoded_data.evio");
       int evcounter = 0;
       int writecounter = 0;       
       while(reader.hasEvent()){
           if(evcounter%400==0){
               System.err.println("-----------> processed events = " + evcounter
               + "  events written = " + writecounter);
           }
           EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
           //event.getDictionary().show();
           //event.showNodes();
           //System.err.println("\n\n");
           //System.err.println("***************************************************************");
           //System.err.println("*********************  EVENT START " + evcounter);
           //System.err.println("***************************************************************");
           //System.err.println("\n\n");
           int[] buffer = event.getInt(57604, 1);
           
           if(buffer!=null){
               System.err.println(" BUFFER LENGTH = " + buffer.length);
               
               try {
               //System.err.println(" buffer length = " + buffer.length);
                   decoder.decode(buffer);
                   
                   EvioDataEvent outevent = writer.createEvent((EvioDataDictionary) event.getDictionary());
                   
                   EvioDataBank  bstbank = decoder.getConvertedBanks(event);
                   System.err.println(" writing event " + evcounter + "  with " +
                           bstbank.rows() + "  banks");
                   EvioDataEvent evout = writer.createEvent((EvioDataDictionary) event.getDictionary());
                   if(bstbank.rows()>4){
                       evout.appendBank(bstbank);
                       writer.writeEvent(evout);                   
                       writecounter++;                   
                   }
               } catch (Exception e){
                   System.err.println("********* ERROR : something went wrong with event # "
                   + evcounter);
               }
           }
           evcounter++;
       }
       writer.close();
       System.err.println("\n\n FINAL ------> processed events = " + evcounter
               + "  events written = " + writecounter);
       System.err.println("\n\n Done...\n\n");
   }
}
