package cnuphys.bCNU.fortune;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

import cnuphys.bCNU.util.FileUtilities;

/**
 * Unix fortunes
 * @author heddle
 *
 */
public class FortuneManager {
	
	private static final String FFNAME = "data/fortunes";
	
	private static final String _noFortune = "I got nothing.";
	
	//the singleton
	private static FortuneManager _instance;
	
	private static Random _random;
	
	private FortuneDialog _dialog;
	
	private FortuneManager() {

	}
	
	public void showDialog() {
		if (_dialog == null) {
			_dialog = new FortuneDialog();
		}
		_dialog.setVisible(true);
	}
	
	public String getFortune() {
		BufferedReader bufferedReader = bufferedReaderFromFile();
		if (bufferedReader == null) {
			bufferedReader = bufferedReaderFromResource();
		}

		if (bufferedReader != null) {
		
			StringBuffer sb = new StringBuffer(362000);
			int linecount = 0;
			String line = null;
			do {
				try {
					line = bufferedReader.readLine();
					if (line != null) {
						linecount++;
						sb.append(line + "\n");
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			} while (line != null);
			
			try {
				bufferedReader.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			
//			System.out.println("read " + linecount + " lines");
			
			if (linecount > 100) {
				String fullStr = sb.toString();
//				System.out.println("full string size " + fullStr.length());

				String cookies[] = FileUtilities.tokens(fullStr, "%");
				// System.out.println("fortune count: " + cookies.length);

				if (cookies.length > 1) {
					if (_random == null) {
						_random = new Random();
					}

					int rint = _random.nextInt(cookies.length);
					return cookies[rint].trim();
				}
				
//				for (int i = 0; i < 10; i++) {
//					System.out.println("[" + cookies[i].trim() + "]");
//				}
			}
			
		}
		
		return _noFortune;
	}
	
	//try from a local file
	private BufferedReader bufferedReaderFromFile() {
		File file = new File(FFNAME);
		BufferedReader br = null;

		if (file.exists()) {
			try {
				FileReader fileReader = new FileReader(file);
				br = new BufferedReader(fileReader);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return br;
	}
	
	//try from the jar
	private BufferedReader bufferedReaderFromResource() {
		InputStream inStream = getClass().getClassLoader().getResourceAsStream(
				FFNAME);
		return new BufferedReader(new InputStreamReader(inStream));
	}


	/**
	 * Access to the singleton
	 * @return the singleton FortuneManager
	 */
	public static FortuneManager getInstance() {
		if (_instance == null) {
			_instance = new FortuneManager();
		}
		
		return _instance;
	}
	
	
	public static void main(String arg[]) {
		FortuneManager fm = getInstance();
		System.out.println(fm.getFortune());
	}
}
