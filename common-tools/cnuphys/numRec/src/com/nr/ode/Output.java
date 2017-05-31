package com.nr.ode;

import static com.nr.NRUtil.*;

/**
 * Structure for output from ODE solver such as odeint.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Output {
    public int kmax;
    public int nvar;
    public int nsave;
    public boolean dense;
    public int count;
    public double x1, x2, xout, dxout;
    public double[] xsave;
    public double[][] ysave;

    public Output() {
	kmax = -1;
	dense = false;
	count = 0;
    }

    /**
     * Constructor provides dense output at nsave equally spaced intervals. If
     * nsave <= 0, output is saved only at the actual integration steps.
     */
    public Output(final int nsavee) {
	kmax = 500;
	nsave = nsavee;
	count = 0;
	xsave = new double[kmax];
	dense = nsave > 0 ? true : false;
    }

    /**
     * Called by Odeint constructor, which passes neqn, the number of equations,
     * xlo, the starting point of the integration, and xhi, the ending point.
     * 
     * @param neqn
     * @param xlo
     * @param xhi
     */
    public void init(final int neqn, final double xlo, final double xhi) {
	nvar = neqn;
	if (kmax == -1)
	    return;
	ysave = new double[nvar][kmax];
	if (dense) {
	    x1 = xlo;
	    x2 = xhi;
	    xout = x1;
	    dxout = (x2 - x1) / nsave;
	}
    }

    public void resize() {
	int kold = kmax;
	kmax *= 2;
	double[] tempvec = buildVector(xsave);
	xsave = new double[kmax];
	for (int k = 0; k < kold; k++)
	    xsave[k] = tempvec[k];
	double[][] tempmat = buildMatrix(ysave);
	ysave = new double[nvar][kmax];
	for (int i = 0; i < nvar; i++)
	    for (int k = 0; k < kold; k++)
		ysave[i][k] = tempmat[i][k];
    }

    /**
     * Invokes dense_out function of stepper routine to produce output at xout.
     * Normally called by out rather than directly. Assumes that xout is between
     * xold and xold+h, where the stepper must keep track of xold, the location
     * of the previous step, and x=xold+h, the current step.
     * 
     * @param s
     * @param xout
     * @param h
     */
    public void save_dense(final StepperBase s, final double xout,
	    final double h) {
	if (count == kmax)
	    resize();
	for (int i = 0; i < nvar; i++)
	    ysave[i][count] = s.dense_out(i, xout, h);
	xsave[count++] = xout;
    }

    public void save(final double x, final double[] y) {
	if (kmax <= 0)
	    return;
	if (count == kmax)
	    resize();
	for (int i = 0; i < nvar; i++)
	    ysave[i][count] = y[i];
	xsave[count++] = x;
    }

    public void out(final int nstp, final double x, final double[] y,
	    final StepperBase s, final double h) {
	if (!dense)
	    throw new IllegalArgumentException(
		    "dense output not set in Output!");
	if (nstp == -1) {
	    save(x, y);
	    xout += dxout;
	} else {
	    while ((x - xout) * (x2 - x1) > 0.0) {
		save_dense(s, xout, h);
		xout += dxout;
	    }
	}
    }
}
