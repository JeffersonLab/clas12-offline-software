package ec;

import org.junit.Test;
import static org.junit.Assert.*;

import events.TestEvent;
import org.jlab.clas.swimtools.MagFieldsEngine;

import org.jlab.io.base.DataEvent;
import org.jlab.service.ec.ECEngine;
import org.jlab.service.eb.EBHBEngine;

/**
 *
 * @author naharrison
 */
public class ECReconstructionTest {
	
	@Test
	public void testECReconstruction() {
		
		DataEvent testEvent = TestEvent.getECSector1PhotonEvent();
                MagFieldsEngine enf = new MagFieldsEngine();
                enf.init();
                enf.processDataEvent(testEvent);
		ECEngine engineEC = new ECEngine();
		engineEC.init();
		engineEC.processDataEvent(testEvent);

		EBHBEngine engineEBHB = new EBHBEngine();
		engineEBHB.init();
		engineEBHB.processDataEvent(testEvent);

		//testEvent.show();
		//testEvent.getBank("ECAL::clusters").show();
		//testEvent.getBank("RECHB::Particle").show();
		//testEvent.getBank("RECHB::Detector").show();

		assertEquals(testEvent.hasBank("FAKE::Bank"), false);
	}
	
	
	public static boolean isWithinXPercent(double X, double val, double standard) {
		if(val > (1.0 - (X/100.0))*standard && val < (1.0 + (X/100.0))*standard) return true;
		return false;
	}
	
	
}
