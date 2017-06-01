package com.nr.model;

import static com.nr.sf.Gamma.*;
import static java.lang.Math.*;

public class Plog {
    double[] dat;
    int ndat;
    double[] stau, slogtau;

    public Plog(final double[] data) {
	dat = data;
	ndat = data.length;
	stau = new double[ndat];
	slogtau = new double[ndat];
	int i;
	stau[0] = slogtau[0] = 0.;
	for (i = 1; i < ndat; i++) {
	    stau[i] = dat[i] - dat[0];
	    slogtau[i] = slogtau[i - 1] + log(dat[i] - dat[i - 1]);
	}
    }

    public double get(final State s) {
	int i, ilo, ihi, n1, n2;
	double st1, st2, stl1, stl2, ans;
	ilo = 0;
	ihi = ndat - 1;
	while (ihi - ilo > 1) {
	    i = (ihi + ilo) >> 1;
	    if (s.tc > dat[i])
		ilo = i;
	    else
		ihi = i;
	}
	n1 = ihi;
	n2 = ndat - 1 - ihi;
	st1 = stau[ihi];
	st2 = stau[ndat - 1] - st1;
	stl1 = slogtau[ihi];
	stl2 = slogtau[ndat - 1] - stl1;
	ans = n1 * (s.k1 * log(s.lam1) - factln(s.k1 - 1)) + (s.k1 - 1) * stl1
		- s.lam1 * st1;
	ans += n2 * (s.k2 * log(s.lam2) - factln(s.k2 - 1)) + (s.k2 - 1) * stl2
		- s.lam2 * st2;
	return (s.plog = ans);
    }
}
