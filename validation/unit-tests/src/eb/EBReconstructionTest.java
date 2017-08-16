package eb;

import org.junit.Test;
import static org.junit.Assert.*;

import events.TestEvent;

import org.jlab.io.base.DataEvent;
import org.jlab.service.dc.DCHBEngine;
import org.jlab.service.dc.DCTBEngine;
import org.jlab.service.ec.ECEngine;
import org.jlab.service.ftof.FTOFEngine;
import org.jlab.service.htcc.HTCCReconstructionService;
import org.jlab.service.ltcc.LTCCEngine;
import org.jlab.service.eb.EBHBEngine;
import org.jlab.service.eb.EBTBEngine;

/**
 *
 * @author nbaltzell
 */
public class EBReconstructionTest {

    public void processAllEngines(DataEvent ev) {

        DCHBEngine engineDCHB = new DCHBEngine();
        engineDCHB.init();
        engineDCHB.processDataEvent(ev);

        DCTBEngine engineDCTB = new DCTBEngine();
        engineDCTB.init();
        engineDCTB.processDataEvent(ev);

        ECEngine engineEC = new ECEngine();
        engineEC.init();
        engineEC.processDataEvent(ev);

        FTOFEngine engineFTOF = new FTOFEngine();
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
        
        ev.show();
    }

    @Test
    public void testEBReconstruction() {

        DataEvent photonEvent = TestEvent.getECSector1PhotonEvent();
        processAllEngines(photonEvent);
        assertEquals(photonEvent.hasBank("RECHB::Event"), true);
        assertEquals(photonEvent.hasBank("RECHB::Particle"), true);
        assertEquals(photonEvent.hasBank("RECHB::Calorimeter"), true);
        assertEquals(photonEvent.hasBank("REC::Event"), true);
        assertEquals(photonEvent.hasBank("REC::Particle"), true);
        assertEquals(photonEvent.hasBank("REC::Calorimeter"), true);

        DataEvent electronEvent = TestEvent.getDCSector1ElectronEvent();
        processAllEngines(electronEvent);
        assertEquals(electronEvent.hasBank("REC::Event"), true);
        assertEquals(electronEvent.hasBank("REC::Particle"), true);
        //assertEquals(electronEvent.hasBank("REC::Tracks"), true);
        //assertEquals(electronEvent.hasBank("REC::Cherenkov"), true);
        //assertEquals(electronEvent.hasBank("REC::Scintillator"), true);

    }
}
