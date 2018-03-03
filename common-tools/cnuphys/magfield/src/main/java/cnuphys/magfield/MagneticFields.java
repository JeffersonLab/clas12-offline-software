package cnuphys.magfield;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.StringTokenizer;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.EventListenerList;

/**
 * Static support for magnetic fields
 * 
 * @author heddle
 * 
 */
public class MagneticFields {
	
	//vbersion of mag field package
	private static String VERSION = "1.06";
		
	//constants for different torus grids
    public static final int SYMMETRIC_TORUS = 0;
    public static final int TORUS_025       = 1;
    public static final int TORUS_050       = 2;
    public static final int TORUS_075       = 3;
    public static final int TORUS_100       = 4;
    public static final int TORUS_125       = 5;
    public static final int TORUS_150       = 6;
    public static final int TORUS_200       = 7;

	//initialize only once
	private boolean _initialized = false;

	// solenoidal field
	private Solenoid _solenoid;

	// torus field (with 12-fold symmetry)
	private Torus _torus;
		
	//which torus map is loaded
	private TorusMap _torusMap = null;

	// composite field
	private CompositeField _compositeField;
	
	// composite rotated
	private RotatedCompositeField _rotatedCompositeField;

	// optional full path to torus set by command line argument in ced
	//private String _torusPath = sysPropOrEnvVar("TORUSMAP");

	// optional full path to torus set by command line argument in ced
	private String _solenoidPath = sysPropOrEnvVar("SOLENOIDMAP");
	
	//singleton
	private static MagneticFields instance;

	// which field is active
	private IField _activeField;

	//directories to look for maps
	private String[] _dataDirs;
	
	// types of fields
	public enum FieldType {
		TORUS, SOLENOID, COMPOSITE, COMPOSITEROTATED, ZEROFIELD
	}

	// List of magnetic field change listeners
	private EventListenerList _listenerList;

	// menu stuff
	private JRadioButtonMenuItem _torusItem;
	private JRadioButtonMenuItem _solenoidItem;
	private JRadioButtonMenuItem _bothItem;
	private JRadioButtonMenuItem _zeroItem;
	
	private TorusMenu _torusMenu;


	private JRadioButtonMenuItem _interpolateItem;
	private JRadioButtonMenuItem _nearestNeighborItem;

	//for scaling
	private ScaleFieldPanel _scaleTorusPanel;
	private ScaleFieldPanel _scaleSolenoidPanel;
	
	//for shifting
	private MisplacedPanel _shiftSolenoidPanel;
	
	//private constructor for singleton
	private MagneticFields() {
	}
	
	/**
	 * Get the version of the magfield package
	 * @return the version of the magfield package
	 */
	public String getVersion() {
		return VERSION;
	}
	
	/**
	 * public access to the singleton
	 * @return the MagneticFields singleton
	 */
	public static MagneticFields getInstance() {
		if (instance == null) {
			instance = new MagneticFields();
		}
		return instance;
	}
	
	/**
	 * Shift the solenoid along Z for misplacement. A negative shift
	 * moved the solenoid upstream.
	 * @param shiftZ the shift in cm
	 */
	public void setSolenoidShift(double shiftZ) {
		if (_solenoid != null) {
			_solenoid.setShiftZ(shiftZ);
		}
	}

	/**
	 * This programatically adjusts everything for new scale factors.
	 * This is used when data found in the file
	 * @param torusScale the torus scale factor
	 * @param solenoidScale the solenoid scale facter
	 */
	public boolean changeFieldsAndMenus(
			double torusScale,
			double solenoidScale) {
	
		boolean solenoidScaleChange = false;
		boolean torusScaleChange = false;
		
		//see if fieldtype changed;
		FieldType currentType = getActiveFieldType();
		boolean wantTorus = Math.abs(torusScale) > 0.01;
		boolean wantSolenoid = Math.abs(solenoidScale) > 0.01;
		FieldType desiredFieldType = FieldType.ZEROFIELD;
		
		if (wantTorus && wantSolenoid) {
			desiredFieldType = FieldType.COMPOSITE;
		}
		else if (wantTorus && !wantSolenoid) {
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
			//don't change scale if we aren't using torus
			if ((desiredFieldType == FieldType.TORUS) || (desiredFieldType == FieldType.COMPOSITE)) {
				_torus.setScaleFactor(torusScale);
				_scaleTorusPanel.fixText();
			}
		}
		if (solenoidScaleChange) {
			//don't change scale if we aren't using solenoid
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
			_zeroItem.setSelected(desiredFieldType == FieldType.ZEROFIELD);
		}
		
		boolean changed = solenoidScaleChange || torusScaleChange || fieldChange;
		return changed;
	}
	
	/**
	 * Get the field type of the active field
	 * @return the field type of the active field
	 */
	public FieldType getActiveFieldType() {
		if (_activeField != null) {
			
			if (_activeField == _torus) {
				return FieldType.TORUS;
			}
			else if (_activeField == _solenoid) {
				return FieldType.SOLENOID;
			}
			else if (_activeField == _compositeField) {
				return FieldType.COMPOSITE;
			}
			else if (_activeField == _rotatedCompositeField) {
				return FieldType.COMPOSITEROTATED;
			}

		}
		
		return FieldType.ZEROFIELD;
	}
	
	/**
	 * Is the active field solenoid only
	 * @return <code>true</code> of the active field is solenoid only
	 */
	public boolean isSolenoidOnly() {
		return ((_activeField != null) && (_activeField == _solenoid));
	}
	
	/**
	 * Is the active field torus only
	 * @return <code>true</code> of the active field is torus only
	 */
	public boolean isTorusOnly() {
		return ((_activeField != null) && (_activeField == _torus));
	}

	/**
	 * Is the active field solenoid and torus composite
	 * @return <code>true</code> of the active field is solenoid and torus composite
	 */
	public boolean isCompositeField() {
		return ((_activeField != null) && (_activeField == _compositeField));
	}

	
	// optional full path to solenoid set by command line argument in ced


	//get a property or environment variable
	private String sysPropOrEnvVar(String key) {
		String s = System.getProperty(key);
		if (s == null) {
			s = System.getenv(key);
		}

		System.out.println("***  For key: \"" + key + "\" found value: " + s);

		return s;
	}

	
	/**
	 * Sets the active field
	 * 
	 * @param field the new active field
	 */
	public void setActiveField(IField field) {
		_activeField = field;
	}

	/**
	 * Sets the active field
	 * 
	 * @param ftype one of the enum values
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
		// init(); //harmless if already inited
		return _activeField;
	}

	/**
	 * Get a specific field map.
	 * 
	 * @param ftype the field map to get
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
	 * @param ftype the field type
	 * @return the scale factor got the field type. Composite fields return NaN.
	 */
	public double getScaleFactor(FieldType ftype) {
		
		double scale = Double.NaN;

		switch (ftype) {
		case TORUS:
			if (_torus != null); {
				scale = _torus.getScaleFactor();
			}
			break;
		case SOLENOID:
			if (_solenoid != null); {
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
	 * @param ftype the field type
	 * @return the shift in z (cm) for the field type. Composite fields return NaN.
	 */
	public double getShiftZ(FieldType ftype) {
		
		double shiftz = Double.NaN;

		switch (ftype) {
		case TORUS:
			if (_torus != null); {
				shiftz = _torus.getShiftZ();
			}
			break;
		case SOLENOID:
			if (_solenoid != null); {
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
	 * @param x the x coordinate in cm
	 * @param y the y coordinate in cm
	 * @param z the z coordinate in cm
	 * @param result a array holding the retrieved (interpolated) field in
	 *            kiloGauss. The 0,1 and 2 indices correspond to x, y, and z
	 *            components.
	 */
	public void field(float x, float y, float z, float result[]) {

		// init(); //harmless if already inited

		if (_activeField == null) {
			result[0] = 0f;
			result[1] = 0f;
			result[2] = 0f;
		}
		else {
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
	 * @param ftype the specific field to use.
	 * @param x the x coordinate in cm
	 * @param y the y coordinate in cm
	 * @param z the z coordinate in cm
	 * @param result a array holding the retrieved (interpolated) field in
	 *            kiloGauss. The 0,1 and 2 indices correspond to x, y, and z
	 *            components.
	 */
	public void field(FieldType ftype, float x, float y, float z,
			float result[]) {

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
		default:
			break;
		}

		if (ifield == null) {
			result[0] = 0f;
			result[1] = 0f;
			result[2] = 0f;
		}
		else {
			ifield.field(x, y, z, result);
		}

	}
	
	//try to get the solenoid
	private Solenoid getSolenoid(String baseName) {
		
		if (_solenoid != null) {
			return _solenoid;
		}
		
		Solenoid solenoid = null;

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

		System.out.println("\nAttempted to read solenoid from [" + cp + "]  success: " + (solenoid != null));
		return solenoid;
	}

	
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

		System.out.println("\nAttempted to read torus from [" + cp + "]  success: " + (torus != null));
		return torus;
	}
	
	//read a full torus file
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

		System.out.println("\nAttempted to read full torus from [" + cp + "]  success: " + (fullTorus != null));
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
	 * Tries to load the magnetic fields from fieldmaps
	 */
	public void initializeMagneticFields() {
		String homeDir = getProperty("user.home");
		initializeMagneticFields(".:" + homeDir + "/fieldMaps:../../../data:../../data:../data:data:cedbuild/data", TorusMap.SYMMETRIC);
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
	 * Tries to load the magnetic fields from fieldmaps
	 * A unix like colon separated path
	 */
	public void initializeMagneticFields(String dataPath, TorusMap torusMap) {
		
		if (_initialized) {
			return;
		}
		
		_dataDirs = tokens(dataPath, ":");
		System.out.println("Number of possible data directories = " + _dataDirs.length);
		
		
		
		_initialized = true;
		
		System.out.println("===========================================");
		System.out.println("======  Initializing Magnetic Fields  =====");
		System.out.println("===========================================");
		
		initializeTorus(torusMap);
		
		_solenoid = getSolenoid("clas12-fieldmap-solenoid.dat");
				
		System.out.println("Torus found: " + (_torus != null));
		System.out.println("Solenoid found: " + (_solenoid != null));

		finalInit();
	}
	
	/**
	 * Get the active torus map (doesn't mean active field has a torus).
	 * It just returns the type of torus loaded (if any)
	 * @return the type of torus loaded
	 */
	public TorusMap getTorusMap() {
		return _torusMap;
	}
	
	/**
	 * Set the torus to a new map
	 * @param tmap the new map
	 */
	public void setTorus(TorusMap tmap) {
		
		FieldType ftype = getActiveFieldType();
		
		if (tmap == null) {
			return;
		}
		
		if (tmap == _torusMap) {
			return;
		}
		
		if (!tmap.foundField()) {
			return;
		}
		
		double scaleFact = -1;
		double shiftz = 0.0;
		
		if (_torus != null) {
			scaleFact = _torus.getScaleFactor();
			shiftz = _torus.getShiftZ();
		}
		
		_torusMap = tmap;
	
		if (_torusMap.fullField()) {
			_torus = readFullTorus(new File(tmap.getDirName(), tmap.getFileName()).getPath());
		} else {
			_torus = readTorus(new File(tmap.getDirName(), tmap.getFileName()).getPath());
			System.gc();
		}
		System.out.println("** USING Torus map [" + tmap.getName() + "]");
		
		if (_torus != null) {
			_torus.setScaleFactor(scaleFact);
			_torus.setShiftZ(shiftz);
		}
		
//		float B1A = _torus.getB1(7506285);
//		float B1B = _torus.getB1(7569286);
//		System.err.println("B1's   " + B1A + "     " + B1B);
		
		
		makeComposites();
		setActiveField(ftype);
		notifyListeners();
	}
	
	//load the requested torus map
	private void initializeTorus(TorusMap torusMap) {
		_torusMap = null;
		
		//default to symmetric
		if (torusMap == null) {
			torusMap = TorusMap.SYMMETRIC;
		}
		
		//hopefully there are data dirs to search
		if ((_dataDirs == null) || (_dataDirs.length < 1)) {
			_dataDirs = new String[2];
			_dataDirs[0] = "../../../data";
			_dataDirs[1] = "~/fieldMaps";
		}
		
		//lets see which maps we find
		
		for (TorusMap tmap : TorusMap.values()) {
			String fName = tmap.getFileName();
			for (String dataDir : _dataDirs) {
				if (tmap.foundField()) {
					break;
				}
				File file = new File(dataDir, fName);
				if (file.exists()) {
					tmap.setFound(true);
					tmap.setDirName(dataDir);
					
					System.out.println("** FOUND Torus map [" + fName + "] in directory: [" + dataDir + "]");
				}

			}
		}

		// did we find the one we want?

		if (torusMap.foundField()) {
			_torusMap = torusMap;
			if (_torusMap.fullField()) {
				_torus = readFullTorus(new File(_torusMap.getDirName(), _torusMap.getFileName()).getPath());
			} else {
				_torus = readTorus(new File(_torusMap.getDirName(), _torusMap.getFileName()).getPath());
			}
			TorusMenu.getInstance().fixTitle(_torusMap);
			System.out.println("** USING Torus map [" + _torusMap.getName() + "]");
		} else {
			TorusMenu.getInstance().fixTitle(null);
			System.err.println("WARNING: Did not find a map for torus field: [" + torusMap.getName() + "]");
		}
		
	}
	
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
		}
		else if (_torus != null) {
			_activeField = _torus;
		}
		else if (_solenoid != null) {
			_activeField = _solenoid;
		}
	}
	
	//final initialziation
	private void finalInit() {
		makeComposites();

		System.out.println("\n***********************************");
		System.out.println("* Magfield package version: " + VERSION );
		System.out.println("***********************************");
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
	public JMenu getMagneticFieldMenu(boolean incRotatedField,
			boolean includeTestFields) {
		// // init(); //harmless if already inited
		JMenu menu = new JMenu("Field");
		
		_torusMenu = TorusMenu.getInstance();
		menu.add(_torusMenu);
		menu.addSeparator();

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
//		_bothRotatedItem = createRadioMenuItem(_rotatedCompositeField, "Rotated Composite", menu, bg, al);

		_zeroItem = createRadioMenuItem(null, "No Field", menu, bg, al);

		// interpolation related
		menu.addSeparator();
		ButtonGroup bgi = new ButtonGroup();
		_interpolateItem = createRadioMenuItem("Interpolate", menu, bgi,
				MagneticField.isInterpolate(), al);
		_nearestNeighborItem = createRadioMenuItem("Nearest Neighbor", menu,
				bgi, !MagneticField.isInterpolate(), al);

		if (_torus != null) {
			menu.addSeparator();
			_scaleTorusPanel = new ScaleFieldPanel(FieldType.TORUS, "Torus",
					_torus.getScaleFactor());
			menu.add(_scaleTorusPanel);
		}

		if (_solenoid != null) {
			menu.addSeparator();
			_scaleSolenoidPanel = new ScaleFieldPanel(FieldType.SOLENOID,
					"Solenoid", _solenoid.getScaleFactor());
			_shiftSolenoidPanel = new MisplacedPanel(FieldType.SOLENOID,
					"Solenoid", _solenoid.getShiftZ());
			menu.add(_scaleSolenoidPanel);
			menu.add(_shiftSolenoidPanel);
		}


		_torusItem.setEnabled(_torus != null);
		_solenoidItem.setEnabled(_solenoid != null);
		_bothItem.setEnabled((_torus != null) && (_solenoid != null));

		return menu;
	}
	

	// handle the radio menu selections from the magnetic field menu
	private void handleMenuSelection(ActionEvent ae) {
		Object source = ae.getSource();

		if (source == _torusItem) {
			_activeField = _torus;
		}
		else if (source == _solenoidItem) {
			_activeField = _solenoid;
		}
		else if (source == _bothItem) {
			_activeField = _compositeField;
		}
		else if (source == _zeroItem) {
			_activeField = null;
		}
		else if (source == _interpolateItem) {
			MagneticField.setInterpolate(true);
		}
		else if (source == _nearestNeighborItem) {
			MagneticField.setInterpolate(false);
		}

		System.err.println("Active Field: " + getActiveFieldDescription());
		notifyListeners();
	}

	//mag field changed scale
	protected void changedScale(MagneticField field) {
		if (field != null) {
			if (field == _torus) {
				if (_scaleTorusPanel != null) {
					_scaleTorusPanel._textField.setText(String.format("%7.3f", field.getScaleFactor()));
				}
				notifyListeners();
			}
			else if (field == _solenoid) {
				if (_scaleTorusPanel != null) {
					_scaleSolenoidPanel._textField.setText(String.format("%7.3f", field.getScaleFactor()));
				}
				notifyListeners();
			}
		}
	}
	
	
	//mag field changed shift for alignment
	protected void changedShift(MagneticField field) {
		if (field != null) {
			if (field == _torus) {
			}
			else if (field == _solenoid) {
				if (_shiftSolenoidPanel != null) {
					_shiftSolenoidPanel._textField.setText(String.format("%7.3f", field.getShiftZ()));
				}
				notifyListeners();
			}
		}
	}
	

	// notify listeners of a change in the magnetic field
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
				MagneticFieldChangeListener listener = (MagneticFieldChangeListener) listeners[i
						+ 1];
				listener.magneticFieldChanged();
			}

		}
	}

	/**
	 * Add a magnetic field change listener
	 * 
	 * @param magChangeListener the listener to add
	 */
	public void addMagneticFieldChangeListener(
			MagneticFieldChangeListener magChangeListener) {

		if (_listenerList == null) {
			_listenerList = new EventListenerList();
		}

		// avoid adding duplicates
		_listenerList.remove(MagneticFieldChangeListener.class,
				magChangeListener);
		_listenerList.add(MagneticFieldChangeListener.class,
				magChangeListener);
	}

	/**
	 * Remove a MagneticFieldChangeListener.
	 * 
	 * @param magChangeListener the MagneticFieldChangeListener to
	 *            remove.
	 */

	public void removeMagneticFieldChangeListener(
			MagneticFieldChangeListener magChangeListener) {

		if ((magChangeListener == null) || (_listenerList == null)) {
			return;
		}

		_listenerList.remove(MagneticFieldChangeListener.class,
				magChangeListener);
	}

	// convenience method for adding a radio button
	private JRadioButtonMenuItem createRadioMenuItem(IField field, String label,
			JMenu menu, ButtonGroup bg, ActionListener al) {
		
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
	private JRadioButtonMenuItem createRadioMenuItem(String label,
			JMenu menu, ButtonGroup bg, boolean on, ActionListener al) {
		
		
		JRadioButtonMenuItem mi = new JRadioButtonMenuItem(label, on);
		mi.addActionListener(al);
		bg.add(mi);
		menu.add(mi);
		return mi;
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
			}
			else if (_activeField instanceof CompositeField) {
				return ((CompositeField)_activeField).hasTorus();
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
			}
			else if (_activeField instanceof CompositeField) {
				return ((CompositeField)_activeField).hasSolenoid();
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
	 * @return the composite field
	 */
	public CompositeField getCompositeField() {
		return _compositeField;
	}
	
	/**
	 * Get the rotated composite field
	 * @return the rotated composite field
	 */
	public RotatedCompositeField getRotatedCompositeField() {
		return _rotatedCompositeField;
	}
	

	private static void threadTest(MagneticFields mf) {
		int cores = Runtime.getRuntime().availableProcessors();
		System.err.println("Number of available cores: " + cores);
		
		System.err.println("Torus capacity: " + mf._torus.capacity());

		System.err.println("Thread count: " + ManagementFactory.getThreadMXBean().getThreadCount());
	}
	/**
	 * For testing and also as an example
	 * 
	 * @param arg command line arguments
	 */
	public static void main(String arg[]) {
		final JFrame testFrame = new JFrame("Magnetic Field");
		
		final MagneticFields mf = MagneticFields.getInstance();
		
		//test explicit path load
		
		mf.initializeMagneticFields();

		testFrame.setLayout(new GridLayout(2, 1, 0, 10));
		// drawing canvas
		final MagneticFieldCanvas canvas1 = new MagneticFieldCanvas(-50, -350,
				650, 700., MagneticFieldCanvas.CSType.XZ);
		JPanel magPanel1 = canvas1.getPanelWithStatus(500, 465);
		final MagneticFieldCanvas canvas2 = new MagneticFieldCanvas(-50, -350,
				650, 700., MagneticFieldCanvas.CSType.YZ);
		JPanel magPanel2 = canvas2.getPanelWithStatus(500, 465);

		// set up what to do if the window is closed
		WindowAdapter windowAdapter = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				System.err.println("Done");
				System.exit(1);
			}
		};

		// add the menu
		JMenuBar mb = new JMenuBar();
		testFrame.setJMenuBar(mb);
		mb.add(mf.getMagneticFieldMenu(true, true));

		testFrame.add(magPanel1);
		testFrame.add(magPanel2);

		testFrame.addWindowListener(windowAdapter);
		testFrame.pack();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				testFrame.setVisible(true);
//				mf.compositeTest();
				
//				Runnable runner = new Runnable() {
//
//					@Override
//					public void run() {
//						mf.threadTest(mf);
//					}
//				};
//				new Thread(runner).start();
				
				//create full field torus
//				mf.createFullTorus(7); //experimental
				mf.createFullTorus(0);
				mf.createFullTorus(1);
				mf.createFullTorus(2);
				mf.createFullTorus(3);
				mf.createFullTorus(4);
				mf.createFullTorus(5);
				mf.createFullTorus(6);
//				mf.differentTorusTest(10000);
			}
		});

	}
	
//	private void differentTorusTest(int num) {
//		
//		
//		double phi0 = 13.80031;  double rho0 = 253.81205;  double z0 = 599.97256; 
//		
//		
//		
//		Random rand = new Random();
//		System.out.println("Starting random test");
//		
//		float r1[] = new float[3];
//		float r2[] = new float[3];
//		float r1max[] = new float[3];
//		float r2max[] = new float[3];
//		double diffMax = -1;
//		double phimax = Double.NaN;
//		double rhomax = Double.NaN;
//		double zmax = Double.NaN;
//		
//		
//		for (int i = 0; i < num; i++) {
//			double base = 60*rand.nextInt(6);
//			double phi = -30 + base + (3 + 54*rand.nextDouble());
//			
////			double phi = -29 + 58*rand.nextDouble();
//			
//			if (phi < 0) {
//				phi += 360;
//			}
//		
//			double rho = 4 + 490*rand.nextDouble();
//			double z = 104.0 + 490*rand.nextDouble();
//			
//			
//			double x = rho*Math.cos(Math.toRadians(phi));
//			double y = rho*Math.sin(Math.toRadians(phi));
//			
//		//	System.out.println("phi = " + phi + "  rho = " + rho + "   z = " + z);
//			
//			_torus.field((float)x, (float)y, (float)z, r1);
//			_fullTorus.field((float)x, (float)y, (float)z, r2);
//			double diff = diff(r1, r2);
//			if (diff > diffMax) {
//				diffMax = diff;
//				for (int j = 0; j < 3; j++) {
//					r1max[j] = r1[j];
//					r2max[j] = r2[j];
//					
//					rhomax = rho;
//					zmax = z;
//					phimax = phi;
//				}
//			}
//		}
//		String s = String.format("MaxDiff = %-9.7f  phi = %-8.5f  rho = %-8.5f  z = %-8.5f   %s   %s", 
//				diffMax, phimax, rhomax, 
//				zmax, printVec("symm ", r1max), printVec("full ", r2max) );
//		System.out.println(s);
//	}
//	
//	private String printVec(String name, float vec[]) {
//		return String.format(" %s [%-8.5f, %-8.5f, %-8.5f] ", name, vec[0], vec[1], vec[2]);
//	}
//	
//	private double diff(float r1[], float r2[]) {
//		double xsq = Math.pow(r2[0]-r1[0], 2);
//		double ysq = Math.pow(r2[1]-r1[1], 2);
//		double zsq = Math.pow(r2[2]-r1[2], 2);
//		return Math.sqrt(xsq + ysq + zsq);
//	}
//

	
	private String printVec(String name, float vec[]) {
		return String.format(" %s [%-8.5f, %-8.5f, %-8.5f] ", name, vec[0], vec[1], vec[2]);
	}
	
	private double diff(float r1[], float r2[]) {
		double xsq = Math.pow(r2[0]-r1[0], 2);
		double ysq = Math.pow(r2[1]-r1[1], 2);
		double zsq = Math.pow(r2[2]-r1[2], 2);
		return Math.sqrt(xsq + ysq + zsq);
	}
	
	
	//try to create full field torus
	private void createFullTorus(int opt) {
		
//		int modnum[] = {0, 2, 3, 4, 5, 6, 8};
		String pstfix[] = {"_0.25", "_0.50", "_0.75", "_1.00", 
				"_1.25", "_1.50", "_2.00", "_0.25E"};
		int nphi[] = {1441, 721, 481, 361, 289, 241, 181, 1441};
		double dphi[] = {0.25, 0.50, 0.75, 1.0, 1.25, 1.50, 2.00, 0.25};
		
		
		Torus torus = MagneticFields.getInstance()._torus;

		if (torus != null) {
			String binaryFileName = "../../../data/clas12TorusFull" + pstfix[opt] + ".dat";

			File file = new File(binaryFileName);
			if (file.exists()) {
				if (opt == 7) {
					file.delete();
				} else {
					try {
						System.out.println(file.getCanonicalPath()
								+ " already exists. Not overwriting. Delete it first if you want to remake.");
						return;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			System.out.println("\nAttempting to create full field torus.");

			try {
				System.err.println("Full map written at: " + new File(binaryFileName).getCanonicalPath());
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			try {
				DataOutputStream dos = new DataOutputStream(
						new FileOutputStream(binaryFileName));
				
				int nPhi = nphi[opt];
				int nRho = 251;
				int nZ = 251;
				double dp = dphi[opt];
				double dRho = 2.0;
				double dZ = 2.0;
				
				float phimin = 0.0f;
				float phimax = 360.0f;
				float rhomin = 0.0f;
				float rhomax = 500.0f;
				float zmin = 100.0f;
				float zmax = 600.0f;

				dos.writeInt(0xced);
				dos.writeInt(0);
				dos.writeInt(1);
				dos.writeInt(0);
				dos.writeInt(0);
				dos.writeInt(0);
				dos.writeFloat(phimin);
				dos.writeFloat(phimax);
				dos.writeInt(nPhi);
				dos.writeFloat(rhomin);
				dos.writeFloat(rhomax);
				dos.writeInt(nRho);
				dos.writeFloat(zmin);
				dos.writeFloat(zmax);
				dos.writeInt(nZ);
				
				//write reserved
				dos.writeInt(0);
				dos.writeInt(0);
				dos.writeInt(0);
				dos.writeInt(0);
				dos.writeInt(0);
				
				int size = 3 * 4 * nPhi * nRho * nZ;
				System.err.println("FILE SIZE = " + size + " bytes");

				byte bytes[] = new byte[size];
				
				ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
				FloatBuffer field = byteBuffer.asFloatBuffer();

				
				

				float result[] = new float[3];
				
				double oldScale = torus.getScaleFactor();
				torus.setScaleFactor(1.0);
				
				MagneticField.setInterpolate(true);

				int index = 0;
				int index1 = 7506285;
				int index2 = 7569286;	
								

				
				for (int phiIndex = 0; phiIndex < nPhi; phiIndex++) {

					// if ((opt == 0) || ((phiIndex % modnum[opt]) == 0)) {

					double phi = phiIndex * dp;

					if ((phiIndex % 8) == 0) {
						System.err.println("phi = " + phi);
					}
					for (int rhoIndex = 0; rhoIndex < nRho; rhoIndex++) {
							double rho = rhoIndex * dRho;
							for (int zIndex = 0; zIndex < nZ; zIndex++) {
								double z = zmin + zIndex * dZ;

								torus.fieldCylindrical(phi, rho, z, result);
								
//								if ((opt == 7) && (index == index2)) {
//									System.err.println("writing bad tagged values on purpose");
//									result[0] = 2.22222f;
//									result[1] = 3.33333f;
//									result[2] = 1.11111f;
//								}
								
								
//								dos.writeFloat(result[0]);
//								dos.writeFloat(result[1]);
//	
//								dos.writeFloat(result[2]);
								
								if ((index == index1) || (index == index2)) {
//									torus.fieldCylindrical(phi, rho, z, result);
									System.err.println(String.format("B at index = %d = (%5e, %5e, %5e)", index, result[0], result[1], result[2]));
									System.err.println("Phi = " + phi + "  rho = " + rho + "   z = " + z);
								}
								
							//	if (index == index2) System.exit(1);
								
								int xindex = 3*index;
								field.put(xindex, result[0]);
								field.put(xindex+1, result[1]);
								field.put(xindex+2, result[2]);

								index++;
							}

						}
//					}
				}
				
				dos.write(bytes);

				torus.setScaleFactor(oldScale);
				MagneticField.setInterpolate(true);

				
				dos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		
			if ((opt == 0) || (opt == 7)) {
				setTorus(TorusMap.FULL_025);
				float B1A = _torus.getB1(7506285);
				float B1B = _torus.getB1(7569286);
				System.err.println("B1's   " + B1A + "     " + B1B);

			}
		}
		else {
			System.err.println("\nNo torus from which to create full torus.");
		}
	}
		
	private void compositeTest() {
		
		System.err.println("========= START Composite TEST ==========");
		
		System.err.println("COMP FIELD 1: " + _compositeField.getName());
		CompositeField compositeField2 = new CompositeField();
		compositeField2.add(_torus);
		System.err.println("COMP FIELD 2: " + compositeField2.getName());
		
		
		_rotatedCompositeField.remove(_torus);
		_rotatedCompositeField.remove(_solenoid);
		_rotatedCompositeField.add(_solenoid);
		_rotatedCompositeField.add(_torus);
		System.err.println("ROT COMP FIELD 1: " + _rotatedCompositeField.getName());
		RotatedCompositeField rotatedCompositeField2 = new RotatedCompositeField();
		rotatedCompositeField2.add(_torus);
		System.err.println("ROT COMP FIELD 2: " + rotatedCompositeField2.getName());

		
		int N1 = 1000;
		int N2 = 1000;
		
		_solenoid.setZeroField(true);
		
		double xMax = 50;
		double yMax = 50;
		double zMax = 300;
		float b1[] = new float[3];
		float b2[] = new float[3];
		double TOLERANCE = 1.0e-6;

		for (int j = 0; j < N2; j++) {
			
			for (int i = 0; i < N1; i++) {
				double xs = xMax*Math.random();
				double ys = yMax*Math.random();
				double zs = zMax*Math.random();
				
				_rotatedCompositeField.field((float)xs, (float)ys, (float)zs, b1);
				rotatedCompositeField2.field((float)xs, (float)ys, (float)zs, b2);
				
				if ((absdif(b1[0], b2[0]) > TOLERANCE) || (absdif(b1[1], b2[1]) > TOLERANCE)
						|| (absdif(b1[2], b2[2]) > TOLERANCE)) {
					
					System.err.println("FAILURE");
					System.exit(1);
				}
			}
			

		}
		System.err.println("========= END Composite TEST (SUCCESS) ==========");

}
	
	private static double absdif(double v1, double v2) {
		return Math.abs(v1-v2);
	}


}
