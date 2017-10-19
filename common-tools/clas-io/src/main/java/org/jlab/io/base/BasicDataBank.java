/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.io.base;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author gavalian
 */
public class BasicDataBank implements DataBank {
    /**
     * Defining the containers to hold bank data.
     */
    private Boolean  isUniLength = true;
    private Map<String,short[]>   shortContainer   = new LinkedHashMap<String,short[]>();
    private Map<String,int[]>     intContainer     = new LinkedHashMap<String,int[]>();
    private Map<String,float[]>   floatContainer   = new LinkedHashMap<String,float[]>();
    private Map<String,double[]>  doubleContainer  = new LinkedHashMap<String,double[]>();
    private Map<String,byte[]>    byteContainer    = new LinkedHashMap<String,byte[]>();
    private Map<String,long[]>    longContainer    = new LinkedHashMap<String,long[]>();
    private DataDescriptor            bankDescriptor;
    
    public BasicDataBank(DataDescriptor desc){
        bankDescriptor = desc;
    }
    
    public BasicDataBank(){
        //bankDescriptor = desc;
    }
    
    private void printWarningColumnExists(String routine,String column_name){
        System.err.println("[BasicDataBank::"+ routine + ": " + 
                this.getDescriptor().getName() +
                "]---> warning :: the variable with name "
                + column_name + " already exists.");
    }
    
    private void printWarningColumnDoesNotExist(String routine,String column_name){
        System.err.println("[BasicDataBank::"+ routine + ": " + 
                this.getDescriptor().getName() + "]---> warning :: the variable with name "
                + column_name + " does not exist exists.");
    }
    
    private void printOutOfBoundsWarning(String routine, String column_name, int index, int length){
        System.err.println("[BasicDataBank::"+ routine +"]---> warning :: requested row "
                + index + " of column "
        + column_name + " with length = " + length);
    
    }
    
    
    public String[] getColumnList() {
        return null;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public DataDescriptor getDescriptor() {
        return bankDescriptor;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    /**
     * Returns a double array for given column name.
     * @param path name of the column
     * @return double array
     */
    
    public double[] getDouble(String path) {
        if(doubleContainer.containsKey(path)==true){
            return doubleContainer.get(path);
        }
        return new double[0];
    }
    /**
     * Adds given array to the doubles container. If the array with given
     * column name already exists it will not be replaced.
     * @param path column name
     * @param arr array of doubles to add to the container. 
     */
    
    public void setDouble(String path, double[] arr) {
        if(doubleContainer.containsKey(path)==true){
            this.printWarningColumnExists("setDouble", path);
        } else {
            doubleContainer.put(path, arr);
        }
    }
    /**
     * Change the value for individual entry for given column. Out of bounds
     * check is performed.
     * @param path column name
     * @param row column element
     * @param value new value to set
     */
    
    public void setDouble(String path, int row, double value) {
        if(doubleContainer.containsKey(path)==false){
            this.printWarningColumnDoesNotExist("setDouble", path);
            return;
        }
        
        if(row>=0 && row<doubleContainer.get(path).length){
            doubleContainer.get(path)[row] = value;
        } else {
            this.printOutOfBoundsWarning("setDouble", path, row,
                    doubleContainer.get(path).length);
        }
    }
    /**
     * Not implemented.
     * @param path
     * @param arr 
     */
    
    public void appendDouble(String path, double[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    /**
     * Returns a float array for given column name.
     * @param path
     * @return array of floats
     */
    
    public float[] getFloat(String path) {
        if(floatContainer.containsKey(path)==true){
            return floatContainer.get(path);
        }
        return new float[0];
    }
    /**
     * Adds given array to the floats container. If the array with given
     * column name already exists it will not be replaced.
     * @param path
     * @param arr 
     */
    
    public void setFloat(String path, float[] arr) {
        if(floatContainer.containsKey(path)==true){
            this.printWarningColumnExists("setDouble", path);
        } else {
            floatContainer.put(path, arr);
        }
    }
    /**
     * Change the value for individual entry for given column. Out of bounds
     * check is performed.
     * @param path
     * @param row
     * @param value 
     */
    
    public void setFloat(String path, int row, float value) {
        if(floatContainer.containsKey(path)==false){
            this.printWarningColumnDoesNotExist("setDouble", path);
            return;
        }
        
        if(row>=0 && row<floatContainer.get(path).length){
            floatContainer.get(path)[row] = value;
        } else {
            this.printOutOfBoundsWarning("setDouble", path, row,
                    floatContainer.get(path).length);
        }
    }
    /**
     * Not implemented.
     * @param path
     * @param arr 
     */
    
    public void appendFloat(String path, float[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    public int[] getInt(String path) {
        if(intContainer.containsKey(path)==true){
            return intContainer.get(path);
        }
        return new int[0];
    }
    
    
    public void setInt(String path, int[] arr) {
        if(intContainer.containsKey(path)==true){
            this.printWarningColumnExists("setInt", path);
        } else {
            intContainer.put(path, arr);
        }
    }
    
    
    public void setInt(String path, int row, int value) {
        if(intContainer.containsKey(path)==false){
            this.printWarningColumnDoesNotExist("setInt", path);
            return;
        }
        
        if(row>=0 && row<intContainer.get(path).length){
            intContainer.get(path)[row] = value;
        } else {
            this.printOutOfBoundsWarning("setDouble", path, row,
                    intContainer.get(path).length);
        }        
    }
    /**
     * Not implemented.
     * @param path
     * @param arr 
     */
    
    public void appendInt(String path, int[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public short[] getShort(String path) {
        if(shortContainer.containsKey(path)==true){
            return shortContainer.get(path);
        }
        return new short[0];
    }

    
    public void setShort(String path, short[] arr) {
        if(shortContainer.containsKey(path)==true){
            this.printWarningColumnExists("setShort", path);
        } else {
            shortContainer.put(path, arr);
        }
    }

    
    public void setShort(String path, int row, short value) {
        if(shortContainer.containsKey(path)==false){
            this.printWarningColumnDoesNotExist("setShort", path);
            return;
        }
        
        if(row>=0 && row<shortContainer.get(path).length){
            shortContainer.get(path)[row] = value;
        } else {
            this.printOutOfBoundsWarning("setShort", path, row,
                    shortContainer.get(path).length);
        }
    }

    
    public void appendShort(String path, short[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public byte[] getByte(String path) {
        if(byteContainer.containsKey(path)==true){
            return byteContainer.get(path);
        }
        return new byte[0];        
    }

    
    public void setByte(String path, byte[] arr) {
        if(byteContainer.containsKey(path)==true){
            this.printWarningColumnExists("setByte", path);
        } else {
            byteContainer.put(path, arr);
        }
    }

    
    public void setByte(String path, int row, byte value) {
        if(byteContainer.containsKey(path)==false){
            this.printWarningColumnDoesNotExist("setByte", path);
            return;
        }
        
        if(row>=0 && row<byteContainer.get(path).length){
            byteContainer.get(path)[row] = value;
        } else {
            this.printOutOfBoundsWarning("setShort", path, row,
                    byteContainer.get(path).length);
        }
    }

    
    public void appendByte(String path, byte[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public long[] getLong(String path) {
        if(longContainer.containsKey(path)==true){
            return longContainer.get(path);
        }
        return new long[0];
    }

    public void setLong(String path, long[] arr) {
        if(longContainer.containsKey(path)==true){
            this.printWarningColumnExists("setLong", path);
        } else {
            longContainer.put(path, arr);
        }
    }

    public void setLong(String path, int row, long value) {
        if(longContainer.containsKey(path)==false){
            this.printWarningColumnDoesNotExist("setLong", path);
            return;
        }
        
        if(row>=0 && row<longContainer.get(path).length){
            longContainer.get(path)[row] = value;
        } else {
            this.printOutOfBoundsWarning("setLong", path, row,
                    longContainer.get(path).length);
        }
    }

    public void appendLong(String path, long[] arr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public int columns() {
        int ic = 0;
        ic += shortContainer.size();
        ic += byteContainer.size();
        ic += intContainer.size();
        ic += floatContainer.size();
        ic += doubleContainer.size();
        ic += longContainer.size();
        return ic;
    }

    
    public int rows() {
        int nrows = 0;
        for(Map.Entry<String,float[]> item : floatContainer.entrySet()){
            float[] itemdata = item.getValue();
            if(itemdata!=null)
                nrows = itemdata.length;           
        }
        for(Map.Entry<String,int[]> item : intContainer.entrySet()){
            int[] itemdata = item.getValue();
            if(itemdata!=null)
                nrows = itemdata.length;
        }
        for(Map.Entry<String,double[]> item : doubleContainer.entrySet()){
            double[] itemdata = item.getValue();
            if(itemdata!=null)
                nrows = itemdata.length;
        }
        return nrows;
    }

    
    public void show() {
        int size = this.columns();
        String bankname = "undefined";
        if(this.bankDescriptor!=null){
            bankname = this.bankDescriptor.getName();
        }
        System.out.println("*****>>>>> BANK " + bankname 
                + "  >>>> SIZE = " + size);
        
        for(Map.Entry<String,byte[]> item : byteContainer.entrySet()){
            System.out.print(String.format("%14s (byte)  : ",item.getKey()));
            byte[] itemdata = item.getValue();
            if(itemdata!=null){
                for(int loop = 0; loop < itemdata.length;loop++) 
                    System.out.print(String.format(" %12d  ", itemdata[loop]));             
            }
            System.out.println();
        }
        
        for(Map.Entry<String,short[]> item : shortContainer.entrySet()){
            System.out.print(String.format("%14s (short) : ",item.getKey()));
            short[] itemdata = item.getValue();
            for(int loop = 0; loop < itemdata.length;loop++) 
                System.out.print(String.format(" %12d  ", itemdata[loop]));
            System.out.println();
        }
       
        for(Map.Entry<String,int[]> item : intContainer.entrySet()){
            System.out.print(String.format("%14s  (int)  : ",item.getKey()));
            int[] itemdata = item.getValue();
            for(int loop = 0; loop < itemdata.length;loop++) 
                System.out.print(String.format(" %12d  ", itemdata[loop]));
            System.out.println();
        }
        for(Map.Entry<String,float[]> item : floatContainer.entrySet()){
            System.out.print(String.format("%14s  (float) : ",item.getKey()));
            float[] itemdata = item.getValue();
            for(int loop = 0; loop < itemdata.length;loop++) 
                System.out.print(String.format(" %12.5f  ", itemdata[loop]));
            System.out.println();
        }
        for(Map.Entry<String,double[]> item : doubleContainer.entrySet()){
            System.out.print(String.format("%14s (double) : ",item.getKey()));
            double[] itemdata = item.getValue();
            for(int loop = 0; loop < itemdata.length;loop++) 
                System.out.print(String.format(" %12.5f  ", itemdata[loop]));
            System.out.println();
        }
        for(Map.Entry<String,long[]> item : longContainer.entrySet()){
            System.out.print(String.format("%14s (long) : ",item.getKey()));
            long[] itemdata = item.getValue();
            for(int loop = 0; loop < itemdata.length;loop++) 
                System.out.print(String.format(" %12.5f  ", itemdata[loop]));
            System.out.println();
        }
    }

    
    public void reset() {
        shortContainer.clear();
        byteContainer.clear();
        intContainer.clear();
        floatContainer.clear();
        doubleContainer.clear();
        longContainer.clear();
    }

    
    public void allocate(int rows) {
        System.err.println("[BasicDataBank]---> allocate is not implemented..");
    }

    
    public double getDouble(String path, int index) {
        if(doubleContainer.containsKey(path)==true){
            if(doubleContainer.get(path).length<=index){
                System.err.println("[BasicDataBank::getDouble] ERROR : variable " +
                        this.getDescriptor().getName() + "." + path + 
                        " array length is " + doubleContainer.get(path).length
                + " requested index="+index);
                return 0.0;
            } else {
                return doubleContainer.get(path)[index];
            }
        }
        return 0.0;
    }

    
    public float getFloat(String path, int index) {
        if(floatContainer.containsKey(path)==true){
            if(floatContainer.get(path).length<=index){
                System.err.println("[BasicDataBank::getFloat] ERROR : variable " +
                        this.getDescriptor().getName() + "." + path + 
                        " array length is " + floatContainer.get(path).length
                + " requested index="+index);
                return (float) 0.0;
            } else {
                return floatContainer.get(path)[index];
            }
        }
        return (float) 0.0;
    }

    
    public int getInt(String path, int index) {
        if(intContainer.containsKey(path)==true){
            if(intContainer.get(path).length<=index){
                System.err.println("[BasicDataBank::getInt] ERROR : variable " +
                        this.getDescriptor().getName() + "." + path + 
                        " array length is " + intContainer.get(path).length
                + " requested index="+index);
                return 0;
            } else {
                return intContainer.get(path)[index];
            }
        }
        return 0;
    }

    
    public long getLong(String path, int index) {
        if(longContainer.containsKey(path)==true){
            if(longContainer.get(path).length<=index){
                System.err.println("[BasicDataBank::getLong] ERROR : variable " +
                        this.getDescriptor().getName() + "." + path + 
                        " array length is " + longContainer.get(path).length
                + " requested index="+index);
                return 0;
            } else {
                return longContainer.get(path)[index];
            }
        }
        return 0;
    }

    public short getShort(String path, int index) {
        if(shortContainer.containsKey(path)==true){
            if(shortContainer.get(path).length<=index){
                System.err.println("[BasicDataBank::getShort] ERROR : variable " +
                        this.getDescriptor().getName() + "." + path + 
                        " array length is " + shortContainer.get(path).length
                + " requested index="+index);
                return 0;
            } else {
                return shortContainer.get(path)[index];
            }
        }
        return 0;
    }

    
    public byte getByte(String path, int index) {
         if(byteContainer.containsKey(path)==true){
            if(byteContainer.get(path).length<=index){
                System.err.println("[BasicDataBank::getByte] ERROR : variable " +
                        this.getDescriptor().getName() + "." + path + 
                        " array length is " + byteContainer.get(path).length
                + " requested index="+index);
                return 0;
            } else {
                return byteContainer.get(path)[index];
            }
        }
        return 0;
    }

    public TableModel getTableModel(String mask) {
        
        String[] tokens = mask.split(":");
        Set<String>  entryMask = new HashSet<String>();
        for(String item : tokens){
            entryMask.add(item);
        }
        
        String[] columns = this.bankDescriptor.getEntryList();
        int nrows = this.rows();
        
        Object[][] objects = new Object[nrows][columns.length];
        for(int loop = 0; loop < columns.length; loop++){
            if(entryMask.contains(columns[loop])==true||entryMask.size()<2){
                for(int row = 0; row < nrows; row++){                    
                    if(this.byteContainer.containsKey(columns[loop])==true){
                        objects[row][loop] = new Byte(this.getByte(columns[loop], row));
                    }
                    if(this.shortContainer.containsKey(columns[loop])==true){
                        objects[row][loop] = new Short(this.getShort(columns[loop], row));
                    }
                    if(this.intContainer.containsKey(columns[loop])==true){
                        objects[row][loop] = new Integer(this.getInt(columns[loop], row));
                    }
                    if(this.floatContainer.containsKey(columns[loop])==true){
                        //objects[row][loop] = new Float(this.getFloat(columns[loop], row));
                        objects[row][loop] = String.format("%12.5f", this.getFloat(columns[loop], row));
                    }
                    if(this.doubleContainer.containsKey(columns[loop])==true){
                        objects[row][loop] = String.format("%12.5f",this.getDouble(columns[loop], row));
                    }
                    if(this.longContainer.containsKey(columns[loop])==true){
                        objects[row][loop] = new Long(this.getLong(columns[loop], row));
                    }
                }
            }
        }
        return new DefaultTableModel(objects,columns);
    }
    
}
