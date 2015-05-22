package cnuphys.magfield;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class RotatedCompositeField extends ArrayList<IField> implements IField {

    // the angle in degrees
    private float _angle = -25.0f;
    private float _sin = (float) Math.sin(Math.toRadians(_angle));
    private float _cos = (float) Math.cos(Math.toRadians(_angle));

    /**
     * Set the rotation angle
     * 
     * @param angle
     *            the rotation angle in degrees
     */
    public void setRotationAngle(float angle) {
	_angle = angle;
	_sin = (float) Math.sin(Math.toRadians(_angle));
	_cos = (float) Math.cos(Math.toRadians(_angle));
    }

    /**
     * Obtain the magnetic field at a given location expressed in Cartesian
     * coordinates. The field is returned as a Cartesian vector in kiloGauss.
     *
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
    @Override
    public void field(float xs, float ys, float zs, float[] result) {

	float x = xs * _cos - zs * _sin;
	float y = ys;
	float z = zs * _cos + xs * _sin;

	float bx = 0, by = 0, bz = 0;
	for (IField field : this) {
	    field.field(x, y, z, result);
	    bx += result[0];
	    by += result[1];
	    bz += result[2];
	}
	result[0] = bx * _cos + bz * _sin;
	result[1] = by;
	result[2] = bz * _cos - bx * _sin;
    }

    /**
     * Check whether the field is ready to be used.For a composite field, all
     * fields must be ready.
     * 
     * @return <code>true</code> if the field is ready.
     */
    @Override
    public boolean isFieldLoaded() {
	for (IField field : this) {
	    if (!field.isFieldLoaded()) {
		return false;
	    }
	}
	return true;
    }

    /**
     * Read a magnetic field from a binary file. The file has the documented
     * format. Since this object is a composite field, this should not be called
     * so an empty implementation is provided to complete the interface. The
     * individual field that make up this composite field should have had their
     * readBinaryMagneticField methods called.
     *
     * @param binaryFile
     *            the binary file.
     * @throws FileNotFoundException
     *             the file not found exception
     */
    @Override
    public void readBinaryMagneticField(File binaryFile)
	    throws FileNotFoundException {
    }

    // the rotation only works for the Cartesian call
    // TODO fix
    @Override
    public void fieldCylindrical(double phi, double rho, double z,
	    float[] result) {
	System.err.println("Cannot use cylindrical call for rotated field");
	result[0] = Float.NaN;
	result[1] = Float.NaN;
	result[2] = Float.NaN;
    }

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
    @Override
    public float fieldMagnitudeCylindrical(double phi, double r, double z) {
	float result[] = new float[3];
	fieldCylindrical(phi, r, z, result);
	return vectorLength(result);
    }

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
    @Override
    public float fieldMagnitude(float x, float y, float z) {
	float result[] = new float[3];
	field(x, y, z, result);
	return vectorLength(result);

    }

    /**
     * Vector length.
     *
     * @param v
     *            the v
     * @return the float
     */
    private float vectorLength(float v[]) {
	float vx = v[0];
	float vy = v[1];
	float vz = v[2];
	return (float) Math.sqrt(vx * vx + vy * vy + vz * vz);
    }

    @Override
    public float getMaxFieldMagnitude() {
	float maxField = 0f;
	for (IField field : this) {
	    maxField = Math.max(maxField, field.getMaxFieldMagnitude());
	}
	return maxField;
    }

}
