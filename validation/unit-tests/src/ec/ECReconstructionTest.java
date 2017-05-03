package ec;

import org.junit.Test;
import static org.junit.Assert.*;

import events.TestEvent;

import org.jlab.io.base.DataEvent;
import org.jlab.service.ec.ECEngine;

/**
 *
 * @author naharrison
 */
public class ECReconstructionTest {
	
	@Test
	public void testECReconstruction() {
		
		DataEvent testEvent = TestEvent.getECSector1PhotonEvent();

		ECEngine engine = new ECEngine();
		engine.init();
		engine.processDataEvent(testEvent);

		testEvent.show();

	}
	
	
	public static boolean isWithinXPercent(double X, double val, double standard) {
		if(val > (1.0 - (X/100.0))*standard && val < (1.0 + (X/100.0))*standard) return true;
		return false;
	}
	
	
}
