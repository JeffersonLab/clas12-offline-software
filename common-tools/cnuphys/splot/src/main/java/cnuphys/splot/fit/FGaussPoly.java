package cnuphys.splot.fit;

import static com.nr.NRUtil.SQR;
import static java.lang.Math.exp;

import org.netlib.util.doubleW;

import com.nr.model.MultiFuncd;

public class FGaussPoly implements MultiFuncd {
	public final int numGauss;
	public final int numPoly;

	/**
	 * Function evaluator for combined guassians and polynomial
	 * 
	 * @param numGauss
	 * @param numPoly  one more than the order, e.g. 3 for quadratic
	 */
	public FGaussPoly(final int numGauss, final int numPoly) {
		this.numGauss = numGauss;
		this.numPoly = numPoly;
	}

	@Override
	public void funk(final double x, final double[] a, final doubleW y, final double[] dyda) {

		double fac, ex, arg;
		y.val = 0.;
		int ng3 = 3 * numGauss;
		int ntot = ng3 + numPoly;

		// first the gaussian
		for (int i = 0; i < ng3; i += 3) {
			arg = (x - a[i + 1]) / a[i + 2];
			ex = exp(-SQR(arg));
			fac = a[i] * ex * 2. * arg;
			y.val += a[i] * ex;
			dyda[i] = ex;
			dyda[i + 1] = fac / a[i + 2];
			dyda[i + 2] = fac * arg / a[i + 2];
		}

		// now the poly

		if (numPoly > 0) {
			double[] p = new double[numPoly];
			p[0] = 1.0;
			for (int j = 1; j < numPoly; j++) {
				p[j] = p[j - 1] * x;
			}

			for (int i = ng3; i < ntot; i++) {
				int j = i - ng3;
				y.val += a[i] * p[j];
				dyda[i] = p[j];
			}
		}
	}

}
