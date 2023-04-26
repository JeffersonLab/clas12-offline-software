package org.jlab.rec.cvt.services;

import cnuphys.magfield.MagneticFields;
import org.junit.Test;
import static org.junit.Assert.*;

import org.jlab.io.base.DataEvent;
import org.jlab.service.eb.EBHBEngine;
import org.jlab.service.eb.EBTBEngine;

import org.jlab.analysis.physics.TestEvent;
import org.jlab.analysis.math.ClasMath;
import org.jlab.clas.swimtools.MagFieldsEngine;
import org.jlab.jnp.hipo4.data.SchemaFactory;
import org.jlab.logging.DefaultLogger;
import org.jlab.utils.CLASResources;
import org.jlab.utils.system.ClasUtilsFile;

/**
 *
 * @author naharrison
 */
public class CVTReconstructionTest {
	
    @Test
    public void testCVTReconstruction() {
        
        DefaultLogger.debug();

        System.setProperty("CLAS12DIR", "../../");
       
        String mapDir = CLASResources.getResourcePath("etc")+"/data/magfield";
        try {
            MagneticFields.getInstance().initializeMagneticFields(mapDir,
                    "Symm_torus_r2501_phi16_z251_24Apr2018.dat","Symm_solenoid_r601_phi1_z1201_13June2018.dat");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        String dir = ClasUtilsFile.getResourceDir("CLAS12DIR", "etc/bankdefs/hipo4");
        SchemaFactory schemaFactory = new SchemaFactory();
        schemaFactory.initFromDirectory(dir);
    
        DataEvent testEvent = TestEvent.getCVTTestEvent(schemaFactory);

        MagFieldsEngine enf = new MagFieldsEngine();
        enf.init();
        enf.processDataEvent(testEvent);
        
        CVTEngine CVTengine = new CVTEngine();
        CVTengine.init();
        
        testEvent.show();
        CVTengine.processDataEvent(testEvent);
        EBHBEngine EBHBengine = new EBHBEngine();
        EBHBengine.init();
        EBHBengine.processDataEvent(testEvent);

        CVTSecondPassEngine CVTSPengine = new CVTSecondPassEngine();
        CVTSPengine.init();
        CVTSPengine.processDataEvent(testEvent);
        testEvent.show();

        EBTBEngine EBTBengine = new EBTBEngine();
        EBTBengine.init();
        EBTBengine.processDataEvent(testEvent);

        assertEquals(testEvent.hasBank("REC::Particle"), true);
        assertEquals(testEvent.getBank("REC::Particle").rows(), 1);
        assertEquals(testEvent.getBank("REC::Particle").getByte("charge", 0), 1);
        assertEquals(ClasMath.isWithinXPercent(10.0, testEvent.getBank("REC::Particle").getFloat("px", 0), 1.9504), true);
        assertEquals(ClasMath.isWithinXPercent(10.0, testEvent.getBank("REC::Particle").getFloat("py", 0), 0.2741), true);
        assertEquals(ClasMath.isWithinXPercent(10.0, testEvent.getBank("REC::Particle").getFloat("pz", 0), 0.3473), true);
        assertEquals(ClasMath.isWithinXPercent(30.0, testEvent.getBank("REC::Particle").getFloat("vz", 0), -1.95444), true); 

    }
    
}
