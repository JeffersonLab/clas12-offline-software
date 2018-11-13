package cnuphys.ced.geometry.util;

import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * A set of static methods for use with the apache math library
 * @author heddle
 *
 */
public class GeometryUtilities {
	
	//tolerance below which points are considered identical
	private static final double tolerance = 1.0e-8;;

	/**
	 * Create a plane specified by a constant value of phi
	 * @param phi the azimuthal angle in degrees
	 * @return a plane of constant phi
	 */
	public static Plane constantPhiPlane(double phi) {
		double phiRad = Math.toRadians(phi);
		Vector3D normal = new Vector3D(-Math.sin(phiRad), Math.cos(phiRad), 0); 
		return new Plane(normal, tolerance);
	}
}
