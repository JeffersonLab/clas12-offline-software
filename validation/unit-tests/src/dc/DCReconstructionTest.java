package dc;

import org.junit.Test;
import static org.junit.Assert.*;

import events.TestEvent;
import org.jlab.clas.swimtools.MagFieldsEngine;

import org.jlab.io.base.DataEvent;
import org.jlab.service.dc.DCHBEngine;
import org.jlab.service.dc.DCTBEngine;

/**
 *
 * @author naharrison
 */
public class DCReconstructionTest {
	
	@Test
	public void testDCReconstruction() {
		
		DataEvent testEvent = TestEvent.getDCSector1ElectronEvent();
                MagFieldsEngine enf = new MagFieldsEngine();
                enf.init();
                enf.processDataEvent(testEvent);    
		DCHBEngine engineHB = new DCHBEngine();
		engineHB.init();
		engineHB.processDataEvent(testEvent);

		assertEquals(testEvent.hasBank("HitBasedTrkg::HBTracks"), true);
		assertEquals(testEvent.getBank("HitBasedTrkg::HBTracks").rows(), 1);
		assertEquals(testEvent.getBank("HitBasedTrkg::HBTracks").getByte("q", 0), -1);
		assertEquals(isWithinXPercent(16.0, testEvent.getBank("HitBasedTrkg::HBTracks").getFloat("p0_x", 0), 1.057), true);
		assertEquals(testEvent.getBank("HitBasedTrkg::HBTracks").getFloat("p0_y", 0) > -0.1, true);
		assertEquals(testEvent.getBank("HitBasedTrkg::HBTracks").getFloat("p0_y", 0) < 0.1, true);
		assertEquals(isWithinXPercent(16.0, testEvent.getBank("HitBasedTrkg::HBTracks").getFloat("p0_z", 0), 2.266), true);
		
		DCTBEngine engineTB = new DCTBEngine();
		engineTB.init();
		engineTB.processDataEvent(testEvent);
		
		assertEquals(testEvent.hasBank("TimeBasedTrkg::TBTracks"), true);
		assertEquals(testEvent.getBank("TimeBasedTrkg::TBTracks").rows(), 1);
		assertEquals(testEvent.getBank("TimeBasedTrkg::TBTracks").getByte("q", 0), -1);
		assertEquals(isWithinXPercent(5.0, testEvent.getBank("TimeBasedTrkg::TBTracks").getFloat("p0_x", 0), 1.057), true);
		assertEquals(testEvent.getBank("TimeBasedTrkg::TBTracks").getFloat("p0_y", 0) > -0.05, true);
		assertEquals(testEvent.getBank("TimeBasedTrkg::TBTracks").getFloat("p0_y", 0) < 0.05, true);
		assertEquals(isWithinXPercent(5.0, testEvent.getBank("TimeBasedTrkg::TBTracks").getFloat("p0_z", 0), 2.266), true);
		
	}
	
	
	public static boolean isWithinXPercent(double X, double val, double standard) {
		if(val > (1.0 - (X/100.0))*standard && val < (1.0 + (X/100.0))*standard) return true;
		return false;
	}
	
	
}
