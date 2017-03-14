package org.jlab.geom;

import java.util.*;

public class G4Volume extends HashMap<String,String> {

    public G4Volume() {
        this.put("mother", "root");
        this.put("description", "");
        this.put("pos", "0*cm 0*cm 0*cm");
        this.put("rotation", "ordered: zxy 0*deg 0*deg 0*deg");
        this.put("color", "808080");
        this.put("type", "G4Trap");
        this.put("dimensions", "0*cm 0*deg 0*deg 0*cm 0*cm 0*cm 0*deg 0*cm 0*cm 0*cm 0*deg");
        this.put("material", "Air");
        this.put("mfield", "no");
        this.put("ncopy", "1");
        this.put("pMany", "1");
        this.put("exist", "1");
        this.put("visible", "1");
        this.put("style", "0");
        this.put("sensitivity", "no");
        this.put("hit_type", "");
        this.put("identifiers", "");
    }

    @Override
    public String toString() {
        StringBuilder msg = new StringBuilder();
        for (Map.Entry<String,String> e : this.entrySet()) {
            msg.append(e.getKey()+": "+e.getValue()+"\n");
        }
        return msg.toString();
    }
}
