package org.jlab.geom.gemc;

public class GemcMaterial extends GemcPropertyMap {
    /**
     * defines the keys and order thereof for this map
     **/
    public GemcMaterial() {
        // order matters here!
        super.put("name", "");
        super.put("description", "");
        super.put("density", "");
        super.put("ncomponents", "");
        super.put("components", "");
        super.put("photonEnergy", "");
        super.put("indexOfRefraction", "");
        super.put("absorptionLength", "");
        super.put("reflectivity", "");
        super.put("efficiency", "");
        super.put("fastcomponent", "");
        super.put("slowcomponent", "");
        super.put("scintillationyield", "");
        super.put("resolutionscale", "");
        super.put("fasttimeconstant", "");
        super.put("slowtimeconstant", "");
        super.put("yieldratio", "");
        super.put("rayleigh", "");
        super.put("variation", "");
        super.lock();
    }
}
