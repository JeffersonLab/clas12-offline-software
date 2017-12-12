/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.utils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.coda.jevio.DataType;
import org.jlab.coda.jevio.EventBuilder;
import org.jlab.coda.jevio.EventWriter;
//import org.jlab.coda.jevio.EvioCompactEventWriter;
import org.jlab.coda.jevio.EvioEvent;
import org.jlab.coda.jevio.EvioException;
import org.jlab.coda.jevio.EvioNode;
import org.jlab.coda.jevio.EvioReader;
import org.jlab.io.evio.EvioFactory;

/**
 *
 * @author gavalian
 */
public class EvioFileRecover {
    public EvioFileRecover(){
        
    }
    public static void test(){
        String outputfile = "/Users/gavalian/Work/Software/Release-8.0/COATJAVA/coatjava/../svt257er_000015_recovered.evio";
        
        try {
            EventWriter writer = new EventWriter(outputfile);
            EventBuilder eventBuilder = new EventBuilder(1, DataType.BANK, 1);
            //byte[] byteData1 = new byte[499990];
            
            EvioEvent ev = eventBuilder.getEvent();
            
            //ev.appendByteData(byteData1);
            writer.writeEvent(ev);
            writer.close();
        } catch (EvioException ex) {
            Logger.getLogger(EvioFileRecover.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EvioFileRecover.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public static void recoverFile(String inputfile, String outputfile)  {
        
        File outFile = new File(outputfile);
        if(outFile.exists()==true){
            System.out.println();
        }
        System.out.println("----> opening file : " + inputfile);
        if(inputfile.compareTo(outputfile)==0){
            System.out.println("----> error");
            System.out.println("----> input and output files are the same");
            System.out.println("----> exiting\n\n");
            return;
        }
        
        EvioReader reader = null;
        
        EventWriter writer = null;
        
        try {
            
            reader = new EvioReader(inputfile,true,true);
            writer = new EventWriter(outputfile);
            int counter = 0;
            while(true){
                try {
                    EvioEvent event = reader.parseNextEvent();
                    counter++;
                    writer.writeEvent(event);
                } catch (Exception e){
                    System.out.println("****** REACHED END OF FILE ****  EVENT # " 
                            + counter + "   *******" );
                    break;
                }
            }
            writer.close();
        } catch (EvioException ex) {
            Logger.getLogger(EvioFileRecover.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EvioFileRecover.class.getName()).log(Level.SEVERE, null, ex);
        }
       
    }
    public static void main(String[] args){
        /*
        String inputfile  = "/Users/gavalian/Work/Software/Release-8.0/COATJAVA/coatjava/../svt257er_000015.evio.0";
        String outputfile = "/Users/gavalian/Work/Software/Release-8.0/COATJAVA/coatjava/../svt257er_000015_recovered.evio";
        */
        String inputfile  = args[0];
        String outputfile = args[1];
        EvioFileRecover.recoverFile(inputfile, outputfile);
        /*
        try {
            EvioReader reader = new EvioReader(inputfile);
            
            int evcount = reader.getEventCount();
            System.out.println("Number of events = " + evcount);
            
            EvioEvent event = reader.parseEvent(4);
            byte[] array = event.getByteData();
            if(array!=null){
                System.out.println("eurica");
            } else {
                System.out.println("this is not right");
            }
            //EvioEvent event = reader.getEvent(4);
            System.out.println(event);
            //List<EvioNode>  nodes =
            
            //EvioFileRecover.test();
            /*
            //String inputfile = args[0];
            
            
            EvioReader reader;
            EventWriter writer;
            
            try {
            reader = new EvioReader(inputfile);
            //writer = new EventWriter(outputfile);
            
            EvioCompactEventWriter evioWriter = new EvioCompactEventWriter(outputfile, null,
            0, 0,
            15*300,
            2000,
            8*1024*1024,
            ByteOrder.LITTLE_ENDIAN, null, true);
            //, 1000000, 2,
            //        ByteOrder.LITTLE_ENDIAN, null, null);
            int counter = 0;
            while(true&counter<100){
            
            EvioEvent  event = reader.nextEvent();
            
            if(event==null) break;
            System.out.println(" event # ---> " + counter);
            System.out.println(event);
            byte[]  array = event.getByteData();
            
            if(array!=null){
            ByteBuffer  byteArray = ByteBuffer.wrap(array);
            byteArray.order(event.getByteOrder());
            //writer.writeEvent(event);
            evioWriter.writeEvent(byteArray);
            } else {
            System.out.println("event buffer = null at event # " + counter);
            }
            counter++;
            if(counter%1000==0)
            System.out.println("EVENT # " + counter + " is properly read");
            }
            
            System.out.println(" LAST EVENT READ = " + counter);
            evioWriter.close();
            //writer.close();
            } catch (EvioException ex) {
            Logger.getLogger(EvioFileRecover.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
            Logger.getLogger(EvioFileRecover.class.getName()).log(Level.SEVERE, null, ex);
            }*/
        
    }
}
