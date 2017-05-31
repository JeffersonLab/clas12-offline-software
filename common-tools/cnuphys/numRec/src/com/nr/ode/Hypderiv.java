package com.nr.ode;

import com.nr.Complex;

public class Hypderiv implements DerivativeInf {
    Complex a, b, c, z0, dz;

    public Hypderiv(final Complex aa, final Complex bb, final Complex cc,
	    final Complex z00, final Complex dzz) {
	a = aa;
	b = bb;
	c = cc;
	z0 = z00;
	dz = dzz;

    }

    @Override
    public void jacobian(final double x, final double[] y, final double[] dydx,
	    final double[][] dfdy) {
    }

    @Override
    public void derivs(final double s, final double[] yy, final double[] dyyds) {
	get(s, yy, dyyds);
    }

    public void get(final double s, final double[] yy, final double[] dyyds) {
	Complex z;
	Complex[] y = new Complex[2];
	Complex[] dyds = new Complex[2];

	y[0] = new Complex(yy[0], yy[1]);
	y[1] = new Complex(yy[2], yy[3]);
	z = z0.add(dz.scale(s));
	dyds[0] = y[1].mul(dz);
	// dyds[1]=(a*b*y[0]-(c-(a+b+1.0)*z)*y[1])*dz/(z*(1.0-z));

	Complex aa = c.sub((a.add(b).add(new Complex(1, 0))).mul(z));
	Complex bb = a.mul(b).mul(y[0]).sub(aa.mul(y[1]));
	dyds[1] = bb.mul(dz).div(z.mul(new Complex(1.0, 0).sub(z)));

	dyyds[0] = dyds[0].re();
	dyyds[1] = dyds[0].im();
	dyyds[2] = dyds[1].re();
	dyyds[3] = dyds[1].im();
    }

}
