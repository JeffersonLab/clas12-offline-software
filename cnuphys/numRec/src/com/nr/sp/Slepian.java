package com.nr.sp;

import static java.lang.Math.*;
import static com.nr.NRUtil.*;

public class Slepian extends Spectreg {

    int jres, kt;
    double[][] dpss;
    double p, pp, d, dd;

    public Slepian(final int em, final int jjres, final int kkt) {
	super(em);
	jres = jjres;
	kt = kkt;
	dpss = new double[kkt][2 * em];

	if (jres < 1 || kt >= 2 * jres)
	    throw new IllegalArgumentException("kt too big or jres too small");
	filltable();
    }

    public void renorm(final int n) {
	p = ldexp(p, n);
	pp = ldexp(pp, n);
	d = ldexp(d, n);
	dd = ldexp(dd, n);
    }

    public void adddataseg(final double[] data) {
	int k;
	if (data.length != m2)
	    throw new IllegalArgumentException("wrong size data segment");
	for (k = 0; k < kt; k++) {
	    Slepwindow window = new Slepwindow(k, dpss);
	    super.adddataseg(data, window);
	}
    }

    public void filltable() {
	final double EPS = 1.e-10, PI = 4. * atan(1.);
	double xx, xnew = 0, xold, sw, ppp, ddd, sum, bet, ssub, ssup;// ,*u;
	int i, j, k, nl;
	double[] dg = new double[m2], dgg = new double[m2], gam = new double[m2], sup = new double[m2 - 1], sub = new double[m2 - 1];
	sw = 2. * SQR(sin(jres * PI / m2));
	dg[0] = 0.25 * (2 * m2 + sw * SQR(m2 - 1.) - 1.);
	for (i = 1; i < m2; i++) {
	    dg[i] = 0.25 * (sw * SQR(m2 - 1. - 2 * i) + (2 * (m2 - i) - 1.)
		    * (2 * i + 1.));
	    sub[i - 1] = sup[i - 1] = -i * (double) (m2 - i) / 2.;
	}
	xx = -0.10859 - 0.068762 / jres + 1.5692 * jres;
	xold = xx + 0.47276 + 0.20273 / jres - 3.1387 * jres;
	for (k = 0; k < kt; k++) {
	    // u = &dpss[k][0];
	    for (i = 0; i < 20; i++) {
		pp = 1.;
		p = dg[0] - xx;
		dd = 0.;
		d = -1.;
		for (j = 1; j < m2; j++) {
		    ppp = pp;
		    pp = p;
		    ddd = dd;
		    dd = d;
		    p = pp * (dg[j] - xx) - ppp * SQR(sup[j - 1]);
		    d = -pp + dd * (dg[j] - xx) - ddd * SQR(sup[j - 1]);
		    if (abs(p) > 1.e30)
			renorm(-100);
		    else if (abs(p) < 1.e-30)
			renorm(100);
		}
		xnew = xx - p / d;
		if (abs(xx - xnew) < EPS * abs(xnew))
		    break;
		xx = xnew;
	    }
	    xx = xnew - (xold - xnew);
	    xold = xnew;
	    for (i = 0; i < m2; i++)
		dgg[i] = dg[i] - xnew;
	    nl = m2 / 3;
	    dgg[nl] = 1.;
	    ssup = sup[nl];
	    ssub = sub[nl - 1];
	    // u[0] = sup[nl] = sub[nl-1] = 0.;
	    dpss[k][0] = sup[nl] = sub[nl - 1] = 0.;
	    bet = dgg[0];
	    for (i = 1; i < m2; i++) {
		gam[i] = sup[i - 1] / bet;
		bet = dgg[i] - sub[i - 1] * gam[i];
		// u[i] = ((i==nl? 1. : 0.) - sub[i-1]*u[i-1])/bet;
		dpss[k][i] = ((i == nl ? 1. : 0.) - sub[i - 1] * dpss[k][i - 1])
			/ bet;
	    }
	    for (i = m2 - 2; i >= 0; i--)
		dpss[k][i] -= gam[i + 1] * dpss[k][i + 1];
	    sup[nl] = ssup;
	    sub[nl - 1] = ssub;
	    sum = 0.;
	    for (i = 0; i < m2; i++)
		sum += SQR(dpss[k][i]);
	    sum = (dpss[k][3] > 0.) ? sqrt(sum) : -sqrt(sum);
	    for (i = 0; i < m2; i++)
		dpss[k][i] /= sum;
	}
    }
}
