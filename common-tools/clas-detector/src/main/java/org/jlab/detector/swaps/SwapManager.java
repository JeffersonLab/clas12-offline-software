package org.jlab.detector.swaps;

import org.jlab.detector.swaps.SwapTable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
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
    private final Map<String,String> banksToTables = new HashMap<>();
    private final Map<String,Detector> detectors = new HashMap<>();
    private ConstantsManager prevConman = null;
    private ConstantsManager currConman = null;

    private static SwapManager instance = null;

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

    private SwapManager() {
    }
    
    public static SwapManager getInstance() {
        if (instance == null) {
            instance = new SwapManager();
        }
        return instance;
    }

    /**
     * @param detectorNames
     * @param prevTimestamp in CCDB format:  MM/DD/YYYY
     * @param currTimestamp in CCDB format:  MM/DD/YYYY
     */
    public SwapManager(List<String> detectorNames, String prevTimestamp,String currTimestamp) {
        this.initialize(detectorNames, prevTimestamp, currTimestamp);    
    }

    /**
     * @param detectorNames
     * @param previous timestamp/variation used for translation tables during decoding
     * @param current timestamp/variation with correct translation tables
     */
    public SwapManager(List<String> detectorNames,ConstantsManager previous,ConstantsManager current) {
        this.initialize(detectorNames, previous, current);
    }

    /**
     * @param detectorNames
     * @param prevTimestamp in CCDB format:  MM/DD/YYYY
     * @param currTimestamp in CCDB format:  MM/DD/YYYY
     */
    public void initialize(List<String> detectorNames, String prevTimestamp,String currTimestamp) {
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
    public void initialize(List<String> detectorNames,ConstantsManager previous,ConstantsManager current) {
        this.initDetectors(detectorNames);
        this.prevConman = previous;
        this.currConman = current;
    }

    /**
     * @param run run number
     * @param tableName CCDB translation table name, e.g. "/dat/tt/ecal"
     * @param slco array of old indices (sector,layer,component,order)
     * @return array of new table indices (sector/layer/component/order)
     */
    public int[] get(int run, String tableName, int... slco) {
        if (this.currConman == null || this.prevConman == null) {
            return slco;
        }
        if (!this.swaps.containsKey(run)) {
            this.add(run);
        }
        if (this.swaps.get(run).containsKey(tableName)) {
            return this.swaps.get(run).get(tableName).get(slco);
        }
        else {
            return slco;
        }
    }

    /**
     * @param run run number
     * @param tableName CCDB translation table name, e.g. "/daq/tt/ecal"
     * @param varName name of new index to retrieve (sector/layer/component/order)
     * @param slco array of old indices (sector/layer/component/order)
     * @return new value of the requested index (sector/layer/component/order)
     */
    public int get(int run,String tableName,String varName,int... slco) {
        return this.get(run,tableName,slco)[SwapTable.getVariableIndex(varName)];
    }

    /**
     * @param run run number
     * @param bank ADC/TDC bank
     * @param row row index in bank
     * @return array of new table indices (sector/layer/component/order)
     */
    public int[] get(int run, DataBank bank, int row) {
        final int sector = bank.getByte("sector", row);
        final int layer = bank.getByte("layer", row);
        final int comp = bank.getShort("component", row);
        final int order = bank.getByte("order", row);
        return this.get(run,banksToTables.get(bank.getDescriptor().getName()),sector,layer,comp,order);
    }

    /**
     * @param event the HIPO event
     * @param bankName name of ADC/TDC bank
     * @param row row index in bank
     * @return array of new table indices (sector/layer/component/order)
     */
    public int[] get(DataEvent event, String bankName, int row) {
        if (event.hasBank("RUN::config")) {
            if (event.hasBank(bankName)) {
                final int run = event.getBank("RUN::config").getInt("run",0);
                return this.get(run,event.getBank(bankName),row);
            }
        }
        return null;
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
                this.banksToTables.put(detName+"::adc",tableName);
            }
            if (schema.hasSchema(detName+"::tdc")) {
                det.banks.add(detName+"::tdc");
                this.banksToTables.put(detName+":tadc",tableName);
            }
            this.detectors.put(detName,det);
        }
    }

    private final void initDetectors(List<String> detectorNames) {
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
        System.out.println(man.get(11014, man.tableNames.get(0),"sector",99,22,33,44));
        System.out.println(Arrays.toString(man.get(11014, man.tableNames.get(0),99,22,33,44)));
        System.out.println(Arrays.toString(man.get(11014, man.tableNames.get(0),3,5,320,0)));
    }

}
