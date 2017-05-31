package com.nr.interp;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;

import org.netlib.util.doubleW;

import com.nr.UniVarRealValueFun;
import com.nr.la.LUdcmp;

/**
 * Object for interpolation by kriging, using npt points in ndim dimensions.
 * Call constructor once, then interp as many times as desired.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 * 
 */
public class Krig {
    final double[][] x;
    final UniVarRealValueFun vgram;
    int ndim, npt;
    double lastval, lasterr;
    double[] y, dstar, vstar, yvi;
    double[][] v;
    LUdcmp vi;

    public Krig(final double[][] xx, final double[] yy,
	    final UniVarRealValueFun vargram) {
	this(xx, yy, vargram, null);
    }

    /**
     * The npt ndim matrix xx inputs the data points, the vector yy the function
     * values. vargram is the variogram function or functor. The argument err is
     * not used for interpolation;
     * 
     * @param xx
     * @param yy
     * @param vargram
     * @param err
     */
    public Krig(final double[][] xx, final double[] yy,
	    final UniVarRealValueFun vargram, final double[] err) {
	x = xx;
	y = yy;
	vgram = vargram;
	npt = xx.length;
	ndim = xx[0].length;
	dstar = new double[npt + 1];
	vstar = new double[npt + 1];
	v = new double[npt + 1][npt + 1];
	y = new double[npt + 1];
	yvi = new double[npt + 1];

	int i, j;
	for (i = 0; i < npt; i++) {
	    y[i] = yy[i];
	    for (j = i; j < npt; j++) {
		v[i][j] = v[j][i] = vgram.funk(rdist(x[i], x[j]));
	    }
	    v[i][npt] = v[npt][i] = 1.;
	}
	v[npt][npt] = y[npt] = 0.;
	if (err != null)
	    for (i = 0; i < npt; i++)
		v[i][i] -= SQR(err[i]);
	vi = new LUdcmp(v);
	vi.solve(y, yvi);
    }

    /**
     * Return an interpolated value at the point xstar.
     * 
     * @param xstar
     * @return
     */
    public double interp(final double[] xstar) {
	int i;
	for (i = 0; i < npt; i++)
	    vstar[i] = vgram.funk(rdist(xstar, x[i]));
	vstar[npt] = 1.;
	lastval = 0.;
	for (i = 0; i <= npt; i++)
	    lastval += yvi[i] * vstar[i];
	return lastval;
    }

    /**
     * Return an interpolated value at the point xstar, and return its estimated
     * error as esterr.
     * 
     * @param xstar
     * @param esterr
     * @return
     */
    public double interp(final double[] xstar, final doubleW esterr) {
	lastval = interp(xstar);
	vi.solve(vstar, dstar);
	lasterr = 0;
	for (int i = 0; i <= npt; i++)
	    lasterr += dstar[i] * vstar[i];
	esterr.val = lasterr = sqrt(max(0., lasterr));
	return lastval;
    }

    public double rdist(final double[] x1, final double[] x2) {
	double d = 0.;
	for (int i = 0; i < ndim; i++)
	    d += SQR(x1[i] - x2[i]);
	return sqrt(d);
    }
}
