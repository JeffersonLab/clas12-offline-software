package cnuphys.ced.swimtest;

public abstract class SingleRun {

	//time for all iterations in nanosec (approx)
	private long time;
	
	//number of iterations used
	private int iterations;
	
	protected String _shortDescription; 
	
	
	public SingleRun() {
		this("???");
	}
	
	public SingleRun(String shortDescription) {
		_shortDescription = shortDescription;
	}
	
	public String getShortDescription() {
		return _shortDescription;
	}

	//called just befor the iteration loop
	protected abstract void readyRun();

		
	/**
	 * A single iteration run test
	 * @param index the index of the iteration 0..NumIteration-1
	 */
	protected abstract void run(int index);
	
	
	/**
	 * Get the time of the last test (assuming it was times) in seconds
	 * @return the time of the test in seconds
	 */
	public String timeInSeconds() {
		double timeSec = (time)/1.0e9;
		return String.format("Approximate test time: %12.6f s", timeSec);
	}
	
	/**
	 * Get the time of the last test (assuming it was times) in milliseconds
	 * @return the time of the test in milliseconds
	 */
	public String timeInMilliSeconds() {
		double timeMilliSec = (time)/1.0e6;
		return String.format("Approximate test time: %12.6f ms", timeMilliSec);
	}
	
	/**
	 * Set the number of iterations used in the last test
	 * @param iterations the number of iterations used in the last test
	 */
	protected void setIterarions(int iterations) {
		this.iterations = iterations;
	}
	
	/**
	 * Get the number of iterations used in the last test
	 * @return the number of iterations used in the last test
	 */
	public int getIterations() {
		return iterations;
	}
	
	/**
	 * Set the time in approx nanosec for the last test
	 * @param time the approx time in nanosec
	 */
	public void setTime(long time) {
		this.time = time;
	}

}
