package cnuphys.cnf.properties;

import java.io.File;
import java.util.Properties;

import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.SerialIO;

public class PropertiesManager {

	private static PropertiesManager _instance;

	// the user preferences
	private static Properties _userPref;

	// the user preferences file
	private static File _upFile;

	// private constructor for simgleton
	private PropertiesManager() {
	}
	
	/** property event string */
	public static final String STATE_CHANGE = "state change";

	/**
	 * Get the singleton for the Properties Manager
	 * 
	 * @return the singleton
	 */
	public static PropertiesManager getInstance() {
		if (_instance == null) {
			_instance = new PropertiesManager();
			_instance.getPropertiesFromDisk();
		}
		return _instance;
	}

	/**
	 * Put a value into the user prefs an write them out.
	 * 
	 * @param key   the key
	 * @param value the associated value
	 */
	public void putAndWrite(String key, String value) {
		if ((_userPref != null) && (_upFile != null) && (key != null) && (value != null)) {

			_userPref.put(key, value);
			writeProperties();
		}
	}

	/**
	 * Get a property
	 * 
	 * @param key the key value
	 * @return the associated property, or <code>null</code>
	 */
	public String get(String key) {
		if ((_userPref != null) && (_upFile != null)) {
			return _userPref.getProperty(key);
		}
		return null;
	}

	/**
	 * Write the user preferences to a disk file
	 */
	public void writeProperties() {
		if ((_upFile == null) || (_userPref == null)) {
			return;
		}

		SerialIO.serialWrite(_userPref, _upFile.getPath());
	}

	// read the properties
	private void getPropertiesFromDisk() {
		try {
			String homeDir = Environment.getInstance().getHomeDirectory();
			_upFile = new File(homeDir, ".cnf.user.pref");
			// System.err.println("User pref file: " + _upFile.getPath());
			if (_upFile.exists()) {
				_userPref = (Properties) SerialIO.serialRead(_upFile.getPath());
			} else {
				_userPref = new Properties();
			}
		} catch (Exception e) {
			_userPref = new Properties();
			Log.getInstance().exception(e);
		}
	}
}
