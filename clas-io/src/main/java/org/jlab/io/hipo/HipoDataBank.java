/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.hipo;

import java.util.Map;
import javax.swing.table.TableModel;
import org.jlab.hipo.data.HipoGroup;
import org.jlab.hipo.data.HipoNode;
import org.jlab.hipo.schema.Schema;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataDescriptor;

/**
 *
 * @author gavalian
 */
public class HipoDataBank implements DataBank  {
    
    private HipoDataDescriptor descriptor = null;
    private HipoGroup          hipoGroup  = null;
    
    public HipoDataBank(HipoDataDescriptor desc, int size){        
        Map<Integer,HipoNode>  nodes = desc.getSchema().createNodeMap(size);
        this.descriptor = desc;
        hipoGroup = new HipoGroup(nodes,this.descriptor.getSchema());
    }
    
    public HipoDataBank(Map<Integer,HipoNode> nodes, Schema desc){
        descriptor = new HipoDataDescriptor();
        descriptor.init(desc);
        hipoGroup = new HipoGroup(nodes,this.descriptor.getSchema());
    }
    
    public HipoDataBank(HipoGroup group){
        this.hipoGroup = group;
        descriptor = new HipoDataDescriptor();
        descriptor.init(this.hipoGroup.getSchema());
    }
    
    public HipoGroup getGroup(){
        return this.hipoGroup;
    }
    
    public String[] getColumnList() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public DataDescriptor getDescriptor() {
        return this.descriptor;
    }

    public double[] getDouble(String path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

    public byte[] getByte(String path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
