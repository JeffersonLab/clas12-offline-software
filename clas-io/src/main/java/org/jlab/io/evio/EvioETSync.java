/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.evio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.coda.et.EtAttachment;
import org.jlab.coda.et.EtConstants;
import org.jlab.coda.et.EtEvent;
import org.jlab.coda.et.EtEventImpl;
import org.jlab.coda.et.EtStation;
import org.jlab.coda.et.EtStationConfig;
import org.jlab.coda.et.EtSystem;
import org.jlab.coda.et.EtSystemOpenConfig;
import org.jlab.coda.et.enums.Mode;
import org.jlab.coda.et.exception.EtBusyException;
import org.jlab.coda.et.exception.EtClosedException;
import org.jlab.coda.et.exception.EtDeadException;
import org.jlab.coda.et.exception.EtEmptyException;
import org.jlab.coda.et.exception.EtException;
import org.jlab.coda.et.exception.EtExistsException;
import org.jlab.coda.et.exception.EtTimeoutException;
import org.jlab.coda.et.exception.EtTooManyException;
import org.jlab.coda.et.exception.EtWakeUpException;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataSync;

/**
 *
 * @author gavalian
 */
public class EvioETSync implements DataSync {

     private Boolean  connectionOK = false;
    private String   etRingHost   = "";
    private Integer  etRingPort   = 11111;
    private EtSystem sys = null;
    private EtAttachment  myAttachment = null;
    
    
    public EvioETSync(String ip){
        this.etRingHost = ip;
    }
    
    public void open(String filename) {
        try {
            this.connectionOK = true;
            String etFile = filename;
            
            EtSystemOpenConfig config = new EtSystemOpenConfig( etFile,this.etRingHost,this.etRingPort);
            sys = new EtSystem(config);
            sys.open();
            
            EtStationConfig statConfig = new EtStationConfig();
            statConfig.setBlockMode(EtConstants.stationBlocking);
            statConfig.setUserMode(EtConstants.stationUserSingle);
            statConfig.setRestoreMode(EtConstants.stationRestoreOut);
            
            EtStation gsStation = sys.stationNameToObject("GRAND_CENTRAL");
            
            //EtStation station = sys.createStation(statConfig, "my_station");
            //EtStation etst = new EtStation();
            //EtStation station = sys.attach();
            myAttachment = sys.attach(gsStation);
            
        } catch (EtException ex) {
            this.connectionOK = false;
            ex.printStackTrace();
        } catch (IOException ex) {
            this.connectionOK = false;
            Logger.getLogger(EvioETSource.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EtTooManyException ex) {
            this.connectionOK = false;
            Logger.getLogger(EvioETSource.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EtDeadException ex) {
            Logger.getLogger(EvioETSource.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EtClosedException ex) {
            Logger.getLogger(EvioETSource.class.getName()).log(Level.SEVERE, null, ex);
        } 
    
    }

    public void writeEvent(DataEvent event) {
        EvioDataEvent  evioEvent = (EvioDataEvent) event;
        
        //byte[]  buffer = evioEvent.getHandler().getStructure().getByteBuffer().array();
        //EtEventImpl  etEvent = new EtEventImpl(buffer.length);
        //etEvent.setData(buffer);
        //etEvent.setOwner(1);
        //EtEvent[]  evs = new EtEvent[]{etEvent};
        byte[] buffer = new byte[200];
        int eventLength = buffer.length;
        
        System.out.println("WRITING DATA WITH LENGTH " + buffer.length);
         try {
             
             EtEvent[]  etevents = sys.newEvents(myAttachment, Mode.SLEEP, false, 
                     0,1,buffer.length, 1);
             
             ByteBuffer byteBuffer = etevents[0].getDataBuffer();
             byteBuffer.put(buffer);
             byteBuffer.flip();
             
             etevents[0].setLength(eventLength);
             
             //System.out.println("# of events = " + etevents.length + "  BUFFER SIZE = " + byteBuffer.capacity()
             //+ "  LENGTH = " + etevents[0].getLength());
             
             
             sys.putEvents(myAttachment, etevents);
             
         } catch (IOException ex) {
             Logger.getLogger(EvioETSync.class.getName()).log(Level.SEVERE, null, ex);
         } catch (EtException ex) {
             Logger.getLogger(EvioETSync.class.getName()).log(Level.SEVERE, null, ex);
         } catch (EtDeadException ex) {
             Logger.getLogger(EvioETSync.class.getName()).log(Level.SEVERE, null, ex);
         } catch (EtClosedException ex) {
             Logger.getLogger(EvioETSync.class.getName()).log(Level.SEVERE, null, ex);
         } catch (EtEmptyException ex) {
             Logger.getLogger(EvioETSync.class.getName()).log(Level.SEVERE, null, ex);
         } catch (EtBusyException ex) {
             Logger.getLogger(EvioETSync.class.getName()).log(Level.SEVERE, null, ex);
         } catch (EtTimeoutException ex) {
             Logger.getLogger(EvioETSync.class.getName()).log(Level.SEVERE, null, ex);
         } catch (EtWakeUpException ex) {
             Logger.getLogger(EvioETSync.class.getName()).log(Level.SEVERE, null, ex);
         }
    }

    public void close() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    public static void main(String[] args){
        String ethost = "129.57.76.215";
        String file   = "/tmp/myEtRing";
        String evioFile = "/Users/gavalian/Work/Software/Release-8.0/COATJAVA/etaPXSection_0_recon.evio";
        /*
        if(args.length>1){
            ethost = args[0];
            file   = args[1];
        } else {
            System.out.println("\n\n\nUsage : et-connect host etfile\n");
            System.exit(0);            
        }*/
        
        EvioETSync  writer = new EvioETSync(ethost);
        writer.open(file);
        
        EvioSource reader = new EvioSource();
        reader.open(evioFile);
        int counter = 0;
        while(reader.hasEvent()&&counter<20){
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Logger.getLogger(EvioETSource.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("Reading next event # " + counter);
            counter++;
            EvioDataEvent  event = (EvioDataEvent) reader.getNextEvent();
            writer.writeEvent(event);
        }
    }

    public DataEvent createEvent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
