package org.jlab.service.swaps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.io.base.DataBank;
import org.jlab.jnp.hipo4.data.SchemaFactory;

/**
 *
 * Modifies ADC/TDC banks based on two CCDB timestamps, the standard one for
 * ReconstructionEngine and this one's previousTimestamp.  The result is HIPO
 * files decoded with previousTimestamp get modified as if they were decoded
 * timestamp.
 * 
 *@author baltzell
 */
public class SwapEngine extends ReconstructionEngine {

    public static final String CURRENT_CCDB_VARIATION = "swaps";
    public static final String PREVIOUS_CCDB_VARIATION = "default";

	public static final String[] DET_NAMES = {"FTCAL","FTHODO","FTTRK","LTCC","ECAL","FTOF","HTCC","DC","CTOF","CND","BST","RF","BMT","FMT","RICH","HEL","BAND","RTPC"};

    SwapManager swapman = null;
    ConstantsManager previousConman = new ConstantsManager();
    List<String> detectorNames = new ArrayList<>();
    List<Detector> detectors = new ArrayList<>();
    List<String> tableNames = new ArrayList<>();

    private class Detector {
        String name;
        String table;
        public List<String> banks;
        public Detector(String name,String table) {
            this.name = name;
            this.table = table;
        }
    }
    
    public SwapEngine(String name){
        super(name,"baltzell","1.0");
    }

    private void updateBank(int run,String tableName,DataBank bank) {
        final int nvars = bank.getDescriptor().hasEntry("order") ? 4 : 3;
        final int type = bank.getDescriptor().getName().contains("adc") ? SwapManager.ADC : SwapManager.TDC;
        for (int irow=0; irow<bank.rows(); irow++) {
            int[] slco = new int[nvars];
            slco[0] = (int)bank.getByte("sector",irow);
            slco[1] = (int)bank.getByte("layer",irow);
            slco[2] = (int)bank.getShort("component",irow);
            if (nvars==4) slco[3] = (int)bank.getByte("order",irow);
            bank.setByte("sector",irow,(byte)swapman.get(type,run,tableName,"sector",slco));
            bank.setByte("layer",irow,(byte)swapman.get(type,run,tableName,"layer",slco));
            bank.setShort("component",irow,(short)swapman.get(type,run,tableName,"component",slco));
            if (nvars==4) bank.setByte("order",irow,(byte)swapman.get(type,run,tableName,"order",slco));
        }
    }
    
    @Override
    public boolean processDataEvent(DataEvent event) {
        DataBank bank = event.getBank("RUN::config");
        int run = bank.getInt("run",0);
        for (int idet=0; idet<detectors.size(); idet++) {
            for (int ibank=0; ibank<detectors.get(idet).banks.size(); ibank++) {
                bank = event.getBank(detectors.get(idet).banks.get(ibank));
                event.removeBank(detectors.get(idet).banks.get(ibank));
                this.updateBank(run,detectors.get(idet).table,bank);
                event.appendBank(bank);
            }
        }
        return true;
    }

    private void initNames() {
        SchemaFactory schema = new SchemaFactory();
        schema.initFromDirectory(System.getenv("CLAS12DIR")+"/etc/bankdefs/hipo4");
        for (String detName : this.detectorNames) {
            // some detectors broke the bank/table naming convention:
            String tableName = detName.equals("BST") ? "/daq/tt/svt" : "/daq/tt/"+detName.toLowerCase();
            Detector det = new Detector(detName,tableName);
            this.tableNames.add(tableName);
            if (schema.hasSchema(detName+":adc")) {
                det.banks.add(detName+":adc");
            }
            if (schema.hasSchema(detName+":tdc")) {
                det.banks.add(detName+":tdc");
            }
            this.detectors.add(det);
        }
    }

    private boolean initDetectors(String userDets) {
        List<String> allDets = Arrays.asList(DET_NAMES);
        if (userDets == null) {
            System.out.println("["+this.getName()+"] --> No detectors specified in YAML, assuming all.");
            this.detectorNames.addAll(allDets);
        }
        else {
            for (String userDet : userDets.split(",")) {
                if (allDets.contains(userDet)) {
                    this.detectorNames.add(userDet);
                }
                else {
                    System.err.println("["+this.getName()+"] --> Invalid detector name from YAML:  "+userDet);
                    System.err.println("["+this.getName()+"] --> Valid detector names:  "+String.join(",",allDets));
                    return false;
                }
            }
        }
        return true;
    }
   
    @Override
    public boolean init() {

        // to protect from interfering with ongoing decoding, currently we use
        // a special variation just for implementing service-time swaps, and so
        // a user-defined variation is not allowed:
        if (this.getEngineConfigString("variation") != null) {
            System.err.println("["+this.getName()+"] --> SwapEngine does not honor variation in YAML.");
            return false;
        }

        // get timestamp for old translation tables:
        String previousTimestamp = this.getEngineConfigString("previousTimestamp");
        if (previousTimestamp == null) {
            System.err.println("["+this.getName()+"] --> Missing previousTimestamp in YAML.");
            return false;
        }

        // select detector list:
        if (!this.initDetectors(this.getEngineConfigString("detectors"))) {
            return false;
        }
       
        // initialize the bank and translation table names that will be used:
        this.initNames();

        // initialize old translation tables:
        this.previousConman.setTimeStamp(previousTimestamp);
        this.previousConman.setVariation(PREVIOUS_CCDB_VARIATION);
        this.previousConman.init(tableNames);

        // initialize new translation tables:
        requireConstants(tableNames);
        this.getConstantsManager().setVariation(CURRENT_CCDB_VARIATION);

        // initialize the swapper:
        this.swapman = new SwapManager(tableNames,previousConman,this.getConstantsManager());
        
        System.out.println("["+this.getName()+"] --> swaps are ready....");
        return true;
    }
    
}
