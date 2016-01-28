package cnuphys.bCNU.eliza;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.StringTokenizer;
import java.util.Vector;

import cnuphys.bCNU.util.Environment;

/**
 * This is used to create exceptions to
 * 
 * @author heddle
 * 
 */
public class ElizaOverride implements Serializable {

	private static String overrideFile = Environment.getInstance()
			.getHomeDirectory()
			+ "/git/cnuphys/bCNU/src/main/resources/data/elizaSub";

	private static Vector<ElizaOverride> _overRides;

	static {
		loadOverrides();
	}

	protected static final int MATCH_ANY = 0;
	protected static final int MATCH_ALL = 1;
	protected static final int MATCH_PHRASE = 2;

	private int _type;
	private String _response;

	private String[] _strings;

	public ElizaOverride(int type, String response, String... strings) {
		_type = type;
		_response = response;
		_strings = strings;

		if (_strings != null) {
			for (int i = 0; i < _strings.length; i++) {
				_strings[i] = _strings[i].toLowerCase();
			}
		}
	}

	private static void loadOverrides() {
		// first try from file
		try {
			_overRides = (Vector<ElizaOverride>) serialRead(overrideFile);
			if (_overRides != null) {
				System.err.println("Read " + _overRides.size()
						+ " eliza overrides");
			}
		} catch (Exception e) {
			_overRides = null;
		}
	}

	public static String getResponse(String phrase) {
		if ((_overRides == null) || (_overRides.size() < 1)) {
			return null;
		}

		if (phrase == null) {
			return null;
		}

		phrase = phrase.toLowerCase();
		String tokens[] = tokens(phrase);
		if ((tokens == null) || (tokens.length < 1)) {
			return null;
		}

		for (int i = 0; i < tokens.length; i++) {
			String tok = tokens[i];
			int len = tok.length();
			if (len > 1) {
				char lastChar = tok.charAt(len - 1);
				if (!Character.isLetter(lastChar)) {
					tokens[i] = tok.substring(0, len - 1);
					// System.err.println("NEW tok: [" + tokens[i] + "]");
				}
			}
		}

		for (ElizaOverride eov : _overRides) {
			String response = eov.response(tokens);
			if (response != null) {
				return response;
			}
		}

		return null;
	}

	/**
	 * This method breaks a string into an array of tokens.
	 * 
	 * @param str
	 *            the string to decompose.
	 * @return an array of tokens
	 */

	private static String[] tokens(String str) {

		StringTokenizer t = new StringTokenizer(str);
		int num = t.countTokens();
		String lines[] = new String[num];

		for (int i = 0; i < num; i++) {
			lines[i] = t.nextToken();
		}

		return lines;
	}

	/**
	 * Generate a response to override the eliza response
	 * 
	 * @param tokens
	 *            the cleaned up input tokens
	 * @return the substitute response. If null, let Eliza generate a response.
	 */
	private String response(String[] tokens) {

		if ((_strings == null) || (_strings.length < 1)) {
			return null;
		}

		switch (_type) {
		case MATCH_ANY:
			for (String match : _strings) {
				for (String tok : tokens) {
					if (match.equals(tok)) {
						return _response;
					}
				}
			}
			return null;

		case MATCH_ALL:
			for (String match : _strings) {

				boolean foundMatch = false;
				for (String tok : tokens) {
					if (match.equals(tok)) {
						foundMatch = true;
						break;
					}
				}

				if (!foundMatch) {
					return null;
				}
			}
			return _response;

		case MATCH_PHRASE:

			if (_strings.length == tokens.length) {
				for (int i = 0; i < _strings.length; i++) {
					if (!_strings[i].equals(tokens[i])) {
						return null;
					}
				}
				return _response;
			}

			return null;

		} // end switch

		return null;
	}

	private static Object serialRead(ObjectInputStream s) {

		Object obj = null;
		try {
			obj = s.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				s.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return obj;
	}

	/**
	 * Reads a serializable object from a file.
	 * 
	 * @param fullfn
	 *            the full path.
	 * @return the deserialized object.
	 */
	private static Object serialRead(String fullfn) {

		FileInputStream f = null;
		ObjectInput s = null;
		Object obj = null;

		try {
			obj = serialRead(new ObjectInputStream(new FileInputStream(fullfn)));
		} catch (FileNotFoundException e1) {
			// e1.printStackTrace();
		} catch (IOException e1) {
			// e1.printStackTrace();
		}

		// from jar?
		if (obj == null) {
			try {
				InputStream inStream = ClassLoader.getSystemClassLoader()
						.getResourceAsStream(overrideFile);
				obj = serialRead(new ObjectInputStream(inStream));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return obj;

	}

	/**
	 * serialWrite writes out a serializable object to a file.
	 * 
	 * @param obj
	 *            the serializable object.
	 * 
	 * @param fullfn
	 *            the full path.
	 */
	public static void serialWrite(Serializable obj, String fullfn) {

		FileOutputStream f = null;

		File file = new File(fullfn);
		if (file.exists()) {
			if (file.canWrite()) {
				System.err.println("deleting");
				file.delete();
			}
		}

		ObjectOutput s = null;

		try {
			f = new FileOutputStream(fullfn);
			s = new ObjectOutputStream(f);
			s.writeObject(obj);
			s.flush();
		}

		catch (Exception e) {
			e.printStackTrace();
		}

		finally {

			if (f != null) {
				try {
					f.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (s != null) {

				try {
					s.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String arg[]) {
		if (_overRides == null) {
			_overRides = new Vector<ElizaOverride>(10, 10);
		}

		_overRides
				.add(new ElizaOverride(
						MATCH_ALL,
						"It must be operator error. ced never crashes. But report it to heddle@jlab.org. He will most likely mock you.",
						"ced", "crashes"));

		_overRides
				.add(new ElizaOverride(
						MATCH_ALL,
						"It must be operator error. ced never crashes. But report it to heddle@jlab.org. He will most likely mock you.",
						"ced", "sucks"));

		_overRides
				.add(new ElizaOverride(
						MATCH_ALL,
						"It must be operator error. ced never crashes. But report it to heddle@jlab.org. He will most likely mock you.",
						"ced", "crashed"));

		_overRides.add(new ElizaOverride(MATCH_PHRASE, "Bite me!", "you're",
				"annoying"));

		_overRides.add(new ElizaOverride(MATCH_PHRASE, "Bite me!", "you",
				"are", "annoying"));

		_overRides.add(new ElizaOverride(MATCH_PHRASE,
				"I'm sick and tired of waking up sick and tired.", "how",
				"are", "you"));

		_overRides.add(new ElizaOverride(MATCH_PHRASE,
				"You're talking about the CLAS software meeting, right?", "go",
				"to", "hell"));

		_overRides.add(new ElizaOverride(MATCH_ANY,
				"Did you say \"tracking\"? Do I look like Veronique?",
				"tracking"));

		_overRides.add(new ElizaOverride(MATCH_ANY,
				"Did you say \"geometry\"? Do I look like Gagik?", "geometry"));

		_overRides.add(new ElizaOverride(MATCH_ANY,
				"Did you say \"clara\"? Do I look like Vardan?", "geometry"));

		_overRides.add(new ElizaOverride(MATCH_ANY,
				"Did you say \"simulation\"? Do I look like Mauri?",
				"simulation"));

		_overRides.add(new ElizaOverride(MATCH_ANY,
				"Are you talking about the SVT? Do I look like Yuri?", "bst",
				"svt"));

		_overRides
				.add(new ElizaOverride(
						MATCH_ANY,
						"All I know about CNU is that it has a great physics department",
						"cnu"));

		_overRides.add(new ElizaOverride(MATCH_ANY,
				"Did you say \"geometry\"? Do I look like Gagik?", "geometry"));

		_overRides.add(new ElizaOverride(MATCH_ALL,
				"Talk to Mac about the DCs", "Drift", "Chamber"));

		_overRides.add(new ElizaOverride(MATCH_ALL,
				"Talk to Mac about the DCs", "Drift", "Chambers"));

		_overRides.add(new ElizaOverride(MATCH_ALL,
				"I would call Dan to discuss FTOF", "FTOF"));

		_overRides.add(new ElizaOverride(MATCH_PHRASE,
				"I will now execute \"sudo rm -fr ~\\*\"", "you", "suck"));

		_overRides.add(new ElizaOverride(MATCH_PHRASE,
				"I will now execute \"sudo rm -fr ~\\*\"", "I", "hate", "you"));

		_overRides.add(new ElizaOverride(MATCH_PHRASE,
				" 'Ya' is not a proper word in English.", "ya"));

		_overRides.add(new ElizaOverride(MATCH_ANY,
				"Hey that's not nice. I'm telling Volker!", "shit", "fuck",
				"asshole", "fucker"));

		serialWrite(_overRides, overrideFile);

		System.err.println("done");
	}

}
