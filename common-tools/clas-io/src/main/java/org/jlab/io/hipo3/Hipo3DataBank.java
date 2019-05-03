/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.hipo3;

import java.util.List;
import java.util.Map;
import javax.swing.table.TableModel;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataDescriptor;
import org.jlab.jnp.hipo.data.HipoGroup;
import org.jlab.jnp.hipo.data.HipoNode;
import org.jlab.jnp.hipo.schema.Schema;

/**
 *
 * @author gavalian
 */
public class Hipo3DataBank implements DataBank  {
    
    private Hipo3DataDescriptor descriptor = null;
    private HipoGroup          hipoGroup  = null;
    
    public Hipo3DataBank(Hipo3DataDescriptor desc, int size){        
        Map<Integer,HipoNode>  nodes = desc.getSchema().createNodeMap(size);
        this.descriptor = desc;
        hipoGroup = new HipoGroup(nodes,this.descriptor.getSchema());
    }
    
    public Hipo3DataBank(Map<Integer,HipoNode> nodes, Schema desc){
        descriptor = new Hipo3DataDescriptor();
        descriptor.init(desc);
        hipoGroup = new HipoGroup(nodes,this.descriptor.getSchema());
    }
    
    public Hipo3DataBank(HipoGroup group){
        this.hipoGroup = group;
        descriptor = new Hipo3DataDescriptor();
        descriptor.init(this.hipoGroup.getSchema());
    }
    
    public HipoGroup getGroup(){
        return this.hipoGroup;
    }
    
    public String[] getColumnList() {
        List<String> columnsList = this.hipoGroup.getSchema().schemaEntryList();
        String[] columns = new String[columnsList.size()];
        for(int i = 0; i < columns.length; i++) columns[i] = columnsList.get(i);
        return columns;
    }

    public DataDescriptor getDescriptor() {
        return this.descriptor;
    }

    public double[] getDouble(String path) {
        return this.hipoGroup.getNode(path).getDouble();
    }

    public double getDouble(String path, int index) {
        return this.hipoGroup.getNode(path).getDouble(index);
    }

    public void setDouble(String path, double[] arr) {
        //this.getNode(path).setDouble(index, value);
    }

    public void setDouble(String path, int row, double value) {
        this.hipoGroup.getNode(path).setDouble(row, value);
    }

    public void appendDouble(String path, double[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public float[] getFloat(String path) {
        return this.hipoGroup.getNode(path).getFloat();
    }

    public float getFloat(String path, int index) {
        return this.hipoGroup.getNode(path).getFloat(index);
    }

    public void setFloat(String path, float[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setFloat(String path, int row, float value) {
        this.hipoGroup.getNode(path).setFloat(row, value);
    }

    public void appendFloat(String path, float[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int[] getInt(String path) {
        return this.getGroup().getNode(path).getInt();
    }

    public int getInt(String path, int index) {
        return this.hipoGroup.getNode(path).getInt(index);
    }

    public void setInt(String path, int[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setInt(String path, int row, int value) {
        this.hipoGroup.getNode(path).setInt(row, value);
    }

    public void appendInt(String path, int[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public short[] getShort(String path) {
        return this.hipoGroup.getNode(path).getShort();
    }

    public short getShort(String path, int index) {
        return this.hipoGroup.getNode(path).getShort(index);
    }

    public void setShort(String path, short[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setShort(String path, int row, short value) {
        this.hipoGroup.getNode(path).setShort(row, value);
    }

    public void appendShort(String path, short[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public long[] getLong(String path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public long getLong(String path, int index) {
        return this.hipoGroup.getNode(path).getLong(index);
    }

    public void setLong(String path, long[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setLong(String path, int row, long value) {
        this.hipoGroup.getNode(path).setLong(row, value);
    }

    public void appendLong(String path, long[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public byte[] getByte(String path) {
        return this.hipoGroup.getNode(path).getByte();
    }

    public byte getByte(String path, int index) {
        return this.hipoGroup.getNode(path).getByte(index);
    }

    public void setByte(String path, byte[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setByte(String path, int row, byte value) {
        this.hipoGroup.getNode(path).setByte(row, value);
    }

    public void appendByte(String path, byte[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int columns() {
        return this.hipoGroup.getSchema().getEntries();
    }

    public int rows() {
        return this.hipoGroup.getMaxSize();
    }

    public void show() {
        System.out.println(" SHOWING BANK");
        this.hipoGroup.show();
    }

    public void reset() {
        
    }

    public void allocate(int rows) {
        
    }

    public TableModel getTableModel(String mask) {
        return null;
    }
    
}
