package cnuphys.bCNU.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Jar {

    private static final int MAXCHUNK = 512; /*
					      * number of bytes to grab at a
					      * time
					      */

    /**
     * Obtain bytes, presumably over the network, and write them to a file. This
     * can be used to get entire files from the jar. The path is probably a path
     * in a jar file such as "data/medres.maps". The bytes will be written to a
     * (local) temp file, and the name of the temp file will be returned. The
     * temp file is set to disappear when the JVM that created it shuts down--
     * so it truly is a temp file.
     * 
     * @param cl A class loader--probably this.getClass().getClassLoader of the
     *            calling object.
     * @param file the file.
     * @param entry The path, probably in a jar file that is in the class path.
     * @return The number of bytes written
     */

    public static int writeResourceToFile(File file, String entry) {

	if ((file == null) || (entry == null)) {
	    return 0;
	}

	InputStream is = Jar.class.getClassLoader().getResourceAsStream(entry);

	if (is == null) {
	    return 0;
	}

	try {

	    if (file.exists()) {
		file.delete();
	    }

	    FileOutputStream fos = new FileOutputStream(file);
	    byte b[] = new byte[MAXCHUNK];

	    boolean done = false;

	    int totalRead = 0;

	    while (!done) {
		int numRead = is.read(b);

		done = (numRead == -1);
		if (!done) {
		    fos.write(b, 0, numRead);
		    totalRead += numRead;
		}

	    }

	    fos.close();
	    return totalRead;
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return 0;
    }

    /**
     * Obtain an vector of the jar file entries.
     * 
     * @param jarPath the full path for the jar file.
     * @return an vector of the jar file entries.
     */
    public static Vector<String> getEntries(String jarPath) {
	if (jarPath == null) {
	    return null;
	}

	Vector<String> v = null;

	try {
	    JarFile jarFile = new JarFile(jarPath);
	    Enumeration<JarEntry> enum1 = jarFile.entries();

	    while (enum1.hasMoreElements()) {
		JarEntry entry = (enum1.nextElement());
		String name = entry.getName();

		if (v == null) {
		    v = new Vector<String>(25);
		}

		v.add(name);
	    } // while

	    jarFile.close();

	} catch (IOException e) {
	    e.printStackTrace();
	}

	return v;
    }

}