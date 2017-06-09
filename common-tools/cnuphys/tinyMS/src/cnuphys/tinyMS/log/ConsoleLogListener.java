package cnuphys.tinyMS.log;

public class ConsoleLogListener implements ILogListener {

	@Override
	public void error(String message) {
		System.err.println("[LOG ERROR] " + message);
	}

	@Override
	public void config(String message) {
		System.out.println("[LOG CONFIG] " + message);
	}

	@Override
	public void warning(String message) {
		System.err.println("[LOG WARNING] " + message);
	}

	@Override
	public void info(String message) {
		System.out.println("[LOG INFO] " + message);
	}

	@Override
	public void exception(String t) {
		System.err.println("[LOG EXCEPTION] " + t);
	}

}
