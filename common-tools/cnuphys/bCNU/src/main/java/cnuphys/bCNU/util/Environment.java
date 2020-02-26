package cnuphys.bCNU.util;

import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
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
import javax.swing.JComponent;
import javax.swing.UIManager;
import cnuphys.bCNU.format.DoubleFormat;
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
	private static Color _defaultPanelBackgroundColor;

	// properties from a preferences file
	private Properties _properties;

	// used to save lists as single strings
	private static String LISTSEP = "$$";

	// this is used to recommend to non AWT threads to wait to call for an
	// update
	private boolean _dragging;
	
	//for scaling things like fonts
	private float _resolutionScaleFactor;
	
	//screen dots per inch
	private int _dotsPerInch;

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
		
		//screen information
		getScreenInformation();

		// any png image writers?
		Iterator<ImageWriter> iterator = ImageIO.getImageWritersByFormatName("png");
		if ((iterator == null) || !iterator.hasNext()) {
			System.err.println("no png writer");
		} else {
			_pngWriter = iterator.next(); // take the first
		}

		// read the preferences if the file exists
		File pfile = this.getPreferencesFile();
		_properties = null;
		if (pfile.exists()) {
			try {
				_properties = (Properties) SerialIO.serialRead(pfile.getPath());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (_properties == null) {
			_properties = new Properties();
		}
	}

	/**
	 * Get the common panel background color
	 * 
	 * @return the common panel background color
	 */
	public static Color getCommonPanelBackground() {
		return _defaultPanelBackgroundColor;
	}

	/**
	 * Check whether we are dragging or modifying an item.
	 * 
	 * @return <code>true</code> if we are dragging or modifying an item.
	 */
	public boolean isDragging() {
		return _dragging;
	}

	/**
	 * Set whether or not dragging is occurring. This cam be used to pause
	 * threads that might be affecting the screen.
	 * 
	 * @param dragging
	 *            <code>true</code> if dragging is occuring.
	 */
	public void setDragging(boolean dragging) {
		_dragging = dragging;
	}
	
	//to help with resolution issues
	private void getScreenInformation() {
		_dotsPerInch=java.awt.Toolkit.getDefaultToolkit().getScreenResolution(); 
		double dpcm = _dotsPerInch/2.54;
		_resolutionScaleFactor = (float) (dpcm/42.91);
	}
	
	/**
	 * For scaling things like fonts. Their size should be multiplied by this.
	 * @return the resolutionScaleFactor
	 */
	public float getResolutionScaleFactor() {
		return _resolutionScaleFactor;
	}
	
	/**
	 * Get the dots per inch for the main display
	 * @return the dots per inch
	 */
	public double getDotsPerInch() {
		return _dotsPerInch;
	}
	
	/**
	 * Get the dots per inch for the main display
	 * @return the dots per inch
	 */
	public double getDotsPerCentimeter() {
		return getDotsPerInch()/2.54;
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
	 * On Mac, uses the say command to say something.
	 * 
	 * @param sayThis
	 *            the string to say
	 */
	public void say(String sayThis) {
		if (sayThis == null) {
			return;
		}
		if (isMac()) {
			try {
				Runtime.getRuntime().exec("say -v Karen " + sayThis);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
			_defaultPanelBackgroundColor = UIManager.getColor("Panel.background");
			if (_defaultPanelBackgroundColor == null) {
				_defaultPanelBackgroundColor = new Color(238, 238, 238);
			}
		}

		return _defaultPanelBackgroundColor;
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
	 * @param key
	 *            the key
	 * @return the String corresponding to the key, or <code>null</code>.
	 */
	public String getPreference(String key) {
		if (_properties == null) {
			return null;
		}

		return _properties.getProperty(key);
	}

	/**
	 * Get the properties, which start out as the user preferences (or null) but
	 * which can be added to.
	 * 
	 * @return the properties
	 */
	public Properties getProperties() {
		return _properties;
	}

	/**
	 * Convenience method to get a Vector of strings as a single string in the
	 * preferences file. For example, it might be a Vector of recently visited
	 * files.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the vector holding the strings
	 * @return a Vector of preferences
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
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void savePreference(String key, String value) {

		if ((key == null) || (value == null)) {
			return;
		}

		if (_properties == null) {
			_properties = new Properties();
		}

		_properties.put(key, value);
		writePreferences();
	}

	/**
	 * Convenience method to save a Vector of strings as a single string in the
	 * preferences file. For example, it might be a Vector of recently visited
	 * files.
	 * 
	 * @param key
	 *            the key
	 * @param values
	 *            the vector holding the strings
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

			if ((_properties != null) && !_properties.isEmpty()) {
				SerialIO.serialWrite(_properties, file.getPath());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Useful for making common look components
	 * 
	 * @param component
	 *            the component
	 * @param color
	 *            the background color--if <code>null</code> use default.
	 */
	public void commonize(JComponent component, Color color) {
		component.setOpaque(true);
		color = (color == null) ? _defaultPanelBackgroundColor : color;
		component.setBackground(color);
	}

	/**
	 * Print a memory report
	 * 
	 * @param message
	 *            a message to add on
	 */
	public static String memoryReport(String message) {
		System.gc();
		System.gc();

		StringBuilder sb = new StringBuilder(1024);
		double total = (Runtime.getRuntime().totalMemory()) / 1048576.;
		double free = Runtime.getRuntime().freeMemory() / 1048576.;
		double used = total - free;
		sb.append("==== Memory Report =====\n");
		if (message != null) {
			sb.append(message + "\n");
		}
		sb.append("Total memory in JVM: " + DoubleFormat.doubleFormat(total, 1) + "MB\n");
		sb.append(" Free memory in JVM: " + DoubleFormat.doubleFormat(free, 1) + "MB\n");
		sb.append(" Used memory in JVM: " + DoubleFormat.doubleFormat(used, 1) + "MB\n");

		return sb.toString();

	}

	/**
	 * Get a short summary string
	 * 
	 * @return a short summary string
	 */
	public String summaryString() {
		return " [" + _userName + "]" + " [" + _osName + "]" + " [" + _hostName + "]" + " [" + _currentWorkingDirectory
				+ "]";
	}

	/**
	 * Split the application's class path into directories and jar files
	 * 
	 * @return an array of the class path segments
	 */
	private String[] splitClassPath() {
		return splitPath(_classPath);
	}
	
	/**
	 * Split the path into directories and jar files
	 * 
	 * @return an array of the path segments
	 */
	public String[] splitPath(String classPath) {
		String cp = new String(classPath);
		cp = cp.replace(".", File.separator);
 		return FileUtilities.tokens(cp, File.pathSeparator);
	}
	
	public GraphicsDevice[] getGraphicsDevices() {
		GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devices = g.getScreenDevices();

		return devices;
	}

	/**
	 * Convert to a string representation.
	 * 
	 * @return a string representation of the <code>Environment</code> object.
	 */

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(2048);
		sb.append("Environment: \n");
		
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
		sb.append("Current Working Directory: " + getCurrentWorkingDirectory() + "\n");
		sb.append("Class Path: " + getClassPath() + "\n");

		// not the tokenized class path
		String cptokens[] = splitClassPath();
		if (cptokens != null) {
			sb.append("Class Path Token Count: " + cptokens.length + "\n");
			for (int index = 0; index < cptokens.length; index++) {
				sb.append("  Class Path Token [" + index + "] = " + cptokens[index] + "\n");
			}
		}

		sb.append("Dots per Inch: " + _dotsPerInch + "\n");
		sb.append("Dots per Centimeter: " + DoubleFormat.doubleFormat(getDotsPerCentimeter(), 2) + "\n");
		sb.append("Resolution Scale Factor: " + DoubleFormat.doubleFormat(getResolutionScaleFactor(), 2) + "\n");
		sb.append("PNG Writer: " + ((_pngWriter == null) ? "none" : _pngWriter) + "\n");

		sb.append("Monitors:\n");
		GraphicsDevice[] devices = getGraphicsDevices();
		if (devices != null) {
			for (GraphicsDevice device : devices) {
				Rectangle bounds = device.getDefaultConfiguration().getBounds();
				int width = device.getDisplayMode().getWidth();
				int height = device.getDisplayMode().getWidth();
				sb.append("   [W, H] = [" + width + ", " + height + "] bounds: " + bounds + "\n");
			}
		}


		sb.append("\n" + memoryReport(null));
		return sb.toString();
	}

	/**
	 * Main program for testing.
	 * 
	 * @param arg
	 *            command line arguments (ignored).
	 */
	public static void main(String arg[]) {
		Environment env = Environment.getInstance();
//		env.say("Hello " + env.getUserName() + ", this is the bCNU Environment test.");
		System.out.println(env);
		System.out.println("Done.");

	}
}
