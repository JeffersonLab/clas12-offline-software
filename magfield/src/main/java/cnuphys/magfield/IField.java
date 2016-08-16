/*
 * 
 */
package cnuphys.magfield;

import java.io.File;
import java.io.FileNotFoundException;

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
     * Get the field by trilinear interpolation.
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
    public void fieldCylindrical(double phi, double rho, double z,
	    float result[]);

    /**
     * Obtain the magnetic field at a given location expressed in Cartesian
     * coordinates. The field is returned as a Cartesian vector in kiloGauss.
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
    public void field(float x, float y, float z, float result[]);

    /**
     * Get the field magnitude in kiloGauss at a given location expressed in
     * cylindrical coordinates.
     * 
     * @param phi
     *            azimuthal angle in degrees.
     * @param r
     *            in cm.
     * @param z
     *            in cm
     * @return the magnitude of the field in kiloGauss.
     */
    public float fieldMagnitudeCylindrical(double phi, double r, double z);

    /**
     * Get the field magnitude in kiloGauss at a given location expressed in
     * Cartesian coordinates.
     * 
     * @param x
     *            the x coordinate in cm
     * @param y
     *            the y coordinate in cm
     * @param z
     *            the z coordinate in cm
     * @return the magnitude of the field in kiloGauss.
     */
    public float fieldMagnitude(float x, float y, float z);

    /**
     * Get the maximum field magnitude in kiloGauss
     * 
     * @return the maximum field magnitude in kiloGauss
     */
    public float getMaxFieldMagnitude();

    /**
     * Read a magnetic field from a binary file. The file has the documented
     * format.
     *
     * @param binaryFile
     *            the binary file.
     * @throws FileNotFoundException
     *             the file not found exception
     */
    public void readBinaryMagneticField(File binaryFile)
	    throws FileNotFoundException;
}
