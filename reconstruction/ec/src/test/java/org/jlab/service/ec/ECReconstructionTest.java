package org.jlab.service.ec;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jlab.io.base.DataEvent;
import org.jlab.service.ec.ECEngine;
import org.jlab.service.eb.EBHBEngine;

import org.jlab.analysis.physics.TestEvent;
import org.jlab.analysis.math.ClasMath;

/**
 *
 * @author naharrison
 */
public class ECReconstructionTest {
	
  @Test
  public void testECReconstruction() {
    System.setProperty("CLAS12DIR", "../../");

    DataEvent testEvent = TestEvent.getECSector1PhotonEvent();
    
    ECEngine engineEC = new ECEngine();
    engineEC.init();
    engineEC.processDataEvent(testEvent);
    
    EBHBEngine engineEBHB = new EBHBEngine();
    engineEBHB.init();
    engineEBHB.processDataEvent(testEvent);
    
    assertEquals(testEvent.hasBank("FAKE::Bank"), false);
    assertEquals(testEvent.hasBank("RECHB::Particle"), true);
    assertEquals(testEvent.getBank("RECHB::Particle").rows(), 1);
    assertEquals(ClasMath.isWithinXPercent(25.0, testEvent.getBank("RECHB::Particle").getFloat("px", 0), 1.057), true);
    assertEquals(testEvent.getBank("RECHB::Particle").getFloat("py", 0) > -0.15, true);
    assertEquals(testEvent.getBank("RECHB::Particle").getFloat("py", 0) < 0.15, true);
    assertEquals(ClasMath.isWithinXPercent(25.0, testEvent.getBank("RECHB::Particle").getFloat("pz", 0), 2.266), true);
  }

}
