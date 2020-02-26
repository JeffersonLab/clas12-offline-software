package cnuphys.ced.swimtest;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

/**
 * Abstract class used for running a test
 * @author heddle
 *
 */
public abstract class ASwimTest {
	
	/** data set via properties */
	protected Properties properties = new Properties();
	
	//random number generator
	protected Random rand;
	protected long seed;
	
	//base properties
	protected static final String ITERATIONS = "P_iterations";
	protected static final String NAME =  "P_name";
	protected static final String SEED = "P_seed";
	
	/** time of the test approx nanoseconds */
	protected long time;
	
	/** A list of single runs. If more than one they will be timed against
	 * each other with the same number of iterations 
	 */
	protected ArrayList<SingleRun> singleRuns = new ArrayList<>();
	
	public ASwimTest() {
		initialize();
		createSingleRuns();
	}
	
		
	//for subclasses to initialize
	protected void initialize() {
		properties.put(NAME, "???");
		properties.put(ITERATIONS, "1");
	}
	
	//create the runs
	protected abstract void createSingleRuns();
	
	/**
	 * Add a single run. All single runs
	 * will be timed for the same number of iterations.
	 * @param sr the single run to add
	 */
	public void addSingleRun(SingleRun sr) {
		singleRuns.add(sr);
	}
	
	/**
	 * Get the name of the test
	 * @return the name of the test
	 */
	public String getName() {
		return properties.getProperty(NAME);
	}
	
	public long getSeed() {
		try {
			return Long.parseLong(properties.getProperty(SEED));
		}
		catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	
	//everything is intialized, about to launch, final prep
	protected abstract void aboutToLaunch();
	

	/**
	 * Launch the test
	 */
	public void launch() {
		System.out.println("Launch starting...");

		SwimTester tester = SwimTester.getInstance();
		properties.setProperty(ITERATIONS, tester.getIterationTF().getText());
		properties.setProperty(SEED, tester.getRandomTF().getText());
		
		//init the random number generator
		//a non positive seed means don't set the seed
		rand = new Random();
		long seed = getSeed();
		if (seed > 0) {
			rand.setSeed(seed);
		}

		int iterations = getIterationCount();
		
		 aboutToLaunch();
		
		for (SingleRun sr : singleRuns) {
			sr.setIterarions(iterations);
			sr.readyRun();
			System.err.println("Starting iterations over: " + sr.getShortDescription());
			
			//run a few times to get classes loaded
			int fewTimes = Math.max(100, getIterationCount());
			for (int i = 0; i < fewTimes; i++) {
				sr.run(i);
			}
			
			time = System.nanoTime();
			for (int i = 0; i < getIterationCount(); i++) {
				sr.run(i);
			}

			sr.setTime(System.nanoTime()-time);
		}

		System.out.println("Launch completed.");
	}
	
	/**
	 * Get the number of iterations
	 * @return the number of iterations
	 */
	public int getIterationCount() {
		try {
			return Integer.parseInt(properties.getProperty(ITERATIONS));
		}
		catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
	}
	
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
	
	
	@Override
	public String toString() {
		return getName();
	}
	
	//get the next random double in a given range
	protected double nextDouble(double minVal, double maxVal) {
		double del = maxVal - minVal;
		return minVal + del*rand.nextDouble();
	}
	
	//get the next random float in a given range
	protected float nextFloat(float minVal, float maxVal) {
		float del = maxVal - minVal;
		return minVal + del*rand.nextFloat();
	}
	
	protected double[] nextRandom3Vect(double minVal1, double maxVal1,
			double minVal2, double maxVal2,
			double minVal3, double maxVal3) {
		double val[] = new double[3];
		nextRandom3Vect(minVal1, maxVal1, minVal2, maxVal2, minVal3, maxVal3, val);
		return val;
	}
	
	protected void nextRandom3Vect(double minVal1, double maxVal1,
			double minVal2, double maxVal2,
			double minVal3, double maxVal3,
			double val[]) {
		
		val[0] = nextDouble(minVal1, maxVal1);
		val[1] = nextDouble(minVal2, maxVal2);
		val[2] = nextDouble(minVal3, maxVal3);
	}
	
	protected float[] nextRandom3Vect(float minVal1, float maxVal1,
			float minVal2, float maxVal2,
			float minVal3, float maxVal3) {
		float val[] = new float[3];
		nextRandom3Vect(minVal1, maxVal1, minVal2, maxVal2, minVal3, maxVal3, val);
		return val;
	}
	
	protected void nextRandom3Vect(float minVal1, float maxVal1,
			float minVal2, float maxVal2,
			float minVal3, float maxVal3,
			float val[]) {
		
		val[0] = nextFloat(minVal1, maxVal1);
		val[1] = nextFloat(minVal2, maxVal2);
		val[2] = nextFloat(minVal3, maxVal3);
	}

	protected double magnitude(float[] v) {
		double x2 = v[0]*v[0];
		double y2 = v[1]*v[1];
		double z2 = v[2]*v[2];
		return Math.sqrt(x2 + y2 + z2);
	}
	
	protected double absoluteMagnitudeDifference(float[] v1, float[] v2) {
		return Math.abs(magnitude(v2) - magnitude(v1));
	}

}
