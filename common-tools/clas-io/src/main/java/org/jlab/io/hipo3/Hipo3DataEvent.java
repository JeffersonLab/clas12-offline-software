/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.hipo3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataDictionary;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.jnp.hipo.data.HipoEvent;
import org.jlab.jnp.hipo.data.HipoGroup;
import org.jlab.jnp.hipo.data.HipoNode;
import org.jlab.jnp.hipo.schema.Schema;
import org.jlab.jnp.hipo.schema.Schema.SchemaEntry;
import org.jlab.jnp.hipo.schema.SchemaFactory;

/**
 *
 * @author gavalian
 */
public class Hipo3DataEvent implements DataEvent {
    
    private HipoEvent hipoEvent = null;
    private DataEventType eventType = DataEventType.EVENT_ACCUMULATE;
    
    public Hipo3DataEvent(byte[] array, SchemaFactory factory){
        hipoEvent = new HipoEvent(array,factory);
    }
    
    public Hipo3DataEvent(HipoEvent event){
        this.hipoEvent = event;
    }
    
    public HipoEvent  getHipoEvent(){return this.hipoEvent;}
    
    public void initDictionary(SchemaFactory factory){
        this.hipoEvent.getSchemaFactory().copy(factory);
    }
    
    @Override
    public String[] getBankList() {
        List<Schema> schemaList = hipoEvent.getSchemaFactory().getSchemaList();
        List<String> existingBanks = new ArrayList<String>();
        for(Schema schema : schemaList){
            int group = schema.getGroup();
            if(hipoEvent.hasGroup(group)==true){
                existingBanks.add(schema.getName());
            }
        }
        
        String[] list = new String[existingBanks.size()];
        for(int i = 0; i < list.length; i++) list[i] = existingBanks.get(i);
        return list;
    }

    public String[] getColumnList(String bank_name) {
       List<String> columnsList = this.hipoEvent.getSchemaFactory().getSchema(bank_name).schemaEntryList();       
        String[] columns = new String[columnsList.size()];
        for(int i = 0; i < columns.length; i++) columns[i] = columnsList.get(i);
        return columns;
    }

    public DataDictionary getDictionary() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public ByteBuffer getEventBuffer() {
        ByteBuffer buffer = ByteBuffer.wrap(this.hipoEvent.getDataBuffer());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer;
    }

    public void appendBank(DataBank bank) {
        if(bank==null) return;
        if(bank instanceof Hipo3DataBank){
            HipoGroup group =  ((Hipo3DataBank) bank).getGroup();
            hipoEvent.writeGroup(group);
        }
    }

    public void appendBanks(DataBank... bank) {
        for(DataBank item : bank){
            this.appendBank(item);
        }
    }

    @Override
    public boolean hasBank(String name) {
        return (this.hipoEvent.hasGroup(name));
    }

    @Override
    public DataBank getBank(String bank_name) {
        if(this.hipoEvent.getSchemaFactory().hasSchema(bank_name)==true){
            //Schema schema = this.hipoEvent.getSchemaFactory().getSchema(bank_name);
            //Map<Integer,HipoNode> map = this.hipoEvent.getGroup(schema.getGroup());
            HipoGroup group = hipoEvent.getGroup(bank_name);
            Hipo3DataBank bank = new Hipo3DataBank(group.getNodesMap(),group.getSchema());
            return bank;
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
        HipoNode node = this.getHipoNodeByPath(path);        
        if(node==null){
            System.out.println("\n>>>>> error : getting node failed : " + path);
            return new double[0];
        }
        return node.getDouble();
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setDouble(String path, double[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void appendDouble(String path, double[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public float[] getFloat(String path) {
        HipoNode node = this.getHipoNodeByPath(path);        
        if(node==null){
            System.out.println("\n>>>>> error : getting node failed : " + path);
            return new float[0];
        }
        return node.getFloat();
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setFloat(String path, float[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void appendFloat(String path, float[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int[] getInt(String path) {
        HipoNode node = this.getHipoNodeByPath(path);        
        if(node==null){
            System.out.println("\n>>>>> error : getting node failed : " + path);
            return new int[0];
        }
        return node.getInt();
    }

    public void setInt(String path, int[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void appendInt(String path, int[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public short[] getShort(String path) {
        HipoNode node = this.getHipoNodeByPath(path);        
        if(node==null){
            System.out.println("\n>>>>> error : getting node failed : " + path);
            return new short[0];
        }
        return node.getShort();
    }

    public void setShort(String path, short[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void appendShort(String path, short[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public HipoNode getHipoNodeByPath(String path){
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
    }
    
    public byte[] getByte(String path) {
        HipoNode node = this.getHipoNodeByPath(path);        
        if(node==null){
            System.out.println("\n>>>>> error : getting node failed : " + path);
            return new byte[0];
        }
        return node.getByte();
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
        this.hipoEvent.show();
    }
    
    public void showBankByOrder(int order){
        this.hipoEvent.showGroupByOrder(order);
    }
    
    @Override
    public DataBank createBank(String bank_name, int rows) {
        if(this.hipoEvent.getSchemaFactory().hasSchema(bank_name)==false){
            System.out.println(">>>>> error :  descriptor not found : " + bank_name);
            System.out.println(">>>>> error : number of descriptors : " + 
                    hipoEvent.getSchemaFactory().getSchemaList().size());
            System.out.println();
            this.hipoEvent.getSchemaFactory().show();
            return null;
        }
        HipoGroup group = this.hipoEvent.getSchemaFactory().getSchema(bank_name).createGroup(rows);
        Hipo3DataBank bank = new Hipo3DataBank(group);
        return bank;
    }

    @Override
    public void removeBank(String bankName) {
        this.hipoEvent.removeGroup(bankName);
    }

    @Override
    public void removeBanks(String... bankNames) {
        
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long[] getLong(String path) {
        HipoNode node = this.getHipoNodeByPath(path);        
        if(node==null){
            System.out.println("\n>>>>> error : getting node failed : " + path);
            return new long[0];
        }
        int size = node.getDataSize();
        long[] data = new long[size];
        for(int i =0; i < data.length; i++) data[i] = node.getLong(i);
        return data;
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
