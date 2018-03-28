package cnuphys.ced.swimtest.tests;

import cnuphys.ced.swimtest.ASwimTest;
import cnuphys.ced.swimtest.SingleRun;
import cnuphys.magfield.MagneticField;
import cnuphys.magfield.MagneticFields;

public class MagFieldMathLibTest extends ASwimTest {
	
	//where field will be evaluated
	private float x[];
	private float y[];
	private float z[];
	
	@Override
	protected void initialize() {
		super.initialize();
		
		properties.setProperty(NAME, "Magnetic Field Math-Lib Test");
	}


	@Override
	protected void createSingleRuns() {
		MySingleRun run1 = new MySingleRun("Standard Library", MagneticField.MathLib.DEFAULT);
		MySingleRun run2 = new MySingleRun("Apache Library", MagneticField.MathLib.FAST);		
		addSingleRun(run1);
		addSingleRun(run2);
	}

	@Override
	protected void aboutToLaunch() {
		int iterations = getIterationCount();
		
		x = new float[iterations];
		y = new float[iterations];
		z = new float[iterations];
		
		for (int i = 0; i < iterations; i++) {
			x[i] = nextFloat(0, 340);
			y[i] = nextFloat(0, 120);
			z[i] = nextFloat(260, 400);
			
//			System.err.println(String.format("(%8.4f, %8.4f, %8.4f)", x[i], y[i], z[i]));
		}

	}

	@Override
	public String getDescription() {
		return "This will test the standard Java math library "
				+ "against the Apache common maths.";
	}

	@Override
	public String getResults() {
		StringBuffer sb = new StringBuffer(1024);
		
		MySingleRun standard = (MySingleRun) singleRuns.get(0);
		MySingleRun apache = (MySingleRun) singleRuns.get(1);

		sb.append(String.format("%s %s: %s\n", standard.lib, standard.getShortDescription(), standard.timeInMilliSeconds()));
		sb.append(String.format("%s %s: %s\n", apache.lib, apache.getShortDescription(), apache.timeInMilliSeconds()));

		
		int maxIndex = -1;
		double maxAbsDiff = Double.NEGATIVE_INFINITY;
		
		for (int i = 0; i < getIterationCount(); i++) {
			double diff = absoluteMagnitudeDifference(standard.results[i].result, apache.results[i].result);
			if (diff > maxAbsDiff) {
				maxAbsDiff = diff;
				maxIndex = i;
			}
		}
		
		if (maxIndex < 0) {
			sb.append("No difference in magnitudes");
		}
		else {
			float sr[] = standard.results[maxIndex].result;
			float ar[] = apache.results[maxIndex].result;
			
			float xx = x[maxIndex];
			float yy = y[maxIndex];
			float zz = z[maxIndex];

			String s = String.format(
					"Index = %d  Max absolute magnitude diff = %8.5e\nat (x,y,z) = " + 
			"(%6.3f, %6.3f, %6.3f)\n" + 
							"Standard: (%6.3f, %6.3f, %6.3f)\n" +
							"  Apache: (%6.3f, %6.3f, %6.3f)\n", 
							maxIndex, maxAbsDiff, xx, yy, zz, sr[0], sr[1], sr[2], ar[0], ar[1], ar[2]);

//		    String s = String.format("Max absolute magnitude diff = %8.5f at (x,y,z) = "
//		    		+ "(%12.5f, %12.5f, %12.5f) \n", 
//		    		maxAbsDiff,
//		    		x[maxIndex], y[maxIndex] + z[maxIndex],
//		    		sr[0], sr[1], sr[2], ar[0], ar[1], ar[2]);
		    
		    sb.append(s);
		}
		
		return sb.toString();
	}
	
	
	class Result {
		public float[] result;
		
		public Result() {
			result = new float[3];
		}
	}
	
	public class MySingleRun extends SingleRun {
		
		MagneticField.MathLib lib;
		Result results[];
		
		public MySingleRun(String shortDescription, MagneticField.MathLib lib) {
			super(shortDescription);
			this.lib = lib;
		}
		
		@Override
		protected void readyRun() {
			MagneticField.setMathLib(lib);
			
			int iterations = getIterationCount();
			results = new Result[iterations];
			
			for (int i = 0; i < iterations; i++) {
				results[i] = new Result();
			}
		}

		
		@Override
		protected void run(int index) {
			MagneticFields.getInstance().field(x[index], y[index], z[index], results[index].result);
		}

	}

}
