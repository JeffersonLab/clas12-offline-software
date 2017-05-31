package randomChoice;

import java.awt.Color;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import javax.imageio.ImageWriter;
import javax.swing.JComponent;
import javax.swing.UIManager;


public class Environment {

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
				Runtime.getRuntime().exec("say -v Samantha " + sayThis);
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
	 * Get a short summary string
	 * 
	 * @return a short summary string
	 */
	public String summaryString() {
		return " [" + _userName + "]" + " [" + _osName + "]" + " [" + _hostName + "]" + " [" + _currentWorkingDirectory
				+ "]";
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
		

		sb.append("Host Name: " + getHostName() + "\n");
		sb.append("Host Address: " + getHostAddress() + "\n");
		sb.append("User Name: " + getUserName() + "\n");
		sb.append("Temp Directory: " + getTempDirectory() + "\n");
		sb.append("OS Name: " + getOsName() + "\n");
		sb.append("Home Directory: " + getHomeDirectory() + "\n");
		sb.append("Current Working Directory: " + getCurrentWorkingDirectory() + "\n");
		sb.append("Class Path: " + getClassPath() + "\n");

		sb.append("Dots per Inch: " + _dotsPerInch + "\n");
		sb.append("PNG Writer: " + ((_pngWriter == null) ? "none" : _pngWriter) + "\n");
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
		env.say("Hello " + env.getUserName() + ", this is the bCNU Environment test.");
		System.out.println(env);
		System.out.println("Done.");

	}
}
