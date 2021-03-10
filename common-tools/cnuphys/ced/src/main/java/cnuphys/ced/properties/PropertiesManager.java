package cnuphys.ced.properties;

import java.io.File;
import java.util.Properties;

import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.SerialIO;

/**
 * For dealing with persistent user preferences
 * @author heddle
 *
 */
public class PropertiesManager {

	private static PropertiesManager _instance;

	private static Properties _userPref;

	private static File _upFile;

	//private constructor for singleton
	private PropertiesManager() {
	}

	/**
	 * Public access for the singleton
	 * @return the PropertiesManager singleton.
	 */
	public static PropertiesManager getInstance() {
		if (_instance == null) {
			_instance = new PropertiesManager();
			_instance.getPropertiesFromDisk();
		}
		return _instance;
	}

	/**
	 * Put in a property, then write the preferences
	 * @param key the key
	 * @param value the value
	 */
	public void putAndWrite(String key, String value) {
		if ((_userPref != null) && (_upFile != null) && (key != null) && (value != null)) {

			_userPref.put(key, value);
			writeProperties();
		}
	}

	/**
	 * Get a property from the user preferences
	 * @param key the key
	 * @return the property, or <code>null</code> if not found
	 */
	public String get(String key) {
		if ((_userPref != null) && (_upFile != null)) {
			return _userPref.getProperty(key);
		}
		return null;
	}

	/**
	 * Write the properties file. Should be .ced.user.pref in the home dir
	 */
	private void writeProperties() {
		if ((_upFile == null) || (_userPref == null)) {
			return;
		}

		SerialIO.serialWrite(_userPref, _upFile.getPath());
	}

	/**
	 * Read the property from from the home forectory.
	 */
	private void getPropertiesFromDisk() {
		try {
			String homeDir = Environment.getInstance().getHomeDirectory();
			_upFile = new File(homeDir, ".ced.user.pref");
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
