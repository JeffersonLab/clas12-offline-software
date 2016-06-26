/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.calib.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author gavalian
 */
public class ConstantsManager {
    
    private DatabaseConstantsDescriptor  defaultDescriptor = new DatabaseConstantsDescriptor();
    private Map<Integer,DatabaseConstantsDescriptor>  runConstants = new LinkedHashMap<Integer,DatabaseConstantsDescriptor>();
    private String   databaseVariation = "default";
    
    public ConstantsManager(){
        
    }
    
    public ConstantsManager(String variation){
        this.databaseVariation = variation;
    }
    
    public String getVariation(){
        return this.databaseVariation;
    }
    
    public void setVariation(String variation){
        this.databaseVariation = variation;
    }
    
    public void init(List<String>  tables){
        this.defaultDescriptor.addTables(tables);
    }
    
    public void init(List<String>  keys, List<String>  tables){
        this.defaultDescriptor.addTables(keys,tables);
    }
    
    public IndexedTable  getConstants(int run, String table){
        if(this.runConstants.containsKey(run)==false){
            this.loadConstantsForRun(run);
        }
        DatabaseConstantsDescriptor  descriptor = this.runConstants.get(run);
        if(descriptor.getMap().containsKey(table)==false){
            System.out.println("[getConstants] error ( run = " + run + " ) "
                    + " table not found with name : " + table);
        }
        return descriptor.getMap().get(table);
    }
    
    private void loadConstantsForRun(int run){

        System.out.println("[ConstantsManager] --->  loading table for run = " + run);
        DatabaseConstantsDescriptor desc = defaultDescriptor.getCopy(run);
        DatabaseConstantProvider provider = new DatabaseConstantProvider(run,
                this.databaseVariation);

            for(int i = 0; i < desc.getTableNames().size(); i++){                
                String tableName = desc.getTableNames().get(i);
                try {
                    IndexedTable  table = provider.readTable(tableName);
                    desc.getMap().put(desc.getTableKeys().get(i), table);
                    System.out.println("adding : table " + tableName 
                    + "  key = " + desc.getTableKeys().get(i));
                } catch (Exception e) {
                    System.out.println("[ConstantsManager] ---> error reading table : "
                    + tableName);
                }
            }
            this.runConstants.put(run, desc);
            System.out.println(this.toString());
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        for(Map.Entry<Integer,DatabaseConstantsDescriptor> entry : runConstants.entrySet()){
            str.append("CONSTANTS SET FOR RUN = ");
            str.append(entry.getKey());
            str.append("\n");
            DatabaseConstantsDescriptor desc = entry.getValue();
            for(Map.Entry<String,IndexedTable>  tables : desc.getMap().entrySet()){
                str.append(String.format("TABLE : %s\n", tables.getKey()));
            }
        }
        return str.toString();
    }
    /**
     * Helper class to hold all constants for particular run.
     */
    public static class DatabaseConstantsDescriptor{
        
        private String  descName   = "descriptor";
        private int     runNumber  = 10;
        
        List<String>    tableNames = new ArrayList<String>();
        List<String>    mapKeys    = new ArrayList<String>();
        
        Map<String,IndexedTable>  hashTables = new LinkedHashMap<String,IndexedTable>();
        
        public DatabaseConstantsDescriptor(){
            
        }
        
        public void addTables(String[] tables){
            tableNames.addAll(Arrays.asList(tables));
            mapKeys.addAll(Arrays.asList(tables));
        }
        
        public void addTables(List<String> tables){
            for(String table : tables){
                tableNames.add(table);
                mapKeys.add(table);
            }
        }
        
        public void addTables(List<String> keys, List<String> tables){
            if(keys.size()!=tables.size()){
                System.out.println("[DatabaseConstantsDescriptor] error --> "
                + " size of keys ("+keys.size()+") does not match size of"
                        + " tables ("+tables.size()+")");
            } else {
                mapKeys.addAll(keys);
                tableNames.addAll(tables);                
            }
        }
        
        public boolean hasTable(String name){
            return hashTables.containsKey(name);
        }
        
        public int getRunNumber(){
            return this.runNumber;
        }
        
        public void setRunNumber(int run){
            this.runNumber = run;
        }
        
        public Map<String,IndexedTable>  getMap(){
            return this.hashTables;
        }
        
        public IndexedTable  getTable(String table){
            return this.hashTables.get(table);
        }
        
        public List<String>  getTableNames(){
            return this.tableNames;
        }
        
        public List<String>  getTableKeys(){
            return this.mapKeys;
        }
        
        public DatabaseConstantsDescriptor  getCopy(int run){
            DatabaseConstantsDescriptor desc = new DatabaseConstantsDescriptor();
            desc.addTables(this.getTableKeys(),this.getTableNames());
            return desc;
        }
        
        @Override
        public String toString(){
            StringBuilder str = new StringBuilder();
            for(int i = 0; i < this.tableNames.size();i++){
                str.append(String.format("%4d : %s\n", i , this.tableNames.get(i)));
            }
            return str.toString();
        }
    }
    
    
    public static void main(String[] args){
        
        ConstantsManager  manager = new ConstantsManager("default");
        manager.init(Arrays.asList(new String[]{
            "/daq/fadc/ec",
            "/daq/fadc/ftof","/daq/fadc/htcc"}));        
        IndexedTable  table1 = manager.getConstants(10, "/daq/fadc/htcc");
        IndexedTable  table2 = manager.getConstants(10, "/daq/fadc/ec");
        IndexedTable  table3 = manager.getConstants(12, "/daq/fadc/htcc");
    }
}
