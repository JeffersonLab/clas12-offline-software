/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.service;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.detector.decode.CLASDecoder;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataSource;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioETSource;
import org.jlab.io.evio.EvioSource;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.ring.DataDistributionRing;
import org.jlab.io.ring.DataRingProducer;
import org.jlab.utils.benchmark.ProgressPrintout;
import org.jlab.utils.options.OptionParser;

/**
 *
 * @author gavalian
 */
public class DataDistributionService {
    
    //private EvioETSource etSource = null;
    private DataDistributionRing ringProducer = null;
    private CLASDecoder decoder = null;
    private DataSource  dataSource  = null;
    private int decoderDelay        = 1;
    
    public DataDistributionService(){
        decoder = new CLASDecoder();
    }
    
    public void connect(String host,String file, boolean remote){
        dataSource = new EvioETSource(host);
        ( (EvioETSource) dataSource).setRemote(remote);
        dataSource.open(file);        
    }
    
    public void connect(String file){
        dataSource = new EvioSource();
        dataSource.open(file);
    }
    
    public void connectHipo(String file){
        dataSource = new HipoDataSource();
        dataSource.open(file);
    }
    
    public void setDelay(int delay){
        this.decoderDelay = delay;
    }
    /**
     * service works off ET ring
     */
    public void startServiceEt(){
        
        ProgressPrintout progress = new ProgressPrintout();
        ringProducer = new DataDistributionRing();
        ringProducer.initProxy();
        ringProducer.initRegistrar();
        ringProducer.initRing();
        ringProducer.setDelay(0);
        
        EvioETSource etSource = (EvioETSource) this.dataSource;
        
        while(true){
            while(etSource.hasEvent()==false){
                this.waitFor(200);
                etSource.loadEvents();                
            }
            
            while(etSource.hasEvent()==true){
                DataEvent event = dataSource.getNextEvent();
                
                HipoDataEvent decodedEvent = (HipoDataEvent) decoder.getDataEvent(event);
                ringProducer.addEvioEvent((EvioDataEvent) event);
                ringProducer.addEvent(decodedEvent);
                progress.updateStatus();
                if(this.decoderDelay>2){
                    this.waitFor(decoderDelay);
                }
            }
        }
    }
    
    /**
     * Introduces delay
     * @param ms 
     */
    private void waitFor(int ms){ 
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Logger.getLogger(DataDistributionService.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
    public void startService(){
        
        ProgressPrintout progress = new ProgressPrintout();
        ringProducer = new DataDistributionRing();
        ringProducer.initProxy();
        ringProducer.initRegistrar();
        ringProducer.initRing();
        ringProducer.setDelay(decoderDelay);
        
        int counter = 0;
        
        while(true){
            
            while(dataSource.hasEvent()==true){
                DataEvent event = dataSource.getNextEvent();                        
                if(event!=null){
                    HipoDataEvent decodedEvent = (HipoDataEvent) decoder.getDataEvent(event);
                    ringProducer.addEvent(decodedEvent);
                    progress.updateStatus();
                    if(this.decoderDelay>2){
                        this.waitFor(this.decoderDelay);
                    }
                } else {
                    System.out.println(" error there are no events");
                }
            }
            counter++;
            /*System.out.println();
            System.out.println(">>>>> source is being reset interations : " + counter);
            System.out.println();*/
            dataSource.reset();
        }
    }
    
    
    public void startServiceHipo(){
        
        ProgressPrintout progress = new ProgressPrintout();
        ringProducer = new DataDistributionRing();
        ringProducer.initProxy();
        ringProducer.initRegistrar();
        ringProducer.initRing();
        ringProducer.setDelay(decoderDelay);
        int counter = 0;
        Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    ringProducer.shutdown();
                    
                    System.exit(0);
                }
        });
        while(true){
            
            while(dataSource.hasEvent()==true){
                HipoDataEvent event = (HipoDataEvent) dataSource.getNextEvent(); 
                try {
                    ringProducer.addEvent(event);
                } catch (Exception e) {
                    
                }
                progress.updateStatus();
                if(this.decoderDelay>0){
                    this.waitFor(this.decoderDelay);
                }
            }
            counter++;
            /*System.out.println();
            System.out.println(">>>>> source is being reset interations : " + counter);
            System.out.println();*/
            dataSource.reset();
        }
    }
        
    public static void main(String[] args){
        
        OptionParser  parser = new OptionParser();
        
        //parser.addOption("-et","");
        parser.addOption("-d","1");
        parser.addRequired("-type");
        parser.addRequired("-file");
        parser.addOption("-r","true");
        parser.addOption("-host", "localhost");
        
        parser.parse(args);
        
        if(parser.hasOption("-type")&&parser.hasOption("-file")){
            
            String    type = parser.getOption("-type").stringValue();
            String    file = parser.getOption("-file").stringValue();
            String  etHost = parser.getOption("-host").stringValue();
            
            int    delay = parser.getOption("-d").intValue();
            
            if(type.compareTo("evio")!=0&&type.compareTo("et")!=0&&
                    type.compareTo("hipo")!=0){
                System.out.println("\n\n TYPE parameter has to be evio or et or hipo");
                System.exit(0);
            }
            
            if(type.contains("et")==true){
                DataDistributionService service = new DataDistributionService();
                service.setDelay(delay);
                service.connect(etHost,file, true);
                service.startServiceEt();
            } 
            if(type.contains("evio")==true){
                System.out.println("   >>>> starting hipo service");
                DataDistributionService service = new DataDistributionService();
                service.setDelay(delay);
                service.connect(file);
                service.startService();
            }
            
            if(type.contains("hipo")==true){
                System.out.println("   >>>> starting hipo service");
                DataDistributionService service = new DataDistributionService();
                service.setDelay(delay);
                service.connectHipo(file);
                service.startServiceHipo();
            }
        }
        
        /*
        if(parser.hasOption("-et")==true){
            DataDistributionService service = new DataDistributionService();
            boolean remote = true;
            if(parser.hasOption("-r")==true){
                if(parser.getOption("-r").stringValue().contains("false")==true){
                    remote = false;
                }
            }
            service.connect(parser.getOption("-et").stringValue(),remote);
            service.startService();
        }*/               
    }
}
