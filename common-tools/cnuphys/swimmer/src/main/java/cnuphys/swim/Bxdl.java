package cnuphys.swim;

import cnuphys.magfield.FieldProbe;
import cnuphys.magfield.RotatedCompositeProbe;

public class Bxdl {

	// the cumulative pathlength in meters
	private double _pathlength;

	// the cumulative integral B*dl in kiloGauss-meters
	private double _bxdl;
	
	/**
	 * Accumulate the integral of b cross dl
	 * 
	 * @param previous
	 *            the previous accumulation
	 * @param current
	 *            the (after returning) current accumulation
	 * @param p0
	 *            the starting [x,y,z] position
	 * @param p1
	 *            the ending [x,y,z] position
	 * @param probe
	 *            the object that can return the magnetic field
	 */
	public static void accumulate(Bxdl previous, Bxdl current, double[] p0, double[] p1, FieldProbe probe) {

		if (probe instanceof RotatedCompositeProbe) {
			System.err.println("SHOULD NOT HAPPEN. In rotated composite field probe, should not call Bxdl accumlate without the sector argument.");

			(new Throwable()).printStackTrace();
			System.exit(1);
			
		}

		double dr[] = new double[3];

		for (int i = 0; i < 3; i++) {
			dr[i] = p1[i] - p0[i];
		}

		double pathlength = vecmag(dr);

		// use the average position (in cm) to compute B for b cross dl
		float xavgcm = (float) (100. * (p0[0] + p1[0]) / 2);
		float yavgcm = (float) (100. * (p0[1] + p1[1]) / 2);
		float zavgcm = (float) (100. * (p0[2] + p1[2]) / 2);
		float b[] = new float[3];
		probe.field(xavgcm, yavgcm, zavgcm, b);

		double bxdl[] = cross(b, dr);
		double magbxdl = vecmag(bxdl);

		// make it cumulative if we have a previous
		if (previous != null) {
			pathlength += previous._pathlength;
			magbxdl += previous._bxdl;
		}

		// set the current
		current._pathlength = pathlength;
		current._bxdl = magbxdl;
	}
	
	/**
	 * Accumulate the integral of b cross dl
	 * 
	 * @param previous
	 *            the previous accumulation
	 * @param current
	 *            the (after returning) current accumulation
	 * @param p0
	 *            the starting [x,y,z] position
	 * @param p1
	 *            the ending [x,y,z] position
	 * @param probe
	 *            the object that can return the magnetic field
	 */
	public static void sectorAccumulate(int sector, Bxdl previous, Bxdl current, double[] p0, double[] p1, RotatedCompositeProbe probe) {

		double dr[] = new double[3];

		for (int i = 0; i < 3; i++) {
			dr[i] = p1[i] - p0[i];
		}

		double pathlength = vecmag(dr);

		// use the average position (in cm) to compute B for b cross dl
		float xavgcm = (float) (100. * (p0[0] + p1[0]) / 2);
		float yavgcm = (float) (100. * (p0[1] + p1[1]) / 2);
		float zavgcm = (float) (100. * (p0[2] + p1[2]) / 2);
		float b[] = new float[3];
		probe.field(sector, xavgcm, yavgcm, zavgcm, b);

		double bxdl[] = cross(b, dr);
		double magbxdl = vecmag(bxdl);

		// make it cumulative if we have a previous
		if (previous != null) {
			pathlength += previous._pathlength;
			magbxdl += previous._bxdl;
		}

		// set the current
		current._pathlength = pathlength;
		current._bxdl = magbxdl;
	}

	/**
	 * Get the cumulative pathlength in meters
	 * 
	 * @return the cumulative pathlength in meters
	 */
	public double getPathlength() {
		return _pathlength;
	}

	/**
	 * Get the cumulative integral |b cross dl| in kG-m
	 * 
	 * @return the cumulative integral b cross dl in kG-m
	 */
	public double getIntegralBxdl() {
		return _bxdl;
	}

	/**
	 * Set the cumulative pathlength in meters
	 * 
	 * @param pathlength
	 *            the cumulative pathlength in meters
	 */
	public void setPathlength(double pathlength) {
		_pathlength = pathlength;
	}

	/**
	 * Set the cumulative integral |b cross dl| in kG-m
	 * 
	 * @param bxdl
	 *            the cumulative integral b cross dl in kG-m
	 */
	public void setIntegralBxdl(double bxdl) {
		_bxdl = bxdl;
	}

	/**
	 * Set the values based on another object
	 * 
	 * @param bxdl
	 *            the other object
	 */
	public void set(Bxdl bxdl) {
		_pathlength = bxdl._pathlength;
		_bxdl = bxdl._bxdl;
	}

	// usual cross product
	private static double[] cross(float a[], double b[]) {
		double c[] = new double[3];
		c[0] = a[1] * b[2] - a[2] * b[1];
		c[1] = a[2] * b[0] - a[0] * b[2];
		c[2] = a[0] * b[1] - a[1] * b[0];
		return c;
	}


//	/**
//	 * Accumulate the integral of b cross dl
//	 * 
//	 * @param previous
//	 *            the previous accumulation
//	 * @param current
//	 *            the (after returning) current accumulation
//	 * @param p0
//	 *            the starting [x,y,z] position
//	 * @param p1
//	 *            the ending [x,y,z] position
//	 * @param probe
//	 *            the object that can return the magnetic field
//	 */
//	public static void accumulate(Bxdl previous, Bxdl current, double[] p0, double[] p1, FieldProbe probe) {
//
//		if (probe instanceof RotatedCompositeProbe) {
//			System.err.println("SHOULD NOT HAPPEN. In rotated composite field probe, should not call Bxdl accumlate without the sector argument.");
//
//			(new Throwable()).printStackTrace();
//			System.exit(1);
//			
//		}
//
//		double dr[] = new double[3];
//		double bxdl[] = new double[3];
//
//		for (int i = 0; i < 3; i++) {
//			dr[i] = p1[i] - p0[i];
//		}
//
//		double pathlength = vecmag(dr);
//		float b[] = new float[3];
//		
//		//how many steps?
//		double minstep = 0.01;  //m
//		int ns = Math.max(2, (int)(pathlength/minstep));
//		
//		double[] dt = new double[3];
//		
//		double r0[] = new double[3];
//		
//		for (int i = 0; i < 3; i++) {
//			r0[i] = p0[i];
//			dt[i] = (p1[i] - p0[i])/ns;
//		}
//		
//		double magbxdl = 0;
//		
//		for (int i =  1; i <= ns; i++) {
//			magbxdl += accumulateBDL(r0, dt, b, bxdl, probe);
//			
//			for (int j = 0; j < 3; j++) {
//				r0[j] += dt[j];
//			}
//
//		}
//		
//		// set the current
//		if (previous != null) {
//			pathlength += previous._pathlength;
//			magbxdl += previous._bxdl;
//		}
//
//		current._pathlength = pathlength;
//		current._bxdl = magbxdl;
//
//	}
//	
//	private static double accumulateBDL(double[] p0, double[] dr, float[] b, double[] bxdl, FieldProbe probe) {
//		float xavgcm = (float) (100. * (p0[0] + dr[0]/2));
//		float yavgcm = (float) (100. * (p0[1] + dr[1]/2));
//		float zavgcm = (float) (100. * (p0[2] + dr[2]/2));
//		 
//		probe.field(xavgcm, yavgcm, zavgcm, b);
//
//		cross(b, dr, bxdl);
//		return vecmag(bxdl);
//	}
//	
//	private static double sectorAccumulateBDL(int sector, double[] p0, double[] dr, float[] b, double[] bxdl, FieldProbe probe) {
//		float xavgcm = (float) (100. * (p0[0] + dr[0]/2));
//		float yavgcm = (float) (100. * (p0[1] + dr[1]/2));
//		float zavgcm = (float) (100. * (p0[2] + dr[2]/2));
//		 
//		probe.field(sector, xavgcm, yavgcm, zavgcm, b);
//
//		cross(b, dr, bxdl);
//		return vecmag(bxdl);
//	}
//	
//	/**
//	 * Accumulate the integral of b cross dl
//	 * 
//	 * @param previous
//	 *            the previous accumulation
//	 * @param current
//	 *            the (after returning) current accumulation
//	 * @param p0
//	 *            the starting [x,y,z] position
//	 * @param p1
//	 *            the ending [x,y,z] position
//	 * @param probe
//	 *            the object that can return the magnetic field
//	 */
//	public static void sectorAccumulate(int sector, Bxdl previous, Bxdl current, double[] p0, double[] p1, RotatedCompositeProbe probe) {
//
//		double dr[] = new double[3];
//		double bxdl[] = new double[3];
//
//		for (int i = 0; i < 3; i++) {
//			dr[i] = p1[i] - p0[i];
//		}
//
//		double pathlength = vecmag(dr);
//		float b[] = new float[3];
//		
//		//how many steps?
//		double minstep = 0.01;  //m
//		int ns = Math.max(2, (int)(pathlength/minstep));
//		
//		double[] dt = new double[3];
//		
//		double r0[] = new double[3];
//		
//		for (int i = 0; i < 3; i++) {
//			r0[i] = p0[i];
//			dt[i] = (p1[i] - p0[i])/ns;
//		}
//		
//		double magbxdl = 0;
//		
//		for (int i =  1; i <= ns; i++) {
//			magbxdl += sectorAccumulateBDL(sector, r0, dt, b, bxdl, probe);
//			
//			for (int j = 0; j < 3; j++) {
//				r0[j] += dt[j];
//			}
//
//		}
//		
//		// set the current
//		if (previous != null) {
//			pathlength += previous._pathlength;
//			magbxdl += previous._bxdl;
//		}
//
//		current._pathlength = pathlength;
//		current._bxdl = magbxdl;
//
//	}
//
//	/**
//	 * Get the cumulative pathlength in meters
//	 * 
//	 * @return the cumulative pathlength in meters
//	 */
//	public double getPathlength() {
//		return _pathlength;
//	}
//
//	/**
//	 * Get the cumulative integral |b cross dl| in kG-m
//	 * 
//	 * @return the cumulative integral b cross dl in kG-m
//	 */
//	public double getIntegralBxdl() {
//		return _bxdl;
//	}
//
//	/**
//	 * Set the cumulative pathlength in meters
//	 * 
//	 * @param pathlength
//	 *            the cumulative pathlength in meters
//	 */
//	public void setPathlength(double pathlength) {
//		_pathlength = pathlength;
//	}
//
//	/**
//	 * Set the cumulative integral |b cross dl| in kG-m
//	 * 
//	 * @param bxdl
//	 *            the cumulative integral b cross dl in kG-m
//	 */
//	public void setIntegralBxdl(double bxdl) {
//		_bxdl = bxdl;
//	}
//
//	/**
//	 * Set the values based on another object
//	 * 
//	 * @param bxdl
//	 *            the other object
//	 */
//	public void set(Bxdl bxdl) {
//		_pathlength = bxdl._pathlength;
//		_bxdl = bxdl._bxdl;
//	}
//
//	// usual cross product
//	private static void cross(float a[], double b[], double c[]) {
//		c[0] = a[1] * b[2] - a[2] * b[1];
//		c[1] = a[2] * b[0] - a[0] * b[2];
//		c[2] = a[0] * b[1] - a[1] * b[0];
//	}

	// usual vec mag
	private static double vecmag(double a[]) {
		double asq = a[0] * a[0] + a[1] * a[1] + a[2] * a[2];
		return Math.sqrt(asq);
	}
}
