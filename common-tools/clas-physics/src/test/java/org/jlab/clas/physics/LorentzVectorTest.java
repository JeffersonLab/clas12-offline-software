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

		v.print();
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
	public void testAddSubInvert() {
		LorentzVector v1 = new LorentzVector(0.0, 1.0, 0.0, Math.sqrt(1.0 + 0.938*0.938));
		LorentzVector v2 = new LorentzVector(1.0, 0.0, 0.0, Math.sqrt(1.0 + 0.938*0.938));
		v1.add(v2);
		v1.sub(v2);
		v1.add(v2);
		v1.invert();

		assertEquals(v1.px(), -1.0, 1e-6);
		assertEquals(v1.py(), -1.0, 1e-6);
		assertEquals(v1.pz(), 0.0, 1e-6);
		assertEquals(v2.vect().x(), 1.0, 1e-6);
		assertEquals(v2.vect().y(), 0.0, 1e-6);
		assertEquals(v2.vect().z(), 0.0, 1e-6);
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
		LorentzVector v = new LorentzVector(1.0, 1.0, 1.0, Math.sqrt(3.0 + 0.938*0.938));
		double e = v.e();
		v.boost(betaX, 0.0, 0.0);
		double gamma = 1.0/Math.sqrt(1.0 - betaX*betaX);
		assertEquals(v.px(), 1.0 + (gamma-1.0) + gamma*betaX*e, 1e-6);
		assertEquals(v.py(), 1.0, 1e-6);
		assertEquals(v.pz(), 1.0, 1e-6);
		assertEquals(v.e(), gamma*(e + betaX), 1e-6);
	}


	@Test
	public void testBoostY() {
		double betaY = 0.5;
		Vector3 vboost = new Vector3(0.0, betaY, 0.0);
		LorentzVector v = new LorentzVector(1.0, 1.0, 1.0, Math.sqrt(3.0 + 0.938*0.938));
		double e = v.e();
		v.boost(vboost);
		double gamma = 1.0/Math.sqrt(1.0 - betaY*betaY);
		assertEquals(v.px(), 1.0, 1e-6);
		assertEquals(v.py(), 1.0 + (gamma-1.0) + gamma*betaY*e, 1e-6);
		assertEquals(v.pz(), 1.0, 1e-6);
		assertEquals(v.e(), gamma*(e + betaY), 1e-6);
	}

}
