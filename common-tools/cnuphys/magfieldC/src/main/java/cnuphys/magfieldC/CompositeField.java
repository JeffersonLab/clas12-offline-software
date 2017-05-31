package cnuphys.magfieldC;

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
	 *            the result vector
	 */
	@Override
	public void field(float x, float y, float z, CartesianVector result) {
		
		float bx = 0, by = 0, bz = 0;
		for (IField field : this) {
			field.field(x, y, z, result);
			bx += result.x;
			by += result.y;
			bz += result.z;
		}
		result.x = bx;
		result.y = by;
		result.z = bz;
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
	public void fieldCylindrical(double phi, double rho, double z, CartesianVector result) {
		float bx = 0, by = 0, bz = 0;
		for (IField field : this) {
			field.fieldCylindrical(phi, rho, z, result);
			bx += result.x;
			by += result.y;
			bz += result.z;
		}
		result.x = bx;
		result.y = by;
		result.z = bz;
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
		CartesianVector result = new CartesianVector();
		fieldCylindrical(phi, r, z, result);
		return (float) result.length();
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
		CartesianVector result = new CartesianVector();
		field(x, y, z, result);
		return (float) result.length();

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
