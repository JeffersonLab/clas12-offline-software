package cnuphys.adaptiveSwim;

import cnuphys.rk4.ButcherTableau;
import cnuphys.rk4.IDerivative;

public class ButcherAdvance implements IAdaptiveAdvance {
	
	//a safety fudge factor
	private static final double _safety = 0.9;
	
	//power used when the step should grow
	private static final double _pgrow = -0.20;
	
	//power used when the step should shrink
	private static final double _pshrink = -0.25;
	
	//for error control
	private static final double _errControl = 1.89e-4;
	
	//to make a correction that gives us 5th order accuracy
	private static final double _correctFifth = 1. / 15;
	
	//a tiny number
	private static final double _tiny = 1.0e-14;

	
	//the dimension of the problem (length of state vector)
	private int _nDim;
	
	private ButcherTableau _tableau;
	
	private double _uscale[];

	private double _error[];
	
	public ButcherAdvance(int nDim, ButcherTableau tableau) {
		_nDim = nDim;
		_tableau = tableau;
		_uscale = new double[_nDim];

		_error = new double[_nDim];
	}

	@Override
	public void advance(double s, double[] u, double[] du, double h, IDerivative deriv, double[] uf, double eps,
			AdaptiveStepResult result) {

		boolean done = false;

		// almost relative error, but with safety when values of u are small
		for (int i = 0; i < _nDim; i++) {
			_uscale[i] = Math.abs(u[i]) + Math.abs(h * du[i]) + _tiny;
		}

		while (!done) {

			AdaptiveSwimUtilities.singleButcherStep(eps, u, du, h, deriv, uf, _error, _tableau);

//			System.out.print(String.format("[%7.4f, %7.4f, %7.4f, %7.4f, %7.4f, %7.4f]", uf[0], uf[1], uf[2], uf[3], uf[4], uf[5]));

			double errMax = 0;
			for (int i = 0; i < _nDim; i++) {
				// set utemp to be the difference between the two step solution and the one step
				errMax = Math.max(errMax, Math.abs(_error[i] / _uscale[i]));
			}

			// scale based on tolerance in eps
			errMax = errMax / eps;
//			System.out.println(String.format("  err: %-6.3f", errMax));

			double hnew;
			if (errMax > 1) {
				// get smaller h, then try again since done = false

				double shrinkFact = _safety * Math.pow(errMax, _pshrink);

				// no more than a factor of 4
				shrinkFact = Math.max(shrinkFact, 0.25);
				h = h * shrinkFact;
			} else { // can grow
				if (errMax > _errControl) {
					double growFact = _safety * Math.pow(errMax, _pgrow);
					hnew = h * growFact;		
				} else {
					hnew = 5 * h;
				}

				result.setHNew(hnew);
				result.setSNew(s + h);
				done = true;
			}

		} // !done

		// mop up 5th order truncation error
		//so result is actually 5th order
		for (int i = 0; i < _nDim; i++) {
			uf[i] = uf[i] + _error[i] * _correctFifth;
		}
	} // end advance

}
