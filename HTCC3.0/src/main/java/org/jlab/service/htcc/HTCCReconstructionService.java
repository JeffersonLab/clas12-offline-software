/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.htcc;
import java.util.ArrayList;
import java.util.Arrays;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.htcc.HTCCReconstruction;
import org.jlab.utils.groups.IndexedTable;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
/**
 *
 * @author gavalian
 */
public class HTCCReconstructionService extends ReconstructionEngine{

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
              reco.gain  = this.getConstantsManager().getConstants(runNo, "/calibration/htcc/gain");
              reco.processEvent(event);
        } catch (Exception e){
            System.out.println("----> error with HTCC reconstruction..");
            e.printStackTrace();
        }

      
        return true;
    }

    @Override
    public boolean init() {
        
  //       String[]  ecTables = new String[]{
  //          "/calibration/htcc/gain", 
  //      };
            String[]  htccTables = new String[]{
            "/calibration/htcc/gain", 
        };
        
        requireConstants(Arrays.asList(htccTables));
  //      gain.show();
        System.out.println("-----> INITIALIZING HTCC as a SERVICE...");
        return true;
    }

   
    
}
