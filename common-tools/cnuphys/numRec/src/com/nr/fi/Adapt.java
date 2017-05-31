package com.nr.fi;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;
import com.nr.UniVarRealValueFun;

/**
 * adaptive quadrature
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Adapt {
    final double alpha = sqrt(2.0 / 3.0);
    final double beta = 1.0 / sqrt(5.0);
    final double x1 = 0.942882415695480;
    final double x2 = 0.641853342345781;
    final double x3 = 0.236383199662150;
    final double[] x = new double[] { 0, -x1, -alpha, -x2, -beta, -x3, 0.0, x3,
	    beta, x2, alpha, x1 };
    // static final double alpha,beta,x1,x2,x3,x[12];

    double TOL, toler;

    boolean terminate, out_of_tolerance;

    public Adapt(final double tol) {
	TOL = tol;
	terminate = true;
	out_of_tolerance = false;

	final double EPS = DBL_EPSILON;
	if (TOL < 10.0 * EPS)
	    TOL = 10.0 * EPS;
    }

    public double integrate(final UniVarRealValueFun func, final double a,
	    final double b) {
	double m, h, fa, fb, i1, i2, is, erri1, erri2, r;
	double[] y = new double[13];
	m = 0.5 * (a + b);
	h = 0.5 * (b - a);
	fa = y[0] = func.funk(a);
	fb = y[12] = func.funk(b);
	for (int i = 1; i < 12; i++)
	    y[i] = func.funk(m + x[i] * h);
	i2 = (h / 6.0) * (y[0] + y[12] + 5.0 * (y[4] + y[8]));
	i1 = (h / 1470.0)
		* (77.0 * (y[0] + y[12]) + 432.0 * (y[2] + y[10]) + 625.0
			* (y[4] + y[8]) + 672.0 * y[6]);
	is = h
		* (0.0158271919734802 * (y[0] + y[12]) + 0.0942738402188500
			* (y[1] + y[11]) + 0.155071987336585 * (y[2] + y[10])
			+ 0.188821573960182 * (y[3] + y[9]) + 0.199773405226859
			* (y[4] + y[8]) + 0.224926465333340 * (y[5] + y[7]) + 0.242611071901408 * y[6]);
	erri1 = abs(i1 - is);
	erri2 = abs(i2 - is);
	r = (erri2 != 0.0) ? erri1 / erri2 : 1.0;
	toler = (r > 0.0 && r < 1.0) ? TOL / r : TOL;
	if (is == 0.0)
	    is = b - a;
	is = abs(is);
	return adaptlob(func, a, b, fa, fb, is);
    }

    public double adaptlob(final UniVarRealValueFun func, final double a,
	    final double b, final double fa, final double fb, final double is) {
	double m, h, mll, ml, mr, mrr, fmll, fml, fm, fmrr, fmr, i1, i2;
	m = 0.5 * (a + b);
	h = 0.5 * (b - a);
	mll = m - alpha * h;
	ml = m - beta * h;
	mr = m + beta * h;
	mrr = m + alpha * h;
	fmll = func.funk(mll);
	fml = func.funk(ml);
	fm = func.funk(m);
	fmr = func.funk(mr);
	fmrr = func.funk(mrr);
	i2 = h / 6.0 * (fa + fb + 5.0 * (fml + fmr));
	i1 = h
		/ 1470.0
		* (77.0 * (fa + fb) + 432.0 * (fmll + fmrr) + 625.0
			* (fml + fmr) + 672.0 * fm);
	if (abs(i1 - i2) <= toler * is || mll <= a || b <= mrr) {
	    if ((mll <= a || b <= mrr) && terminate) {
		out_of_tolerance = true;
		terminate = false;
	    }
	    return i1;
	} else
	    return adaptlob(func, a, mll, fa, fmll, is)
		    + adaptlob(func, mll, ml, fmll, fml, is)
		    + adaptlob(func, ml, m, fml, fm, is)
		    + adaptlob(func, m, mr, fm, fmr, is)
		    + adaptlob(func, mr, mrr, fmr, fmrr, is)
		    + adaptlob(func, mrr, b, fmrr, fb, is);
    }
}
