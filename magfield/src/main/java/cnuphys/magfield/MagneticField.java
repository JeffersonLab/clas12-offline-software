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
    protected float maxVectorField[] = new float[3];

    /** The location of the maximum field. */
    protected float maxFieldLocation[] = new float[3];

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

    /** reserved word */
    protected int reserved1;

    /** reserved word */
    protected int reserved2;

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
    private int N23 = -1;

    // used internally for index calculations
    private int N3;

    // is the field ready to use?
    protected boolean _fieldReady = false;

    // set to the zero field?
    protected boolean _zeroField;

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
    public void setScaleFactor(double scale) {
	_scaleFactor = scale;
	MagneticFields.changedScale(this);
    }

    /**
     * Get the factor that scales the field. Only scale factors between 0 and 1
     * are permitted. For negative scale factors, use in combination with an
     * inverted setting
     * 
     * @param scale
     *            the scale factor between 0 and 1
     */
    public double getScaleFactor() {
	return _scaleFactor;
    }

    /**
     * Checks whether the field has been set to always return zero-- useful at
     * times for debugging.
     * 
     * @return <code>true</code> if the field is set to return zero.
     */
    public boolean isZeroField() {
	return _zeroField;
    }

    /**
     * For debugging you can set the field to always return 0.
     * 
     * @param zeroField
     *            if set to <code>true</code> the field will always return 0.
     */
    public void setZeroField(boolean zeroField) {
	_zeroField = zeroField;
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
    public void field(float x, float y, float z, float result[]) {
	float rho = (float) Math.sqrt(x * x + y * y);
	float phi = (float) Math.toDegrees(Math.atan2(y, x));
	fieldCylindrical(phi, rho, z, result);
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
    protected int getCompositeIndex(int n1, int n2, int n3) {
	if (N23 < 1) { // first time
	    N23 = q2Coordinate.getNumPoints() * q3Coordinate.getNumPoints();
	    N3 = q3Coordinate.getNumPoints();
	}

	return n1 * N23 + n2 * N3 + n3;
    }

    /**
     * Convert a composite indix back to the coordinate indices
     * 
     * @param index
     *            the composite index.
     * @param qindices
     *            the coordinate indices
     */
    protected void getCoordinateIndices(int index, int qindices[]) {
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
    public int maxFieldMagnitude() {
	return maxFieldIndex;
    }

    // compute max field quantities
    protected void computeMaxField() {

	double maxf = -1.0e10;
	maxFieldIndex = -1;

	double sum = 0.0;

	for (int i = 0; i < numFieldPoints; i++) {
	    double fm = Math.sqrt(squareMagnitude(i));
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
    protected double squareMagnitude(int index) {
	int i = 3 * index;
	float B1 = field.get(i);
	float B2 = field.get(i + 1);
	float B3 = field.get(i + 2);
	return B1 * B1 + B2 * B2 + B3 * B3;
    }

    /**
     * Get the vector for a given index.
     * 
     * @param index
     *            the index.
     * @param vv
     *            an array of three floats to hold the result.
     */
    protected void vectorField(int index, float vv[]) {
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
    public String toString() {
	StringBuffer sb = new StringBuffer(1024);
	sb.append("\n");
	sb.append(q1Coordinate.toString());
	sb.append("\n");
	sb.append(q2Coordinate.toString());
	sb.append("\n");
	sb.append(q3Coordinate.toString());
	sb.append("\n");
	sb.append(String.format("Num Field Values: %d\n", numFieldPoints));

	sb.append("Grid CS: " + gridCoordinateSystem + "\n");
	sb.append("Field CS: " + fieldCoordinateSystem + "\n");
	sb.append("length Unit: " + lengthUnit + "\n");
	sb.append("angular Unit: " + angularUnit + "\n");
	sb.append("field Unit: " + fieldUnit + "\n");

	sb.append("max field at index: " + maxFieldIndex + "\n");
	sb.append(String.format("Max Field Magnitude: %f %s\n", maxField,
		fieldUnit));
	sb.append("Max Field Vector:" + vectorToString(maxVectorField) + "\n");

	sb.append(String.format(
		"Max Field Location: (%s, %s, %s) = (%8.5f, %8.5f, %8.5f)\n",
		q1Coordinate.getName(), q2Coordinate.getName(),
		q3Coordinate.getName(), maxFieldLocation[0],
		maxFieldLocation[1], maxFieldLocation[2]));

	sb.append(String.format("Avg Field Magnitude: %f %s\n", avgField,
		fieldUnit));

	float xyz[] = new float[3];
	// xyz[0] = 27.4412f;
	// xyz[1] = 15.7716f;
	// xyz[2] = 0.372474f;
	// -31.7007, 31.1478, 28.158
	xyz[0] = -31.7007f;
	xyz[1] = 31.1478f;
	xyz[2] = 28.158f;

	float vals[] = new float[3];
	field(xyz[0], xyz[1], xyz[2], vals);
	sb.append("test location (XYZ): " + vectorToString(xyz) + "\n");
	sb.append("test Field Vector (XYZ): " + vectorToString(vals) + "\n");

	// convert to cylindrical
	double phi = Math.atan2(xyz[1], xyz[0]);
	double rho = Math.hypot(xyz[0], xyz[1]);
	double cyl[] = { phi, rho, xyz[2] };
	String s = String.format("(%8.5f, %8.5f, %8.5f) magnitude: %8.5f",
		Math.toDegrees(cyl[0]), cyl[1], cyl[2],
		Math.hypot(cyl[1], cyl[2]));
	sb.append("test location (CYL): " + s + "\n");

	double bx = vals[0];
	double by = vals[1];
	double bz = vals[2];

	double sin = Math.sin(phi);
	double cos = Math.cos(phi);

	double bphi = -sin * bx + cos * by;
	double brho = cos * bx + sin * by;
	s = String.format("(%8.5f, %8.5f, %8.5f)", bphi, brho, bz);
	sb.append("test Field Vector (CYL): " + s + "\n");

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
	String s = String.format("(%8.5f, %8.5f, %8.5f) magnitude: %8.5f",
		v[0], v[1], v[2], vectorLength(v));
	return s;
    }

    /**
     * Vector length.
     *
     * @param v
     *            the v
     * @return the float
     */
    protected float vectorLength(float v[]) {
	float vx = v[0];
	float vy = v[1];
	float vz = v[2];
	return (float) Math.sqrt(vx * vx + vy * vy + vz * vz);
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
    public void readBinaryMagneticField(File binaryFile)
	    throws FileNotFoundException {

	N23 = -1;

	_fieldReady = false;

	try {
	    DataInputStream dos = new DataInputStream(new FileInputStream(
		    binaryFile));

	    boolean swap = false;
	    int magicnum = dos.readInt(); // magic number

	    // TODO handle swapping if necessary
	    swap = (magicnum != MAGICNUMBER);
	    if (swap) {
		System.err
			.println("byte swapping required but not yet implemented.");
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
	    reserved1 = dos.readInt();
	    reserved2 = dos.readInt();
	    reserved3 = dos.readInt();
	    reserved4 = dos.readInt();
	    reserved5 = dos.readInt();

	    // now get the field values
	    int size = 3 * 4 * numFieldPoints;

	    byte bytes[] = new byte[size];

	    // read the bytes as a block
	    dos.read(bytes);
	    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
	    field = byteBuffer.asFloatBuffer();
	    computeMaxField();

	    System.out.println(toString());
	    dos.close();
	    _fieldReady = true;
	    // cache some quantities
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /**
     * Interpolates a vector by trilinear interpolation.
     * 
     * @param q1
     *            the q1 coordinate
     * @param q2
     *            the q2 coordinate
     * @param q3
     *            the q3 coordinate
     * @param result
     *            will hold the result
     */
    protected void interpolateField(double q1, double q2, double q3,
	    float result[]) {

	result[0] = 0f;
	result[1] = 0f;
	result[2] = 0f;

	int n0 = q1Coordinate.getIndex(q1);
	if (n0 < 0) {
	    return;
	}
	int n1 = q2Coordinate.getIndex(q2);
	if (n1 < 0) {
	    return;
	}
	int n2 = q3Coordinate.getIndex(q3);
	if (n2 < 0) {
	    return;
	}

	double f0 = q1Coordinate.getFraction(q1);
	double f1 = q2Coordinate.getFraction(q2);
	double f2 = q3Coordinate.getFraction(q3);

	if (!_interpolate) {
	    f0 = (f0 < 0.5) ? 0 : 1;
	    f1 = (f1 < 0.5) ? 0 : 1;
	    f2 = (f2 < 0.5) ? 0 : 1;
	}

	double g0 = 1 - f0;
	double g1 = 1 - f1;
	double g2 = 1 - f2;

	// get the neighbor indices
	int i000 = getCompositeIndex(n0, n1, n2);
	int i001 = i000 + 1;

	int i010 = getCompositeIndex(n0, n1 + 1, n2);
	int i011 = i010 + 1;

	int i100 = getCompositeIndex(n0 + 1, n1, n2);
	int i101 = i100 + 1;

	int i110 = getCompositeIndex(n0 + 1, n1 + 1, n2);
	int i111 = i110 + 1;

	double b000 = getB1(i000);
	double b001 = getB1(i001);
	double b010 = getB1(i010);
	double b011 = getB1(i011);
	double b100 = getB1(i100);
	double b101 = getB1(i101);
	double b110 = getB1(i110);
	double b111 = getB1(i111);

	double x = b000 * g0 * g1 * g2 + b001 * g0 * g1 * f2 + b010 * g0 * f1
		* g2 + b011 * g0 * f1 * f2 + b100 * f0 * g1 * g2 + b101 * f0
		* g1 * f2 + b110 * f0 * f1 * g2 + b111 * f0 * f1 * f2;

	// now y
	b000 = getB2(i000);
	b001 = getB2(i001);
	b010 = getB2(i010);
	b011 = getB2(i011);
	b100 = getB2(i100);
	b101 = getB2(i101);
	b110 = getB2(i110);
	b111 = getB2(i111);

	double y = b000 * g0 * g1 * g2 + b001 * g0 * g1 * f2 + b010 * g0 * f1
		* g2 + b011 * g0 * f1 * f2 + b100 * f0 * g1 * g2 + b101 * f0
		* g1 * f2 + b110 * f0 * f1 * g2 + b111 * f0 * f1 * f2;

	// now z
	b000 = getB3(i000);
	b001 = getB3(i001);
	b010 = getB3(i010);
	b011 = getB3(i011);
	b100 = getB3(i100);
	b101 = getB3(i101);
	b110 = getB3(i110);
	b111 = getB3(i111);

	double z = b000 * g0 * g1 * g2 + b001 * g0 * g1 * f2 + b010 * g0 * f1
		* g2 + b011 * g0 * f1 * f2 + b100 * f0 * g1 * g2 + b101 * f0
		* g1 * f2 + b110 * f0 * f1 * g2 + b111 * f0 * f1 * f2;

	result[0] = (float) x;
	result[1] = (float) y;
	result[2] = (float) z;
    }

    /**
     * Interpolates the field magnitude by trilinear interpolation.
     *
     * @param q1
     *            the q1 coordinate
     * @param q2
     *            the q2 coordinate
     * @param q3
     *            the q3 coordinate return the interpolated value of the field
     *            magnitude
     * @return the float
     */
    protected float interpolateFieldMagnitude(double q1, double q2, double q3) {

	int n0 = q1Coordinate.getIndex(q1);
	if (n0 < 0) {
	    return 0f;
	}
	int n1 = q2Coordinate.getIndex(q2);
	if (n1 < 0) {
	    return 0f;
	}
	int n2 = q3Coordinate.getIndex(q3);
	if (n2 < 0) {
	    return 0f;
	}

	double f0 = q1Coordinate.getFraction(q1);
	double f1 = q2Coordinate.getFraction(q2);
	double f2 = q3Coordinate.getFraction(q3);

	double g0 = 1 - f0;
	double g1 = 1 - f1;
	double g2 = 1 - f2;

	// get the neighbor indices
	int i000 = getCompositeIndex(n0, n1, n2);
	int i001 = i000 + 1;

	int i010 = getCompositeIndex(n0, n1 + 1, n2);
	int i011 = i010 + 1;

	int i100 = getCompositeIndex(n0 + 1, n1, n2);
	int i101 = i100 + 1;

	int i110 = getCompositeIndex(n0 + 1, n1 + 1, n2);
	int i111 = i110 + 1;

	double b000 = fieldMagnitude(i000);
	double b001 = fieldMagnitude(i001);
	double b010 = fieldMagnitude(i010);
	double b011 = fieldMagnitude(i011);
	double b100 = fieldMagnitude(i100);
	double b101 = fieldMagnitude(i101);
	double b110 = fieldMagnitude(i110);
	double b111 = fieldMagnitude(i111);

	return (float) (b000 * g0 * g1 * g2 + b001 * g0 * g1 * f2 + b010 * g0
		* f1 * g2 + b011 * g0 * f1 * f2 + b100 * f0 * g1 * g2 + b101
		* f0 * g1 * f2 + b110 * f0 * f1 * g2 + b111 * f0 * f1 * f2);

    }

    /**
     * Get the magnitude for a given index.
     * 
     * @param index
     *            the index.
     * @return the field magnitude at the given index.
     */
    public float fieldMagnitude(int index) {
	int i = 3 * index;
	float B1 = field.get(i);
	float B2 = field.get(i + 1);
	float B3 = field.get(i + 2);
	return (float) Math.sqrt(B1 * B1 + B2 * B2 + B3 * B3);
    }

    /**
     * Get the location at a given index
     * 
     * @param index
     *            the composite index
     * @param r
     *            a vector that holds the three components of the location
     */
    public void getLocation(int index, float r[]) {
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
    protected float getB1(int index) {
	int i = 3 * index;
	return field.get(i);
    }

    /**
     * Get B2 at a given index.
     * 
     * @param index
     *            the index.
     * @return the B2 at the given index.
     */
    protected float getB2(int index) {
	int i = 3 * index;
	return field.get(i + 1);
    }

    /**
     * Get B3 at a given index.
     * 
     * @param index
     *            the index.
     * @return the B3 at the given index.
     */
    protected float getB3(int index) {
	int i = 3 * index;
	return field.get(i + 2);
    }

    /**
     * Get the q1 coordinate.
     *
     * @return the q1 coordinate
     */
    public GridCoordinate getQ1Coordinate() {
	return q1Coordinate;
    }

    /**
     * Get the q2 coordinate.
     *
     * @return the q2 coordinate
     */
    public GridCoordinate getQ2Coordinate() {
	return q2Coordinate;
    }

    /**
     * Get the q3 coordinate.
     *
     * @return the q3 coordinate
     */
    public GridCoordinate getQ3Coordinate() {
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
     * Check whether the solenoidal field is ready to be used.
     * 
     * @return <code>true</code> if the field is ready.
     */
    @Override
    public boolean isFieldLoaded() {
	return _fieldReady;
    }

    /**
     * Get the name of the field
     * 
     * @return the name, e.e. "Torus"
     */
    public abstract String getName();

    /**
     * Check whether we interpolate or use nearest neighbor
     * 
     * @return the interpolate flag
     */
    public static boolean isInterpolate() {
	return _interpolate;
    }

    /**
     * Set whether we interpolate or use nearest neighbor
     * 
     * @param interpolate
     *            the interpolate flag to set
     */
    public static void setInterpolate(boolean interpolate) {
	_interpolate = interpolate;
	System.out.println("Interpolating fields: " + _interpolate);
    }

}
