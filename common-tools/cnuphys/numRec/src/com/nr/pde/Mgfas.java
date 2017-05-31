package com.nr.pde;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;

import org.netlib.util.doubleW;

public class Mgfas {
    int n, ng;
    double[][] uj, uj1;
    // NRvector<NRmatrix<double> *> rho;
    double[][][] rho;

    public double[][] u;

    /**
     * 
     * @param u
     * @param maxcyc
     */
    public Mgfas(final double[][] u, final int maxcyc) {
	this.u = u;
	n = u.length;
	ng = 0;
	int nn = n;
	while ((nn >>>= 1) != 0)
	    ng++;
	if ((n - 1) != (1 << ng))
	    throw new IllegalArgumentException(
		    "n-1 must be a power of 2 in mgfas.");
	nn = n;
	int ngrid = ng - 1;
	rho = new double[ng][][];
	// rho.resize(ng);
	rho[ngrid] = new double[nn][nn];
	copyAssign(rho[ngrid], u);
	while (nn > 3) {
	    nn = nn / 2 + 1;
	    rho[--ngrid] = new double[nn][nn];
	    rstrct(rho[ngrid], rho[ngrid + 1]);
	}
	nn = 3;
	uj = new double[nn][nn];
	slvsm2(uj, rho[0]);
	for (int j = 1; j < ng; j++) {
	    nn = 2 * nn - 1;
	    uj1 = uj;
	    uj = new double[nn][nn];
	    double[][] temp = new double[nn][nn];
	    interp(uj, uj1);
	    for (int jcycle = 0; jcycle < maxcyc; jcycle++) {
		doubleW trerr = new doubleW(1.0);
		mg(j, uj, temp, rho, trerr);
		lop(temp, uj);
		matsub(temp, rho[j], temp);
		double res = anorm2(temp);
		if (res < trerr.val)
		    break;
	    }
	}
	this.u = uj;
    }

    public static void matadd(final double[][] a, final double[][] b,
	    final double[][] c) {
	int n = a.length;
	for (int j = 0; j < n; j++)
	    for (int i = 0; i < n; i++)
		c[i][j] = a[i][j] + b[i][j];
    }

    public static void matsub(final double[][] a, final double[][] b,
	    final double[][] c) {
	int n = a.length;
	for (int j = 0; j < n; j++)
	    for (int i = 0; i < n; i++)
		c[i][j] = a[i][j] - b[i][j];
    }

    public static void slvsm2(final double[][] u, final double[][] rhs) {
	double h = 0.5;
	for (int i = 0; i < 3; i++)
	    for (int j = 0; j < 3; j++)
		u[i][j] = 0.0;
	double fact = 2.0 / (h * h);
	double disc = sqrt(fact * fact + rhs[1][1]);
	u[1][1] = -rhs[1][1] / (fact + disc);
    }

    public static void relax2(final double[][] u, final double[][] rhs) {
	int n = u.length;
	int jsw = 1;
	double h = 1.0 / (n - 1);
	double h2i = 1.0 / (h * h);
	double foh2 = -4.0 * h2i;
	for (int ipass = 0; ipass < 2; ipass++, jsw = 3 - jsw) {
	    int isw = jsw;
	    for (int j = 1; j < n - 1; j++, isw = 3 - isw) {
		for (int i = isw; i < n - 1; i += 2) {
		    double res = h2i
			    * (u[i + 1][j] + u[i - 1][j] + u[i][j + 1]
				    + u[i][j - 1] - 4.0 * u[i][j]) + u[i][j]
			    * u[i][j] - rhs[i][j];
		    u[i][j] -= res / (foh2 + 2.0 * u[i][j]);
		}
	    }
	}
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

    public static void lop(final double[][] out, final double[][] u) {
	int n = u.length;
	double h = 1.0 / (n - 1);
	double h2i = 1.0 / (h * h);
	for (int j = 1; j < n - 1; j++)
	    for (int i = 1; i < n - 1; i++)
		out[i][j] = h2i
			* (u[i + 1][j] + u[i - 1][j] + u[i][j + 1]
				+ u[i][j - 1] - 4.0 * u[i][j]) + u[i][j]
			* u[i][j];
	for (int i = 0; i < n; i++)
	    out[i][0] = out[i][n - 1] = out[0][i] = out[n - 1][i] = 0.0;
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

    public static double anorm2(final double[][] a) {
	double sum = 0.0;
	int n = a.length;
	for (int j = 0; j < n; j++)
	    for (int i = 0; i < n; i++)
		sum += a[i][j] * a[i][j];
	return sqrt(sum) / n;
    }

    public static void mg(final int j, final double[][] u,
	    final double[][] rhs, final double[][][] rho, final doubleW trerr) {
	final int NPRE = 1, NPOST = 1;
	final double ALPHA = 0.33;
	doubleW dum = new doubleW(-1.0);
	int nf = u.length;
	int nc = (nf + 1) / 2;
	double[][] temp = new double[nf][nf];
	if (j == 0) {
	    matadd(rhs, rho[j], temp);
	    slvsm2(u, temp);
	} else {
	    double[][] v = new double[nc][nc], ut = new double[nc][nc], tau = new double[nc][nc], tempc = new double[nc][nc];
	    for (int jpre = 0; jpre < NPRE; jpre++) {
		if (trerr.val < 0.0) {
		    matadd(rhs, rho[j], temp);
		    relax2(u, temp);
		} else
		    relax2(u, rho[j]);
	    }
	    rstrct(ut, u);
	    copyAssign(v, ut);
	    lop(tau, ut);
	    lop(temp, u);
	    if (trerr.val < 0.0)
		matsub(temp, rhs, temp);
	    rstrct(tempc, temp);
	    matsub(tau, tempc, tau);
	    if (trerr.val > 0.0)
		trerr.val = ALPHA * anorm2(tau);
	    mg(j - 1, v, tau, rho, dum);
	    matsub(v, ut, tempc);
	    interp(temp, tempc);
	    matadd(u, temp, u);
	    for (int jpost = 0; jpost < NPOST; jpost++) {
		if (trerr.val < 0.0) {
		    matadd(rhs, rho[j], temp);
		    relax2(u, temp);
		} else
		    relax2(u, rho[j]);
	    }
	}
    }
}
