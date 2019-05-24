package cnuphys.splot.fit;

import com.nr.model.Fitmrq;

public class AltPolynomialFit extends Fitmrq implements IValueGetter {

	/**
	 * Create an alternate polynomial fit that uses the nonlinear fitter rather than
	 * generalized least squares.
	 * 
	 * @param fit the fit data
	 */
	public AltPolynomialFit(Fit fit) {
		super(fit.getX(), fit.getY(), fit.getSigmaY(), initialGuess(fit), createFPoly(fit.getPolynomialOrder() + 1),
				fit.getTolerance());

		// set holds, then fit
		for (FitHold fh : fit.getHolds()) {
			hold(fh.index, fh.value);
		}
		try {
			fit();
			fit.setFit(this);
		}
		catch (IllegalArgumentException e) {
			System.err.println("Alt Polynomial fit failed.");
			e.printStackTrace();
		}
	}

	private static double[] initialGuess(Fit fit) {
		int n = fit.getPolynomialOrder() + 1;
		double aa[] = new double[n];
		for (int i = 0; i < n; i++) {
			aa[i] = 1.0;
		}

		return aa;
	}

	// public void funk(final double x, final double[]a, final doubleW y, final
	// double[] dyda){

	@Override
	public double value(double x) {

		double y = 0.;
		int numPoly = a.length;

		// now the poly
		if (numPoly > 0) {

			double[] p = new double[numPoly];
			p[0] = 1.0;
			for (int j = 1; j < numPoly; j++) {
				p[j] = p[j - 1] * x;
			}

			for (int i = 0; i < numPoly; i++) {
				y += a[i] * p[i];
			}
		}

		return y;
	}

	public static FPoly createFPoly(final int numPoly) {
		FPoly fpoly = new FPoly(numPoly);
		return fpoly;
	}

}
