package org.jlab.service.swaps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataBank;

/**
 *
 * Modifies ADC/TDC banks based on two CCDB timestamps, the standard one for
 * ReconstructionEngine and this one's previousTimestamp.  The result is HIPO
 * files decoded with previousTimestamp get modified as if they were decoded
 * with the new timestamp.
 * 
 * @author baltzell
 */
public class SwapEngine extends ReconstructionEngine {

    SwapManager swapman = null;
    SwapManager.Detector[] detectors = null;

    public SwapEngine() {
        super("SwapEngine","baltzell","1.0");
    }

    private void updateBank(int run,String tableName,DataBank bank) {
        final int nvars = bank.getDescriptor().hasEntry("order") ? 4 : 3;
        final int type = bank.getDescriptor().getName().contains("adc") ? Swap.ADC : Swap.TDC;
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
        for (SwapManager.Detector det : this.detectors) {
            for (int ibank=0; ibank<det.banks.size(); ibank++) {
                bank = event.getBank(det.banks.get(ibank));
                event.removeBank(det.banks.get(ibank));
                this.updateBank(run,det.table,bank);
                event.appendBank(bank);
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

        System.out.println("["+this.getName()+"] --> Setting current variation : "+SwapManager.DEF_CURRENT_CCDB_VARIATION);
        System.out.println("["+this.getName()+"] --> Setting previous variation : "+SwapManager.DEF_PREVIOUS_CCDB_VARIATION);
        System.out.println("["+this.getName()+"] --> Setting current timestamp : "+currentTimestamp);
        System.out.println("["+this.getName()+"] --> Setting previous timestamp : "+previousTimestamp);
        System.out.println("["+this.getName()+"] --> Setting detectors : "+this.getEngineConfigString("detectors"));

        this.swapman = new SwapManager(dets,previousTimestamp,currentTimestamp);
        this.detectors = this.swapman.getDetectors();
       
        System.out.println("["+this.getName()+"] --> swaps are ready....");
        return true;
    }

}
