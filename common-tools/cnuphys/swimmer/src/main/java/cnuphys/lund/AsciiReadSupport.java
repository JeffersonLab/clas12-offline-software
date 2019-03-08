package cnuphys.lund;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.zip.ZipInputStream;

/**
 * Support for simpe ASCII reads
 * 
 * @author heddle
 *
 */
public class AsciiReadSupport {

	// Comment lines are blank lines or lines whose first non white
	// space character is a "!"
	protected static final String commentChar = "!";

	/**
	 * Get the next non comment line
	 * 
	 * @param bufferedReader a buffered reader which should be linked to an ascii
	 *                       file
	 * @return the next non comment line (or <code>null</code>)
	 */
	public static String nextNonComment(BufferedReader bufferedReader) {
		String s = null;
		try {
			s = bufferedReader.readLine();
			if (s != null) {
				s = s.trim();
			}
			while ((s != null) && (s.startsWith(commentChar) || s.length() < 1)) {
				s = bufferedReader.readLine();
				if (s != null) {
					s = s.trim();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return s;
	}

	/**
	 * Counts the number of non-comment lines in a file
	 * 
	 * @param file the file in question
	 * @return the number of non-comment lines in the file
	 */
	public static int countNonCommentLines(File file) {

		int count = 0;
		try {

			BufferedReader bufferedReader;

			if (file.getPath().endsWith(".zip")) {
				FileInputStream fis = new FileInputStream(file);
				ZipInputStream zis = new ZipInputStream(fis);
				zis.getNextEntry();
				InputStreamReader isr = new InputStreamReader(zis);
				bufferedReader = new BufferedReader(isr);
			} else {
				bufferedReader = new BufferedReader(new FileReader(file));
			}

			String s = nextNonComment(bufferedReader);

			while (s != null) {
				count++;
				s = nextNonComment(bufferedReader);
			}

			bufferedReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return count;
	}

	/**
	 * Read an entire ascii file into a single string.
	 * 
	 * @param file the file to read
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
	 * Count the lines in an ASCII file, without skipping comment
	 * 
	 * @param file the file in question
	 * @return the number of lines in the file
	 */
	public static int countLines(File file) {
		int count = 0;
		FileReader fileReader;
		try {

			BufferedReader bufferedReader;

			if (file.getPath().endsWith(".zip")) {
				FileInputStream fis = new FileInputStream(file);
				ZipInputStream zis = new ZipInputStream(fis);
				zis.getNextEntry();
				InputStreamReader isr = new InputStreamReader(zis);
				bufferedReader = new BufferedReader(isr);
			} else {
				bufferedReader = new BufferedReader(new FileReader(file));
			}

			boolean reading = true;
			while (reading) {
				String line = bufferedReader.readLine();
				if (line == null) {
					reading = false;
				} else {
					count++;
				}
			}

			bufferedReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return count;
	}

	/**
	 * Get the next tokens from a buffered reader
	 * 
	 * @param bufferedReader a buffered reader which should be linked to an ascii
	 *                       file
	 * @return the next set of white-space separated tokens.
	 */
	public static String[] nextTokens(BufferedReader bufferedReader) {
		String line = nextNonComment(bufferedReader);
		return tokens(line);
	}

	/**
	 * This method breaks a string into an array of tokens.
	 * 
	 * @param str       the string to decompose.
	 * @param delimiter the delimiter
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
	 * @param str the string to decompose.
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
	 * Skip lines in an ascii file
	 * 
	 * @param n              the number of lines to skip
	 * @param bufferedReader a buffered reader which should be linked to an ascii
	 *                       file
	 */
	public static void skipLines(int n, BufferedReader bufferedReader) {
		for (int i = 0; i < n; i++) {
			nextNonComment(bufferedReader);
		}
	}

}