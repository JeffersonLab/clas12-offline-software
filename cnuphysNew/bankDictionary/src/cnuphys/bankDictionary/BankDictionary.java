package cnuphys.bankDictionary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import org.xml.sax.SAXException;

/**
 * Create a dictionary from the bankdef XML files. This is an alternative to a
 * evio dictionary.
 * 
 * @author heddle
 * 
 */
public class BankDictionary extends Hashtable<String, DictionaryEntry> {

    /**
     * Create a dictionary from a user-supplied jar file of xml files
     * 
     * @param jarFile
     *            the jar file
     * @throws IOException
     */
    public void buildDictionary(File jarFile) throws IOException {
	scanJarFile(jarFile);
    }

    // jar file of bank def xml files
    private static final String _bankDefJar = "bankdefs.jar";

    /**
     * Build the dictionary from the jar file of xml files. If it completes
     * without throwing an exception, you can then use the dictionary. It looks
     * in reasonable places for the jar file.
     * 
     * @throws IOException
     *             upon failure to read the xml
     */
    public void buildDictionary() throws IOException {
	// look for the jar file
	File jarFile = findBankdefJar();
	if (jarFile == null) {
	    throw new FileNotFoundException("Could not find bankdefs.jar");
	}

	scanJarFile(jarFile);
    }

    // scan the jar file
    private void scanJarFile(File jarFile) throws IOException {
	// also throws IO exception. If successful, we get a collection
	// of temporary files extracted from the jar.
	Vector<File> files = FileUtilities.unzip(jarFile.getPath());

	// ready to parse the xml and build the dictionary
	if (!files.isEmpty()) {
	    for (File file : files) {
		try {
		    Parser.parse(this, file);
		} catch (SAXException e) {
		    e.printStackTrace();
		}
	    }
	}
    }

    // look for the jar file bankdefs.jar
    private File findBankdefJar() {
	String cwd = System.getProperty("user.dir");

	File file = null;

	File search = new File(cwd);
	search = search.getParentFile();
	System.err.println("first try for clasJlib: " + search.getPath());
	File clasJlibDir = FileUtilities.findDirectory(search.getPath(),
		"clasJlib", 3);

	if (clasJlibDir == null) {
	    search = search.getParentFile().getParentFile();
	    System.err.println("second try for clasJlib: " + search.getPath());
	    clasJlibDir = FileUtilities.findDirectory(search.getPath(),
		    "clasJlib", 3);
	}
	if ((clasJlibDir == null) || !clasJlibDir.exists()) {
	    System.out
		    .println("Error: Could not find clasJlib in two locations");
	} else {
	    file = FileUtilities
		    .findFile(clasJlibDir.getPath(), _bankDefJar, 4);
	    if (file != null) {
		System.out.println("found bankdef jar at: " + file.getPath());
	    }
	}

	// not found, try home dir
	if (file == null) {
	    file = FileUtilities.findFile(null, _bankDefJar, 4);
	    if (file != null) {
		System.out.println("found bankdef jar at: " + file.getPath());
	    }
	}

	// still not found try cwd
	if (file == null) {
	    String upOne = (new File(cwd)).getParent();
	    file = FileUtilities.findFile(upOne, _bankDefJar, 4);
	    if (file != null) {
		System.out.println("found bankdef jar at: " + file.getPath());
	    }
	}

	return file;
    }

    /**
     * Get the dictionary entry for the given tag and num
     * 
     * @param tag
     *            the bank tag
     * @param num
     *            the column num
     * @return the dictionary entry, or <code>null</code> if not found.
     */
    public DictionaryEntry getEntry(int tag, int num) {
	return this.get(DictionaryEntry.hashKey(tag, num));
    }

    // main program for testing
    public static void main(String arg[]) {
	BankDictionary bd = new BankDictionary();
	try {
	    bd.buildDictionary();
	} catch (IOException e) {
	    e.printStackTrace();
	}

	DictionaryEntry entry = bd.getEntry(400, 5);
	System.out.println("Tag: " + entry.getTag());
	System.out.println("Bank name: " + entry.getBankName());
	System.out.println("Bank info: " + entry.getBankInfo());
	System.out.println("Num: " + entry.getNum());
	System.out.println("Column name: " + entry.getColumnName());
	System.out.println("Data type: " + entry.getType());
	System.out.println("Column info: " + entry.getColumnInfo());
	System.out.println("--------------------");

	entry = bd.getEntry(400, 0);
	System.out.println("Tag: " + entry.getTag());
	System.out.println("Bank name: " + entry.getBankName());
	System.out.println("Bank info: " + entry.getBankInfo());
	System.out.println("Description: " + entry.getDescription());

    }
}
