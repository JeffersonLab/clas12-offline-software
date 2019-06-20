/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.hipo;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataDictionary;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.SchemaFactory;
import org.jlab.jnp.hipo4.data.Schema;

/**
 *
 * @author gavalian
 */
public class HipoDataEvent implements DataEvent {
    
    private Event hipoEvent = null;
    private SchemaFactory schemaFactory = null;
    
    private DataEventType eventType = DataEventType.EVENT_ACCUMULATE;
    
    public HipoDataEvent(byte[] array, SchemaFactory factory){
        hipoEvent = new Event(array.length);
        hipoEvent.initFrom(array);
        schemaFactory = factory;
    }
    
    public HipoDataEvent(Event event){
        this.hipoEvent = event;
    }
    
    public HipoDataEvent(Event event,SchemaFactory factory){
        this.hipoEvent = event;
        schemaFactory = factory;
    }
    
    public Event  getHipoEvent(){return this.hipoEvent;}
    
    public void initDictionary(SchemaFactory factory){
        //this.hipoEvent.getSchemaFactory().copy(factory);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public String[] getBankList() {
        List<Schema> schemaList = schemaFactory.getSchemaList();
        List<String> existingBanks = new ArrayList<String>();
        for(Schema schema : schemaList){
            int group = schema.getGroup();
            int item  = schema.getItem();
            if(hipoEvent.scan(group, item)>0){
                existingBanks.add(schema.getName());
            }
        }
        
        String[] list = new String[existingBanks.size()];
        for(int i = 0; i < list.length; i++) list[i] = existingBanks.get(i);
        return list;
    }

    public String[] getColumnList(String bank_name) {
        Schema schema = schemaFactory.getSchema(bank_name);
        int  ncolumns = schemaFactory.getSchema(bank_name).getElements();
        //List<String> columnsList = schemaFactory.getSchema(bank_name).getElements();
        String[] columns = new String[ncolumns];
        for(int i = 0; i < columns.length; i++) columns[i] = schema.getElementName(i);
        return columns;
    }
    
    public void addSchema(Schema schema){
        schemaFactory.addSchema(schema);
    }
    
    public void addSchemaList(List<Schema> schemaList){
        for(Schema schema : schemaList) addSchema(schema);
    }
    
    @Override
    public DataDictionary getDictionary() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ByteBuffer getEventBuffer() {
        //ByteBuffer buffer = ByteBuffer.wrap(this.hipoEvent.getEventBuffer()
        //buffer.order(ByteOrder.LITTLE_ENDIAN);
        return hipoEvent.getEventBuffer();
    }

    @Override
    public void appendBank(DataBank bank) {
        if(bank==null) return;
        if(bank instanceof HipoDataBank){
            Bank group =  ((HipoDataBank) bank).getBank();
            hipoEvent.write(group);
        }
    }

    public void appendBanks(DataBank... bank) {
        for(DataBank item : bank){
            this.appendBank(item);
        }
    }

    @Override
    public boolean hasBank(String name) {
        Schema schema = schemaFactory.getSchema(name);
        if(schema==null) return false;
        return hipoEvent.scan(schema.getGroup(),schema.getItem())>0;
    }

    @Override
    public DataBank getBank(String bank_name) {
        
        Schema schema = schemaFactory.getSchema(bank_name);
        
        if(schema!=null){            
            Bank bank = new Bank(schema);            
            hipoEvent.read(bank);
            HipoDataBank dataBank = new HipoDataBank(bank);
            return dataBank;
        }        
        //HipoDataBank bank = new HipoDataBank();
        return null;
    }

    public void getBank(String bank_name, DataBank bank) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setProperty(String property, String value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String getProperty(String property) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public double[] getDouble(String path) {
        /*HipoNode node = this.getHipoNodeByPath(path);        
        if(node==null){
            System.out.println("\n>>>>> error : getting node failed : " + path);
            return new double[0];
        }
        return node.getDouble();*/
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setDouble(String path, double[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void appendDouble(String path, double[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public float[] getFloat(String path) {
       /* HipoNode node = this.getHipoNodeByPath(path);        
        if(node==null){
            System.out.println("\n>>>>> error : getting node failed : " + path);
            return new float[0];
        }
        return node.getFloat();*/
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setFloat(String path, float[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void appendFloat(String path, float[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int[] getInt(String path) {
        /*HipoNode node = this.getHipoNodeByPath(path);        
        if(node==null){
            System.out.println("\n>>>>> error : getting node failed : " + path);
            return new int[0];
        }
        return node.getInt();*/
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setInt(String path, int[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void appendInt(String path, int[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public short[] getShort(String path) {
        /*HipoNode node = this.getHipoNodeByPath(path);        
        if(node==null){
            System.out.println("\n>>>>> error : getting node failed : " + path);
            return new short[0];
        }
        return node.getShort();*/
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setShort(String path, short[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void appendShort(String path, short[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

 /*   public HipoNode getHipoNodeByPath(String path){
        String[] bank_and_item = path.split("[.]+");
        if(bank_and_item.length<2){
            System.out.println("\n>>>>> error : syntax error in path name : " + path);
            return null;
        }
        Schema schema = this.hipoEvent.getSchemaFactory().getSchema(bank_and_item[0]);
        if(schema==null){
            System.out.println("\n>>>>> error : can not find schema with name : " 
                    + bank_and_item[0]);
            return null;
        }
        
        SchemaEntry entry = schema.getEntry(bank_and_item[1]);
        if(entry==null){
            System.out.println("\n>>>>> error : schema  " + bank_and_item[0] +
                    " dose not have an entry with name :"
                    + bank_and_item[1]);
            return null;
        }
        
        HipoNode node = hipoEvent.getNode(schema.getGroup(), entry.getId());
        return node;
    }*/
    
    public byte[] getByte(String path) {
        
        /*HipoNode node = this.getHipoNodeByPath(path);        
        if(node==null){
            System.out.println("\n>>>>> error : getting node failed : " + path);
            return new byte[0];
        }
        return node.getByte();*/
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setByte(String path, byte[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void appendByte(String path, byte[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setType(DataEventType type) {
        this.eventType = type;
    }

    public DataEventType getType() {
        return this.eventType;
    }
    
    public void show(){
        //this.hipoEvent.show();
        this.hipoEvent.scan();
    }
    
    public void showBankByOrder(int order){
       // this.hipoEvent.showGroupByOrder(order);
    }
    
    @Override
    public DataBank createBank(String bank_name, int rows) {
        
        //System.out.println(" CREATING BANK = " + bank_name);
        
        Schema schema = schemaFactory.getSchema(bank_name);
        if(schema ==null) {
            System.out.println(" SCHEMA FOR ["+bank_name + "] = NULL");
            List<String>  scList = schemaFactory.getSchemaKeys();
            System.out.println(" SCHEMA FACTORY SIZE = " + scList.size());
            Collections.sort(scList);
            for(String sc : scList){
                System.out.println("\t ----> " + sc);
            }
        }
        //System.out.println("SCHEMA = " + schema.getName());
        
        Bank   bank   = new Bank(schema,rows);
        
        HipoDataBank dataBank = new HipoDataBank(bank);
        return dataBank;
        /*
        if(this.hipoEvent.getSchemaFactory().hasSchema(bank_name)==false){
            System.out.println(">>>>> error :  descriptor not found : " + bank_name);
            System.out.println(">>>>> error : number of descriptors : " + 
                    hipoEvent.getSchemaFactory().getSchemaList().size());
            System.out.println();
            this.hipoEvent.getSchemaFactory().show();
            return null;
        }
        HipoGroup group = this.hipoEvent.getSchemaFactory().getSchema(bank_name).createGroup(rows);
        HipoDataBank bank = new HipoDataBank(group);
        return bank;*/
    }

    @Override
    public void removeBank(String bankName) {
        if(schemaFactory.hasSchema(bankName)==true){
            hipoEvent.remove(schemaFactory.getSchema(bankName));
        }
        //this.hipoEvent.removeGroup(bankName);
    }

    @Override
    public void removeBanks(String... bankNames) {
        for(String bank : bankNames){
            removeBank(bank);
        }
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long[] getLong(String path) {
       /* HipoNode node = this.getHipoNodeByPath(path);        
        if(node==null){
            System.out.println("\n>>>>> error : getting node failed : " + path);
            return new long[0];
        }
        int size = node.getDataSize();
        long[] data = new long[size];
        for(int i =0; i < data.length; i++) data[i] = node.getLong(i);
        return data;*/
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setLong(String path, long[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void appendLong(String path, long[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
