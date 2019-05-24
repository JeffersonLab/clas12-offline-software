package cnuphys.splot.fit;

import com.nr.model.Fitexy;

public class LinearExyFit extends Fitexy implements IValueGetter {

	/**
	 * Create a Error function Fit and perform the fit. This is for data with y
	 * errors only. If there are x errors too, use LinearXYE.
	 * 
	 * @param fit the fit data
	 */
	public LinearExyFit(Fit fit) throws IllegalArgumentException {
		super(fit.getX(), fit.getY(), fit.getSigmaX(), fit.getSigmaY());
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

	public static void main(String arg[]) {
		// 3.000000 228.380525 0.070000 0.218365
		// 4.000000 303.564738 0.080000 0.227791
		// 5.000000 379.971954 0.090000 0.222510

		double x[] = { 3, 4, 5 };
		double y[] = { 228.380525, 303.564738, 379.971954 };
		double xSig[] = { 0.07, 0.08, 0.09 };
		double ySig[] = { 0.218365, 0.227791, 0.222510 };

		Fit fit = new Fit(x, y, xSig, ySig, 0);
		LinearExyFit fitExy = new LinearExyFit(fit);

		System.out.println("slope = " + fitExy.getSlope() + " +- " + fitExy.getSlopeSigma());
		System.out.println("intercept = " + fitExy.getIntercept() + " +- " + fitExy.getInterceptSigma());

	}

}
