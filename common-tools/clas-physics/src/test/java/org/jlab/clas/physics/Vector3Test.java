package org.jlab.clas.physics;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author naharrison
 */
public class Vector3Test {

	@Test
	public void testDotAndCross() {
		Vector3 v1 = new Vector3();
		Vector3 v2 = new Vector3(v1);
		v1.setMagThetaPhi(1.0, Math.toRadians(90.0), 0.0);
		v2.setXYZ(0.0, 2.0, 0.0);
		v2.unit();
		assertEquals(v1.dot(v2), 0.0, 1e-6);

		Vector3 v3 = v1.cross(v2);
		Vector3 vz = new Vector3(0.0, 0.0, 1.0);
		assertEquals(v3.x(), vz.x(), 1e-6);
		assertEquals(v3.y(), vz.y(), 1e-6);
		assertEquals(v3.z(), vz.z(), 1e-6);

		Vector3 v4 = v2.cross(v1);
		vz.negative();
		assertEquals(v4.x(), vz.x(), 1e-6);
		assertEquals(v4.y(), vz.y(), 1e-6);
		assertEquals(v4.z(), vz.z(), 1e-6);
	}


	@Test
	public void testCompare() {
		Vector3 v1 = new Vector3(1, 2, 3);
		Vector3 v2 = new Vector3(1, 2, 3);
		assertEquals(v1.compare(v2), 0.0, 1e-6);
		assertEquals(v1.compareWeighted(v2), 0.0, 1e-6);
	}


	@Test
	public void testStrings() {
		Vector3 v1 = new Vector3(1, 2, 3);
		System.out.println(v1.getXYZString());
		System.out.println(v1.getMagThetaPhiString());
		System.out.println(v1.toString());
	}

}
