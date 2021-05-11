package cnuphys.splot.fit;

import java.util.Vector;

import cnuphys.splot.pdata.DataColumn;
import cnuphys.splot.pdata.HistoData;
import cnuphys.splot.plot.Environment;
import cnuphys.splot.plot.UnicodeSupport;

/**
 * General Fit container class. It holds all the information for a fit, but the
 * actual fit is performed by the object held in the <code>_fit</code> instance
 * variable, which is a numerical recipes fit object.
 * 
 * @author heddle
 *
 */

public class Fit {

	protected enum ErrorType {
		Y_Errors, X_Errors, XY_Errors, Massaged_Zeroes, No_Errors
	};

	// the fit type
	private FitType _type = FitType.LINE;

	// the order for polynomial fit
	private int _order = 2;

	// default number of gaussians if relevant
	private int _numGauss = 1;

	// the tolerance
	private double _tolerance = 1.0e-8;

	// how errors were used
	private ErrorType _errorType;

	// the x data used for the fit
	private double _x[];

	// the y data used for the fit
	private double _y[];

	// the y error data used for the fit
	private double _sigmaY[];

	// the x error data used for the fit
	private double _sigmaX[];

	// holds on the fit parameters
	private Vector<FitHold> _holds = new Vector<FitHold>();

	// this holds the fit object. It is probably an NR object like a Fitmrq
	// or a Fitsvd
	private Object _fit;

	public Fit() {
	}

	public Fit(double x[], double y[], double sigmaY[]) {
		this(x, y, sigmaY, 1.0e-8);
	}

	/**
	 * Create a fit which is someone poorly named. It is really just a container
	 * that holds all the data for a fit. It will be used in the constructor of an
	 * actual fit.
	 * 
	 * @param x         the x data
	 * @param y         the y data
	 * @param sigmaY    the y errors
	 * @param tolerance accuracy parameter (for nonlinear fits)
	 */
	public Fit(double x[], double y[], double sigmaY[], double tolerance) {
		this(x, y, null, sigmaY, tolerance);
	}

	/**
	 * Create a fit which is someone poorly named. It is really just a container
	 * that holds all the data for a fit. It will be used in the constructor of an
	 * actual fit.
	 * 
	 * @param x         the x data
	 * @param y         the y data
	 * @param sigmaX    the x errors
	 * @param sigmaY    the y errors
	 * @param tolerance accuracy parameter (for nonlinear fits)
	 */
	public Fit(double x[], double y[], double sigmaX[], double sigmaY[], double tolerance) {
		_x = x;
		_y = y;
		_sigmaX = sigmaX;
		_sigmaY = sigmaY;
		_tolerance = tolerance;
		FitUtilities.fixNullSig(this);
	}

	/**
	 * The x and y data arrays should be the same size. This returns the smaller of
	 * the two sizes.
	 * 
	 * @return the minimum size of the data arrays
	 */
	public int size() {
		int xsize = (_x == null) ? 0 : _x.length;
		int ysize = (_y == null) ? 0 : _y.length;

		if (xsize != ysize) {
			System.err.println("Different x and y array sizes in Fit.");
			System.err.println("x size: " + xsize + "  ysize: " + ysize);
		}

		return Math.min(xsize, ysize);
	}

	/**
	 * Set a hold on a fit parameter. If there already is one it will be overwritten
	 * using the new value.
	 * 
	 * @param index the index of the parameter
	 * @param val   the value at which the parameter is held.
	 */
	public void hold(int index, double val) {

		// already one for that index?
		for (FitHold fh : _holds) {
			if (fh.index == index) {
				fh.value = val;
				return;
			}
		}

		// wasn't one, so create it
		FitHold fh = new FitHold(index, val);
		_holds.add(fh);
	}

	/**
	 * Get the fit parameter holds
	 * 
	 * @return the fit parameter holds
	 */
	protected Vector<FitHold> getHolds() {
		return _holds;
	}

	/**
	 * Check whether a parameter was held
	 * 
	 * @param index the index of the parameter
	 * @return <code>true</code> if the index was held
	 */
	public boolean isHeld(int index) {
		for (FitHold fh : _holds) {
			if (fh.index == index) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the x data for the fit
	 * 
	 * @return the x data for the fit
	 */
	public double[] getX() {
		return _x;
	}

	/**
	 * Get the y data for the fit
	 * 
	 * @return the y data for the fit
	 */
	public double[] getY() {
		return _y;
	}

	/**
	 * Get the y error data for the fit
	 * 
	 * @return the y error data for the fit
	 */
	public double[] getSigmaY() {
		return _sigmaY;
	}

	/**
	 * Get the x error data for the fit
	 * 
	 * @return the x error data for the fit
	 */
	public double[] getSigmaX() {
		return _sigmaX;
	}

	/**
	 * Set the x data for the fit
	 * 
	 * @param x the x data
	 */
	public void setX(double x[]) {
		_x = x;
	}

	/**
	 * Set the y data for the fit
	 * 
	 * @param y the y data
	 */
	public void setY(double y[]) {
		_y = y;
	}

	/**
	 * Set the y error data for the fit
	 * 
	 * @param sigma the y error data
	 */
	public void setSigmaY(double sigma[]) {
		_sigmaY = sigma;
	}

	/**
	 * Set the x error data for the fit
	 * 
	 * @param sigma the x error data
	 */
	public void setSigmaX(double sigma[]) {
		_sigmaX = sigma;
	}

	/**
	 * Set the error type used by the fitting. The client does no set this, the
	 * methods that call the fits in FitUtilities set this. If the type is "Massaged
	 * zeroes" it meant that error of zero were given fake fakes (equal to the mean
	 * error) to prevent the fits from failing.
	 * 
	 * @param errorType the way the errors were used.
	 */
	protected void setErrorType(ErrorType errorType) {
		_errorType = errorType;
	}

	/**
	 * Get the error type that was used in the fitting
	 * 
	 * @return the error type that was used in the fitting
	 */
	public ErrorType getErrorType() {
		return _errorType;
	}

	/**
	 * Get the polynomial order--the power of the highest power
	 * 
	 * @param order the polynomial order--the power of the highest power
	 */
	public void setPolynomialOrder(int order) {
		if (order != _order) {
			_order = order;
			setDirty();
		}
	}

	/**
	 * Get the polynomial order, the polynomial order--the power of the highest
	 * power
	 * 
	 * @return the polynomial order
	 */
	public int getPolynomialOrder() {
		return _order;
	}

	/**
	 * Get the number of gaussians
	 * 
	 * @return the number of gaussians
	 */
	public int getNumGaussian() {
		return _numGauss;
	}

	/**
	 * Set the number of gaussians
	 * 
	 * @param numGauss the new number of gaussians
	 */
	public void setNumGaussian(int numGauss) {
		if (numGauss != _numGauss) {
			_numGauss = numGauss;
			setDirty();
		}
	}

	/**
	 * Set the tolerance
	 * 
	 * @param tolerance the value of the tolerance
	 */
	public void setTolerance(double tolerance) {
		if (tolerance != _tolerance) {
			_tolerance = tolerance;
			setDirty();
		}
	}

	/**
	 * Get the tolerance
	 * 
	 * @return the tolerance
	 */
	public double getTolerance() {
		return _tolerance;
	}

	/**
	 * Returns the fit object, which is most likely a numerical recipes fit
	 * 
	 * @return the fit object.
	 */
	public Object getFit() {
		return _fit;
	}

	/**
	 * Set the fit
	 * 
	 * @param fit the new fit object, which is most likely a numerical recipes fit
	 */
	public void setFit(Object fit) {
		_fit = fit;
	}

	/**
	 * Set the fit to be dirty. Perhaps more data have been added
	 */
	public void setDirty() {
		_fit = null;
	}

	/**
	 * Check whether fit needs to be recomputed
	 * 
	 * @return <code>true</code> if fit must be recomputed
	 */
	public boolean isDirty() {
		return (_fit == null);
	}

	/**
	 * Get the fit type
	 * 
	 * @return the fit type
	 */
	public FitType getFitType() {
		return _type;
	}

	/**
	 * Set the fit type
	 * 
	 * @param type the fit type
	 */
	public void setFitType(FitType type) {
		if (type != _type) {
			_type = type;
			setDirty();
		}
	}

	/**
	 * Get a report on the fit parameters in html
	 * 
	 * @param curve the active curve
	 * @return the html fit string description
	 */
	public String getFitString(DataColumn curve) {

		StringBuffer sb = new StringBuffer(1024);

		sb.append(curve.getName() + "<BR>");
		if (curve.isHistogram1D()) {
			HistoData hd = curve.getHistoData();
			String cntStr = String.format(
					"Total counts: %d<BR> Good counts: %d<BR> Over counts: %d<BR> Under counts: %d", hd.getTotalCount(),
					hd.getGoodCount(), hd.getUnderCount(), hd.getOverCount());
			sb.append(cntStr + "<BR>");

			double results[] = hd.getBasicStatistics();
			String statStr = String.format(UnicodeSupport.SMALL_MU + " = %9.3g&nbsp;&nbsp;"
					+ colorStr("rms = %9.3g&nbsp;&nbsp;", "red") + UnicodeSupport.SMALL_SIGMA + " = %9.3g", results[0],
					results[2], results[1]);
			sb.append(statStr + "<BR>");
		}
		sb.append(FitType.getFitString(this));

		if (Environment.getInstance().isLinux()) {
			return "<body style=\"font-size:8px;color:blue\">" + sb.toString() + "</body>";
		}
		else {
			return "<body style=\"font-size:9px;color:blue\">" + sb.toString() + "</body>";
		}
	}

	// create an html colored string
	private String colorStr(String s, String color) {
		return "<font color=" + color + ">" + s + "</font>";
	}

	public static void main(String arg[]) {
		double rawdata[] = { 25.0, 1.000900900900901, 0.01733697900764091, 26.0, 1.000900900900901, 0.01733697900764091,
				27.0, 1.000900900900901, 0.01733697900764091, 28.0, 1.000900900900901, 0.01733697900764091, 29.0,
				1.000900900900901, 0.01733697900764091, 30.0, 1.000900900900901, 0.01733697900764091, 31.0,
				1.000900900900901, 0.01733697900764091, 32.0, 1.000900900900901, 0.01733697900764091, 33.0,
				1.000900900900901, 0.01733697900764091, 34.0, 1.000900900900901, 0.01733697900764091, 35.0,
				1.000900900900901, 0.01733697900764091, 36.0, 1.000900900900901, 0.01733697900764091, 37.0,
				1.000900900900901, 0.01733697900764091, 38.0, 1.000900900900901, 0.01733697900764091, 39.0,
				1.000900900900901, 0.01733697900764091, 40.0, 1.000900900900901, 0.01733697900764091, 41.0,
				1.000900900900901, 0.01733697900764091, 42.0, 0.9996996996996997, 0.017326572656758746, 43.0,
				1.0003003003003004, 0.017331776613222914, 44.0, 1.0006006006006005, 0.017334378005599775, 45.0,
				0.9990990990990991, 0.017321367136840526, 46.0, 0.9972972972972973, 0.017305741182250943, 47.0,
				0.9927927927927928, 0.017266614428186246, 48.0, 0.9927927927927928, 0.017266614428186246, 49.0,
				0.9888888888888889, 0.017232632715199488, 50.0, 0.9774774774774775, 0.017132915105820917, 51.0,
				0.9621621621621622, 0.016998164201903792, 52.0, 0.9597597597597598, 0.016976929759882967, 53.0,
				0.9414414414414415, 0.016814135350353657, 54.0, 0.8996996996996997, 0.016437155775860673, 55.0,
				0.8687687687687687, 0.016152136767399645, 56.0, 0.8369369369369369, 0.015853466923502115, 57.0,
				0.7672672672672672, 0.015179281628949083, 58.0, 0.7018018018018019, 0.014517275633960144, 59.0,
				0.6336336336336337, 0.013794215108535558, 60.0, 0.578978978978979, 0.013185884924985075, 61.0,
				0.4921921921921922, 0.01215752701501331, 62.0, 0.4297297297297297, 0.01135992811974639, 63.0,
				0.3483483483483483, 0.010227859679235077, 64.0, 0.26036036036036037, 0.008842301420021296, 65.0,
				0.1990990990990991, 0.007732368282032272, 66.0, 0.15195195195195196, 0.006755088215736933, 67.0,
				0.12252252252252252, 0.006065768731748996, 68.0, 0.09759759759759759, 0.005413740653849834, 69.0,
				0.05795795795795796, 0.004171905101936878, 70.0, 0.04924924924924925, 0.0038457202627224314, 71.0,
				0.030930930930930932, 0.003047715184712378, 72.0, 0.02132132132132132, 0.002530375307260168, 73.0,
				0.014414414414414415, 0.002080541510593246, 74.0, 0.0075075075075075074, 0.0015015015015015015, 75.0,
				0.0045045045045045045, 0.0011630580619241492, 76.0, 0.0036036036036036037, 0.001040270755296623, 77.0,
				0.0015015015015015015, 6.71491885135072E-4, 78.0, 0.0015015015015015015, 6.71491885135072E-4, 79.0,
				9.009009009009009E-4, 5.201353776483115E-4,
				// 80.0, 0, 0,
				// 81.0, 0, 0,
				// 82.0, 0, 0,
		};

		int len = rawdata.length / 3;
		double x[] = new double[len];
		double y[] = new double[len];
		double sigma[] = new double[len];

		for (int i = 0; i < len; i++) {
			int j = i * 3;
			x[i] = rawdata[j];
			y[i] = rawdata[j + 1];
			sigma[i] = rawdata[j + 2];
		}

		Fit fit = new Fit(x, y, sigma, 1.0e-4);
		// fit.hold(0, 0.5);
		// fit.hold(1, -0.5);
		ErfFit efit = new ErfFit(fit);

		double a[] = efit.getFitParameters();
		System.out.println("\nErf Fit");
		System.out.println("A = " + a[0]);
		System.out.println("B = " + a[1]);
		System.out.println("mu = " + a[2]);
		System.out.println("S = " + a[3] / Math.sqrt(2));
		System.out.println("chisq = " + efit.getChiSquare());

		System.out.println("\nLinear Fit");

		double xx[] = { 1, 2, 3, 4, 5, 6, 7 };
		double yy[] = { 1, 2, 3, 4, 5, 6, 7 };
		double sig[] = { .1, .2, .1, .2, .05, .05, .2 };
		Fit fit2 = new Fit(xx, yy, sig);
		LineFit lfit = new LineFit(fit2);
		System.out.println("slope = " + lfit.getSlope() + " +- " + lfit.getSlopeSigma());
		System.out.println("intercept = " + lfit.getIntercept() + " +- " + lfit.getInterceptSigma());

	}

}
