package cnuphys.splot.fit;

import com.nr.UniVarRealMultiValueFun;
import com.nr.model.Fitsvd;

public class PolyFit extends Fitsvd implements IValueGetter {

	private int _n;
	private UniVarRealMultiValueFun _funcs;

	/**
	 * 
	 * @param fit the fit data
	 */
	public PolyFit(Fit fit) {
		super(fit.getX(), fit.getY(), fit.getSigmaY(), getFuncs(fit.getPolynomialOrder() + 1), fit.getTolerance());
		_funcs = getFuncs(fit.getPolynomialOrder() + 1);
		_n = fit.getPolynomialOrder() + 1;
		fit.setFitType(FitType.POLYNOMIAL);

		try {
			fit();
			fit.setFit(this);
		}
		catch (IllegalArgumentException e) {
			System.err.println("Polynomial fit failed.");
		}
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

	@Override
	public double value(double x) {
		double p[] = _funcs.funk(x);

		double sum = 0;
		for (int i = 0; i < _n; i++) {
			sum += a[i] * p[i];
		}
		return sum;
	}

	private static UniVarRealMultiValueFun getFuncs(final int n) {
		UniVarRealMultiValueFun funcs = new UniVarRealMultiValueFun() {

			@Override
			public double[] funk(double x) {
				double[] p = new double[n];
				p[0] = 1.0;
				for (int j = 1; j < n; j++) {
					p[j] = p[j - 1] * x;
				}
				return p;
			}
		};

		return funcs;
	}
}
