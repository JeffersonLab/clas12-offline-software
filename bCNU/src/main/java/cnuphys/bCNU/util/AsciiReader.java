package cnuphys.bCNU.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Simplifies the reading of an ascii file. Assumes comments begin with a "!".
 * This should be extended, and the processLine method overridden.
 * 
 * @author heddle
 *
 */
public class AsciiReader {

    // the number of noncomment lines processed
    private int nonCommentLineCount;

    /**
     * Constructor
     * 
     * @param file
     *            the ascii file to be processed
     * @throws FileNotFoundException
     */
    public AsciiReader(File file) throws FileNotFoundException {
	FileReader fileReader = new FileReader(file);
	final BufferedReader bufferedReader = new BufferedReader(fileReader);

	boolean reading = true;
	while (reading) {
	    String s = AsciiReadSupport.nextNonComment(bufferedReader);
	    if (s != null) {
		nonCommentLineCount++;
		processLine(s);
	    } else {
		reading = false;
	    }
	}
    }

    /**
     * Process one non comment line from the file. This method should be
     * overridden.
     * 
     * @param line
     */
    protected void processLine(String line) {
	System.out.println(line);
    }

    /**
     * @return the nonCommentLineCount
     */
    public int getNonCommentLineCount() {
	return nonCommentLineCount;
    }

}
