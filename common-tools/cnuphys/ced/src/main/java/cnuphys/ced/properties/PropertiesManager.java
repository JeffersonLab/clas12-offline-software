package cnuphys.ced.properties;

import java.io.File;
import java.util.Properties;

import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.SerialIO;

public class PropertiesManager {

	private static PropertiesManager _instance;

	private static Properties _userPref;

	private static File _upFile;

	private PropertiesManager() {
	}

	public static PropertiesManager getInstance() {
		if (_instance == null) {
			_instance = new PropertiesManager();
			_instance.getPropertiesFromDisk();
		}
		return _instance;
	}
	
	public void putAndWrite(String key, String value) {
		if ((_userPref != null) && (_upFile != null) &&
				(key != null) && (value != null)) {
			
			_userPref.put(key, value);
			writeProperties();
		}
	}
	
	public String get(String key) {
		if ((_userPref != null) && (_upFile != null)) {
			return _userPref.getProperty(key);
		}
		return null;
	}

	public void writeProperties() {
		if ((_upFile == null) || (_userPref == null)) {
			return;
		}

		SerialIO.serialWrite(_userPref, _upFile.getPath());
	}

	private void getPropertiesFromDisk() {
		try {
			String homeDir = Environment.getInstance().getHomeDirectory();
			_upFile = new File(homeDir, ".ced.user.pref");
	//		System.err.println("User pref file: " + _upFile.getPath());
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
