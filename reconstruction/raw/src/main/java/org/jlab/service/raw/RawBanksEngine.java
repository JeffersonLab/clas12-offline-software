package org.jlab.service.raw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.raw.StatusManager;
import org.jlab.detector.raw.SwapManager;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataBank;

/**
 *
 * Initializes StatusManager singleton based on this engine's standard
 * variation and timestamp.
 * 
 * Initializes SwapManager singleton based on two CCDB timestamps to allow
 * fixing cable swaps in decoded HIPO files.  Optionally modifies ADC/TDC banks.
 * 
 * Then, downstream, can do:
 * 
 * int status = StatusManager.getInstance().getStatus(...)
 * int[] slco = SwapManager.getInstance().get(...)
 * 
 * @author baltzell
 */
public class RawBanksEngine extends ReconstructionEngine {

    private StatusManager statman = null;
    private SwapManager swapman = null;
    private boolean modifyBanks = false;

    public RawBanksEngine() {
        super("SwapEngine","baltzell","1.0");
    }

    private void updateBank(int run,String tableName,DataBank bank) {
        for (int irow=0; irow<bank.rows(); irow++) {
            int[] slco = SwapManager.getInstance().get(run,bank,irow);
            bank.setByte("sector",irow,(byte)swapman.get(run,tableName,"sector",slco));
            bank.setByte("layer",irow,(byte)swapman.get(run,tableName,"layer",slco));
            bank.setShort("component",irow,(short)swapman.get(run,tableName,"component",slco));
            bank.setByte("order",irow,(byte)swapman.get(run,tableName,"order",slco));
        }
    }

    @Override
    public boolean processDataEvent(DataEvent event) {
        if (modifyBanks) {
            DataBank bank = event.getBank("RUN::config");
            final int run = bank.getInt("run",0);
            for (DetectorType detector : this.swapman.getDetectors()) {
                for (String bankName : this.swapman.getBanks(detector)) {
                    bank = event.getBank(bankName);
                    event.removeBank(bankName);
                    this.updateBank(run,this.swapman.getTable(detector),bank);
                    event.appendBank(bank);
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
        
        // get timestamp for new translation tables:
        String currentTimestamp = this.getEngineConfigString("timestamp");

        // select detector list, initialize bank and translation table names:
        List<String> dets = new ArrayList<>();
        if (this.getEngineConfigString("detectors") == null) {
            System.out.println("["+this.getName()+"] --> No detectors specified in YAML, assuming all.");
        }
        else {
            dets.addAll(Arrays.asList(this.getEngineConfigString("detectors").split(",")));
        }

        if (this.getEngineConfigString("modifyBanks") != null) {
            if (this.getEngineConfigString("modifyBanks").equals("true")) {
                this.modifyBanks = true;
            }
        }

        System.out.println("["+this.getName()+"] --> Setting current variation : "+SwapManager.DEF_CURRENT_CCDB_VARIATION);
        System.out.println("["+this.getName()+"] --> Setting previous variation : "+SwapManager.DEF_PREVIOUS_CCDB_VARIATION);
        System.out.println("["+this.getName()+"] --> Setting current timestamp : "+currentTimestamp);
        System.out.println("["+this.getName()+"] --> Setting previous timestamp : "+previousTimestamp);
        System.out.println("["+this.getName()+"] --> Setting detectors : "+this.getEngineConfigString("detectors"));
        if (this.modifyBanks) {
            System.out.println("["+this.getName()+"] --> Modifying ADC/TDC banks!");
        }

        this.swapman = SwapManager.getInstance();
        this.swapman.initialize(dets,previousTimestamp,currentTimestamp);
      
        this.statman = StatusManager.getInstance();
        this.statman.initialize(this.getConstantsManager());

        // and then anywhere:
        //StatusManager.getInstance().getStatus(100301, DetectorType.RTPC, 1,4,3);
        
        System.out.println("["+this.getName()+"] --> swaps are ready....");
        return true;
    }

}
