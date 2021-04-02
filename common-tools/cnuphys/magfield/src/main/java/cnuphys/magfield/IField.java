/*
 * 
 */
package cnuphys.magfield;

/**
 * The Interface IField.
 * 
 * @author David Heddle
 * @author Nicole Schumacher
 * @version 1.0
 */
public interface IField {

	public String getName();

	/**
	 * Obtain the magnetic field at a given location expressed in Cartesian
	 * coordinates. The field is returned as a Cartesian vector in kiloGauss.
	 *
	 * @param x      the x coordinate in cm
	 * @param y      the y coordinate in cm
	 * @param z      the z coordinate in cm
	 * @param result a float array holding the retrieved field in kiloGauss. The 0,1
	 *               and 2 indices correspond to x, y, and z components.
	 */
	public void field(float x, float y, float z, float result[]);

	/**
	 * Obtain the magnetic field at a given location expressed in Cartesian
	 * coordinates for the sector system. The other "field" methods are for the lab
	 * system. The field is returned as a Cartesian vector in kiloGauss.
	 * 
	 * @param sector the sector [1..6]
	 * @param x      the x sector coordinate in cm
	 * @param y      the y sector coordinate in cm
	 * @param z      the z sector coordinate in cm
	 * @param result the result is a float array holding the retrieved field in
	 *               kiloGauss. The 0,1 and 2 indices correspond to x, y, and z
	 *               components.
	 */
	public void field(int sector, float xs, float ys, float zs, float[] result);


	/**
	 * Get the field magnitude in kiloGauss at a given location expressed in
	 * Cartesian coordinates.
	 * 
	 * @param x the x coordinate in cm
	 * @param y the y coordinate in cm
	 * @param z the z coordinate in cm
	 * @return the magnitude of the field in kiloGauss.
	 */
	public float fieldMagnitude(float x, float y, float z);


	/**
	 * Obtain an approximation for the magnetic field gradient at a given location
	 * expressed in Cartesian coordinates. The field is returned as a Cartesian
	 * vector in kiloGauss/cm.
	 *
	 * @param x      the x coordinate in cm
	 * @param y      the y coordinate in cm
	 * @param z      the z coordinate in cm
	 * @param result a float array holding the retrieved field in kiloGauss. The 0,1
	 *               and 2 indices correspond to x, y, and z components.
	 */
	public void gradient(float x, float y, float z, float result[]);

	/**
	 * Get the maximum field magnitude in kiloGauss
	 * 
	 * @return the maximum field magnitude in kiloGauss
	 */
	public float getMaxFieldMagnitude();

	/**
	 * Check whether this field is the zero field (possibly because the scale factor
	 * is set to 0)
	 * 
	 * @return <code>true</code> if this field is a zero field
	 */
	public boolean isZeroField();

	/**
	 * Check whether the field boundaries include the point
	 * 
	 * @param x the x coordinate in the map units
	 * @param y the y coordinate in the map units
	 * @param z the z coordinate in the map units
	 * @return <code>true</code> if the point is included in the boundary of the
	 *         field
	 */
	public boolean contains(double x, double y, double z);


}
