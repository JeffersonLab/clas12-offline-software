package cnuphys.bCNU.util;

public class AddressSupport {

	/**
	 * converts from a long to an ip string
	 * 
	 * @param i
	 *            the ipaddress as a long
	 * @return the ip address as a string
	 */
	public static String longToIp(long i) {
		return ((i >> 24) & 0xFF) + "." + ((i >> 16) & 0xFF) + "."
				+ ((i >> 8) & 0xFF) + "." + (i & 0xFF);
	}

	/**
	 * converts from a String to a long
	 * 
	 * @param addr
	 *            the address as a string e.g., 123.24.55.34
	 * @return the address as an int
	 */
	public static long ipToLong(String addr) {
		String[] addrArray = addr.split("\\.");

		long num = 0;
		for (int i = 0; i < addrArray.length; i++) {
			int power = 3 - i;

			try {
				num += ((Integer.parseInt(addrArray[i]) % 256 * Math.pow(256,
						power)));
			} catch (NumberFormatException e) {
				return 0;
			}
		}
		return num;
	}

	/**
	 * Convert mac address of the form 00:c0:4f:a3:58:23 or 00-c0-4f-a3-58-23 to
	 * a long
	 * 
	 * @param s
	 *            the mac address
	 * @return the long
	 */
	public static long macAddressToLong(String s) {
		s = s.trim();

		// either a colon or a hyphen delimiter
		String delimiter;
		if (s.indexOf(":") > -1) {
			delimiter = ":";
		} else {
			delimiter = "-";
			;
		}

		String tokens[] = FileUtilities.tokens(s, delimiter);
		if ((tokens == null) || (tokens.length != 6)) {
			System.err.println("WARNING ill formed macaddress: " + s);
			return 0;
		}

		long lval[] = new long[6];
		for (int i = 0; i < 6; i++) {
			lval[i] = Long.parseLong(tokens[i], 16);
		}

		return (lval[0] << 40) + (lval[1] << 32) + (lval[2] << 24)
				+ (lval[3] << 16) + (lval[4] << 8) + lval[5];
	}

	/**
	 * Convert mac address from a long to the form 00:c0:4f:a3:58:23
	 * 
	 * @param maddr
	 *            the mac address as a long
	 * @return the mac address as a String
	 */
	public static String longToMacAddress(long maddr) {
		long lval[] = new long[6];
		for (int i = 5; i >= 0; i--) {
			lval[i] = maddr & 0xFF;
			maddr = maddr >> 8;
		}
		return String.format("%02x:%02x:%02x:%02x:%02x:%02x", lval[0], lval[1],
				lval[2], lval[3], lval[4], lval[5]);
	}

}
