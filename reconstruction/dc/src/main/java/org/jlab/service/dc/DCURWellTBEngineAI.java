package org.jlab.service.dc;

/**
 *
 * @author Tongtong Cao
 */
public class DCURWellTBEngineAI extends DCURWellTBEngine {
    
    public DCURWellTBEngineAI() {
        super("DCTAI");
        this.getBanks().init("TimeBasedTrkg", "AI", "AI");
    }
    
}
