package org.jlab.geom;

import java.util.*;

public class G4VolumeMap extends HashMap<String,G4Volume> {
    @Override
    public String toString() {
        StringBuilder msg = new StringBuilder();
        for (Map.Entry<String,G4Volume> vol : this.entrySet()) {
            msg.append(vol.getKey()+":\n");
            msg.append(vol.getValue().toString().replaceAll("(?m)^", "    "));
        }
        return msg.toString();
    }
}
