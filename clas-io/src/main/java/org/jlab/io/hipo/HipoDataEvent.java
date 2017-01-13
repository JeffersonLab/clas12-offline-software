/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.hipo;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jlab.hipo.data.HipoEvent;
import org.jlab.hipo.data.HipoGroup;
import org.jlab.hipo.data.HipoNode;
import org.jlab.hipo.schema.Schema;
import org.jlab.hipo.schema.SchemaFactory;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataDictionary;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;

/**
 *
 * @author gavalian
 */
public class HipoDataEvent implements DataEvent {
    
    private HipoEvent hipoEvent = null;
    private DataEventType eventType = DataEventType.EVENT_ACCUMULATE;
    
    public HipoDataEvent(byte[] array, SchemaFactory factory){
        hipoEvent = new HipoEvent(array,factory);
    }
    
    public HipoDataEvent(HipoEvent event){
        this.hipoEvent = event;
    }
    
    public HipoEvent  getHipoEvent(){return this.hipoEvent;}
    
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        if(bank instanceof HipoDataBank){
            HipoGroup group =  ((HipoDataBank) bank).getGroup();
            this.hipoEvent.addNodes(group.getNodes());
        }
    }

    public void appendBanks(DataBank... bank) {
        for(DataBank item : bank){
            this.appendBank(item);
        }
    }

    public boolean hasBank(String name) {
        return (this.hipoEvent.getGroup(name)!=null);
    }

    public DataBank getBank(String bank_name) {
        if(this.hipoEvent.getSchemaFactory().hasSchema(bank_name)==true){
            Schema schema = this.hipoEvent.getSchemaFactory().getSchema(bank_name);
            Map<Integer,HipoNode> map = this.hipoEvent.getGroup(schema.getGroup());
            HipoDataBank bank = new HipoDataBank(map,schema);
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setDouble(String path, double[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void appendDouble(String path, double[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public float[] getFloat(String path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setFloat(String path, float[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void appendFloat(String path, float[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int[] getInt(String path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setInt(String path, int[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void appendInt(String path, int[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public short[] getShort(String path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setShort(String path, short[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void appendShort(String path, short[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public byte[] getByte(String path) {
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
        this.hipoEvent.show();
    }
    
    public void showBankByOrder(int order){
        this.hipoEvent.showGroupByOrder(order);
    }
    
    public DataBank createBank(String bank_name, int rows) {
        if(this.hipoEvent.getSchemaFactory().hasSchema(bank_name)==false){
            System.out.println(">>>>> error : descriptor not found : " + bank_name);
            //this.hipoEvent.getSchemaFactory().show();
            return null;
        }
        HipoGroup group = this.hipoEvent.getSchemaFactory().getSchema(bank_name).createGroup(rows);
        HipoDataBank bank = new HipoDataBank(group);
        return bank;
    }
}
