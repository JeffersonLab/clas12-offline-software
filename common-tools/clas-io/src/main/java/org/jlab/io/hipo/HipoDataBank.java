/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.io.hipo;

import javax.swing.table.TableModel;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataDescriptor;
import org.jlab.jnp.hipo4.data.Bank;


/**
 *
 * @author gavalian
 */
public class HipoDataBank implements DataBank  {
    
    private HipoDataDescriptor descriptor = null;
    private Bank               hipoGroup  = null;
    
    public HipoDataBank(Bank bank){
        hipoGroup = bank;
        descriptor = new HipoDataDescriptor(bank.getSchema());
    }
    
    public HipoDataBank(HipoDataDescriptor desc, int size){        
        hipoGroup = new Bank( desc.getSchema(),size);
        //Map<Integer,HipoNode>  nodes = desc.getSchema();
        this.descriptor = desc;
        //hipoGroup = new HipoGroup(nodes,this.descriptor.getSchema());
    }
    /*
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
    */
    
    public Bank getBank(){
        return hipoGroup;
    }
    
    /*public HipoGroup getGroup(){
        return this.hipoGroup;
    }*/
    
    public String[] getColumnList() {

        String[] columns = new String[descriptor.getSchema().getElements()];
        for(int i = 0; i < columns.length; i++) columns[i] = descriptor.getSchema().getElementName(i);
        return columns;
    }

    @Override
    public DataDescriptor getDescriptor() {
        return this.descriptor;
    }

    public double[] getDouble(String path) {
        int    nrows = this.hipoGroup.getRows();
        double[] result = new double[nrows];
        for(int i = 0; i < nrows; i++) result[i] = hipoGroup.getDouble(path, i);
        return result;
        //return this.hipoGroup.getNode(path).getDouble();
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getDouble(String path, int index) {
        return this.hipoGroup.getDouble(path, index);
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setDouble(String path, double[] arr) {
        //this.getNode(path).setDouble(index, value);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setDouble(String path, int row, double value) {
        hipoGroup.putDouble(path,row,value);
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void appendDouble(String path, double[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public float[] getFloat(String path) {
        int    nrows = this.hipoGroup.getRows();
        float[] result = new float[nrows];
        for(int i = 0; i < nrows; i++) result[i] = hipoGroup.getFloat(path, i);
        return result;
        //return this.hipoGroup.getNode(path).getFloat();
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public float getFloat(String path, int index) {
        return this.hipoGroup.getFloat(path, index);
    }

    public void setFloat(String path, float[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setFloat(String path, int row, float value) {
        this.hipoGroup.putFloat(path, row, value);
    }

    public void appendFloat(String path, float[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int[] getInt(String path) {
        int    nrows = this.hipoGroup.getRows();
        int[] result = new int[nrows];
        for(int i = 0; i < nrows; i++) result[i] = hipoGroup.getInt(path, i);
        return result;
        //return this.getGroup().getNode(path).getInt();
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int getInt(String path, int index) {
        return hipoGroup.getInt(path, index);
    }

    public void setInt(String path, int[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setInt(String path, int row, int value) {
        hipoGroup.putInt(path, row, value);
    }

    public void appendInt(String path, int[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public short[] getShort(String path) {
        int    nrows = this.hipoGroup.getRows();
        short[] result = new short[nrows];
        for(int i = 0; i < nrows; i++) result[i] = hipoGroup.getShort(path, i);
        return result;
        //return this.hipoGroup.getNode(path).getShort();
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public short getShort(String path, int index) {
        return hipoGroup.getShort(path, index);        
    }

    public void setShort(String path, short[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setShort(String path, int row, short value) {
        hipoGroup.putShort(path, row, value);
    }

    public void appendShort(String path, short[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public long[] getLong(String path) {
        int    nrows = this.hipoGroup.getRows();
        long[] result = new long[nrows];
        for(int i = 0; i < nrows; i++) result[i] = hipoGroup.getLong(path, i);
        return result;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public long getLong(String path, int index) {
        return hipoGroup.getLong(path, index);        
    }

    public void setLong(String path, long[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setLong(String path, int row, long value) {
        hipoGroup.putLong(path, row, value);
    }

    public void appendLong(String path, long[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public byte[] getByte(String path) {
        int    nrows = this.hipoGroup.getRows();
        byte[] result = new byte[nrows];
        for(int i = 0; i < nrows; i++) result[i] = hipoGroup.getByte(path, i);
        return result;
        //return this.hipoGroup.getNode(path).getByte();
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public byte getByte(String path, int index) {
        return hipoGroup.getByte(path, index);
    }

    public void setByte(String path, byte[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setByte(String path, int row, byte value) {
        hipoGroup.putByte(path, row, value);
    }

    public void appendByte(String path, byte[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int columns() {
        return hipoGroup.getSchema().getElements();
    }

    public int rows() {
        return hipoGroup.getRows();
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
