package cnuphys.magfield;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * For magnetic fields stored in a specific format.
 * 
 * @author David Heddle
 * @author Nicole Schumacher
 * @version 1.0
 */
public abstract class MagneticField implements IField {


	/** Magic number used to check if byteswapping is necessary. */
	public static final int MAGICNUMBER = 0xced;

	/**
	 * Index where max field magnitude resides
	 */
	protected int maxFieldIndex = -1;

	/** The maximum magnitude of the field. */
	protected float maxField = (float) -1.0e10;

	/** The max vector field. */
	protected final float maxVectorField[] = new float[3];

	/** The location of the maximum field. */
	protected final float maxFieldLocation[] = new float[3];

	/** The average magnitude of the field. */
	protected float avgField = Float.NaN;

	/** The grid coordinate system. */
	protected CoordinateSystem gridCoordinateSystem;

	/** The field coordinate system. */
	protected CoordinateSystem fieldCoordinateSystem;

	/** The length unit. */
	protected LengthUnit lengthUnit;

	/** The angular unit. */
	protected AngularUnit angularUnit;

	/** The field unit. */
	protected FieldUnit fieldUnit;

	/** holds the field in a float buffer. */
	protected FloatBuffer field;

	/** high word of unix creation time */
	protected int highTime;

	/** low word of unix creation time */
	protected int lowTime;

	/** reserved word */
	protected int reserved3;

	/** reserved word */
	protected int reserved4;

	/** reserved word */
	protected int reserved5;

	/** The coordinate 1 name. (ced and ded: 'phi') */
	private String _q1Name = "phi";

	/** The coordinate 2 name. (ced: 'rho' ded: 'z') */
	private String _q2Name = "rho";

	/** The coordinate 3 name. (ced: 'z' ded: 'rho') */
	private String _q3Name = "z";

	/** the full path to the file */
	private String _baseFileName;
	
	/** shift in z direction in cm (misalignment) */
	protected double _shiftZ; //cm
	
	// for rotating field
	protected static final double ROOT3OVER2 = 0.866025403784439;
	protected static final double cosSect[] = {Double.NaN, 1, 0.5, -0.5, -1, -0.5, 0.5};
	protected static final double sinSect[] = {Double.NaN, 0, ROOT3OVER2, ROOT3OVER2, 0, -ROOT3OVER2, -ROOT3OVER2};
	
	/**
	 * Holds the grid info for the slowest changing coordinate (as stored in the
	 * file).
	 */
	protected GridCoordinate q1Coordinate;

	/**
	 * Holds the grid info for the medium changing coordinate (as stored in the
	 * file).
	 */
	protected GridCoordinate q2Coordinate;

	/**
	 * Holds the grid info for the fastest changing coordinate (as stored in the
	 * file).
	 */
	protected GridCoordinate q3Coordinate;

	/** Total number of field points. */
	protected int numFieldPoints;

	// used internally for index calculations
	// private int N23 = -1;

	// used internally for index calculations
	// private int N3;

	// scale factor always treated as positive
	protected double _scaleFactor = 1.0;

	// determine whether we use interpolation or nearest neighbor
	protected static boolean _interpolate = true;

	// indices of components
	protected static final int X = 0;
	protected static final int Y = 1;
	protected static final int Z = 2;
	

	/**
	 * Scale the field.
	 * 
	 * @param scale
	 *            the scale factor
	 */
	public final void setScaleFactor(double scale) {
		_scaleFactor = scale;
		MagneticFields.getInstance().changedScale(this);
	}
	
	/**
	 * Change the shift in the z direction
	 * @param shiftZ the shift in cm
	 */
	public final void setShiftZ(double shiftZ) {
		_shiftZ = shiftZ;
		MagneticFields.getInstance().changedShift(this);
	}

	/**
	 * Get the factor that scales the field. Only scale factors between 0 and 1
	 * are permitted. For negative scale factors, use in combination with an
	 * inverted setting
	 * 
	 * @param scale
	 *            the scale factor between 0 and 1
	 */
	public final double getScaleFactor() {
		return _scaleFactor;
	}
	
	/**
	 * Get the shift in z. 
	 * @return the z shift in cm.
	 */
	public final double getShiftZ() {
		return _shiftZ;
	}
	
	/**
     * Is the physical magnet represented by the map misaligned?
     * @return <code>true</code> if magnet is misaligned
     */
	@Override
    public boolean isMisaligned() {
    	return false;
    }

	/**
	 * Checks whether the field has been set to always return zero.
	 * 
	 * @return <code>true</code> if the field is set to return zero.
	 */
	@Override
	public final boolean isZeroField() {
		return (Math.abs(_scaleFactor) < 1.0e-6);
	}

	/**
	 * For debugging you can set the field to always return 0.
	 * 
	 * @param zeroField
	 *            if set to <code>true</code> the field will always return 0.
	 */
	public final void setZeroField(boolean zeroField) {
		setScaleFactor(0.0);
	}

	/**
	 * Obtain the magnetic field at a given location expressed in Cartesian
	 * coordinates. The field is returned as a Cartesian vector in kiloGauss.
	 * The coordinates are in the canonical CLAS system with the origin at the
	 * nominal target, x through the middle of sector 1 and z along the beam.
	 * 
	 * @param x
	 *            the x coordinate in cm
	 * @param y
	 *            the y coordinate in cm
	 * @param z
	 *            the z coordinate in cm
	 * @param result
	 *            a array holding the retrieved (interpolated) field in
	 *            kiloGauss. The 0,1 and 2 indices correspond to x, y, and z
	 *            components.
	 */
	@Override
	public final void field(float x, float y, float z, float result[]) {
		
		if (!contains(x, y, z)) {
			result[X] = 0f;
			result[Y] = 0f;
			result[Z] = 0f;
			return;
		}

		double rho = FastMath.sqrt(x * x + y * y);
		double phi = FastMath.atan2Deg(y, x);
		fieldCylindrical(phi, rho, z, result);
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
		
		//use three point derivative
		float del = 1f; //cm
		float del2 = 2*del;
		
		float baseVal = fieldMagnitude(x, y, z);
		float bv3 = -3*baseVal;
		
		float bx0 = fieldMagnitude(x+del, y, z);
		float bx1 = fieldMagnitude(x+del2, y, z);
		
//		System.err.println(" " + baseVal + "  " + bx0 + "  " + bx1);
		float by0 = fieldMagnitude(x, y+del, z);
		float by1 = fieldMagnitude(x, y+del2, z);
		float bz0 = fieldMagnitude(x, y, z+del);
		float bz1 = fieldMagnitude(x, y, z+del2);
		
		result[0] = (bv3 + 4*bx0 - bx1)/del2;
		result[1] = (bv3 + 4*by0 - by1)/del2;
		result[2] = (bv3 + 4*bz0 - bz1)/del2;
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
		double x = rho*FastMath.cos(phi);
    	double y = rho*FastMath.sin(phi);
    	gradient((float)x, (float)y, (float)z, result);
    }


	/**
	 * Get the creation date
	 * @return the creation date as a string
	 */
	public String getCreationDate() {
		
		long dlow = lowTime & 0x00000000ffffffffL;
		long time = ((long)highTime << 32) | (dlow & 0xffffffffL);
		
		if (time < 1) {
			return "unknown";
		}

        return MagneticFields.dateStringLong(time);
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
	public final float fieldMagnitudeCylindrical(double phi, double r, double z) {
		float result[] = new float[3];
		fieldCylindrical(phi, r, z, result);
		return FastMath.vectorLength(result);
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
	public final float fieldMagnitude(float x, float y, float z) {
		float result[] = new float[3];
		field(x, y, z, result);
		return FastMath.vectorLength(result);
	}

	/**
	 * Get the composite index to take me to the correct place in the buffer.
	 * 
	 * @param n1
	 *            the index in the q1 direction
	 * @param n2
	 *            the index in the q2 direction
	 * @param n3
	 *            the index in the q3 direction
	 * @return the composite index (buffer offset)
	 */
	public final int getCompositeIndex(int n1, int n2, int n3) {
		return n1 * (q2Coordinate.getNumPoints() * q3Coordinate.getNumPoints()) + n2 * q3Coordinate.getNumPoints() + n3;
	}

	/**
	 * Convert a composite index back to the coordinate indices
	 * 
	 * @param index
	 *            the composite index.
	 * @param qindices
	 *            the coordinate indices
	 */
	protected final void getCoordinateIndices(int index, int qindices[]) {
		int N3 = q3Coordinate.getNumPoints();
		int n3 = index % N3;
		index = (index - n3) / N3;

		int N2 = q2Coordinate.getNumPoints();
		int n2 = index % N2;
		int n1 = (index - n2) / N2;
		qindices[0] = n1;
		qindices[1] = n2;
		qindices[2] = n3;
	}

	/**
	 * Obtain the maximum field magnitude of any point in the map.
	 * 
	 * @return the maximum field magnitude in the units of the map.
	 */
	@Override
	public float getMaxFieldMagnitude() {
		return maxField;
	}

	/**
	 * Get the maximum field magnitude.
	 * 
	 * @return the index of the max field magnitude.
	 */
	public final int maxFieldMagnitude() {
		return maxFieldIndex;
	}

	// compute max field quantities
	protected void computeMaxField() {

		double maxf = -1.0e10;
		maxFieldIndex = -1;

		double sum = 0.0;

		for (int i = 0; i < numFieldPoints; i++) {
			double fm = FastMath.sqrt(squareMagnitude(i));
			sum += fm;

			if (fm > maxf) {
				maxf = fm;
				maxFieldIndex = i;
			}
		}
		vectorField(maxFieldIndex, maxVectorField);
		maxField = (float) maxf;
		avgField = (float) sum / numFieldPoints;
		getLocation(maxFieldIndex, maxFieldLocation);
	}

	/**
	 * Get the square of magnitude for a given index.
	 * 
	 * @param index
	 *            the index.
	 * @return the square of field magnitude at the given index.
	 */
	protected final double squareMagnitude(int index) {
		int i = 3 * index;
		float B1 = field.get(i);
		float B2 = field.get(i + 1);
		float B3 = field.get(i + 2);
		return B1 * B1 + B2 * B2 + B3 * B3;
	}

	/**
	 * Get the math lib being used.  Kept here for backwards compatibility.
	 * 
	 * @return the math lib being used
	 */
	public static FastMath.MathLib getMathLib() {
		return FastMath.getMathLib();
	}

	/**
	 * Set the math library to use. Kept here for backwards compatibility.
	 * 
	 * @param lib
	 *            the math library enum
	 */
	public static void setMathLib(FastMath.MathLib lib) {
		FastMath.setMathLib(lib);
	}

	/**
	 * Get the vector for a given index.
	 * 
	 * @param index
	 *            the index.
	 * @param vv
	 *            an array of three floats to hold the result.
	 */
	protected final void vectorField(int index, float vv[]) {
		int i = 3 * index;
		vv[0] = field.get(i);
		vv[1] = field.get(i + 1);
		vv[2] = field.get(i + 2);
	}

	/**
	 * Get some data as a string.
	 * 
	 * @return a string representation.
	 */
	@Override
	public final String toString() {
		StringBuffer sb = new StringBuffer(1024);
		
		//creation date
		sb.append("  Created: " + getCreationDate() + "\n");
		
		
		//if a torus, was it a full torus?
		if (this instanceof FullTorus) {
			sb.append("  Full torus with no assumed symmetry\n");
		}
		else if (this instanceof Torus) {
			sb.append("  Reduced torus with assumed symmetry\n");
		}
		
		sb.append("  " + q1Coordinate.toString());
		sb.append("\n");
		sb.append("  " + q2Coordinate.toString());
		sb.append("\n");
		sb.append("  " + q3Coordinate.toString());
		sb.append("\n");
		sb.append(String.format("  num field values: %d\n", numFieldPoints));

		sb.append("  grid cs: " + gridCoordinateSystem + "\n");
		sb.append("  field cs: " + fieldCoordinateSystem + "\n");
		sb.append("  length unit: " + lengthUnit + "\n");
		sb.append("  angular unit: " + angularUnit + "\n");
		sb.append("  field unit: " + fieldUnit + "\n");

		sb.append("  max field at index: " + maxFieldIndex + "\n");
		sb.append(String.format("  max field magnitude: %f %s\n", maxField, fieldUnit));
		sb.append("  max field vector:" + vectorToString(maxVectorField) + "\n");

		sb.append(String.format("  max field location: (%s, %s, %s) = (%6.2f, %6.2f, %6.2f)\n", q1Coordinate.getName(),
				q2Coordinate.getName(), q3Coordinate.getName(), maxFieldLocation[0], maxFieldLocation[1],
				maxFieldLocation[2]));

		sb.append(String.format("  avg field magnitude: %f %s\n", avgField, fieldUnit));
		
		return sb.toString();
	}

	/**
	 * Convert a array used as a vector to a readable string.
	 *
	 * @param v
	 *            the vector (float array) to represent.
	 * @return a string representation of the vector (array).
	 */
	protected String vectorToString(float v[]) {
		String s = String.format("(%8.5f, %8.5f, %8.5f) magnitude: %8.5f", v[0], v[1], v[2], FastMath.vectorLength(v));
		return s;
	}

	
	/**
	 * Get the base file name
	 * @return the base file name
	 */
	public String getBaseFileName() {
		return _baseFileName;
	}

	/**
	 * Read a magnetic field from a binary file. The file has the documented
	 * format.
	 *
	 * @param binaryFile
	 *            the binary file.
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	@Override
	public final void readBinaryMagneticField(File binaryFile) throws FileNotFoundException {

		_baseFileName = (binaryFile == null) ? "???" : binaryFile.getName();
		int index = _baseFileName .lastIndexOf(".");
		if (index > 1) {
			_baseFileName = _baseFileName.substring(0, index);
		}
				
		// N23 = -1;

		try {
			DataInputStream dos = new DataInputStream(new FileInputStream(binaryFile));

			boolean swap = false;
			int magicnum = dos.readInt(); // magic number

			// TODO handle swapping if necessary
			swap = (magicnum != MAGICNUMBER);
			if (swap) {
				System.err.println("byte swapping required but not yet implemented.");
				dos.close();
				return;
			}

			// grid cs
			gridCoordinateSystem = CoordinateSystem.fromInt(dos.readInt());

			// field cs
			fieldCoordinateSystem = CoordinateSystem.fromInt(dos.readInt());

			lengthUnit = LengthUnit.fromInt(dos.readInt()); // length units
			angularUnit = AngularUnit.fromInt(dos.readInt()); // angle units
			fieldUnit = FieldUnit.fromInt(dos.readInt()); // field units

			float q1Min = dos.readFloat();
			float q1Max = dos.readFloat();
			int nQ1 = dos.readInt();
			q1Coordinate = new GridCoordinate(_q1Name, q1Min, q1Max, nQ1);

			float q2Min = dos.readFloat();
			float q2Max = dos.readFloat();
			int nQ2 = dos.readInt();
			q2Coordinate = new GridCoordinate(_q2Name, q2Min, q2Max, nQ2);

			float q3Min = dos.readFloat();
			float q3Max = dos.readFloat();
			int nQ3 = dos.readInt();
			q3Coordinate = new GridCoordinate(_q3Name, q3Min, q3Max, nQ3);

			numFieldPoints = nQ1 * nQ2 * nQ3;

			// last five reserved
			highTime = dos.readInt();
			lowTime = dos.readInt();
			reserved3 = dos.readInt();
			reserved4 = dos.readInt();
			reserved5 = dos.readInt();

			// now get the field values
			int size = 3 * 4 * numFieldPoints;

			byte bytes[] = new byte[size];

			// read the bytes as a block
			dos.read(bytes);
			ByteBuffer byteBuffer = ByteBuffer.wrap(bytes).asReadOnlyBuffer();
			field = byteBuffer.asFloatBuffer().asReadOnlyBuffer();

			computeMaxField();

			System.out.println(toString());
			dos.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected int indexOfNearestNeighbor(double q1, double q2, double q3) {
		int n0 = q1Coordinate.getIndex(q1);
		if (n0 < 0) {
			return -1;
		}
		int n1 = q2Coordinate.getIndex(q2);
		if (n1 < 0) {
			return -1;
		}
		int n2 = q3Coordinate.getIndex(q3);
		if (n2 < 0) {
			return -1;
		}

		int index = getCompositeIndex(n0, n1, n2);

		return index;
	}


	/**
	 * Get the magnitude for a given index.
	 * 
	 * @param index
	 *            the index.
	 * @return the field magnitude at the given index.
	 */
	public final double fieldMagnitude(int index) {
		int i = 3 * index;
		float B1 = field.get(i);
		float B2 = field.get(i + 1);
		float B3 = field.get(i + 2);
		return FastMath.sqrt(B1 * B1 + B2 * B2 + B3 * B3);
	}

	/**
	 * Get the location at a given index
	 * 
	 * @param index
	 *            the composite index
	 * @param r
	 *            a vector that holds the three components of the location
	 */
	public final void getLocation(int index, float r[]) {
		int qindices[] = new int[3];
		getCoordinateIndices(index, qindices);
		r[0] = (float) q1Coordinate.getValue(qindices[0]);
		r[1] = (float) q2Coordinate.getValue(qindices[1]);
		r[2] = (float) q3Coordinate.getValue(qindices[2]);
	}

	/**
	 * Get B1 at a given index.
	 * 
	 * @param index
	 *            the index.
	 * @return the B1 at the given index.
	 */
	public final float getB1(int index) {
		int i = 3 * index;

		try {
			if (i >= field.limit()) {
				return 0f;
			}
			float val = field.get(i);
			return val;
		}
		catch (IndexOutOfBoundsException e) {
			System.err.println("error in mag field index1 = " + index);
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * Get B2 at a given index.
	 * 
	 * @param index
	 *            the index.
	 * @return the B2 at the given index.
	 */
	public final float getB2(int index) {
		int i = 1 + 3 * index;
		if (i >= field.limit()) {
			return 0f;
		}
		float val = field.get(i);
		return val;
	}

	/**
	 * Get B3 at a given index.
	 * 
	 * @param index
	 *            the index.
	 * @return the B3 at the given index.
	 */
	public final float getB3(int index) {
		int i = 2 + 3 * index;
		if (i >= field.limit()) {
			return 0f;
		}
		float val = field.get(i);
		return val;
	}

	/**
	 * Get the q1 coordinate.
	 *
	 * @return the q1 coordinate
	 */
	public final GridCoordinate getQ1Coordinate() {
		return q1Coordinate;
	}

	/**
	 * Get the q2 coordinate.
	 *
	 * @return the q2 coordinate
	 */
	public final GridCoordinate getQ2Coordinate() {
		return q2Coordinate;
	}

	/**
	 * Get the q3 coordinate.
	 *
	 * @return the q3 coordinate
	 */
	public final GridCoordinate getQ3Coordinate() {
		return q3Coordinate;
	}

	/**
	 * Set the names of the coordinate grid directions.
	 *
	 * @param q1name
	 *            name in the q1 direction (e.g., "x")
	 * @param q2name
	 *            name in the q2 direction (e.g., "y")
	 * @param q3name
	 *            name in the q3 direction (e.g., "z")
	 */
	public void setCoordinateNames(String q1name, String q2name, String q3name) {
		_q1Name = q1name;
		if (q1Coordinate != null) {
			q1Coordinate.setName(q1name);
		}
		_q2Name = q2name;
		if (q2Coordinate != null) {
			q2Coordinate.setName(q2name);
		}
		_q3Name = q3name;
		if (q3Coordinate != null) {
			q3Coordinate.setName(q3name);
		}
	}

	/**
	 * Get the name of the field
	 * 
	 * @return the name, e.e. "Torus"
	 */
	@Override
	public abstract String getName();

	/**
	 * Check whether we interpolate or use nearest neighbor
	 * 
	 * @return the interpolate flag
	 */
	public static final boolean isInterpolate() {
		return _interpolate;
	}

	/**
	 * Set whether we interpolate or use nearest neighbor
	 * 
	 * @param interpolate
	 *            the interpolate flag to set
	 */
	public static final void setInterpolate(boolean interpolate) {
		_interpolate = interpolate;
		System.out.println("Interpolating fields: " + _interpolate);
	}

	public int capacity() {
		return field.capacity();
	}

	/**
	 * Get the sector [1..6] from the phi value
	 * 
	 * @param phi
	 *            the value of phi in degrees
	 * @return the sector [1..6]
	 */
	public static int getSector(double phi) {
		// convert phi to [0..360]

		while (phi < 0) {
			phi += 360.0;
		}
		while (phi > 360.0) {
			phi -= 360.0;
		}

		if ((phi > 30.0) && (phi <= 90.0)) {
			return 2;
		}
		if ((phi > 90.0) && (phi <= 150.0)) {
			return 3;
		}
		if ((phi > 150.0) && (phi <= 210.0)) {
			return 4;
		}
		if ((phi > 210.0) && (phi <= 270.0)) {
			return 5;
		}
		if ((phi > 270.0) && (phi <= 330.0)) {
			return 6;
		}
		return 1;
	}

	/**
	 * Calculate using a 3D 
	
	 /**
     * Check whether the field boundaries include the point
     * @param x the x coordinate in the map units
     * @param y the y coordinate in the map units
     * @param z the z coordinate in the map units
     * @return <code>true</code> if the point is included in the boundary of the field
     */
	@Override
    public boolean contains(float x, float y, float z) {
		double rho = FastMath.sqrt(x * x + y * y);
		double phi = FastMath.atan2Deg(y, x);
        return containsCylindrical((float)phi, (float)rho, z);
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
	public abstract boolean containsCylindrical(float phi, float rho, float z);
	

}
