/**
 * 
 */
package cnuphys.bCNU.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import cnuphys.bCNU.component.filetree.ExtensionFileFilter;
import cnuphys.bCNU.graphics.ImageManager;

public class FileUtilities {

	// constants for append or overwrite
	public static final int AO_APPEND = 0;
	public static final int AO_OVERWRITE = 1;
	public static final int AO_CANCEL = 2;

	// the default directory
	private static String _defaultDir;

	/**
	 * Fixes a string so that any file separators match the current platform.
	 * 
	 * @param s
	 *            the input string.
	 * @return the string with the correct file separator.
	 */
	public static String fixSeparator(String s) {
		if (s == null) {
			return null;
		}

		if (File.separatorChar == '/') {
			return s.replace('\\', File.separatorChar);
		} else if (File.separatorChar == '\\') {
			return s.replace('/', File.separatorChar);
		}
		return s;
	}

	/**
	 * Fixes a string so that any path separators match the current platform.
	 * 
	 * @param s
	 *            the input string.
	 * @return the string with the correct path separator.
	 */
	public static String fixPathSeparator(String s) {
		if (s == null) {
			return null;
		}

		if (File.pathSeparatorChar == ';') {
			return s.replace(':', File.pathSeparatorChar);
		} else if (File.separatorChar == ':') {
			return s.replace(';', File.pathSeparatorChar);
		}
		return s;
	}

	/**
	 * Attempt to break a path down into its components.
	 * 
	 * @param path
	 *            the path to break down.
	 * @return an array of components.
	 */
	public static String[] tokenizePath(String path) {
		if (path == null) {
			return null;
		}

		String fixedPath = fixSeparator(path);
		return tokens(fixedPath, File.separator);
	}

	/*
	 * Gets the extension of a file.
	 * 
	 * @return the file extension, converted to lowercase, and assumed to be any
	 * characters after the last period. If no period, then returns
	 * <code>null</code>.
	 */
	public static String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}

	/**
	 * This method breaks a string into an array of tokens.
	 * 
	 * @param str
	 *            the string to decompose.
	 * @param delimiter
	 *            the delimiter
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
	 * This method breaks a string into an array of tokens.
	 * 
	 * @param str
	 *            the string to decompose.
	 * @return an array of tokens
	 */

	public static String[] tokens(String str) {

		StringTokenizer t = new StringTokenizer(str);
		int num = t.countTokens();
		String lines[] = new String[num];

		for (int i = 0; i < num; i++) {
			lines[i] = t.nextToken();
		}

		return lines;
	}

	/**
	 * Obtain a filter based on file extensions.
	 * 
	 * @param filterDescription
	 *            the filter description.
	 * @param extensions
	 *            Variable length list of extensions to filter on. If no filter,
	 *            pass null.
	 * @return a filter based on the extensions
	 */
	public static FileFilter extensionFilter(final String filterDescription,
			final String... extensions) {
		FileFilter ff = null;

		if ((extensions != null) && (extensions.length > 0)) {

			ff = new FileFilter() {

				@Override
				public boolean accept(File f) {
					if (f.isDirectory()) {
						return true;
					}

					String extension = getExtension(f);

					for (String fext : extensions) {
						if (fext.equals(extension)) {
							return true;
						}
					}
					return false;
				} // accept

				@Override
				public String getDescription() {
					return filterDescription;
				} // get description
			}; // file filter

		} // have extensions

		return ff;
	}

	/**
	 * Open a predefined save dialog
	 * 
	 * @param defaultDirName
	 *            the starting directory.
	 * @param defaultFileName
	 *            the default name of the saved file.
	 * @param filterDescription
	 *            the filter description.
	 * @param extensions
	 *            Variable length list of extensions to filter on. If no filter,
	 *            pass null.
	 * @return the File object of the saved file, or <code>null</code>.
	 */
	public static File saveFile(String defaultDirName, String defaultFileName,
			String filterDescription, String... extensions) {
		return saveFile(defaultDirName, defaultFileName, filterDescription,
				null, extensions);
	}

	/**
	 * Open a predefined save dialog
	 * 
	 * @param defaultDirName
	 *            the starting directory.
	 * @param defaultFileName
	 *            the default name of the saved file.
	 * @param filterDescription
	 *            the filter description.
	 * @param accessory
	 *            an acessory component used to customize the file chooser
	 * @param extensions
	 *            Variable length list of extensions to filter on. If no filter,
	 *            pass null.
	 * @return the File object of the saved file, or <code>null</code>.
	 */
	public static File saveFile(String defaultDirName, String defaultFileName,
			String filterDescription, JComponent accessory,
			String... extensions) {

		FileFilter ff = extensionFilter(filterDescription, extensions);

		JFileChooser fc = new JFileChooser();

		if (accessory != null) {
			fc.setAccessory(accessory);
		}

		if (ff != null) {
			fc.setFileFilter(ff);
		}

		fc.setSelectedFile(new File(defaultDirName, defaultFileName));

		int ans = fc.showSaveDialog(null);
		if (ans == JFileChooser.APPROVE_OPTION) {
			String filename = fc.getSelectedFile().getName();

			String dirname = fc.getSelectedFile().getParent();
			File file = new File(dirname, filename);

			// already exists?

			if (file.exists()) {
				int answer = JOptionPane.showConfirmDialog(null, filename
						+ "  already exists. Do you want to overwrite it?",
						"Overwite Existing File?", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, ImageManager.cnuIcon);

				if (answer == JFileChooser.APPROVE_OPTION) {
					return file;
				} else {
					return null;
				}

			} else {
				return file;
			}

		} else {
			return null;
		}

	} // saveFile

	/**
	 * Open a predefined open file dialog
	 * 
	 * @param defaultDirName
	 *            the starting directory.
	 * @param filterDescription
	 *            the filter description.
	 * @param extensions
	 *            Variable length list of extensions to filter on. If no filter,
	 *            pass null.
	 * @return the File object of the saved file, or <code>null</code>.
	 */
	public static File openFile(String defaultDirName,
			String filterDescription, String... extensions) {

		FileFilter ff = extensionFilter(filterDescription, extensions);

		JFileChooser fc = new JFileChooser();

		if (ff != null) {
			fc.setFileFilter(ff);
		}

		if (defaultDirName != null) {
			File dir = new File(defaultDirName);
			if ((dir.exists()) && (dir.isDirectory())) {
				fc.setCurrentDirectory(dir);
			}
		}

		int ans = fc.showOpenDialog(null);
		if (ans == JFileChooser.APPROVE_OPTION) {
			return fc.getSelectedFile();
		} else {
			return null;
		}

	} // openFile

	/**
	 * Open a predefined open file dialog
	 * 
	 * @param defaultDirName
	 *            the starting directory.
	 * @param filterDescription
	 *            the filter description.
	 * @param extensions
	 *            Variable length list of extensions to filter on. If no filter,
	 *            pass null.
	 * @return the File object of the saved file, or <code>null</code>.
	 */
	public static File[] openFiles(String defaultDirName,
			String filterDescription, String... extensions) {

		FileFilter ff = extensionFilter(filterDescription, extensions);

		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);

		if (ff != null) {
			fc.setFileFilter(ff);
		}

		fc.setCurrentDirectory(new File(defaultDirName));

		int ans = fc.showOpenDialog(null);
		if (ans == JFileChooser.APPROVE_OPTION) {
			return fc.getSelectedFiles();
		} else {
			return null;
		}

	} // openFile

	/**
	 * Strip the leading directory.
	 * 
	 * @param fullName
	 *            the full path name
	 * @param keepExtension
	 *            if <code>true</code> keep the extension, otherwise strip that
	 *            too.
	 * @return the bare file name.
	 */
	public static String bareName(String fullName, boolean keepExtension) {

		if (fullName == null) {
			return null;
		}

		String s = fixSeparator(fullName);
		int index = s.lastIndexOf(File.separatorChar);
		s = s.substring(index + 1);

		if (!keepExtension) {
			String bareName = new String(s);
			index = bareName.lastIndexOf(".");
			if (index > 1) {
				bareName = bareName.substring(0, index);
			}
			return bareName;
		}
		return s;
	}

	/**
	 * Given a search path and a baseName, find a file.
	 * 
	 * @param searchPath
	 *            the search path, e.g. "dir1;dir2;..."
	 * @param baseName
	 *            the base file name with extension, e.g., myfile.txt
	 * @return the first matching file.
	 */
	public static File findFile(String searchPath, String baseName) {
		if ((searchPath == null) || (baseName == null)) {
			return null;
		}

		String fixedPath = fixPathSeparator(searchPath);

		String tokens[] = tokens(fixedPath, File.pathSeparator);
		if (tokens != null) {
			for (String dir : tokens) {
				File file = new File(dir, baseName);
				if ((file != null) && file.exists()) {
					return file;
				}
			}
		}

		return null;
	}

	/**
	 * Given a search path and a baseName, find a readable file.
	 * 
	 * @param searchPath
	 *            the search path, e.g. "dir1;dir2;..."
	 * @param baseName
	 *            the base file name with extension, e.g., myfile.txt
	 * @return the first matching file that can be read.
	 */
	public static File findReadableFile(String searchPath, String baseName) {
		if ((searchPath == null) || (baseName == null)) {
			return null;
		}

		String tokens[] = tokens(searchPath, File.pathSeparator);
		if (tokens != null) {
			for (String dir : tokens) {
				File file = new File(dir, baseName);
				if ((file != null) && (file.exists() && (file.canRead()))) {
					return file;
				}
			}
		}

		return null;
	}

	/**
	 * @param defaultDir
	 *            the defaultDir to set
	 * @param checkExist
	 *            if <code>true</code>, only set if it exists.
	 */
	public static void setDefaultDir(String defaultDir, boolean checkExist) {

		if (defaultDir != null) {
			if (checkExist) {
				File file = new File(defaultDir);
				if (!file.exists() || !file.isDirectory()) {
					return;
				}
			}
		}
		FileUtilities._defaultDir = defaultDir;
	}

	/**
	 * @param defaultDir
	 *            the defaultDir to set
	 */
	public static void setDefaultDir(String defaultDir) {
		setDefaultDir(defaultDir, true);
	}

	/**
	 * @return the default directory
	 */
	public static String getDefaultDir() {
		return _defaultDir;
	}

	/**
	 * Takes an ascii file and returns an array of the non comment strings.
	 * Comments start with a given character (e.g., "!" or "#".) Inline comments
	 * starting with the usual "//" are also removed. Blank lines are also
	 * removed.
	 * 
	 * @param file
	 *            the file to be parsed.
	 * @param commentChar
	 *            if this is the first character the line is a comment line
	 * @return a string array of the non-comment text
	 */
	public static String[] nonComments(File file, String commentChar) {

		Vector<String> strings = null;

		try {
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			if (bufferedReader != null) {
				String s = bufferedReader.readLine();
				while (s != null) {
					if (!s.startsWith(commentChar)) {
						s = stripInLineComment(s);
						if (s != null) {
							s = s.trim();
							if (s.length() > 0) { // remove blank lines too
								if (strings == null) {
									strings = new Vector<String>(250, 10);
								}
								strings.add(s);
							}
						}
					}

					s = bufferedReader.readLine();
				} // s != null

				bufferedReader.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if ((strings == null) || (strings.size() < 1)) {
			return null;
		} else {
			String sarray[] = new String[strings.size()];
			int index = 0;
			for (String s : strings) {
				sarray[index] = s;
				index++;
			}
			return sarray;
		}
	}

	// strip in line commenets (starting from 1st "//")
	private static String stripInLineComment(String s) {
		if (s == null) {
			return null;
		}
		int index = s.indexOf("//");
		if (index >= 0) {
			s = s.substring(0, index);
		}
		if (s.length() < 1) {
			return null;
		}
		return s;
	}

	public static int appendOrOverwrite(String prompt) {
		Object[] options = { "Append", "Overwrite", "Cancel" };

		return JOptionPane.showOptionDialog(null, prompt,
				"Append or Overwrite?", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, ImageManager.cnuIcon, options, options[0]);
	}

	/**
	 * Converts a base path and some other path to the relative path (for the
	 * other path) For example if basePath is "/Users/heddle/clax/wd" and
	 * otherPath is "/Users/heddle/myws" this returns "../../myws"
	 * 
	 * @param basePath
	 *            the base path from which we denote relative paths
	 * @param otherPath
	 *            the path that we want to relativize
	 * @return the relative path
	 */
	public static String convertToRelativePath(String basePath, String otherPath) {
		StringBuilder relativePath = null;

		// Thanks to:
		// http://mrpmorris.blogspot.com/2007/05/convert-absolute-path-to-relative-path.html
		basePath = basePath.replaceAll("\\\\", "/");
		otherPath = otherPath.replaceAll("\\\\", "/");

		if (basePath.equals(otherPath) == true) {

		} else {
			String[] absoluteDirectories = basePath.split("/");
			String[] relativeDirectories = otherPath.split("/");

			// Get the shortest of the two paths
			int length = absoluteDirectories.length < relativeDirectories.length ? absoluteDirectories.length
					: relativeDirectories.length;

			// Use to determine where in the loop we exited
			int lastCommonRoot = -1;
			int index;

			// Find common root
			for (index = 0; index < length; index++) {
				if (absoluteDirectories[index]
						.equals(relativeDirectories[index])) {
					lastCommonRoot = index;
				} else {
					break;
					// If we didn't find a common prefix then throw
				}
			}
			if (lastCommonRoot != -1) {
				// Build up the relative path
				relativePath = new StringBuilder();
				// Add on the ..
				for (index = lastCommonRoot + 1; index < absoluteDirectories.length; index++) {
					if (absoluteDirectories[index].length() > 0) {
						relativePath.append("../");
					}
				}
				for (index = lastCommonRoot + 1; index < relativeDirectories.length - 1; index++) {
					relativePath.append(relativeDirectories[index] + "/");
				}
				relativePath
						.append(relativeDirectories[relativeDirectories.length - 1]);
			}
		}
		return relativePath == null ? null : relativePath.toString();
	}

	/**
	 * Scan from a root, digging down, looking for a named directory. Hidden
	 * directories (starting with a ".") are skipped.
	 * 
	 * @param root
	 *            the root dir to start from, if null the home dir is used.
	 * @param baseName
	 *            the baseName of the dir you are looking for. It can be a
	 *            subpath such as "bankdefs/trunk/clas12".
	 * @param maxLevel
	 *            the maximum number of levels to drill down
	 * @return the first matching dir that is found, or null.
	 */
	public static File findDirectory(String root, String baseName, int maxLevel) {
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
			root = Environment.getInstance().getHomeDirectory();
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
				} else {
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
	 * Scan from a root, digging down, looking for a named file. Hidden
	 * directories and files (starting with a ".") are skipped.
	 * 
	 * @param root
	 *            the root dir to start from, if null the home dir is used.
	 * @param baseName
	 *            the baseName of the file you are looking for. It can be a
	 *            subpath such as "bankdefs/trunk/clas12/field.data".
	 * @param maxLevel
	 *            the maximum number of levels to drill down
	 * @return the first matching file that is found, or null.
	 */
	public static File findFile(String root, String baseName, int maxLevel) {
		return findFile(fixSeparator(root), baseName, 0,
				Math.max(Math.min(maxLevel, 15), 1));
	}

	// recursive call used by public findDirectory
	private static File findFile(String root, String baseName,
			int currentLevel, int maxLevel) {

		if (baseName == null) {
			return null;
		}

		if (root == null) {
			root = Environment.getInstance().getHomeDirectory();
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
			// skip hidden files
			if (!file.getName().startsWith(".")
					&& !file.getName().startsWith("$")) {
				if (file.isDirectory()) {
					File ff = findFile(file.getPath(), baseName,
							currentLevel + 1, maxLevel);
					if (ff != null) {
						return ff;
					}
				} else if (file.getPath().endsWith(baseName)) {
					return file;
				}
			}
		} // end for
		return null;
	}

	/**
	 * Concatenate all the files in a directory into a single string
	 * 
	 * @param dir
	 *            the directory in question
	 * @param extension
	 *            the extension filter (e.g., "xml");
	 * @return a single string;
	 */
	public static String concatenate(File dir, String extension) {
		if ((dir == null) || !dir.isDirectory() || (extension == null)) {
			return null;
		}

		ArrayList<String> extensions = new ArrayList<String>();
		extensions.add(extension);

		ExtensionFileFilter ef = new ExtensionFileFilter(extensions);

		File files[] = dir.listFiles(ef);
		if (files == null) {
			return null;
		}

		StringBuffer sb = new StringBuffer(5000 * files.length);
		for (File file : files) {
			if (!file.isDirectory()) {
				sb.append(AsciiReadSupport.asciiFileToString(file));
			}
		}

		return sb.toString();
	}

	/**
	 * Obtain a list of classes found in a jar file
	 * 
	 * @param jarFile
	 *            the jar file to examine
	 * @param ignoreInner
	 *            if true, ignore if contains a "$"
	 * @return list of classes found in a jar file
	 */
	public static Vector<String> getJarEntries(File jarFile, boolean ignoreInner) {

		JarFile jar = null;
		try {
			jar = new JarFile(jarFile);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		Vector<String> classes = new Vector<String>(100, 25);
		// Getting the files into the jar

		Enumeration<? extends JarEntry> enumeration = jar.entries();
		while (enumeration.hasMoreElements()) {
			ZipEntry zipEntry = enumeration.nextElement();

			// Is this a class?
			if (zipEntry.getName().endsWith(".class")) {
				if (!ignoreInner || !zipEntry.getName().contains("$")) {
					classes.add(zipEntry.getName());
					// System.out.println(zipEntry.getName());
				}
			}
		}

		try {
			jar.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return classes;
	}


}
