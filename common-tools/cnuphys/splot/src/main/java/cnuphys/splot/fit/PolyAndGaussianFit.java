package cnuphys.splot.fit;

import static com.nr.NRUtil.SQR;
import static java.lang.Math.exp;

import com.nr.model.Fitmrq;

public class PolyAndGaussianFit extends Fitmrq implements IValueGetter {

	int _numGauss;
	int _numPoly;

	/**
	 * 
	 * @param fit       the fit data
	 * @param tolerance
	 */
	public PolyAndGaussianFit(Fit fit) {
		super(fit.getX(), fit.getY(), fit.getSigmaY(), initialGuess(fit),
				createFGaussPoly(fit.getNumGaussian(), fit.getPolynomialOrder() + 1), fit.getTolerance());
		_numGauss = fit.getNumGaussian();
		_numPoly = fit.getPolynomialOrder() + 1;

		fit.setFitType(FitType.POLYPLUSGAUSS);
		// set holds, then fit
		for (FitHold fh : fit.getHolds()) {
			hold(fh.index, fh.value);
		}

		try {
			fit();
			fit.setFit(this);
		}
		catch (IllegalArgumentException e) {
			System.err.println("Gaussian plus Polynomial fit failed.");
		}
	}

	/**
	 * A method to generate an initial guess
	 * 
	 * @param xx       the x data
	 * @param yy       the y data
	 * @param numGauss the number of gaussians
	 * @param np       number of terms in poly (order + 1)
	 * @return
	 */
	private static double[] initialGuess(Fit fit) {
		double bb[] = GaussianFit.initialGuess(fit);

		int ng3 = 3 * fit.getNumGaussian();
		int ntot = ng3 + fit.getPolynomialOrder() + 1;
		double aa[] = new double[ntot];
		for (int i = 0; i < ntot; i++) {
			if (i < ng3) {
				aa[i] = bb[i];
			}
			else {
				aa[i] = 1.0;
			}
		}
		return aa;
	}

	// public void funk(final double x, final double[]a, final doubleW y, final
	// double[] dyda){

	@Override
	public double value(double x) {
		int ng3 = _numGauss * 3;
		double ex, arg;
		double y = 0.;
		for (int i = 0; i < ng3 - 1; i += 3) {
			arg = (x - a[i + 1]) / a[i + 2];
			ex = exp(-SQR(arg));
			y += a[i] * ex;
		}

		// now the poly
		if (_numPoly > 0) {
			int ntot = ng3 + _numPoly;

			double[] p = new double[_numPoly];
			p[0] = 1.0;
			for (int j = 1; j < _numPoly; j++) {
				p[j] = p[j - 1] * x;
			}

			for (int i = ng3; i < ntot; i++) {
				int j = i - ng3;
				y += a[i] * p[j];
			}
		}

		return y;
	}

	/**
	 * Get the fit parameters
	 * 
	 * @return the fit parameters
	 */
	public double[] getFitParameters() {
		return a;
	}

	/**
	 * Get the covariance matrix
	 * 
	 * @return the covariance matrix
	 */
	public double[][] getCovarianceMatrix() {
		return covar;
	}

	/**
	 * Get the chi square of the fit
	 * 
	 * @return the chi square
	 */
	public double getChiSquare() {
		return chisq;
	}

	public static FGaussPoly createFGaussPoly(final int numGauss, final int numPoly) {
		FGaussPoly fpoly = new FGaussPoly(numGauss, numPoly);
		return fpoly;
	}

}
