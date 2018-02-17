package org.jlab.clas.physics;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author naharrison
 */
public class LorentzVectorTest {

	@Test
	public void testLorentzVector() {
		LorentzVector v = new LorentzVector(0.0, 0.0, 6.0, Math.sqrt(6.0*6.0 + 0.938*0.938));
		assertEquals(v.mass(), 0.938, 1e-6);
		assertEquals(v.theta(), 0.0, 1e-6);
		assertEquals(v.p(), 6.0, 1e-6);
		assertEquals(v.e(), Math.sqrt(6.0*6.0 + 0.938*0.938), 1e-6);

		v.setPxPyPzM(6.0, 0.0, 0.0, 0.000511);
		assertEquals(v.mass(), 0.000511, 1e-6);
		assertEquals(v.theta(), Math.PI/2.0, 1e-6);
		assertEquals(v.phi(), 0.0, 1e-6);
		assertEquals(v.p(), 6.0, 1e-6);
		assertEquals(v.e(), Math.sqrt(6.0*6.0 + 0.000511*0.000511), 1e-6);
	}
}
