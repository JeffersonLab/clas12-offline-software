/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.decode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioSource;

/**
 *
 * @author gavalian
 */
public class CLAS12DecoderCompact {
    
    
    public static void readFile(String inputFile){
        CodaEventDecoder decoder = new CodaEventDecoder();
        EvioSource reader = new EvioSource();
        System.out.println("openning file : " + inputFile);
        reader.open(inputFile);
        int counter = 0;

        while(reader.hasEvent()==true&&counter<20){
            EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
            //List<FADCData>  adc = decoder.getADCEntries(event, 73,57638);
            System.out.println("========================= EVENT # " + counter);
            List<FADCData>  adc = decoder.getADCEntries(event);//, 73,57601);
            
            for(FADCData data : adc){
                //System.out.println(data);
                data.show();
            }
            
            List<DetectorDataDgtz> dgtz = FADCData.convert(adc);
            
            for(DetectorDataDgtz data : dgtz){
                System.out.println("ADC size = " + data.getADCSize() + "  " + data.getADCData(0).getPulseArray().length);
                System.out.println(data);
            }
            //List<DetectorDataDgtz> data = decoder.getDataEntries(event);
            counter++;
        }
    }
    
    public static void main(String[] args){
        //String inputFile = "/Users/gavalian/Work/Software/project-4a.0.0/data/compressed/cnd_000096.evio.0";
        //String inputFile = "/Users/gavalian/Work/Software/project-4a.0.0/data/compressed/cnd_000106.evio.0";
        //String inputFile = "/Users/gavalian/Work/Software/project-4a.0.0/data/compressed/cnd_004459.evio.0";
        String inputFile = "/Users/gavalian/Work/Software/project-5a.0.0/clas_004604.evio.00000";
       CLAS12DecoderCompact.readFile(inputFile);
        /*CodaEventDecoder decoder = new CodaEventDecoder();
        EvioSource reader = new EvioSource();
        System.out.println("openning file : " + inputFile);
        reader.open(inputFile);
        int counter = 0;
        int uncompressed = 0;
        int compressed   = 0;
        while(reader.hasEvent()==true&&counter<50){
            EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
            //List<FADCData>  adc = decoder.getADCEntries(event, 73,57638);
            List<FADCData>  adc = decoder.getADCEntries(event, 73,57601);
            Map<Integer,FADCData> rawMap = new HashMap<>();
            Map<Integer,FADCData> encMap = new HashMap<>();
            
            if(adc!=null){
                for(int i = 0; i < adc.size(); i++){
                    FADCData data = adc.get(i);
                    data.getDescriptor().setSectorLayerComponent(data.getDescriptor().getCrate(),
                            data.getDescriptor().getSlot(),data.getDescriptor().getChannel());
                    if(data.getSize()>95){                        
                        rawMap.put(data.getDescriptor().getHashCode(), data);
                    } else {
                        encMap.put(data.getDescriptor().getHashCode(), data);
                    }
                }
            }
            
            for(Map.Entry<Integer,FADCData>  data : encMap.entrySet()){
                
                int hash = data.getValue().getDescriptor().getHashCode();
                
                if(rawMap.containsKey(hash)==true){
                    data.getValue().show();
                    uncompressed += rawMap.get(hash).getSize();
                    compressed   += data.getValue().getSize();
                    List<Short> decodedList = data.getValue().getDecoded();
                    System.out.println(data.getValue().getPulseString());
                    System.out.println("------- decode size = " + decodedList.size());
                    data.getValue().decode();
                    System.out.println("------- end decode");
                    System.out.println(rawMap.get(hash).getPulseString());
                    System.out.println("------- array ");
                    StringBuilder str = new StringBuilder();
                    for(int j = 0; j < decodedList.size(); j++){
                        str.append(String.format("%6d", decodedList.get(j)));
                        if((j+1)%16==0) str.append("\n");
                    }
                    System.out.println(str.toString());
                    System.out.println("--- end");
                }
            }*/
            /*
            if(adc!=null){
                System.out.println("------------------------------ EVENT " + counter);
                System.out.println(" SIZE = " + adc.size());
                for(int i = 0; i < adc.size(); i++){
                    //System.out.println("\t ADC PULSE SIZE = " + i + " : "+ adc.get(i).getSize());
                    adc.get(i).show();
                    System.out.println(adc.get(i).getPulseString());
                    if(adc.get(i).getSize()<96) adc.get(i).decode();
                    if(adc.get(i).getSize()==96){
                        uncompressed += adc.get(i).getSize();
                    } else {
                        compressed += adc.get(i).getSize();
                    }
                }
            }*/
            
            /*counter++;
        }
        double ratio = ((double) compressed)/uncompressed;
        System.out.println("events processed = " + counter + "  ratio = " + ratio);*/
    }
}
