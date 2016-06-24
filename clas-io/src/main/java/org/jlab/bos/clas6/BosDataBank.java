/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.bos.clas6;

import java.util.HashMap;
import java.util.Map;
import javax.swing.table.TableModel;
import org.jlab.data.io.DataBank;
import org.jlab.data.io.DataDescriptor;

/**
 *
 * @author gavalian
 */
public class BosDataBank implements DataBank {
    
    private BosDataDescriptor bankDescriptor = null;
    private HashMap<String,short[]> shortContainer = new HashMap<String,short[]>();
    private HashMap<String,int[]> intContainer = new HashMap<String,int[]>();
    private HashMap<String,float[]> floatContainer = new HashMap<String,float[]>();
    
    public BosDataBank(BosDataDescriptor d){
        bankDescriptor = d;
    }
    @Override
    public String[] getColumnList() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DataDescriptor getDescriptor() {
        return this.bankDescriptor;
    }

    @Override
    public double[] getDouble(String path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDouble(String path, double[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void appendDouble(String path, double[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public float[] getFloat(String path) {
        return floatContainer.get(path);
    }

    @Override
    public void setFloat(String path, float[] arr) {
        floatContainer.put(path, arr);
    }

    @Override
    public void appendFloat(String path, float[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int[] getInt(String path) {
        return intContainer.get(path);
    }

    @Override
    public void setInt(String path, int[] arr) {
        intContainer.put(path, arr);
    }

    @Override
    public void appendInt(String path, int[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public short[] getShort(String path) {
        return shortContainer.get(path);
    }

    @Override
    public void setShort(String path, short[] arr) {
        shortContainer.put(path, arr);
    }

    @Override
    public void appendShort(String path, short[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void show() {
        int size = shortContainer.size() + intContainer.size() + floatContainer.size();
        System.out.println("*****>>>>> BANK " + this.bankDescriptor.getName() 
                + "  >>>> SIZE = " + size);
        
        for(Map.Entry<String,short[]> item : shortContainer.entrySet()){
            System.out.print(String.format("%14s (short) : ",item.getKey()));
            short[] itemdata = item.getValue();
            for(int loop = 0; loop < itemdata.length;loop++) 
                System.out.print(String.format(" %12.3f  ", itemdata[loop]));
            System.out.println();
        }
       
        for(Map.Entry<String,int[]> item : intContainer.entrySet()){
            System.out.print(String.format("%14s ( int ) : ",item.getKey()));
            int[] itemdata = item.getValue();
            for(int loop = 0; loop < itemdata.length;loop++) 
                System.out.print(String.format(" %12d  ", itemdata[loop]));
            System.out.println();
        }
        for(Map.Entry<String,float[]> item : floatContainer.entrySet()){
            System.out.print(String.format("%14s (float) : ",item.getKey()));
            float[] itemdata = item.getValue();
            for(int loop = 0; loop < itemdata.length;loop++) 
                System.out.print(String.format(" %12.5f  ", itemdata[loop]));
            System.out.println();
        }
        
    }

    @Override
    public int columns() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int rows() {
        int nrows = 0;
        for(Map.Entry<String,float[]> item : floatContainer.entrySet()){
            float[] itemdata = item.getValue();
            nrows = itemdata.length;
        }
        /*
        for(Map.Entry<String,short[]> item : shortContainer.entrySet()){
            short[] itemdata = item.getValue();
            nrows = itemdata.length;
        }*/
        
        return nrows;
    }

    @Override
    public byte[] getByte(String path) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setByte(String path, byte[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void appendByte(String path, byte[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDouble(String path, int row, double value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setFloat(String path, int row, float value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setInt(String path, int row, int value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setShort(String path, int row, short value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setByte(String path, int row, byte value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void allocate(int rows) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getDouble(String path, int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public float getFloat(String path, int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getInt(String path, int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public short getShort(String path, int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte getByte(String path, int index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public TableModel getTableModel(String mask) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
