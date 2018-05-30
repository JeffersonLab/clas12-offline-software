package cnuphys.magfield;

import java.util.ArrayList;

public class CompositeProbe extends FieldProbe {
	
	protected ArrayList<IField> probes = new ArrayList<IField>();

	public CompositeProbe(CompositeField field) {
		super(field);
		for (IField f : field) {
			probes.add(FieldProbe.factory(f));
		}

	}
	
	
	/**
	 * Obtain the magnetic field at a given location expressed in Cartesian
	 * coordinates for the sector system. The field is returned as a Cartesian vector in kiloGauss.
	 * @param sector the sector [1..6]
	 * @param x
	 *            the x sector coordinate in cm
	 * @param y
	 *            the y sector coordinate in cm
	 * @param z
	 *            the z sector coordinate in cm
	 * @param result
	 *            the result is a float array holding the retrieved field in
	 *            kiloGauss. The 0,1 and 2 indices correspond to x, y, and z
	 *            components.
	 */	
	@Override
	public void field(int sector, float x, float y, float z, float[] result) {
				
		
		//rotate to the correct sector to get the lab coordinates. We can use the result array!
		MagneticFields.sectorToLab(sector, result, x, y, z);
		x = result[0];
		y = result[1];
		z = result[2];


		float bx = 0, by = 0, bz = 0;
		for (IField probe : probes) {
			probe.field(x, y, z, result);
			bx += result[0];
			by += result[1];
			bz += result[2];
		}
		
		//rotate back
		MagneticFields.labToSector(sector, result, bx, by, bz);
	}
	
	
	@Override
	public void field(float x, float y, float z, float result[]) {
		
		float bx = 0;
		float by = 0;
		float bz = 0;
		
		for (IField probe : probes) {
			probe.field(x, y, z, result);
			bx += result[0];
			by += result[1];
			bz += result[2];
		}
		
		result[0] = bx;
		result[1] = by;
		result[2] = bz;
	}

	
	@Override
	public void fieldCylindrical(double phi, double rho, double z, float[] result) {
		
		float bx = 0;
		float by = 0;
		float bz = 0;
		
		for (IField probe : probes) {
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
		for (IField probe : probes) {
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
	public void gradient(float x, float y, float z, float result[]) {
 		float bx = 0, by = 0, bz = 0;
 		for (IField probe : probes) {
 			probe.gradient(x, y, z, result);
 			bx += result[0];
 			by += result[1];
 			bz += result[2];
 		}
 		result[0] = bx;
 		result[1] = by;
 		result[2] = bz;
     }
    
	
 	/**
 	 * Check whether we have a torus field
 	 * 
 	 * @return <code>true</code> if we have a torus
 	 */
 	public boolean hasTorus() {
 		for (IField field : probes) {
 			if ((field instanceof TorusProbe) || ((field instanceof Torus))) {
 				return true;
 			}
 		}

 		return false;
	}

	/**
	 * Check whether we have a solenoid field
	 * 
	 * @return <code>true</code> if we have a solenoid
	 */
	public boolean hasSolenoid() {
		for (IField field : probes) {
			if ((field instanceof SolenoidProbe) || ((field instanceof Solenoid))) {
				return true;
			}
		}

		return false;
	}
	
}
