package com.nr.model;

import static com.nr.NRUtil.SQR;
import static java.lang.Math.exp;

import org.netlib.util.doubleW;

public class FGauss implements MultiFuncd {

    @Override
    public void funk(final double x, final double[] a, final doubleW y,
	    final double[] dyda) {
	int i, na = a.length;
	double fac, ex, arg;
	y.val = 0.;
	for (i = 0; i < na - 1; i += 3) {
	    arg = (x - a[i + 1]) / a[i + 2];
	    ex = exp(-SQR(arg));
	    fac = a[i] * ex * 2. * arg;
	    y.val += a[i] * ex;
	    dyda[i] = ex;
	    dyda[i + 1] = fac / a[i + 2];
	    dyda[i + 2] = fac * arg / a[i + 2];
	}
    }
}
