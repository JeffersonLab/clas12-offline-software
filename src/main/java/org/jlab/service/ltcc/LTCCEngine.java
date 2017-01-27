package org.jlab.service.ltcc;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataBank;
import java.util.List;

/**
 * LTCC Reconstruction Engine.
 *
 * @author S. Joosten
 */
public class LTCCEngine extends ReconstructionEngine {
    private static boolean DEBUG = true;
    
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
                LTCCCluster.writeClusters(event, clusters);
                if (DEBUG) {
                    event.getBank("LTCC::clusters").show();
                }
            }
            
            return true;
        }
        
    @Override
        public boolean init() {
            System.out.println("[LTCC] --> initialization successful...");
            return true;
        }
       
}
