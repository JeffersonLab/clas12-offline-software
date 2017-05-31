package com.nr.fi;

import static com.nr.NRUtil.*;
import static com.nr.fi.GaussianWeights.*;
import static java.lang.Math.*;
import com.nr.RealValueFun;
import com.nr.UniVarRealValueFun;

public abstract class Stiel {
    int j, n;
    double aa, bb, hmax;
    double[] a, b;
    Quadrature s1, s2;
    pp ppfunc = new pp();
    ppx ppxfunc = new ppx();

    class pp implements UniVarRealValueFun, RealValueFun {
	@Override
	public double funk(final double[] x) {
	    double pval = p(x[0]);
	    return pval * wt1(x[0], x[1]) * pval;
	}

	@Override
	public double funk(final double t) {
	    double x = fx(t);
	    double pval = p(x);
	    return pval * wt2(x) * fdxdt(t) * pval;
	}
    }

    class ppx implements UniVarRealValueFun, RealValueFun {
	@Override
	public double funk(final double[] x) {
	    return ppfunc.funk(new double[] { x[0], x[1] }) * x[0];
	}

	@Override
	public double funk(final double t) {
	    return ppfunc.funk(t) * fx(t);
	}
    }

    double p(final double x) {
	double pval = 0, pj, pjm1;
	if (j == 0)
	    return 1.0;
	else {
	    pjm1 = 0.0;
	    pj = 1.0;
	    for (int i = 0; i < j; i++) {
		pval = (x - a[i]) * pj - b[i] * pjm1;
		pjm1 = pj;
		pj = pval;
	    }
	}
	return pval;
    }

    public Stiel(final int nn, final double aaa, final double bbb,
	    final double hmaxx) {
	n = nn;
	aa = aaa;
	bb = bbb;
	hmax = hmaxx;
	a = new double[nn];
	b = new double[nn];
	s1 = new DErule(ppfunc, aa, bb, hmax);
	s2 = new DErule(ppxfunc, aa, bb, hmax);
    }

    public Stiel(final int nn, final double aaa, final double bbb) {
	n = nn;
	aa = aaa;
	bb = bbb;
	a = new double[nn];
	b = new double[nn];
	s1 = new Trapzd(ppfunc, aa, bb);
	s2 = new Trapzd(ppxfunc, aa, bb);
    }

    public double quad(final Quadrature s) {
	final double EPS = 3.0e-11, MACHEPS = DBL_EPSILON;
	final int NMAX = 11;
	double olds = 0, sum;
	s.n = 0;
	for (int i = 1; i <= NMAX; i++) {
	    sum = s.next();
	    if (i > 3)
		if (abs(sum - olds) <= EPS * abs(olds))
		    return sum;
	    if (i == NMAX)
		if (abs(sum) <= MACHEPS && abs(olds) <= MACHEPS)
		    return 0.0;
	    olds = sum;
	}
	throw new IllegalArgumentException("no convergence in quad");
    }

    public void get_weights(final double[] x, final double[] w) {
	double amu0, c, oldc = 1.0;
	if (n != x.length)
	    throw new IllegalArgumentException("bad array size in Stiel");
	for (int i = 0; i < n; i++) {
	    j = i;
	    c = quad(s1);
	    b[i] = c / oldc;
	    a[i] = quad(s2) / c;
	    oldc = c;
	}
	amu0 = b[0];
	gaucof(a, b, amu0, x, w);
    }

    public double wt1(final double x, final double del) {
	return -9999;
    }

    public double wt2(final double x) {
	return -9999;
    }

    public double fx(final double t) {
	return -9999;
    }

    public double fdxdt(final double t) {
	return -9999;
    }

}
