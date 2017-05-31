package com.nr.fe;

import static java.lang.Math.*;

public class Eulsum {
    private double[] wksp;
    private int n, ncv;
    public boolean cnvgd;
    private final double eps;
    private double sum, lastval, lasteps;

    public Eulsum(final int nmax, final double epss) {
	wksp = new double[nmax];
	n = 0;
	ncv = 0;
	cnvgd = false;
	sum = 0;
	eps = epss;
	lastval = 0;
    }

    public double next(final double term) {
	int j;
	double tmp, dum;
	if (n + 1 > wksp.length)
	    throw new IllegalArgumentException("wksp too small in eulsum");
	if (n == 0) {
	    sum = 0.5 * (wksp[n++] = term);
	} else {
	    tmp = wksp[0];
	    wksp[0] = term;
	    for (j = 1; j < n; j++) {
		dum = wksp[j];
		wksp[j] = 0.5 * (wksp[j - 1] + tmp);
		tmp = dum;
	    }
	    wksp[n] = 0.5 * (wksp[n - 1] + tmp);
	    if (abs(wksp[n]) <= abs(wksp[n - 1]))
		sum += (0.5 * wksp[n++]);
	    else
		sum += wksp[n];
	}
	lasteps = abs(sum - lastval);
	if (lasteps <= eps)
	    ncv++;
	if (ncv >= 2)
	    cnvgd = true;
	return (lastval = sum);
    }
}
