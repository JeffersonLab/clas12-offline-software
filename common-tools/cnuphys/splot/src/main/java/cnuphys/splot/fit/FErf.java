package cnuphys.splot.fit;

//import org.apache.commons.math3.special.Erf;
import org.netlib.util.doubleW;

import com.nr.model.MultiFuncd;
import com.nr.sf.Erf;

public class FErf implements MultiFuncd {

	private static double _fact = 2. / Math.sqrt(Math.PI);
	private static Erf _erf = new Erf();

	/**
	 * Evaluates the Erf function
	 * 
	 * @param x    the independent variable
	 * @param a    the function parameter array
	 * @param y    the dependent variable
	 * @param dyda must be filled with the derivatives with respect to the fit
	 *             parameters
	 */
	@Override
	public void funk(final double x, final double[] a, final doubleW y, final double[] dyda) {

		double A = a[0];
		double B = a[1];
		double MU = a[2];
		double S = a[3];
		double z = (x - MU) / S;
		double erf = _erf.erf(z);
		double f = -_fact * B;
		double g = Math.exp(-z * z);
		y.val = A + B * erf;
		dyda[0] = 1;
		dyda[1] = erf;
		dyda[2] = f * g / S;
		dyda[3] = dyda[2] * z;
	}

}
