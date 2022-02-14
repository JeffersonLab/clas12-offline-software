package org.jlab.detector.calib.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jlab.geom.base.ConstantProvider;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author gavalian
 */
public class ConstantsManager {

    private static Logger LOGGER = Logger.getLogger(ConstantsManager.class.getName());

    private DatabaseConstantsDescriptor defaultDescriptor = new DatabaseConstantsDescriptor();
    private volatile Map<Integer, DatabaseConstantsDescriptor> runConstants = new LinkedHashMap<Integer, DatabaseConstantsDescriptor>();
    private volatile Map<Integer, Integer> runConstantRequestHistory = new LinkedHashMap<Integer, Integer>();
    private static volatile Map<Integer, RCDBConstants> rcdbConstants = new LinkedHashMap<Integer, RCDBConstants>();

    private String databaseVariation = "default";
    private String timeStamp = "";
    private int requestStatus = 0;
    private int maxRequests = 2;

    public ConstantsManager() {

    }

    public ConstantsManager(String variation) {
        this.databaseVariation = variation;
    }

    public String getVariation() {
        return this.databaseVariation;
    }

    public void setVariation(String variation) {
        this.databaseVariation = variation;
    }

    public void setTimeStamp(String timestamp) {
        this.timeStamp = timestamp;
    }

    public synchronized void init(List<String> tables) {
        this.defaultDescriptor.addTables(tables);
    }
    
    /**
     * use a map just to avoid name clash
     * @param tables map of table_name to #indices 
     */
    public synchronized void init(Map<String,Integer>  tables){
        this.defaultDescriptor.addTables(tables);
    }
    
    public int getRequestStatus(){
        return requestStatus;
    }

    public synchronized void init(List<String> keys, List<String> tables) {
        Set<String> keysSet = new LinkedHashSet<String>(keys);
        Set<String> tablesSet = new LinkedHashSet<String>(tables);
        this.defaultDescriptor.addTables(keysSet, tablesSet);

    }

    public IndexedTable getConstants(int run, String table) {
        if (this.runConstants.containsKey(run) == false) {
            this.loadConstantsForRun(run);
        }
        DatabaseConstantsDescriptor descriptor = this.runConstants.get(run);
        if (descriptor.getMap().containsKey(table) == false) {
            LOGGER.log(Level.SEVERE,
                    "[getConstants] error ( run = " + run + " ) " + " table not found with name : " + table);
        }
        return descriptor.getMap().get(table);
    }

    public RCDBConstants getRcdbConstants(int run) {
        if (this.rcdbConstants.containsKey(run) == false) {
            this.loadConstantsForRun(run);
        }
        return this.rcdbConstants.get(run);
    }

    public RCDBConstants.RCDBConstant getRcdbConstant(int run, String name) {
        return getRcdbConstants(run).get(name);
    }

    private synchronized void loadConstantsForRun(int run) {

        if (this.runConstants.containsKey(run) == true)
            return;

        if (this.runConstantRequestHistory.containsKey(run) == false) {
            runConstantRequestHistory.put(run, 1);
        } else {
            int requests = runConstantRequestHistory.get(run);
            runConstantRequestHistory.put(run, requests + 1);
            if (requests > maxRequests) {
                requestStatus = -1;
                LOGGER.log(Level.SEVERE,
                        "[ConstantsManager] exceeded maximum requests " + requests + " for run " + run);
            }
        }

        LOGGER.log(Level.INFO, "[ConstantsManager] --->  loading table for run = " + run);
        DatabaseConstantsDescriptor desc = defaultDescriptor.getCopy(run);
        DatabaseConstantProvider provider = new DatabaseConstantProvider(run, this.databaseVariation, this.timeStamp);

        List<String> tn = new ArrayList<String>(desc.getTableNames());
        List<String> tk = new ArrayList<String>(desc.getTableKeys());

        for (int i = 0; i < desc.getTableNames().size(); i++) {
            String tableName = tn.get(i);
            try {
                IndexedTable  table = provider.readTable(tableName, desc.getTableIndices().get(i));
                desc.getMap().put(tk.get(i), table);
                LOGGER.log(Level.INFO, String.format("***** >>> adding : %14s / table = %s", tk.get(i), tableName));
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "[ConstantsManager] ---> error reading table : " + tableName);
                // This happens if missing table or variation. No point in trying
                // again, just set error status to trigger abort.
                requestStatus = -1;
            }
        }
        provider.disconnect();
        this.runConstants.put(run, desc);

        if (this.rcdbConstants.containsKey(run) == false) {
            RCDBProvider rcdbpro = new RCDBProvider();
            this.rcdbConstants.put(run, rcdbpro.getConstants(run));
            rcdbpro.disconnect();
        }
    }

    public void reset() {
        this.runConstants.clear();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (Map.Entry<Integer, DatabaseConstantsDescriptor> entry : runConstants.entrySet()) {
            str.append("CONSTANTS SET FOR RUN = ");
            str.append(entry.getKey());
            str.append("\n");
            DatabaseConstantsDescriptor desc = entry.getValue();
            for (Map.Entry<String, IndexedTable> tables : desc.getMap().entrySet()) {
                str.append(String.format("TABLE : %s\n", tables.getKey()));
            }
        }
        return str.toString();
    }

    /**
     * Helper class to hold all constants for particular run.
     */
    public static class DatabaseConstantsDescriptor {
        
        Logger LOGGER = Logger.getLogger(DatabaseConstantsDescriptor.class.getName());

        private String  descName   = "descriptor";
        private int     runNumber  = 10;
        List<Integer>  tableIndices = new ArrayList<Integer>();
        Set<String>    tableNames  = new LinkedHashSet<String>();
        Set<String>    mapKeys     = new LinkedHashSet<String>();
        Map<String,IndexedTable>  hashTables = new LinkedHashMap<String,IndexedTable>();
        
        public DatabaseConstantsDescriptor(){
            
        }
       
        public void addTable(String table, int indices) {
            if (tableNames.add(table)) {
                mapKeys.add(table);
                tableIndices.add(indices);
            }
        }

        public void addTables(List<String> tables){
            for(String table : tables){
                addTable(table, DatabaseConstantProvider.DEFAULT_INDICES);
            }
        }
        
        public void addTables(String[] tables){
            addTables(Arrays.asList(tables));
        }
       
        public void addTables(Set<String> keys, Set<String> tables){
            if(keys.size()!=tables.size()){
                LOGGER.log(Level.SEVERE,"[DatabaseConstantsDescriptor] error --> "
                + " size of keys ("+keys.size()+") does not match size of"
                        + " tables ("+tables.size()+")");
            } else {
                mapKeys.addAll(keys);
                tableNames.addAll(tables);
                for (int i=0; i<mapKeys.size(); i++) {
                    tableIndices.add(DatabaseConstantProvider.DEFAULT_INDICES);
                }
            }
        }

        public void addTables(Set<String> keys, Set<String> tables, List<Integer> indices){
            if(keys.size()!=tables.size()){
                LOGGER.log(Level.SEVERE,"[DatabaseConstantsDescriptor] error --> "
                + " size of keys ("+keys.size()+") does not match size of"
                        + " tables ("+tables.size()+")");
            } else {
                mapKeys.addAll(keys);
                tableNames.addAll(tables);
                tableIndices.addAll(indices);
            }
        }

        /**
         * 
         * @param tables 
         */
        public void addTables(Map<String,Integer> tables) {
            for (String table : tables.keySet()) {
                addTable(table, tables.get(table));
            } 
        }

        public boolean hasTable(String name){
            return hashTables.containsKey(name);
        }

        public int getRunNumber() {
            return this.runNumber;
        }

        public void setRunNumber(int run) {
            this.runNumber = run;
        }

        public Map<String, IndexedTable> getMap() {
            return this.hashTables;
        }

        public IndexedTable getTable(String table) {
            return this.hashTables.get(table);
        }

        public Set<String> getTableNames() {
            return this.tableNames;
        }

        public Set<String> getTableKeys() {
            return this.mapKeys;
        }
       
        public List<Integer> getTableIndices(){
            return this.tableIndices;
        }

        public DatabaseConstantsDescriptor  getCopy(int run){
            DatabaseConstantsDescriptor desc = new DatabaseConstantsDescriptor();
            desc.addTables(this.getTableKeys(),this.getTableNames(),this.getTableIndices());
            return desc;
        }

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder();
            int i = 0;
            for(String name : tableNames){
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
        for(int i = 0; i < 3 ; i++){
            IndexedTable  table1 = manager.getConstants(10, "/daq/fadc/htcc");
            IndexedTable  table2 = manager.getConstants(10, "/daq/fadc/ec");
            manager.reset();
            LOGGER.log(Level.INFO,"\n\n STATUS = " + manager.getRequestStatus());
        }

        ConstantsManager conman = new ConstantsManager("default");
        Map<String,Integer> tables = new HashMap<>();
        tables.put("/calibration/dc/time_corrections/T0Corrections",4);
        tables.put("/calibration/dc/time_corrections/timingcuts",3);
        conman.init(tables);
        IndexedTable t4 = conman.getConstants(4013, "/calibration/dc/time_corrections/T0Corrections");
        IndexedTable t3 = conman.getConstants(4013, "/calibration/dc/time_corrections/timingcuts");
        LOGGER.log(Level.INFO,"4conman:  "+t4.getColumnCount());
        LOGGER.log(Level.INFO,"4conman:  "+t4.toString());
        LOGGER.log(Level.INFO,"4conman:  1/4/6/1:  "+t4.getDoubleValue("T0Correction", 1,4,6,1));
        LOGGER.log(Level.INFO,"3conman:  0/2/56:   "+t3.getDoubleValue("LinearCoeff",0,2,56));
        
    }
}
