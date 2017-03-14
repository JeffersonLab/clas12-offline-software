/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.io.evio;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.jlab.coda.jevio.DataType;
import org.jlab.coda.jevio.EventBuilder;
import org.jlab.coda.jevio.EvioBank;
import org.jlab.coda.jevio.EvioEvent;
import org.jlab.coda.jevio.EvioException;
import org.jlab.io.base.DataDescriptor;
import org.jlab.io.base.DataEntryType;
import org.jlab.utils.CLASResources;
import org.xml.sax.SAXException;

/**
 *
 * @author gavalian
 */
public class EvioFactory {
    
    private final static EvioDataDictionary factoryDict = EvioFactory.readDefaultDictionary();
                
    public static EvioDataDictionary readDefaultDictionary(){

        EvioDataDictionary dict = new EvioDataDictionary();
        //return dict;
        String clasDictionaryPath = CLASResources.getResourcePath("etc/bankdefs/clas12");
        if(clasDictionaryPath!=null){
            dict.initWithDir(clasDictionaryPath);
        }
        return dict;
    }
    
    public static void resetDictionary(){
        EvioFactory.factoryDict.clear();
    }
    
    public static EvioDataDictionary getDictionary(){
        return factoryDict;
    }
    
    public static EvioDataDictionary createDictionary(String path){
        EvioDataDictionary dict = new EvioDataDictionary(path);
        return dict;
    }
    
    public static void loadDictionary(String path){
        factoryDict.initWithDir(path);
    }
    
    public static EvioDataBank createBank(String name, int rows){
        EvioDataDescriptor desc = (EvioDataDescriptor) EvioFactory.getDictionary().getDescriptor(name);
        if(desc==null){
            System.err.println("[EvioFactory::createBank]---> ERROR. bank with name " +
                    name + " does not exist in descriptor list.");
            return null;
        }
        EvioDataBank bank = new EvioDataBank(desc);
        bank.allocate(rows);
        return bank;
    }
    
    public static void loadDictionary(){
        //factoryDict = EvioFactory.readDefaultDictionary();
    }
    
    public static EvioDataBank createEvioBank(String name){
        EvioDataDescriptor desc = (EvioDataDescriptor) EvioFactory.getDictionary().getDescriptor(name);
        return new EvioDataBank(desc);
    }
    
    public static EvioDataBank createEvioBank(String name,int rows){
        EvioDataDescriptor desc = (EvioDataDescriptor) EvioFactory.getDictionary().getDescriptor(name);
        if(desc==null){
            System.err.println("-----------> error descriptor is null...");
            return null;
        }
        EvioDataBank bank = new EvioDataBank(desc);
        String[] entries = desc.getEntryList();
        for(String entry : entries){
            int type_id = desc.getProperty("type", entry);
            if(DataEntryType.getType(type_id)==DataEntryType.INTEGER){
                bank.setInt(entry, new int[rows]);
            }
            if(DataEntryType.getType(type_id)==DataEntryType.DOUBLE){
                bank.setDouble(entry, new double[rows]);
            }
            if(DataEntryType.getType(type_id)==DataEntryType.BYTE){
                bank.setByte(entry, new byte[rows]);
            }
            if(DataEntryType.getType(type_id)==DataEntryType.FLOAT){
                bank.setFloat(entry, new float[rows]);
            }
            if(DataEntryType.getType(type_id)==DataEntryType.SHORT){
                bank.setShort(entry, new short[rows]);
            }
        }
        return bank;
    }
    
    public static EvioDataEvent  createEvioEvent(){
        try {
            //EvioEvent baseBank = new EvioEvent(1, DataType.BANK, 0);
            EventBuilder builder = new EventBuilder(1,DataType.BANK,0);
            EvioEvent event = builder.getEvent();
            EvioBank baseBank = new EvioBank(10, DataType.ALSOBANK, 0);
            
            builder.addChild(event, baseBank);
            
            ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
            
            int byteSize = event.getTotalBytes();
            //System.out.println("base bank size = " + byteSize);
            ByteBuffer bb = ByteBuffer.allocate(byteSize);
            bb.order(byteOrder);
            event.write(bb);
            bb.flip();
         
            return new EvioDataEvent(bb,EvioFactory.getDictionary());
        } catch (EvioException ex) {
            Logger.getLogger(EvioDataSync.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static EvioDataBank createEvioBank(String name,DataDescriptor desc,int rows){
        //EvioDataDescriptor desc = (EvioDataDescriptor) EvioFactory.getDictionary().getDescriptor(name);
        EvioDataBank bank = new EvioDataBank(desc);
        String[] entries = desc.getEntryList();
        for(String entry : entries){
            int type_id = desc.getProperty("type", entry);
            if(DataEntryType.getType(type_id)==DataEntryType.INTEGER){
                bank.setInt(entry, new int[rows]);
            }
            if(DataEntryType.getType(type_id)==DataEntryType.DOUBLE){
                bank.setDouble(entry, new double[rows]);
            }
            if(DataEntryType.getType(type_id)==DataEntryType.BYTE){
                bank.setByte(entry, new byte[rows]);
            }
            if(DataEntryType.getType(type_id)==DataEntryType.FLOAT){
                bank.setFloat(entry, new float[rows]);
            }
            if(DataEntryType.getType(type_id)==DataEntryType.SHORT){
                bank.setShort(entry, new short[rows]);
            }
        }
        return bank;
    }
}
