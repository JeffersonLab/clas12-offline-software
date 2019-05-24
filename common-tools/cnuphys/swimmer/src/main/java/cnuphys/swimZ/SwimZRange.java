package cnuphys.swimZ;

public class SwimZRange {

	/** the starting value of the range */
	private final double _zo;

	/** the ending value of the range */
	private final double _zf;

	/** the stepSize */
	private final double _stepSize;

	/**
	 * the number of steps. Defined such that z(0) = zo and z(numStep) = zf
	 * (exactly)
	 */
	private final int _numStep;

	/**
	 * Get the starting value of z
	 * 
	 * @return the starting value of z
	 */
	public double getZo() {
		return _zo;
	}

	/**
	 * Get the final value of z
	 * 
	 * @return the final value of z
	 */
	public double getZf() {
		return _zf;
	}

	/**
	 * Get the number of steps
	 * 
	 * @return the number of steps
	 */
	public int getNumStep() {
		return _numStep;
	}

	/**
	 * Get the step size. This is the first stepsize smaller than the one provided
	 * in the constructor that ensures an integer number of steps takes us exactly
	 * to zf, i.e. zf = zo + numStep*stepSize
	 * 
	 * @return the step size in Z
	 */
	public double getSetpSize() {
		return _stepSize;
	}

	/**
	 * Create an integration range
	 * 
	 * @param zo       the staring value
	 * @param zf       the ending value
	 * @param stepSize the approximate stepsize. It will be modified to the next
	 *                 smaller stepsize that requires an integer number of steps.
	 */
	public SwimZRange(double zo, double zf, double stepSize) {
		_zo = zo;
		_zf = zf;

		_numStep = (int) (Math.abs(zf - zo) / stepSize) + 1;
		// redefine stepsize to make it exact
		_stepSize = (zf - zo) / _numStep;
	}

	/**
	 * Obtain z for a given step number n [0, numStep]
	 * 
	 * @param n the value of n
	 * @return the z value
	 */
	public double z(int n) {
		if ((n < 0) || (n > _numStep)) {
			return Double.NaN;
		}
		return _zo + n * _stepSize;
	}

	public static void main(String arg[]) {
		SwimZRange szr = new SwimZRange(0.1234567, -9.87654321, .222);

		System.out.println("zo: " + szr._zo);
		System.out.println("zf: " + szr._zf);
		System.out.println("stepSize: " + szr._stepSize);
		System.out.println("numStep: " + szr._numStep);

		for (int i = 0; i <= szr._numStep; i++) {
			System.out.print("Z[" + i + "] = " + szr.z(i));
			if (i == 0) {
				System.out.println("");
			} else {
				System.out.println("    DEL: " + (szr.z(i) - szr.z(i - 1)));
			}
		}
	}
}
