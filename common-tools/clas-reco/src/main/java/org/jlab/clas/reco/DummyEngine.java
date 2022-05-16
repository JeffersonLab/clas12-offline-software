package org.jlab.clas.reco;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import cnuphys.magfield.MagneticField;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;

/**
 *
 * @author gavalian
 */
public class DummyEngine extends ReconstructionEngine {

    public static Logger LOGGER = Logger.getLogger(DummyEngine.class.getName());

    public DummyEngine(){
        super("DUMMY","gavalian","1.0");
        LOGGER.log(Level.FINEST,">>>>>> Executing constructor");
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
