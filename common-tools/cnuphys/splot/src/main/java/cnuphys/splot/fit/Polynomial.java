package cnuphys.splot.fit;

import java.awt.Point;
import java.util.Arrays;
import java.util.Comparator;

import com.nr.UniVarRealValueFun;
import com.nr.root.Roots;
import com.nr.root.Roots.Zbrak;

public class Polynomial implements IValueGetter {

	// the coefficients. Poly is a[0] + a[1]x + a[2]x^2 +...
	private double _a[];

	// coeficients for the derivative
	private double _aderiv[];

	public Polynomial(double a[]) {
		_a = a;

		// get the derivative
		int n = _a.length;
		if (n > 1) {
			int m = n - 1;
			_aderiv = new double[m];
			for (int i = 1; i < n; i++) {
				_aderiv[i - 1] = i * _a[i];
			}
		}
	}

	@Override
	public double value(double x) {
		double[] p = new double[_a.length];
		p[0] = 1.0;
		for (int i = 1; i < _a.length; i++) {
			p[i] = p[i - 1] * x;
		}

		double sum = 0;
		for (int i = 0; i < _a.length; i++) {
			sum += _a[i] * p[i];
		}
		return sum;
	}

	/**
	 * Find the roots of the polynomial in a range
	 * 
	 * @param xmin the min value of the range
	 * @param xmax the max value of the range
	 * @return an array of roots, or <code>null</code>
	 */
	public double[] findRoots(double xmin, double xmax) {

		UniVarRealValueFun fx = new UniVarRealValueFun() {

			@Override
			public double funk(double x) {
				return value(x);
			}

		};

		Zbrak zb = new Zbrak();
		zb.zbrak(fx, xmin, xmax, 100);

		double roots[] = null;
		if (zb.nroot > 0) {
			roots = new double[zb.nroot];
			for (int i = 0; i < zb.nroot; i++) {
				double x1 = zb.xb1[i];
				double x2 = zb.xb2[i];
				roots[i] = Roots.rtsec(fx, x1, x2, 1.0e-6);
			}
		}
		return roots;
	}

	/**
	 * Find the xy values of the maxima of the polynomial in a given range
	 * 
	 * @param xmin the min value of the range
	 * @param xmax the max value of the range
	 * @return and array of xy values, or <code>null</code>
	 */
	public Point.Double[] findXValsOfMaxima(double xmin, double xmax) {
		int n = _a.length;
		if (n < 3) {
			return null;
		}

		// find zeroes of derivatives
		Polynomial firstDeriv = getDerivative();
		double roots[] = firstDeriv.findRoots(xmin, xmax);
		if (roots == null) {
			return null;
		}

		// get second deriv
		Polynomial secondDeriv = firstDeriv.getDerivative();
		// count maxim
		int count = 0;
		for (double x : roots) {
			if (secondDeriv.value(x) < 0.) {
				count++;
			}
		}

		if (count < 1) {
			return null;
		}

		double xvals[] = new double[count];
		int ncount = 0;
		for (double x : roots) {
			if (secondDeriv.value(x) < 0.) {
				xvals[ncount] = x;
				ncount++;
				if (ncount == count) {
					break;
				}
			}
		}

		Point.Double results[] = new Point.Double[count];

		for (int i = 0; i < count; i++) {
			results[i] = new Point.Double();
			results[i].x = xvals[i];
			results[i].y = value(xvals[i]);
		}

		if (count > 1) {
			Comparator<Point.Double> comp = new Comparator<Point.Double>() {

				@Override
				public int compare(java.awt.geom.Point2D.Double p0, java.awt.geom.Point2D.Double p1) {
					return Double.compare(p1.y, p0.y);
				}

			};
			Arrays.sort(results, comp);
		}

		return results;
	}

	public int getMaxNumberOfPositiveRoots() {
		return maxNumPositiveRoots(_a);
	}

	public Polynomial getDerivative() {
		if (_a.length < 2) {
			return null;
		}
		return new Polynomial(_aderiv);
	}

	public static int maxNumPositiveRoots(double a[]) {
		int n = a.length;
		if (n < 2) {
			return 0;
		}

		// get first nonzero a
		double sign = 0;
		int inz = n;

		for (int i = 0; i < (n - 1); i++) {
			sign = Math.signum(a[i]);
			if (sign != 0.) {
				inz = i;
				break;
			}
		}
		if (inz >= (n - 1)) {
			return 0;
		}

		int count = 0;

		for (int i = inz + 1; i < n; i++) {
			double nsign = Math.signum(a[i]);
			if ((nsign != 0) && (nsign != sign)) {
				count++;
				sign = nsign;
			}
		}
		return count;
	}

}
