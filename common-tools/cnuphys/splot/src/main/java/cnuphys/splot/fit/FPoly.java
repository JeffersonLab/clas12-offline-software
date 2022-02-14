package cnuphys.splot.fit;

import org.netlib.util.doubleW;

import com.nr.model.MultiFuncd;

public class FPoly implements MultiFuncd {
	public final int numPoly;

	/**
	 * 
	 * @param numPoly one more than the order, e.g. 3 for quadratic
	 */
	public FPoly(final int numPoly) {
		this.numPoly = numPoly;
	}

	@Override
	public void funk(final double x, final double[] a, final doubleW y, final double[] dyda) {

		y.val = 0.;

		if (numPoly > 0) {
			double[] p = new double[numPoly];
			p[0] = 1.0;
			for (int j = 1; j < numPoly; j++) {
				p[j] = p[j - 1] * x;
			}

			for (int i = 0; i < numPoly; i++) {
				y.val += a[i] * p[i];
				dyda[i] = p[i];
			}
		}
	}

}