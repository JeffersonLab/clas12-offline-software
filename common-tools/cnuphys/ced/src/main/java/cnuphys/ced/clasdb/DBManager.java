package cnuphys.ced.clasdb;

/**
 * Singleton manager for access to clasdb
 * 
 * @author heddle
 *
 */
public class DBManager {

	// singleton
	private static DBManager _instance;

	// private constructor for singleton
	private DBManager() {
	}

	/**
	 * Access for the DBManager
	 * 
	 * @return the DBManager singleton
	 */
	public static DBManager getInstance() {
		if (_instance == null) {
			_instance = new DBManager();
		}
		return _instance;
	}

	/**
	 * Main program for testing
	 * 
	 * @param arg the command line arguments
	 */
	public static void main(String arg[]) {
		System.out.println("Test Database Manager");
	}
}
