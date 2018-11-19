package cnuphys.ced.swimtest.tests;

import cnuphys.ced.swimtest.ASwimTest;
import cnuphys.ced.swimtest.SingleRun;

public class SameTrackDifferentMaxStep extends ASwimTest {

//	public SwimTrajectory swim(int charge, double xo, double yo, double zo, double momentum, double theta, double phi,
//			final double fixedZ, double accuracy, double maxR, double maxPathLength, double stepSize,
//			double relTolerance[], double hdata[]) throws RungeKuttaException {
	
	
	
	public SameTrackDifferentMaxStep() {
		super();
	}

	@Override
	protected void initialize() {
		super.initialize();
		properties.setProperty(NAME, "Same Track, Different Max Step");
	}

	@Override
	protected void createSingleRuns() {
		
		SingleRun run1 = new SingleRun() {

			@Override
			protected void run(int index) {
			}

			@Override
			protected void readyRun() {
				// TODO Auto-generated method stub
				
			}
			
		};
		
		SingleRun run2 = new SingleRun() {

			@Override
			protected void run(int index) {
				System.err.println("Index = " + index);
			}

			@Override
			protected void readyRun() {
				// TODO Auto-generated method stub
				
			}
			
		};
		
		addSingleRun(run1);
		addSingleRun(run2);
		
	}
	
	//everything is initialized, about to launch, final prep
	@Override
	protected void aboutToLaunch() {
		System.err.println("PREPARE TO LAUNCH");
		System.err.println("NUMBER OF ITERATIONS = " + getIterationCount());
		System.err.println("RANDOM NUMBER SEED = " + getSeed());
	}


	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public String getResults() {
		return null;
	}

}
