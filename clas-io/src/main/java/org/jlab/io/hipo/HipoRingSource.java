/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.hipo;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.jlab.coda.xmsg.core.xMsg;
import org.jlab.coda.xmsg.core.xMsgCallBack;
import org.jlab.coda.xmsg.core.xMsgConstants;
import org.jlab.coda.xmsg.core.xMsgMessage;
import org.jlab.coda.xmsg.core.xMsgTopic;
import org.jlab.coda.xmsg.core.xMsgUtil;
import org.jlab.coda.xmsg.data.xMsgRegInfo;
import org.jlab.coda.xmsg.excp.xMsgException;
import org.jlab.coda.xmsg.net.xMsgProxyAddress;
import org.jlab.coda.xmsg.net.xMsgRegAddress;
import org.jlab.hipo.schema.SchemaFactory;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventList;
import org.jlab.io.base.DataSource;

/**
 *
 * @author gavalian
 */
public class HipoRingSource  extends xMsg implements DataSource {
    
    private List<HipoDataEvent>  eventStore = new ArrayList<HipoDataEvent>();
    private int       eventStoreMaxCapacity = 50;
    private SchemaFactory        dictionary = new SchemaFactory();
    
    public HipoRingSource(String host){
        super("DataSource",
                new xMsgProxyAddress(host, xMsgConstants.DEFAULT_PORT),
                new xMsgRegAddress(host, xMsgConstants.REGISTRAR_PORT),
                2);
                final String domain  = "clas12-domain";
        final String subject = "clas12-data";
        final String type    = "data";
        final String description = "clas12 data distribution ring";
        
        xMsgTopic topic = xMsgTopic.build(domain, subject, type);

        try {
            // Register this subscriber
            register(xMsgRegInfo.subscriber(topic, description));
        } catch (xMsgException ex) {
            Logger.getLogger(HipoRingSource.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            // Subscribe to default proxy
            subscribe(topic, new MyCallBack());
        } catch (xMsgException ex) {
            Logger.getLogger(HipoRingSource.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.printf("Subscribed to = %s%n", topic);
        
        this.dictionary.initFromDirectory("CLAS12DIR", "etc/bankdefs/hipo");
    }
    
    public HipoRingSource(){
        
        super("DataSource",1);
        
        final String domain  = "clas12-domain";
        final String subject = "clas12-data";
        final String type    = "data";
        final String description = "clas12 data distribution ring";
        
        xMsgTopic topic = xMsgTopic.build(domain, subject, type);

        try {
            // Register this subscriber
            register(xMsgRegInfo.subscriber(topic, description));
        } catch (xMsgException ex) {
            Logger.getLogger(HipoRingSource.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            // Subscribe to default proxy
            subscribe(topic, new MyCallBack());
        } catch (xMsgException ex) {
            Logger.getLogger(HipoRingSource.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.printf("Subscribed to = %s%n", topic);
    }
    
    public static HipoRingSource createSource(){
        String s = (String)JOptionPane.showInputDialog(
                    null,
                    "Complete the sentence:\n"
                    + "\"Green eggs and...\"",
                    "Customized Dialog",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "0.0.0.0");
        if(s!=null){
            System.out.println("----> connecting to host : " + s);
            HipoRingSource source = new HipoRingSource(s);
            System.out.println("\n\n");
            System.out.println("   |---->  caching ");
            for(int i = 0; i < 5; i++){
                try {
                    Thread.sleep(1000);
                    System.out.println("   |---->  caching ");
                } catch (InterruptedException ex) {
                    Logger.getLogger(HipoRingSource.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return source;            
        } 
        
        return null;
    }
    
    @Override
    public boolean hasEvent() {
        return (eventStore.size()>0);
    }

    @Override
    public void open(File file) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void open(String filename) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void open(ByteBuffer buff) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getSize() {
        return this.eventStore.size();
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
        System.out.println("   >>> get next event : size = " + eventStore.size());
        if(eventStore.isEmpty()){
            return null;
        }
        HipoDataEvent event = this.eventStore.get(0);
        System.out.println("   >>> success getting event : size = " + eventStore.size());
        event.show();

        this.eventStore.remove(0);
        System.out.println("   >>>   FILO cleanup : size = " + eventStore.size());
        System.out.println("\n\n");
        return event;
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
        this.eventStore.clear();
    }

    @Override
    public int getCurrentIndex() {
        return 0;
    }
    
    
    public void close() {
        try {
            this.getConnection().close();
        } catch (xMsgException ex) {
            Logger.getLogger(HipoRingSource.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private class MyCallBack implements xMsgCallBack {

        @Override
        public void callback(xMsgMessage mm) {
            byte[] data = mm.getData();
            String type = mm.getMimeType();
            //System.out.println("\n\n     >>>>>> received data : mime " + type);
            //System.out.println("     >>>>>> received data : size " + data.length);
            if(eventStore.size()<eventStoreMaxCapacity){
                HipoDataEvent event = new HipoDataEvent(data,dictionary);
                eventStore.add(event);
                //System.out.printf("     >>>>>> adding event to the store : size = %d \n", eventStore.size());
            } else {
                //System.out.printf("     >>>>>> event store is full : size = %d \n", eventStore.size());
            }
        }
    }
    
    public static void main(String[] args){
        HipoRingSource reader = HipoRingSource.createSource();
        
        while(reader.hasEvent()==true){
            
            HipoDataEvent  event = (HipoDataEvent) reader.getNextEvent();
            //event.show();
            
            try {
                Thread.sleep(8000);
            } catch (InterruptedException ex) {
                Logger.getLogger(HipoRingSource.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        System.out.println("DONE");
        /*
        String host = args[0];
        try (HipoRingSource subscriber = new HipoRingSource(host)) {
            xMsgUtil.keepAlive();
        } */   
    }
}
