package cnuphys.magfield;

import java.util.ArrayList;

public class RotatedCompositeProbe extends FieldProbe {
	
	// the angle in degrees
	private double _angle = -25.0;
	private double _sin = Math.sin(Math.toRadians(_angle));
	private double _cos = Math.cos(Math.toRadians(_angle));

	private ArrayList<FieldProbe> probes = new ArrayList<FieldProbe>();


	public RotatedCompositeProbe(RotatedCompositeField field) {
		super(field);
		for (IField f : field) {
			probes.add(FieldProbe.factory(f));
		}
	}
	
	/**
	 * Obtain the magnetic field at a given location expressed in Cartesian
	 * coordinates. The field is returned as a Cartesian vector in kiloGauss.
	 * @param sector the sector [1..6]
	 * @param xs
	 *            the x coordinate in cm
	 * @param ys
	 *            the y coordinate in cm
	 * @param zs
	 *            the z coordinate in cm
	 * @param result
	 *            the result is a float array holding the retrieved field in
	 *            kiloGauss. The 0,1 and 2 indices correspond to x, y, and z
	 *            components.
	 */	
	public void field(int sector, float xs, float ys, float zs, float[] result) {
		 //TODO Implement the sector effect to get the field from the right sector
		 //For now using sector 1

		 field(xs, ys, zs, result);
	}



	/**
	 * Obtain the magnetic field at a given location expressed in Cartesian
	 * coordinates. The field is returned as a Cartesian vector in kiloGauss.
	 * THIS ASSUMES COORDINATES ARE IN A SECTOR 1 SECTOR SYSTEM 
	 *
	 * @param xs
	 *            the x coordinate in cm in sector 1 sector system
	 * @param ys
	 *            the y coordinate in cm in sector 1 sector system
	 * @param zs
	 *            the z coordinate in cm in sector 1 sector system
	 * @param result
	 *            the result is a float array holding the retrieved field in
	 *            kiloGauss. The 0,1 and 2 indices correspond to x, y, and z
	 *            components.
	 */
	@Override
	public void field(float xs, float ys, float zs, float[] result) {
		
		//first rotate location
		double x = xs * _cos - zs * _sin;
		double y = ys;
		double z = zs * _cos + xs * _sin;

//		System.out.println("NEW R: [" + x + ", " + y + ", " + z + "]");

		float bx = 0, by = 0, bz = 0;
		for (FieldProbe probe : probes) {
			probe.field((float) x, (float) y, (float) z, result);
			bx += result[0];
			by += result[1];
			bz += result[2];
		}
		

		//now rotate the field in the opposite sense
		result[0] = (float) (bx * _cos + bz * _sin);
		result[1] = (by);
		result[2] = (float) (bz * _cos - bx * _sin);
		
	}


	@Override
	public void fieldCylindrical(double phi, double rho, double z, float[] result) {
		
		float bphi = 0;
		float brho = 0;
		float bz = 0;
		
		for (FieldProbe probe : probes) {
			probe.fieldCylindrical(phi, rho, z, result);
			bphi += result[0];
			brho += result[1];
			bz += result[2];
		}
		
		result[0] = bphi;
		result[1] = brho;
		result[2] = bz;
	}
	

    /**
     * Obtain an approximation for the magnetic field gradient at a given location expressed in cylindrical
     * coordinates. The field is returned as a Cartesian vector in kiloGauss/cm.
     *
     * @param phi
     *            azimuthal angle in degrees.
     * @param rho
     *            the cylindrical rho coordinate in cm.
     * @param z
     *            coordinate in cm
     * @param result
     *            the result
     * @result a Cartesian vector holding the calculated field in kiloGauss.
     */
	@Override
    public void gradientCylindrical(double phi, double rho, double z,
    	    float result[]) {
		
		float bx = 0, by = 0, bz = 0;
		for (FieldProbe probe : probes) {
			probe.gradientCylindrical(phi, rho, z, result);
			bx += result[0];
			by += result[1];
			bz += result[2];
		}
		result[0] = bx;
		result[1] = by;
		result[2] = bz;
   	
    }
	

    /**
     * Obtain an approximation for the magnetic field gradient at a given location expressed in Cartesian
     * coordinates. The field is returned as a Cartesian vector in kiloGauss/cm.
     *
     * @param x
     *            the x coordinate in cm
     * @param y
     *            the y coordinate in cm
     * @param z
     *            the z coordinate in cm
     * @param result
     *            a float array holding the retrieved field in kiloGauss. The
     *            0,1 and 2 indices correspond to x, y, and z components.
     */
     @Override
	public void gradient(float xs, float ys, float zs, float result[]) {
 		double x = xs * _cos - zs * _sin;
 		double y = ys;
 		double z = zs * _cos + xs * _sin;
 		float bx = 0, by = 0, bz = 0;
 		for (FieldProbe probe : probes) {
 			probe.gradient((float)x, (float)y, (float)z, result);
 			bx += result[0];
 			by += result[1];
 			bz += result[2];
 		}
		result[0] = (float) (bx * _cos + bz * _sin);
		result[1] = (by);
		result[2] = (float) (bz * _cos - bx * _sin);
     }
	
}
