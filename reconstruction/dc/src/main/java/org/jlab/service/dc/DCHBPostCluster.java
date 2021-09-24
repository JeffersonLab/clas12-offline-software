package org.jlab.service.dc;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.banks.Banks;

/**
 * @author zigler
 */
public class DCHBPostCluster extends DCEngine {

    private double triggerPhase;
    private Banks  bankNames = new Banks();
    
    public DCHBPostCluster(String trking) {
        super(trking);
        this.initBankNames();
    }

    public void initBankNames() {
        //Initialize bank names
    }

    @Override
    public boolean init() {
        super.LoadTables();
        return true;
    }

    public double getTriggerPhase() {
        return triggerPhase;
    }

    public void setTriggerPhase(double triggerPhase) {
        this.triggerPhase = triggerPhase;
    }

    public int getRun(DataEvent event) {
        if (!event.hasBank("RUN::config")) {
            return 0;
        }
        DataBank bank = event.getBank("RUN::config");
        if(Constants.DEBUG)
            System.out.println("EVENT "+bank.getInt("event", 0));
        
        int run = bank.getInt("run", 0);
        return run;
    }

    public Banks getBankNames() {
        return bankNames;
    }

    public void setBankNames(Banks bankNames) {
        this.bankNames = bankNames;
    }

    
    
}
