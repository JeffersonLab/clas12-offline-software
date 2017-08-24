package org.jlab.rec.eb;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.geom.prim.Vector3D;

public class EBCCDBConstants {

    public static boolean LOADED = false;
    
    public static final String ebTablePrefix="/calibration/eb/";

    public static final String[] ebTableNames={
            "electron_sf",
            "photon_sf",
            "neutron_beta",
            "pid",
            "ecal_matching",
            "ftof_matching",
            "ctof_matching",
            "cnd_matching",
            "htcc_matching",
            "ltcc_matching"
    };
    
    public static final String[] otherTableNames={
        "/geometry/target",
        "/calibration/rf/offset"
    };

    public static List <String> getAllTableNames() {
        List <String> ret=new ArrayList <String>();
        for (String ss : ebTableNames) ret.add(ebTablePrefix+ss);
        for (String ss : otherTableNames) ret.add(ss);
        return ret;
    }

    private static Map <String,IndexedTable> tables = new HashMap<String,IndexedTable>();
    private static Map <EBCCDBEnum,Double> dbDoubles = new HashMap<EBCCDBEnum,Double>();
    private static Map <EBCCDBEnum,Integer> dbIntegers = new HashMap<EBCCDBEnum,Integer>();
    private static Map <EBCCDBEnum,Vector3D> dbVector3Ds = new HashMap<EBCCDBEnum,Vector3D>();
    private static Map <EBCCDBEnum,Double[]> dbArrays = new HashMap<EBCCDBEnum,Double[]>();

    static EBDatabaseConstantProvider DBP = new EBDatabaseConstantProvider(10,"default");

    // fill maps:
    public static synchronized void setDouble(EBCCDBEnum key,Double value) {
        dbDoubles.put(key,value);
    }
    public static synchronized void setVector3D(EBCCDBEnum key,Vector3D value) {
        dbVector3Ds.put(key,value);
    }
    public static synchronized void setInteger(EBCCDBEnum key,int value) {
        dbIntegers.put(key,value);
    }
    public static synchronized void setArray(EBCCDBEnum key,Double[] value) {
        dbArrays.put(key,value);
    }
    
    // read maps:
    public static synchronized double getDouble(EBCCDBEnum key) {
        if (!dbDoubles.containsKey(key)) 
            throw new RuntimeException("Missing Double Key:  "+key);
        return dbDoubles.get(key);
    }
    public static synchronized Vector3D getVector3D(EBCCDBEnum key) {
        if (!dbVector3Ds.containsKey(key)) 
            throw new RuntimeException("Missing Vector3D Key:  "+key);
        return dbVector3Ds.get(key);
    }
    public static synchronized int getInteger(EBCCDBEnum key) {
        if (!dbIntegers.containsKey(key)) 
            throw new RuntimeException("Missing Integer Key:  "+key);
        return dbIntegers.get(key);
    }
    public static synchronized Double[] getArray(EBCCDBEnum key) {
        if (!dbArrays.containsKey(key)) 
            throw new RuntimeException("Missing Integer Key:  "+key);
        return dbArrays.get(key);
    }

    // read ccdb tables:
    private static synchronized void loadTable(
            int run,
            ConstantsManager manager, 
            String fullTableName) {
        tables.put(fullTableName,manager.getConstants(run,fullTableName));
    }
    private static synchronized void loadEbTable(
            int run,
            ConstantsManager manager, 
            String shortTableName) {
        tables.put(shortTableName,manager.getConstants(run,ebTablePrefix+shortTableName));
    }

    // read ccdb values, fill maps:
    private static synchronized void loadDouble(
            EBCCDBEnum key,
            String tableName,
            String columnName,
            int sector,int layer,int component) {
        double value=tables.get(tableName).getDoubleValue(columnName,sector,layer,component);
        setDouble(key,value);
    }
    private static synchronized void loadInteger(
            EBCCDBEnum key,
            String tableName,
            String columnName,
            int sector,int layer,int component) {
        int value=tables.get(tableName).getIntValue(columnName,sector,layer,component);
        setInteger(key,value);
    }
    private static synchronized void loadVector3D(
            EBCCDBEnum key,
            String tableName,
            String colName1, String colName2, String colName3,
            int sector,int layer,int component) {
        double val1=tables.get(tableName).getDoubleValue(colName1,sector,layer,component);
        double val2=tables.get(tableName).getDoubleValue(colName2,sector,layer,component);
        double val3=tables.get(tableName).getDoubleValue(colName3,sector,layer,component);
        setVector3D(key,new Vector3D(val1,val2,val3));
    }
    private static synchronized void loadArray(
            EBCCDBEnum key,
            String tableName,
            String[] colNames,
            int sector,int layer,int component) {
        Double vals[]=new Double[colNames.length];
        for (int ii=0; ii<colNames.length; ii++)
            vals[ii]=tables.get(tableName).getDoubleValue(colNames[ii],sector,layer,component);
        setArray(key,vals);
    }

    public static final synchronized void load(int run,ConstantsManager manager) {

        // load /calibration/eb tables:
        for (String ss : ebTableNames) loadEbTable(run,manager,ss);

        //load non-/calibration/eb tables:
        for (String ss : otherTableNames) loadTable(run,manager,ss);

        String[] sf ={"sf1", "sf2", "sf3", "sf4"};
        String[] sfs={"sfs1","sfs2","sfs3","sfs4"};
        loadArray(EBCCDBEnum.ELEC_SF,"electron_sf",sf,0,0,0);

        /*
        for (EBCCDBEnum key : dbArrays.keySet()) {
            for (Double dd : dbArrays.get(key)) {
                System.err.println(key+" "+dd);
            }
        }
        */

        loadArray(EBCCDBEnum.PHOT_SF,"photon_sf",  sf,0,0,0);
        loadArray(EBCCDBEnum.ELEC_SFS,"electron_sf",sfs,0,0,0);
        loadArray(EBCCDBEnum.PHOT_SFS,"photon_sf",  sfs,0,0,0);

        loadDouble(EBCCDBEnum.PCAL_MATCHING,"ecal_matching","dr2",0,1,0);
        loadDouble(EBCCDBEnum.ECIN_MATCHING,"ecal_matching","dr2",0,4,0);
        loadDouble(EBCCDBEnum.ECOUT_MATCHING,"ecal_matching","dr2",0,7,0);
        loadDouble(EBCCDBEnum.PCAL_hitRes,"ecal_matching","dr",0,1,0);
        loadDouble(EBCCDBEnum.ECIN_hitRes,"ecal_matching","dr",0,4,0);
        loadDouble(EBCCDBEnum.ECOUT_hitRes,"ecal_matching","dr",0,7,0);
        loadDouble(EBCCDBEnum.PCAL_TimingRes,"ecal_matching","dt",0,1,0);
        loadDouble(EBCCDBEnum.ECIN_TimingRes,"ecal_matching","dt",0,4,0);
        loadDouble(EBCCDBEnum.ECOUT_TimingRes,"ecal_matching","dt",0,7,0);

        String[] ts={"t1","t2","t3","t4"};
        loadArray(EBCCDBEnum.FTOF1A_TimingRes,"ftof_matching",ts,0,1,0);
        loadArray(EBCCDBEnum.FTOF1B_TimingRes,"ftof_matching",ts,0,2,0);
        loadArray(EBCCDBEnum.FTOF2_TimingRes,"ftof_matching",ts,0,0,0);
        loadDouble(EBCCDBEnum.FTOF_MATCHING_1A,"ftof_matching","dx",0,1,0);
        loadDouble(EBCCDBEnum.FTOF_MATCHING_1B,"ftof_matching","dx",0,2,0);
        loadDouble(EBCCDBEnum.FTOF_MATCHING_2,"ftof_matching","dx",0,0,0);
        loadVector3D(EBCCDBEnum.FTOF1A_hitRes,"ftof_matching","dx","dy","dz",0,1,0);
        loadVector3D(EBCCDBEnum.FTOF1B_hitRes,"ftof_matching","dx","dy","dz",0,2,0);
        loadVector3D(EBCCDBEnum.FTOF2_hitRes,"ftof_matching","dx","dy","dz",0,3,0);

        loadDouble(EBCCDBEnum.HTCC_TimingRes,"htcc_matching","dt",0,0,0);
        loadDouble(EBCCDBEnum.HTCC_NPHE_CUT,"htcc_matching","nphe",0,0,0);
        loadDouble(EBCCDBEnum.HTCC_DTHETA,"htcc_matching","dtheta",0,0,0);
        loadDouble(EBCCDBEnum.HTCC_DPHI,"htcc_matching","dphi",0,0,0);

        loadDouble(EBCCDBEnum.LTCC_TimingRes,"ltcc_matching","dt",0,0,0);
        loadDouble(EBCCDBEnum.LTCC_NPHE_CUT,"ltcc_matching","nphe",0,0,0);
        loadDouble(EBCCDBEnum.LTCC_DTHETA,"ltcc_matching","dtheta",0,0,0);
        loadDouble(EBCCDBEnum.LTCC_DPHI,"ltcc_matching","dphi",0,0,0);

        loadDouble(EBCCDBEnum.CTOF_DR,"ctof_matching","dr",0,0,0);
        loadDouble(EBCCDBEnum.CTOF_DPHI,"ctof_matching","dphi",0,0,0);
        loadDouble(EBCCDBEnum.CTOF_DZ,"ctof_matching","dz",0,0,0);
        
        loadDouble(EBCCDBEnum.CND_DR,"cnd_matching","dr",0,0,0);
        loadDouble(EBCCDBEnum.CND_DPHI,"cnd_matching","dphi",0,0,0);
        loadDouble(EBCCDBEnum.CND_DZ,"cnd_matching","dz",0,0,0);

        //oadDouble(EBCCDBEnum.HTCC_PION_THRESHOLD,
        //loadDouble(EBCCDBEnum.LTCC_LOWER_PION_THRESHOLD,
        //loadDouble(EBCCDBEnum.LTCC_UPPER_PION_THRESHOLD,
        
        loadDouble(EBCCDBEnum.TARGET_POSITION,"/geometry/target","position",0,0,0);
    
        //loadDouble(EBCCDBEnum.RF_BUCKET_LENGTH,
        //loadDouble(EBCCDBEnum.RF_OFFSET,
        //loadDouble(EBCCDBEnum.RF_TDC2TIME,
    
        //loadDouble(EBCCDBEnum.RF_CYCLES,
        //loadDouble(EBCCDBEnum.RF_ID,
        //loadDouble(EBCCDBEnum.RF_LARGE_INTEGER,
        //
        //loadDouble(EBCCDBEnum.TRIGGER_ID,

        loadDouble(EBCCDBEnum.NEUTRON_maxBeta,"neutron_beta","neutron_beta",0,0,0);

        LOADED = true;
        setDB(DBP);
    }
    
    private static EBDatabaseConstantProvider DB;
    public static final EBDatabaseConstantProvider getDB() { return DB; }
    public static final void setDB(EBDatabaseConstantProvider db) { DB=db; }
}
