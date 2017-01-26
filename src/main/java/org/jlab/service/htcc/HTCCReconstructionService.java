/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.htcc;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.htcc.HTCCReconstruction;

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
        try {
            HTCCReconstruction reco = new HTCCReconstruction();
            reco.processEvent(event);
        } catch (Exception e){
            System.out.println("----> error with HTCC reconstruction..");
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean init() {
        System.out.println("-----> INITIALIZING HTCC as a SERVICE...");
        return true;
    }

   
    
}
