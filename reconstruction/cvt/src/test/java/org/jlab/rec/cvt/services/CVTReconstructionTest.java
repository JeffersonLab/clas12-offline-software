package org.jlab.rec.cvt.services;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.services.CVTReconstruction;
import org.jlab.service.eb.EBHBEngine;
import org.jlab.service.eb.EBTBEngine;

import org.jlab.analysis.physics.TestEvent;
import org.jlab.analysis.math.ClasMath;

/**
 *
 * @author naharrison
 */
public class CVTReconstructionTest {
	
	@Test
	public void testCVTReconstruction() {
    System.setProperty("CLAS12DIR", "../../");
		
		DataEvent testEvent = TestEvent.getCVTTestEvent();

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
		assertEquals(ClasMath.isWithinXPercent(10.0, testEvent.getBank("REC::Particle").getFloat("px", 0), 1.9504), true);
		assertEquals(ClasMath.isWithinXPercent(10.0, testEvent.getBank("REC::Particle").getFloat("py", 0), 0.2741), true);
		assertEquals(ClasMath.isWithinXPercent(10.0, testEvent.getBank("REC::Particle").getFloat("pz", 0), 0.3473), true);
		assertEquals(ClasMath.isWithinXPercent(30.0, testEvent.getBank("REC::Particle").getFloat("vz", 0), -1.95444), true);
	}

}
