package org.jlab.detector.base;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author naharrison
 */
public class DetectorDescriptorTest {

	@Test
	public void testDetectorDescriptor() {
		DetectorDescriptor dd = new DetectorDescriptor(DetectorType.DC);
        dd.setOrder(0);
        dd.setCrateSlotChannel(1, 2, 3);
        dd.setSectorLayerComponent(4, 5, 6);
		assertEquals(dd.getOrder(), 0);
		assertEquals(dd.getCrate(), 1);
		assertEquals(dd.getSlot(), 2);
		assertEquals(dd.getChannel(), 3);
		assertEquals(dd.getSector(), 4);
		assertEquals(dd.getLayer(), 5);
		assertEquals(dd.getComponent(), 6);

		DetectorDescriptor dd2 = new DetectorDescriptor(DetectorType.DC);
        dd2.setOrder(0);
        dd2.setCrateSlotChannel(1, 2, 3);
        dd2.setSectorLayerComponent(4, 5, 6);
		assertEquals(dd.compare(dd2), true);
	}
}
