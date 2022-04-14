package org.jlab.service.htcc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.rec.htcc.HTCCReconstruction;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
/**
 *
 * @author gavalian
 */
public class HTCCReconstructionService extends ReconstructionEngine{

    public static Logger LOGGER = Logger.getLogger(HTCCReconstructionService.class.getName());

    public HTCCReconstructionService(){
        super("HTCC","henkins","1.0");
    }
    
    @Override
    public boolean processDataEvent(DataEvent event) {
        int runNo = 10;

        if(event.hasBank("RUN::config")==true){
            DataBank bank = event.getBank("RUN::config");
            runNo = bank.getInt("run", 0);
        }
       
        try {
            HTCCReconstruction reco = new HTCCReconstruction();
              reco.gain       = this.getConstantsManager().getConstants(runNo, "/calibration/htcc/gain");
              reco.time       = this.getConstantsManager().getConstants(runNo, "/calibration/htcc/time");
              reco.ring_time  = this.getConstantsManager().getConstants(runNo, "/calibration/htcc/ring_time");
              reco.cluster_par= this.getConstantsManager().getConstants(runNo, "/calibration/htcc/cluster");
              reco.status     = this.getConstantsManager().getConstants(runNo, "/calibration/htcc/status");
              reco.geometry   = this.getConstantsManager().getConstants(runNo, "/geometry/htcc/htcc");
              reco.processEvent(event);
        } catch (Exception e){
            LOGGER.log(Level.SEVERE,"----> error with HTCC reconstruction..");
            e.printStackTrace();
        }

      
        return true;
    }

    @Override
    public boolean init() {
        

            String[]  htccTables = new String[]{
            "/calibration/htcc/gain", 
            "/calibration/htcc/time", 
            "/calibration/htcc/ring_time", 
            "/calibration/htcc/cluster", 
            "/calibration/htcc/status", 
            "/geometry/htcc/htcc", 
    
        };
            
        this.registerOutputBank("HTCC::rec");
        
        requireConstants(Arrays.asList(htccTables));
        return true;
    }

   
    
}
