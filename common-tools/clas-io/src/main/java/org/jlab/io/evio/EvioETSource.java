/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.evio;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.coda.et.EtAttachment;
import org.jlab.coda.et.EtConstants;
import org.jlab.coda.et.EtEvent;
import org.jlab.coda.et.EtStation;
import org.jlab.coda.et.EtStationConfig;
import org.jlab.coda.et.EtSystem;
import org.jlab.coda.et.EtSystemOpenConfig;
import org.jlab.coda.et.enums.Mode;
import org.jlab.coda.et.enums.Modify;
import org.jlab.coda.et.exception.EtBusyException;
import org.jlab.coda.et.exception.EtClosedException;
import org.jlab.coda.et.exception.EtDeadException;
import org.jlab.coda.et.exception.EtEmptyException;
import org.jlab.coda.et.exception.EtException;
import org.jlab.coda.et.exception.EtExistsException;
import org.jlab.coda.et.exception.EtTimeoutException;
import org.jlab.coda.et.exception.EtTooManyException;
import org.jlab.coda.et.exception.EtWakeUpException;
import org.jlab.coda.jevio.EvioCompactReader;
import org.jlab.coda.jevio.EvioException;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventList;
import org.jlab.io.base.DataSource;
import org.jlab.io.base.DataSourceType;
import org.jlab.utils.options.OptionParser;


/**
 *
 * @author gavalian
 */

public class EvioETSource implements DataSource {
    

    private Boolean  connectionOK = false;
    private String   etRingHost   = "localhost";
    private Integer  etRingPort   = 11111;
    private String   etStation    = "reader_station";
    
    private EtSystem sys = null;
    private EtAttachment  myAttachment = null;
    private Boolean       remoteConnection = false;
    private Integer       MAX_NEVENTS = 20;
    private int           currentEventPosition = 0;
    
    List<EvioDataEvent>   readerEvents     = new ArrayList<EvioDataEvent>();

    public EvioETSource(){
        this.etRingPort = EtConstants.serverPort;
        this.setRemote(true);
    }
    
    public EvioETSource(String host){
        this.etRingHost = host;
        this.etRingPort = EtConstants.serverPort;
        this.setRemote(true);
    }
    
    public EvioETSource(String host, int port){
        this.etRingHost = host;
        this.etRingPort = port;
        this.setRemote(true);
    }
    
    public EvioETSource(String host, int port, String station){
        this.etRingHost = host;
        this.etRingPort = port;
        this.etStation  = station;
        this.setRemote(true);
    }
    
    public EvioETSource(String host, String station){
        this.etRingHost = host;
        this.etRingPort = EtConstants.serverPort;
        this.etStation  = station;
        this.setRemote(true);
    }
    
    public EvioETSource(String host, String station, int max_events){
        this.etRingHost  = host;
        this.etRingPort  = EtConstants.serverPort;
        this.etStation   = station;
        this.MAX_NEVENTS = max_events;
        this.setRemote(true);
    }
    
    public final void setRemote(Boolean flag){
        this.remoteConnection = flag;
    }
    
    @Override
    public boolean hasEvent() {
        return (this.currentEventPosition<this.readerEvents.size());
    }

    @Override
    public void open(File file) {
        
    }
    
    @Override
    public void open(String filename) {
        System.out.println("[ETSOURCE] -->>> connecting to host : [" +
                this.etRingHost + "]  FILE [" + filename + "]  PORT [" + 
                this.etRingPort + "]");
        System.out.println("[ETSOURCE] -->>> connecting remotely : " + this.remoteConnection);
        try {
            this.connectionOK = true;
            String etFile = filename;
            
            EtSystemOpenConfig config = new EtSystemOpenConfig( etFile,this.etRingHost,this.etRingPort);
            if(this.remoteConnection==true){
                config.setConnectRemotely(true);
            }
            //config.setConnectRemotely(true);
            //System.out.println("-------------->>>>> CONNECTING REMOTELY");
            
            //System.out.println("-------------->>>>> CONNECTING LOCALY");
            
            sys = new EtSystem(config);
            sys.setDebug(EtConstants.debugInfo);
            sys.open();
            
            EtStationConfig statConfig = new EtStationConfig();
            //statConfig.setBlockMode(EtConstants.stationBlocking);
            statConfig.setBlockMode(EtConstants.stationNonBlocking);
            
            statConfig.setUserMode(EtConstants.stationUserMulti);
            statConfig.setRestoreMode(EtConstants.stationRestoreOut);
            //EtStation station = sys.createStation(statConfig, "GRAND_CENTRAL");
            EtStation station = sys.createStation(statConfig, this.etStation);
            
            myAttachment = sys.attach(station);
            
            //this.loadEvents();
            //sys.detach(myAttachment);
            //System.out.println("[ET-RING] ----> opened a stream with events # = " + this.readerEvents.size());
            
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
        } catch (EtExistsException ex) {
            Logger.getLogger(EvioETSource.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void clearEvents(){
        this.readerEvents.clear();
        this.currentEventPosition = 0;
    }
    
    public void loadEvents(){
        this.readerEvents.clear();
        this.currentEventPosition = 0;
        if(this.connectionOK == false){
            System.out.println("[EvioETSource] ---->  connection was not estabilished...");
        }
        
        if(sys.alive()==true){
            try {
                
                EtEvent[] events = sys.getEvents(myAttachment, Mode.SLEEP, null, 0, this.MAX_NEVENTS);                
                
                if(events!=null){
                    
                    if(events.length>0){
                        
                        for(int nevent = 0; nevent < events.length; nevent++){
                            ByteBuffer buffer = events[nevent].getDataBuffer();
                            byte[] data = events[nevent].getData();                        
                            byte[]  array = buffer.array();
                            int length = events[nevent].getLength();                                              
                            ByteBuffer  evioBuffer = ByteBuffer.allocate(events[nevent].getLength());
                            evioBuffer.put(data, 0, length);
                            evioBuffer.order(buffer.order());                                                    
                            //EvioReader reader;
                            //System.out.println("------> parsing event # " + nevent + 
                            //        " width length = " + length);
                            try {
                                //reader = new EvioReader(buffer);
                                EvioCompactReader reader = new EvioCompactReader(buffer);
                                //EvioEvent  event = reader.parseNextEvent();
                                /*List<BaseStructure>  nodes = event.getChildrenList();
                                System.out.println("----> rawbytes size = " + event.getRawBytes().length);
                                for(BaseStructure base : nodes){
                                    System.out.println("----> event # " + nevent + " " + base);
                                }*/
                                ByteBuffer  localBuffer = reader.getEventBuffer(1);
                                /*
                                EvioDataEvent dataEvent = new EvioDataEvent(
                                        event.getRawBytes(),reader.getByteOrder(),
                                        EvioFactory.getDictionary());
                                        */
                                //System.out.println("---> compact event buffer size = " + localBuffer.capacity());
                                EvioDataEvent dataEvent = new EvioDataEvent(localBuffer,EvioFactory.getDictionary());
                                this.readerEvents.add(dataEvent);                                
                            } catch (EvioException ex) {
                                System.out.println("*** ERROR *** : problem reading event # " + nevent );
                                //Logger.getLogger(EvioETSource.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                    sys.putEvents(myAttachment, events);                    
                }
            } catch (EtException ex) {
                Logger.getLogger(EvioETSource.class.getName()).log(Level.SEVERE, null, ex);
            } catch (EtDeadException ex) {
                Logger.getLogger(EvioETSource.class.getName()).log(Level.SEVERE, null, ex);
            } catch (EtClosedException ex) {
                Logger.getLogger(EvioETSource.class.getName()).log(Level.SEVERE, null, ex);
            } catch (EtEmptyException ex) {
                Logger.getLogger(EvioETSource.class.getName()).log(Level.SEVERE, null, ex);
            } catch (EtBusyException ex) {
                Logger.getLogger(EvioETSource.class.getName()).log(Level.SEVERE, null, ex);
            } catch (EtTimeoutException ex) {
                //Logger.getLogger(EvioETSource.class.getName()).log(Level.SEVERE, null, ex);
            } catch (EtWakeUpException ex) {
                Logger.getLogger(EvioETSource.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(EvioETSource.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    @Override
    public void open(ByteBuffer buff) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getSize() {
        if(sys.alive()==true){
            return this.readerEvents.size();
        }
        return 0;
    }

    @Override
    public DataEventList getEventList(int start, int stop) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DataEventList getEventList(int nrecords) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public void putEvents(){
        
    }
    
    public DataEvent getNextEvent() {
        if(this.currentEventPosition<this.readerEvents.size()){
            EvioDataEvent evt = this.readerEvents.get(this.currentEventPosition);
            this.currentEventPosition++;
            return evt;
        }
        return null;
    }

    @Override
    public DataEvent getPreviousEvent() {
        return null;
    }

    @Override
    public DataEvent gotoEvent(int index) {
        return null;
    }

    @Override
    public void reset() {
        
    }

    @Override
    public int getCurrentIndex() {
        return this.currentEventPosition;
    }
    
    public static void main(String[] args){
        
        
        OptionParser parser = new OptionParser("et-debug");
        parser.addRequired("-host", "Host to connect to");
        parser.addRequired("-f", "File name to connect to");
        parser.addOption("-s", "reader_station", "station name to be created in the ET ring");
        parser.addOption("-n", "30", "number of events to pull each time");
        parser.addOption("-show", "false", "if set to true this will show the first event.");
        
        
        System.out.println("[ET] PARSING ARGUMENTS");
        parser.parse(args);
        
        System.out.println("[ET] STARTING DEBUG");
        
        String host       = parser.getOption("-host").stringValue();
        String station    = parser.getOption("-s").stringValue();
        String file       = parser.getOption("-f").stringValue();
        String show       = parser.getOption("-show").stringValue();
        int    max_events = parser.getOption("-n").intValue();
        
        EvioETSource reader = new EvioETSource(host,station,max_events);
        reader.open(file);
        
        reader.loadEvents();
        int cycle = 0;        
        while(true){
            int iteration = 0;

            while(reader.hasEvent()==false){
                reader.loadEvents();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    Logger.getLogger(EvioETSource.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("[ET] READING ET RING ITERATION # " + iteration);
                iteration++;
            }
            System.out.println("[ET] >> LOADED EVENTS FROM ET RING. COUNT = " + reader.getSize());
            System.out.println("[ET] >> DON'T WORRY EVERYTHING IS FINE. JUST SLEEPING FOR A WHILE. CYCLE # "
                    + cycle + "\n");
            for(int k = 0; k < reader.getSize(); k++){
                DataEvent event = reader.getNextEvent();
                if(show.compareTo("true")==0&&k==0){
                    event.show();
                }
            }
            cycle++;
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(EvioETSource.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        /*
        String ethost = "129.57.76.215";
        String file   = "/tmp/myEtRing";
        Boolean isRemote = false;
        
        if(args.length>1){
            ethost = args[0];
            file   = args[1];
        } else {
            System.out.println("\n\n\nUsage : et-connect host etfile\n");
            System.exit(0);            
        }
        
        if(args.length>2){
            String flag = args[2];
            if(flag.compareTo("true")==0){
                isRemote = true;
            }
        }
        
        EvioETSource reader = new EvioETSource(ethost);
        reader.setRemote(isRemote);
        reader.open(file);
        //for(int loop = 0 ; loop < 10 ; loop++){
        int counter = 0;
        EvioEventDecoder  decoder = new EvioEventDecoder();
        while(reader.hasEvent()){  
            counter++;
            System.out.println("Reading next event # " + counter);
            EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
            if(event!=null) {
                System.out.println("------> received an event");
                event.getHandler().list();
                List<DetectorRawData> rawdata = decoder.getDataEntries(event);
                for(DetectorRawData data : rawdata){
                    System.out.println(data);
                }
            }
        }*/
        //}
    }

    @Override
    public DataSourceType getType() {
        return DataSourceType.STREAM;
    }

    @Override
    public void waitForEvents() {
        try {
            this.loadEvents();
        } catch (Exception e){
            System.out.println("\n   >>>>> [evioETsource] error loading events\n");
        }
    }
}
          
