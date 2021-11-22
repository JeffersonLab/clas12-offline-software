package org.jlab.service.ltcc;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;
import java.util.List;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * LTCC Reconstruction Engine.
 *
 * @author S. Joosten
 */
public class LTCCEngine extends ReconstructionEngine {

    public static Logger LOGGER = Logger.getLogger(LTCCEngine.class.getName());

    private static final boolean DEBUG = false;
    private static final List<String> CC_TABLES = 
        Arrays.asList("/calibration/ltcc/spe",
                      "/calibration/ltcc/status"
                );
    
    public LTCCEngine() {
    	super("LTCC", "joosten", "1.0");
    }
    
    @Override
	public boolean processDataEvent(DataEvent event) {
            if (DEBUG) event.show();
            // only process the event if the LTCC bank is present
            if (event.hasBank("LTCC::adc")) {
                if (DEBUG) event.getBank("LTCC::adc").show();
                List<LTCCHit> hits = 
                        LTCCHit.loadHits(event, this.getConstantsManager());
                List<LTCCCluster> clusters =
                        LTCCClusterFinder.findClusters(hits);
                LTCCCluster.saveClusters(event, clusters);
                if (DEBUG) {
                    event.getBank("LTCC::clusters").show();
                }
            }
            
            return true;
        }
        
    @Override
        public boolean init() {
            this.requireConstants(CC_TABLES);            
            this.registerOutputBank("LTCC::clusters");
            return true;
        }
       
}
