package com.nr.ode;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;

/**
 * Driver for ODE solvers with adaptive stepsize control. The template parameter
 * should be one of the derived classes of StepperBase defining a particular
 * integration algorithm.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 * 
 */
public class Odeint {
    static final int MAXSTP = 50000;
    double EPS;
    int nok;
    int nbad;
    int nvar;
    double x1, x2, hmin;

    /**
     * true if dense output requested by out
     * 
     */
    boolean dense;
    double[] ystart;
    Output out;

    /**
     * Get the type of derivs from the stepper
     */
    DerivativeInf derivs;
    StepperBase s;
    int nstp;
    double h;

    /*
     * XXX ref to StepperBase double[] y,dydx; double x;
     */
    /**
     * Constructor sets everything up. The routine integrates starting values
     * ystart[0..nvar-1] from xx1 to xx2 with absolute tolerance atol and
     * relative tolerance rtol. The quantity h1 should be set as a guessed first
     * stepsize, hmin as the minimum allowed stepsize (can be zero). An Output
     * object out should be input to control the saving of intermediate values.
     * On output, nok and nbad are the number of good and bad (but retried and
     * fixed) steps taken, and ystart is replaced by values at the end of the
     * integration interval. derivs is the user-supplied routine (function or
     * functor) for calculating the right-hand side derivative.
     * 
     * @param ystartt
     * @param xx1
     * @param xx2
     * @param atol
     * @param rtol
     * @param h1
     * @param hminn
     * @param outt
     * @param derivss
     * @param s
     */
    public Odeint(final double[] ystartt, final double xx1, final double xx2,
	    final double atol, final double rtol, final double h1,
	    final double hminn, final Output outt, final DerivativeInf derivss,
	    final StepperBase s) {
	nvar = ystartt.length;
	ystart = ystartt;
	double[] y = new double[nvar];
	double[] dydx = new double[nvar];
	double x = xx1;
	nok = 0;
	nbad = 0;
	x1 = xx1;
	x2 = xx2;
	hmin = hminn;
	dense = outt.dense;
	out = outt;
	derivs = derivss;

	this.s = s;
	s.setParam(y, dydx, x, atol, rtol, dense);

	EPS = DBL_EPSILON;
	h = SIGN(h1, x2 - x1);
	for (int i = 0; i < nvar; i++)
	    y[i] = ystart[i];
	out.init(s.neqn, x1, x2);
    }

    public void integrate() {
	derivs.derivs(s.x, s.y, s.dydx);
	if (dense)
	    out.out(-1, s.x, s.y, s, h);
	else
	    out.save(s.x, s.y);
	for (nstp = 0; nstp < MAXSTP; nstp++) {
	    if ((s.x + h * 1.0001 - x2) * (x2 - x1) > 0.0)
		h = x2 - s.x;
	    s.step(h, derivs);
	    if (s.hdid == h)
		++nok;
	    else
		++nbad;
	    if (dense)
		out.out(nstp, s.x, s.y, s, s.hdid);
	    else
		out.save(s.x, s.y);
	    if ((s.x - x2) * (x2 - x1) >= 0.0) {
		for (int i = 0; i < nvar; i++)
		    ystart[i] = s.y[i];
		if (out.kmax > 0
			&& abs(out.xsave[out.count - 1] - x2) > 100.0 * abs(x2)
				* EPS)
		    out.save(s.x, s.y);
		return;
	    }
	    if (abs(s.hnext) <= hmin)
		throw new IllegalArgumentException(
			"Step size too small in Odeint");
	    h = s.hnext;
	}
	throw new IllegalArgumentException("Too many steps in routine Odeint");
    }

    public static void rk4(final double[] y, final double[] dydx,
	    final double x, final double h, final double[] yout,
	    final DerivativeInf derivs) {
	int n = y.length;
	double[] dym = new double[n], dyt = new double[n], yt = new double[n];
	double hh = h * 0.5;
	double h6 = h / 6.0;
	double xh = x + hh;
	for (int i = 0; i < n; i++)
	    yt[i] = y[i] + hh * dydx[i];
	derivs.derivs(xh, yt, dyt);
	for (int i = 0; i < n; i++)
	    yt[i] = y[i] + hh * dyt[i];
	derivs.derivs(xh, yt, dym);
	for (int i = 0; i < n; i++) {
	    yt[i] = y[i] + h * dym[i];
	    dym[i] += dyt[i];
	}
	derivs.derivs(x + h, yt, dyt);
	for (int i = 0; i < n; i++)
	    yout[i] = y[i] + h6 * (dydx[i] + dyt[i] + 2.0 * dym[i]);
    }
}
