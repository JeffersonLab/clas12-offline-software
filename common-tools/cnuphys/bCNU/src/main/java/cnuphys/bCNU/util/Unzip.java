package cnuphys.bCNU.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import cnuphys.bCNU.log.Log;

/**
 * @author heddle
 * 
 */
public class Unzip {

	private static Log log = Log.getInstance();

	/**
	 * Unzip a file into its current location
	 * 
	 * @param filename
	 *            the full path to the zip file
	 * @return a vector of strings, each entry a full path to a created file
	 */
	public static Vector<String> unzip(String filename) throws IOException {

		if (filename == null) {
			throw new IOException("null zip file name in Unzip.unzip");
		}

		// get rid of DOS-like path mangling
		File tfile = new File(filename);
		filename = tfile.getCanonicalPath();

		ZipFile zipFile = new ZipFile(filename);

		Vector<String> unzippedFiles = new Vector<String>(100);

		Enumeration<?> entries = zipFile.entries();

		// create a cache for dirs already made

		Vector<String> dirsMade = new Vector<String>(25);

		while (entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			writeEntry(zipFile, entry, dirsMade, unzippedFiles);
		}
		return unzippedFiles;
	}

	// write an entry to a zip file
	private static void writeEntry(ZipFile zipFile, ZipEntry entry,
			Vector<String> dirsMade, Vector<String> unzippedFiles)
			throws IOException {
		if ((zipFile == null) || (entry == null) || (dirsMade == null)
				|| (unzippedFiles == null)) {
			return;
		}

		String ename = entry.getName();
		if (ename == null) {
			return;
		}

		// if dir ignore--the relevants dirs will be created when the files are
		// created

		if (ename.endsWith("/")) {
			return;
		}

		// no absolute paths
		if (ename.startsWith("/")) {
			ename = ename.substring(1);
		}

		// get basedir, where zip file lives
		String baseDir = getLeadingDir(zipFile.getName());

		// get subdir (if any) for entry

		String subDir = getLeadingDir(ename);
		if (subDir != null) {
			if (!dirsMade.contains(subDir)) { // must create the dir
				File d = new File(baseDir, subDir);

				// If it already exists as a dir, don't do anything
				if (!(d.exists() && d.isDirectory())) {
					// Try to create the directory, warn if it fails
					if (!d.mkdirs()) {
						log.warning("Warning: unable to mkdir " + subDir
								+ " [writeEntry in Zip]");
					}
					dirsMade.add(subDir);
				}
			} // create dir

			// ready to create

			String fullname = baseDir + "/" + ename;
			FileOutputStream os = new FileOutputStream(fullname);
			unzippedFiles.add(fixSeparator(fullname));

			InputStream is = zipFile.getInputStream(entry);
			copyInputStream(is, os);
		}

	}

	// fix the separator string
	private static String fixSeparator(String s) {
		if (s == null) {
			return null;
		}

		if (File.separatorChar == '/') {
			return s.replace('\\', File.separatorChar);
		} else {
			return s.replace('/', File.separatorChar);
		}
	}

	// get the leading directory
	private static String getLeadingDir(String filename) {
		if (filename == null) {
			return null;
		}

		filename = filename.replace('\\', '/');

		int index = filename.lastIndexOf('/');
		if (index < 1) {
			return null;
		}

		return filename.substring(0, index);

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

}
