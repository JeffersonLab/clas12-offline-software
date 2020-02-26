/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.decode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioSource;

/**
 *
 * @author gavalian
 */
public class DetectorDecoderDebug {
    
    private CodaEventDecoder          codaDecoder = null; 
    private int      totalRawSize = 0;
    private int totalComparedSize = 0;
    private int       totalErrors = 0;
    private int        totalEvent = 0;

    public DetectorDecoderDebug(){
        codaDecoder = new CodaEventDecoder();
    }
    
    public int getDifference(DetectorDataDgtz a, DetectorDataDgtz b){
        int summ = 0;
        short[] ap = a.getADCData(0).getPulseArray();
        short[] bp = a.getADCData(0).getPulseArray();
        if(ap.length!=bp.length){
            System.out.println("*** ERROR : pulses do not have same size....");
            return 0;
        } 
        
        for(int i = 0; i < ap.length; i++){
            if(ap[i]!=bp[i]) return i;
        }
        
        return summ;
    }
    
    public void compareMaps(Map<Integer,DetectorDataDgtz> raw, Map<Integer,DetectorDataDgtz> packed, int eventNumber){
        int size = raw.size();
        int comparedSize = 0;
        
        this.totalEvent++;
        this.totalRawSize += size;
        
        for(Map.Entry<Integer,DetectorDataDgtz> entry : raw.entrySet()){
            if(packed.containsKey(entry.getKey())==false){
                System.out.println(
                        String.format("*** ERROR : event #%6d -> %4d %4d %4d raw pulse does not exist in bit-packed array",
                                eventNumber,
                                entry.getValue().getDescriptor().getCrate(),
                                entry.getValue().getDescriptor().getSlot(),
                                entry.getValue().getDescriptor().getChannel()
                        ));
                this.totalErrors++;
            } else {
                DetectorDataDgtz data = packed.get(entry.getKey());
                int diff = this.getDifference(entry.getValue(), data);
                if(diff==0){
                    comparedSize++;
                } else {
                    System.out.println("*** ERROR : something went wrong with DATA : " + data.getDescriptor().toString());
                    this.totalErrors++;
                }
            }
        }
        
        this.totalComparedSize+=comparedSize;
        System.out.println(String.format(">>> COMPARISION FOR event #%8d ->  processed %8d / passed %8d",
                eventNumber,size,comparedSize));
    }
    
    public Map<Integer,DetectorDataDgtz> getADCMapPacked(DataEvent event){
        Map<Integer,DetectorDataDgtz> dataMap = new HashMap<Integer,DetectorDataDgtz>();
         List<FADCData>  fadcPacked = codaDecoder.getADCEntries((EvioDataEvent) event);
         if(fadcPacked!=null){
             List<DetectorDataDgtz> fadcUnpacked = FADCData.convert(fadcPacked);
             for(DetectorDataDgtz data : fadcUnpacked){
                 if (data.getADCSize()>0) {
                     if(data.getADCData(0).getPulseSize()>0){
                         Integer hash =  DetectorDescriptor.generateHashCode(
                                 data.getDescriptor().getCrate(),
                                 data.getDescriptor().getSlot(),
                                 data.getDescriptor().getChannel()
                         );
                         dataMap.put(hash, data);
                     }
                 }
             }
         }
        return dataMap;
    }
    
    public Map<Integer,DetectorDataDgtz> getADCMapRaw(DataEvent event){
        
        Map<Integer,DetectorDataDgtz> dataMap = new HashMap<Integer,DetectorDataDgtz>();
        List<DetectorDataDgtz> dataList = codaDecoder.getDataEntries( (EvioDataEvent) event);
        
        for(DetectorDataDgtz data : dataList){
            if (data.getADCSize()>0) {
                if(data.getADCData(0).getPulseSize()>0){
                    Integer hash =  DetectorDescriptor.generateHashCode(
                            data.getDescriptor().getCrate(),
                            data.getDescriptor().getSlot(),
                            data.getDescriptor().getChannel()
                            );
                    dataMap.put(hash, data);
                }
            }
        }
        
        return dataMap;
    }
    
    public void printComparisonStats()  {
        System.out.println("\n Comparison statistics:");
        System.out.println("\t Number of events:         " + this.totalEvent);
        System.out.println("\t Number of pulses:         " + this.totalRawSize);
        System.out.println("\t Number of matched pulses: " + this.totalComparedSize);        
        System.out.println("\t Number of errors:         " + this.totalErrors);        
    }
    
    public static void main(String[] args){
        String inputFile = "/Users/gavalian/Work/Software/project-5a.0.0/data/raw/test_004714.evio.00000";
        
        if(args.length>0){
            inputFile = args[0];
        }
        DetectorDecoderDebug debugger = new DetectorDecoderDebug();
        EvioSource reader = new EvioSource();
        reader.open(inputFile);
        int eventNumber = 0;
        while(reader.hasEvent()==true){
            EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
            Map<Integer,DetectorDataDgtz> mapRow = debugger.getADCMapRaw(event);
            Map<Integer,DetectorDataDgtz> mapPkt = debugger.getADCMapPacked(event);
            debugger.compareMaps(mapRow, mapPkt, eventNumber);
            eventNumber++;
        }
        debugger.printComparisonStats();
                
    }
}
