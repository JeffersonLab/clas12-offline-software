package cnuphys.swimtest;

import java.io.Serializable;
import java.util.Vector;

public class TestTrajectories implements Serializable {

	private Vector<TrajectorySummary> summaries;
	
	public TestTrajectories() {
		summaries = new Vector<TrajectorySummary>();
	}
}
