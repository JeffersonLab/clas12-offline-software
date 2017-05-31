package com.nr.min;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;

import org.netlib.util.intW;

import com.nr.RealValueFun;
import com.nr.ran.Ranq1;

/**
 * Downhill simplex minimization with simulated annealing
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Amebsa {
    RealValueFun funk;
    final double ftol;
    private Ranq1 ran;
    public double yb;
    public int ndim;
    public double[] pb;
    public int mpts;
    public double[] y;
    public double[][] p;
    public double tt;

    public Amebsa(final double[] point, final double del,
	    final RealValueFun funkk, final double ftoll) {
	funk = funkk;
	ftol = ftoll;
	ran = new Ranq1(1234);
	yb = Double.MAX_VALUE;
	ndim = point.length;
	pb = new double[ndim];
	mpts = ndim + 1;
	y = new double[mpts];
	p = new double[mpts][ndim];

	for (int i = 0; i < mpts; i++) {
	    for (int j = 0; j < ndim; j++)
		p[i][j] = point[j];
	    if (i != 0)
		p[i][i - 1] += del;
	}
	inity();
    }

    public Amebsa(final double[] point, final double[] dels,
	    final RealValueFun funkk, final double ftoll) {
	funk = funkk;
	ftol = ftoll;
	ran = new Ranq1(1234);
	yb = Double.MAX_VALUE;
	ndim = point.length;
	pb = new double[ndim];
	mpts = ndim + 1;
	y = new double[mpts];
	p = new double[mpts][ndim];
	for (int i = 0; i < mpts; i++) {
	    for (int j = 0; j < ndim; j++)
		p[i][j] = point[j];
	    if (i != 0)
		p[i][i - 1] += dels[i - 1];
	}
	inity();
    }

    public Amebsa(final double[][] pp, final RealValueFun funkk,
	    final double ftoll) {
	funk = funkk;
	ftol = ftoll;
	ran = new Ranq1(1234);
	yb = Double.MAX_VALUE;
	ndim = pp[0].length;
	pb = new double[ndim];
	mpts = pp.length;
	y = new double[mpts];
	p = new double[pp.length][pp[0].length];
	copyAssign(p, pp);
	inity();
    }

    public void inity() {
	double[] x = new double[ndim];
	for (int i = 0; i < mpts; i++) {
	    for (int j = 0; j < ndim; j++)
		x[j] = p[i][j];
	    y[i] = funk.funk(x);
	}
    }

    public boolean anneal(final intW iter, final double temperature) { // int[]
								       // simulated
								       // pointer.
	double[] psum = new double[ndim];
	tt = -temperature;
	get_psum(p, psum);
	for (;;) {
	    int ilo = 0;
	    int ihi = 1;
	    double ylo = y[0] + tt * log(ran.doub());
	    double ynhi = ylo;
	    double[] yhi = new double[1];
	    yhi[0] = y[1] + tt * log(ran.doub());
	    if (ylo > yhi[0]) {
		ihi = 0;
		ilo = 1;
		ynhi = yhi[0];
		yhi[0] = ylo;
		ylo = ynhi;
	    }
	    for (int i = 3; i <= mpts; i++) {
		double yt = y[i - 1] + tt * log(ran.doub());
		if (yt <= ylo) {
		    ilo = i - 1;
		    ylo = yt;
		}
		if (yt > yhi[0]) {
		    ynhi = yhi[0];
		    ihi = i - 1;
		    yhi[0] = yt;
		} else if (yt > ynhi) {
		    ynhi = yt;
		}
	    }
	    double rtol = 2.0 * abs(yhi[0] - ylo) / (abs(yhi[0]) + abs(ylo));
	    if (rtol < ftol || iter.val < 0) {
		swap(y, 0, ilo);
		for (int n = 0; n < ndim; n++) {
		    // SWAP(p[0][n],p[ilo][n]);
		    double dum = p[0][n];
		    p[0][n] = p[ilo][n];
		    p[ilo][n] = dum;
		}
		if (rtol < ftol)
		    return true;
		else
		    return false;
	    }
	    iter.val -= 2;
	    double ytry = amotsa(p, y, psum, ihi, yhi, -1.0);
	    if (ytry <= ylo) {
		ytry = amotsa(p, y, psum, ihi, yhi, 2.0);
	    } else if (ytry >= ynhi) {
		double ysave = yhi[0];
		ytry = amotsa(p, y, psum, ihi, yhi, 0.5);
		if (ytry >= ysave) {
		    for (int i = 0; i < mpts; i++) {
			if (i != ilo) {
			    for (int j = 0; j < ndim; j++) {
				psum[j] = 0.5 * (p[i][j] + p[ilo][j]);
				p[i][j] = psum[j];
			    }
			    y[i] = funk.funk(psum);
			}
		    }
		    iter.val -= ndim;
		    get_psum(p, psum);
		}
	    } else
		++iter.val;
	}
    }

    public void get_psum(final double[][] p, final double[] psum) {
	for (int n = 0; n < ndim; n++) {
	    double sum = 0.0;
	    for (int m = 0; m < mpts; m++)
		sum += p[m][n];
	    psum[n] = sum;
	}
    }

    public double amotsa(final double[][] p, final double[] y,
	    final double[] psum, final int ihi, final double[] yhi,
	    final double fac) {
	double[] ptry = new double[ndim];
	double fac1 = (1.0 - fac) / ndim;
	double fac2 = fac1 - fac;
	for (int j = 0; j < ndim; j++)
	    ptry[j] = psum[j] * fac1 - p[ihi][j] * fac2;
	double ytry = funk.funk(ptry);
	if (ytry <= yb) {
	    for (int j = 0; j < ndim; j++)
		pb[j] = ptry[j];
	    yb = ytry;
	}
	double yflu = ytry - tt * log(ran.doub());
	if (yflu < yhi[0]) {
	    y[ihi] = ytry;
	    yhi[0] = yflu;
	    for (int j = 0; j < ndim; j++) {
		psum[j] += ptry[j] - p[ihi][j];
		p[ihi][j] = ptry[j];
	    }
	}
	return yflu;
    }
}
