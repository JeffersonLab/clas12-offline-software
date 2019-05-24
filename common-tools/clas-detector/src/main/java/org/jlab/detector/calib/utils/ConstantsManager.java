/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.calib.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author gavalian
 */
public class ConstantsManager {
    
    private DatabaseConstantsDescriptor  defaultDescriptor = new DatabaseConstantsDescriptor();
    private volatile Map<Integer,DatabaseConstantsDescriptor>  runConstants = new LinkedHashMap<Integer,DatabaseConstantsDescriptor>();
    private String   databaseVariation = "default";
    private String   timeStamp         = "";
   
    private volatile Map<Integer,RCDBConstants> rcdbConstants = new LinkedHashMap<Integer,RCDBConstants>();
    
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
    
    public void setTimeStamp(String timestamp){
        this.timeStamp = timestamp;
    }
    
    public synchronized void init(List<String>  tables){
        this.defaultDescriptor.addTables(tables);
    }
    
    public synchronized void init(List<String>  keys, List<String>  tables){
        Set<String> keysSet = new LinkedHashSet<String>(keys);
        Set<String> tablesSet = new LinkedHashSet<String>(tables);
        this.defaultDescriptor.addTables(keysSet,tablesSet);
        
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
  
    public RCDBConstants getRcdbConstants(int run) {
        if(this.rcdbConstants.containsKey(run)==false){
            this.loadConstantsForRun(run);
        }
        return this.rcdbConstants.get(run);
    }
    
    public RCDBConstants.RCDBConstant getRcdbConstant(int run,String name) {
        return getRcdbConstants(run).get(name);
    }
    
    private synchronized void loadConstantsForRun(int run){

        if(this.runConstants.containsKey(run)==true) return;
        
        System.out.println("[ConstantsManager] --->  loading table for run = " + run);
        DatabaseConstantsDescriptor desc = defaultDescriptor.getCopy(run);
        DatabaseConstantProvider provider = new DatabaseConstantProvider(run,
                this.databaseVariation, this.timeStamp);
        
        
        List<String>   tn = new ArrayList<String>(desc.getTableNames());
        List<String>   tk = new ArrayList<String>(desc.getTableKeys());
        
        //for(String tableName : desc.getTableNames())
        
        for(int i = 0; i < desc.getTableNames().size(); i++){                
            String tableName = tn.get(i);
            try {
                IndexedTable  table = provider.readTable(tableName);
                desc.getMap().put(tk.get(i), table);
                System.out.println(String.format("***** >>> adding : %14s / table = %s", tk.get(i),tableName));
                //System.out.println("***** >>> adding : table " + tableName 
                //        + "  key = " + tk.get(i));
            } catch (Exception e) {
                System.out.println("[ConstantsManager] ---> error reading table : "
                        + tableName);
            }
        }
        provider.disconnect();
        this.runConstants.put(run, desc);
        //System.out.println(this.toString());

        RCDBProvider rcdbpro = new RCDBProvider();
        this.rcdbConstants.put(run,rcdbpro.getConstants(run));
        rcdbpro.disconnect();
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
    public static class DatabaseConstantsDescriptor {
        
        private String  descName   = "descriptor";
        private int     runNumber  = 10;
        private int     nIndex     = 3;
        
        Set<String>    tableNames  = new LinkedHashSet<String>();
        
        Set<String>    mapKeys     = new LinkedHashSet<String>();
        
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
        
        public void addTables(Set<String> keys, Set<String> tables){
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
        
        public Set<String>  getTableNames(){
            return this.tableNames;
        }
        
        public Set<String>  getTableKeys(){
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
            int i = 0;
            for(String name : tableNames){
                //for(int i = 0; i < this.tableNames.size();i++){
                str.append(String.format("%4d : %s\n", i , name));
                i++;
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
