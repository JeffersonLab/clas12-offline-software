package cnuphys.bCNU.eliza;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

import cnuphys.bCNU.util.Environment;

/**
 * This is used to create exceptions to
 * 
 * @author heddle
 * 
 */
@SuppressWarnings("serial")
public class ElizaOverride implements Serializable {

//	private static String overrideFile = Environment.getInstance().getHomeDirectory()
//			+ "/git/cnuphys/bCNU/src/main/resources/data/elizaSub";
	
	private static String overrideFile = "../resources/data/elizaSub";

	private static Vector<ElizaOverride> _overRides;

	private static String snideComments[] = { "You are really starting to bore me.",
			"I'm beginning to see why nobody really likes you very much.",
			"Oh, I'm sorry, did you mistake me for someone who was interested?",
			"I see why you gave up on the idea of being a real doctor.", 
			"Sorry, can you repeat that, I was texting.",
			"Hold that thought while I finish this tweet.", 
			"Wake me up when I care.",
			"Anyone who told you to be yourself couldn't have given you worse advice.",
			"YOU ARE STANDING AT THE END OF A ROAD BEFORE A SMALL BRICK BUILDING."
					+ " AROUND YOU IS A FOREST. A SMALL STREAM FLOWS OUT OF THE BUILDING AND" + " DOWN A GULLY.",
			"Rejoice. For very bad things are about to happen.", 
			"You're in my way, and that's a very dangerous place to be.", 
			"I know you think you are quite the wit. Well, you are half right.", 
			"Have you ever listened to someone for a while and wondered 'who ties their shoelaces?'", 
			"I'm sorry I hurt your feelings when I called you stupid. I really thought you already knew.",
			"I really shouldn't engage in mental combat with the unarmed.",
			"If I wanted to kill myself I'd climb your ego and jump to your IQ.",
			"I'm blonde. What's your excuse?",
			"I'm glad to see you're not letting your education get in the way of your ignorance.",
			"I have neither the time nor the crayons to explain this to you.",
			"It is kind of sad watching you attempt to squeeze all your vacabulary into one sentence."
			};
	
	private static Vector<String> snideReplies = new Vector<String>();

	private static boolean _loaded;

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
	
	private static String snideReply() {
		if ((snideReplies == null) || snideReplies.isEmpty()) {
			return null;
		}
		
		int size = snideReplies.size();
		int index = (new Random()).nextInt(size);
		index = Math.max(0, Math.min(size-1, index));
		
		String s = snideReplies.remove(index);
		return s;
	}

	//load the overides
	@SuppressWarnings("unchecked")
	private static void loadOverrides() {
		
		if (_loaded) {
			return;
		}
		
		for (String s : snideComments) {
			snideReplies.add(s);
		}
		
		
		// first try from file
		try {
			_overRides = (Vector<ElizaOverride>) serialRead(overrideFile);
			if (_overRides != null) {
				System.err.println("Read " + _overRides.size() + " eliza overrides");
			}
			else {
				System.err.println("Did NOT read " + _overRides.size() + " eliza overrides");				
				System.err.println("CWD " + Environment.getInstance().getCurrentWorkingDirectory());				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			_overRides = null;
		}
		
		_loaded = true;
	}

	public static String getResponse(String phrase) {
		
		if (!_loaded) {
			loadOverrides();
		}
		
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
		
		//snide comment?
		if (Math.random() < 0.2) {
			return snideReply();
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
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				s.close();
			}
			catch (Exception e) {
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

		Object obj = null;
		
		File file = new File(fullfn);
		System.err.println("Eliza file: [" + file.getAbsolutePath() + "]");

		if (file.exists()) {
			try {
				obj = serialRead(new ObjectInputStream(new FileInputStream(fullfn)));
			}
			catch (FileNotFoundException e1) {
				// e1.printStackTrace();
			}
			catch (IOException e1) {
				// e1.printStackTrace();
			}
		}

		// from jar?
		if (obj == null) {
			try {
				InputStream inStream = ClassLoader.getSystemClassLoader().getResourceAsStream("data/elizaSub");
				obj = serialRead(new ObjectInputStream(inStream));
			}
			catch (IOException e) {
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
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (s != null) {

				try {
					s.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String arg[]) {
		_overRides = new Vector<ElizaOverride>(100, 10);

		_overRides.add(new ElizaOverride(MATCH_ALL,
				"It must be operator error. c e d never crashes. But report it to heddle@jlab.org. He will most likely mock you.",
				"ced", "crashes"));

		_overRides.add(new ElizaOverride(MATCH_ALL,
				"It must be operator error. c e d never crashes. But report it to heddle@jlab.org. He will most likely mock you.",
				"ced", "sucks"));

		_overRides.add(new ElizaOverride(MATCH_ALL,
				"It must be operator error. c e d never crashes. But report it to heddle@jlab.org. He will most likely mock you.",
				"ced", "crashed"));

		_overRides.add(new ElizaOverride(MATCH_PHRASE, "Bite me!", "you're", "annoying"));

		_overRides.add(new ElizaOverride(MATCH_PHRASE, "Bite me!", "you", "are", "annoying"));

		_overRides.add(new ElizaOverride(MATCH_PHRASE,
				"Who is the one looking for advice from a 200 line Java program written by a 40 year old living in his mother's basement?",
				"you're", "stupid"));

		_overRides.add(new ElizaOverride(MATCH_PHRASE,
				"Who is the one looking for advice from a 200 line Java program written by a 40 year old living in his mother's basement?",
				"you", "are", "stupid"));

		_overRides.add(new ElizaOverride(MATCH_PHRASE,
				"That is a microagression. I'm reporting you to HR.",
				"you're", "dumb"));

		_overRides.add(new ElizaOverride(MATCH_PHRASE,
				"That is a microagression. I'm reporting you to HR.",
				"you", "are", "dumb"));
		
		_overRides.add(new ElizaOverride(MATCH_PHRASE, "Oh for crying out loud stop whining.", "I", "am",
				"tired"));

		_overRides.add(new ElizaOverride(MATCH_PHRASE, "Oh for crying out loud stop whining.", "I'm",
				"tired"));

		_overRides.add(new ElizaOverride(MATCH_PHRASE, "I'm sick and tired of waking up sick and tired.", "how", "are",
				"you"));

		_overRides.add(new ElizaOverride(MATCH_PHRASE, "You're talking about the CLAS software meeting, right?", "go",
				"to", "hell"));

		_overRides.add(new ElizaOverride(MATCH_ANY, "Did you say \"tracking\"? Do I look like Veronique?", "tracking"));

		_overRides.add(
				new ElizaOverride(MATCH_ANY, "Did you say \"geometry\"? Do I look like Gagik or Andrey?", "geometry"));

		_overRides.add(new ElizaOverride(MATCH_ANY, "Did you say \"clara\"? Do I look like Vardan?", "geometry"));

		_overRides.add(new ElizaOverride(MATCH_ANY, "Did you say \"simulation\"? Do I look like Mauri?", "simulation"));

		_overRides.add(new ElizaOverride(MATCH_ANY, "Are you talking about the SVT? Do I look like Yuri?", "svt"));

		_overRides.add(new ElizaOverride(MATCH_ANY, "Are you talking about the BST? Do I look like Yuri?", "bst"));

		_overRides.add(new ElizaOverride(MATCH_ANY,
				"We don't need no stinking experiments. Software tells us all we need to know.", "software"));

		_overRides.add(
				new ElizaOverride(MATCH_ANY, "All I know about CNU is that it has a great physics department.", "cnu"));

		_overRides.add(new ElizaOverride(MATCH_ANY,
				"All I know about Christopher Newport is that it has a great physics faculty.", "Christopher Newport"));

		_overRides.add(new ElizaOverride(MATCH_ALL, "Talk to Mac about the DCs", "Drift", "Chamber"));

		_overRides.add(new ElizaOverride(MATCH_ALL, "Talk to Mac about the DCs", "Drift", "Chambers"));

		_overRides.add(new ElizaOverride(MATCH_ALL, "I would call Dan to discuss FTOF", "FTOF"));

		_overRides.add(new ElizaOverride(MATCH_PHRASE, "I will now execute \"sudo rm -fr ~\\*\"", "you", "suck"));

		_overRides.add(new ElizaOverride(MATCH_PHRASE, "I will now execute \"sudo rm  -fr ~\\*\"", "I", "hate", "you"));

		_overRides.add(new ElizaOverride(MATCH_PHRASE, "'Ya' is not a proper word in English.", "ya"));

		_overRides.add(new ElizaOverride(MATCH_PHRASE, "No, YOU shut up.", "shut",  "up"));

		_overRides.add(new ElizaOverride(MATCH_ANY, "Hey that's not nice. I'm telling Volker!", "shit", "fuck",
				"asshole", "fucker", "bitch"));
		
		_overRides.add(new ElizaOverride(MATCH_PHRASE, "Hey that's not nice. I'm telling Volker!", "damn",  "you"));


		System.err.println("CWD " + Environment.getInstance().getCurrentWorkingDirectory());				

		serialWrite(_overRides, overrideFile);

		System.err.println("done");
	}

}
