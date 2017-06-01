package com.nr.fe;

import static java.lang.Math.*;

import org.netlib.util.doubleW;

import com.nr.UniVarRealValueFun;

public class Dfridr {
    private Dfridr() {
    }

    /**
     * Returns the derivative of a function func at a point x by Ridders' method
     * of polynomial extrap- olation. The value h is input as an estimated
     * initial stepsize; it need not be small, but rather should be an increment
     * in x over which func changes substantially. An estimate of the error in
     * the derivative is returned as err.
     * 
     * @param func
     * @param x
     * @param h
     * @param err
     * @return
     */
    public static double dfridr(final UniVarRealValueFun func, final double x,
	    final double h, final doubleW err) {
	final int ntab = 10;
	final double con = 1.4, con2 = (con * con);
	final double big = Double.MAX_VALUE;
	final double safe = 2.0;
	int i, j;
	double errt, fac, hh, ans = 0;
	double[][] a = new double[ntab][ntab];
	if (h == 0.0)
	    throw new IllegalArgumentException("h must be nonzero in dfridr.");
	hh = h;
	a[0][0] = (func.funk(x + hh) - func.funk(x - hh)) / (2.0 * hh);
	err.val = big;
	for (i = 1; i < ntab; i++) {
	    hh /= con;
	    a[0][i] = (func.funk(x + hh) - func.funk(x - hh)) / (2.0 * hh);
	    fac = con2;
	    for (j = 1; j <= i; j++) {
		a[j][i] = (a[j - 1][i] * fac - a[j - 1][i - 1]) / (fac - 1.0);
		fac = con2 * fac;
		errt = max(abs(a[j][i] - a[j - 1][i]), abs(a[j][i]
			- a[j - 1][i - 1]));
		if (errt <= err.val) {
		    err.val = errt;
		    ans = a[j][i];
		}
	    }
	    if (abs(a[i][i] - a[i - 1][i - 1]) >= safe * err.val)
		break;
	}
	return ans;
    }
}
