package cnuphys.magfield;

public class DoubleTorusProbe extends FieldProbe {
	
	private TorusProbe _torus1;
	private TorusProbe _torus2;

	/**
	 * Create a composite probe from a composite field.
	 * @param field the composite field
	 */
	public DoubleTorusProbe(DoubleTorus field) {
		super(field);
		_torus1 = new TorusProbe(field._torus1, false);
		_torus2 = new TorusProbe(field._torus2, false);
		
		_torus1._notDoubleTorus = false;
		_torus2._notDoubleTorus = false;
		
		field._torus1._notDoubleTorus = false;
		field._torus2._notDoubleTorus = false;
	
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


		float bx1 = 0, by1 = 0, bz1 = 0;
		float bx2 = 0, by2 = 0, bz2 = 0;
		
		_torus1.field(x, y, z, result);
		bx1 = result[0];
		by1 = result[1];
		bz1 = result[2];
		
		_torus2.field(x, y, z, result);
		bx2 = result[0];
		by2 = result[1];
		bz2 = result[2];
	
		//rotate back
		MagneticFields.labToSector(sector, result, bx2-bx1, by2-by1, bz2-bz1);
	}
	
	
	@Override
	public void field(float x, float y, float z, float result[]) {
		
		float bx1 = 0, by1 = 0, bz1 = 0;
		float bx2 = 0, by2 = 0, bz2 = 0;
		
		_torus1.field(x, y, z, result);
		bx1 = result[0];
		by1 = result[1];
		bz1 = result[2];
		
		_torus2.field(x, y, z, result);
		bx2 = result[0];
		by2 = result[1];
		bz2 = result[2];
		
		result[0] = bx2 - bx1;
		result[1] = by2 - by1;
		result[2] = bz2 - bz1;
	}
	
	public void diagnosticField(float x, float y, float z) {
		float result[] = new float[3];
		
		_torus1.field(x, y, z, result);
		System.out.println(String.format("Torus1: (%-9.5f, %-9.5f, %-9.5f)", result[0], result[1], result[2]));
		
		
		_torus2.field(x, y, z, result);
		System.out.println(String.format("Torus2: (%-9.5f, %-9.5f, %-9.5f)", result[0], result[1], result[2]));

	
		field(x, y, z, result);
		System.out.println(String.format("Double: (%-9.5f, %-9.5f, %-9.5f)", result[0], result[1], result[2]));
}

	/**
	 * Checks whether the field has been set to always return zero.
	 * 
	 * @return <code>true</code> if the field is set to return zero.
	 */
	@Override
	public boolean isZeroField() {
		return false;
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
 		result[0] = Float.NaN;
 		result[1] = Float.NaN;
 		result[2] = Float.NaN;
     }
    
	
 	/**
 	 * Check whether we have a torus field
 	 * 
 	 * @return <code>true</code> if we have a torus
 	 */
 	public boolean hasTorus() {
  		return true;
	}

	/**
	 * Check whether we have a solenoid field
	 * 
	 * @return <code>true</code> if we have a solenoid
	 */
	public boolean hasSolenoid() {
		return false;
	}

}
