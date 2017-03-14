/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.evio;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.coda.xmsg.core.xMsg;
import org.jlab.coda.xmsg.core.xMsgCallBack;
import org.jlab.coda.xmsg.core.xMsgConstants;
import org.jlab.coda.xmsg.core.xMsgMessage;
import org.jlab.coda.xmsg.core.xMsgTopic;
import org.jlab.coda.xmsg.data.xMsgRegInfo;
import org.jlab.coda.xmsg.excp.xMsgException;
import org.jlab.coda.xmsg.net.xMsgProxyAddress;
import org.jlab.coda.xmsg.net.xMsgRegAddress;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventList;
import org.jlab.io.base.DataSource;
import org.jlab.io.base.DataSourceType;
import org.jlab.io.hipo.HipoRingSource;
import org.jlab.utils.options.OptionParser;

/**
 *
 * @author gavalian
 */
public class EvioRingSource implements DataSource {

    private List<EvioDataEvent>  eventStore = new ArrayList<EvioDataEvent>();
    private int       eventStoreMaxCapacity = 500;
    private xMsg                 xmsgServer = null;
    
    
    private boolean createConnection(String host){

        boolean result = true;
        this.xmsgServer = new xMsg("EvioDataSource",
                new xMsgProxyAddress(host, xMsgConstants.DEFAULT_PORT),
                new xMsgRegAddress(host, xMsgConstants.REGISTRAR_PORT),
                2);
        try {
            if(this.xmsgServer.getConnection()!=null){
                System.out.println("   >>> connection to server " + host + " : success");
            } 

        } catch (xMsgException ex) {
            //Logger.getLogger(HipoRingSource.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("   >>> connection to server " + host + " : failed");
            this.xmsgServer.destroy();
            this.xmsgServer = null;
            result = false;
        }
        //System.out.println("-----> connection estabilished...");
        return result;
    }
    
    @Override
    public boolean hasEvent() {
        return eventStore.size()>0;
    }

    @Override
    public void open(File file) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void open(String filename) {
        String[] hostList = filename.split(":");
        
        for(String host : hostList){
            boolean result = this.createConnection(host);
            if(result==true) break;
        }
        
        if(this.xmsgServer==null){
            System.out.println("----> error finding server.");
            return;
        }
        System.out.println("   >>> subscribing  to topic : data-evio");
        
        final String subject = "clas12data";
        final String type    = "data-evio";
        final String description = "clas12 data distribution ring";
        final String domain  = "clas12domain";
        xMsgTopic topic = xMsgTopic.build(domain, subject, type);
        try {
            // Register this subscriber
            this.xmsgServer.register(xMsgRegInfo.subscriber(topic, description));
        } catch (xMsgException ex) {
            Logger.getLogger(EvioRingSource.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            // Subscribe to default proxy
            this.xmsgServer.subscribe(topic, new EvioRingSource.MyCallBack());
        } catch (xMsgException ex) {
            Logger.getLogger(EvioRingSource.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("   >>> subscription to topic : success\n\n");
        
    }

    @Override
    public void open(ByteBuffer buff) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getSize() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DataEventList getEventList(int start, int stop) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DataEventList getEventList(int nrecords) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DataEvent getNextEvent() {
        if(eventStore.isEmpty()){
            return null;
        }
        EvioDataEvent event = this.eventStore.get(0);
        //System.out.println("   >>> success getting event : size = " + eventStore.size());
        //event.show();
        this.eventStore.remove(0);
        //System.out.println("   >>>   FILO cleanup : size = " + eventStore.size());
        //System.out.println("\n\n");
        return event;
    }

    @Override
    public DataEvent getPreviousEvent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DataEvent gotoEvent(int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getCurrentIndex() {
        return 0;
    }

    @Override
    public DataSourceType getType() {
        return DataSourceType.STREAM;
        //        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void waitForEvents() {
        // For the Ring source, waiting for the events        
    }
    
    
    private class MyCallBack implements xMsgCallBack {

        @Override
        public void callback(xMsgMessage mm) {
            
            byte[] data = mm.getData();            
            String type = mm.getMimeType();
            
            System.out.println("\n\n     >>>>>> received data : mime " + type);
            System.out.println("     >>>>>> received data : size " + data.length);
            if(eventStore.size()<eventStoreMaxCapacity){
                EvioDataEvent event = new EvioDataEvent(data,ByteOrder.BIG_ENDIAN);
                eventStore.add(event);
                //System.out.printf("     >>>>>> adding event to the store : size = %d \n", eventStore.size());
            } else {
                //System.out.printf("     >>>>>> event store is full : size = %d \n", eventStore.size());
            }
        }
    }
    
    public static void main(String[] args){
        
        OptionParser parser = new OptionParser();
        parser.addOption("-s", "localhost");
        parser.parse(args);
        
        EvioRingSource reader = new EvioRingSource();
        reader.open(parser.getOption("-s").stringValue());
        //reader.open("128.82.188.90:129.57.76.220:129.57.76.215:129.57.76.230");
        
        while(true){
            if(reader.hasEvent()==true){
                //System.out.println("has event");
                DataEvent event = reader.getNextEvent();
                try {
                    event.show();
                } catch (Exception e) {
                    System.out.println("something went wrong");
                }
            } else {
                //System.out.println("no event");
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ex) {
                    Logger.getLogger(HipoRingSource.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
