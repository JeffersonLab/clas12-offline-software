package org.jlab.geom.gemc;

public class GemcBank extends GemcPropertyMap {
    /**
     * defines the keys and order thereof for this map
     **/
    public GemcBank() {
        // order matters here!
        super.put("bankname", "");
        super.put("name", "");
        super.put("description", "");
        super.put("num", "");
        super.put("type", "");
        super.put("variation", "");
        super.lock();
    }
}
