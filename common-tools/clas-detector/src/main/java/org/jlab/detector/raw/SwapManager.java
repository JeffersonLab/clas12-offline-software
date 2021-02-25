package org.jlab.detector.raw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jlab.detector.base.DetectorType;
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
    public static final DetectorType[] DEF_DETECTOR_NAMES = {
        DetectorType.FTCAL,
        DetectorType.FTHODO,
        DetectorType.FTTRK,
        DetectorType.LTCC,
        DetectorType.ECAL,
        DetectorType.FTOF,
        DetectorType.HTCC,
        DetectorType.DC,
        DetectorType.CTOF,
        DetectorType.CND,
        DetectorType.BST,
        DetectorType.RF,
        DetectorType.BMT,
        DetectorType.FMT,
        DetectorType.RICH,
        DetectorType.HEL,
        DetectorType.BAND,
        DetectorType.RTPC
    };

    private final HashMap<Integer,HashMap<String,SwapTable>> swaps = new HashMap<>();

    private final Map<String,String> banksToTables = new HashMap<>();
    private final Map<DetectorType,String> detsToTables = new HashMap<>();
    private final Map<DetectorType,List<String>> detsToBanks = new HashMap<>();

    private ConstantsManager prevConman = null;
    private ConstantsManager currConman = null;
    private static SwapManager instance = null;

    private SwapManager() {
    }
    
    public static SwapManager getInstance() {
        if (instance == null) {
            instance = new SwapManager();
        }
        return instance;
    }

    /**
     * @return list of all registered detector names 
     */
    public Set<DetectorType> getDetectors() {
        return this.detsToBanks.keySet();
    }
    
    /**
     * @return list of all registered CCDB table names 
     */
    public List<String> getTables() {
        return new ArrayList<>(this.detsToTables.values());
    }
    
    /**
     * @param detector
     * @return corresponding CCDB table name 
     */
    public String getTable(DetectorType detector) {
        return this.detsToTables.get(detector);
    }
    
    /**
     * @param detector
     * @return list of corresponding ADC/TDC bank names 
     */
    public List<String> getBanks(DetectorType detector) {
        return this.detsToBanks.get(detector);
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
    public final void initialize(List<String> detectorNames, String prevTimestamp,String currTimestamp) {
        this.initDetectorsByString(detectorNames);
        this.prevConman = new ConstantsManager();
        this.currConman = new ConstantsManager();
        this.prevConman.setTimeStamp(prevTimestamp);
        this.prevConman.setVariation(DEF_PREVIOUS_CCDB_VARIATION);
        this.prevConman.init(new ArrayList<>(this.detsToTables.values()));
        if (currTimestamp != null) this.currConman.setTimeStamp(currTimestamp);
        this.currConman.setVariation(DEF_CURRENT_CCDB_VARIATION);
        this.currConman.init(new ArrayList<>(this.detsToTables.values()));
    }

    /**
     * @param detectorNames
     * @param previous timestamp/variation used for translation tables during decoding
     * @param current timestamp/variation with correct translation tables
     */
    public final void initialize(List<String> detectorNames,ConstantsManager previous,ConstantsManager current) {
        this.initDetectorsByString(detectorNames);
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
        for (String tableName : this.detsToTables.values()) {
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
    private void initDetectorsByString(List<String> detectorNames) {
        List<DetectorType> allDets = Arrays.asList(DEF_DETECTOR_NAMES);
        List<DetectorType> thisDets = new ArrayList<>();
        if (detectorNames == null || detectorNames.isEmpty()) {
            thisDets.addAll(allDets);
        }
        else {
            for (String detectorName : detectorNames) {
                if (allDets.contains(DetectorType.getType(detectorName))) {
                    thisDets.add(DetectorType.getType(detectorName));
                }
                else {
                    throw new RuntimeException("[SwapManager] --> Invalid detector name:  "+detectorName);
                }
            }
        }

        SchemaFactory schema = new SchemaFactory();
        schema.initFromDirectory(System.getenv("CLAS12DIR")+"/etc/bankdefs/hipo4");
        for (DetectorType det : thisDets) {
            // some detectors broke the bank/table naming convention:
            String tableName = det==DetectorType.BST ? "/daq/tt/svt" : "/daq/tt/"+det.getName().toLowerCase();
            this.detsToTables.put(det,tableName);
            for (String suffix : new String[]{"::adc","::tdc"}) {
                String bankName = det.getName()+suffix;
                if (schema.hasSchema(bankName)) {
                    if (!this.detsToBanks.containsKey(det)) {
                        this.detsToBanks.put(det,new ArrayList<>());
                    }
                    this.detsToBanks.get(det).add(bankName);
                    this.banksToTables.put(bankName,tableName);
                }
            }
        }
    }

    private void initDetectorsByType(List<DetectorType> detectors) {
        List<String> dets = new ArrayList<>();
        for (DetectorType t : detectors) {
            dets.add(t.getName());
        }
        this.initDetectorsByString(dets);
    }

    public static void main(String[] args) {
        SwapManager man = new SwapManager(Arrays.asList("BMT"),"08/10/2020","10/13/2020");
        man.get(11014, man.getTable(DetectorType.BMT),"sector",3,6,8,0);
        System.out.println("SwapManager:\n"+man);
        System.out.println(man.get(11014, man.getTable(DetectorType.BMT),"sector",99,22,33,44));
        System.out.println(Arrays.toString(man.get(11014, man.getTable(DetectorType.BMT),99,22,33,44)));
        System.out.println(Arrays.toString(man.get(11014, man.getTable(DetectorType.BMT),3,5,320,0)));
    }

}
