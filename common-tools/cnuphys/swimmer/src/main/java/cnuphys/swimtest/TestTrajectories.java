package cnuphys.swimtest;

import java.io.Serializable;
import java.util.Vector;

import cnuphys.swim.Swimmer;
import cnuphys.swim.util.TrajectorySummary;

public class TestTrajectories implements Serializable {

	// parameters common to all trajectories
	private boolean _includeTorus = true;
	private boolean _includeSolenoid = true;

	private double _torusScale = -1;
	private double _solenoidScale = 1;

	private String _torusPath = "";

	private double _fixedZ = 5; // m
	private double _maxPathLength = 8; // m
	private double _accuracy = 1.0e-5; // m
	private double _initStepSize = 5e-3; // m

	private double[] _relTolerance = Swimmer.CLAS_Tolerance;

	// all the trajectoies
	private Vector<TrajectorySummary> summaries;

	public TestTrajectories() {
		summaries = new Vector<TrajectorySummary>();
	}

	/**
	 * add a summary to the collection
	 * 
	 * @param summary the summary to add
	 */
	public void addTrajectory(TrajectorySummary summary) {
		summaries.add(summary);
	}

	/**
	 * Get the relative tolerance used by the swimmer
	 * 
	 * @return the relative tolerance
	 */
	public double[] getReletiveTolerance() {
		return _relTolerance;
	}

	/**
	 * Set the relative tolerance for the swimmer
	 * 
	 * @param relTol the relative tolerance
	 * @return this object for chaining
	 */
	public TestTrajectories setRelativeTolerance(double[] relTol) {
		int len = relTol.length;
		_relTolerance = new double[len];
		System.arraycopy(relTol, 0, _relTolerance, 0, len);
		return this;
	}

	/**
	 * Get the initial step size
	 * 
	 * @return the initial step size in meters
	 */
	public double get() {
		return _initStepSize;
	}

	/**
	 * Set the initial step size
	 * 
	 * @param value the value in meters
	 * @return this object for chaining
	 */
	public TestTrajectories set(double value) {
		_initStepSize = value;
		return this;
	}

	/**
	 * Get the z stopping accuracy
	 * 
	 * @return the z stopping accuracy in meters
	 */
	public double getAccuracy() {
		return _accuracy;
	}

	/**
	 * Set the z stopping accuracy in meters
	 * 
	 * @param value the value in meters
	 * @return this object for chaining
	 */
	public TestTrajectories setAccuracy(double value) {
		_accuracy = value;
		return this;
	}

	/**
	 * Get the max path length
	 * 
	 * @return the max path length in meters
	 */
	public double getMaxPathLength() {
		return _maxPathLength;
	}

	/**
	 * Set the max path length
	 * 
	 * @param value the value in meters
	 * @return this object for chaining
	 */
	public TestTrajectories setMaxPathLength(double value) {
		_maxPathLength = value;
		return this;
	}

	/**
	 * Get the z stopping value in meters
	 * 
	 * @return the z stopping value in meters
	 */
	public double getFixedZ() {
		return _fixedZ;
	}

	/**
	 * Set the z stopping value in meters
	 * 
	 * @param value the value in meters
	 * @return this object for chaining
	 */
	public TestTrajectories setFixedZ(double value) {
		_fixedZ = value;
		return this;
	}

	/**
	 * Get the path to the torus
	 * 
	 * @return the path used by the torus
	 */
	public String getTorusPath() {
		return _torusPath;
	}

	/**
	 * Set the path used by the torus
	 * 
	 * @param path the full path
	 * @return this object for chaining
	 */
	public TestTrajectories setTorusPath(String path) {
		_torusPath = new String(path);
		return this;
	}

	/**
	 * Get the scale factor used by the torus
	 * 
	 * @return the scale factor used by the torus
	 */
	public double getTorusScale() {
		return _torusScale;
	}

	/**
	 * Set the scale factor used by the torus
	 * 
	 * @param scale the scale factor
	 * @return this object for chaining
	 */
	public TestTrajectories setTorusScale(double scale) {
		_torusScale = scale;
		return this;
	}

	/**
	 * Get the scale factor used by the solenoid
	 * 
	 * @return the scale factor used by the solenoid
	 */
	public double getSolenoidScale() {
		return _solenoidScale;
	}

	/**
	 * Set the scale factor used by the solenoid
	 * 
	 * @param scale the scale solenoid
	 * @return this object for chaining
	 */
	public TestTrajectories setSolenoidScale(double scale) {
		_solenoidScale = scale;
		return this;
	}

	/**
	 * Was the torus used
	 * 
	 * @return <code>true</code> if the torus was included in the swimming
	 */
	public boolean includeTorus() {
		return _includeTorus;
	}

	/**
	 * Set whether we included the torus
	 * 
	 * @param incTorus the value of the flag
	 * @return this object for chaining
	 */
	public TestTrajectories setIncludeTorus(boolean incTorus) {
		_includeTorus = incTorus;
		return this;
	}

	/**
	 * Set whether we included the solenoid
	 * 
	 * @param incSolenoid the value of the flag
	 */
	public TestTrajectories setIncludeSolenoid(boolean incSolenoid) {
		_includeSolenoid = incSolenoid;
		return this;
	}

	/**
	 * Was the solenoid used
	 * 
	 * @return <code>true</code> if the solenoid was included in the swimming
	 */
	public boolean includeSolenoid() {
		return _includeSolenoid;
	}

	/**
	 * Get the number of summaries
	 * 
	 * @return the number of summaries
	 */
	public int size() {
		return summaries.size();
	}
}
