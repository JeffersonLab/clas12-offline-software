package cnuphys.bCNU.geometry;

/**
 * A plane is defined by the equation (r - ro).norm = 0
 * Where r is an arbitrary point on the plane, ro is a
 * given point on the plane and norm is the normal to the plane
 * @author heddle
 *
 */
public class Plane {
	
	//unit vector normal to plane
	private Vector _norm;
	
	//point on the pane
	private Point _ro;
		
	/**
	 * Create a plane from a normal vector and a point on the plane 
	 * @param norm the normal vector 
	 * @param p a point in the plane 
	 * @return the plane that contains p and its normal is norm
	 */
	public Plane (Vector norm, Point p) {
		//lets make it a unit vector
		_norm = norm.unitVector();
		_ro = new Point(p);
	}
	
	/**
	 * Get the constants A, B, C and D for the plane
	 * equation Ax + By + cZ = D
	 * @param abcd upon return, [A, B, C, D]
	 */
	public void getABCD(double abcd[]) {
		abcd[0] = _norm.x; //A
		abcd[1] = _norm.y; //B
		abcd[2] = _norm.z; //C
		abcd[3] = abcd[0]*_ro.x + abcd[1]*_ro.y + abcd[2]*_ro.z; //D
	}
	
	/**
	 * Compute the intersection of an infinite line with the plane 
	 * @param line the line
	 * @param intersection the 
	 * @return the t parameter. If NaN it means the line is parallel to
	 * the plane. If t [0,1] then the segment intersects the line. If t outside
	 * [0, 1] the infinite line intersects the plane, but not the segment
	 */
	public double lineIntersection(Line line, Point intersection) {
		
		//use the formulafor the t parameter
		// t = ((ro - po) dot n)/(l dot n)
		// po "start" point of line
		// l is the "dP" of the line
		// ro given point on plane
		// n normal to plane
		
		double ldotn = line.getDelP().dot(_norm);
		
		//if ldotn is zero, the line is parallel to the plane
		
		if (Math.abs(ldotn) < Constants.TINY) {
			intersection.set(Double.NaN, Double.NaN, Double.NaN);
			return Double.NaN;
		}
		
		Point pmr = Vector.difference(_ro, line.getPo());
		double numer = pmr.dot(_norm);
		
		double t = numer/ldotn;
		line.getP(t, intersection);
		return t;
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
		Point p =new Point(cphi, sphi, 0);
		
		//normal
		Vector norm = new Vector(sphi, -cphi, 0);
		
		return new Plane(norm, p);
	}
	
	public String toString() {
		double abcd[] = new double[4];
		getABCD(abcd);
		String pstr = String.format("abcd = [%10.6G, %10.6G, %10.6G, %10.6G]", abcd[0], abcd[1], abcd[2], abcd[3]);
		return pstr + "  p = " + _ro + " norm = " + _norm;
	}
	
	public static void main(String arg[]) {
//		Plane p = constantPhiPlane(30);
		
		Point p = new Point(1, 1, 1);
		Vector norm = new Vector(1, 1, 1);
		
		Plane plane = new Plane(norm, p);
		
		
		System.out.println("Init plane: " + plane);
		
		Point po = new Point(2, 4, -7);
		Point p1 = new Point(0, 2, 5);
		Point intersection = new Point();
		
		Line line = new Line(po, p1);
		double t = plane.lineIntersection(line, intersection);
		System.out.println(" t = " + t + "   intersect: " + intersection);
		
		double phi = -210;
		plane = constantPhiPlane(phi);
		System.out.println("constant phi plane phi = " + phi);
		t = plane.lineIntersection(line, intersection);
		System.out.println(" t = " + t + "   intersect: " + intersection + "  phicheck = " + Math.toDegrees(Math.atan2(intersection.y, intersection.x)));
		

	}

}
