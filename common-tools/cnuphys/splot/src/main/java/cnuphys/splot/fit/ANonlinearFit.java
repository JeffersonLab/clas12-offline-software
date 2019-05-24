package cnuphys.splot.fit;

import org.netlib.util.doubleW;

import com.nr.model.Fitmrq;
import com.nr.model.MultiFuncd;

public abstract class ANonlinearFit implements IValueGetter {

	// the underlying Numerical Recipe nonlinear fitter
	private Fitmrq _fitmrq;

	// the user supplied fitting function
	private MultiFuncd _func;

	/**
	 * Create a Error function Fit
	 * 
	 * @param fit       the fit data
	 * @param tolerance the tolerance
	 */
	public ANonlinearFit(Fit fit) throws IllegalArgumentException {

		_func = getFunc();
		_fitmrq = new Fitmrq(fit.getX(), fit.getY(), fit.getSigmaY(), initialGuess(fit), _func, fit.getTolerance());

		// set any holds
		for (FitHold fh : fit.getHolds()) {
			_fitmrq.hold(fh.index, fh.value);
		}

		// call for the actual fit
		_fitmrq.fit();

		// cache the fit
		fit.setFit(this);
	}

	/**
	 * Get the fit parameters
	 * 
	 * @return the fit parameters
	 */
	public double[] getFitParameters() {
		return _fitmrq.a;
	}

	/**
	 * Get the covariance matrix
	 * 
	 * @return the covariance matrix
	 */
	public double[][] getCovarianceMatrix() {
		return _fitmrq.covar;
	}

	/**
	 * Get the chi square of the fit
	 * 
	 * @return the chi square
	 */
	public double getChiSquare() {
		return _fitmrq.chisq;
	}

	/**
	 * This method is an "after the fact" evaluator. After the fit is complete is
	 * should be able to use the resulting fit parameters to simply evaluate the
	 * function at a given x. The default implementation uses the fitting function
	 * _func. This is good in that it guarantees consistency, but the fitting
	 * function also computes the derivatives so there is an inefficiency. NOTE:
	 * this function is NOT used by the fitting process. It is only for convenience,
	 * afterward, such as in plotting.
	 */
	@Override
	public double value(double x) {
		final doubleW y = new doubleW(0);
		final double a[] = getFitParameters();
		final double dyda[] = new double[a.length];
		_func.funk(x, a, y, dyda);
		return y.val;
	}

	protected abstract double[] initialGuess(Fit fit);

	protected abstract MultiFuncd getFunc();

}
