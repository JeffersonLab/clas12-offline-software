/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.reco;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;

/**
 *
 * @author gavalian
 */
public class DummyEngine extends ReconstructionEngine {

    public DummyEngine(){
        super("DUMMY","gavalian","1.0");
        System.out.println(">>>>>> Executing constructor");
    }
    
    @Override
    public boolean processDataEvent(DataEvent event) {
        EvioDataEvent  evioEvent = (EvioDataEvent) event;
        if(evioEvent.hasBank("FTOF::dgtz")){
            //System.out.println("----> found your event ::: FTOF bank is present");
        }
        return true;
    }

    @Override
    public boolean init() {
        /**
         * Define calibration tables to be accessed during reconstruction.
         * The list is passed to ConstantsManager.
         */
        //List<String>  calibrationTables = new ArrayList<String>();
        //calibrationTables.add("/calibration/ec/attenuations");
        //ReconstructionEngine.constantsManager.init(calibrationTables);
        this.requireConstants(Arrays.asList("/calibration/ec/attenuation"));
        return true;
    }
    
}
