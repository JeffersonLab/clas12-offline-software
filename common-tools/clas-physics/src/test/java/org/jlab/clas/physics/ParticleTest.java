package org.jlab.clas.physics;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author naharrison
 */
public class ParticleTest {

	@Test
	public void testParticle() {
		Particle p = new Particle(11, 0.0, 0.0, 11.0, 0.0, 0.0, 0.0);
		assertEquals(p.mass(), 0.0005, 1e-4);
		assertEquals(p.mass2(), 0.0005*0.0005, 1e-4);
		assertEquals(p.charge(), -1);
		assertEquals(p.px(), 0.0, 1e-6);
		assertEquals(p.py(), 0.0, 1e-6);
		assertEquals(p.pz(), 11.0, 1e-6);
		assertEquals(p.p(), 11.0, 1e-6);
		assertEquals(p.theta(), 0.0, 1e-6);
		assertEquals(p.vertex().x(), 0.0, 1e-6);
		assertEquals(p.vertex().y(), 0.0, 1e-6);
		assertEquals(p.vertex().z(), 0.0, 1e-6);
		assertEquals(p.vx(), 0.0, 1e-6);
		assertEquals(p.vy(), 0.0, 1e-6);
		assertEquals(p.vz(), 0.0, 1e-6);

		Particle p2 = new Particle(p);
		assertEquals(p2.mass(), 0.0005, 1e-4);
		assertEquals(p2.mass2(), 0.0005*0.0005, 1e-4);
		assertEquals(p2.charge(), -1);
		assertEquals(p2.px(), 0.0, 1e-6);
		assertEquals(p2.py(), 0.0, 1e-6);
		assertEquals(p2.pz(), 11.0, 1e-6);
		assertEquals(p2.p(), 11.0, 1e-6);
		assertEquals(p2.theta(), 0.0, 1e-6);
		assertEquals(p2.vertex().x(), 0.0, 1e-6);
		assertEquals(p2.vertex().y(), 0.0, 1e-6);
		assertEquals(p2.vertex().z(), 0.0, 1e-6);
		assertEquals(p2.vx(), 0.0, 1e-6);
		assertEquals(p2.vy(), 0.0, 1e-6);
		assertEquals(p2.vz(), 0.0, 1e-6);
	}


	@Test
	public void testEuclideanDistance() {
		Particle p1 = new Particle();
		p1.setVector(11, 3.0, 0.0, 0.0, 0.0, 0.0, 0.0);
		Particle p2 = new Particle(22, 0.0, 4.0, 0.0);
		assertEquals(p1.euclideanDistance(p2), 5.0, 1e-6);
	}

}

