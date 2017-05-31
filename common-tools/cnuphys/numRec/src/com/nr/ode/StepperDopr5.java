package com.nr.ode;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;
import org.netlib.util.*;

/**
 * Dormand-Prince fifth-order stepper
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class StepperDopr5 extends StepperBase {
    double[] k2, k3, k4, k5, k6;
    double[] rcont1, rcont2, rcont3, rcont4, rcont5;
    double[] dydxnew;
    Controller con = new Controller();

    public static class Controller {
	static final double beta = 0.0, alpha = 0.2 - beta * 0.75, safe = 0.9,
		minscale = 0.2, maxscale = 10.0;
	double hnext, errold;
	boolean reject;

	public Controller() {
	    reject = false;
	    errold = 1.0e-4;
	}

	public boolean success(final double err, final doubleW h) {

	    double scale;
	    if (err <= 1.0) {
		if (err == 0.0)
		    scale = maxscale;
		else {
		    scale = safe * pow(err, -alpha) * pow(errold, beta);
		    if (scale < minscale)
			scale = minscale;
		    if (scale > maxscale)
			scale = maxscale;
		}
		if (reject)
		    hnext = h.val * min(scale, 1.0);
		else
		    hnext = h.val * scale;
		errold = max(err, 1.0e-4);
		reject = false;
		return true;
	    } else {
		scale = max(safe * pow(err, -alpha), minscale);
		h.val *= scale;
		reject = true;
		return false;
	    }
	}
    }

    public StepperDopr5() {

    }

    @Override
    public void setParam(final double[] yy, final double[] dydxx,
	    final double xx, final double atoll, final double rtoll,
	    final boolean dense) {
	super.setParam(yy, dydxx, xx, atoll, rtoll, dense);
	k2 = new double[n];
	k3 = new double[n];
	k4 = new double[n];
	k5 = new double[n];
	k6 = new double[n];
	rcont1 = new double[n];
	rcont2 = new double[n];
	rcont3 = new double[n];
	rcont4 = new double[n];
	rcont5 = new double[n];
	dydxnew = new double[n];

	EPS = DBL_EPSILON;
    }

    public StepperDopr5(final double[] yy, final double[] dydxx,
	    final double xx, final double atoll, final double rtoll,
	    final boolean dense) {
	setParam(yy, dydxx, xx, atoll, rtoll, dense);
    }

    @Override
    public double dense_out(final int i, final double x, final double h) {
	double s = (x - xold) / h;
	double s1 = 1.0 - s;
	return rcont1[i]
		+ s
		* (rcont2[i] + s1
			* (rcont3[i] + s * (rcont4[i] + s1 * rcont5[i])));
    }

    private static final double c2 = 0.2, c3 = 0.3, c4 = 0.8, c5 = 8.0 / 9.0,
	    a21 = 0.2, a31 = 3.0 / 40.0, a32 = 9.0 / 40.0, a41 = 44.0 / 45.0,
	    a42 = -56.0 / 15.0, a43 = 32.0 / 9.0, a51 = 19372.0 / 6561.0,
	    a52 = -25360.0 / 2187.0, a53 = 64448.0 / 6561.0,
	    a54 = -212.0 / 729.0, a61 = 9017.0 / 3168.0, a62 = -355.0 / 33.0,
	    a63 = 46732.0 / 5247.0, a64 = 49.0 / 176.0,
	    a65 = -5103.0 / 18656.0, a71 = 35.0 / 384.0, a73 = 500.0 / 1113.0,
	    a74 = 125.0 / 192.0, a75 = -2187.0 / 6784.0, a76 = 11.0 / 84.0,
	    e1 = 71.0 / 57600.0, e3 = -71.0 / 16695.0, e4 = 71.0 / 1920.0,
	    e5 = -17253.0 / 339200.0, e6 = 22.0 / 525.0, e7 = -1.0 / 40.0;

    public void dy(final double h, final DerivativeInf derivs) {
	double[] ytemp = new double[n];
	int i;
	for (i = 0; i < n; i++)
	    ytemp[i] = y[i] + h * a21 * dydx[i];
	derivs.derivs(x + c2 * h, ytemp, k2);
	for (i = 0; i < n; i++)
	    ytemp[i] = y[i] + h * (a31 * dydx[i] + a32 * k2[i]);
	derivs.derivs(x + c3 * h, ytemp, k3);
	for (i = 0; i < n; i++)
	    ytemp[i] = y[i] + h * (a41 * dydx[i] + a42 * k2[i] + a43 * k3[i]);
	derivs.derivs(x + c4 * h, ytemp, k4);
	for (i = 0; i < n; i++)
	    ytemp[i] = y[i] + h
		    * (a51 * dydx[i] + a52 * k2[i] + a53 * k3[i] + a54 * k4[i]);
	derivs.derivs(x + c5 * h, ytemp, k5);
	for (i = 0; i < n; i++)
	    ytemp[i] = y[i]
		    + h
		    * (a61 * dydx[i] + a62 * k2[i] + a63 * k3[i] + a64 * k4[i] + a65
			    * k5[i]);
	double xph = x + h;
	derivs.derivs(xph, ytemp, k6);
	for (i = 0; i < n; i++)
	    yout[i] = y[i]
		    + h
		    * (a71 * dydx[i] + a73 * k3[i] + a74 * k4[i] + a75 * k5[i] + a76
			    * k6[i]);
	derivs.derivs(xph, yout, dydxnew);
	for (i = 0; i < n; i++) {
	    yerr[i] = h
		    * (e1 * dydx[i] + e3 * k3[i] + e4 * k4[i] + e5 * k5[i] + e6
			    * k6[i] + e7 * dydxnew[i]);
	}
    }

    public double error() {
	double err = 0.0, sk;
	for (int i = 0; i < n; i++) {
	    sk = atol + rtol * max(abs(y[i]), abs(yout[i]));
	    err += SQR(yerr[i] / sk);
	}
	return sqrt(err / n);
    }

    private static final double d1 = -12715105075.0 / 11282082432.0,
	    d3 = 87487479700.0 / 32700410799.0,
	    d4 = -10690763975.0 / 1880347072.0,
	    d5 = 701980252875.0 / 199316789632.0,
	    d6 = -1453857185.0 / 822651844.0, d7 = 69997945.0 / 29380423.0;

    public void prepare_dense(final double h, final DerivativeInf derivs) {
	// double[] ytemp = new double[n];
	for (int i = 0; i < n; i++) {
	    rcont1[i] = y[i];
	    double ydiff = yout[i] - y[i];
	    rcont2[i] = ydiff;
	    double bspl = h * dydx[i] - ydiff;
	    rcont3[i] = bspl;
	    rcont4[i] = ydiff - h * dydxnew[i] - bspl;
	    rcont5[i] = h
		    * (d1 * dydx[i] + d3 * k3[i] + d4 * k4[i] + d5 * k5[i] + d6
			    * k6[i] + d7 * dydxnew[i]);
	}
    }

    @Override
    public void step(final double htry, final DerivativeInf derivs) {
	doubleW h = new doubleW(htry);
	for (;;) {
	    dy(h.val, derivs);
	    double err = error();
	    if (con.success(err, h))
		break;
	    if (abs(h.val) <= abs(x) * EPS)
		throw new IllegalArgumentException(
			"stepsize underflow in StepperDopr5");
	}
	if (dense)
	    prepare_dense(h.val, derivs);

	System.arraycopy(dydxnew, 0, dydx, 0, dydxnew.length);
	System.arraycopy(yout, 0, y, 0, y.length);
	// dydx=dydxnew;
	// y=yout;

	xold = x;
	x += (hdid = h.val);
	hnext = con.hnext;
    }
}
