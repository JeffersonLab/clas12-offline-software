package cnuphys.magfield.converter;

import java.io.File;
import java.io.FileNotFoundException;

public class ZFile implements Comparable<ZFile> {
	public File file;
	public Double z = Double.NaN;

	public ZFile(File file) {
		this.file = file;
		getZ();
	}

	private void getZ() {
		AsciiReader ar;
		try {
			ar = new AsciiReader(file) {

				@Override
				protected void processLine(String line) {
					String tokens[] = AsciiReadSupport.tokens(line);
					if (tokens.length == 7) {
						z = Double.parseDouble(tokens[2]) / 10; // convert mm to cm
						stop();
					}
				}

				@Override
				public void done() {
				}

			};
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int compareTo(ZFile o) {
		return z.compareTo(o.z);
	}

}
