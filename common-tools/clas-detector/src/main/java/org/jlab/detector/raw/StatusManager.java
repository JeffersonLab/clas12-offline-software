package org.jlab.detector.raw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.utils.groups.IndexedTable;

/**
 *
 * @author baltzell
 */
public class StatusManager {
  
    public final static int NOSTATUS = 999999;
    
    private ConstantsManager conman = null;
    private static StatusManager instance = null;

    private Map<DetectorType,Table> detectors = new HashMap<>();

    // since it looks like a consistent nameing convention wasn't followed, we'd need this:
    private static final Map<DetectorType,Table> DETECTORS = new HashMap<DetectorType,Table>() {{
        put(DetectorType.FTCAL, new Table("/calibration/ft/ftcal/status"));
        put(DetectorType.FTHODO,new Table("/calibration/ft/fthodo/status"));
        put(DetectorType.LTCC,  new Table("/calibration/ltcc/status"));
        put(DetectorType.ECAL,  new Table("/calibration/ec/status"));
        put(DetectorType.HTCC,  new Table("/calibration/htcc/status"));
        put(DetectorType.DC,    new Table("/calibration/dc/tracking/wire_status"));
        put(DetectorType.CTOF,  new Table("/calibration/ctof/status","status_upstream","status_downstream"));
        put(DetectorType.FTOF,  new Table("/calibration/ftof/status","status_left","status_right"));
        put(DetectorType.BST,   new Table("/calibration/cvt/status"));
    }};

    private static class Table {
        public String tableName;
        public List<String> varName = new ArrayList<>();
        public Table(String tableName,String... varName) {
            this.tableName=tableName;
            this.varName.addAll(Arrays.asList(varName));
        }
        public Table(String tableName) {
            this.tableName = tableName;
            this.varName.add("status");
        }
        @Override
        public String toString() {
            return tableName+"."+String.join("+",varName);
        }
    }

    public List<DetectorType> getDetectors() {
        return new ArrayList<>(this.detectors.keySet());
    }
   
    public static StatusManager getInstance() {
        if (instance == null) {
            instance = new StatusManager();
        }
        return instance;
    }

    public StatusManager() {
        this.conman = new ConstantsManager();
    }
   
    /**
     * @param type DetectorType for this detector
     * @param conman ConstantsManager instance to use
     */
    public StatusManager(DetectorType type,ConstantsManager conman) {
        this.conman = conman;
        this.addDetector(type);
    }
   
    /**
     * @param types
     * @param conman 
     */
    public StatusManager(List<DetectorType> types,ConstantsManager conman) {
        this.conman = conman;
        this.addDetectors(types);
    }

    public Set<String> getTables() {
        Set<String> ret = new HashSet<>();
        Iterator it = this.detectors.values().iterator();
        while (it.hasNext()) {
            ret.add(((Table)it.next()).tableName);
        }
        return ret;
    }
   
    /**
     * an example of how not to organize a CCDB table
     */
    private String getVarName(DetectorType type,int order) {
        if (type == DetectorType.FTOF) {
            switch (order) {
                case 0:
                    return "status_left";
                case 1:
                    return "status_right";
                case 2:
                    return "status_left";
                case 3:
                    return "status_right";
                default:
                    throw new RuntimeException("Unknown order "+order+" for DetectorType:  "+type);
            }
        }
        else if (type == DetectorType.CTOF) {
            switch (order) {
                case 0:
                    return "status_upstream";
                case 1:
                    return "status_downstream";
                case 2:
                    return "status_upstream";
                case 3:
                    return "status_downstream";
                default:
                    throw new RuntimeException("Unknown order "+order+" for DetectorType:  "+type);
            }
        }
        throw new RuntimeException("Unknown order "+order+" for DetectorType:  "+type);
    }
    
    public int getStatus(int run,DetectorType type,String varName,int... slc) {
        if (conman == null || !this.detectors.containsKey(type)) {
            return NOSTATUS;
        }
        IndexedTable table = conman.getConstants(run, this.detectors.get(type).tableName);
        return table.getIntValue(varName,slc);
    }

    public int getStatus(int run,DetectorType type,int... slc) {
        String varName;
        if (slc.length > 3) {
            varName = this.getVarName(type,slc[3]);
        }
        else {
            varName = this.detectors.get(type).varName.get(0);
        }
        return this.getStatus(run,type,varName,slc);
    }

    public int getStatus(int run, int... slc) {
        if (this.detectors.size() == 1) {
            return this.getStatus(run,this.getDetectors().get(0),slc);
        }
        else {
            throw new UnsupportedOperationException("");
        }
    }

    private void addDetector(DetectorType type) {
        if (DETECTORS.containsKey(type)) {
            this.detectors.put(type,DETECTORS.get(type));
            this.conman.init(Arrays.asList(DETECTORS.get(type).tableName));
        }
        else {
            throw new RuntimeException("Unknown DetectorType:  "+type);
        }
    }

    private void addDetectors(List<DetectorType> types) {
        for (DetectorType type : types) {
            this.addDetector(type);
        }
    }

    private void addAllDetectors() {
        this.addDetectors(new ArrayList<>(DETECTORS.keySet()));
    }
    
    public void setVariation(String variation) {
        this.conman.setVariation(variation);
    }
    
    public void setTimestamp(String timestamp) {
        this.conman.setTimeStamp(timestamp);
    }
  
    public void initialize(ConstantsManager conman,List<DetectorType> types) {
        this.conman = conman;
        this.addDetectors(types);
    }
    
    public void initialize(ConstantsManager conman,DetectorType type) {
        this.conman = conman;
        this.addDetector(type);
    }

    public void initialize(ConstantsManager conman) {
        this.conman = conman;
        this.addAllDetectors();
    }
    
    public static void main(String[] args) {

        StatusManager statman = new StatusManager();
        statman.addAllDetectors();
        
        for (int c=50; c<65; c++) {
            System.out.println(String.format("%d/%d/%d : %d",
                    1,1,c,statman.getStatus(17,DetectorType.FTCAL,1,1,c)));
        }
        System.out.println(String.format("%d/%d/%d/%d : %d",
                1,2,3,4,statman.getStatus(9810,DetectorType.FTOF,1,2,3,3)));
    }
}
