package cvt;

import org.junit.Test;
import static org.junit.Assert.*;

import events.TestEvent;

import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.services.CVTReconstruction;
import org.jlab.service.eb.EBHBEngine;
import org.jlab.service.eb.EBTBEngine;
import org.jlab.clas.swimtools.MagFieldsEngine;
/**
 *
 * @author naharrison
 */
public class CVTReconstructionTest {
	
	@Test
	public void testCVTReconstruction() {
		
		DataEvent testEvent = TestEvent.getCVTTestEvent();
                MagFieldsEngine enf = new MagFieldsEngine();
                enf.init();
                enf.processDataEvent(testEvent);
		CVTReconstruction CVTengine = new CVTReconstruction();
		CVTengine.init();
		CVTengine.processDataEvent(testEvent);

		EBHBEngine EBHBengine = new EBHBEngine();
		EBHBengine.init();
		EBHBengine.processDataEvent(testEvent);

		EBTBEngine EBTBengine = new EBTBEngine();
		EBTBengine.init();
		EBTBengine.processDataEvent(testEvent);

		assertEquals(testEvent.hasBank("REC::Particle"), true);
		assertEquals(testEvent.getBank("REC::Particle").rows(), 1);
		assertEquals(testEvent.getBank("REC::Particle").getByte("charge", 0), 1);
		assertEquals(isWithinXPercent(10.0, testEvent.getBank("REC::Particle").getFloat("px", 0), 1.9504), true);
		assertEquals(isWithinXPercent(10.0, testEvent.getBank("REC::Particle").getFloat("py", 0), 0.2741), true);
		assertEquals(isWithinXPercent(10.0, testEvent.getBank("REC::Particle").getFloat("pz", 0), 0.3473), true);
		assertEquals(isWithinXPercent(30.0, testEvent.getBank("REC::Particle").getFloat("vz", 0), -1.95444), true);
	}
	
	
	public static boolean isWithinXPercent(double X, double val, double standard) {
		if(standard >= 0 && val > (1.0 - (X/100.0))*standard && val < (1.0 + (X/100.0))*standard) return true;
		else if(standard < 0 && val < (1.0 - (X/100.0))*standard && val > (1.0 + (X/100.0))*standard) return true;
		return false;
	}
	
	
}
