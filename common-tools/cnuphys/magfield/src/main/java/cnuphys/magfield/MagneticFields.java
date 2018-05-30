package cnuphys.magfield;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.EventListenerList;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Static support for magnetic fields
 * 
 * @author heddle
 * 
 */
public class MagneticFields {
	
	//0.866...
	private static final double ROOT3OVER2 = Math.sqrt(3)/2;
	private static double _cosPhi[] = {Double.NaN, 1, 0.5, -0.5, -1, -0.5, 0.5};
	private static double _sinPhi[] = {Double.NaN, 0, ROOT3OVER2, ROOT3OVER2, 0, -ROOT3OVER2, -ROOT3OVER2};


	/**
	 * A formatter to get the time in down to seconds (no day info).
	 */
	private static SimpleDateFormat formatterlong;

	static {
		TimeZone tz = TimeZone.getDefault();

		formatterlong = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		formatterlong.setTimeZone(tz);
	}

	// version of mag field package
	private static String VERSION = "1.095";

	// constants for different torus grids
	public static final int SYMMETRIC_TORUS = 0;
	public static final int TORUS_025 = 1;
	public static final int TORUS_050 = 2;
	public static final int TORUS_075 = 3;
	public static final int TORUS_100 = 4;
	public static final int TORUS_125 = 5;
	public static final int TORUS_150 = 6;
	public static final int TORUS_200 = 7;

	// initialize only once
	private static boolean _initialized = false;

	// solenoidal field
	private Solenoid _solenoid;

	// torus field (with 12-fold symmetry)
	private Torus _torus;

	// composite field
	private CompositeField _compositeField;

	// composite rotated
	private RotatedCompositeField _rotatedCompositeField;

	// optional full path to torus set by command line argument in ced
	private String _torusPath = sysPropOrEnvVar("TORUSMAP");

	// optional full path to solenoid set by command line argument in ced
	private String _solenoidPath = sysPropOrEnvVar("SOLENOIDMAP");

	// singleton
	private static MagneticFields instance;

	// which field is active
	private IField _activeField;

	// directories to look for maps
	private String[] _dataDirs;

	// types of fields
	public enum FieldType {
		TORUS, SOLENOID, COMPOSITE, COMPOSITEROTATED, ZEROFIELD
	}

	// List of magnetic field change listeners
	private EventListenerList _listenerList;

	// menu stuff

	private JMenuItem _loadItem; // load different torus

	private JRadioButtonMenuItem _torusItem;
	private JRadioButtonMenuItem _solenoidItem;
	private JRadioButtonMenuItem _bothItem;
	private JRadioButtonMenuItem _bothRotatedItem;
	private JRadioButtonMenuItem _zeroItem;

	private JRadioButtonMenuItem _interpolateItem;
	private JRadioButtonMenuItem _nearestNeighborItem;

	// for scaling
	private ScaleFieldPanel _scaleTorusPanel;
	private ScaleFieldPanel _scaleSolenoidPanel;

	// for shifting
	private MisplacedPanel _shiftSolenoidPanel;

	// private constructor for singleton
	private MagneticFields() {
	}

	/**
	 * Get the version of the magfield package
	 * 
	 * @return the version of the magfield package
	 */
	public String getVersion() {
		return VERSION;
	}

	/**
	 * public access to the singleton
	 * 
	 * @return the MagneticFields singleton
	 */
	public static MagneticFields getInstance() {
		if (instance == null) {
			instance = new MagneticFields();
		}
		return instance;
	}

	/**
	 * Load a new torus map from a file selector.
	 */

	/** Last selected data dir */
	private static String dataFilePath;

	/**
	 * Open a new torus map from the file selector
	 */
	public void openNewTorus() {

		FileNameExtensionFilter filter = new FileNameExtensionFilter("Torus Maps", "dat", "torus", "map");

		JFileChooser chooser = new JFileChooser(dataFilePath);
		chooser.setSelectedFile(null);
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			try {
				dataFilePath = file.getPath();
				if (file.exists()) {
					openNewTorus(file.getPath());
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Open a new torus map from a full path
	 * 
	 * @param path
	 *            the path to the torus map
	 * @throws FileNotFoundException
	 */
	public void openNewTorus(String path) throws FileNotFoundException {
		File file = new File(path);
		if (!file.exists()) {
			throw new FileNotFoundException("No torus at [" + path + "]");
		}

		boolean torusFull = isFieldmapFullField(path);

		Torus oldTorus = _torus;
		boolean activeFieldWasTorus = (_activeField == oldTorus);

		// load the torus
		if (torusFull) {
			_torus = readFullTorus(path);
		} else {
			_torus = readTorus(path);
		}

		if (activeFieldWasTorus) {
			_activeField = _torus;
		}

		if (_torus != null) {
			if (_compositeField != null) {
				if (oldTorus != null) {
					_compositeField.remove(oldTorus);
				}
				if (_torus != null) {
					_compositeField.add(_torus);
				}
			}

			if (_rotatedCompositeField != null) {
				if (oldTorus != null) {
					_rotatedCompositeField.remove(oldTorus);
				}
				if (_torus != null) {
					_rotatedCompositeField.add(_torus);
				}
			}
		}

		// System.out.println(_torus);
		notifyListeners();

	}

	/**
	 * Shift the solenoid along Z for misplacement. A negative shift moved the
	 * solenoid upstream.
	 * 
	 * @param shiftZ
	 *            the shift in cm
	 */
	public void setSolenoidShift(double shiftZ) {
		if (_solenoid != null) {
			_solenoid.setShiftZ(shiftZ);
		}
	}

	/**
	 * This programatically adjusts everything for new scale factors. This is
	 * used when data found in the file
	 * 
	 * @param torusScale
	 *            the torus scale factor
	 * @param solenoidScale
	 *            the solenoid scale facter
	 */
	public boolean changeFieldsAndMenus(double torusScale, double solenoidScale) {

		boolean solenoidScaleChange = false;
		boolean torusScaleChange = false;

		// see if fieldtype changed;
		FieldType currentType = getActiveFieldType();
		boolean wantTorus = Math.abs(torusScale) > 0.01;
		boolean wantSolenoid = Math.abs(solenoidScale) > 0.01;
		FieldType desiredFieldType = FieldType.ZEROFIELD;

		if (wantTorus && wantSolenoid) {
			desiredFieldType = FieldType.COMPOSITE;
		} else if (wantTorus && !wantSolenoid) {
			desiredFieldType = FieldType.TORUS;
		} else if (!wantTorus && wantSolenoid) {
			desiredFieldType = FieldType.SOLENOID;
		}
		boolean fieldChange = desiredFieldType != currentType;

		// torus scale change?
		if (_torus != null) {
			double currentScale = _torus.getScaleFactor();
			torusScaleChange = (Math.abs(currentScale - torusScale) > 0.001);
		}

		// solenoid scale change?
		if (_solenoid != null) {
			double currentScale = _solenoid.getScaleFactor();
			solenoidScaleChange = (Math.abs(currentScale - solenoidScale) > 0.001);
		}

		if (torusScaleChange) {
			// don't change scale if we aren't using torus
			if ((desiredFieldType == FieldType.TORUS) || (desiredFieldType == FieldType.COMPOSITE)) {
				_torus.setScaleFactor(torusScale);
				_scaleTorusPanel.fixText();
			}
		}
		if (solenoidScaleChange) {
			// don't change scale if we aren't using solenoid
			if ((desiredFieldType == FieldType.SOLENOID) || (desiredFieldType == FieldType.COMPOSITE)) {
				_solenoid.setScaleFactor(solenoidScale);
			}
			_scaleSolenoidPanel.fixText();
		}
		if (fieldChange) {
			setActiveField(desiredFieldType);
			_torusItem.setSelected(desiredFieldType == FieldType.TORUS);
			_solenoidItem.setSelected(desiredFieldType == FieldType.SOLENOID);
			_bothItem.setSelected(desiredFieldType == FieldType.COMPOSITE);
			if (_bothRotatedItem != null) {
				_bothRotatedItem.setSelected(desiredFieldType == FieldType.COMPOSITEROTATED);
			}
			_zeroItem.setSelected(desiredFieldType == FieldType.ZEROFIELD);
			// _uniformItem.setSelected(desiredFieldType == FieldType.UNIFORM);
		}

		boolean changed = solenoidScaleChange || torusScaleChange || fieldChange;
		return changed;
	}

	/**
	 * Get the field type of the active field
	 * 
	 * @return the field type of the active field
	 */
	public FieldType getActiveFieldType() {
		if (_activeField != null) {

			if (_activeField == _torus) {
				return FieldType.TORUS;
			} else if (_activeField == _solenoid) {
				return FieldType.SOLENOID;
			} else if (_activeField == _compositeField) {
				return FieldType.COMPOSITE;
			} else if (_activeField == _rotatedCompositeField) {
				return FieldType.COMPOSITEROTATED;
			}
			// else if (_activeField == _uniform) {
			// return FieldType.UNIFORM;
			// }

		}

		return FieldType.ZEROFIELD;
	}

	/**
	 * Is the active field solenoid only
	 * 
	 * @return <code>true</code> of the active field is solenoid only
	 */
	public boolean isSolenoidOnly() {
		return ((_activeField != null) && (_activeField == _solenoid));
	}

	/**
	 * Is the active field torus only
	 * 
	 * @return <code>true</code> of the active field is torus only
	 */
	public boolean isTorusOnly() {
		return ((_activeField != null) && (_activeField == _torus));
	}

	/**
	 * Is the active field solenoid and torus composite
	 * 
	 * @return <code>true</code> of the active field is solenoid and torus
	 *         composite
	 */
	public boolean isCompositeField() {
		return ((_activeField != null) && (_activeField == _compositeField));
	}

	// optional full path to solenoid set by command line argument in ced

	// get a property or environment variable
	// the property takes precedence
	private String sysPropOrEnvVar(String key) {
		String s = System.getProperty(key);
		if (s == null) {
			s = System.getenv(key);
		}
		return s;
	}

	/**
	 * Sets the active field
	 * 
	 * @param field
	 *            the new active field
	 */
	public void setActiveField(IField field) {
		_activeField = field;
	}

	/**
	 * Sets the active field
	 * 
	 * @param ftype
	 *            one of the enum values
	 */
	public void setActiveField(FieldType ftype) {
		// init();
		switch (ftype) {
		case TORUS:
			_activeField = _torus;
			break;
		case SOLENOID:
			_activeField = _solenoid;
			break;
		case COMPOSITE:
			_activeField = _compositeField;
			break;
		case COMPOSITEROTATED:
			_activeField = _rotatedCompositeField;
			break;
		case ZEROFIELD:
			_activeField = null;
			break;
		}

		notifyListeners();
	}

	/**
	 * Get a string description of the active field
	 * 
	 * @return a string description of the active field
	 */
	public final String getActiveFieldDescription() {
		return (_activeField == null) ? "None" : _activeField.getName();
	}

	/**
	 * Get the active field
	 * 
	 * @return the active field
	 */
	public IField getActiveField() {
		return _activeField;
	}

	/**
	 * Get a specific field map.
	 * 
	 * @param ftype
	 *            the field map to get
	 * @return the field map, which might be <code>null</code>.
	 */
	public IField getIField(FieldType ftype) {
		IField ifield = null;

		switch (ftype) {
		case TORUS:
			ifield = _torus;
			break;
		case SOLENOID:
			ifield = _solenoid;
			break;
		// case UNIFORM:
		// ifield = _uniform;
		// break;
		case COMPOSITE:
			ifield = _compositeField;
			break;
		case COMPOSITEROTATED:
			ifield = _rotatedCompositeField;
			break;
		case ZEROFIELD:
			ifield = null;
			break;
		}

		return ifield;
	}

	/**
	 * Get the scale factor got the field type.
	 * 
	 * @param ftype
	 *            the field type
	 * @return the scale factor got the field type. Composite fields return NaN.
	 */
	public double getScaleFactor(FieldType ftype) {

		double scale = Double.NaN;

		switch (ftype) {
		case TORUS:
			if (_torus != null)
				; {
			scale = _torus.getScaleFactor();
		}
			break;
		case SOLENOID:
			if (_solenoid != null)
				; {
			scale = _solenoid.getScaleFactor();
		}
			break;
		case COMPOSITE:
			break;
		case COMPOSITEROTATED:
			break;
		case ZEROFIELD:
			scale = 0;
			break;
		}

		return scale;
	}

	/**
	 * Get the shift Z given the field type.
	 * 
	 * @param ftype
	 *            the field type
	 * @return the shift in z (cm) for the field type. Composite fields return
	 *         NaN.
	 */
	public double getShiftZ(FieldType ftype) {

		double shiftz = Double.NaN;

		switch (ftype) {
		case TORUS:
			if (_torus != null)
				; {
			shiftz = _torus.getShiftZ();
		}
			break;
		case SOLENOID:
			if (_solenoid != null)
				; {
			shiftz = _solenoid.getShiftZ();
		}
			break;
		case COMPOSITE:
			break;
		case COMPOSITEROTATED:
			break;
		case ZEROFIELD:
			shiftz = 0;
			break;
		}

		return shiftz;
	}

	/**
	 * Obtain the magnetic field (from the active field) at a given location
	 * using expressed in Cartesian coordinates. The field is returned as a
	 * Cartesian vector in kiloGauss. The coordinates are in the canonical CLAS
	 * system with the origin at the nominal target, x through the middle of
	 * sector 1 and z along the beam.
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
	public void field(float x, float y, float z, float result[]) {

		// init(); //harmless if already inited

		if (_activeField == null) {
			result[0] = 0f;
			result[1] = 0f;
			result[2] = 0f;
		} else {
			_activeField.field(x, y, z, result);
		}
	}

	/**
	 * Get the maximum value of the active field in kG
	 * 
	 * @return the maximum value of the active field in kG
	 */
	public double maxFieldMagnitude() {
		// init(); //harmless if already inited
		double maxVal = 0;
		if (_activeField != null) {
			maxVal = Math.max(maxVal, _activeField.getMaxFieldMagnitude());
		}

		return maxVal;
	}

	/**
	 * Obtain the magnetic field (from a specific field) at a given location
	 * using expressed in Cartesian coordinates. The field is returned as a
	 * Cartesian vector in kiloGauss. The coordinates are in the canonical CLAS
	 * system with the origin at the nominal target, x through the middle of
	 * sector 1 and z along the beam.
	 * 
	 * @param ftype
	 *            the specific field to use.
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
	public void field(FieldType ftype, float x, float y, float z, float result[]) {

		// init(); //harmless if already inited
		IField ifield = null;

		switch (ftype) {
		case TORUS:
			ifield = _torus;
			break;
		case SOLENOID:
			ifield = _solenoid;
			break;
		case COMPOSITE:
			ifield = _compositeField;
			break;
		case ZEROFIELD:
			ifield = null;
			break;
		case COMPOSITEROTATED:
			ifield = _rotatedCompositeField;
			break;
		// case UNIFORM:
		// ifield = _uniform;
		// break;
		default:
			break;
		}

		if (ifield == null) {
			result[0] = 0f;
			result[1] = 0f;
			result[2] = 0f;
		} else {
			ifield.field(x, y, z, result);
		}

	}

	// try to get the solenoid
	private Solenoid getSolenoid(String baseName) {

		if (_solenoid != null) {
			return _solenoid;
		}

		Solenoid solenoid = null;

		// try env variable first
		if (_solenoidPath != null) {
			solenoid = readSolenoid(_solenoidPath);
			if (solenoid != null) {
				return solenoid;
			}
		}

		for (String ds : _dataDirs) {
			_solenoidPath = ds + "/" + baseName;
			solenoid = readSolenoid(_solenoidPath);
			if (solenoid != null) {
				return solenoid;
			}
		}

		return solenoid;
	}

	// read the solenoidal field
	private Solenoid readSolenoid(String fullPath) {
		File file = new File(fullPath);
		String cp;
		try {
			cp = file.getCanonicalPath();
		} catch (IOException e1) {
			cp = "???";
			e1.printStackTrace();
		}

		Solenoid solenoid = null;
		if (file.exists()) {
			try {
				solenoid = Solenoid.fromBinaryFile(file);
			} catch (Exception e) {
			}
		}

		_solenoidPath = fullPath;

		// System.out.println("\nAttempted to read solenoid from [" + cp + "]
		// success: " + (solenoid != null));
		return solenoid;
	}

	// read the torus field
	private Torus readTorus(String fullPath) {
		File file = new File(fullPath);
		String cp;
		try {
			cp = file.getCanonicalPath();
		} catch (IOException e1) {
			cp = "???";
			e1.printStackTrace();
		}

		Torus torus = null;
		if (file.exists()) {
			try {
				torus = Torus.fromBinaryFile(file);
			} catch (Exception e) {
			}
		}

		_torusPath = fullPath;
		// System.out.println("\nAttempted to read torus from [" + cp + "]
		// success: " + (torus != null));
		return torus;
	}

	// read a full torus file
	private FullTorus readFullTorus(String fullPath) {
		File file = new File(fullPath);
		String cp;
		try {
			cp = file.getCanonicalPath();
		} catch (IOException e1) {
			cp = "???";
			e1.printStackTrace();
		}

		FullTorus fullTorus = null;
		if (file.exists()) {
			try {
				fullTorus = FullTorus.fromBinaryFile(file);
			} catch (Exception e) {
			}
		}

		_torusPath = fullPath;
		// System.out.println("\nAttempted to read full torus from [" + cp + "]
		// success: " + (fullTorus != null));
		return fullTorus;
	}

	/**
	 * This method breaks a string into an array of tokens.
	 * 
	 * @param str
	 *            the string to decompose.
	 * @param delimiter
	 *            the delimiter
	 * @return an array of tokens
	 */

	public static String[] tokens(String str, String delimiter) {

		StringTokenizer t = new StringTokenizer(str, delimiter);
		int num = t.countTokens();
		String lines[] = new String[num];

		for (int i = 0; i < num; i++) {
			lines[i] = t.nextToken();
		}

		return lines;
	}

	/**
	 * Attempts to initialize the magnetic fields using the property or
	 * environment variables TORUSMAP and SOLENOIDMAP as full paths to the torus
	 * and solenoid
	 * 
	 * @throws MagneticFieldInitializationException
	 *             if neither environment variable is not found. Will proceed if
	 *             just one is found.
	 * @throws FileNotFoundException
	 *             if neither file is not found. Will proceed if just one is
	 *             found.
	 */
	public void initializeMagneticFieldsFromEnv() throws MagneticFieldInitializationException, FileNotFoundException {
		_torusPath = sysPropOrEnvVar("TORUSMAP");
		if (_torusPath == null) {
			System.err.println("No envrionment variable or property named TORUSMAP");
		}

		_solenoidPath = sysPropOrEnvVar("SOLENOIDMAP");
		if (_solenoidPath == null) {
			System.err.println("No envrionment variable or property named SOLENOIDMAP");
		}

		if ((_torusPath == null) && (_solenoidPath == null)) {
			throw new MagneticFieldInitializationException();
		}

		initializeMagneticFieldsFromPath(_torusPath, _solenoidPath);
	}

	/**
	 * Initialize the magnetic field package
	 * 
	 * @param dataDir
	 *            the common data directory containing the torus and solenoid
	 * @param torusName
	 *            the base name of the torus map
	 * @param solenoidName
	 *            the base name of the solenoid map
	 * @throws FileNotFoundException
	 *             if either full path is not null but the corresponding file
	 *             cannot be found
	 * @throws MagneticFieldInitializationException
	 *             if both full paths are null. Will proceed as long as one path
	 *             is not null.
	 */
	public void initializeMagneticFields(String dataDir, String torusName, String solenoidName)
			throws FileNotFoundException, MagneticFieldInitializationException {
		initializeMagneticFields(dataDir, torusName, dataDir, solenoidName);
	}

	/**
	 * @param torusDataDir
	 *            the data directory containing the torus
	 * @param torusName
	 *            the base name of the torus map
	 * @param solenoidDataDir
	 *            the data directory containing the solenoid
	 * @param solenoidName
	 *            the base name of the solenoid map
	 * @throws FileNotFoundException
	 *             if either path is not null but the corresponding file cannot
	 *             be found
	 * @throws MagneticFieldInitializationException
	 *             if both paths are null. Will proceed as long as one path is
	 *             not null.
	 */
	public void initializeMagneticFields(String torusDataDir, String torusName, String solenoidDataDir,
			String solenoidName) throws FileNotFoundException, MagneticFieldInitializationException {
		String torusPath = null;
		String solenoidPath = null;
		if (torusDataDir != null && torusName != null) {
			torusPath = (new File(torusDataDir, torusName)).getPath();
		}
		if (solenoidDataDir != null && solenoidName != null) {
			solenoidPath = (new File(solenoidDataDir, solenoidName)).getPath();
		}
		initializeMagneticFieldsFromPath(torusPath, solenoidPath);
	}

	/**
	 * Initialize the field from the two full paths. One of them can be null.
	 * 
	 * @param torusPath
	 *            the full path to the torus map. Can be null.
	 * @param solenoidPath
	 *            the full path to the solenoid map. Can be null.
	 * @throws MagneticFieldInitializationException
	 *             if both paths are null. Will proceed as long as one path is
	 *             not null.
	 * @throws FileNotFoundException
	 *             if either path is not null but the corresponding file cannot
	 *             be found
	 */
	public void initializeMagneticFieldsFromPath(String torusPath, String solenoidPath)
			throws MagneticFieldInitializationException, FileNotFoundException {

		if ((torusPath == null) && (solenoidPath == null)) {
			throw new MagneticFieldInitializationException();
		}

		File torusFile = null;
		File solenoidFile = null;

		if (torusPath != null) {
			torusFile = new File(torusPath);
			if (!torusFile.exists()) {
				torusFile = null;
				throw new FileNotFoundException("TORUS map not found at [" + torusPath + "]");
			}
		}

		if (solenoidPath != null) {
			solenoidFile = new File(solenoidPath);
			if (!solenoidFile.exists()) {
				solenoidFile = null;
				throw new FileNotFoundException("SOLENOID map not found at [" + solenoidPath + "]");
			}
		}

		System.out.println("===========================================");
		System.out.println("  Initializing Magnetic Fields");
		System.out.println("  Version " + VERSION);
		System.out.println("  Contact: david.heddle@cnu.edu");
		System.out.println();

		if (torusFile != null) {
			boolean torusFull = isFieldmapFullField(torusPath);
			System.out.println("  TORUS: [" + torusPath + "] is full map: " + torusFull);
			// load the torus
			if (torusFull) {
				_torus = readFullTorus(torusPath);
			} else {
				_torus = readTorus(torusPath);
			}
		}

		if (solenoidFile != null) {
			System.out.println("  SOLENOID: [" + solenoidPath + "]");
			// load the solenoid
			_solenoid = readSolenoid(solenoidPath);
		}

		System.out.println("  Torus loaded: " + (_torus != null));
		System.out.println("  Solenoid loaded: " + (_solenoid != null));
		System.out.println("===========================================");

		// _uniform = new Uniform(0, 0, 2);
		//
		finalInit();

		_torusPath = torusPath;
		_solenoidPath = solenoidPath;

	}

	/**
	 * Tries to load the magnetic fields from fieldmaps
	 */
	public void initializeMagneticFields() {

		// three dirs to try (they should have a magfield directory)
		String dirs[] = { getProperty("user.dir"), getProperty("user.home"),
				getProperty("user.dir") + "/../../../../../../etc/data" };

		boolean goodDir[] = new boolean[dirs.length];

		for (int i = 0; i < goodDir.length; i++) {
			File magdir = new File(dirs[i], "magfield");
			goodDir[i] = (magdir.exists() && magdir.isDirectory());

			try {
				System.out.println("MagDir [" + magdir.getCanonicalPath() + "]  Good: " + goodDir[i]);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		for (int i = 0; i < goodDir.length; i++) {
			if (goodDir[i]) {
				File magdir = new File(dirs[i], "magfield");
				if (initializeMagneticFields(magdir)) {
					System.out.println("Used fields found in [" + magdir.getPath() + "]");
					return;
				} else {
					System.out.println("WARNING Unable to use fields found in [" + magdir.getPath() + "]");
				}
			}
		}

		System.out.println("WARNING Magnetic Field Package did not initialize.");
	}

	private boolean initializeMagneticFields(File magdir) {

		// Symm_torus_LOWRES_2008.dat
		// Full_torus_r251_phi181_z251_18Apr2018.dat
		File torusFile = new File(magdir, "Symm_torus_LOWRES_2008.dat");
		if (torusFile.exists() && torusFile.canRead()) {
			File solenoidFile = new File(magdir, "Symm_solenoid_r601_phi1_z1201_2008.dat");
			if (solenoidFile.exists() && solenoidFile.canRead()) {
				try {
					MagneticFields.getInstance().initializeMagneticFieldsFromPath(torusFile.getPath(),
							solenoidFile.getPath());
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return false;
				} catch (MagneticFieldInitializationException e) {
					e.printStackTrace();
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Convenience routine for getting a system property.
	 * 
	 * @param keyName
	 *            the key name of the property
	 * @return the property, or <code>null</null>.
	 */
	private String getProperty(String keyName) {
		try {
			return System.getProperty(keyName);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Tries to load the magnetic fields from fieldmaps A unix like colon
	 * separated path
	 */
	public synchronized void initializeMagneticFields(String dataPath, TorusMap torusMap) {

		if (_initialized) {
			return;
		}

		_dataDirs = tokens(dataPath, ":");
		System.out.println("Number of possible data directories = " + _dataDirs.length);

		_initialized = true;

		System.out.println("===========================================");
		System.out.println("=  Initializing Magnetic Fields");
		System.out.println("=  Version " + VERSION);
		System.out.println("===========================================");

		initializeTorus(torusMap);

		// will actually try env variable first
		_solenoid = getSolenoid("clas12-fieldmap-solenoid.dat");

		System.out.println("Torus found: " + (_torus != null));
		System.out.println("Solenoid found: " + (_solenoid != null));

		// _uniform = new Uniform(0, 0, 2);
		//
		finalInit();
	}

	// load the requested torus map
	private void initializeTorus(TorusMap torusMap) {

		// default to symmetric
		if (torusMap == null) {
			torusMap = TorusMap.SYMMETRIC;
		}

		// hopefully there are data dirs to search
		if ((_dataDirs == null) || (_dataDirs.length < 1)) {
			_dataDirs = new String[2];
			_dataDirs[0] = "../../../data";
			_dataDirs[1] = "~/fieldMaps";
		}

		// lets see which maps we find

		for (TorusMap tmap : TorusMap.values()) {
			String fName = tmap.getFileName();
			for (String dataDir : _dataDirs) {
				if (tmap.foundField()) {
					break;
				}
				File file = new File(dataDir, fName);

				// try {
				// System.err.println("SEARCHING FOR TORUS IN [" +
				// file.getCanonicalPath() +
				// "]");
				// } catch (IOException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }

				if (file.exists()) {
					tmap.setFound(true);
					tmap.setDirName(dataDir);

					// System.out.println("** FOUND Torus map [" + fName + "] in
					// directory: [" + dataDir + "]");
				}

			}
		}

		// did we find the one we want?

		if (torusMap.foundField()) {
			if (torusMap.fullField()) {
				_torus = readFullTorus(new File(torusMap.getDirName(), torusMap.getFileName()).getPath());
			} else {
				_torus = readTorus(new File(torusMap.getDirName(), torusMap.getFileName()).getPath());
			}
		}

	}

	// make composite fields
	private void makeComposites() {
		_compositeField = new CompositeField();
		_rotatedCompositeField = new RotatedCompositeField();

		// print some features
		if (_torus != null) {
			// System.out.println("************ Torus: \n" + torus);
			_compositeField.add(_torus);
			_rotatedCompositeField.add(_torus);
		}
		if (_solenoid != null) {
			// System.out.println("************ Solenoid: \n" + solenoid);
			_compositeField.add(_solenoid);
			_rotatedCompositeField.add(_solenoid);
		}

		// set the default active field
		_activeField = null;
		if ((_torus != null) && (_solenoid != null)) {
			_activeField = _compositeField;
		} else if (_torus != null) {
			_activeField = _torus;
		} else if (_solenoid != null) {
			_activeField = _solenoid;
		}
	}

	// final initialziation
	private void finalInit() {
		makeComposites();
	}

	/**
	 * Get the magnetic field menu
	 * 
	 * @return the magnetic field menu
	 */
	public JMenu getMagneticFieldMenu() {
		return getMagneticFieldMenu(false, false);
	}

	/**
	 * Get the magnetic field menu
	 * 
	 * @return the magnetic field menu
	 */
	public JMenu getMagneticFieldMenu(boolean incRotatedField, boolean includeTestFields) {
		// // init(); //harmless if already inited
		JMenu menu = new JMenu("Field");

		// _torusMenu = TorusMenu.getInstance();
		// menu.add(_torusMenu);
		// menu.addSeparator();

		// for the mutually exclusive options
		ButtonGroup bg = new ButtonGroup();

		ActionListener al = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent ae) {
				handleMenuSelection(ae);
			}
		};

		_torusItem = createRadioMenuItem(_torus, "Torus", menu, bg, al);
		_solenoidItem = createRadioMenuItem(_solenoid, "Solenoid", menu, bg, al);
		_bothItem = createRadioMenuItem(_compositeField, "Composite", menu, bg, al);

		if (incRotatedField) {
			_bothRotatedItem = createRadioMenuItem(_rotatedCompositeField, "Rotated Composite", menu, bg, al);
		}

		// _uniformItem = createRadioMenuItem(null, "Uniform", menu, bg, al);
		_zeroItem = createRadioMenuItem(null, "No Field", menu, bg, al);

		// interpolation related
		menu.addSeparator();
		ButtonGroup bgi = new ButtonGroup();
		_interpolateItem = createRadioMenuItem("Interpolate", menu, bgi, MagneticField.isInterpolate(), al);
		_nearestNeighborItem = createRadioMenuItem("Nearest Neighbor", menu, bgi, !MagneticField.isInterpolate(), al);

		if (_torus != null) {
			menu.addSeparator();
			_scaleTorusPanel = new ScaleFieldPanel(FieldType.TORUS, "Torus", _torus.getScaleFactor());
			menu.add(_scaleTorusPanel);
		}

		if (_solenoid != null) {
			menu.addSeparator();
			_scaleSolenoidPanel = new ScaleFieldPanel(FieldType.SOLENOID, "Solenoid", _solenoid.getScaleFactor());
			_shiftSolenoidPanel = new MisplacedPanel(FieldType.SOLENOID, "Solenoid", _solenoid.getShiftZ());
			menu.add(_scaleSolenoidPanel);
			menu.add(_shiftSolenoidPanel);
		}

		_torusItem.setEnabled(_torus != null);
		_solenoidItem.setEnabled(_solenoid != null);
		_bothItem.setEnabled((_torus != null) && (_solenoid != null));

		if (_bothRotatedItem != null) {
			_bothRotatedItem.setEnabled((_torus != null) && (_solenoid != null));
		}
		// _uniformItem.setEnabled(_uniform != null);

		menu.addSeparator();
		_loadItem = new JMenuItem("Load a Different Torus...");
		_loadItem.addActionListener(al);
		menu.add(_loadItem);

		return menu;
	}

	// handle the radio menu selections from the magnetic field menu
	private void handleMenuSelection(ActionEvent ae) {
		Object source = ae.getSource();

		if (source == _torusItem) {
			_activeField = _torus;
		} else if (source == _solenoidItem) {
			_activeField = _solenoid;
		} else if (source == _bothItem) {
			_activeField = _compositeField;
		} else if ((_bothRotatedItem != null) && (source == _bothRotatedItem)) {
			_activeField = _rotatedCompositeField;
		} else if (source == _zeroItem) {
			_activeField = null;
		}
		// else if (source == _uniformItem) {
		// _activeField = _uniform;
		// }
		else if (source == _interpolateItem) {
			MagneticField.setInterpolate(true);
		} else if (source == _nearestNeighborItem) {
			MagneticField.setInterpolate(false);
		} else if (source == _loadItem) {
			openNewTorus();
		}

		System.err.println("Active Field: " + getActiveFieldDescription());
		notifyListeners();
	}

	// mag field changed scale
	protected void changedScale(MagneticField field) {
		if (field != null) {
			if (field == _torus) {
				if (_scaleTorusPanel != null) {
					_scaleTorusPanel._textField.setText(String.format("%7.3f", field.getScaleFactor()));
				}
				notifyListeners();
			} else if (field == _solenoid) {
				if (_scaleTorusPanel != null) {
					_scaleSolenoidPanel._textField.setText(String.format("%7.3f", field.getScaleFactor()));
				}
				notifyListeners();
			}
		}
	}

	// mag field changed shift for alignment (solenoid only)
	protected void changedShift(MagneticField field) {
		if (field != null) {
			if (field == _torus) {
			} else if (field == _solenoid) {
				if (_shiftSolenoidPanel != null) {
					_shiftSolenoidPanel._textField.setText(String.format("%7.3f", field.getShiftZ()));
				}
				notifyListeners();
			}
		}
	}

	/**
	 * Notify all listeners that a change has occurred in the magnetic fields
	 */
	protected void notifyListeners() {

		if (_listenerList == null) {
			return;
		}

		// Guaranteed to return a non-null array
		Object[] listeners = _listenerList.getListenerList();

		// This weird loop is the bullet proof way of notifying all listeners.
		// for (int i = listeners.length - 2; i >= 0; i -= 2) {
		// order is flipped so it goes in order as added
		for (int i = 0; i < listeners.length; i += 2) {
			if (listeners[i] == MagneticFieldChangeListener.class) {
				MagneticFieldChangeListener listener = (MagneticFieldChangeListener) listeners[i + 1];
				listener.magneticFieldChanged();
			}

		}
	}

	/**
	 * Add a magnetic field change listener
	 * 
	 * @param magChangeListener
	 *            the listener to add
	 */
	public void addMagneticFieldChangeListener(MagneticFieldChangeListener magChangeListener) {

		if (_listenerList == null) {
			_listenerList = new EventListenerList();
		}

		// avoid adding duplicates
		_listenerList.remove(MagneticFieldChangeListener.class, magChangeListener);
		_listenerList.add(MagneticFieldChangeListener.class, magChangeListener);
	}

	/**
	 * Remove a MagneticFieldChangeListener.
	 * 
	 * @param magChangeListener
	 *            the MagneticFieldChangeListener to remove.
	 */

	public void removeMagneticFieldChangeListener(MagneticFieldChangeListener magChangeListener) {

		if ((magChangeListener == null) || (_listenerList == null)) {
			return;
		}

		_listenerList.remove(MagneticFieldChangeListener.class, magChangeListener);
	}

	// convenience method for adding a radio button
	private JRadioButtonMenuItem createRadioMenuItem(IField field, String label, JMenu menu, ButtonGroup bg,
			ActionListener al) {

		String s = label;
		if (field != null) {
			s = field.getName();
		}

		boolean on = ((_activeField != null) && (_activeField == field));

		JRadioButtonMenuItem mi = new JRadioButtonMenuItem(s, on);
		mi.addActionListener(al);
		bg.add(mi);
		menu.add(mi);
		return mi;
	}

	// convenience method for adding a radio button
	private JRadioButtonMenuItem createRadioMenuItem(String label, JMenu menu, ButtonGroup bg, boolean on,
			ActionListener al) {

		JRadioButtonMenuItem mi = new JRadioButtonMenuItem(label, on);
		mi.addActionListener(al);
		bg.add(mi);
		menu.add(mi);
		return mi;
	}
	
	
	/**
	 * Removes the overlap between the solenoid and the torus. It does this by 
	 * Adding the solenoid field to the torus field cutting of the solenoid
	 * in the overlap region, then cutton off the solenoid at the min Z of the torus.
	 * This is an experimental method and irreversible. In particular rescaling the torus
	 * after doing this will cause the solenoid part of the overlap area to be scaled too. 
	 * Which is nonsense.
	 */
	public  void removeMapOverlap() {
		
		if ((_torus == null) || (_solenoid == null)) {
			return;
		}
		
		if (_torus.isSolenoidAdded()) {
			System.err.println("Cannot add solenoid into torus a second time.");
			return;
		}
		
		
		float solLimitZ = (float)(_solenoid.getZMax());
		float solLimitR = (float)(_solenoid.getRhoMax());
		
		int stopIndexR = _torus.getQ2Coordinate().getIndex(solLimitR);
		int stopIndexZ = _torus.getQ3Coordinate().getIndex(solLimitZ);
		
//		float tRval = (float) _torus.getQ2Coordinate().getValue(stopIndexR);
//		float tZval = (float) _torus.getQ3Coordinate().getValue(stopIndexZ);
		
//		System.err.println("tRVal = " + tRval);
//		System.err.println("tZVal = " + tZval);
		
		float[] result = new float[3];
		
		for (int nPhi = 0; nPhi < _torus.getQ1Coordinate().getNumPoints(); nPhi++) {
			float phi = (float) _torus.getQ1Coordinate().getValue(nPhi);
//			System.err.println("PHI = "  + phi);
			
			for (int nRho = 0; nRho <= stopIndexR; nRho++) {
				float rho = (float) _torus.getQ2Coordinate().getValue(nRho);
//				System.err.println("Rho = "  + rho);
				
				
				for (int nZ = 0; nZ <= stopIndexZ; nZ++) {
					float z = (float) _torus.getQ3Coordinate().getValue(nZ);
//					System.err.println("Z = "  + z);
					
					//get the solenoid field
					_solenoid.fieldCylindrical(phi, rho, z, result);
					
					//composite index 
			 		int index = _torus.getCompositeIndex(nPhi, nRho, nZ);
	 		     	_torus.addToField(index, result);
					
				}

			}
		}
		
		

		//now cutoff the solenoid
		float zlim = (float)(_torus.getZMin());
		System.err.println("FAKE Z LIM: " + zlim);
		_solenoid.setFakeZLim(zlim);
				
		
		notifyListeners();
	}


	/**
	 * Check whether we have an active torus field
	 * 
	 * @return <code>true</code> if we have a torus
	 */
	public boolean hasTorus() {
		if (_activeField != null) {
			if (_activeField instanceof Torus) {
				return true;
			} else if (_activeField instanceof CompositeField) {
				return ((CompositeField) _activeField).hasTorus();
			}
		}

		return false;
	}

	/**
	 * Check whether we have an active solenoid field
	 * 
	 * @return <code>true</code> if we have a solenoid
	 */
	public boolean hasSolenoid() {
		if (_activeField != null) {
			if (_activeField instanceof Solenoid) {
				return true;
			} else if (_activeField instanceof CompositeField) {
				return ((CompositeField) _activeField).hasSolenoid();
			}
		}

		return false;
	}

	/**
	 * Get the torus field
	 * 
	 * @return the torus field
	 */
	public Torus getTorus() {
		return _torus;
	}

	/**
	 * Get the solenoid field
	 * 
	 * @return the solenoid field
	 */
	public Solenoid getSolenoid() {
		return _solenoid;
	}

	/**
	 * Get the composite field
	 * 
	 * @return the composite field
	 */
	public CompositeField getCompositeField() {
		return _compositeField;
	}

	/**
	 * Get the rotated composite field
	 * 
	 * @return the rotated composite field
	 */
	public RotatedCompositeField getRotatedCompositeField() {
		return _rotatedCompositeField;
	}

	/**
	 * Get the full torus path
	 * 
	 * @return the full torus path
	 */
	public String getTorusPath() {

		if (_torus == null) {
			return null;
		}

		return _torusPath;
	}

	/**
	 * Get the full solenoid path
	 * 
	 * @return the full torus path
	 */
	public String getSolenoidPath() {

		if (_solenoid == null) {
			return null;
		}

		return _solenoidPath;
	}

	/**
	 * Get the torus file base name
	 * 
	 * @return the torus file base name
	 */
	public String getTorusBaseName() {
		if (getTorusPath() == null) {
			return null;
		}
		return (new File(getTorusPath())).getName();
	}
	
    
	/**
	 * Converts the sector 3D coordinates to clas (lab) 3D coordinates
	 * 
	 * @param sector the 1-based sector [1..6]
	 * @param lab will hold the lab 3D Cartesian coordinates (modified)
	 * @param x the sector x coordinate
	 * @param y the sector y coordinate
	 * @param z the sector z coordinate
	 */
	
	public static void sectorToLab(int sector, float lab[],
			float x, float y, float z) {

		if ((sector < 1) || (sector > 6)) {
			String wstr = "Bad sector: " + sector + " in RotatedCompositesectorToLab";
			System.err.println(wstr);
			return;
		}

		lab[2] = z; //z independent of sector
		
		if (sector == 1) {
			lab[0] = x;
			lab[1] = y;
		}
		else if (sector == 4) {
			lab[0] = -x;
			lab[1] = -y;
		}
		else { //sectors 2, 3, 5, 6
			double cosP = _cosPhi[sector];
			double sinP = _sinPhi[sector];
			
			lab[0] = (float)(cosP * x - sinP * y);
			lab[1] = (float)(sinP * x + cosP * y);
		}
	}
	
	
	/**
	 * Converts the clas (lab) 3D coordinates to sector 3D coordinates to
	 * 
	 * @param sector the 1-based sector [1..6]
	 * @param lab will hold the lab 3D Cartesian coordinates (modified)
	 * @param x the lab x coordinate
	 * @param y the lab y coordinate
	 * @param z the lab z coordinate
	 */
	
	public static void labToSector(int sector, float sect[],
			float x, float y, float z) {

		if ((sector < 1) || (sector > 6)) {
			String wstr = "Bad sector: " + sector + " in RotatedCompositesectorToLab";
			System.err.println(wstr);
			return;
		}

		sect[2] = z; //z independent of sector
		
		if (sector == 1) {
			sect[0] = x;
			sect[1] = y;
		}
		else if (sector == 4) {
			sect[0] = -x;
			sect[1] = -y;
		}
		else { //sectors 2, 3, 5, 6
			double cosP = _cosPhi[sector];
			double sinP = _sinPhi[sector];
			
			sect[0] = (float)(cosP * x + sinP * y);
			sect[1] = (float)(-sinP * x + cosP * y);
		}
	}
	
	/**
	 * Get the sector [1..6] from the lab x and y coordinates
	 * 
	 * @param labX the lab x
	 * @param labY the lab y
	 * @return the sector [1..6]
	 */
	public static int getSector(double labX, double labY) {
		double phi = Math.atan2(labY, labX);
		return getSector(Math.toDegrees(phi));
	}
	


	/**
	 * Get the sector [1..6] from the phi value
	 * 
	 * @param phi the value of phi in degrees
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

		if ((phi > 330) || (phi <= 30)) {
			return 1;
		}
		if (phi <= 90.0) {
			return 2;
		}
		if (phi <= 150.0) {
			return 3;
		}
		if (phi <= 210.0) {
			return 4;
		}
		if (phi <= 270.0) {
			return 5;
		}
		return 6;
	}



	/**
	 * Get the solenoid file base name
	 * 
	 * @return the solenoid file base name
	 */
	public String getSolenoidBaseName() {
		if (getSolenoidPath() == null) {
			return null;
		}
		return (new File(getSolenoidPath())).getName();
	}

	/**
	 * Get a description of the torus and solenoid base file names
	 * 
	 * @return the torus and solenoid base file names
	 */
	public String fileBaseNames() {
		String tbn = getTorusBaseName();
		String sbn = getSolenoidBaseName();

		String s = "";
		if ((tbn != null) && ((_activeField == _torus) || (_activeField == _compositeField))) {
			s = s + "Torus [" + tbn + "] ";
		}
		if ((sbn != null) && ((_activeField == _solenoid) || (_activeField == _compositeField))) {
			s = s + "Solenoid [" + sbn + "]";
		}
		return s;
	}

	/**
	 * Returns the time as a string.
	 * 
	 * @param longtime
	 *            the time in millis.
	 * @return a string representation of the current time, down to seconds.
	 */
	public static String dateStringLong(long longtime) {
		return formatterlong.format(longtime);
	}

	/**
	 * Tests whether this is a full field or a phi symmetric field
	 * 
	 * @return <code>true</code> if this is a full field
	 */
	public static boolean isFieldmapFullField(String torusPath) throws FileNotFoundException {
		File file = new File(torusPath);

		if (!file.exists()) {
			throw new FileNotFoundException("TORUS Map not found at [" + torusPath + "]");
		}

		try {
			DataInputStream dos = new DataInputStream(new FileInputStream(file));

			boolean swap = false;
			int magicnum = dos.readInt(); // magic number

			// TODO handle swapping if necessary
			swap = (magicnum != MagneticField.MAGICNUMBER);
			if (swap) {
				System.err.println("byte swapping required but not yet implemented.");
				dos.close();
				return false;
			}

			// read five ints related to cs
			int gridCS = dos.readInt();

			if (gridCS == 0) {
				System.out.println("Cylindrical Grid");
			} else if (gridCS == 1) {
				System.out.println("Rectangular Grid");
				dos.close();
				return true; // rect grids always "full"
			} else {
				System.err.println("Unknown Grid CS indicator: " + gridCS);
				System.exit(1);
			}

			dos.readInt();
			dos.readInt();
			dos.readInt();
			dos.readInt();

			// now read phi min and phi max in degrees

//			float phiMin = dos.readFloat();
			float phiMax = dos.readFloat();

			dos.close();
			return (phiMax > 300);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}
	
	/**
	 * Is this a probe or a composite probe?
	 * @param field the object to test
	 * @return <code>t
	 */
	public boolean isProbeOrCompositeProbe(IField field) {
		//already a probe?
		if ((field instanceof FieldProbe) || 
				(field instanceof CompositeProbe) || 
				(field instanceof RotatedCompositeProbe)) {
			return true;
		}

		return false;
	}

	/**
	 * For testing and also as an example
	 * 
	 * @param arg
	 *            command line arguments
	 */
	public static void main(String arg[]) {
		MagTests.runTests();
	}

}
