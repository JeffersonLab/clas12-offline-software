package cnuphys.magfield;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * A composition of multiple magnetic field maps. The resulting magnetic field
 * at a given point is the sum of the constituent fields at that point.
 * 
 * @author sebouhpaul
 *
 */
@SuppressWarnings("serial")
public class CompositeField extends ArrayList<IField> implements IField {

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
	 *            the result is a float array holding the retrieved field in
	 *            kiloGauss. The 0,1 and 2 indices correspond to x, y, and z
	 *            components.
	 */
	@Override
	public void field(float x, float y, float z, float[] result) {

		float bx = 0, by = 0, bz = 0;
		for (IField field : this) {
			field.field(x, y, z, result);
			bx += result[0];
			by += result[1];
			bz += result[2];
		}
		result[0] = bx;
		result[1] = by;
		result[2] = bz;
	}

	/**
	 * Checks whether the field has been set to always return zero.
	 * 
	 * @return <code>true</code> if the field is set to return zero.
	 */
	@Override
	public final boolean isZeroField() {

		for (IField ifield : this) {
			if (!(ifield.isZeroField())) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean add(IField field) {
		if (field instanceof CompositeField) {
			System.err.println("Cannot add composite field to a composite field.");
			return false;
		}

		// remove(field); //prevent duplicates

		// further check, only one solenoid or one torus
		// (might have different instances for some reason)

		for (IField ifield : this) {
			if (ifield.getClass().equals(field.getClass())) {
				remove(ifield);
				break;
			}
		}

		return super.add(field);
	}

	@Override
	public String getName() {
		String s = "Composite contains: ";

		int count = 1;
		for (IField field : this) {
			if (count == 1) {
				s += field.getName();
			} else {
				s += " + " + field.getName();
			}
			count++;
		}

		return s;
	}

	/**
	 * Is the physical magnet represented by any of the maps misaligned?
	 * 
	 * @return <code>true</code> if any magnet is misaligned
	 */
	@Override
	public boolean isMisaligned() {
		for (IField field : this) {
			if (field.isMisaligned()) {
				return true;
			}
		}
		return false;
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
	public void readBinaryMagneticField(File binaryFile) throws FileNotFoundException {
	}

	@Override
	public void fieldCylindrical(double phi, double rho, double z, float[] result) {

		float bx = 0, by = 0, bz = 0;
		for (IField field : this) {
			field.fieldCylindrical(phi, rho, z, result);
			bx += result[0];
			by += result[1];
			bz += result[2];
		}
		result[0] = bx;
		result[1] = by;
		result[2] = bz;
	}

	/**
	 * Obtain an approximation for the magnetic field gradient at a given
	 * location expressed in cylindrical coordinates. The field is returned as a
	 * Cartesian vector in kiloGauss/cm.
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
	public void gradientCylindrical(double phi, double rho, double z, float result[]) {
		phi = Math.toRadians(phi);
		double x = rho * Math.cos(phi);
		double y = rho * Math.sin(phi);
		gradient((float) x, (float) y, (float) z, result);
	}

	/**
	 * Obtain an approximation for the magnetic field gradient at a given
	 * location expressed in Cartesian coordinates. The field is returned as a
	 * Cartesian vector in kiloGauss/cm.
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
		for (IField field : this) {
			field.gradient(x, y, z, temp);
			float vlen = vectorLength(temp);
			
			if (vlen > max) {
				result[0] = temp[0];
				result[1] = temp[1];
				result[2] = temp[2];
				max = vlen;
			}
		}

		// use three point derivative
//		float del = 1f; // cm
//		float del2 = 2 * del;

		// float baseVal = fieldMagnitude(x, y, z);
		// float bv3 = -3*baseVal;
		//
		// System.err.println("---------");
		// System.err.println("baseVal " + baseVal);
		//
		// float bx0 = fieldMagnitude(x+del, y, z);
		// float bx1 = fieldMagnitude(x+del2, y, z);
		//
		// float by0 = fieldMagnitude(x, y+del, z);
		// float by1 = fieldMagnitude(x, y+del2, z);
		// float bz0 = fieldMagnitude(x, y, z+del);
		// float bz1 = fieldMagnitude(x, y, z+del2);
		//
		// System.err.println("bx0 " + bx0);
		// System.err.println("bx1 " + bx1);
		// System.err.println("by0 " + by0);
		// System.err.println("by1 " + by1);
		// System.err.println("bz0 " + bz0);
		// System.err.println("bz1 " + bz1);
		//
		// result[0] = (bv3 + 4*bx0 - bx1)/del2;
		// result[1] = (bv3 + 4*by0 - by1)/del2;
		// result[2] = (bv3 + 4*bz0 - bz1)/del2;

		// two point
//		float bx0 = fieldMagnitude(x - del, y, z);
//		float bx1 = fieldMagnitude(x + del, y, z);
//		float by0 = fieldMagnitude(x, y - del, z);
//		float by1 = fieldMagnitude(x, y + del, z);
//		float bz0 = fieldMagnitude(x, y, z - del);
//		float bz1 = fieldMagnitude(x, y, z + del);
//
//		result[0] = (bx1 - bx0) / del2;
//		result[1] = (by1 - by0) / del2;
//		result[2] = (bz1 - bz0) / del2;

		// System.err.println("result: " + result[0] + ", " + result[1] + ", " +
		// result[2]);

		// float bx = 0, by = 0, bz = 0;
		// for (IField field : this) {
		// field.gradient(x, y, z, result);
		// bx += result[0];
		// by += result[1];
		// bz += result[2];
		// }
		// result[0] = bx;
		// result[1] = by;
		// result[2] = bz;
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

	/**
	 * Check whether we have a torus field
	 * 
	 * @return <code>true</code> if we have a torus
	 */
	public boolean hasTorus() {
		for (IField field : this) {
			if (field instanceof Torus) {
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
		for (IField field : this) {
			if (field instanceof Solenoid) {
				return true;
			}
		}

		return false;
	}

}
