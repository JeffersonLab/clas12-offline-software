package com.nr.interp;

import static java.lang.Math.*;

/**
 * 
 * Polynomial Interpolation and Extrapolation interpolation routines for one
 * dimension Copyright (C) Numerical Recipes Software 1986-2007 Java translation
 * Copyright (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Poly_interp extends Base_interp {

    public double dy;

    public Poly_interp(final double[] xv, final double[] yv, final int m) {
	super(xv, yv, m);
	dy = 0.;
    }

    /**
     * Given a value x, and using pointers to data xx and yy, this routine
     * returns an interpolated value y, and stores an error estimate dy. The
     * returned value is obtained by mm-point polynomial interpolation on the
     * subrange xx[jl..jl+mm-1].
     */
    @Override
    public double rawinterp(final int jl, final double x) {
	int i, m, ns = 0;
	double y, den, dif, dift, ho, hp, w;
	// double *xa = &xx[jl], *ya = &yy[jl];
	double[] c = new double[mm];
	double[] d = new double[mm];
	dif = abs(x - xx[jl + 0]);
	for (i = 0; i < mm; i++) {
	    if ((dift = abs(x - xx[jl + i])) < dif) {
		ns = i;
		dif = dift;
	    }
	    c[i] = yy[jl + i];
	    d[i] = yy[jl + i];
	}
	y = yy[jl + ns--];
	for (m = 1; m < mm; m++) {
	    for (i = 0; i < mm - m; i++) {
		ho = xx[jl + i] - x;
		hp = xx[jl + i + m] - x;
		w = c[i + 1] - d[i];
		if ((den = ho - hp) == 0.0)
		    throw new IllegalArgumentException("Poly_interp error");
		den = w / den;
		d[i] = hp * den;
		c[i] = ho * den;
	    }
	    y += (dy = (2 * (ns + 1) < (mm - m) ? c[ns + 1] : d[ns--]));
	}
	return y;
    }

}
