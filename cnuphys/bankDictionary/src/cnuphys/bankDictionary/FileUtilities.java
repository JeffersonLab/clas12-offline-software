package cnuphys.bankDictionary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtilities {
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
	    root = System.getProperty("user.home");
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
     * Unzip a file into its current location
     * 
     * @param filename
     *            the full path to the zip file
     * @return a vector of (temproary) xml files that can be parsed
     */
    public static Vector<File> unzip(String filename) throws IOException {

	if (filename == null) {
	    throw new IOException("null zip file name in unzip");
	}

	ZipFile zipFile = new ZipFile(filename);

	Enumeration<?> entries = zipFile.entries();
	Vector<File> files = new Vector<File>();

	while (entries.hasMoreElements()) {
	    ZipEntry entry = (ZipEntry) entries.nextElement();
	    String name = entry.getName().toLowerCase();
	    if (name.endsWith(".xml")) {
		InputStream is = zipFile.getInputStream(entry);
		File tfile = File.createTempFile("bdef", null);
		tfile.deleteOnExit();
		FileOutputStream os = new FileOutputStream(tfile);
		copyInputStream(is, os);

		files.add(tfile);
	    }
	}

	zipFile.close();
	return files;
    }

    // copy input stream to output stream
    private static void copyInputStream(InputStream in, OutputStream out)
	    throws IOException {
	byte[] buffer = new byte[1024];
	int len;

	while ((len = in.read(buffer)) >= 0)
	    out.write(buffer, 0, len);

	in.close();
	out.close();
    }

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
     * Read an entire ascii file into a single string.
     * 
     * @param file
     *            the file to read
     * @return the string with the entire content of the file
     */
    public static String asciiFileToString(File file) {
	FileReader fileReader;
	try {
	    fileReader = new FileReader(file);
	} catch (FileNotFoundException e1) {
	    e1.printStackTrace();
	    return null;
	}
	StringBuffer sb = null;

	final BufferedReader bufferedReader = new BufferedReader(fileReader);

	boolean reading = true;
	while (reading) {
	    String s = null;
	    try {
		s = bufferedReader.readLine();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	    if (s != null) {
		if (sb == null) {
		    sb = new StringBuffer(10000);
		}
		sb.append(s + "\n");
	    } else {
		reading = false;
	    }
	}

	try {
	    bufferedReader.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}

	return sb.toString();
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
	    root = System.getProperty("user.home");
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

}
