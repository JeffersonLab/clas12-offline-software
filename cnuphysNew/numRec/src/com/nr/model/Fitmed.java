package com.nr.model;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;
import static com.nr.sort.Sorter.*;

/**
 * fit a line minimizing absolute deviation
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Fitmed {
    int ndata;
    public double a, b, abdev;
    double[] x, y;

    public Fitmed(final double[] xx, final double[] yy) {
	ndata = xx.length;
	x = xx;
	y = yy;

	int j;
	double b1, b2, del, f, f1, f2, sigb, temp;
	double sx = 0.0, sy = 0.0, sxy = 0.0, sxx = 0.0, chisq = 0.0;
	for (j = 0; j < ndata; j++) {
	    sx += x[j];
	    sy += y[j];
	    sxy += x[j] * y[j];
	    sxx += SQR(x[j]);
	}
	del = ndata * sxx - sx * sx;
	a = (sxx * sy - sx * sxy) / del;
	b = (ndata * sxy - sx * sy) / del;
	for (j = 0; j < ndata; j++) {
	    temp = y[j] - (a + b * x[j]);
	    chisq += (temp * temp);
	}
	sigb = sqrt(chisq / del);
	b1 = b;
	f1 = rofunc(b1);
	if (sigb > 0.0) {
	    b2 = b + SIGN(3.0 * sigb, f1);
	    f2 = rofunc(b2);
	    if (b2 == b1) {
		abdev /= ndata;
		return;
	    }
	    while (f1 * f2 > 0.0) {
		b = b2 + 1.6 * (b2 - b1);
		b1 = b2;
		f1 = f2;
		b2 = b;
		f2 = rofunc(b2);
	    }
	    sigb = 0.01 * sigb;
	    while (abs(b2 - b1) > sigb) {
		b = b1 + 0.5 * (b2 - b1);
		if (b == b1 || b == b2)
		    break;
		f = rofunc(b);
		if (f * f1 >= 0.0) {
		    f1 = f;
		    b1 = b;
		} else {
		    f2 = f;
		    b2 = b;
		}
	    }
	}
	abdev /= ndata;
    }

    public double rofunc(final double b) {
	final double EPS = DBL_EPSILON;
	int j;
	double d, sum = 0.0;
	double[] arr = new double[ndata];
	for (j = 0; j < ndata; j++)
	    arr[j] = y[j] - b * x[j];
	if ((ndata & 1) == 1) {
	    a = select((ndata - 1) >> 1, arr);
	} else {
	    j = ndata >> 1;
	    a = 0.5 * (select(j - 1, arr) + select(j, arr));
	}
	abdev = 0.0;
	for (j = 0; j < ndata; j++) {
	    d = y[j] - (b * x[j] + a);
	    abdev += abs(d);
	    if (y[j] != 0.0)
		d /= abs(y[j]);
	    if (abs(d) > EPS)
		sum += (d >= 0.0 ? x[j] : -x[j]);
	}
	return sum;
    }
}
