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


	@Test
	public void testConstructors() {
		LorentzVector v = new LorentzVector();
		v.setPxPyPzE(6.0, 0.0, 0.0, Math.sqrt(6.0*6.0 + 0.938*0.938));
		assertEquals(v.mass(), 0.938, 1e-6);
		assertEquals(v.p(), 6.0, 1e-6);

		LorentzVector vcopy = new LorentzVector(v);
		assertEquals(vcopy.mass(), 0.938, 1e-6);
		assertEquals(vcopy.p(), 6.0, 1e-6);
	}


	@Test
	public void testRotations() {
		LorentzVector v = new LorentzVector(0.0, 6.0, 0.0, Math.sqrt(6.0*6.0 + 0.938*0.938));

		v.rotateY(0.1234);
		assertEquals(v.px(), 0.0, 1e-6);
		assertEquals(v.py(), 6.0, 1e-6);
		assertEquals(v.pz(), 0.0, 1e-6);

		v.rotateZ(Math.toRadians(90.0));
		assertEquals(v.px(), -6.0, 1e-6);
		assertEquals(v.py(), 0.0, 1e-6);
		assertEquals(v.pz(), 0.0, 1e-6);
	}


	@Test
	public void testBoostX() {
		double betaX = 0.5;
		LorentzVector v = new LorentzVector(1.0, 1.0, 1.0, Math.sqrt(6.0*6.0 + 0.938*0.938));
		double e = v.e();
		v.boost(betaX, 0.0, 0.0);
		double gamma = 1.0/Math.sqrt(1.0 - betaX*betaX);
		assertEquals(v.px(), 1.0 + (gamma-1.0) + gamma*betaX*e, 1e-6);
		assertEquals(v.py(), 1.0, 1e-6);
		assertEquals(v.pz(), 1.0, 1e-6);
		assertEquals(v.e(), gamma*(e + betaX), 1e-6);
	}
}
