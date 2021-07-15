package cnuphys.splot.fit;

import static com.nr.NRUtil.SQR;
import static java.lang.Math.exp;

public class FGaussian implements IValueGetter {

	/**
	 * Evaluate a sum of gaussians.
	 * 
	 * @param a should have 3N parameters where N is the number of gaussians
	 */

	private double _a[];

	public FGaussian(double a[]) {
		_a = a;
	}

	@Override
	public double value(double x) {
		int na = _a.length;
		double ex, arg;

		double y = 0;
		for (int i = 0; i < na - 1; i += 3) {
			arg = (x - _a[i + 1]) / _a[i + 2];
			ex = exp(-SQR(arg));
			y += _a[i] * ex;
		}
		return y;
	}

}
