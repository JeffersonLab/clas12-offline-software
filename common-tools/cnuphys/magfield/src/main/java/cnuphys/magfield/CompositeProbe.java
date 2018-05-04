package cnuphys.magfield;

import java.util.ArrayList;

public class CompositeProbe extends FieldProbe {
	
	private ArrayList<FieldProbe> probes = new ArrayList<FieldProbe>();

	public CompositeProbe(CompositeField field) {
		super(field);
		for (IField f : field) {
			probes.add(FieldProbe.factory(f));
		}
	}

	@Override
	public void fieldCylindrical(double phi, double rho, double z, float[] result) {
		float bx = 0;
		float by = 0;
		float bz = 0;
		
		for (FieldProbe probe : probes) {
			probe.fieldCylindrical(phi, rho, z, result);
			bx += result[0];
			by += result[1];
			bz += result[2];
		}
		
		result[0] = bx;
		result[1] = by;
		result[2] = bz;
	}
	
	 /**
	 * Check whether the field boundaries include the point
	 * 
	 * @param x
	 *            the x coordinate in the map units
	 * @param y
	 *            the y coordinate in the map units
	 * @param z
	 *            the z coordinate in the map units
	 * @return <code>true</code> if the point is included in the boundary of the
	 *         field
	 */
	@Override
	public boolean contained(float x, float y, float z) {
		double rho = Math.sqrt(x * x + y * y);
		double phi = MagneticField.atan2Deg(y, x);
		return containedCylindrical((float) phi, (float) rho, z);
	}
  
	/**
	 * Check whether the field boundaries include the point
	 * 
	 * @param phi
	 *            azimuthal angle in degrees.
	 * @param rho
	 *            the cylindrical rho coordinate in cm.
	 * @param z
	 *            coordinate in cm
	 * @return <code>true</code> if the point is included in the boundary of the
	 *         field
	 * 
	 */
	@Override
	public boolean containedCylindrical(float phi, float rho, float z) {
		for (FieldProbe probe : probes) {
			if (probe.containedCylindrical(phi, rho, z)) {
				return true;
			}
		}
		return false;
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
		phi = Math.toRadians(phi);
		double x = rho*Math.cos(phi);
    	double y = rho*Math.sin(phi);
    	gradient((float)x, (float)y, (float)z, result);   	
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
	public void gradient(float x, float y, float z, float result[]) {
 		
 		float temp[] = new float[3];
 		float max = 0f;

 		
 		// use max of underlying gradients
 		for (FieldProbe probe : probes) {
 			probe.gradient(x, y, z, temp);
 			float vlen = vectorLength(temp);
 			
 			if (vlen > max) {
 				result[0] = temp[0];
 				result[1] = temp[1];
 				result[2] = temp[2];
 				max = vlen;
 			}
 		}
     }
    
	
}
