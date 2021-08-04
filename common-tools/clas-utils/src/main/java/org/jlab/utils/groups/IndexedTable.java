/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.utils.groups;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author gavalian
 */
public class IndexedTable extends DefaultTableModel {
    
    private String tableName        = "IndexedTable";
    private String tableDescription = "Table of indexed values";
    private IndexedList<IndexedEntry> entries    = null;
    private Map<String,Integer>       entryMap   = new LinkedHashMap<String,Integer>();
    private Map<String,String>        entryTypes = new LinkedHashMap<String,String>();
    private List<String>              entryNames = new ArrayList<String>();
    private List<String>              indexNames = new ArrayList<String>();
    private String                    precisionFormat = "%.6f";
    
    private Map<Integer,List<RowConstraint>>  constrains = new HashMap<Integer,List<RowConstraint>>(); 
    
    private int DEBUG_MODE = 0;
    
    public IndexedTable(int indexCount){
        entries = new IndexedList<IndexedEntry>(indexCount);
        for(int i = 0; i < indexCount; i++){
           //this.setIndexName(i, "A"+i);
            this.indexNames.add("A"+i);
        }
    }
    
    public IndexedTable(int indexCount,String format){
        entries = new IndexedList<IndexedEntry>(indexCount);
        for(int i = 0; i < indexCount; i++){
            //this.setIndexName(i, "A"+i);
            this.indexNames.add("A"+i);
        }
        this.parseFormat(format);
    }
    
    public IndexedTable(int indexCount,String[] format){
        entries = new IndexedList<IndexedEntry>(indexCount);
        for(int i = 0; i < indexCount; i++){
           this.indexNames.add("A"+i);
        }
        
        for(int i = 0; i < format.length; i++){
            String[] tokens = format[i].split("/");
            entryMap.put(tokens[0], i);
            entryTypes.put(tokens[0],tokens[1] );
            entryNames.add(tokens[0]);
        }
    
    }
    
    public void setPrecision(Integer precision){
        StringBuilder str = new StringBuilder();
        str.append("%.");
        str.append(precision.toString());
        str.append("f");
        this.precisionFormat = str.toString();
    }
    
    public boolean hasEntry(int... index){
        return this.entries.hasItem(index);
    }
    
    public final void setIndexName(int index, String name){
        indexNames.set(index, name);
    }
    
    public  void addEntry(int... index){
        //System.out.println("adding entry with size = " + entryMap.size());
        this.entries.add(new IndexedEntry(entryMap.size()), index);       
    }
    
    public  void addConstraint(int column, double min, double max){
        if(constrains.containsKey(column)==false){
            constrains.put(column, new ArrayList<RowConstraint>());
        }
        
        //constrains.put(column, new RowConstraint(column,min,max));
        constrains.get(column).add(new RowConstraint(column,min,max));
    }
    
     public  void addConstraint(int column, double min, double max, int condition, int condValue){
        if(constrains.containsKey(column)==false){
            constrains.put(column, new ArrayList<RowConstraint>());
        }
        
        //constrains.put(column, new RowConstraint(column,min,max));
        constrains.get(column).add(new RowConstraint(column,min,max,condition,condValue));
    }
     
    public  void setIntValue(Integer value, String item, int... index){
        if(this.entries.hasItem(index)==false){
            if(DEBUG_MODE>0) System.out.println( "[IndexedTable] ---> error.. entry does not exist");
        } else {
            if(this.entryMap.containsKey(item)==false){
              if(DEBUG_MODE>0) System.out.println( "[IndexedTable] ---> error.. entry does not have item = " + item);
            } else {
                Integer mapIndex = this.entryMap.get(item);
                this.entries.getItem(index).setValue(mapIndex, value);
            }
        }
    }
    
    public  void setDoubleValue(Double value, String item, int... index){
        if(this.entries.hasItem(index)==false){
            if(DEBUG_MODE>0) System.out.println( "[IndexedTable] ---> error.. entry does not exist");
        } else {
            if(this.entryMap.containsKey(item)==false){
               if(DEBUG_MODE>0) System.out.println( "[IndexedTable] ---> error.. entry does not have item = " + item);
            } else {
                Integer mapIndex = this.entryMap.get(item);
                this.entries.getItem(index).setValue(mapIndex, value);
            }
        }
    }
    
    public int  getIntValue(String item, int... index){
        if(this.entries.hasItem(index)==false){
            if(DEBUG_MODE>0) System.out.println( "[IndexedTable] ---> error.. entry does not exist");
        } else {
            if(this.entryMap.containsKey(item)==false){
               if(DEBUG_MODE>0) System.out.println( "[IndexedTable] ---> error.. entry does not have item = " + item);
            } else {
                Integer mapIndex = this.entryMap.get(item);
                return this.entries.getItem(index).getValue(mapIndex).intValue();
            }
        }
        return 0;
    }
    
    public double  getDoubleValue(String item, int... index){
        if(this.entries.hasItem(index)==false){
            if(DEBUG_MODE>0) System.out.println( "[IndexedTable] ---> error.. entry does not exist");
        } else {
            if(this.entryMap.containsKey(item)==false){
                if(DEBUG_MODE>0) System.out.println( "[IndexedTable] ---> error.. entry does not have item = " + item);
            } else {
                Integer mapIndex = this.entryMap.get(item);
                return this.entries.getItem(index).getValue(mapIndex).doubleValue();
            }
        }
        return 0;
    }
    
    public IndexedList getList(){
        return this.entries;
    }
    
    private void parseFormat(String format){
        String[] tokens = format.split(":");
        entryMap.clear();
        entryTypes.clear();
        for(int i = 0; i < tokens.length; i++){
            String[] form = tokens[i].split("/");
            if(form.length==2){
                entryMap.put(form[0], i);
                entryTypes.put(form[0],form[1]);
                entryNames.add(form[0]);
            } else {
                entryMap.put(form[0], i);
                entryTypes.put(form[0], "I");
                entryNames.add(form[0]);
            }
        }
    }
    
    public void addEntryFromString(String[] values){
        int entrySize = entryNames.size() + entries.getIndexSize();
        if(values.length!=entrySize){
            System.out.println("[addEntryFromString] error ---> sizes do not match " 
                    + " ( entry Size = " + entrySize + " ,  array size = " +
                    values.length + " )");
            return;            
        }
        int[] index = new int[entries.getIndexSize()];
        for(int i = 0; i < entries.getIndexSize(); i++){
            index[i] = Integer.parseInt(values[i]);
        }
        
        this.addEntry(index);

        for(int i = 0; i < this.entryNames.size(); i++){
            if(entryTypes.get(entryNames.get(i)).compareTo("D")==0){
                entries.getItem(index).setValue(i, Double.parseDouble(values[i+indexNames.size()]));
            } else {
                entries.getItem(index).setValue(i, Integer.parseInt(values[i+indexNames.size()]));
            }
        }
    }
    
    public void show(){        
        System.out.println(toString());
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("IndexedList SIZE = %d\n", entryMap.size()));
        for(Map.Entry<String,Integer> entry : this.entryMap.entrySet()){
            str.append(String.format("* %-24s * %3s * \n",entry.getKey(),
                    this.entryTypes.get(entry.getKey())));
        }
        return str.toString();
    }
    /**
     * Checks the array of row constraints to see if it passes the cut.
     * this method is used by CellRenderer.
     * @param row
     * @param column
     * @return 
     */
    public boolean isValid(int row, int column){
        if(this.constrains.containsKey(column)==false) return true;
        
        String value  = (String) this.getValueAt(row, column);
        Double dvalue = Double.parseDouble(value);
        for(RowConstraint rc : constrains.get(column)){
            if(rc.conditionColumn()>=0){
                String controlColumn = (String) this.getValueAt(row, rc.conditionColumn());
                Integer intColumnValue = Integer.parseInt(controlColumn);
                if(intColumnValue==rc.conditionColumnValue()){
                    return rc.isValid(dvalue)!=false;
                } 
                //return this.constrains.get(column).isValid(Double.parseDouble(value)) != false;
            } else {
                return rc.isValid(dvalue)!=false;
            }
        }
        return true;
    }
    
    /**
     * Interface classes for Table Model
     * @param col
     * @return 
     */
    @Override
    public String getColumnName(int col) {
        
        if(col>2){
            return this.entryNames.get(col-3);
        }
        return this.indexNames.get(col);
    }
    
    
    
    @Override
    public int getColumnCount(){
        int ncolumns = 0;
        try{
            ncolumns = entries.getIndexSize() + entryTypes.size();
        } catch(Exception e){
            
        }
        return ncolumns;
    }
    
    @Override
    public boolean isCellEditable(int row, int column) {
       //all cells false
       return false;
    }
    
    @Override
    public int getRowCount(){
        //System.out.println("RAW COUNT is " + this.arrayEntries.size());
        //return this.arrayEntries.size();
        //return 2;
        int nrows = 0;
        try {
            nrows = entries.getMap().size();
        } catch (Exception e){
            
        }
        return nrows;
    }
    
    
    @Override
    public Object getValueAt(int row, int column) { 
        Set<Long>  keys = entries.getMap().keySet();
        int   ic = entries.getIndexSize();
        Iterator   iter = keys.iterator();
        Long       value = (Long) iter.next();
        for(int i = 0; i < row; i++){
            value = (Long) iter.next();
        }
        //System.out.println();
        if(column<entries.getIndexSize()){
            Integer index = IndexedList.IndexGenerator.getIndex(value, column);
            return index.toString();
        }
        
        IndexedEntry  trow = entries.getMap().get(value);
        //System.out.println(" number of rows = " + trow.getSize() + "  " + (column-ic));
        /*if((column-ic)>=trow.getSize()){
            return "0";
        }*/
        Number trowNum = trow.getValue(column-ic);
        if(trowNum instanceof Double){
            return String.format(this.precisionFormat, trowNum.doubleValue());
        }
        return trow.getValue(column-ic).toString();
    }
    
    public class RowConstraint {
        
        public int   COLUMN = 0;
        public double   MIN = 0.0;
        public double   MAX;
        public int      CONDITION_COLUMN       = -1;
        public int      CONDITION_COLUMN_VALUE = -1;
        
        public RowConstraint ( int column, double min, double max){
            this.COLUMN = column;
            this.MIN    = min;
            this.MAX    = max;
        }
        
        public RowConstraint ( int column, double min, double max, int ccol, int cvalue){
            this.COLUMN = column;
            this.MIN    = min;
            this.MAX    = max;
            this.CONDITION_COLUMN = ccol;
            this.CONDITION_COLUMN_VALUE = cvalue;
        }
        
        
        public boolean isValid(double value){ return (value>=this.MIN&&value<=this.MAX);}
        public int     conditionColumn(){ return this.CONDITION_COLUMN;}
        public int     conditionColumnValue(){ return this.CONDITION_COLUMN_VALUE;}
    }
    
    
    /**
     * internal class used for cell rendering
     */
    public static class IndexedCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent
                (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
                {
                    Component c = super.getTableCellRendererComponent
                                          (table, value, isSelected, hasFocus, row, column);
                     if(isSelected==true){
                         c.setBackground(new Color(20,20,255));                
                         return c;
                     }
                    if(row%2==0){
                        c.setBackground(new Color(220,255,220));
                    } else {
                        c.setBackground(new Color(220,220,255));
                    }
                    
                    return c;
                }
    }
    
    public static class IndexedEntry {
        
        //private List<Number>  columnValues = ;
        int  entrySize = 0;
        List<Number>   entryValues = new ArrayList<Number>();
        
        public IndexedEntry(int size){
            entrySize = size;
            for(int i = 0; i < size; i++){
                entryValues.add(2.4);
            }
        }
        
        public void setValue(int index, Number value){
            entryValues.set(index, value);
        }
        
        public Number getValue(int index){
            return this.entryValues.get(index);
        }
        
        public int getSize(){
            return this.entryValues.size();
        }
        
        public void setSize(int size){
            this.entryValues.clear();
            this.entrySize = size;
            for(int i = 0; i < size; i++){
                entryValues.add((Integer) 0);
            }
        }
    }
}
