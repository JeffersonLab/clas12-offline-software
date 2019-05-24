package cnuphys.splot.fit;

import com.nr.model.Fitab;

public class LineFit extends Fitab implements IValueGetter {

	/**
	 * Create a Error function Fit and perform the fit. This is for data with y
	 * errors only. If there are x errors too, use LinearExyFit.
	 * 
	 * @param fit the fit data
	 */
	public LineFit(Fit fit) throws IllegalArgumentException {
		super(fit.getX(), fit.getY(), fit.getSigmaY());

		fit.setFitType(FitType.LINE);
		fit.setFit(this);
	}

	/**
	 * Get the slope of the fit
	 * 
	 * @return the slope of the fit
	 */
	public double getSlope() {
		return b;
	}

	/**
	 * Get the intercept of the fit
	 * 
	 * @return the intercept of the fit
	 */
	public double getIntercept() {
		return a;
	}

	/**
	 * Get the error estimate in the slope
	 * 
	 * @return the error estimate in the slope
	 */
	public double getSlopeSigma() {
		return sigb;
	}

	/**
	 * Get the intercept estimate
	 * 
	 * @return the intercept estimate
	 */
	public double getInterceptSigma() {
		return siga;
	}

	/**
	 * Get the chi square of the fit
	 * 
	 * @return the chi square
	 */
	public double getChiSquare() {
		return chi2;
	}

	@Override
	public double value(double x) {
		return a + b * x;
	}

}
