package com.nr.ran;

import static java.lang.Math.*;
import static com.nr.NRUtil.*;
import com.nr.la.Cholesky;

public class Multinormaldev extends Ran {
    int mm;
    double[] mean;
    double[][] var;
    Cholesky chol;
    double[] spt, pt;

    public Multinormaldev(final long j, final double[] mmean,
	    final double[][] vvar) {
	super(j);
	mm = mmean.length;
	mean = buildVector(mmean);
	var = buildMatrix(vvar);
	chol = new Cholesky(var);
	spt = new double[mm];
	pt = new double[mm];
	if (var[0].length != mm || var.length != mm)
	    throw new IllegalArgumentException("bad sizes");
    }

    public double[] dev() {
	int i;
	double u, v, x, y, q;
	for (i = 0; i < mm; i++) {
	    do {
		u = doub();
		v = 1.7156 * (doub() - 0.5);
		x = u - 0.449871;
		y = abs(v) + 0.386595;
		q = SQR(x) + y * (0.19600 * y - 0.25472 * x);
	    } while (q > 0.27597
		    && (q > 0.27846 || SQR(v) > -4. * log(u) * SQR(u)));
	    spt[i] = v / u;
	}
	chol.elmult(spt, pt);
	for (i = 0; i < mm; i++) {
	    pt[i] += mean[i];
	}
	return pt;
    }

}
