package cnuphys.splot.plot;

import java.awt.Color;
import java.awt.Font;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.swing.JComponent;
import javax.swing.UIManager;

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

	// png image writer, if there is one
	private ImageWriter _pngWriter;

	// default panel background color
	private Color _defaultPanelBackgroundColor;

	// common fonts
	private Font _commonFonts[] = new Font[50];
	private String _commonFontName;

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

		// any png image writers?
		Iterator<ImageWriter> iterator = ImageIO.getImageWritersByFormatName("png");
		if ((iterator == null) || !iterator.hasNext()) {
			System.err.println("no png writer");
		}
		else {
			_pngWriter = iterator.next(); // take the first
		}
	}

	/**
	 * Convenience method for sharing common fonts
	 * 
	 * @param size the size of the font
	 * @return the common font
	 */
	public Font getCommonFont(int size) {

		if (_commonFontName == null) {
			_commonFontName = "SansSerif";
		}

		if (isLinux()) {
			size = size - 1;
		}

		if (size >= _commonFonts.length) {
			return new Font(_commonFontName, Font.PLAIN, size);
		}

		if (_commonFonts[size] == null) {
			_commonFonts[size] = new Font(_commonFontName, Font.PLAIN, size);
		}
		return _commonFonts[size];
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
		}
		catch (Exception e) {
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
	 * Convert to a string representation.
	 * 
	 * @return a string representation of the <code>Environment</code> object.
	 */

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(1024);
		sb.append("Environment: \n");

		sb.append("User Name: " + getUserName() + "\n");
		sb.append("Temp Directory: " + getTempDirectory() + "\n");
		sb.append("OS Name: " + getOsName() + "\n");
		sb.append("Home Directory: " + getHomeDirectory() + "\n");
		sb.append("Current Working Directory: " + getCurrentWorkingDirectory() + "\n");
		sb.append("Class Path: " + getClassPath() + "\n");
		sb.append("PNG Writer: " + ((_pngWriter == null) ? "none" : _pngWriter) + "\n");
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
	 * Useful for making common look components
	 * 
	 * @param component the component
	 * @param color     the background color--if <code>null</code> use default.
	 */
	public void commonize(JComponent component, Color color) {
		component.setOpaque(true);
		color = (color == null) ? _defaultPanelBackgroundColor : color;
		component.setBackground(color);
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
		System.err.println("\n==== Memory Report =====");
		if (message != null) {
			System.err.println(message);
		}
		System.err.println("Total memory in JVM: " + DoubleFormat.doubleFormat(total, 1) + "MB");
		System.err.println(" Free memory in JVM: " + DoubleFormat.doubleFormat(free, 1) + "MB");
		System.err.println(" Used memory in JVM: " + DoubleFormat.doubleFormat(used, 1) + "MB");
		System.err.println();
	}

	public static void main(String arg[]) {
		System.out.println(Environment.getInstance());
	}
}