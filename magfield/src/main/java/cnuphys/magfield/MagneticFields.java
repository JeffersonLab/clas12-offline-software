package cnuphys.magfield;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
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

	// solenoidal field
	private static Solenoid _solenoid;

	// torus field
	private static Torus _torus;

	// perfect solenoid
	private static PerfectSolenoid _perfectSolenoid;

	// mac test field
	private static MacTestField _macTest;

	// 1 tesla uniform
	private static ConstantField _uniform;

	// composite field
	private static CompositeField _compositeField;

	// composite rotated
	private static RotatedCompositeField _rotatedCompositeField;

	// optional full path to torus set by command line argument in ced
	private static String _torusFullPath;

	// optional full path to torus set by command line argument in ced
	private static String _solenoidFullPath;

	// optional full path to solenoid set by command line argument in ced

	// load the fields
	static {
		_torusFullPath = sysPropOrEnvVar("TORUSMAP");
		_solenoidFullPath = sysPropOrEnvVar("SOLENOIDMAP");

		getMagneticFields();
	}

	private static String sysPropOrEnvVar(String key) {
		String s = System.getProperty(key);
		if (s == null) {
			s = System.getenv(key);
		}

		System.out.println("***  For key: \"" + key + "\" found value: " + s);

		return s;
	}

	// trigger the fields to be loaded
	// private static boolean _firstTime = true;

	// which field is active
	private static IField _activeField;

	// types of fields
	public enum FieldType {
		TORUS, SOLENOID, COMPOSITE, COMPOSITEROTATED, PERFECTSOLENOID, ZEROFIELD, MACTEST, UNIFORM
	}

	// List of magnetic field change listeners
	private static EventListenerList _listenerList;

	// menu stuff
	private static JRadioButtonMenuItem _torusItem;
	private static JRadioButtonMenuItem _solenoidItem;
	private static JRadioButtonMenuItem _bothItem;
	private static JRadioButtonMenuItem _bothRotatedItem;
	private static JRadioButtonMenuItem _zeroItem;
	private static JRadioButtonMenuItem _perfectSolenoidItem;
	private static JRadioButtonMenuItem _uniformItem;
	private static JRadioButtonMenuItem _macTestItem;

	private static JRadioButtonMenuItem _interpolateItem;
	private static JRadioButtonMenuItem _nearestNeighborItem;

	private static ScaleFieldPanel _scaleTorusPanel;
	private static ScaleFieldPanel _scaleSolenoidPanel;

	/**
	 * Sets the full path to the torus map. If this is set before a call to
	 * getMagneticFields, it will look here first;
	 * 
	 * @param fullPath the full path to the torus field map.
	 */
	public static void setTorusFullPath(String fullPath) {
		_torusFullPath = fullPath;
	}

	/**
	 * Sets the full path to the solenoid map. If this is set before a call to
	 * getMagneticFields, it will look here first;
	 * 
	 * @param fullPath the full path to the solenoid field map.
	 */
	public static void setSolenoidFullPath(String fullPath) {
		_solenoidFullPath = fullPath;
	}

	/**
	 * Sets the active field
	 * 
	 * @param ftype one of the enum values
	 */
	public static void setActiveField(FieldType ftype) {
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
		case MACTEST:
			_activeField = _macTest;
			break;
		case PERFECTSOLENOID:
			_activeField = _perfectSolenoid;
			break;
		case UNIFORM:
			_activeField = _uniform;
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
	public static final String getActiveFieldDescription() {
		if (_activeField == _torus) {
			return "Torus Only";
		}
		else if (_activeField == _solenoid) {
			return "Solenoid Only";
		}
		else if (_activeField == _compositeField) {
			return "Torus and Solenoid";
		}
		else if (_activeField == _rotatedCompositeField) {
			return "Rotated Composite Field";
		}
		else if (_activeField == _macTest) {
			return "Mac's 1/r Test Field Field";
		}
		else if (_activeField == _perfectSolenoid) {
			return "Perfect Solenoid";
		}
		else if (_activeField == _uniform) {
			return "Uniform Field";
		}
		else if (_activeField == null) {
			return "Zero Field";
		}
		else {
			return "???";
		}
	}

	/**
	 * Get the active field
	 * 
	 * @return the active field
	 */
	public static IField getActiveField() {
		// init(); //harmless if already inited
		return _activeField;
	}

	/**
	 * Get a specific field map.
	 * 
	 * @param ftype the field map to get
	 * @return the field map, which might be <code>null</code>.
	 */
	public static IField getIField(FieldType ftype) {
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
		case MACTEST:
			ifield = _macTest;
			break;
		case PERFECTSOLENOID:
			ifield = _perfectSolenoid;
			break;
		case UNIFORM:
			ifield = _uniform;
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
	public static void field(float x, float y, float z, float result[]) {

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
	public static double maxFieldMagnitude() {
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
	public static void field(FieldType ftype, float x, float y, float z,
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
		case MACTEST:
			ifield = _macTest;
			break;
		case PERFECTSOLENOID:
			ifield = _perfectSolenoid;
			break;
		case UNIFORM:
			ifield = _uniform;
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

	/**
	 * Tries to load the magnetic fields from fieldmaps
	 */
	private static void getMagneticFields() {
		System.out.println("===========================================");
		System.out.println("======  Initializing Magnetic Fields  =====");
		System.out.println("===========================================");
		// can always make a mac test field and uniform
		_macTest = new MacTestField();
		_perfectSolenoid = new PerfectSolenoid();
		_uniform = new ConstantField(50);

		System.out.println("CWD: " + System.getProperty("user.dir"));

		// first try any supplied full path
		if (_torusFullPath != null) {
			System.out.println(
					"Will try to read torus from [" + _torusFullPath + "]");
			File file = new File(_torusFullPath);

			if (file.exists()) {
				try {
					_torus = Torus.fromBinaryFile(file);
					System.out.println("Read torus from: " + _torusFullPath);
				} catch (FileNotFoundException e) {
					System.err.println(
							"TORUS map not found in [" + _torusFullPath + "]");
				}
			}
		}
		
		if (_torus == null) {
			_torusFullPath = "../../../data/clas12_torus_fieldmap_binary.dat";
			System.out.println(
					"Will try to read torus from [" + _torusFullPath + "]");
			File file = new File(_torusFullPath);
			if (file.exists()) {
				try {
					_torus = Torus.fromBinaryFile(file);
					System.out.println("Read torus from: " + _torusFullPath);
				} catch (FileNotFoundException e) {
					System.err.println(
							"TORUS map not found in [" + _torusFullPath + "]");
				}
			}
		}

		if (_torus == null) {
			_torusFullPath = "data/clas12_torus_fieldmap_binary.dat";
			System.out.println(
					"Will try to read torus from [" + _torusFullPath + "]");
			File file = new File(_torusFullPath);
			if (file.exists()) {
				try {
					_torus = Torus.fromBinaryFile(file);
					System.out.println("Read torus from: " + _torusFullPath);
				} catch (Exception e) {
					System.err.println(
							"TORUS map not found in [" + _torusFullPath + "]");
				}
			}
		}

		// ditto
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

		// System.out.println("location: (27.16, 0.f, 65.71)");
		// System.out
		// .println(String
		// .format("Test field (should be 1.018, 0, 2.234): %6.3f, %6.3f, %6.3f
		// T",
		// result[0] / 10, result[1] / 10, result[2] / 10));
		// _compositeField.field(55.6f, 0.f, 427.14f, result);
		// System.out.println(String.format(
		// "Test field (should be 0, -1.747, 0): %6.3f, %6.3f, %6.3f T",
		// result[0] / 10, result[1] / 10, result[2] / 10));

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
	public static File findDirectory(String root, String baseName,
			int maxLevel) {
		return findDirectory(fixSeparator(root), baseName, 0,
				Math.max(Math.min(maxLevel, 15), 1));
	}

	// recursive call used by public findDirectory
	private static File findDirectory(String root, String baseName,
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
	public static String fixSeparator(String s) {
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
	public static JMenu getMagneticFieldMenu() {
		return getMagneticFieldMenu(false, false);
	}

	// inits the field (just once--harmless to call again
	// private static void init() {
	// // load the fields the first time we need them.
	// if (_firstTime) {
	// getMagneticFields();
	// _firstTime = false;
	// }
	// }

	/**
	 * Get the magnetic field menu
	 * 
	 * @return the magnetic field menu
	 */
	public static JMenu getMagneticFieldMenu(boolean incRotatedField,
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

		_torusItem = createRadioMenuItem("Torus", menu, bg,
				(_activeField != null) && (_activeField == _torus), al);
		_solenoidItem = createRadioMenuItem("Solenoid", menu, bg,
				(_activeField != null) && (_activeField == _solenoid), al);
		_bothItem = createRadioMenuItem("Torus and Solenoid", menu, bg,
				(_activeField != null) && (_activeField == _compositeField),
				al);

		if (incRotatedField) {
			_bothRotatedItem = createRadioMenuItem(
					"Torus and Solenoid (Rotated)", menu, bg,
					(_activeField != null)
							&& (_activeField == _rotatedCompositeField),
					al);
			_bothRotatedItem
					.setEnabled((_torus != null) && (_solenoid != null));
		}
		_zeroItem = createRadioMenuItem("No Field", menu, bg,
				(_activeField == null), al);

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

		if (includeTestFields) {
			menu.addSeparator();
			_uniformItem = createRadioMenuItem("Constant 5 Tesla Field", menu,
					bg, (_activeField != null) && (_activeField == _uniform),
					al);
			_perfectSolenoidItem = createRadioMenuItem(
					"Perfect 5 Tesla Solenoid", menu, bg, (_activeField != null)
							&& (_activeField == _perfectSolenoid),
					al);
			_macTestItem = createRadioMenuItem("Mac's 1/r Test Field", menu, bg,
					(_activeField != null) && (_activeField == _macTest), al);
		}

		_torusItem.setEnabled(_torus != null);
		_solenoidItem.setEnabled(_solenoid != null);
		_bothItem.setEnabled((_torus != null) && (_solenoid != null));

		if (_macTestItem != null) {
			_macTestItem.setEnabled(_macTest != null);
			_macTestItem.setEnabled(_uniform != null);
		}

		return menu;
	}

	// handle the radio menu selections from the magnetic field menu
	private static void handleMenuSelection(ActionEvent ae) {
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
		else if (source == _bothRotatedItem) {
			_activeField = _rotatedCompositeField;
		}
		else if (source == _zeroItem) {
			_activeField = null;
		}
		else if (source == _macTestItem) {
			_activeField = _macTest;
		}
		else if (source == _perfectSolenoidItem) {
			_activeField = _perfectSolenoid;
		}
		else if (source == _uniformItem) {
			_activeField = _uniform;
		}
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

	// handle the checkbox selections from the magnetic field menu
	private static void handleStateChange(ItemEvent iev) {
		Object source = iev.getSource();

		notifyListeners();
	}

	protected static void changedScale(MagneticField field) {
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
	protected static void notifyListeners() {

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
	 * @param MagneticFieldChangeListener the listener to add
	 */
	public static void addMagneticFieldChangeListener(
			MagneticFieldChangeListener MagneticFieldChangeListener) {

		if (_listenerList == null) {
			_listenerList = new EventListenerList();
		}

		// avoid adding duplicates
		_listenerList.remove(MagneticFieldChangeListener.class,
				MagneticFieldChangeListener);
		_listenerList.add(MagneticFieldChangeListener.class,
				MagneticFieldChangeListener);
	}

	/**
	 * Remove a MagneticFieldChangeListener.
	 * 
	 * @param MagneticFieldChangeListener the MagneticFieldChangeListener to
	 *            remove.
	 */

	public static void removeMagneticFieldChangeListener(
			MagneticFieldChangeListener MagneticFieldChangeListener) {

		if ((MagneticFieldChangeListener == null) || (_listenerList == null)) {
			return;
		}

		_listenerList.remove(MagneticFieldChangeListener.class,
				MagneticFieldChangeListener);
	}

	// convenience method for adding a radio button
	private static JRadioButtonMenuItem createRadioMenuItem(String label,
			JMenu menu, ButtonGroup bg, boolean on, ActionListener al) {
		JRadioButtonMenuItem mi = new JRadioButtonMenuItem(label, on);
		mi.addActionListener(al);
		bg.add(mi);
		menu.add(mi);
		return mi;
	}

	// convenience method for adding a checkbox button
	private static JCheckBoxMenuItem createCheckBoxMenuItem(String label,
			JMenu menu, boolean on, ItemListener il) {
		JCheckBoxMenuItem mi = new JCheckBoxMenuItem(label, on);
		mi.addItemListener(il);
		menu.add(mi);
		return mi;
	}

	/**
	 * Check whether we have a torus field
	 * 
	 * @return <code>true</code> if we have a torus
	 */
	public static boolean hasTorus() {
		return (_torus != null);
	}

	/**
	 * Check whether we have a solenoid field
	 * 
	 * @return <code>true</code> if we have a solenoid
	 */
	public static boolean hasSolenoid() {
		return (_solenoid != null);
	}

	/**
	 * Get the torus field
	 * 
	 * @return the torus field
	 */
	public static Torus getTorus() {
		return _torus;
	}

	/**
	 * Get the solenoid field
	 * 
	 * @return the solenoid field
	 */
	public static Solenoid getSolenoid() {
		return _solenoid;
	}

	/**
	 * For testing and also as an example
	 * 
	 * @param arg command line arguments
	 */
	public static void main(String arg[]) {
		final JFrame testFrame = new JFrame("Magnetic Field");

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
		mb.add(getMagneticFieldMenu(true, true));

		testFrame.add(magPanel1);
		testFrame.add(magPanel2);

		testFrame.addWindowListener(windowAdapter);
		testFrame.pack();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				testFrame.setVisible(true);
			}
		});

	}

	public static void main2(String arg[]) {
		int numThread = 20;

		Thread thread[] = new Thread[numThread];
		for (int i = 0; i < numThread; i++) {

			Runnable rmag = new Runnable() {

				@Override
				public void run() {
					float[] result = new float[3];
					for (int x = 10; x < 200; x = x + 2) {
						for (int z = 10; z < 400; z = z + 4) {
							MagneticFields.field(x, 0f, z, result);
						}

					}
					System.out.println(
							result[0] + ", " + result[1] + ", " + result[2]);
				}

			};

			thread[i] = new Thread(rmag);
		}

		for (Thread t : thread) {
			t.start();
		}

	}

}
