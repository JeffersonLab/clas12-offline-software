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
    private int decoderDelay        = 5;
    
    public DataDistributionService(){
        decoder = new CLASDecoder();
    }
    
    public void connect(String file, boolean remote){
        dataSource = new EvioETSource("localhost");
        ( (EvioETSource) dataSource).setRemote(remote);
        dataSource.open(file);        
    }
    
    public void connect(String file){
        dataSource = new EvioSource();
        dataSource.open(file);
    }
    
    public void setDelay(int delay){
        this.decoderDelay = delay;
    }
    
    public void startService(){
        
        ProgressPrintout progress = new ProgressPrintout();
        ringProducer = new DataDistributionRing();
        ringProducer.initProxy();
        ringProducer.initRegistrar();
        ringProducer.initRing();
        ringProducer.setDelay(decoderDelay);
        
        while(true){
            DataEvent event = dataSource.getNextEvent();
            if(event!=null){
                HipoDataEvent decodedEvent = (HipoDataEvent) decoder.getDataEvent(event);
                ringProducer.addEvent(decodedEvent);
                progress.updateStatus();
            } else {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    Logger.getLogger(DataDistributionService.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println(" error there are no events");
            }
        }
    }
    
    public static void main(String[] args){
        OptionParser  parser = new OptionParser();
        
        parser.addOption("-et","");
        parser.addOption("-d","5");
        parser.addRequired("-file");
        parser.addOption("-r","false");
        
        parser.parse(args);
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
        
        if(parser.hasOption("-file")==true){
            int delay = parser.getOption("-d").intValue();
            DataDistributionService service = new DataDistributionService();
            service.setDelay(delay);
            service.connect(parser.getOption("-file").stringValue());
            service.startService();
        }
    }
}
