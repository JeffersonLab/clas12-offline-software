package org.jlab.rec.eb;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.geom.prim.Vector3D;

/**
 *
 * @author baltzell
 */
public class EBCCDBConstants {

    private int currentRun = -1;
    private boolean isLoaded = false;
    
    private static final String EB_TABLE_PREFIX="/calibration/eb/";

    private static final String[] EB_TABLE_NAMES={
            "electron_sf",
            "photon_sf",
            "neutron_beta",
            "cnd_neutron_beta",
            "pid_cuts",
            "ecal_matching",
            "ftof_matching",
            "ctof_matching",
            "cnd_matching",
            "htcc_matching",
            "ltcc_matching",
            "rf/config",
            "rf/offset",
            "rf/jitter"
    };
    
    private static final String[] OTHER_TABLE_NAMES={
        "/runcontrol/fcup",
        "/runcontrol/hwp",
        "/runcontrol/helicity",
        "/geometry/target",
        "/calibration/ftof/tres",
        //"/calibration/ctof/tres"
    };

    public static List <String> getAllTableNames() {
        List <String> ret=new ArrayList <>();
        for (String ss : EB_TABLE_NAMES) ret.add(EB_TABLE_PREFIX+ss);
        ret.addAll(Arrays.asList(OTHER_TABLE_NAMES));
        return ret;
    }

    private final Map <String,IndexedTable> tables = new HashMap<>();
    private final Map <EBCCDBEnum,Double> dbDoubles = new HashMap<>();
    private final Map <EBCCDBEnum,Integer> dbIntegers = new HashMap<>();
    private final Map <EBCCDBEnum,Vector3D> dbVector3Ds = new HashMap<>();
    private final Map <EBCCDBEnum,Double[]> dbArrays = new HashMap<>();
    private final Map <EBCCDBEnum, Map <Integer,Double[]>> dbSectorArrays = new HashMap<>();

    // fill maps:
    private void setDouble(EBCCDBEnum key,Double value) {
        dbDoubles.put(key,value);
    }
    private void setVector3D(EBCCDBEnum key,Vector3D value) {
        dbVector3Ds.put(key,value);
    }
    private void setInteger(EBCCDBEnum key,int value) {
        dbIntegers.put(key,value);
    }
    private void setArray(EBCCDBEnum key,Double[] value) {
        dbArrays.put(key,value);
    }
    private void setSectorArray(EBCCDBEnum key, Integer sector, Double[] value) {
        if (!dbSectorArrays.containsKey(key)) dbSectorArrays.put(key,new HashMap<>());
        dbSectorArrays.get(key).put(sector,value);
    }
    
    // read maps:
    public double getDouble(EBCCDBEnum key) {
        if (!dbDoubles.containsKey(key)) 
            throw new RuntimeException("Missing Double Key:  "+key);
        return dbDoubles.get(key);
    }
    public Vector3D getVector3D(EBCCDBEnum key) {
        if (!dbVector3Ds.containsKey(key)) 
            throw new RuntimeException("Missing Vector3D Key:  "+key);
        return dbVector3Ds.get(key);
    }
    public int getInteger(EBCCDBEnum key) {
        if (!dbIntegers.containsKey(key)) 
            throw new RuntimeException("Missing Integer Key:  "+key);
        return dbIntegers.get(key);
    }
    public Double[] getArray(EBCCDBEnum key) {
        if (!dbArrays.containsKey(key)) 
            throw new RuntimeException("Missing Integer Key:  "+key);
        return dbArrays.get(key);
    }
    public Double[] getSectorArray(EBCCDBEnum key,int sector) {
        if (!dbSectorArrays.containsKey(key))
            throw new RuntimeException("Missing Integer Key:  "+key);
        if (!dbSectorArrays.get(key).containsKey(sector))
            throw new RuntimeException("Missing Integer Key:  "+sector);
        return dbSectorArrays.get(key).get(sector);
    }
    public Double getSectorDouble(EBCCDBEnum key, int sector) {
        return getSectorArray(key,sector)[0];
    }

    public IndexedTable getTable(String tableName) {
        if (tables.containsKey(tableName)) return tables.get(tableName);
        else return null;
    }

    // read ccdb tables:
    private void loadTable(
            int run,
            ConstantsManager manager, 
            String fullTableName) {
        tables.put(fullTableName,manager.getConstants(run,fullTableName));
    }
    private void loadEbTable(
            int run,
            ConstantsManager manager, 
            String shortTableName) {
        tables.put(shortTableName,manager.getConstants(run,EB_TABLE_PREFIX+shortTableName));
    }

    // read ccdb values, fill maps:
    private void loadDouble(
            EBCCDBEnum key,
            String tableName,
            String columnName,
            int sector,int layer,int component) {
        double value=tables.get(tableName).getDoubleValue(columnName,sector,layer,component);
        setDouble(key,value);
    }
    private void loadInteger(
            EBCCDBEnum key,
            String tableName,
            String columnName,
            int sector,int layer,int component) {
        int value=tables.get(tableName).getIntValue(columnName,sector,layer,component);
        setInteger(key,value);
    }
    private void loadVector3D(
            EBCCDBEnum key,
            String tableName,
            String colName1, String colName2, String colName3,
            int sector,int layer,int component) {
        double val1=tables.get(tableName).getDoubleValue(colName1,sector,layer,component);
        double val2=tables.get(tableName).getDoubleValue(colName2,sector,layer,component);
        double val3=tables.get(tableName).getDoubleValue(colName3,sector,layer,component);
        setVector3D(key,new Vector3D(val1,val2,val3));
    }
    private void loadArray(
            EBCCDBEnum key,
            String tableName,
            String[] colNames,
            int sector,int layer,int component) {
        Double vals[]=new Double[colNames.length];
        for (int ii=0; ii<colNames.length; ii++)
            vals[ii]=tables.get(tableName).getDoubleValue(colNames[ii],sector,layer,component);
        setArray(key,vals);
    }
    private void loadSectorArray(
            EBCCDBEnum key,
            String tableName,
            String[] colNames,
            int sector) {
        Double vals[]=new Double[colNames.length];
        for (int ii=0; ii<colNames.length; ii++)
            vals[ii]=tables.get(tableName).getDoubleValue(colNames[ii],sector,0,0);
        setSectorArray(key,sector,vals);
    }
    private void loadSectorsArrays(
            EBCCDBEnum key,
            String tableName,
            String[] colNames) {
        for (int ii=0; ii<=6; ii++) {
            this.loadSectorArray(key,tableName,colNames,ii);
        }
    }
    private void loadSectorDouble(EBCCDBEnum key,String tableName,String colName) {
        loadSectorsArrays(key,tableName,new String[]{colName});
    }
    
    public void show() {
        System.out.println("EBCCDBConstants:  show()");
        for (EBCCDBEnum ii : dbIntegers.keySet()) {
            System.out.println(String.format("%-30s: %d",ii,dbIntegers.get(ii)));
        }
        for (EBCCDBEnum ii : dbDoubles.keySet()) {
            System.out.println(String.format("%-30s: %f",ii,dbDoubles.get(ii)));
        }
        for (EBCCDBEnum ii : dbArrays.keySet()) {
            System.out.print(String.format("%-30s: ",ii));
            for (double xx : dbArrays.get(ii)) System.out.print(xx+" , ");
            System.out.println();
        }
        for (EBCCDBEnum ii : dbVector3Ds.keySet()) {
           System.out.println(String.format("%-30s",ii)+": "+dbVector3Ds.get(ii));
        }
        for (EBCCDBEnum ii : dbSectorArrays.keySet()) {
            for (int sector : dbSectorArrays.get(ii).keySet()) {
                System.out.print(String.format("%-30s: %d ",ii,sector));
                for (double xx : dbSectorArrays.get(ii).get(sector)) {
                    System.out.print(xx+" , ");
                }
                System.out.println();
            }
        }
    }

    public final void load(int run,ConstantsManager manager) {

        // load /calibration/eb tables:
        for (String ss : EB_TABLE_NAMES) loadEbTable(run,manager,ss);

        //load non-/calibration/eb tables:
        for (String ss : OTHER_TABLE_NAMES) loadTable(run,manager,ss);

        String[] sf ={"sf1", "sf2", "sf3", "sf4"};
        String[] sfs={"sfs1","sfs2","sfs3","sfs4"};
        loadSectorsArrays(EBCCDBEnum.ELEC_SF,"electron_sf",sf);
        loadSectorsArrays(EBCCDBEnum.PHOT_SF,"photon_sf",  sf);
        loadSectorsArrays(EBCCDBEnum.ELEC_SFS,"electron_sf",sfs);
        loadSectorsArrays(EBCCDBEnum.PHOT_SFS,"photon_sf",  sfs);

        loadDouble(EBCCDBEnum.PCAL_MATCHING,"ecal_matching","dr2",0,1,0);
        loadDouble(EBCCDBEnum.ECIN_MATCHING,"ecal_matching","dr2",0,4,0);
        loadDouble(EBCCDBEnum.ECOUT_MATCHING,"ecal_matching","dr2",0,7,0);
        loadDouble(EBCCDBEnum.PCAL_hitRes,"ecal_matching","dr",0,1,0);
        loadDouble(EBCCDBEnum.ECIN_hitRes,"ecal_matching","dr",0,4,0);
        loadDouble(EBCCDBEnum.ECOUT_hitRes,"ecal_matching","dr",0,7,0);
        loadDouble(EBCCDBEnum.PCAL_TimingRes,"ecal_matching","dt",0,1,0);
        loadDouble(EBCCDBEnum.ECIN_TimingRes,"ecal_matching","dt",0,4,0);
        loadDouble(EBCCDBEnum.ECOUT_TimingRes,"ecal_matching","dt",0,7,0);

        final String[] ts={"t1","t2","t3","t4"};
        loadArray(EBCCDBEnum.FTOF1A_TimingRes,"ftof_matching",ts,0,1,0);
        loadArray(EBCCDBEnum.FTOF1B_TimingRes,"ftof_matching",ts,0,2,0);
        loadArray(EBCCDBEnum.FTOF2_TimingRes,"ftof_matching",ts,0,0,0);
        loadDouble(EBCCDBEnum.FTOF_MATCHING_1A,"ftof_matching","dx",0,1,0);
        loadDouble(EBCCDBEnum.FTOF_MATCHING_1B,"ftof_matching","dx",0,2,0);
        loadDouble(EBCCDBEnum.FTOF_MATCHING_2,"ftof_matching","dx",0,0,0);
        loadVector3D(EBCCDBEnum.FTOF1A_hitRes,"ftof_matching","dx","dy","dz",0,1,0);
        loadVector3D(EBCCDBEnum.FTOF1B_hitRes,"ftof_matching","dx","dy","dz",0,2,0);
        loadVector3D(EBCCDBEnum.FTOF2_hitRes,"ftof_matching","dx","dy","dz",0,0,0);

        loadDouble(EBCCDBEnum.HTCC_TimingRes,"htcc_matching","dt",0,0,0);
        loadDouble(EBCCDBEnum.HTCC_NPHE_CUT,"htcc_matching","nphe",0,0,0);
        loadDouble(EBCCDBEnum.HTCC_DTHETA,"htcc_matching","dtheta",0,0,0);
        loadDouble(EBCCDBEnum.HTCC_DPHI,"htcc_matching","dphi",0,0,0);

        loadArray(EBCCDBEnum.CTOF_TimingRes,"ctof_matching",ts,0,0,0);
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

        loadDouble(EBCCDBEnum.CND_NEUTRON_maxBeta,"cnd_neutron_beta","neutron_beta",0,0,0);

        loadDouble(EBCCDBEnum.NEUTRON_maxBeta,"neutron_beta","neutron_beta",0,0,0);
        
        loadDouble(EBCCDBEnum.TARGET_POSITION,"/geometry/target","position",0,0,0);
   
        loadDouble(EBCCDBEnum.FCUP_slope,"/runcontrol/fcup","slope",0,0,0);
        loadDouble(EBCCDBEnum.FCUP_offset,"/runcontrol/fcup","offset",0,0,0);
        loadDouble(EBCCDBEnum.FCUP_atten,"/runcontrol/fcup","atten",0,0,0);
        loadInteger(EBCCDBEnum.HWP_position,"/runcontrol/hwp","hwp",0,0,0);

        loadSectorDouble(EBCCDBEnum.ELEC_SF_nsigma,"pid_cuts","e_sf_nsigma");
        loadSectorDouble(EBCCDBEnum.ELEC_PCAL_min_energy,"pid_cuts","e_pcal_energy");
        
        //loadDouble(EBCCDBEnum.HTCC_PION_THRESHOLD,
        //loadDouble(EBCCDBEnum.LTCC_PION_THRESHOLD,
        //loadDouble(EBCCDBEnum.LTCC_KAON_THRESHOLD,
    
        final int rfStat1=tables.get("rf/config").getIntValue("status",1,1,1);
        final int rfStat2=tables.get("rf/config").getIntValue("status",1,1,2);
        if (rfStat1<=0 && rfStat2<=0)
            throw new RuntimeException("Couldn't find non-positive RF status in CCDB");
        final int rfId = rfStat2>rfStat1 ? 2 : 1;
        setInteger(EBCCDBEnum.RF_ID,rfId);
        loadDouble(EBCCDBEnum.RF_BUCKET_LENGTH,"rf/config","clock",1,1,rfId);
        loadDouble(EBCCDBEnum.RF_OFFSET,"rf/offset","offset",1,1,rfId);
        loadDouble(EBCCDBEnum.RF_TDC2TIME,"rf/config","tdc2time",1,1,rfId);
        loadInteger(EBCCDBEnum.RF_CYCLES,"rf/config","cycles",1,1,rfId);
        loadInteger(EBCCDBEnum.RF_JITTER_CYCLES,"rf/jitter","cycles",0,0,0);
        loadInteger(EBCCDBEnum.RF_JITTER_PHASE ,"rf/jitter","phase",0,0,0);
        loadDouble(EBCCDBEnum.RF_JITTER_PERIOD,"rf/jitter","period",0,0,0);

        loadDouble(EBCCDBEnum.HELICITY_frequency,"/runcontrol/helicity","frequency",0,0,0);
        loadDouble(EBCCDBEnum.HELICITY_tsettle,"/runcontrol/helicity","tsettle",0,0,0);
        loadDouble(EBCCDBEnum.HELICITY_tstable,"/runcontrol/helicity","tstable",0,0,0);
        loadInteger(EBCCDBEnum.HELICITY_delay,"/runcontrol/helicity","delay",0,0,0);
        loadInteger(EBCCDBEnum.HELICITY_pattern,"/runcontrol/helicity","pattern",0,0,0);
        
        //this.show();
        currentRun = run;
        isLoaded = true;
    }
    
    public boolean isLoaded() { return isLoaded; }
    public int getRunNumber() { return currentRun; }

    public EBCCDBConstants() {}

    public EBCCDBConstants(int run,ConstantsManager manager) {
        load(run,manager);
    }
}
