package org.jlab.geom.gemc;

public class GemcDetector extends GemcPropertyMap {
    /**
     * defines the keys and order thereof for this map
     **/
    public GemcDetector() {
        // order matters here!
        super.put("mother", "root");
        super.put("description", "");
        super.put("pos", "0*cm 0*cm 0*cm");
        super.put("rotation", "ordered: zxy 0*deg 0*deg 0*deg");
        super.put("color", "808080");
        super.put("type", "G4Trap");
        super.put("dimensions", "0*cm 0*deg 0*deg 0*cm 0*cm 0*cm 0*deg 0*cm 0*cm 0*cm 0*deg");
        super.put("material", "Air");
        super.put("mfield", "no");
        super.put("ncopy", "1");
        super.put("pMany", "1");
        super.put("exist", "1");
        super.put("visible", "1");
        super.put("style", "0");
        super.put("sensitivity", "no");
        super.put("hit_type", "");
        super.put("identifiers", "");
        super.lock();
    }
}
