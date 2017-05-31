package cnuphys.bCNU.graphics.component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cnuphys.bCNU.util.FileUtilities;

public class IpAddressSupport {

	// private static final String byteStr =
	// "25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?";
	private static final String byteStrPar = "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
	private static final String starOrByte = "(\\*|25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

	public static final String ANY_ADDRESS = "*.*.*.*";

	public static final Pattern BASIC_PATTERN = Pattern.compile("\\b"
			+ byteStrPar + "\\." + byteStrPar + "\\." + byteStrPar + "\\."
			+ byteStrPar + "\\b");

	public static final Pattern SIMPLE_STAR_PATTERN = Pattern
			.compile(starOrByte + "\\." + starOrByte + "\\." + starOrByte
					+ "\\." + starOrByte);

	/**
	 * Checks whether an IP Address is validated by the basic no wildcard
	 * pattern
	 * 
	 * @param ipAddress
	 *            the IP address as a string
	 * @return <code>true</code> if the ipaddress was validated as legal.
	 */
	public static boolean validate(String ipAddress) {
		// checks whether we have a legal address using the basic (no wild card)
		// pattern
		Matcher m = BASIC_PATTERN.matcher(ipAddress);
		return m.matches();
	}

	/**
	 * Checks whether an IP Address matches the simple star wildcard
	 * 
	 * @param ipPattern
	 *            the IP pattern to validate as a string
	 * @return <code>true</code> if the pattern was validated as legal.
	 */
	public static boolean validateSimpleWildcard(String ipPattern) {
		Matcher m = SIMPLE_STAR_PATTERN.matcher(ipPattern);
		return m.matches();
	}

	private static String getRegExpSimpleWildCard(String s) {
		// s will be something like "157.185.*.*" It will have
		// been validated via validateSimpleWildcard
		// now we want to create a regexp so addresses such as
		// 157.185.3.45 match but not 158.185.3.45

		String tokens[] = FileUtilities.tokens(s, ".");

		if ((tokens == null) || (tokens.length != 4)) {
			return null;
		}

		String rexp = getTokStr(tokens[0]) + "\\." + getTokStr(tokens[1])
				+ "\\." + getTokStr(tokens[2]) + "\\." + getTokStr(tokens[3]);

		// System.out.println("rexp: " + rexp);
		return rexp;
	}

	/**
	 * Create a Pattern from a filter string.
	 * 
	 * @param filterString
	 *            something like "157.185.*.*"
	 * @return the pattern that can be used for matching, or <code>null</code>
	 */
	public static Pattern createPattern(String filterString) {
		if (filterString == null) {
			return null;
		}
		if (ANY_ADDRESS.equals(filterString)) {
			return null;
		}

		try {
			String regexp = getRegExpSimpleWildCard(filterString);
			return Pattern.compile(regexp);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	private static String getTokStr(String t) {
		if (t.startsWith("*")) {
			return byteStrPar;
		}
		return "(" + t + ")";
	}

	/**
	 * Main program for testing
	 * 
	 * @param arg
	 *            command arguments ignored
	 */
	public static void main(String arg[]) {
		String addresses[] = { "157.185.22.32", "1.2.3.4", "267.185.22.32",
				"*.*.*.*", "157.*.*.*", "157.185.*.*" };

		System.out.println("=====================");
		System.out.println("Testing basic pattern");

		for (String s : addresses) {
			boolean validated = validate(s);
			String bres = " " + validated;
			String sout = String.format("%15s %s", s, bres);
			System.out.println(sout);
		}

		System.out.println("\n=====================");
		System.out.println("Testing simple wildcard pattern");

		for (String s : addresses) {
			boolean validated = validateSimpleWildcard(s);
			String bres = " " + validated;
			String sout = String.format("%15s %s", s, bres);
			System.out.println(sout);
		}

		System.out.println("\n=====================");
		System.out.println("Create regexp");

		String s1 = "157.185.*.*";
		System.out.println("String: " + s1 + "    validated: "
				+ validateSimpleWildcard(s1));

		// String regexp = getRegExpSimpleWildCard(s1);
		// System.out.println("Regexp: " + regexp);

		Pattern myPatt = createPattern(s1);

		String testStr[] = { "157.185.5.6", "157.185.300.6", "157.186.5.6" };
		for (String s : testStr) {
			Matcher m = myPatt.matcher(s);
			System.out.println(s + "   Passes: " + m.matches());
		}

	}
}
