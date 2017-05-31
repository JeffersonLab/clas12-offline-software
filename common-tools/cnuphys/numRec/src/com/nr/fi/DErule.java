package com.nr.fi;

import static java.lang.Math.*;
import static com.nr.NRUtil.*;
import com.nr.RealValueFun;

/**
 * Structure for implementing the DE (double exponential) rule.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class DErule extends Quadrature {
    double a, b, hmax, s;
    RealValueFun func;

    public DErule(final RealValueFun funcc, final double aa, final double bb) {
	this(funcc, aa, bb, 3.7);
    }

    /**
     * 
     * @param funcc
     * @param aa
     * @param bb
     * @param hmaxx
     */
    public DErule(final RealValueFun funcc, final double aa, final double bb,
	    final double hmaxx) {
	func = funcc;
	a = aa;
	b = bb;
	hmax = hmaxx;
	n = 0;
    }

    /**
     * On the first call to the function next (n D 1), the routine returns the
     * crudest estimate of S(a,b)f(x)dx.. Subsequent calls to next (n = 2,3,...)
     * will improve the accuracy by adding 2^(n-1) additional interior points.
     */
    @Override
    public double next() {
	double del, fact, q, sum, t, twoh;
	int it, j;
	n++;
	if (n == 1) {
	    fact = 0.25;
	    return s = hmax * 2.0 * (b - a) * fact
		    * func.funk(new double[] { 0.5 * (b + a), 0.5 * (b - a) });
	} else {
	    for (it = 1, j = 1; j < n - 1; j++)
		it <<= 1;
	    twoh = hmax / it;
	    t = 0.5 * twoh;
	    for (sum = 0.0, j = 0; j < it; j++) {
		q = exp(-2.0 * sinh(t));
		del = (b - a) * q / (1.0 + q);
		fact = q / SQR(1.0 + q) * cosh(t);
		sum += fact
			* (func.funk(new double[] { a + del, del }) + func
				.funk(new double[] { b - del, del }));
		t += twoh;
	    }
	    return s = 0.5 * s + (b - a) * twoh * sum;
	}
    }
}
