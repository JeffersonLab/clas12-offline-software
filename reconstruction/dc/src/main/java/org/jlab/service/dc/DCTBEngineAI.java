package org.jlab.service.dc;

/**
 *
 * @author ziegler
 */
public class DCTBEngineAI extends DCTBEngine {
    
    public DCTBEngineAI() {
        super("DCTAI");
        this.getBanks().init("TimeBasedTrkg", "AI", "AI");
    }
    
}
