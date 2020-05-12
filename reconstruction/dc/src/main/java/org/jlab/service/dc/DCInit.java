package org.jlab.service.dc;

import cnuphys.snr.NoiseReductionParameters;
import cnuphys.snr.clas12.Clas12NoiseAnalysis;
import cnuphys.snr.clas12.Clas12NoiseResult;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.jlab.clas.swimtools.Swim;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.dc.Constants;
import org.jlab.rec.dc.banks.HitReader;
import org.jlab.rec.dc.banks.RecoBankWriter;
import org.jlab.rec.dc.cluster.ClusterCleanerUtilities;
import org.jlab.rec.dc.cluster.ClusterFinder;
import org.jlab.rec.dc.cluster.ClusterFitter;
import org.jlab.rec.dc.cluster.FittedCluster;
import org.jlab.rec.dc.hit.FittedHit;
import org.jlab.rec.dc.hit.Hit;
import org.jlab.rec.dc.timetodistance.TableLoader;
import org.jlab.utils.groups.IndexedTable;

/**
 * @author zigler
 * 
 * Reads either TDC or NN hit bank
 * Loads the Tables for the rest of the tracking
 * Creates regular banks or banks storing AI assisted output
 */
public class DCInit extends DCEngine {
    //some identifier for the type of clustering,
    //ability to plug in more than once
    private AtomicInteger Run = new AtomicInteger(0);
    private double triggerPhase;
    private int newRun = 0;

    public DCInit() {
        super("DCI");
    }

    @Override
    public boolean init() {
        // Load cuts
        Constants.Load();
        super.setOptions();
        super.LoadTables();
        return true;
    }

    @Override
    public boolean processDataEvent(DataEvent event) {
        if (!event.hasBank("RUN::config")) {
            return true;
        }

       DataBank bank = event.getBank("RUN::config");
       long timeStamp = bank.getLong("timestamp", 0);
       double triggerPhase = 0;

        // Load the constants
        //-------------------
        int newRun = bank.getInt("run", 0);
        if (newRun == 0)
           return true;

        if (Run.get() == 0 || (Run.get() != 0 && Run.get() != newRun)) {
           if (timeStamp == -1)
               return true;
 //          if (debug.get()) startTime = System.currentTimeMillis();
           IndexedTable tabJ = super.getConstantsManager().getConstants(newRun, Constants.TIMEJITTER);
           double period = tabJ.getDoubleValue("period", 0, 0, 0);
           int phase = tabJ.getIntValue("phase", 0, 0, 0);
           int cycles = tabJ.getIntValue("cycles", 0, 0, 0);

           if (cycles > 0) triggerPhase = period * ((timeStamp + phase) % cycles);

           TableLoader.FillT0Tables(newRun, super.variationName);
           TableLoader.Fill(super.getConstantsManager().getConstants(newRun, Constants.TIME2DIST));

           Run.set(newRun);
           if (event.hasBank("MC::Particle") && this.getEngineConfigString("wireDistort")==null) {
               Constants.setWIREDIST(0);
           }

 //          if (debug.get()) System.out.println("NEW RUN INIT = " + (System.currentTimeMillis() - startTime));
       }

        return true;
    }

}
