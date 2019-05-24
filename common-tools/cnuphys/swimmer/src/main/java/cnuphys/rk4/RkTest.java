package cnuphys.rk4;

public class RkTest {

	// used to get good timing
	private static int ITERATIONS = 100;

	// a testing function for y'' = y
	public static IDerivative CoshLike = new IDerivative() {

		@Override
		public void derivative(double t, double[] y, double[] dydt) {
			dydt[0] = y[1];
			dydt[1] = y[0];
		}

	};

	/**
	 * Test the uniform step size
	 * 
	 * @param yo       initial values. Probably something like (xo, yo, zo, vxo,
	 *                 vyo, vzo).
	 * @param to       the initial value of the independent variable, e.g., time.
	 * @param tf       the maximum value of the independent variable.
	 * @param deriv    the derivative computer (interface). This is where the
	 *                 problem specificity resides.
	 * @param stopper  if not <code>null</code> will be used to exit the integration
	 *                 early because some condition has been reached.
	 * @param listener listens for each step
	 * @param answer
	 */
	public static void TestUniform(double yo[], double to, double tf, double h, IDerivative deriv, IStopper stopper,
			IRkListener listener, double answer[]) {

		RungeKutta rk = new RungeKutta();

		for (int i = 1; i <= ITERATIONS; i++) {
			rk.uniformStep(yo, to, tf, h, deriv, stopper, listener);
		}

	}

	public static void test() {
		System.err.println("Testing Coshlike function");
		testCoshLike(0.01);
		testCoshLike(0.001);
		testCoshLike(0.0001);
		testCoshLike(0.00001);

	}

	private static void testCoshLike(double h) {

		double to = 0;
		double tf = 2.0;
		double yo[] = { 1, 0 };
		double result[] = new double[2];
		double answer[] = { Math.cosh(2), Math.sinh(2) };
		double diff[] = new double[result.length];

		IRkListener listener = new IRkListener() {

			@Override
			public void nextStep(double newT, double[] newY, double h) {
				result[0] = newY[0];
				result[1] = newY[1];
			}

		};

		long startTime = System.nanoTime();
		TestUniform(yo, to, tf, h, CoshLike, null, listener, answer);
		long estimatedTime = System.nanoTime() - startTime;
		double time = (1.0e-9 * estimatedTime) / ITERATIONS;

		for (int i = 0; i < result.length; i++) {
			diff[i] = answer[i] - result[i];
		}

		System.err.println("\n-----------\nh = " + h);
		System.err.println("time: " + time);
		System.err.println(vStr("Result", result));
		System.err.println(vStr("Answer", answer));
		System.err.println(vStr("  Diff", diff));

	}

	private static String vStr(String name, double v[]) {
		StringBuffer sb = new StringBuffer(256);
		sb.append(name + ": [");

		int len = v.length;
		int lm1 = len - 1;

		for (int i = 0; i < len; i++) {
			sb.append(v[i]);
			if (i < lm1) {
				sb.append(", ");
			}
		}

		sb.append("]");

		return sb.toString();
	}

	public static void main(String arg[]) {
		test();
	}

}
