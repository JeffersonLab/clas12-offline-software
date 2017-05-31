package cnuphys.bCNU.graphics.component;

import cnuphys.bCNU.log.Log;

public final class CachedText {

	// which standard stream
	public static final int STDOUT = 0;
	public static final int STDERR = 1;
	
	public Log log = Log.getInstance();

	public final String text;

	public final int stream;

	public CachedText(String str, int opt) {
		text = str;
		stream = Math.max(STDOUT, Math.min(STDERR, opt));
	}

	public void write() {
		switch (stream) {
		case STDOUT:
			System.out.print(text);
			log.info(text);
			break;

		case STDERR:
			System.err.print(text);
			log.warning(text);
			break;
		}
	}
}
