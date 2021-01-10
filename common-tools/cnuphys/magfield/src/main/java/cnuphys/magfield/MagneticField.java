package cnuphys.magfield;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * For magnetic fields stored in a specific format. This is low-level,
 * essentiall a container for the field values
 * 
 * @author David Heddle
 * @author Nicole Schumacher
 * @version 1.0
 */
public abstract class MagneticField implements IMagField {

	/** Magic number used to check if byteswapping is necessary. */
	public static final int MAGICNUMBER = 0xced;

	// used to reconfigure fields so solenoid and torus do not overlap
	private double _fakeZMax = Float.POSITIVE_INFINITY;

	/** misalignment tolerance */
	public static final double MISALIGNTOL = 1.0e-6; // cm

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

	/** shift in x direction in cm (misalignment) */
	protected double _shiftX; // cm

	/** shift in y direction in cm (misalignment) */
	protected double _shiftY; // cm

	/** shift in z direction in cm (misalignment) */
	protected double _shiftZ; // cm

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

	/** Overall scale factor */
	protected double _scaleFactor = 1.0;

	// determine whether we use interpolation or nearest neighbor
	protected static boolean _interpolate = true;

	private static final double TINY = 1.0e-5;

	/**
	 * Scale the field.
	 * 
	 * @param scale the scale factor
	 */
	public final void setScaleFactor(double scale) {
		System.out.println("CHANGING SCALE from " + _scaleFactor + " to " + scale + "  for " + getBaseFileName());
		if (Math.abs(scale - _scaleFactor) > TINY) {
			_scaleFactor = scale;
			MagneticFields.getInstance().changedScale(this);
		} else {
			System.out.println("Ignored inconsequential scale change for " + getBaseFileName());
		}
	}

	@Override
	public double getScaleFactor() {
		return _scaleFactor;
	}

	/**
	 * Change the shift in the x direction
	 * 
	 * @param shiftX the shift in cm
	 */
	public final void setShiftX(double shiftX) {
		_shiftX = shiftX;
	}

	/**
	 * Change the shift in the y direction
	 * 
	 * @param shiftY the shift in cm
	 */
	public final void setShiftY(double shiftY) {
		_shiftY = shiftY;
	}

	/**
	 * Change the shift in the z direction
	 * 
	 * @param shiftZ the shift in cm
	 */
	public final void setShiftZ(double shiftZ) {
		_shiftZ = shiftZ;
	}

	/**
	 * Get the shift in x.
	 * 
	 * @return the x shift in cm.
	 */
	public final double getShiftX() {
		return _shiftX;
	}

	/**
	 * Get the shift in y.
	 * 
	 * @return the y shift in cm.
	 */
	public final double getShiftY() {
		return _shiftY;
	}

	/**
	 * Get the shift in z.
	 * 
	 * @return the z shift in cm.
	 */
	public final double getShiftZ() {
		return _shiftZ;
	}

	/**
	 * Checks whether the field has been set to always return zero.
	 * 
	 * @return <code>true</code> if the field is set to return zero.
	 */
	@Override
	public boolean isZeroField() {
		return (Math.abs(_scaleFactor) < 1.0e-6);
	}

	/**
	 * For debugging you can set the field to always return 0.
	 * 
	 * @param zeroField if set to <code>true</code> the field will always return 0.
	 */
	public final void setZeroField(boolean zeroField) {
		setScaleFactor(0.0);
	}

	/**
	 * Get the creation date
	 * 
	 * @return the creation date as a string
	 */
	public String getCreationDate() {

		long dlow = lowTime & 0x00000000ffffffffL;
		long time = ((long) highTime << 32) | (dlow & 0xffffffffL);

		if (time < 1) {
			return "unknown";
		}

		return MagneticFields.dateStringLong(time);
	}

	/**
	 * Get the composite index to take me to the correct place in the buffer.
	 * 
	 * @param n1 the index in the q1 direction
	 * @param n2 the index in the q2 direction
	 * @param n3 the index in the q3 direction
	 * @return the composite index (buffer offset)
	 */
	public final int getCompositeIndex(int n1, int n2, int n3) {
		int n23 = q2Coordinate.getNumPoints() * q3Coordinate.getNumPoints();
		return n1 * n23 + n2 * q3Coordinate.getNumPoints() + n3;
	}

	/**
	 * Convert a composite index back to the coordinate indices
	 * 
	 * @param index    the composite index.
	 * @param qindices the coordinate indices
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
	 * @param index the index.
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
	 * Get the vector for a given index.
	 * 
	 * @param index the index.
	 * @param vv    an array of three floats to hold the result.
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
	public String toString() {
		StringBuffer sb = new StringBuffer(1024);

		// creation date
		sb.append("  Created: " + getCreationDate() + "\n");

		if (this instanceof Torus) {
			Torus torus = (Torus) this;
			boolean fullMap = torus.isFullMap();
			sb.append("  TORUS symmetric: " + !fullMap);
			sb.append("\n");
		} else if (this instanceof Solenoid) {
			sb.append("  SOLENOID ");
			sb.append("\n");
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
	 * @param v the vector (float array) to represent.
	 * @return a string representation of the vector (array).
	 */
	protected String vectorToString(float v[]) {
		String s = String.format("(%8.5f, %8.5f, %8.5f) magnitude: %8.5f", v[0], v[1], v[2], FastMath.vectorLength(v));
		return s;
	}

	/**
	 * Get the base file name of the field map
	 * 
	 * @return the base file name
	 */
	public String getBaseFileName() {
		return _baseFileName;
	}

	/**
	 * Read a magnetic field from a binary file. The file has the documented format.
	 *
	 * @param binaryFile the binary file.
	 * @throws FileNotFoundException the file not found exception
	 */
	public final void readBinaryMagneticField(File binaryFile) throws FileNotFoundException {

		_baseFileName = (binaryFile == null) ? "???" : binaryFile.getName();
		int index = _baseFileName.lastIndexOf(".");
		if (index > 1) {
			_baseFileName = _baseFileName.substring(0, index);
		}

		// N23 = -1;

		try {
			DataInputStream dos = new DataInputStream(new FileInputStream(binaryFile));

			boolean swap = false;
			int magicnum = dos.readInt(); // magic number
			
			System.out.println(String.format("Magic number: %04x", magicnum));

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
//			ByteBuffer byteBuffer = ByteBuffer.wrap(bytes).asReadOnlyBuffer();
//			field = byteBuffer.asFloatBuffer().asReadOnlyBuffer();
			ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
			field = byteBuffer.asFloatBuffer();

			computeMaxField();

			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the magnitude for a given index.
	 * 
	 * @param index the index.
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
	 * @param index the composite index
	 * @param r     a vector that holds the three components of the location
	 */
	public final void getLocation(int index, float r[]) {
		int qindices[] = new int[3];
		getCoordinateIndices(index, qindices);
		r[0] = (float) q1Coordinate.getValue(qindices[0]);
		r[1] = (float) q2Coordinate.getValue(qindices[1]);
		r[2] = (float) q3Coordinate.getValue(qindices[2]);
	}

	/**
	 * Get the B1 component at a given composite index.
	 * 
	 * @param index the composite index.
	 * @return the B1 at the given composite index.
	 */
	@Override
	public final float getB1(int index) {
		int i = 3 * index;

		try {
			if (i >= field.limit()) {
				return 0f;
			}
			float val = field.get(i);
			return val;
		} catch (IndexOutOfBoundsException e) {
			System.err.println("error in mag field index1 = " + index);
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * Get the B2 component at a given composite index.
	 * 
	 * @param index the composite index.
	 * @return the B2 at the given composite index.
	 */
	@Override
	public final float getB2(int index) {
		int i = 1 + 3 * index;
		if (i >= field.limit()) {
			return 0f;
		}
		float val = field.get(i);
		return val;
	}

	/**
	 * Get the B3 component at a given composite index.
	 * 
	 * @param index the composite index.
	 * @return the B3 at the given composite index.
	 */
	@Override
	public final float getB3(int index) {
		int i = 2 + 3 * index;
		if (i >= field.limit()) {
			return 0f;
		}
		float val = field.get(i);
		return val;
	}
	
	/**
	 * Get a component of the magnetic field
	 * @param componentIndex [1..3]
	 * @param compositeIndex
	 * @return the component
	 */
	public double getBComponent(int componentIndex, int compositeIndex) {
		switch (componentIndex) {
		case 1:
			return getB1(compositeIndex);

		case 2:
			return getB2(compositeIndex);
			
		case 3:
			return getB3(compositeIndex);
		
		default: 
			System.err.println("Asked for bad component of the magnetic field: [" + componentIndex + "] sould be 1, 2 or 3 only.");
			System.exit(-1);
		}
		
		return Double.NaN;
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
	 * @param q1name name in the q1 direction (e.g., "x")
	 * @param q2name name in the q2 direction (e.g., "y")
	 * @param q3name name in the q3 direction (e.g., "z")
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
	 * @param interpolate the interpolate flag to set
	 */
	public static final void setInterpolate(boolean interpolate) {
		_interpolate = interpolate;
		System.out.println("Interpolating fields: " + _interpolate);
	}

	/**
	 * @return the phiCoordinate
	 */
	public GridCoordinate getPhiCoordinate() {
		return q1Coordinate;
	}

	/**
	 * @return the rCoordinate
	 */
	public GridCoordinate getRCoordinate() {
		return q2Coordinate;
	}

	/**
	 * @return the zCoordinate
	 */
	public GridCoordinate getZCoordinate() {
		return q3Coordinate;
	}

	/**
	 * Is the map misaligned in the X direction?
	 * 
	 * @return <code>true</code> if map is misaligned
	 */
	public boolean isMisalignedX() {
		return (Math.abs(_shiftX) > MISALIGNTOL);
	}

	/**
	 * Is the map misaligned in the Y direction?
	 * 
	 * @return <code>true</code> if map is misaligned
	 */
	public boolean isMisalignedY() {
		return (Math.abs(_shiftY) > MISALIGNTOL);
	}

	/**
	 * Is the map misaligned in the Z direction?
	 * 
	 * @return <code>true</code> if map is misaligned
	 */
	public boolean isMisalignedZ() {
		return (Math.abs(_shiftZ) > MISALIGNTOL);
	}

	/**
	 * Is the map misaligned in any direction?
	 * 
	 * @return <code>true</code> if solenoid is misaligned
	 */
	public boolean isMisaligned() {
		return (isMisalignedX() || isMisalignedY() || isMisalignedZ());
	}

	/**
	 * Get the fake z lim used to remove overlap with torus
	 * 
	 * @return the fake z lim used to remove overlap with torus (cm)
	 */
	public double getFakeZMax() {
		return _fakeZMax;
	}

	/**
	 * Set the fake z lim used to remove overlap with torus
	 * 
	 * @param zlim the new value in cm
	 */
	public void setFakeZMax(double zlim) {
		_fakeZMax = zlim;
	}

	public double getZMax() {
		return q3Coordinate.getMax();
	}

	public double getZMin() {
		return q3Coordinate.getMin();
	}

	public double getRhoMax() {
		return q2Coordinate.getMax();
	}

	public double getRhoMin() {
		return q2Coordinate.getMin();
	}

	/**
	 * Checks this field active.
	 * 
	 * @return <code>true</code> if this field is active;
	 */
	public abstract boolean isActive();

	/**
	 * Checks whether the field boundary contain the given point.
	 * 
	 * @param x the x coordinate in cm
	 * @param y the y coordinate in cm
	 * @param z the z coordinate in cm
	 * @return <code>true</code> if the field contains the given point
	 */
	@Override
	public boolean contains(double x, double y, double z) {

		if (!isActive()) {
			return false;
		}

		// apply the shifts
		x -= _shiftX;
		y -= _shiftY;
		z -= _shiftZ;

		double rho = FastMath.hypot(x, y);
		return contains(rho, z);
	}

	/**
	 * Checks whether the field boundary contain the given point. Note the azimuthal
	 * coordinate is not provided because it is assumed that all fields are valid
	 * for all phi.
	 * 
	 * @param rho the cylindrical radius in cm
	 * @param z   the z coordinate in cm
	 * @return <code>true</code> if the field contains the given point
	 */
	private boolean contains(double rho, double z) {

		// assumes z has already been shifted backwards
		if (z >= _fakeZMax) {
			return false;
		}

		if (z < getZMin()) {
			return false;
		}
		if (z > getZMax()) {
			return false;
		}
		if ((rho < getRhoMin()) || (rho > getRhoMax())) {
			return false;
		}
		return true;
	}
	

}
