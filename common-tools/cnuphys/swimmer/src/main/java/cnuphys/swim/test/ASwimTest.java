package cnuphys.swim.test;

/**
 * Abstract class used for running a test
 * @author heddle
 *
 */
public abstract class ASwimTest {

	/** the name of test */
	protected String name;
	
	/** time of the test approx nanoseconds */
	protected long time;
	
	/**
	 * Create a test
	 * @param name the name of the test
	 */
	public ASwimTest(String name) {
		setName(name);
	}
	
	/**
	 * Set the name of the test
	 * @param name the name of the test
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Get the name of the test
	 * @return the name of the test
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Time the test
	 */
	public long timedRun() {
		time = System.nanoTime();
		run();
		time = System.nanoTime()-time;
		return time;
	}
	
	/**
	 * Run the test
	 */
	public abstract void run();
	
	/**
	 * Get a description of the test
	 * @return a description of the test
	 */
	public abstract String getDescription();
	
	/**
	 * Provide a String that summarizes the results
	 * @return a String that summarizes the results
	 */
	public abstract String getResults();
	
	/**
	 * Get the time of the test (assuming it was times) in seconds
	 * @return the time of the test in seconds
	 */
	public String timeInSeconds() {
		double timeSec = ((double)time)/1.0e9;
		return String.format("Approximate test time: %12.6f s", timeSec);
	}
	
	/**
	 * Get the time of the test (assuming it was times) in milliseconds
	 * @return the time of the test in milliseconds
	 */
	public String timeInMilliSeconds() {
		double timeMilliSec = ((double)time)/1.0e6;
		return String.format("Approximate test time: %12.6f ms", timeMilliSec);
	}
	
	@Override
	public String toString() {
		return getName();
	}

}
