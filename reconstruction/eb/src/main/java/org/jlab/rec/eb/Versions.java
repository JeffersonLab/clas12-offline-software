package org.jlab.rec.eb;

import java.util.HashMap;
import java.util.Map;
import org.jlab.clas.swimtools.MagFieldsEngine;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.jnp.utils.json.JsonObject;
//import org.jlab.rec.cvt.services.CVTReconstruction;
//import org.jlab.rec.ft.FTEBEngine;
//import org.jlab.rec.ft.cal.FTCALEngine;
//import org.jlab.rec.ft.hodo.FTHODOEngine;
//import org.jlab.rec.rich.RICHEBEngine;
//import org.jlab.service.band.BANDEngine;
//import org.jlab.service.cnd.CNDCalibrationEngine;
import org.jlab.service.ctof.CTOFEngine;
import org.jlab.service.dc.DCHBEngine;
import org.jlab.service.dc.DCTBEngine;
import org.jlab.service.eb.EBHBEngine;
import org.jlab.service.eb.EBTBEngine;
import org.jlab.service.ec.ECEngine;
import org.jlab.service.ftof.FTOFHBEngine;
import org.jlab.service.ftof.FTOFTBEngine;
import org.jlab.service.htcc.HTCCReconstructionService;
import org.jlab.service.ltcc.LTCCEngine;
//import org.jlab.service.rtpc.RTPCEngine;

/**
 *
 * @author baltzell
 */
public class Versions {

    private static final Class[] clazzes = {
        MagFieldsEngine.class,
//        FTCALEngine.class,
//        FTHODOEngine.class,
//        FTEBEngine.class,
        DCHBEngine.class,
        FTOFHBEngine.class,
        ECEngine.class,
//        CVTReconstruction.class,
        CTOFEngine.class,
//        CNDCalibrationEngine.class,
//        BANDEngine.class,
        HTCCReconstructionService.class,
        LTCCEngine.class,
        EBHBEngine.class,
        DCTBEngine.class,
        FTOFTBEngine.class,
        EBTBEngine.class,
//        RICHEBEngine.class,
//        RTPCEngine.class
    };

    public static Map<String,String> getVersions() {
        Map<String,String> ret = new HashMap<>();
        for (Class c : clazzes) {
            ret.put(c.getSimpleName(),c.getPackage().getImplementationVersion());
        }
        ret.put("COATJAVA",ConstantsManager.class.getPackage().getImplementationVersion());
        return ret;
    }

    public static JsonObject getVersionsJson() {
        JsonObject ret = new JsonObject();
        Map<String,String> versions = getVersions();
        for (Map.Entry<String, String> entry : versions.entrySet()) {
            ret.add(entry.getKey(),entry.getValue());
        }
        return ret;
    }
    
}
