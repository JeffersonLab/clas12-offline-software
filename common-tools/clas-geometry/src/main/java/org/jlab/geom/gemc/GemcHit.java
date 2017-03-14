package org.jlab.geom.gemc;

public class GemcHit extends GemcPropertyMap {
    /**
     * defines the keys and order thereof for this map
     **/
    public GemcHit() {
        // order matters here!
        super.put("name", "");
        super.put("description", "");
        super.put("identifiers", "");
        super.put("signalThreshold", "");
        super.put("timeWindow", "");
        super.put("prodThreshold", "");
        super.put("maxStep", "");
        super.put("riseTime", "");
        super.put("fallTime", "");
        super.put("mvToMeV", "");
        super.put("pedestal", "");
        super.put("delay", "");
        super.put("variation", "");
        super.lock();
    }
}
