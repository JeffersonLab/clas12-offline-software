package com.nr.bvp;

import com.nr.RealMultiValueFun;
import com.nr.ode.DerivativeInf;
import com.nr.ode.Odeint;
import com.nr.ode.Output;
import com.nr.ode.StepperDopr853;

public abstract class Shoot implements RealMultiValueFun {
    int nvar;
    double x1, x2;
    double atol, rtol;
    double h1, hmin;
    double[] y;
    DerivativeInf d;

    public Shoot(final int nvarr, final double xx1, final double xx2,
	    final DerivativeInf derivss) {
	nvar = nvarr;
	x1 = xx1;
	x2 = xx2;
	d = derivss;
	atol = 1.0e-14;
	rtol = atol;
	hmin = 0.0;
	y = new double[nvar];
    }

    @Override
    public double[] funk(final double[] v) {
	h1 = (x2 - x1) / 100.0;
	y = load(x1, v);
	Output out = new Output();
	StepperDopr853 s = new StepperDopr853();
	Odeint integ = new Odeint(y, x1, x2, atol, rtol, h1, hmin, out, d, s);
	integ.integrate();
	return score(x2, y);
    }

    public abstract double[] load(final double x, final double[] v);

    public abstract double[] score(final double x, final double[] v);
}
