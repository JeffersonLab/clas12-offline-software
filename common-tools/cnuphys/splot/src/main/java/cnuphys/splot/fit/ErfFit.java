package cnuphys.splot.fit;

//import org.apache.commons.math3.special.Erf;
import com.nr.model.Fitmrq;
import com.nr.sf.Erf;

/**
 * For fitting a single error function to a curve (DataColumn)
 * 
 * @author heddle
 * 
 */
public class ErfFit extends Fitmrq implements IValueGetter {

    // function that evaluates the Erf
    private static FErf _funcs = new FErf();
    private static Erf _erf = new Erf();

    /**
     * Create a Error function Fit and perform the fit
     * 
     * @param fit
     *            the fit data
     * @param tolerance
     *            the tolerance
     */
    public ErfFit(Fit fit) throws IllegalArgumentException {
	super(fit.getX(), fit.getY(), fit.getSigmaY(), initialGuess(fit),
		_funcs, fit.getTolerance());

	fit.setFitType(FitType.ERF);
	// set holds, then fit
	for (FitHold fh : fit.getHolds()) {
	    hold(fh.index, fh.value);
	}
	fit();
	fit.setFit(this);
    }

    /**
     * A method to generate an initial guess
     * 
     * @param fit
     *            the fit data
     * @return initial guess
     */
    public static double[] initialGuess(Fit fit) {

	double xx[] = fit.getX();
	double yy[] = fit.getY();

	double result[] = FitUtilities.stats(yy);
	double aa[] = new double[4];

	double mean = result[0];
	aa[0] = mean;
	double delY = result[2] - result[1];
	aa[1] = (yy[yy.length - 1] - yy[0]) / 2; // half the deltaY

	// find the x that produces a y
	double varmin = Double.POSITIVE_INFINITY;
	int iimag = 0;
	for (int i = 0; i < (yy.length - 1); i++) {
	    double var = (0.5) * (yy[i] + yy[i]);
	    var = Math.abs(var - mean);
	    if (var < varmin) {
		varmin = var;
		iimag = i;
	    }
	}

	aa[2] = xx[iimag];

	// now try to estimate the spread
	int imin = iimag;
	int imax = iimag;

	double fact = 0.25 * delY;
	for (int i = iimag; i >= 0; i--) {
	    if (Math.abs(yy[i] - mean) > fact) {
		imin = i;
		break;
	    }
	}
	for (int i = iimag; i < yy.length; i++) {
	    if (Math.abs(yy[i] - mean) > fact) {
		imax = i;
		break;
	    }
	}

	if (imin == imax) {
	    if (imin > 0) {
		imin--;
	    } else if (imax < (yy.length - 1)) {
		imax++;
	    }
	}

	double spread = Math.abs(xx[imax] - xx[imin]);
	aa[3] = spread;

	return aa;
    }

    // /**
    // * A method to generate an initial guess
    // * @param xx the x data
    // * @param yy the y data
    // * @return initial guess
    // */
    // public static double[] initialGuess2(double xx[], double yy[]) {
    // double aa[] = new double[4];
    //
    // double result[] = stats(yy);
    // aa[0] = result[0];
    // // aa[1] = (result[2] - result[1])/2.;
    // aa[1] = (yy[yy.length-1] - yy[0])/2;
    //
    // //algorithm.
    // //1) get cubic spline.
    // //2) get deriv of cubic spline
    // //3) fit to gaussian
    //
    // CubicSpline cs = new CubicSpline(xx, yy);
    // CubicSpline deriv = cs.derivative();
    //
    // double yyy[] = new double[xx.length];
    // for (int i = 0; i < yyy.length; i++) {
    // yyy[i] = Math.abs(deriv._y[i]);
    // if (yyy[i] < 1.0e-8) {
    // yyy[i] = 0.0;
    // }
    // }
    //
    // try {
    // Fitmrq gf = FitUtilities.fitGaussianNoErrors(xx, yyy, 1, 1.0e-3);
    // aa[2] = gf.a[1];
    // aa[3] = gf.a[2];
    // } catch (Exception e) {
    // System.err.println("Error Gaussian fit for Erf Fit initial guess "
    // + e.getMessage());
    // }
    //
    // return aa;
    // }

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
	double A = a[0];
	double B = a[1];
	double MU = a[2];
	double S = a[3];

	return A + B * _erf.erf((x - MU) / S);
    }

}
