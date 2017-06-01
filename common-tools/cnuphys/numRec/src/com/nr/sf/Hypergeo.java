package com.nr.sf;

import com.nr.Complex;
import com.nr.ode.Hypderiv;
import com.nr.ode.Odeint;
import com.nr.ode.Output;
import com.nr.ode.StepperBS;

/**
 * Hypergeometric Functions
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Hypergeo {
    private Complex series, deriv;

    private Hypergeo() {
    }

    private void hypser(final Complex a, final Complex b, final Complex c,
	    final Complex z) {
	deriv = new Complex(0.0, 0.0);
	Complex fac = new Complex(1.0, 0.0);
	Complex temp = fac;
	Complex aa = a;
	Complex bb = b;
	Complex cc = c;
	for (int n = 1; n <= 1000; n++) {
	    fac = fac.mul(aa.mul(bb).div(cc));
	    deriv = deriv.add(fac);
	    fac = fac.mul(z.mul(1.0 / n));
	    series = temp.add(fac);
	    if (series.equals(temp))
		return;
	    temp = series;
	    aa = aa.add(new Complex(1.0, 0));
	    bb = bb.add(new Complex(1.0, 0));
	    cc = cc.add(new Complex(1.0, 0));
	}
	throw new IllegalArgumentException("convergence failure in hypser");
    }

    private Complex hypgeo0(final Complex a, final Complex b, final Complex c,
	    final Complex z) {
	final double atol = 1.0e-14, rtol = 1.0e-14;
	Complex ans = new Complex(0, 0), dz = new Complex(0, 0), z0 = new Complex(
		0, 0);
	Complex[] y = new Complex[2];
	double[] yy = new double[4];
	if (z.norm() <= 0.25) {
	    hypser(a, b, c, z);
	    ans = series;
	    y[1] = deriv;
	    return ans;
	} else if (z.re() < 0.0)
	    z0 = new Complex(-0.5, 0.0);
	else if (z.re() <= 1.0)
	    z0 = new Complex(0.5, 0.0);
	else
	    z0 = new Complex(0.0, z.im() >= 0.0 ? 0.5 : -0.5);
	dz = z.sub(z0);
	hypser(a, b, c, z0);
	y[0] = series;
	y[1] = deriv;
	yy[0] = y[0].re();
	yy[1] = y[0].im();
	yy[2] = y[1].re();
	yy[3] = y[1].im();
	Hypderiv d = new Hypderiv(a, b, c, z0, dz);
	Output out = new Output();
	StepperBS s = new StepperBS();
	Odeint ode = new Odeint(yy, 0.0, 1.0, atol, rtol, 0.1, 0.0, out, d, s);
	ode.integrate();
	y[0] = new Complex(yy[0], yy[1]);
	return y[0];
    }

    public static Complex hypgeo(final Complex a, final Complex b,
	    final Complex c, final Complex z) {
	Hypergeo h = new Hypergeo();
	return h.hypgeo0(a, b, c, z);
    }

}
