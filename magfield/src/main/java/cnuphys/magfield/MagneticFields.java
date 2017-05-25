package cnuphys.magfield;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.management.ManagementFactory;

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
	
	private boolean USE_BIG_TORUS = true;
	
	//initialize only once
	private boolean _initialized = false;

	// solenoidal field
	private Solenoid _solenoid;

	// torus field
	private Torus _torus;

	// composite field
	private CompositeField _compositeField;
	
	//small torus
	private SmallTorus _smallTorus;

	// composite rotated
	private RotatedCompositeField _rotatedCompositeField;

	// optional full path to torus set by command line argument in ced
	private String _torusFullPath = sysPropOrEnvVar("TORUSMAP");

	// optional full path to torus set by command line argument in ced
	private String _solenoidFullPath = sysPropOrEnvVar("SOLENOIDMAP");
	

	//singleton
	private static MagneticFields instance;

	// which field is active
	private IField _activeField;

	// types of fields
	public enum FieldType {
		TORUS, SOLENOID, COMPOSITE, COMPOSITEROTATED, ZEROFIELD, SMALLTORUS
	}

	// List of magnetic field change listeners
	private EventListenerList _listenerList;

	// menu stuff
	private JRadioButtonMenuItem _torusItem;
	private JRadioButtonMenuItem _solenoidItem;
	private JRadioButtonMenuItem _bothItem;
//	private JRadioButtonMenuItem _bothRotatedItem;
	private JRadioButtonMenuItem _zeroItem;
//	private JRadioButtonMenuItem _perfectSolenoidItem;
//	private JRadioButtonMenuItem _uniformItem;
//	private JRadioButtonMenuItem _macTestItem;

	private JRadioButtonMenuItem _interpolateItem;
	private JRadioButtonMenuItem _nearestNeighborItem;

	private ScaleFieldPanel _scaleTorusPanel;
	private ScaleFieldPanel _scaleSolenoidPanel;
	
	//private constructor for singleton
	private MagneticFields() {
		
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
	 * This programatically adjusts everything for new scale factors.
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
				_solenoid.setScaleFactor(torusScale);
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
			else if (_activeField == _smallTorus) {
				return FieldType.SMALLTORUS;
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
	 * Sets the full path to the torus map. If this is set before a call to
	 * getMagneticFields, it will look here first;
	 * 
	 * @param fullPath the full path to the torus field map.
	 */
	public void setTorusFullPath(String fullPath) {
		_torusFullPath = fullPath;
	}

	/**
	 * Sets the full path to the solenoid map. If this is set before a call to
	 * getMagneticFields, it will look here first;
	 * 
	 * @param fullPath the full path to the solenoid field map.
	 */
	public void setSolenoidFullPath(String fullPath) {
		_solenoidFullPath = fullPath;
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
		case SMALLTORUS:
			_activeField = _smallTorus;
			break;
		case ZEROFIELD:
			_activeField = null;
			break;
		}
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
		case SMALLTORUS:
			ifield = _smallTorus;
			break;
		case ZEROFIELD:
			ifield = null;
			break;
		}

		return ifield;
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
		case SMALLTORUS:
			ifield = _smallTorus;
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
	
	//try to get a torus, big or small
	private Torus getTorus(String baseName) {
		
		System.err.println("Attempting to use the " + (USE_BIG_TORUS ? "big" : "small") + " torus map.");
		
		Torus torus = null;
		
		// first try any supplied full path
		if (_torusFullPath != null) {
			System.out.println(
					"Will try to read torus from [" + _torusFullPath + "]");
			File file = new File(_torusFullPath);

			if (file.exists()) {
				try {
					if (USE_BIG_TORUS) {
						torus = Torus.fromBinaryFile(file);					
					}
					else {
						torus = SmallTorus.fromBinaryFile(file);
					}
					System.out.println("Read torus from: " + _torusFullPath);
				} catch (FileNotFoundException e) {
					System.err.println(
							"TORUS map not found in [" + _torusFullPath + "]");
				}
			}
		}
				
		if (torus == null) {
			_torusFullPath = "../../../data/" + baseName;
			System.out.println(
					"Will try to read torus from [" + _torusFullPath + "]");
			File file = new File(_torusFullPath);
			if (file.exists()) {
				try {
					if (USE_BIG_TORUS) {
						torus = Torus.fromBinaryFile(file);					
					}
					else {
						torus = SmallTorus.fromBinaryFile(file);
					}
					System.out.println("Read torus from: " + _torusFullPath);
				} catch (FileNotFoundException e) {
					System.err.println(
							"TORUS map not found in [" + _torusFullPath + "]");
				}
			}
		}

		if (torus == null) {
			_torusFullPath = "data/" + baseName;
			System.out.println(
					"Will try to read torus from [" + _torusFullPath + "]");
			File file = new File(_torusFullPath);
			if (file.exists()) {
				try {
					if (USE_BIG_TORUS) {
						torus = Torus.fromBinaryFile(file);					
					}
					else {
						torus = SmallTorus.fromBinaryFile(file);
					}
					System.out.println("Read torus from: " + _torusFullPath);
				} catch (Exception e) {
					System.err.println(
							"TORUS map not found in [" + _torusFullPath + "]");
				}
			}
		}	
		
		return torus;
	}

	/**
	 * Tries to load the magnetic fields from fieldmaps
	 */
	public void initializeMagneticFields() {
		
		if (_initialized) {
			return;
		}
		_initialized = true;
		
		System.out.println("===========================================");
		System.out.println("======  Initializing Magnetic Fields  =====");
		System.out.println("===========================================");
		// can always make a mac test field and uniform
		
		if (USE_BIG_TORUS) {
			_torus = getTorus("clas12_torus_fieldmap_binary.dat");
		}		
		else {
			_torus = getTorus("clas12_small_torus.dat");
		}

		// ditto for solenoid
		if (_solenoidFullPath != null) {
			System.out.println("Will try to read solenoid from ["
					+ _solenoidFullPath + "]");
			File file = new File(_solenoidFullPath);
			if (file.exists()) {
				try {
					_solenoid = Solenoid.fromBinaryFile(file);
					System.out.println(
							"Read solenoid from: " + _solenoidFullPath);
				} catch (FileNotFoundException e) {
					System.err.println("SOLENIOD map not found in ["
							+ _solenoidFullPath + "]");
				}
			}
		}
		
		if (_solenoid == null) {
			_solenoidFullPath = "../../../data/solenoid-srr.dat";
			System.out.println("Will try to read solenoid from ["
					+ _solenoidFullPath + "]");
			File file = new File(_solenoidFullPath);
			if (file.exists()) {
				try {
					_solenoid = Solenoid.fromBinaryFile(file);
					System.out.println(
							"Read solenoid from: " + _solenoidFullPath);
				} catch (FileNotFoundException e) {
					System.err.println("SOLENIOD map not found in ["
							+ _solenoidFullPath + "]");
				}
			}
		}
		if (_solenoid == null) {
			_solenoidFullPath = "data/solenoid-srr.dat";
			System.out.println("Will try to read solenoid from ["
					+ _solenoidFullPath + "]");
			File file = new File(_solenoidFullPath);
			if (file.exists()) {
				try {
					_solenoid = Solenoid.fromBinaryFile(file);
					System.out.println(
							"Read solenoid from: " + _solenoidFullPath);
				} catch (FileNotFoundException e) {
					System.err.println("SOLENIOD map not found in ["
							+ _solenoidFullPath + "]");
				}
			}
		}



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
		

		float result[] = new float[3];
		_compositeField.field(27.16f, 0.f, 65.71f, result);

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


	/**
	 * Scan from a root, digging down, looking for a named directory. Hidden
	 * directories (starting with a ".") are skipped.
	 * 
	 * @param root the root dir to start from, if null the home dir is used.
	 * @param baseName the baseName of the dir you are looking for. It can be a
	 *            subpath such as "bankdefs/trunk/clas12".
	 * @param maxLevel the maximum number of levels to drill down
	 * @return the first matching dir that is found, or null.
	 */
	public File findDirectory(String root, String baseName,
			int maxLevel) {
		return findDirectory(fixSeparator(root), baseName, 0,
				Math.max(Math.min(maxLevel, 15), 1));
	}

	// recursive call used by public findDirectory
	private File findDirectory(String root, String baseName,
			int currentLevel, int maxLevel) {

		if (baseName == null) {
			return null;
		}

		if (root == null) {
			root = System.getProperty("user.home");
		}

		if (currentLevel > maxLevel) {
			return null;
		}

		File rootDir = new File(root);
		if (!rootDir.exists() || !rootDir.isDirectory()) {
			return null;
		}

		File files[] = rootDir.listFiles();
		if (files == null) {
			return null;
		}

		for (File file : files) {

			if (file.isDirectory() && !file.getName().startsWith(".")
					&& !file.getName().startsWith("$")) {
				if (file.getPath().endsWith(baseName)) {
					return file;
				}
				else {
					File ff = findDirectory(file.getPath(), baseName,
							currentLevel + 1, maxLevel);
					if (ff != null) {
						return ff;
					}
				}
			}
		}

		return null;
	}

	/**
	 * Fixes a string so that any file separators match the current platform.
	 * 
	 * @param s the input string.
	 * @return the string with the correct file separator.
	 */
	public String fixSeparator(String s) {
		if (s == null) {
			return null;
		}

		if (File.separatorChar == '/') {
			return s.replace('\\', File.separatorChar);
		}
		else if (File.separatorChar == '\\') {
			return s.replace('/', File.separatorChar);
		}
		return s;
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
		JMenu menu = new JMenu("Magnetic Field");

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
			menu.add(_scaleSolenoidPanel);
		}

//		if (includeTestFields) {
//			menu.addSeparator();
//			_uniformItem = createRadioMenuItem(_uniform, "Constant 5 Tesla Field", menu, bg, al);
//			_perfectSolenoidItem = createRadioMenuItem(_perfectSolenoid,"Perfect 5 Tesla Solenoid", menu, bg, al);
//			_macTestItem = createRadioMenuItem(_macTest, "Mac's 1/r Test Field", menu, bg, al);
//		}

		_torusItem.setEnabled(_torus != null);
		_solenoidItem.setEnabled(_solenoid != null);
		_bothItem.setEnabled((_torus != null) && (_solenoid != null));

//		if (_macTestItem != null) {
//			_macTestItem.setEnabled(_macTest != null);
//			_macTestItem.setEnabled(_uniform != null);
//		}

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
//		else if (source == _bothRotatedItem) {
//			_activeField = _rotatedCompositeField;
//		}
//		else if (source == _macTestItem) {
//			_activeField = _macTest;
//		}
//		else if (source == _perfectSolenoidItem) {
//			_activeField = _perfectSolenoid;
//		}
//		else if (source == _uniformItem) {
//			_activeField = _uniform;
//		}
		else if (source == _interpolateItem) {
			MagneticField.setInterpolate(true);
		}
		else if (source == _nearestNeighborItem) {
			MagneticField.setInterpolate(false);

			// //print some values
			// System.err.println("Nearest neighbor values");
			// float result[] = new float[3];
			// float x = 5.0f;
			// float z = 10.0f;
			// getActiveField().field(x, 0f, z, result);
			// double Bt = Math.hypot(result[0], result[1]);
			// System.err.println("x = " + x + " z = " + z + " Bt = " + Bt/10 +
			// " Bl = " + result[2]/10);
			//
			// x = 5.1f;
			// getActiveField().field(x, 0f, z, result);
			// System.err.println("x = " + x + " z = " + z + " Bt = " + Bt/10 +
			// " Bl = " + result[2]/10);
			//
			//
			// x = 5.2f;
			// z = 10.6f;
			// getActiveField().field(x, 0f, z, result);
			// System.err.println("x = " + x + " z = " + z + " Bt = " + Bt/10 +
			// " Bl = " + result[2]/10);
		}

		notifyListeners();
	}

	//mag field changed scale
	protected void changedScale(MagneticField field) {
		if (field != null) {
			if (field == _torus) {
				_scaleTorusPanel._textField.setText(
						String.format("%7.3f", field.getScaleFactor()));
				notifyListeners();
			}
			else if (field == _solenoid) {
				_scaleSolenoidPanel._textField.setText(
						String.format("%7.3f", field.getScaleFactor()));
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
	 * Check whether we have a torus field
	 * 
	 * @return <code>true</code> if we have a torus
	 */
	public boolean hasTorus() {
		return (_torus != null);
	}

	/**
	 * Check whether we have a solenoid field
	 * 
	 * @return <code>true</code> if we have a solenoid
	 */
	public boolean hasSolenoid() {
		return (_solenoid != null);
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
				
				Runnable runner = new Runnable() {

					@Override
					public void run() {
						mf.threadTest(mf);
					}
				};
				new Thread(runner).start();
				
			}
		});

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
