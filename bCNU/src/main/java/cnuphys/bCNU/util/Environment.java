package cnuphys.bCNU.util;

import java.awt.Color;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import cnuphys.bCNU.dialog.VerticalFlowLayout;
import cnuphys.bCNU.format.DoubleFormat;
import cnuphys.bCNU.graphics.component.CommonBorder;
import cnuphys.bCNU.log.Log;

/**
 * This utility class holds environmental information such as the home
 * directory, current working directory, host name, etc.
 * 
 * @author heddle
 * 
 */
public final class Environment {

	// singleton
	private static Environment instance;

	// User's home directory.
	private String _homeDirectory;

	// Current working directory
	private String _currentWorkingDirectory;

	// user name
	private String _userName;

	// operating system name
	private String _osName;

	// temporary directory
	private String _tempDirectory;

	// the java class path
	private String _classPath;

	// the host name
	private String _hostName;

	// the host IP address
	private String _hostAddress;

	// png image writer, if there is one
	private ImageWriter _pngWriter;

	// the application name
	private String _applicationName;

	// default panel background color
	private Color _defaultPanelBackgroundColor;

	// properties from a preferences file
	private Properties _preferences;

	// used to save lists as single strings
	private static String LISTSEP = "$$";

	/**
	 * Private constructor for the singleton.
	 */
	private Environment() {
		_homeDirectory = getProperty("user.home");
		_currentWorkingDirectory = getProperty("user.dir");
		_userName = getProperty("user.name");
		_osName = getProperty("os.name");

		_tempDirectory = getProperty("java.io.tmpdir");
		_classPath = getProperty("java.class.path");

		try {
			InetAddress addr = InetAddress.getLocalHost();

			// Get hostname
			_hostName = addr.getHostName();

			// Get host address
			_hostAddress = addr.getHostAddress();
		} catch (UnknownHostException e) {
			_hostName = "???";
			_hostAddress = "???";
		}

		// any png image writers?
		Iterator<ImageWriter> iterator = ImageIO
				.getImageWritersByFormatName("png");
		if ((iterator == null) || !iterator.hasNext()) {
			System.err.println("no png writer");
		}
		else {
			_pngWriter = iterator.next(); // take the first
		}

		// read the preferences if the file exists
		File pfile = this.getPreferencesFile();
		_preferences = null;
		if (pfile.exists()) {
			try {
				_preferences = (Properties) SerialIO
						.serialRead(pfile.getPath());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (_preferences == null) {
			_preferences = new Properties();
		}
	}

	/**
	 * Public access for the singleton.
	 * 
	 * @return the singleton object.
	 */
	public static Environment getInstance() {
		if (instance == null) {
			instance = new Environment();
		}
		return instance;
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

	/**
	 * Get the JAVA class path.
	 * 
	 * @return the JAVA class path.
	 */
	public String getClassPath() {
		return _classPath;
	}

	/**
	 * Get the current working directory.
	 * 
	 * @return the currentWorkingDirectory.
	 */
	public String getCurrentWorkingDirectory() {
		return _currentWorkingDirectory;
	}

	/**
	 * Gets the user's home directory.
	 * 
	 * @return the user's home directory.
	 */
	public String getHomeDirectory() {
		return _homeDirectory;
	}

	/**
	 * Gets the operating system name.
	 * 
	 * @return the operating system name..
	 */
	public String getOsName() {
		return _osName;
	}

	/**
	 * Gets the temp directory.
	 * 
	 * @return the tempDirectory.
	 */
	public String getTempDirectory() {
		return _tempDirectory;
	}

	/**
	 * Gets the user name.
	 * 
	 * @return the userName.
	 */
	public String getUserName() {
		return _userName;
	}

	/**
	 * Gets the host name.
	 * 
	 * @return the host name.
	 */
	public String getHostName() {
		return _hostName;
	}

	/**
	 * Gets the host address.
	 * 
	 * @return the host name.
	 */
	public String getHostAddress() {
		return _hostAddress;
	}

	/**
	 * Convert to a string representation.
	 * 
	 * @return a string representation of the <code>Environment</code> object.
	 */

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(1024);
		sb.append("Environment: \n");
		sb.append("Application: " + getApplicationName() + "\n");

		File file = getConfigurationFile();
		if (file == null) {
			sb.append("Config File: null\n");
		}
		else {
			sb.append("Config File: " + file.getAbsolutePath() + "\n");
		}
		sb.append("Host Name: " + getHostName() + "\n");
		sb.append("Host Address: " + getHostAddress() + "\n");
		sb.append("User Name: " + getUserName() + "\n");
		sb.append("Temp Directory: " + getTempDirectory() + "\n");
		sb.append("OS Name: " + getOsName() + "\n");
		sb.append("Home Directory: " + getHomeDirectory() + "\n");
		sb.append("Current Working Directory: " + getCurrentWorkingDirectory()
				+ "\n");
		sb.append("Class Path: " + getClassPath() + "\n");
		sb.append("PNG Writer: " + ((_pngWriter == null) ? "none" : _pngWriter)
				+ "\n");
		return sb.toString();
	}

	/**
	 * Check whether we are running on linux
	 * 
	 * @return <code>true</code> if we are running on linux
	 */
	public boolean isLinux() {
		return getOsName().toLowerCase().contains("linux");
	}

	/**
	 * Check whether we are running on Windows
	 * 
	 * @return <code>true</code> if we are running on Windows
	 */
	public boolean isWindows() {
		return getOsName().toLowerCase().contains("windows");
	}

	/**
	 * Check whether we are running on a Mac
	 * 
	 * @return <code>true</code> if we are running on a Mac
	 */
	public boolean isMac() {
		return getOsName().toLowerCase().startsWith("mac");
	}

	/**
	 * @return the pngWriter
	 */
	public ImageWriter getPngWriter() {
		return _pngWriter;
	}

	/**
	 * Get the application name. This is the simple part of the name of the
	 * class with the main metho. That is, if the main method is in
	 * com.yomama.yopapa.Dude, this returns "dude" (converts to lower case.)
	 * 
	 * @return the application name
	 */
	public String getApplicationName() {
		if (_applicationName == null) {
			try {
				ThreadMXBean temp = ManagementFactory.getThreadMXBean();
				ThreadInfo t = temp.getThreadInfo(1, Integer.MAX_VALUE);
				StackTraceElement st[] = t.getStackTrace();
				_applicationName = st[st.length - 1].getClassName();

				if (_applicationName != null) {
					int index = _applicationName.lastIndexOf(".");
					_applicationName = _applicationName.substring(index + 1);
					_applicationName = _applicationName.toLowerCase();
					Log.getInstance()
							.config("Application name: " + _applicationName);
				}
			} catch (Exception e) {
				_applicationName = null;
				Log.getInstance()
						.config("Could not determine application name.");
			}
		}
		return _applicationName;
	}

	/**
	 * Gets a File object for the configuration file. There is no guarantee that
	 * the file exists. It is the application name with a ".xml" extension in
	 * the user's home directory.
	 * 
	 * @return a File object for the configuration file
	 */
	public File getConfigurationFile() {
		String aname = getApplicationName();
		if (aname != null) {
			if (this.getOsName() == "Windows") {
				try {
					return new File(getHomeDirectory(), aname + ".xml");
				} catch (Exception e) {
					System.err
							.println("Could not get configuration file object");
				}
			}
			else { // Unix Based
				try {
					return new File(getHomeDirectory(), "." + aname + ".xml");
				} catch (Exception e) {
					System.err
							.println("Could not get configuration file object");
				}
			}
		}
		return null;
	}

	/**
	 * Singleton objects cannot be cloned, so we override clone to throw a
	 * CloneNotSupportedException.
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	/**
	 * Get the UIManager's choice for panel background color
	 * 
	 * @return the UIManager's choice for panel background color
	 */
	public Color getDefaultPanelBackgroundColor() {
		if (_defaultPanelBackgroundColor == null) {
			_defaultPanelBackgroundColor = UIManager
					.getColor("Panel.background");
			if (_defaultPanelBackgroundColor == null) {
				_defaultPanelBackgroundColor = new Color(238, 238, 238);
			}
		}

		return _defaultPanelBackgroundColor;
	}

	// this is used to recommend to non AWT threads to wait to call for an
	// update
	private boolean _dragging;

	/**
	 * Check whether we are dragging or modifying an item.
	 * 
	 * @return <code>true</code> if we are dragging or modifying an item.
	 */
	public boolean isDragging() {
		return _dragging;
	}

	/**
	 * Get a File object representing the preferences file. No guarantee that it
	 * exists.
	 * 
	 * @return a File object representing the preferences file.
	 */
	private File getPreferencesFile() {
		String bareName = getApplicationName() + ".pref";
		String dirName = getHomeDirectory();
		File file = new File(dirName, bareName);
		return file;
	}

	/**
	 * Obtain a preference from the key
	 * 
	 * @param key the key
	 * @return the String corresponding to the key, or <code>null</code>.
	 */
	public String getPreference(String key) {
		if (_preferences == null) {
			return null;
		}

		return _preferences.getProperty(key);
	}

	/**
	 * Convenience method to get a Vector of strings as a single string in the
	 * preferences file. For example, it might be a Vector of recently visited
	 * files.
	 * 
	 * @param key the key
	 * @param value the vector holding the strings
	 */
	public Vector<String> getPreferenceList(String key) {
		String s = getPreference(key);
		if (s == null) {
			return null;
		}
		String tokens[] = FileUtilities.tokens(s, LISTSEP);

		if ((tokens == null) || (tokens.length < 1)) {
			return null;
		}

		Vector<String> v = new Vector<String>(tokens.length);
		for (String tok : tokens) {
			v.add(tok);
		}
		return v;
	}

	/**
	 * Save a value in the preferences and write the preferneces file.
	 * 
	 * @param key the key
	 * @param value the value
	 */
	public void savePreference(String key, String value) {

		if ((key == null) || (value == null)) {
			return;
		}

		if (_preferences == null) {
			_preferences = new Properties();
		}

		_preferences.put(key, value);
		writePreferences();
	}

	/**
	 * Convenience method to save a Vector of strings as a single string in the
	 * preferences file. For example, it might be a Vector of recently visited
	 * files.
	 * 
	 * @param key the key
	 * @param value the vector holding the strings
	 */
	public void savePreferenceList(String key, Vector<String> values) {
		if ((key == null) || (values == null) || (values.isEmpty())) {
			return;
		}

		String s = "";
		int len = values.size();
		for (int i = 0; i < len; i++) {
			s += values.elementAt(i);
			if (i != (len - 1)) { // the separator
				s += LISTSEP;
			}
		}
		savePreference(key, s);
	}

	/**
	 * Write the preferences file to the home directory.
	 */
	private void writePreferences() {
		try {
			File file = getPreferencesFile();
			if (file.exists() && file.canWrite()) {
				file.delete();
			}

			if ((_preferences != null) && !_preferences.isEmpty()) {
				SerialIO.serialWrite(_preferences, file.getPath());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set whether or not dragging is occurring. This cam be used to pause
	 * threads that might be affecting the screen.
	 * 
	 * @param dragging <code>true</code> if dragging is occuring.
	 */
	public void setDragging(boolean dragging) {
		_dragging = dragging;
	}

	/**
	 * Print a memory report
	 * 
	 * @param message a message to add on
	 */
	public static void memoryReport(String message) {
		System.gc();
		System.gc();
		double total = (Runtime.getRuntime().totalMemory()) / 1048576.;
		double free = Runtime.getRuntime().freeMemory() / 1048576.;
		double used = total - free;
		System.out.println("\n==== Memory Report =====");
		if (message != null) {
			System.out.println(message);
		}
		System.out.println("Total memory in JVM: "
				+ DoubleFormat.doubleFormat(total, 1) + "MB");
		System.out.println(" Free memory in JVM: "
				+ DoubleFormat.doubleFormat(free, 1) + "MB");
		System.out.println(" Used memory in JVM: "
				+ DoubleFormat.doubleFormat(used, 1) + "MB");
		System.out.println();
	}

	public static JPanel getEnvironmentPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new VerticalFlowLayout());

		panel.setBorder(new CommonBorder("User Environment"));
		panel.add(panelLabel("User", getInstance().getUserName()));
		panel.add(panelLabel("Host", getInstance().getHostName()));
		panel.add(panelLabel("OS", getInstance().getOsName()));
		panel.add(
				panelLabel("CWD", getInstance().getCurrentWorkingDirectory()));

		return panel;
	}

	private static JLabel panelLabel(String prompt, String value) {
		JLabel label = new JLabel(prompt + ": " + value);
		label.setFont(Fonts.mediumFont);
		return label;
	}

	/**
	 * Get a short summary string
	 * 
	 * @return a short summary string
	 */
	public String summaryString() {
		return " [" + _userName + "]" + " [" + _osName + "]" + " [" + _hostName
				+ "]" + " [" + _currentWorkingDirectory + "]";
	}

	public static void main(String arg[]) {
		System.out.println(Environment.getInstance().toString());
		Environment.memoryReport(null);
	}

}
