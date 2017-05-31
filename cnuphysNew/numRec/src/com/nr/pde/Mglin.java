package com.nr.pde;

import static com.nr.NRUtil.*;

public class Mglin {
    int n, ng;
    double[][] uj, uj1;

    // NRvector<NRmatrix<double> *> rho;
    double[][][] rho;
    public double[][] u;

    public Mglin(final double[][] u, final int ncycle) {
	this.u = u;
	n = u.length;
	ng = 0;
	int nn = n;
	while ((nn >>>= 1) != 0)
	    ng++;
	if ((n - 1) != (1 << ng))
	    throw new IllegalArgumentException(
		    "n-1 must be a power of 2 in mglin.");
	nn = n;
	int ngrid = ng - 1;
	rho = new double[ng][][];
	rho[ngrid] = new double[nn][nn];
	copyAssign(rho[ngrid], u);
	while (nn > 3) {
	    nn = nn / 2 + 1;
	    rho[--ngrid] = new double[nn][nn];
	    rstrct(rho[ngrid], rho[ngrid + 1]);
	}
	nn = 3;
	uj = new double[nn][nn];
	slvsml(uj, rho[0]);
	for (int j = 1; j < ng; j++) {
	    nn = 2 * nn - 1;
	    uj1 = uj;
	    uj = new double[nn][nn];
	    interp(uj, uj1);
	    for (int jcycle = 0; jcycle < ncycle; jcycle++)
		mg(j, uj, rho[j]);
	}
	this.u = uj;
    }

    public static void interp(final double[][] uf, final double[][] uc) {
	int nf = uf.length;
	int nc = nf / 2 + 1;
	for (int jc = 0; jc < nc; jc++)
	    for (int ic = 0; ic < nc; ic++)
		uf[2 * ic][2 * jc] = uc[ic][jc];
	for (int jf = 0; jf < nf; jf += 2)
	    for (int iif = 1; iif < nf - 1; iif += 2)
		uf[iif][jf] = 0.5 * (uf[iif + 1][jf] + uf[iif - 1][jf]);
	for (int jf = 1; jf < nf - 1; jf += 2)
	    for (int iif = 0; iif < nf; iif++)
		uf[iif][jf] = 0.5 * (uf[iif][jf + 1] + uf[iif][jf - 1]);
    }

    public static void addint(final double[][] uf, final double[][] uc,
	    final double[][] res) {
	int nf = uf.length;
	interp(res, uc);
	for (int j = 0; j < nf; j++)
	    for (int i = 0; i < nf; i++)
		uf[i][j] += res[i][j];
    }

    public static void slvsml(final double[][] u, final double[][] rhs) {
	double h = 0.5;
	for (int i = 0; i < 3; i++)
	    for (int j = 0; j < 3; j++)
		u[i][j] = 0.0;
	u[1][1] = -h * h * rhs[1][1] / 4.0;
    }

    public static void relax(final double[][] u, final double[][] rhs) {
	int n = u.length;
	double h = 1.0 / (n - 1);
	double h2 = h * h;
	for (int ipass = 0, jsw = 1; ipass < 2; ipass++, jsw = 3 - jsw) {
	    for (int j = 1, isw = jsw; j < n - 1; j++, isw = 3 - isw)
		for (int i = isw; i < n - 1; i += 2)
		    u[i][j] = 0.25 * (u[i + 1][j] + u[i - 1][j] + u[i][j + 1]
			    + u[i][j - 1] - h2 * rhs[i][j]);
	}
    }

    public static void resid(final double[][] res, final double[][] u,
	    final double[][] rhs) {
	int n = u.length;
	double h = 1.0 / (n - 1);
	double h2i = 1.0 / (h * h);
	for (int j = 1; j < n - 1; j++)
	    for (int i = 1; i < n - 1; i++)
		res[i][j] = -h2i
			* (u[i + 1][j] + u[i - 1][j] + u[i][j + 1]
				+ u[i][j - 1] - 4.0 * u[i][j]) + rhs[i][j];
	for (int i = 0; i < n; i++)
	    res[i][0] = res[i][n - 1] = res[0][i] = res[n - 1][i] = 0.0;
    }

    public static void rstrct(final double[][] uc, final double[][] uf) {
	int nc = uc.length;
	int ncc = 2 * nc - 2;
	for (int jf = 2, jc = 1; jc < nc - 1; jc++, jf += 2) {
	    for (int iif = 2, ic = 1; ic < nc - 1; ic++, iif += 2) {
		uc[ic][jc] = 0.5
			* uf[iif][jf]
			+ 0.125
			* (uf[iif + 1][jf] + uf[iif - 1][jf] + uf[iif][jf + 1] + uf[iif][jf - 1]);
	    }
	}
	for (int jc = 0, ic = 0; ic < nc; ic++, jc += 2) {
	    uc[ic][0] = uf[jc][0];
	    uc[ic][nc - 1] = uf[jc][ncc];
	}
	for (int jc = 0, ic = 0; ic < nc; ic++, jc += 2) {
	    uc[0][ic] = uf[0][jc];
	    uc[nc - 1][ic] = uf[ncc][jc];
	}
    }

    public static void mg(final int j, final double[][] u, final double[][] rhs) {
	final int NPRE = 1, NPOST = 1;
	int nf = u.length;
	int nc = (nf + 1) / 2;
	if (j == 0)
	    slvsml(u, rhs);
	else {
	    double[][] res = new double[nc][nc], v = new double[nc][nc], temp = new double[nf][nf];
	    for (int jpre = 0; jpre < NPRE; jpre++)
		relax(u, rhs);
	    resid(temp, u, rhs);
	    rstrct(res, temp);
	    mg(j - 1, v, res);
	    addint(u, v, temp);
	    for (int jpost = 0; jpost < NPOST; jpost++)
		relax(u, rhs);
	}
    }
}
