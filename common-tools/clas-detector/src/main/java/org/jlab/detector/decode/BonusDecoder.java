/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.decode;

import java.util.ArrayList;
import java.util.List;
import org.jlab.coda.jevio.EvioNode;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioTreeBranch;
import org.jlab.io.evio.EvioSource;
/**
 *
 * @author gavalian
 */
public class BonusDecoder {
    private  CodaEventDecoder     codaDecoder = null;
    private  EvioSource           reader      = new EvioSource();
    
    public BonusDecoder(){
        codaDecoder = new CodaEventDecoder();
    }
    
    public void open(String filename){     
        reader.open(filename);
    }
    
    public boolean hasEvent(){
        return reader.hasEvent();
    }
    
    public List<DetectorDataDgtz> nextEvent(int crate){
        
        EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
        List<EvioTreeBranch> branches = codaDecoder.getEventBranches(event);
        //System.out.println("Next Event-----");
        for(int i = 0; i < branches.size(); i++){
            EvioTreeBranch branch = branches.get(i);
            //System.out.println("node found : tag = " + branch.getTag() + "  num = " + branch.getNum());
            if(branch.getTag()==crate){
                for(EvioNode node : branch.getNodes()){
                    /*System.out.println("\t ["+branch.getTag()+"] : node : tag = "
                    + node.getTag() + " , num = " + node.getNum() + ", type = " + 
                            node.getDataTypeObj());*/
                    if(node.getTag()==57641){
                        //System.out.println("analyzing data----");
                        List<DetectorDataDgtz> data = codaDecoder.getDataEntries_57641(crate, node, event);
                        return data;
                        //System.out.println("data size = " + data.size());
                        /*for(int d = 0; d < data.size(); d++){
                            System.out.println(data.get(d).toString());
                            //data.get(i).
                        }*/
                    }
                }
            }
        }
        return new ArrayList<DetectorDataDgtz>();
        //EvioTreeBranch cbranch = codaDecoder.getEventBranch(branches, crate);
        //if(cbranch == null ) return ;
/*
        for (EvioNode node : cbranch.getNodes()) {
            if(node.getTag()==57641){
                //  This is bit-packed PULSE mode for BONUS
                System.err.println("found tag = " + node.getTag() + " " + node.getNum());
                //getDataEntries_57640(crate, node, event);
            }
        }*/
    }
    
    public static void main(String[] args){
        String filename = "/Users/gavalian/Work/DataSpace/clas12/bonus/bonustest_000002.evio.00000";
        BonusDecoder decoder = new BonusDecoder();
        decoder.open(filename);
        int counter = 0;
        while(decoder.hasEvent()==true&&counter<100){
            counter++;
            decoder.nextEvent(63);
            
            List<DetectorDataDgtz> data = decoder.nextEvent(63);
            System.out.println("printout event # " + counter);
            for(int i = 0; i < data.size(); i++){
                DetectorDataDgtz bonusData = data.get(i);
                long timestamp = bonusData.getADCData(0).getTimeStamp();
                short[]  pulse = bonusData.getADCData(0).getPulseArray();
                System.out.println("TIME STAMP = " + timestamp);
                int crate = bonusData.getDescriptor().getCrate();
                int  slot = bonusData.getDescriptor().getSlot();
                int channel = bonusData.getDescriptor().getChannel();
                System.out.printf("CRATE : %5d , SLOT : %5d , CHANNEL = %5d \n",crate,slot,channel);
                System.out.printf("%6d : ",pulse.length);
                for(int p = 0; p < pulse.length; p++){
                    System.out.printf("%6d ", pulse[p]);
                }
                System.out.println();
            }
        }
    }
}
