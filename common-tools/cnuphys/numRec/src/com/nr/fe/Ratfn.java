package com.nr.fe;

import static java.lang.Math.*;
import static com.nr.NRUtil.*;

import org.netlib.util.doubleW;

import com.nr.UniVarRealValueFun;
import com.nr.la.LUdcmp;
import com.nr.la.SVD;

public class Ratfn {
    double[] cofs;
    int nn, dd;

    public Ratfn(final double[] num, final double[] den) {
	cofs = new double[num.length + den.length - 1];
	nn = num.length;
	dd = den.length;

	int j;
	for (j = 0; j < nn; j++)
	    cofs[j] = num[j] / den[0];
	for (j = 1; j < dd; j++)
	    cofs[j + nn - 1] = den[j] / den[0];
    }

    public Ratfn(final double[] coffs, final int n, final int d) {
	cofs = buildVector(coffs);
	nn = n;
	dd = d;
    }

    public double get(final double x) {
	int j;
	double sumn = 0., sumd = 0.;
	for (j = nn - 1; j >= 0; j--)
	    sumn = sumn * x + cofs[j];
	for (j = nn + dd - 2; j >= nn; j--)
	    sumd = sumd * x + cofs[j];
	return sumn / (1.0 + x * sumd);
    }

    /**
     * Pade Approximation
     * 
     * Given cof[0..2*n], the leading terms in the power series expansion of a
     * function, solve the linear Pade equations to return a Ratfn object that
     * embodies a diagonal rational function e approximation to the same
     * function.
     * 
     * 
     * @param cof
     * @return
     */
    public static Ratfn pade(final double[] cof) {
	int j, k, n = (cof.length - 1) / 2;
	double sum;
	double[][] q = new double[n][n];
	// double[][] qlu = new double[n][n];
	double[] x = new double[n];
	double[] y = new double[n];
	double[] num = new double[n + 1];
	double[] denom = new double[n + 1];
	for (j = 0; j < n; j++) {
	    y[j] = cof[n + j + 1];
	    for (k = 0; k < n; k++)
		q[j][k] = cof[j - k + n];
	}
	LUdcmp lu = new LUdcmp(q);
	lu.solve(y, x);
	for (j = 0; j < 4; j++)
	    lu.mprove(y, x);
	for (k = 0; k < n; k++) {
	    for (sum = cof[k + 1], j = 0; j <= k; j++)
		sum -= x[j] * cof[k - j];
	    y[k] = sum;
	}
	num[0] = cof[0];
	denom[0] = 1.;
	for (j = 0; j < n; j++) {
	    num[j + 1] = y[j];
	    denom[j + 1] = -x[j];
	}
	return new Ratfn(num, denom);
    }

    /**
     * Rational Chebyshev Approximation
     * 
     * Returns a rational function approximation to the function fn in the
     * interval (a, b). Input quantities mm and kk specify the order of the
     * numerator and denominator, respectively. The maximum absolute deviation
     * of the approximation (insofar as is known) is returned as dev.
     * 
     * @param fn
     * @param a
     * @param b
     * @param mm
     * @param kk
     * @param dev
     * @return
     */
    public static Ratfn ratlsq(final UniVarRealValueFun fn, final double a,
	    final double b, final int mm, final int kk, final doubleW dev) {
	final int NPFAC = 8, MAXIT = 5;
	final double BIG = 1.0e99, PIO2 = 1.570796326794896619;
	int i, it, j, ncof = mm + kk + 1, npt = NPFAC * ncof;
	double devmax, e, hth, power, sum;
	double[] bb = new double[npt];
	double[] coff = new double[ncof];
	double[] ee = new double[npt];
	double[] fs = new double[npt];
	double[] wt = new double[npt];
	double[] xs = new double[npt];

	double[][] u = new double[npt][ncof];

	Ratfn ratbest = new Ratfn(coff, mm + 1, kk + 1);
	dev.val = BIG;
	for (i = 0; i < npt; i++) {
	    if (i < (npt / 2) - 1) {
		hth = PIO2 * i / (npt - 1.0);
		xs[i] = a + (b - a) * SQR(sin(hth));
	    } else {
		hth = PIO2 * (npt - i) / (npt - 1.0);
		xs[i] = b - (b - a) * SQR(sin(hth));
	    }
	    fs[i] = fn.funk(xs[i]);
	    wt[i] = 1.0;
	    ee[i] = 1.0;
	}
	e = 0.0;
	for (it = 0; it < MAXIT; it++) {
	    for (i = 0; i < npt; i++) {
		power = wt[i];
		bb[i] = power * (fs[i] + SIGN(e, ee[i]));
		for (j = 0; j < mm + 1; j++) {
		    u[i][j] = power;
		    power *= xs[i];
		}
		power = -bb[i];
		for (j = mm + 1; j < ncof; j++) {
		    power *= xs[i];
		    u[i][j] = power;
		}
	    }
	    SVD svd = new SVD(u);
	    svd.solve(bb, coff);
	    devmax = sum = 0.0;
	    Ratfn rat = new Ratfn(coff, mm + 1, kk + 1);
	    for (j = 0; j < npt; j++) {
		ee[j] = rat.get(xs[j]) - fs[j];
		wt[j] = abs(ee[j]);
		sum += wt[j];
		if (wt[j] > devmax)
		    devmax = wt[j];
	    }
	    e = sum / npt;
	    if (devmax <= dev.val) {
		ratbest = rat;
		dev.val = devmax;
	    }
	    // cout << " ratlsq iteration= " << it;
	    // cout << "  max error= " << setw(10) << devmax << endl;
	}
	return ratbest;
    }
}
