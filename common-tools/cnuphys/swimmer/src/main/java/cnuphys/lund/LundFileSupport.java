package cnuphys.lund;

import java.io.File;
import java.io.FileNotFoundException;

public class LundFileSupport {

	private static LundFileSupport instance;

	private LundFileSupport() {
	}

	public static LundFileSupport getInstance() {
		if (instance == null) {
			instance = new LundFileSupport();
		}
		return instance;
	}

	public int countEvents(File file) {

		try {
			Counter counter = new Counter(file);
			int count = counter.count();
			return count;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return 0;
		}
	}

	class Counter extends AsciiReader {

		private int skipCount = 0;

		public Counter(File file) throws FileNotFoundException {
			super(file);
		}

		@Override
		protected void processLine(String line) {
			String tokens[] = AsciiReadSupport.tokens(line);

			// System.err.println("[" + count() + "] [" + line + "]");

			if (skipCount == 0) {
				int numPart = Integer.parseInt(tokens[0]);
				skipCount = numPart;
				lcount++;
			} else {
				skipCount--;
			}

		}

		@Override
		public void done() {
			System.err.println("DONE count = " + count());
		}

		public int count() {
			return lcount;
		}

	}

}
