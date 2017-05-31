package com.nr.pde;

import static java.lang.Math.*;

public class Relaxation {
    private Relaxation() {
    }

    public static void sor(final double[][] a, final double[][] b,
	    final double[][] c, final double[][] d, final double[][] e,
	    final double[][] f, final double[][] u, final double rjac) {
	final int MAXITS = 1000;
	final double EPS = 1.0e-13;
	double anormf = 0.0, omega = 1.0;
	int jmax = a.length;
	for (int j = 1; j < jmax - 1; j++)
	    for (int l = 1; l < jmax - 1; l++)
		anormf += abs(f[j][l]);
	for (int n = 0; n < MAXITS; n++) {
	    double anorm = 0.0;
	    int jsw = 1;
	    for (int ipass = 0; ipass < 2; ipass++) {
		int lsw = jsw;
		for (int j = 1; j < jmax - 1; j++) {
		    for (int l = lsw; l < jmax - 1; l += 2) {
			double resid = a[j][l] * u[j + 1][l] + b[j][l]
				* u[j - 1][l] + c[j][l] * u[j][l + 1] + d[j][l]
				* u[j][l - 1] + e[j][l] * u[j][l] - f[j][l];
			anorm += abs(resid);
			u[j][l] -= omega * resid / e[j][l];
		    }
		    lsw = 3 - lsw;
		}
		jsw = 3 - jsw;
		omega = (n == 0 && ipass == 0 ? 1.0 / (1.0 - 0.5 * rjac * rjac)
			: 1.0 / (1.0 - 0.25 * rjac * rjac * omega));
	    }
	    if (anorm < EPS * anormf)
		return;
	}
	throw new IllegalArgumentException("MAXITS exceeded");
    }
}
