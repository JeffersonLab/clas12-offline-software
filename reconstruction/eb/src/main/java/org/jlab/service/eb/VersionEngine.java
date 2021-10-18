package org.jlab.service.eb;

import org.jlab.utils.JsonUtils;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataBank;
import org.jlab.jnp.utils.json.JsonObject;
import org.jlab.rec.eb.Versions;

/**
 *
 * @author baltzell
 */
public class VersionEngine extends ReconstructionEngine {

    static boolean done = false;
    static boolean verbose = true;

    public VersionEngine() {
        super("VersionEngine","baltzell","1.0");
    }

    public static void show(DataBank bank) {
        JsonUtils.show(JsonUtils.read(bank,"json"));
    }

    @Override
    public boolean processDataEvent(DataEvent event) {
        if (!done) {
            JsonObject versions = Versions.getVersionsJson();
            JsonObject config = new JsonObject();
            config.add("versions",versions);
            JsonUtils.extend(event,CONFIG_BANK_NAME,"json",config);
            if (verbose) {
                show(event.getBank(CONFIG_BANK_NAME));
            }
            done = true;
        }
        return true;
    }

    @Override
    public boolean init() {
        return true;
    }

}
