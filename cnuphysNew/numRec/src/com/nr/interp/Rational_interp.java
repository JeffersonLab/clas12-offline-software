package com.nr.interp;

import static java.lang.Math.*;

/**
 * Rational Function Interpolation and Extrapolation
 * 
 * Given a value x, and using pointers to data xx and yy, this routine returns
 * an interpolated value y, and stores an error estimate dy. The returned value
 * is obtained by mm-point polynomial interpolation on the subrange
 * xx[jl..jl+mm-1].
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 * 
 */
public class Rational_interp extends Base_interp {
    public double dy;

    public Rational_interp(final double[] xv, final double[] yv, final int m) {
	super(xv, yv, m);
	dy = 0.;
    }

    /**
     * Given a value x, and using pointers to data xx and yy, this routine
     * returns an interpolated value y, and stores an error estimate dy. The
     * returned value is obtained by mm-point diagonal rational function
     * interpolation on the subrange xx[jl..jl+mm-1].
     */
    @Override
    public double rawinterp(final int jl, final double x) {
	final double TINY = 1.0e-99;
	int m, i, ns = 0;
	double y, w, t, hh, h, dd;
	// const double *xa = &xx[jl], *ya = &yy[jl];
	double[] c = new double[mm];
	double[] d = new double[mm];
	hh = abs(x - xx[jl + 0]);
	for (i = 0; i < mm; i++) {
	    h = abs(x - xx[jl + i]);
	    if (h == 0.0) {
		dy = 0.0;
		return yy[jl + i];
	    } else if (h < hh) {
		ns = i;
		hh = h;
	    }
	    c[i] = yy[jl + i];
	    d[i] = yy[jl + i] + TINY;
	}
	y = yy[jl + ns--];
	for (m = 1; m < mm; m++) {
	    for (i = 0; i < mm - m; i++) {
		w = c[i + 1] - d[i];
		h = xx[jl + i + m] - x;
		t = (xx[jl + i] - x) * d[i] / h;
		dd = t - c[i + 1];
		if (dd == 0.0)
		    throw new IllegalArgumentException(
			    "Error in routine ratint");
		dd = w / dd;
		d[i] = c[i + 1] * dd;
		c[i] = t * dd;
	    }
	    y += (dy = (2 * (ns + 1) < (mm - m) ? c[ns + 1] : d[ns--]));
	}
	return y;
    }

}
