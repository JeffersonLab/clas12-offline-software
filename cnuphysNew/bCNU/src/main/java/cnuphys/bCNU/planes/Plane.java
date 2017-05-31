package cnuphys.bCNU.planes;

import cnuphys.bCNU.util.VectorSupport;

/**
 * A plane is defined by the equation ax + by + cd = d
 * @author heddle
 *
 */
public class Plane {

	//the plane constants
	private double _a;
	private double _b;
	private double _c;
	private double _d;
	
	public Plane(double a, double b, double c, double d) {
		_a = a;
		_b = b;
		_c = c;
		_d = d;
	}
	
	/**
	 * Create a plane from a normal vector and a point on the plane 
	 * @param norm the normal vector where the components (0, 1, 2) map to (x, y, z)
	 * @param p a point in the plane where the components (0, 1, 2) map to (x, y, z)
	 * @return the plane that contains p and its normal is norm
	 */
	public static Plane createPlane(double norm[], double p[]) {
		//lets make it a unit vector
		double u[] = VectorSupport.unitVector(norm);
		
		double a = u[0];
		double b = u[1];
		double c = u[2];
		double d = a*p[0] + b*p[1] + c*p[2];
		return new Plane(a, b, c, d);
	}
	
	/**
	 * Compute the intersection of an infinite line with the plane 
	 * @param p1 a point on the line where the components (0, 1, 2) map to (x, y, z)
	 * @param p2 a point on the line where the components (0, 1, 2) map to (x, y, z)
	 * @return the intersection p where the components (0, 1, 2) map to (x, y, z). If there is
	 * no intersection, returns null.
	 */
	public double[] infiniteLineIntersection(double p1[], double p2[]) {
	
		double u = u(p1, p2);
		if (Double.isNaN(u)) {
			return null;
		}
		
		double p[] = new double[3];
		for (int i = 0; i < 3; i++) {
			p[i] = p1[i] + u*(p2[i]-p1[i]);
		}
		return p;
	}
	
	//
	//Get the u parameter of a line with the plane.
	// p1 one point on the 
	//return NaN if the infinite line does not intersect the plane. Otherwise
	//return u. If 0<u<1 the segment intersects the plane. Otherwise the infinite line
	//intersects the plane, but not the segment
	// @param p1 a point on the line where the components (0, 1, 2) map to (x, y, z)
	// @param p2 a point on the line where the components (0, 1, 2) map to (x, y, z)

	private double u(double p1[], double p2[]) {
	
		double denom = _a*(p1[0]-p2[0]) + _b*(p1[1]-p2[1]) + _c*(p1[2]-p2[2]);
		if (Math.abs(denom) < 1.0e-20) {
		   return Double.NaN;
		}
		double numer = _a*p1[0] + _b*p1[1] + _c*p1[2] + _d;
		return numer/denom;
	}
	
	public static void main(String arg[]) {
	
	}
	
	/**
	 * Create a plane of constant azimuthal angle phi
	 * @param phi the azimuthal angle in degrees
	 * @return the plane of constant phi
	 */
	public static Plane constantPhiPlane(double phi) {
		phi = Math.toRadians(phi);
		
		double cphi = Math.cos(phi);
		double sphi = Math.sin(phi);
		
		//point in the plane
		double p[] = new double[3];
		p[0] = cphi;
		p[1] = sphi;
		p[2] =0;
		
		//normal
		double norm[] = new double[3];
		norm[0] = sphi;
		norm[1] = -cphi;
		norm[2] = 0;
		
		return createPlane(norm, p);
	}
}
