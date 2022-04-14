package cnuphys.magfield;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

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

	public static Logger LOGGER = Logger.getLogger(MagneticField.class.getName());

	// used for rotating to tilted sector coordinates
	private static final double ROOT3OVER2 = Math.sqrt(3) / 2;
	private static double _cosPhi[] = { Double.NaN, 1, 0.5, -0.5, -1, -0.5, 0.5 };
	private static double _sinPhi[] = { Double.NaN, 0, ROOT3OVER2, ROOT3OVER2, 0, -ROOT3OVER2, -ROOT3OVER2 };

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
	private static String VERSION = "1.20";
	
	// solenoidal field
	private Solenoid _solenoid;
	
	// torus field (with 12-fold symmetry)
	private Torus _torus;

	// composite field solenoid and torus
	private CompositeField _compositeField;

	// composite (solenoid and torus) rotated to tilted coordinates
	private RotatedCompositeField _rotatedCompositeField;

	// optional full path to torus set by command line argument in ced
	private String _torusPath = sysPropOrEnvVar("TORUSMAP");

	// optional full path to solenoid set by command line argument in ced
	private String _solenoidPath = sysPropOrEnvVar("SOLENOIDMAP");
	
	// singleton
	private static MagneticFields instance;

	// which field is active
	private IMagField _activeField;

	// types of fields
	public enum FieldType {
		TORUS, SOLENOID, COMPOSITE, COMPOSITEROTATED, ZEROFIELD
	}

	// List of magnetic field change listeners
	private EventListenerList _listenerList;

	// menu stuff

	private JMenuItem _loadNewTorusItem; // load different torus
	private JMenuItem _loadNewSolenoidItem; // load different solenoid

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
	private MisplacedPanel _shiftTorusPanelX;
	private MisplacedPanel _shiftTorusPanelY;
	private MisplacedPanel _shiftTorusPanelZ;

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
	protected void openNewTorus() {

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
	 * @param path the path to the torus map
	 * @throws FileNotFoundException
	 */
	protected void openNewTorus(String path) throws FileNotFoundException {
		File file = new File(path);
		if (!file.exists()) {
			throw new FileNotFoundException("No torus at [" + path + "]");
		}

		Torus oldTorus = _torus;
		boolean activeFieldWasTorus = (_activeField == oldTorus);

		// load the torus
		_torus = null;
		_torus = readTorus(path);

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

		// LOGGER.log(Level.FINE,_torus);
		notifyListeners();

	}
	
	//checks whether the file is for a transverse solenoid
	//this implementation is primitive... looks at the name
	private boolean isTransverseSolenoidFile(File file) {
		if (file == null) {
			return false;
		}
		
		String fname = file.getName();
		return fname.toLowerCase().contains("trans");
	}

	
	/**
	 * Open a new solenoid map from the file selector
	 */
	private void openNewSolenoid() {
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Solenoid Maps", "dat", "solenoid", "map");

		JFileChooser chooser = new JFileChooser(dataFilePath);
		chooser.setSelectedFile(null);
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			
			if (file != null) {
				boolean isTransverse = isTransverseSolenoidFile(file);
				try {
					openSolenoid(file, isTransverse);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}		
	}


	/**
	 * Open a new solenoid map from a file
	 * 
	 * @param file the file holding the solenoid map
	 * @throws FileNotFoundException
	 */
	protected void openSolenoid(File file, boolean isTransverse) throws FileNotFoundException {
		if (!file.exists()) {
			throw new FileNotFoundException("No solenoid at [" + file.getPath() + "]");
		}

		Solenoid oldSolenoid = _solenoid;
		boolean activeFieldWasSolenoid = (_activeField == oldSolenoid);

		// load the solenoid
		_solenoid = null;
		_solenoid = readSolenoid(file.getAbsolutePath(), isTransverse);

		if (activeFieldWasSolenoid) {
			_activeField = _solenoid;
		}

		if (_solenoid != null) {
			if (_compositeField != null) {
				if (oldSolenoid != null) {
					_compositeField.remove(oldSolenoid);
				}
				if (_solenoid != null) {
					_compositeField.add(_solenoid);
				}
			}

			if (_rotatedCompositeField != null) {
				if (oldSolenoid != null) {
					_rotatedCompositeField.remove(oldSolenoid);
				}
				if (_solenoid != null) {
					_rotatedCompositeField.add(_solenoid);
				}
			}
		}

		notifyListeners();

	}


	/**
	 * Shift the solenoid along Z for misplacement. A negative shift moved the
	 * solenoid upstream. Kept for backwards compatibility
	 *
	 * @param shiftZ the shift in cm
	 */
	public void setSolenoidShift(double shiftZ) {
		shiftMagneticField(_solenoid, 0, 0, shiftZ);
	}

	/**
	 * Shift the magnetic field (i.e., a misalignment)
	 *
	 * @param field  the field, either _solenoid or torus
	 * @param shiftX the X shift in cm
	 * @param shiftY the Y shift in cm
	 * @param shiftZ the Z shift in cm A negative shift moved the field upstream.
	 */
	public void shiftMagneticField(MagneticField field, double shiftX, double shiftY, double shiftZ) {

		if (field == null) {
			return;
		}

		if (!(field instanceof Torus) && !(field instanceof Solenoid)) {
			return;
		}

		boolean shift = (Math.abs(_solenoid._shiftX - shiftX) > MagneticField.MISALIGNTOL);
		shift = shift || (Math.abs(_solenoid._shiftY - shiftY) > MagneticField.MISALIGNTOL);
		shift = shift || (Math.abs(_solenoid._shiftZ - shiftZ) > MagneticField.MISALIGNTOL);

		if (shift) {
			field.setShiftX(shiftX);
			field.setShiftY(shiftY);
			field.setShiftZ(shiftZ);
			MagneticFields.getInstance().changedShift(field);
		}

	}
	

	/**
	 * This programatically adjusts everything for new scale factors. This is used
	 * when data found in the file
	 *
	 * @param torusScale    the torus scale factor
	 * @param solenoidScale the solenoid scale factor
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
		} 
				
		else if (wantTorus && !wantSolenoid) {
			desiredFieldType = FieldType.TORUS;
		}

		else if (!wantTorus && wantSolenoid) {
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
		
		if (torusScaleChange && (_torus != null)) {
			// don't change scale if we aren't using torus
			if ((desiredFieldType == FieldType.TORUS) || (desiredFieldType == FieldType.COMPOSITE)) {
				_torus.setScaleFactor(torusScale);
				_scaleTorusPanel.fixText();
			}
		}
		if (solenoidScaleChange&& (_solenoid != null)) {
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
	 * Is the active field composite
	 *
	 * @return <code>true</code> of the active field is solenoid and torus composite
	 */
	public boolean isCompositeField() {
		return ((_activeField != null) && (_activeField == _compositeField));
	}

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
	 * @param field the new active field
	 */
	public void setActiveField(IMagField field) {
		_activeField = field;
	}

	/**
	 * Sets the active field
	 *
	 * @param ftype one of the enum values
	 */
	public void setActiveField(FieldType ftype) {
		if (ftype == getActiveFieldType()) {
			return;
		}
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
	public IMagField getActiveField() {
		return _activeField;
	}

	/**
	 * Get a specific field map.
	 *
	 * @param ftype the field map to get
	 * @return the field map, which might be <code>null</code>.
	 */
	public IMagField getIField(FieldType ftype) {
		IMagField ifield = null;

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
	 *
	 * @param ftype the field type
	 * @return the scale factor got the field type. Composite fields return NaN.
	 */
	public double getScaleFactor(FieldType ftype) {

		double scale = Double.NaN;

		switch (ftype) {
		case TORUS:
			if (_torus != null) {
				scale = _torus.getScaleFactor();
			}
			break;
		case SOLENOID:
			if (_solenoid != null) {
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
	 * @param ftype the field type
	 * @return the shift in z (cm) for the field type. Composite fields return NaN.
	 */
	public double getShiftZ(FieldType ftype) {

		double shiftz = Double.NaN;

		switch (ftype) {
		case TORUS:
			if (_torus != null) {
				shiftz = _torus.getShiftZ();
			}
			break;
		case SOLENOID:
			if (_solenoid != null) {
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
	 * In case someone loads a solenoid externally.
	 *
	 * @param solenoid
	 */
	public void setSolenoid(Solenoid solenoid) {
		if (solenoid != null) {
			if (solenoid != _solenoid) {
				LOGGER.log(Level.INFO,"Manually setting solenoid");
				_solenoid = solenoid;
				notifyListeners();
			}
		}
	}

	/**
	 * In case someone loads a torus externally.
	 *
	 * @param torus
	 */
	public void setTorus(Torus torus) {
		if (torus != null) {
			if (torus != _torus) {
				(new Throwable()).printStackTrace();
				LOGGER.log(Level.INFO,"Manually setting torus");
				_torus = torus;
				notifyListeners();
			}
		}
	}

	// read the solenoidal field
	private Solenoid readSolenoid(String fullPath, boolean isTransverse) {

		File file = new File(fullPath);

		Solenoid solenoid = null;
		if (file.exists()) {
			if (isTransverse) {
				try {
					solenoid = TransverseSolenoid.fromBinaryFile(file);
				} catch (Exception e) {
				}

			} else {
				try {
					solenoid = StandardSolenoid.fromBinaryFile(file);
				} catch (Exception e) {
				}
			}
		}

		_solenoidPath = fullPath;

		return solenoid;
	}
	

	// read the torus field
	private Torus readTorus(String fullPath) {

		File file = new File(fullPath);

		Torus torus = null;
		if (file.exists()) {
			try {
				torus = Torus.fromBinaryFile(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		_torusPath = fullPath;
		return torus;
	}

	/**
	 * This method breaks a string into an array of tokens.
	 *
	 * @param str       the string to decompose.
	 * @param delimiter the delimiter
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
	 * Attempts to initialize the magnetic fields using the property or environment
	 * variables TORUSMAP and SOLENOIDMAP as full paths to the torus and solenoid
	 *
	 * @throws MagneticFieldInitializationException if neither environment variable
	 *                                              is not found. Will proceed if
	 *                                              just one is found.
	 * @throws FileNotFoundException                if neither file is not found.
	 *                                              Will proceed if just one is
	 *                                              found.
	 */
	public void initializeMagneticFieldsFromEnv() throws MagneticFieldInitializationException, FileNotFoundException {
		_torusPath = sysPropOrEnvVar("COAT_MAGFIELD_TORUSMAP");
		if (_torusPath == null) {
			LOGGER.log(Level.SEVERE,"No envrionment variable or property named COAT_MAGFIELD_TORUSMAP");
		}

		_solenoidPath = sysPropOrEnvVar("COAT_MAGFIELD_SOLENOIDMAP");
		if (_solenoidPath == null) {
			LOGGER.log(Level.SEVERE,"No envrionment variable or property named COAT_MAGFIELD_SOLENOIDMAP");
		}

		if ((_torusPath == null) && (_solenoidPath == null)) {
			throw new MagneticFieldInitializationException();
		}

		initializeMagneticFieldsFromPath(_torusPath, _solenoidPath);
	}

	/**
	 * Initialize the magnetic field package
	 * 
	 * @param dataDir      the common data directory containing the torus and
	 *                     solenoid
	 * @param torusName    the base name of the torus map
	 * @param solenoidName the base name of the solenoid map
	 * @throws FileNotFoundException                if either full path is not null
	 *                                              but the corresponding file
	 *                                              cannot be found
	 * @throws MagneticFieldInitializationException if both full paths are null.
	 *                                              Will proceed as long as one path
	 *                                              is not null.
	 */
	public void initializeMagneticFields(String dataDir, String torusName, String solenoidName)
			throws FileNotFoundException, MagneticFieldInitializationException {
		
		
		if (dataDir == null) {
			return;
		}
		
		String torusPath = null;
		String solenoidPath = null;
		
		
		if (torusName != null) {
			torusPath = (new File(dataDir, torusName)).getPath();
		}
		if (solenoidName != null) {
			solenoidPath = (new File(dataDir, solenoidName)).getPath();
		}

		initializeMagneticFieldsFromPath(torusPath, solenoidPath);
	}

	/**
	 * Initialize the magnetic field package
	 *
	 * @param dataDir      the common data directory containing the torus and
	 *                     solenoid
	 * @param torusName    the base name of the torus map
	 * @param solenoidName the base name of the solenoid map
	 * @deprecated
	 * @throws FileNotFoundException                if either full path is not null
	 *                                              but the corresponding file
	 *                                              cannot be found
	 * @throws MagneticFieldInitializationException if both full paths are null.
	 *                                              Will proceed as long as one path
	 *                                              is not null.
	 */
	public void initializeMagneticFields(String dataDir, String torusName, String solenoidName, String transverseSolenoidName)
			throws FileNotFoundException, MagneticFieldInitializationException {
		
		
		LOGGER.log(Level.WARNING,"WARNING calling deprecated intialization with:");
		LOGGER.log(Level.WARNING,"  torus: " + torusName);
		LOGGER.log(Level.WARNING,"  solenoid: " + solenoidName);
		LOGGER.log(Level.WARNING,"  transverseSolenoid: " + transverseSolenoidName);
		
		LOGGER.log(Level.WARNING,"Change to initializeMagneticFields(dataDir, torusName, solenoidName)");
		LOGGER.log(Level.WARNING,"Where solenoidName is either a standard or transverse map.");
		
		
		String solName = (solenoidName != null) ? solenoidName : transverseSolenoidName;
		initializeMagneticFields(dataDir, torusName, solName);
	}

	

	/**
	 * Initialize the field from the two full paths. One of them can be null.
	 *
	 * @param torusPath    the full path to the torus map. Can be null.
	 * @param solenoidPath the full path to the solenoid map. Can be null.
	 * @throws MagneticFieldInitializationException if both paths are null. Will
	 *                                              proceed as long as one path is
	 *                                              not null.
	 * @throws FileNotFoundException                if either path is not null but
	 *                                              the corresponding file cannot be
	 *                                              found
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

		LOGGER.log(Level.INFO, "===========================================");
		LOGGER.log(Level.INFO, "  Initializing Magnetic Fields");
		LOGGER.log(Level.INFO, "  Version " + VERSION);
		LOGGER.log(Level.INFO, "  Contact: david.heddle@cnu.edu");
		LOGGER.log(Level.INFO, "");

		if (torusFile != null) {
			LOGGER.log(Level.INFO, "  TORUS: [" + torusPath + "]");
			_torus = readTorus(torusPath);
		}

		if (solenoidFile != null) {
			LOGGER.log(Level.INFO, "  SOLENOID: [" + solenoidPath + "]");
			// load the solenoid
			_solenoid = readSolenoid(solenoidPath, isTransverseSolenoidFile(solenoidFile));
		}
		

		LOGGER.log(Level.INFO,"  Torus loaded: " + (_torus != null));
		LOGGER.log(Level.INFO,"  Solenoid loaded: " + (_solenoid != null));
		LOGGER.log(Level.INFO,"===========================================");

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

		// dirs to try (they should have a magfield directory)

		ArrayList<String> dirs = new ArrayList<>();
		String coatdir = System.getenv("COATJAVA");
		String c12dir = System.getenv("CLAS12DIR");
		LOGGER.log(Level.INFO,"In initializeMagneticFields COATJAVA = [" + coatdir + "]");
		LOGGER.log(Level.INFO,"In initializeMagneticFields CLAS12DIR = [" + c12dir + "]");

		if (coatdir != null) {
			String d = coatdir + "/etc/data";
			d = d.replace("//", "/");
			dirs.add(d);
		}
		if (c12dir != null) {
			String d = c12dir + "/etc/data";
			d = d.replace("//", "/");
			dirs.add(d);
		}

		dirs.add(getProperty("user.dir"));
		dirs.add(getProperty("user.home"));
		dirs.add(getProperty(getProperty("user.dir") + "/../../../../../../etc/data"));

		int len = dirs.size();
		boolean goodDir[] = new boolean[len];

		for (int i = 0; i < len; i++) {
			File magdir = new File(dirs.get(i), "magfield");
			goodDir[i] = (magdir.exists() && magdir.isDirectory());

			try {
				LOGGER.log(Level.INFO,"MagDir [" + magdir.getCanonicalPath() + "]  Good: " + goodDir[i]);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		for (int i = 0; i < len; i++) {
			if (goodDir[i]) {
				File magdir = new File(dirs.get(i), "magfield");
				if (initializeMagneticFields(magdir)) {
					LOGGER.log(Level.INFO,"Used fields found in [" + magdir.getPath() + "]");
					return;
				} else {
					LOGGER.log(Level.SEVERE,"WARNING Unable to use fields found in [" + magdir.getPath() + "]");
				}
			}
		}

		LOGGER.log(Level.SEVERE,"WARNING Magnetic Field Package did not initialize.");
	}

	private boolean initializeMagneticFields(File magdir) {


		String defaultTorus = "Symm_torus_r2501_phi16_z251_24Apr2018.dat";
		File torusFile = new File(magdir, defaultTorus);
		if (!torusFile.exists()) {
			defaultTorus = "Symm_solenoid_r601_phi1_z1201_2008.dat";
			if (!torusFile.exists()) {
				torusFile = null;
			}
		}


		String defaultSolenoid = "Symm_solenoid_r601_phi1_z1201_13June2018.dat";
		File solenoidFile = new File(magdir, defaultSolenoid);
		if (!solenoidFile.exists()) {
			solenoidFile = null;
		}
		
		defaultTorus = (torusFile == null) ? null : torusFile.getPath(); 
		defaultSolenoid = (solenoidFile == null) ? null : solenoidFile.getPath(); 		

		try {
			MagneticFields.getInstance().initializeMagneticFieldsFromPath(defaultTorus,
					defaultSolenoid);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (MagneticFieldInitializationException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Convenience routine for getting a system property.
	 *
	 * @param keyName the key name of the property
	 * @return the property, or <code>null</null>.
	 */
	private String getProperty(String keyName) {
		try {
			return System.getProperty(keyName);
		} catch (Exception e) {
			return null;
		}
	}


	// make composite fields
	private void makeComposites() {
		_compositeField = new CompositeField();
		_rotatedCompositeField = new RotatedCompositeField();

		// print some features
		if (_torus != null) {
			_compositeField.add(_torus);
			_rotatedCompositeField.add(_torus);
		}
		if (_solenoid != null) {
			_compositeField.add(_solenoid);
			_rotatedCompositeField.add(_solenoid);
		}

		// set the default active field
		_activeField = null;
		if ((_torus != null) && (_solenoid != null)) {
			_activeField = _compositeField;
		}
		else if ((_torus != null) && (_solenoid != null)) {
			_activeField = _compositeField;
		}

		else if (_torus != null) {
			_activeField = _torus;
		}
		else if (_solenoid != null) {
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

		JMenu menu = new JMenu("Field");

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
		_bothItem = createRadioMenuItem(_compositeField, "Composite (Torus and Solenoid)", menu, bg, al);

		if (incRotatedField) {
			_bothRotatedItem = createRadioMenuItem(_rotatedCompositeField, "Rotated Composite (Torus and Solenoid)", menu, bg, al);
		}

		_zeroItem = createRadioMenuItem(null, "No Field", menu, bg, al);

		// interpolation related
		menu.addSeparator();
		ButtonGroup bgi = new ButtonGroup();
		_interpolateItem = createRadioMenuItem("Interpolate", menu, bgi, MagneticField.isInterpolate(), al);
		_nearestNeighborItem = createRadioMenuItem("Nearest Neighbor", menu, bgi, !MagneticField.isInterpolate(), al);

		if (_torus != null) {
			menu.addSeparator();
			_scaleTorusPanel = new ScaleFieldPanel(FieldType.TORUS, "Torus", _torus.getScaleFactor());
			_shiftTorusPanelX = new MisplacedPanel(FieldType.TORUS, "Torus", _torus.getShiftZ(), MisplacedPanel.SHIFTX);
			_shiftTorusPanelY = new MisplacedPanel(FieldType.TORUS, "Torus", _torus.getShiftZ(), MisplacedPanel.SHIFTY);
			_shiftTorusPanelZ = new MisplacedPanel(FieldType.TORUS, "Torus", _torus.getShiftZ(), MisplacedPanel.SHIFTZ);
			menu.add(_scaleTorusPanel);
			menu.add(_shiftTorusPanelX);
			menu.add(_shiftTorusPanelY);
			menu.add(_shiftTorusPanelZ);
		}

		if (_solenoid != null) {
			menu.addSeparator();
			_scaleSolenoidPanel = new ScaleFieldPanel(FieldType.SOLENOID, "Solenoid", _solenoid.getScaleFactor());
			_shiftSolenoidPanel = new MisplacedPanel(FieldType.SOLENOID, "Solenoid", _solenoid.getShiftZ(),
					MisplacedPanel.SHIFTZ);
			menu.add(_scaleSolenoidPanel);
			menu.add(_shiftSolenoidPanel);
		}
		


		_torusItem.setEnabled(_torus != null);
		_solenoidItem.setEnabled(_solenoid != null);
		_bothItem.setEnabled((_torus != null) && (_solenoid != null));

		if (_bothRotatedItem != null) {
			_bothRotatedItem.setEnabled((_torus != null) && (_solenoid != null));
		}

		menu.addSeparator();
		_loadNewTorusItem = new JMenuItem("Load a Different Torus...");
		_loadNewTorusItem.addActionListener(al);
		menu.add(_loadNewTorusItem);

		_loadNewSolenoidItem = new JMenuItem("Load a Different Solenoid...");
		_loadNewSolenoidItem.addActionListener(al);
		menu.add(_loadNewSolenoidItem);
		
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
		else if ((_bothRotatedItem != null) && (source == _bothRotatedItem)) {
			_activeField = _rotatedCompositeField;
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
		else if (source == _loadNewTorusItem) {
			openNewTorus();
		}
		else if (source == _loadNewSolenoidItem) {
			openNewSolenoid();
		}

		LOGGER.log(Level.INFO,"Active Field: " + getActiveFieldDescription());
		notifyListeners();
	}

	/**
	 * Make sure the menus are consistent.
	 */
	public void fixMenus() {
		boolean interpolate = MagneticField.isInterpolate();
		if (_nearestNeighborItem != null) {
			_nearestNeighborItem.setSelected(!interpolate);
		}
		if (_interpolateItem != null) {
			_interpolateItem.setSelected(interpolate);
		}

	}

	// mag field changed scale
	protected void changedScale(MagneticField field) {
		if (field != null) {
			if (field == _torus) {
				if (_scaleTorusPanel != null) {
					_scaleTorusPanel._textField.setText(String.format("%7.3f", field.getScaleFactor()));
				}
				notifyListeners();
			}
			else if (field == _solenoid) {
				if (_scaleSolenoidPanel != null) {
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
	 * @param magChangeListener the listener to add
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
	 * @param magChangeListener the MagneticFieldChangeListener to remove.
	 */

	public void removeMagneticFieldChangeListener(MagneticFieldChangeListener magChangeListener) {

		if ((magChangeListener == null) || (_listenerList == null)) {
			return;
		}

		_listenerList.remove(MagneticFieldChangeListener.class, magChangeListener);
	}

	// convenience method for adding a radio button
	private JRadioButtonMenuItem createRadioMenuItem(IMagField field, String label, JMenu menu, ButtonGroup bg,
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
	 * Adding the solenoid field to the torus field cutting of the solenoid in the
	 * overlap region, then cut off the solenoid at the min Z of the torus. This
	 * is an experimental method and irreversible. In particular rescaling the torus
	 * after doing this will cause the solenoid part of the overlap area to be
	 * scaled too. Which is nonsense.
	 */
	public void removeMapOverlap() {

		if ((_torus == null) || (_solenoid == null)) {
			return;
		}

		if (_torus.isSolenoidAdded()) {
			LOGGER.log(Level.SEVERE,"Cannot add solenoid into torus a second time.");
			return;
		}

		synchronized (_torus) {
			synchronized (_solenoid) {
				// the z and rho solenoid limits
				double solLimitZ = _solenoid.getZMax() + _solenoid._shiftZ;
				double solLimitR = _solenoid.getRhoMax();

				int stopIndexR = _torus.getQ2Coordinate().getIndex(solLimitR);
				int stopIndexZ = _torus.getQ3Coordinate().getIndex(solLimitZ);

				// float tRval = (float)
				// _torus.getQ2Coordinate().getValue(stopIndexR);
				// float tZval = (float)
				// _torus.getQ3Coordinate().getValue(stopIndexZ);

				// System.err.println("tRVal = " + tRval);
				// System.err.println("tZVal = " + tZval);

				float[] result = new float[3];

				FieldProbe probe = FieldProbe.factory(_solenoid);

				for (int nPhi = 0; nPhi < _torus.getQ1Coordinate().getNumPoints(); nPhi++) {
					double phi = _torus.getQ1Coordinate().getValue(nPhi);

					//System.err.println("PHI = " + phi);
					double phiRad = Math.toRadians(phi);

					double cosPhi = Math.cos(phiRad);
					double sinPhi = Math.sin(phiRad);

					// get the solenoid field
					for (int nRho = 0; nRho <= stopIndexR; nRho++) {
						double rho = _torus.getQ2Coordinate().getValue(nRho);
						// System.err.println("Rho = " + rho);

						float x = (float) (rho * cosPhi);
						float y = (float) (rho * sinPhi);

						for (int nZ = 0; nZ <= stopIndexZ; nZ++) {
							double z = _torus.getQ3Coordinate().getValue(nZ);
							// System.err.println("Z = " + z);

							// get the solenoid field
							probe.field(x, y, (float) z, result);

							// composite index
							int index = _torus.getCompositeIndex(nPhi, nRho, nZ);
							_torus.addToField(index, result);

						}

					}

				}

				// now cutoff the solenoid
				double zlim = _torus.getZMin();
				_solenoid.setFakeZMax(zlim);
			}
		}

		notifyListeners();
	}

	/**
	 * Check whether we have an active torus field
	 *
	 * @return <code>true</code> if we have a torus
	 */
	public boolean hasActiveTorus() {

		if (_activeField != null) {
			if (_activeField instanceof Torus) {
				return true;
			} else if (_activeField instanceof TorusProbe) {
				return true;
			} else if (_activeField instanceof CompositeProbe) {
				return ((CompositeProbe) _activeField).hasTorus();
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
	public boolean hasActiveSolenoid() {
		if (_activeField != null) {
			if (_activeField instanceof Solenoid) {
				return true;
			} else if (_activeField instanceof SolenoidProbe) {
				return true;
			} else if (_activeField instanceof CompositeProbe) {
				return ((CompositeProbe) _activeField).hasSolenoid();
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
	 * @return the full solenoid path
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
	 * @param lab    will hold the lab 3D Cartesian coordinates (modified)
	 * @param x      the sector x coordinate
	 * @param y      the sector y coordinate
	 * @param z      the sector z coordinate
	 */

	public static void sectorToLab(int sector, float lab[], float x, float y, float z) {

		if ((sector < 1) || (sector > 6)) {
			String wstr = "Bad sector: " + sector + " in MagneticFields sectorToLab";
			LOGGER.log(Level.SEVERE,wstr);
			return;
		}

		lab[2] = z; // z independent of sector

		if (sector == 1) {
			lab[0] = x;
			lab[1] = y;
		} else if (sector == 4) {
			lab[0] = -x;
			lab[1] = -y;
		} else { // sectors 2, 3, 5, 6
			double cosP = _cosPhi[sector];
			double sinP = _sinPhi[sector];

			lab[0] = (float) (cosP * x - sinP * y);
			lab[1] = (float) (sinP * x + cosP * y);
		}
	}

	/**
	 * Converts the clas (lab) 3D coordinates to sector 3D coordinates to
	 *
	 * @param sector the 1-based sector [1..6]
	 * @param lab    will hold the lab 3D Cartesian coordinates (modified)
	 * @param x      the lab x coordinate
	 * @param y      the lab y coordinate
	 * @param z      the lab z coordinate
	 */

	public static void labToSector(int sector, float sect[], float x, float y, float z) {

		if ((sector < 1) || (sector > 6)) {
			String wstr = "Bad sector: " + sector + " in MagneticFields labToSector";
			LOGGER.log(Level.SEVERE,wstr);
			return;
		}

		sect[2] = z; // z independent of sector

		if (sector == 1) {
			sect[0] = x;
			sect[1] = y;
		} else if (sector == 4) {
			sect[0] = -x;
			sect[1] = -y;
		} else { // sectors 2, 3, 5, 6
			double cosP = _cosPhi[sector];
			double sinP = _sinPhi[sector];

			sect[0] = (float) (cosP * x + sinP * y);
			sect[1] = (float) (-sinP * x + cosP * y);
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
			s = s + "Solenoid [" + sbn + "] ";
		}

		return s;
	}

	/**
	 * Returns the time as a string.
	 *
	 * @param longtime the time in millis.
	 * @return a string representation of the current time, down to seconds.
	 */
	public static String dateStringLong(long longtime) {
		return formatterlong.format(longtime);
	}

	/**
	 * Is this a probe or a composite probe?
	 *
	 * @param field the object to test
	 * @return <code>t
	 */
	public boolean isProbeOrCompositeProbe(IField field) {
		// already a probe?
		if ((field instanceof FieldProbe) || (field instanceof CompositeProbe)
				|| (field instanceof RotatedCompositeProbe)) {
			return true;
		}

		return false;
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
	 * For testing and also as an example
	 *
	 * @param arg command line arguments
	 */
	public static void main(String arg[]) {
		MagTests.runTests();
	}

	public String getCurrentConfiguration() {
		String s = getActiveFieldType().name();

		// TORUS, SOLENOID, COMPOSITE, COMPOSITEROTATED, ZEROFIELD
		switch (getActiveFieldType()) {
		case TORUS:
			if (!_torus.isZeroField()) {
				s += " ";
				s += _torus.getBaseFileName();
			}
			break;
			
		case SOLENOID:
			if (!_solenoid.isZeroField()) {
				s += " ";
				s += _solenoid.getBaseFileName();
			}
			break;
			
		case COMPOSITE:
		case COMPOSITEROTATED:
			if (!_solenoid.isZeroField()) {
				s += " ";
				s += _solenoid.getBaseFileName();
			}
			if (!_torus.isZeroField()) {
				s += " ";
				s += _torus.getBaseFileName();
			}
			break;
			
			
		case ZEROFIELD:
			s += " zero field";
			break;
		}

		return s;
	}

	public String getCurrentConfigurationMultiLine() {
		String s = getActiveFieldType().name() + "\n";

		// TORUS, SOLENOID, COMPOSITE, COMPOSITEROTATED, ZEROFIELD
		switch (getActiveFieldType()) {
		case TORUS:
			s += String.format("Torus [%s] scale: %-7.3f\n", _torus.getBaseFileName(), _torus.getScaleFactor());
			break;
			
		case SOLENOID:
			s += String.format("Solenoid [%s] scale: %-7.3f\n", _solenoid.getBaseFileName(),
					_solenoid.getScaleFactor());
			break;
			
			
		case COMPOSITE:
		case COMPOSITEROTATED:
			s += String.format("Solenoid [%s] scale: %-7.3f\n", _solenoid.getBaseFileName(),
					_solenoid.getScaleFactor());
			s += String.format("Torus [%s] scale: %-7.3f\n", _torus.getBaseFileName(), _torus.getScaleFactor());
			break;
			
			
		case ZEROFIELD:
			s += " zero field";
			break;
		}

		return s;
	}

	/**
	 * Write the binary file header
	 * @param dos the output stream
	 * @param gridCS grid coordinate system [0: cylindrical, 1:Cartesian]
	 * @param fieldCS field coordinate system [0: cylindrical, 1:Cartesian]
	 * @param lenUnit length units [0:cm, 1:m]
	 * @param angUnit angular units [0:deg, 1:radians]
	 * @param fieldUnit field units [0:kG, 1:T]
	 * @param q1Min first coordinate min
	 * @param q1Max first coordinate max
	 * @param nq1 first coordinate num grid points including ends
	 * @param q2Min second coordinate min
	 * @param q2Max second coordinate max
	 * @param nq2 second coordinate num grid points including ends
	 * @param q3Min third coordinate min
	 * @param q3Max third coordinate max
	 * @param nq3 third coordinate num grid points including ends
	 */
	public static void writeHeader(DataOutputStream dos, int gridCS,
								   int fieldCS, int lenUnit, int angUnit, int fieldUnit,
								   float q1Min, float q1Max, int nq1,
								   float q2Min, float q2Max, int nq2,
								   float q3Min, float q3Max, int nq3) {
		try {
			dos.writeInt(0xced);
			dos.writeInt(gridCS);
			dos.writeInt(fieldCS);
			dos.writeInt(lenUnit);
			dos.writeInt(angUnit);
			dos.writeInt(fieldUnit);
			dos.writeFloat(q1Min);
			dos.writeFloat(q1Max);
			dos.writeInt(nq1);
			dos.writeFloat(q2Min);
			dos.writeFloat(q2Max);
			dos.writeInt(nq2);
			dos.writeFloat(q3Min);
			dos.writeFloat(q3Max);
			dos.writeInt(nq3);
			long unixTime = System.currentTimeMillis();

			int high = (int) (unixTime >> 32);
			int low = (int) unixTime;

			// write time and reserved
			dos.writeInt(high); // first word of unix time
			dos.writeInt(low); // second word of unix time
			dos.writeInt(0);
			dos.writeInt(0);
			dos.writeInt(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}

	/**
	 * Print a one line version of the magnetic field configuration
	 *
	 * @param ps the print stream
	 */
	public void printCurrentConfiguration(PrintStream ps) {
		ps.println("Current magfied configuration: ");
		getActiveField().printConfiguration(ps);
	}

}
