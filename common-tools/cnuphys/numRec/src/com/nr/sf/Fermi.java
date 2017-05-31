package com.nr.sf;

import static java.lang.Math.*;
import com.nr.RealValueFun;
import com.nr.UniVarRealValueFun;
import com.nr.fi.DErule;
import com.nr.fi.Trapzd;

/**
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Fermi implements UniVarRealValueFun, RealValueFun {
    double kk, etaa, thetaa;

    public Fermi() {
    }

    public double get(final double t) {
	double x;
	x = exp(t - exp(-t));
	return x * (1.0 + exp(-t)) * pow(x, kk) * sqrt(1.0 + thetaa * 0.5 * x)
		/ (exp(x - etaa) + 1.0);
    }

    @Override
    public double funk(final double t) {
	return get(t);
    }

    @Override
    public double funk(final double[] x) {
	return get(x[0], x[1]);
    }

    public double get(final double x, final double del) {
	if (x < 1.0)
	    return pow(del, kk) * sqrt(1.0 + thetaa * 0.5 * x)
		    / (exp(x - etaa) + 1.0);
	else
	    return pow(x, kk) * sqrt(1.0 + thetaa * 0.5 * x)
		    / (exp(x - etaa) + 1.0);
    }

    public double val(final double k, final double eta, final double theta) {
	final double EPS = 3.0e-9;
	final int NMAX = 11;
	double a, aa, b, bb, hmax, olds = 0, sum;
	kk = k;
	etaa = eta;
	thetaa = theta;
	if (eta <= 15.0) {
	    a = -4.5;
	    b = 5.0;
	    Trapzd s = new Trapzd(this, a, b);
	    for (int i = 1; i <= NMAX; i++) {
		sum = s.next();
		if (i > 3)
		    if (abs(sum - olds) <= EPS * abs(olds))
			return sum;
		olds = sum;
	    }
	} else {
	    a = 0.0;
	    b = eta;
	    aa = eta;
	    bb = eta + 60.0;
	    hmax = 4.3;
	    DErule s = new DErule(this, a, b, hmax);
	    DErule ss = new DErule(this, aa, bb, hmax);
	    for (int i = 1; i <= NMAX; i++) {
		sum = s.next() + ss.next();
		if (i > 3)
		    if (abs(sum - olds) <= EPS * abs(olds))
			return sum;
		olds = sum;
	    }
	}
	throw new IllegalArgumentException("no convergence in fermi");
    }
}
