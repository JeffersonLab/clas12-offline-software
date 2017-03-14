/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.ring;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.coda.xmsg.core.xMsgConstants;
import org.jlab.coda.xmsg.core.xMsgUtil;
import org.jlab.coda.xmsg.excp.xMsgException;
import org.jlab.coda.xmsg.net.xMsgContext;
import org.jlab.coda.xmsg.net.xMsgProxyAddress;
import org.jlab.coda.xmsg.net.xMsgRegAddress;
import org.jlab.coda.xmsg.sys.xMsgProxy;
import org.jlab.coda.xmsg.sys.xMsgRegistrar;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.io.hipo.HipoDataSource;

/**
 *
 * @author gavalian
 */
public class DataDistributionRing {
    
    private xMsgProxy proxy = null;    
    private xMsgRegistrar registrar = null;
    private DataRingProducer producer = null;
    
    public DataDistributionRing(){
        
    }
    
    /**
     * Initialize proxy on local host.
     */
    public void initProxy(){
        try {
            
            String host = xMsgUtil.localhost();
            xMsgProxyAddress address = new xMsgProxyAddress(host,xMsgConstants.DEFAULT_PORT);            
            System.out.println("\n   >>>>> starting xmsg proxy : " + host + "/" + xMsgConstants.DEFAULT_PORT);
            proxy = new xMsgProxy(xMsgContext.newContext(), address);
            
           /* Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    xMsgContext.destroyContext();
                    proxy.shutdown();
                }
            });*/            
            proxy.start();
            System.out.println("\n   >>>>> starting xmsg proxy : success");
        } catch (xMsgException ex) {
            Logger.getLogger(DataDistributionRing.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setDelay(int ms){
        this.producer.setDelay(ms);
    }
    
    /**
     * initialize registrar service on local host
     */
    public void initRegistrar(){
        try {
            xMsgRegAddress address = new xMsgRegAddress("localhost", xMsgConstants.REGISTRAR_PORT);
            xMsgRegistrar registrar = new xMsgRegistrar(xMsgContext.newContext(), address);
            /* Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    xMsgContext.destroyContext();
                    registrar.shutdown();
                }
            });*/

            registrar.start();
            System.out.println("\n   >>>>> starting xmsg registrar : success");
        } catch (xMsgException ex) {
            Logger.getLogger(DataDistributionRing.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void initRing(){
        producer = new DataRingProducer();
        producer.setDelay(3000);
        producer.start();
        /*
        Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {

                    System.out.println("\n\n\n");
                    System.out.println("   ********* Graceful exit initiated");
                    producer.shutdown();
                    xMsgContext.destroyContext();
                    System.out.println("   ********* Destroying xMsg context : done");
                    registrar.shutdown();
                    System.out.println("   ********* Registrar shudown  : done");
                    proxy.shutdown();
                    System.out.println("   ********* Proxy service  shudown  : done");
                    System.out.println("   ********* Exiting Data Distribution\n\n");
                    System.exit(0);
                }
        });*/
    }
    
    public void shutdown(){
        System.out.println("\n\n\n");
        System.out.println("   ********* Graceful exit initiated");
        producer.shutdown();
        
        System.out.println("   ********* Destroying xMsg context : done");
        
        registrar.shutdown();
        
        System.out.println("   ********* Registrar shudown  : done");
        proxy.shutdown();
        System.out.println("   ********* Proxy service  shudown  : done");
        System.out.println("   ********* Exiting Data Distribution\n\n");
        System.exit(0);
    }
    
    public void addEvent(HipoDataEvent event){
        this.producer.addEvent(event);
    }
    
    public void addEvioEvent(EvioDataEvent event){
        this.producer.addEvioEvent(event);
    }
    
    public static void main(String[] args){
        DataDistributionRing ring = new DataDistributionRing();
        ring.initProxy();
        ring.initRegistrar();
        ring.initRing();
        
        int delay   = Integer.parseInt(args[0]);
        String file = args[1];
        
        ring.setDelay(delay);
        
        while(true){
            HipoDataSource reader = new HipoDataSource();
            reader.open(file);
            while(reader.hasEvent()==true){
                HipoDataEvent event = (HipoDataEvent) reader.getNextEvent();
                ring.addEvent(event);
            }
        }
    }
}
