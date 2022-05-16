package org.jlab.service.eb;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jlab.analysis.physics.TestEvent;
import org.jlab.analysis.math.ClasMath;
import org.jlab.clas.swimtools.MagFieldsEngine;

import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.jnp.hipo4.data.SchemaFactory;
import org.jlab.logging.DefaultLogger;
import org.jlab.service.dc.DCHBEngine;
import org.jlab.service.dc.DCTBEngine;
import org.jlab.service.ec.ECEngine;
import org.jlab.service.ftof.FTOFTBEngine;
import org.jlab.service.htcc.HTCCReconstructionService;
import org.jlab.service.ltcc.LTCCEngine;
import org.jlab.utils.system.ClasUtilsFile;

/**
 *
 * @author nbaltzell
 */
public class EBReconstructionTest {

    public void processAllEngines(DataEvent ev) {
        MagFieldsEngine enf = new MagFieldsEngine();
        enf.init();
        enf.processDataEvent(ev);
        DCHBEngine engineDCHB = new DCHBEngine();
        engineDCHB.init();
        engineDCHB.processDataEvent(ev);

        DCTBEngine engineDCTB = new DCTBEngine();
        engineDCTB.init();
        engineDCTB.processDataEvent(ev);

        ECEngine engineEC = new ECEngine();
        engineEC.init();
        engineEC.processDataEvent(ev);

        FTOFTBEngine engineFTOF = new FTOFTBEngine();
        engineFTOF.init();
        engineFTOF.processDataEvent(ev);

        HTCCReconstructionService engineHTCC = new HTCCReconstructionService();
        engineHTCC.init();
        engineHTCC.processDataEvent(ev);
        
        LTCCEngine engineLTCC = new LTCCEngine();
        engineLTCC.init();
        engineLTCC.processDataEvent(ev);

        EBHBEngine engineEBHB = new EBHBEngine();
        engineEBHB.init();
        engineEBHB.processDataEvent(ev);
        
        EBTBEngine engineEBTB = new EBTBEngine();
        engineEBTB.init();
        engineEBTB.processDataEvent(ev);
        
        //ev.show();
    }

    /**
     *
     * Check that index references are within valid range
     *
     * bankNameFrom - bank containing the index
     * bankNameTo - bank to which the index refers
     * idxVarName - name of the index variable
     *
     */
    public boolean hasValidRefs(DataEvent ev,
                        String bankNameFrom,
                        String bankNameTo,
                        String idxVarName) {
        DataBank bFrom=ev.getBank(bankNameFrom);
        DataBank bTo=ev.getBank(bankNameTo);
        for (int ii=0; ii<bFrom.rows(); ii++) {
            int ref=bFrom.getInt(idxVarName,ii);
            if (ref>=bTo.rows() || ref<0) {
                //bFrom.show();
                //bTo.show();
                System.err.println(String.format(
                        "\bnhasValidRefs: failed on (%s0>%s) %d->%d\n",
                        bankNameFrom,bankNameTo,ii,ref));
                return false;
            }
        }
        return true;
    }

    @Test
    public void testEBReconstruction() {
        DefaultLogger.debug();

        System.setProperty("CLAS12DIR", "../../");
        String dir = ClasUtilsFile.getResourceDir("CLAS12DIR", "etc/bankdefs/hipo4");
        SchemaFactory schemaFactory = new SchemaFactory();
        schemaFactory.initFromDirectory(dir);
        
        DataEvent photonEvent = TestEvent.getECSector1PhotonEvent(schemaFactory);
        processAllEngines(photonEvent);
        assertEquals(photonEvent.hasBank("RECHB::Event"), true);
        assertEquals(photonEvent.hasBank("RECHB::Particle"), true);
        assertEquals(photonEvent.hasBank("RECHB::Calorimeter"), true);
        assertEquals(photonEvent.hasBank("REC::Event"), true);
        assertEquals(photonEvent.hasBank("REC::Particle"), true);
        assertEquals(photonEvent.hasBank("REC::Calorimeter"), true);
        assertEquals(hasValidRefs(photonEvent,"REC::Calorimeter","REC::Particle","pindex"),true);
        // additional EC reco. tests:
        assertEquals(photonEvent.getBank("RECHB::Particle").rows(), 1);
        assertEquals(ClasMath.isWithinXPercent(25.0, photonEvent.getBank("RECHB::Particle").getFloat("px", 0), 1.057), true);
        assertEquals(photonEvent.getBank("RECHB::Particle").getFloat("py", 0) > -0.15, true);
        assertEquals(photonEvent.getBank("RECHB::Particle").getFloat("py", 0) < 0.15, true);
        assertEquals(ClasMath.isWithinXPercent(25.0, photonEvent.getBank("RECHB::Particle").getFloat("pz", 0), 2.266), true);

        DataEvent electronEvent = TestEvent.getDCSector1ElectronEvent(schemaFactory);
        processAllEngines(electronEvent);
        assertEquals(electronEvent.hasBank("REC::Event"), true);
        assertEquals(electronEvent.hasBank("REC::Particle"), true);
        assertEquals(electronEvent.hasBank("REC::Track"), true);
        assertEquals(hasValidRefs(photonEvent,"REC::Track","TimeBasedTrkg::TBTracks","index"),true);
        //assertEquals(electronEvent.hasBank("REC::Cherenkov"), true);
        //assertEquals(electronEvent.hasBank("REC::Scintillator"), true);

    }
}
