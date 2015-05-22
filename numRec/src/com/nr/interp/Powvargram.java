package com.nr.interp;

import static java.lang.Math.*;
import static com.nr.NRUtil.*;
import com.nr.UniVarRealValueFun;

/**
 * Functor for variogram v(r)=ar^b, where b is specified, a is fitted from the
 * data.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 * 
 */
public class Powvargram implements UniVarRealValueFun {
    private double alph, bet, nugsq;

    public Powvargram(final double[][] x, final double[] y) {
	this(x, y, 1.5, 0.);
    }

    public Powvargram(final double[][] x, final double[] y, final double beta,
	    final double nug) {
	bet = beta;
	nugsq = nug * nug;
	int i, j, k, npt = x.length, ndim = x[0].length;
	double rb, num = 0., denom = 0.;
	for (i = 0; i < npt; i++)
	    for (j = i + 1; j < npt; j++) {
		rb = 0.;
		for (k = 0; k < ndim; k++)
		    rb += SQR(x[i][k] - x[j][k]);
		rb = pow(rb, 0.5 * beta);
		num += rb * (0.5 * SQR(y[i] - y[j]) - nugsq);
		denom += SQR(rb);
	    }
	alph = num / denom;
    }

    @Override
    public double funk(final double r) {
	return nugsq + alph * pow(r, bet);
    }
}
