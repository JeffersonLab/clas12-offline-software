package com.nr.interp;

import static java.lang.Math.*;

/**
 * Abstract base class used by all interpolation routines in this chapter. Only
 * the routine interp is called directly by the user.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public abstract class Base_interp {
    int n, mm, jsav, cor, dj;
    double[] xx, yy;

    /**
     * Set up for interpolating on a table of x's and y’s of length m. Normally
     * called by a derived class, not by the user.
     * 
     * @param x
     * @param y
     * @param m
     */
    public Base_interp(final double[] x, final double[] y, final int m) {
	n = x.length;
	mm = m;
	jsav = 0;
	cor = 0;
	xx = x;
	yy = y;

	dj = max(1, (int) pow(n, 0.25));
    }

    public double interp(final double x) {
	int jlo = (cor != 0) ? hunt(x) : locate(x);
	return rawinterp(jlo, x);
    }

    public abstract double rawinterp(int jlo, double x);

    /**
     * Given a value x, return a value j such that x is (insofar as possible)
     * centered in the subrange xx[j..j+mm-1], where xx is the stored pointer.
     * The values in xx must be monotonic, either increasing or decreasing. The
     * returned value is not less than 0, nor greater than n-1.
     * 
     * @param x
     * @return
     */
    public int locate(final double x) {
	int ju, jm, jl;
	if (n < 2 || mm < 2 || mm > n)
	    throw new IllegalArgumentException("locate size error");
	boolean ascnd = (xx[n - 1] >= xx[0]);
	jl = 0;
	ju = n - 1;
	while (ju - jl > 1) {
	    jm = (ju + jl) >> 1;
	    if (x >= xx[jm] == ascnd)
		jl = jm;
	    else
		ju = jm;
	}
	cor = abs(jl - jsav) > dj ? 0 : 1;
	jsav = jl;
	return max(0, min(n - mm, jl - ((mm - 2) >> 1)));
    }

    /**
     * Given a value x, return a value j such that x is (insofar as possible)
     * centered in the subrange xx[j..j+mm-1], where xx is the stored pointer.
     * The values in xx must be monotonic, either increasing or decreasing. The
     * returned value is not less than 0, nor greater than n-1.
     * 
     * @param x
     * @return
     */
    public int hunt(final double x) {
	int jl = jsav, jm, ju, inc = 1;
	if (n < 2 || mm < 2 || mm > n)
	    throw new IllegalArgumentException("hunt size error");
	boolean ascnd = (xx[n - 1] >= xx[0]);
	if (jl < 0 || jl > n - 1) {
	    jl = 0;
	    ju = n - 1;
	} else {
	    if (x >= xx[jl] == ascnd) {
		for (;;) {
		    ju = jl + inc;
		    if (ju >= n - 1) {
			ju = n - 1;
			break;
		    } else if (x < xx[ju] == ascnd)
			break;
		    else {
			jl = ju;
			inc += inc;
		    }
		}
	    } else {
		ju = jl;
		for (;;) {
		    jl = jl - inc;
		    if (jl <= 0) {
			jl = 0;
			break;
		    } else if (x >= xx[jl] == ascnd)
			break;
		    else {
			ju = jl;
			inc += inc;
		    }
		}
	    }
	}
	while (ju - jl > 1) {
	    jm = (ju + jl) >> 1;
	    if (x >= xx[jm] == ascnd)
		jl = jm;
	    else
		ju = jm;
	}
	cor = abs(jl - jsav) > dj ? 0 : 1;
	jsav = jl;
	return max(0, min(n - mm, jl - ((mm - 2) >> 1)));
    }
}
