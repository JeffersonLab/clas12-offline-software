package org.jlab.service.dc;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jlab.io.base.DataEvent;
import org.jlab.service.dc.DCHBEngine;
import org.jlab.service.dc.DCTBEngine;

import org.jlab.analysis.physics.TestEvent;
import org.jlab.analysis.math.ClasMath;

/**
 *
 * @author naharrison
 */
public class DCReconstructionTest {

  @Test
  public void testDCReconstruction() {
    System.setProperty("CLAS12DIR", "../../");

    DataEvent testEvent = TestEvent.getDCSector1ElectronEvent();

    DCHBEngine engineHB = new DCHBEngine();
    engineHB.init();
    engineHB.processDataEvent(testEvent);
    
    assertEquals(testEvent.hasBank("HitBasedTrkg::HBTracks"), true);
    assertEquals(testEvent.getBank("HitBasedTrkg::HBTracks").rows(), 1);
    assertEquals(testEvent.getBank("HitBasedTrkg::HBTracks").getByte("q", 0), -1);
    assertEquals(ClasMath.isWithinXPercent(16.0, testEvent.getBank("HitBasedTrkg::HBTracks").getFloat("p0_x", 0), 1.057), true);
    assertEquals(testEvent.getBank("HitBasedTrkg::HBTracks").getFloat("p0_y", 0) > -0.1, true);
    assertEquals(testEvent.getBank("HitBasedTrkg::HBTracks").getFloat("p0_y", 0) < 0.1, true);
    assertEquals(ClasMath.isWithinXPercent(16.0, testEvent.getBank("HitBasedTrkg::HBTracks").getFloat("p0_z", 0), 2.266), true);

    DCTBEngine engineTB = new DCTBEngine();
    engineTB.init();
    engineTB.processDataEvent(testEvent);
    
    assertEquals(testEvent.hasBank("TimeBasedTrkg::TBTracks"), true);
    assertEquals(testEvent.getBank("TimeBasedTrkg::TBTracks").rows(), 1);
    assertEquals(testEvent.getBank("TimeBasedTrkg::TBTracks").getByte("q", 0), -1);
    assertEquals(ClasMath.isWithinXPercent(5.0, testEvent.getBank("TimeBasedTrkg::TBTracks").getFloat("p0_x", 0), 1.057), true);
    assertEquals(testEvent.getBank("TimeBasedTrkg::TBTracks").getFloat("p0_y", 0) > -0.05, true);
    assertEquals(testEvent.getBank("TimeBasedTrkg::TBTracks").getFloat("p0_y", 0) < 0.05, true);
    assertEquals(ClasMath.isWithinXPercent(5.0, testEvent.getBank("TimeBasedTrkg::TBTracks").getFloat("p0_z", 0), 2.266), true);
  }

}
