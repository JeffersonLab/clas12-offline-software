package org.jlab.service.swaps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.jnp.hipo4.data.SchemaFactory;
import org.jlab.utils.groups.IndexedTable;

/**
 * 
 * Cache manager for cable swaps across multiple run numbers and CCDB tables,
 * interpreted from two ConstantsManager instances, implemented to enable
 * transforming a decoded HIPO file's sector/layer/component/order from the
 * translation tables used at decoding time into a different one.
 * 
 * @author baltzell
 */
public class SwapManager {

    public static final String DEF_CURRENT_CCDB_VARIATION = "swaps";
    public static final String DEF_PREVIOUS_CCDB_VARIATION = "default";
    public static final String[] DEF_DETECTOR_NAMES = {
        "FTCAL",
        "FTHODO",
        "FTTRK",
        "LTCC",
        "ECAL",
        "FTOF",
        "HTCC",
        "DC",
        "CTOF",
        "CND",
        "BST",
        "RF",
        "BMT",
        "FMT",
        "RICH",
        "HEL",
        "BAND",
        "RTPC"
    };

    private final HashMap<Integer,HashMap<String,SwapTable>> swaps = new HashMap<>();
    private final List<String> tableNames = new ArrayList<>();
    private final List<String> detectorNames = new ArrayList<>();
    private final Map<String,Detector> detectors = new HashMap<>();
    private final ConstantsManager prevConman;
    private final ConstantsManager currConman;

    public Set<String> getDetectors() {
        return this.detectors.keySet();
    }
    public String getTable(String detectorName) {
        return this.detectors.get(detectorName).table;
    }
    public List<String> getBanks(String detectorName) {
        return this.detectors.get(detectorName).getBanks();
    }
    
    public class Detector {
        private final String name;
        private final String table;
        private final List<String> banks;
        public Detector(String name,String table) {
            this.banks = new ArrayList<>();
            this.name = name;
            this.table = table;
        }
        public List<String> getBanks() {
            //List<String> ret=new ArrayList<>();
            return this.banks;
        }
        public void addBank(String b){this.banks.add(b);}
        @Override
        public String toString() {
            return this.name+":"+this.table+":"+String.join(":",this.banks);
        }
    }

    /**
     * @param detectorNames
     * @param prevTimestamp in CCDB format:  MM/DD/YYYY
     * @param currTimestamp in CCDB format:  MM/DD/YYYY
     */
    public SwapManager(List<String> detectorNames, String prevTimestamp,String currTimestamp) {
        this.initDetectors(detectorNames);
        this.prevConman = new ConstantsManager();
        this.currConman = new ConstantsManager();
        this.prevConman.setTimeStamp(prevTimestamp);
        this.prevConman.setVariation(DEF_PREVIOUS_CCDB_VARIATION);
        this.prevConman.init(this.tableNames);
        if (currTimestamp != null) this.currConman.setTimeStamp(currTimestamp);
        this.currConman.setVariation(DEF_CURRENT_CCDB_VARIATION);
        this.currConman.init(this.tableNames);
    }

    /**
     * @param detectorNames
     * @param previous timestamp/variation used for translation tables during decoding
     * @param current timestamp/variation with correct translation tables
     */
    public SwapManager(List<String> detectorNames,ConstantsManager previous,ConstantsManager current) {
        this.initDetectors(detectorNames);
        this.prevConman = previous;
        this.currConman = current;
    }

    /**
     * Get an unswapped value. 
     * @param run run number
     * @param tableName CCDB translation table name, e.g. "/daq/tt/ecal"
     * @param varName name of new variable to retrieve (sector/layer/component/order)
     * @param slco array of old variables (sector/layer/component/order)
     * @return new value of the requested variable
     */
    public int get(int run,String tableName,String varName,int... slco) {
        if (!this.swaps.containsKey(run)) {
            this.add(run);
        }
        return this.swaps.get(run).get(tableName).get(varName,slco);
    }

    /**
     * Initialize the swaps for a given run number.
     * @param run 
     */
    private void add(int run) {
        this.swaps.put(run,new HashMap<>());
        for (String tableName : tableNames) {
            IndexedTable prev = prevConman.getConstants(run, tableName);
            IndexedTable curr = currConman.getConstants(run, tableName);
            this.swaps.get(run).put(tableName,new SwapTable(prev,curr));
        }
    }

    @Override
    public String toString() {
        String ret = "";
        for (int run : this.swaps.keySet()) {
            for (String table : this.swaps.get(run).keySet()) {
                ret += this.swaps.get(run).get(table);
            }
        }
        return ret;
    }

    /**
     * Initialize the appropriate bank names and corresponding translation
     * table names, based on the given detector names.
     */
    private void initNames(List<String> detectorNames) {
        SchemaFactory schema = new SchemaFactory();
        schema.initFromDirectory(System.getenv("CLAS12DIR")+"/etc/bankdefs/hipo4");
        //schema.initFromDirectory("/Users/baltzell/cos-iss611-swaps/coatjava/etc/bankdefs/hipo4/");
        //schema.show();
        for (String detName : detectorNames) {
            // some detectors broke the bank/table naming convention:
            String tableName = detName.equals("BST") ? "/daq/tt/svt" : "/daq/tt/"+detName.toLowerCase();
            Detector det = new Detector(detName,tableName);
            this.tableNames.add(tableName);
            if (schema.hasSchema(detName+"::adc")) {
                det.banks.add(detName+"::adc");
            }
            if (schema.hasSchema(detName+"::tdc")) {
                det.banks.add(detName+"::tdc");
            }
            this.detectors.put(detName,det);
        }
    }

    public final void initDetectors(List<String> detectorNames) {
        List<String> allDets = Arrays.asList(DEF_DETECTOR_NAMES);
        if (detectorNames == null || detectorNames.isEmpty()) {
            this.detectorNames.addAll(allDets);
        }
        else {
            for (String detectorName : detectorNames) {
                if (allDets.contains(detectorName)) {
                    this.detectorNames.add(detectorName);
                }
                else {
                    this.detectorNames.clear();
                    throw new RuntimeException("[SwapManager] --> Invalid detector name:  "+detectorName);
                }
            }
        }
        this.initNames(this.detectorNames);
    }

    public static void main(String[] args) {
        SwapManager man = new SwapManager(Arrays.asList("BMT"),"08/10/2020","10/13/2020");
        man.get(11014, man.tableNames.get(0),"sector",3,6,8,0);
        System.out.println("SwapManager:\n"+man);
    }

}
