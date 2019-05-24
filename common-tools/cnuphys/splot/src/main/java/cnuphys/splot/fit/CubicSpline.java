package cnuphys.splot.fit;

import java.awt.Point;
import java.util.Arrays;
import java.util.Comparator;

import org.netlib.util.doubleW;

import com.nr.UniVarRealValueFun;
import com.nr.fe.Dfridr;
import com.nr.interp.Spline_interp;
import com.nr.root.Roots;
import com.nr.root.Roots.Zbrak;

public class CubicSpline implements IValueGetter {

	private Spline_interp _nrspline;

	double _x[];
	double _y[];

	public CubicSpline(final double x[], final double y[]) {
		_x = x;
		_y = y;
		_nrspline = new Spline_interp(x, y);
	}

	@Override
	public double value(double x) {
		return _nrspline.interp(x);
	}

	/**
	 * Obtain cubic spline for the derivative by numeric differentiation
	 * 
	 * @return
	 */
	public CubicSpline derivative() {
		int n = _x.length;
		final double h = (_x[n - 1] - _x[0]) / 20;

		final UniVarRealValueFun func = new UniVarRealValueFun() {

			@Override
			public double funk(double x) {
				return value(x);
			}

		};

		final doubleW err = new doubleW(0);

		final double deriv[] = new double[n];
		for (int i = 0; i < n; i++) {
			final double x = _x[i];
			deriv[i] = Dfridr.dfridr(func, _x[i], h, err);
		}

		return new CubicSpline(_x, deriv);
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
	 * Find the xy values of the maxima of the interpolated function in a given
	 * range
	 * 
	 * @param xmin the min value of the range
	 * @param xmax the max value of the range
	 * @return and array of xy values, or <code>null</code>
	 */
	public Point.Double[] findXValsOfMaxima(double xmin, double xmax) {

		// find zeroes of derivatives
		CubicSpline firstDeriv = derivative();
		double roots[] = firstDeriv.findRoots(xmin, xmax);
		if (roots == null) {
			return null;
		}

		// get second deriv
		CubicSpline secondDeriv = firstDeriv.derivative();
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

}
