/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jlab.detector.calib.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.ccdb.CcdbPackage;
import javax.swing.JFrame;
import org.jlab.ccdb.Assignment;

import org.jlab.ccdb.Directory;
import org.jlab.ccdb.JDBCProvider;
import org.jlab.ccdb.TypeTable;
import org.jlab.ccdb.TypeTableColumn;
import org.rcdb.RCDB;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.utils.groups.IndexedTableViewer;



/**
 *
 * @author gavalian
 */
public class DatabaseConstantProvider implements ConstantProvider {
    
    private HashMap<String,String[]> constantContainer = new HashMap<String,String[]>();
    private boolean PRINT_ALL = true;
    private String variation  = "default";
    private Integer runNumber = 10;
    private Integer loadTimeErrors = 0;
    private Boolean PRINTOUT_FLAG  = false;
    private Integer dataYear       = 118;
    private Integer dataMonth      = 1;
    private Date    databaseDate   = new Date();
    
    private org.jlab.ccdb.JDBCProvider provider;
    
    private int          debugMode = 1;
    
    public DatabaseConstantProvider(){
        this.loadTimeErrors = 0;
        this.runNumber = 10;
        this.variation = "default";
        
        String address = "mysql://clas12reader@clasdb.jlab.org/clas12";
        
        String envAddress = this.getEnvironment();        
        if(envAddress!=null) address = envAddress;
        this.initialize(address);
    }
    
    public DatabaseConstantProvider(int run, String var){
        
        this.loadTimeErrors = 0;
        this.runNumber = run;
        this.variation = var;
        
        String address = "mysql://clas12reader@clasdb.jlab.org/clas12";
        
        String envAddress = this.getEnvironment();        
        if(envAddress!=null) address = envAddress;
        this.initialize(address);
    }
    
    public DatabaseConstantProvider(int run, String var, String timestamp){
        
        this.loadTimeErrors = 0;
        this.runNumber = run;
        this.variation = var;
        
        String address = "mysql://clas12reader@clasdb.jlab.org/clas12";
        
        String envAddress = this.getEnvironment();        
        if(envAddress!=null) address = envAddress;
        if(timestamp.length()>8){
            this.setTimeStamp(timestamp);
        }
        this.initialize(address);
    }
    
    public DatabaseConstantProvider(String address){
        this.initialize(address);
    }
    
    public DatabaseConstantProvider(String address, String var){
        this.variation = var;
        this.initialize(address);
    }
    
    public void setPrintout(Boolean flag){ this.PRINTOUT_FLAG = flag;}
    
    public Set<String> getEntrySet(){
        Set<String> entries = new HashSet<String>();
        for(Map.Entry<String,String[]> entry: this.constantContainer.entrySet()){
            entries.add(entry.getKey());
        }
        return entries;
    }
    
    private String getEnvironment(){
        
        String envCCDB   = System.getenv("CCDB_DATABASE");
        String envCLAS12 = System.getenv("CLAS12DIR");
        String connection = System.getenv("CCDB_CONNECTION");
        
        if(connection!=null){
            return connection;
        }
        
        String propCLAS12 = System.getProperty("CLAS12DIR");
        String propCCDB   = System.getProperty("CCDB_DATABASE");
        
        //System.out.println("ENVIRONMENT : " + envCLAS12 + " " + envCCDB + " " + propCLAS12 + " " + propCCDB);
        
        if(envCCDB!=null&&envCLAS12!=null){
            StringBuilder str = new StringBuilder();
            str.append("sqlite:///");
            if(envCLAS12.charAt(0)!='/') str.append("/");
            str.append(envCLAS12);
            if(envCCDB.charAt(0)!='/' && envCLAS12.charAt(envCLAS12.length()-1)!='/'){
                str.append("/");
            }
            str.append(envCCDB);
            return str.toString();
        }
        
        if(propCCDB!=null&&propCLAS12!=null){
            StringBuilder str = new StringBuilder();
            str.append("sqlite:///");
            if(propCLAS12.charAt(0)!='/') str.append("/");
            str.append(propCLAS12);
            if(propCCDB.charAt(0)!='/' && propCLAS12.charAt(propCLAS12.length()-1)!='/'){
                str.append("/");
            }
            str.append(propCCDB);
            return str.toString();
        }
        
        return null;
    }
    
    private void initialize(String address){
        provider = CcdbPackage.createProvider(address);
        if(debugMode>0){
            System.out.println("[DB] --->  open connection with : " + address);
            System.out.println("[DB] --->  database variation   : " + this.variation);
            System.out.println("[DB] --->  database run number  : " + this.runNumber);
            System.out.println("[DB] --->  database time stamp  : " + databaseDate);
        }
        
        provider.connect();
        
        if(provider.isConnected()==true){
            if(debugMode>0) System.out.println("[DB] --->  database connection  : success");
        } else {
            System.out.println("[DB] --->  database connection  : failed");
        }
        
        provider.setDefaultVariation(variation);
        provider.setDefaultDate(databaseDate);
        provider.setDefaultRun(this.runNumber);

        //Directory dir = provider.getDirectory("/calibration/ftof/");        
        //Assignment asgmt = provider.getData("/test/test_vars/test_table");
    }
    
    public final void setTimeStamp(String timestamp){
        String pattern = "MM/dd/yyyy";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        
        try {
            databaseDate = format.parse(timestamp);
        } catch (ParseException ex) {
            System.out.println("\n\n ***** TIMESTAMP ERROR ***** error parsing timestamp : " + timestamp);
            databaseDate = new Date();
            System.out.println(" ***** TIMESTAMP WARNING ***** setting date to : " + databaseDate);

        }
        
    }
    /**
     * Reads calibration constants for given table in the database.
     * @param table_name
     * @return 
     */
    public CalibrationConstants  readConstants(String table_name){
        
        Assignment asgmt = provider.getData(table_name);
        int ncolumns = asgmt.getColumnCount();
        Vector<TypeTableColumn> typecolumn = asgmt.getTypeTable().getColumns();
        String[] format = new String[ncolumns-3];

        for(int loop = 3; loop < ncolumns; loop++){
            //System.out.println("COLUMN " + typecolumn.get(loop).getName() 
            //        + "  " + typecolumn.get(loop).getCellType().name());
            if(typecolumn.get(loop).getCellType().name().compareTo("DOUBLE")==0){
                format[loop-3] = typecolumn.get(loop).getName() + "/D";
            } else {
                format[loop-3] = typecolumn.get(loop).getName() + "/I";
            }
            //format[loop-3] = 
        }
        
        CalibrationConstants  table = new CalibrationConstants(3,format);
        for(int i = 0; i < 3; i++){
            table.setIndexName(i, typecolumn.get(i).getName());
        }
        table.show();
                List< Vector<String> >  tableRows = new ArrayList< Vector<String> >();
        
        
        for(int loop = 0; loop < ncolumns; loop++){
            String name = typecolumn.get(loop).getName();
            Vector<String> column = asgmt.getColumnValuesString(name);
            tableRows.add(column);
        }
        
        int nrows = tableRows.get(0).size();
        
        for(int nr = 0 ; nr < nrows; nr++){
            String[] values = new String[ncolumns];
            for(int nc = 0; nc < ncolumns; nc++){
                values[nc] = tableRows.get(nc).get(nr);
            }
            /*System.out.println("\n LENGTH = " + values.length);
            for(String item : values){
                System.out.print(item + "  :  ");
            }*/
            //table.addRow(values);
            table.addEntryFromString(values);
        }
        /*
                String[] values = new String[row.size()];
                System.out.println("VALUES SIZE = " + row.size());
                for(int el = 0; el < row.size(); el++){
                    values[el] = row.elementAt(el);                    
                    //for(String cell: row){
                    //System.out.print(cell + " ");
                }
                table.addRow(values);
        }*/
        return table;  
    }
    
    public IndexedTable  readTable(String table_name){
        return this.readTable(table_name, 3);
    }
    
    public IndexedTable  readTable(String table_name,int nindex){

        Assignment asgmt = provider.getData(table_name);
        int ncolumns = asgmt.getColumnCount();
        Vector<TypeTableColumn> typecolumn = asgmt.getTypeTable().getColumns();
        
        String[] format = new String[ncolumns-nindex];

        for(int loop = nindex; loop < ncolumns; loop++){
            //System.out.println("COLUMN " + typecolumn.get(loop).getName() 
            //        + "  " + typecolumn.get(loop).getCellType().name());
            if(typecolumn.get(loop).getCellType().name().compareTo("DOUBLE")==0){
                format[loop-nindex] = typecolumn.get(loop).getName() + "/D";
            } else {
                format[loop-nindex] = typecolumn.get(loop).getName() + "/I";
            }
            //format[loop-3] = 
        }
        
        IndexedTable  table = new IndexedTable(nindex,format);
        for(int i = 0; i < nindex; i++){
            table.setIndexName(i, typecolumn.get(i).getName());
        }
        //table.show();
                
        
        List< Vector<String> >  tableRows = new ArrayList< Vector<String> >();
        
        
        for(int loop = 0; loop < ncolumns; loop++){
            String name = typecolumn.get(loop).getName();
                Vector<String> column = asgmt.getColumnValuesString(name);
                tableRows.add(column);
        }
        
        int nrows = tableRows.get(0).size();
        
        for(int nr = 0 ; nr < nrows; nr++){
            String[] values = new String[ncolumns];
            for(int nc = 0; nc < ncolumns; nc++){
                values[nc] = tableRows.get(nc).get(nr);
            }
            /*System.out.println("\n LENGTH = " + values.length);
            for(String item : values){
                System.out.print(item + "  :  ");
            }*/
            //table.addRow(values);
            table.addEntryFromString(values);
        }
        /*
                String[] values = new String[row.size()];
                System.out.println("VALUES SIZE = " + row.size());
                for(int el = 0; el < row.size(); el++){
                    values[el] = row.elementAt(el);                    
                    //for(String cell: row){
                    //System.out.print(cell + " ");
                }
                table.addRow(values);
        }*/
        return table;        
    }
    
    public void loadTable(String table_name){
        try {
            Assignment asgmt = provider.getData(table_name);
            
            int ncolumns = asgmt.getColumnCount();
            TypeTable  table = asgmt.getTypeTable();
            Vector<TypeTableColumn> typecolumn = asgmt.getTypeTable().getColumns();
            System.out.println("[DB LOAD] ---> loading data table : " + table_name);
            System.out.println("[DB LOAD] ---> number of columns  : " + typecolumn.size());
            System.out.println();
            for(int loop = 0; loop < ncolumns; loop++){
                //System.out.println("Reading column number " + loop 
                //+ "  " + typecolumn.elementAt(loop).getCellType()
                //+ "  " + typecolumn.elementAt(loop).getName());
                String name = typecolumn.get(loop).getName();
                Vector<String> row = asgmt.getColumnValuesString(name);
                String[] values = new String[row.size()];
                for(int el = 0; el < row.size(); el++){
                    values[el] = row.elementAt(el);
                    //for(String cell: row){
                    //System.out.print(cell + " ");
                }
                StringBuilder str = new StringBuilder();
                str.append(table_name);
                str.append("/");
                str.append(typecolumn.elementAt(loop).getName());
                constantContainer.put(str.toString(), values);
                //System.out.println(); //next line after a row
            }
            //provider.close();
        } catch (Exception e){
            System.out.println("[DB LOAD] --->  error loading table : " + table_name);
            this.loadTimeErrors++;
        }
    }
    
    public void loadTables(String... tbl){
        for(String table : tbl){
            
        }
    }
    
    @Override
    public boolean hasConstant(String string) {
        return constantContainer.containsKey(string);
    }

    @Override
    public int length(String string) {
        if(this.hasConstant(string)) return constantContainer.get(string).length;
        return 0;
    }

    @Override
    public double getDouble(String string, int i) {
        if(this.hasConstant(string)==true && i < this.length(string)){
            return Double.parseDouble(constantContainer.get(string)[i]);
        } else {
            
        }
        return 0.0;
    }

    @Override
    public int getInteger(String string, int i) {
        if(this.hasConstant(string)==true && i < this.length(string)){
            return Integer.parseInt(constantContainer.get(string)[i]);
        } else {
            
        }
        return 0;
    }
    
    public void disconnect(){
        System.out.println("[DB] --->  database disconnect  : success");
        this.provider.close();
    }
    /**
     * prints out table with loaded values.
     */
    public void show(){
        /*
        System.out.println("\n\n");
        StringTable table = new StringTable();
        System.out.println("\t" + StringTable.getCharacterString("*", 70));
        System.out.println(String.format("\t*  %-52s : %8s   *", "Item Name","Length"));
        System.out.println("\t" + StringTable.getCharacterString("*", 70));
        System.out.print(this.showString());
        System.out.println("\t" + StringTable.getCharacterString("*", 70));
        */
    }
    /**
     * returns a string representing a table printout of the constants
     * 
     * @return 
     */
    public String showString(){
        StringBuilder str = new StringBuilder();
        for(Map.Entry<String,String[]> item : this.constantContainer.entrySet()){
            str.append(String.format("\t*  %-52s : %8d   *\n", item.getKey(),item.getValue().length));
        }
        return str.toString();
    }
    
    @Override
    public String toString(){
        System.err.println("Database Constat Provider: ");
        StringBuilder str = new StringBuilder();
        if(PRINT_ALL==true){
            for(Map.Entry<String,String[]> entry : constantContainer.entrySet()){
                str.append(String.format("%24s : %d\n", entry.getKey(),entry.getValue().length));
                for(int loop = 0; loop < entry.getValue().length; loop++){
                    //str.append("\n");
                    str.append(String.format("%18s ", entry.getValue()[loop]));
                }
                str.append("\n");
            }
        } else {
            for(Map.Entry<String,String[]> entry : constantContainer.entrySet()){
                str.append(String.format("%24s : %d\n", entry.getKey(),entry.getValue().length));
            }
        }
        return str.toString();
    }
    
    
    public void clear(){
        this.constantContainer.clear();
    }
    
    public int getSize(){
        return this.constantContainer.size();
    }
    
    public int getSize(String name){
        if(this.hasConstant(name)==true){
            String[] array = this.constantContainer.get(name);
            return array.length;
        }
        return 0;
    }
    
    

    public static void main(String[] args){
        
        
        
            DatabaseConstantProvider provider = new DatabaseConstantProvider(10,"default");
            IndexedTable table = provider.readTable("/test/fc/fadc");
            
            provider.disconnect();
            JFrame frame = new JFrame();
            frame.setSize(600, 600);
            
            
            IndexedTableViewer canvas = new IndexedTableViewer(table);
            frame.add(canvas);
            frame.pack();
            frame.setVisible(true);
            table.show();
         
         
        String pattern = "MM/dd/yyyy";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        Date dateNow = new Date();
        System.out.println(dateNow);
        
        try {
            Date dateThen = format.parse("01/23/2017");
            System.out.println(dateThen);            
        } catch (ParseException ex) {
            Logger.getLogger(DatabaseConstantProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        org.rcdb.JDBCProvider provider_rcdb = RCDB.createProvider("mysql://rcdb@clasdb.jlab.org/rcdb");
        provider_rcdb.connect();
        
        // The real database is going to be used for the example
        // Run 31000 is used everywhere:
        long runNumber = 2475;
        System.out.println("Run Number = " + runNumber);
        // get long value
        long eventCount = provider_rcdb.getCondition(runNumber, "event_count").toLong();
        System.out.println("event_count = " + eventCount);

        // get bool value
        boolean isValidRunEnd = provider_rcdb.getCondition(runNumber, "is_valid_run_end").toBoolean();
        System.out.println("is_valid_run_end = " + isValidRunEnd);

        // List all available condition names
	//        Vector<ConditionType> cndTypes = provider.getConditionTypes();
        //HashMap<String, ConditionType> cndTypeByNames = provider.getConditionTypeByNames();
        // get double
           double sol_scale = provider_rcdb.getCondition(runNumber, "solenoid_scale").toDouble();
           System.out.println("solenoid_scale = " + sol_scale);
           double tor_scale = provider_rcdb.getCondition(runNumber, "torus_scale").toDouble();
           System.out.println("torus_scale = " + tor_scale);
           provider_rcdb.close();
    }

}
