package cnuphys.cnf.properties;

import java.io.File;
import java.util.Properties;

import org.jlab.io.base.DataEvent;

import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.util.Environment;
import cnuphys.bCNU.util.SerialIO;
import cnuphys.cnf.event.EventManager;
import cnuphys.cnf.event.IEventListener;

public class PropertiesManager implements IEventListener {

	private static PropertiesManager _instance;

	// the user preferences
	private static Properties _userPref;

	// the user preferences file
	private static File _upFile;

	// private constructor for simgleton
	private PropertiesManager() {
	}
	
	/**
	 * Get the singleton for the Properties Manager
	 * 
	 * @return the singleton
	 */
	public static PropertiesManager getInstance() {
		if (_instance == null) {
			_instance = new PropertiesManager();
			_instance.getPropertiesFromDisk();
			EventManager.getInstance().addEventListener(_instance, 0);
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

	@Override
	public void newEvent(DataEvent event, boolean isStreaming) {
	}

	@Override
	public void openedNewEventFile(File file) {
	}
	
	/**
	 * Rewound the current file
	 * @param file the file
	 */
	@Override
	public void rewoundFile(File file) {
		
	}
	

	/**
	 * Streaming start message
	 * @param file file being streamed
	 * @param numToStream number that will be streamed
	 */
	@Override
	public void streamingStarted(File file, int numToStream) {
	}
	
	/**
	 * Streaming ended message
	 * @param file the file that was streamed
	 * @param int the reason the streaming ended
	 */
	@Override
	public void streamingEnded(File file, int reason) {
	}

}
