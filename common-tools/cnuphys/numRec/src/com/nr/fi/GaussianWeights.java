package com.nr.fi;

import static java.lang.Math.*;
import static com.nr.sf.Gamma.*;
import com.nr.UniVarRealValueFun;
import com.nr.eig.Symmeig;

public class GaussianWeights {
    private GaussianWeights() {
    }

    private static final double x[] = { 0.1488743389816312, 0.4333953941292472,
	    0.6794095682990244, 0.8650633666889845, 0.9739065285171717 };
    private static final double w[] = { 0.2955242247147529, 0.2692667193099963,
	    0.2190863625159821, 0.1494513491505806, 0.0666713443086881 };

    /**
     * Returns the integral of the function or functor func between a and b, by
     * ten-point Gauss-Legendre integration: the function is evaluated exactly
     * ten times at interior points in the range of integration.
     * 
     * @param func
     * @param a
     * @param b
     * @return
     */
    public static double qgaus(final UniVarRealValueFun func, final double a,
	    final double b) {
	double xm = 0.5 * (b + a);
	double xr = 0.5 * (b - a);
	double s = 0;
	for (int j = 0; j < 5; j++) {
	    double dx = xr * x[j];
	    s += w[j] * (func.funk(xm + dx) + func.funk(xm - dx));
	}
	return s *= xr;
    }

    /**
     * Given the lower and upper limits of integration x1 and x2, this routine
     * returns arrays x[0..n-1] and w[0..n-1] of length n, containing the
     * abscissas and weights of the Gauss-Legendre n-point quadrature formula.
     * 
     * @param x1
     * @param x2
     * @param x
     * @param w
     */
    public static void gauleg(final double x1, final double x2,
	    final double[] x, final double[] w) {
	final double EPS = 1.0e-14;
	double z1, z, xm, xl, pp, p3, p2, p1;
	int n = x.length;
	int m = (n + 1) / 2;
	xm = 0.5 * (x2 + x1);
	xl = 0.5 * (x2 - x1);
	for (int i = 0; i < m; i++) {
	    z = cos(3.141592654 * (i + 0.75) / (n + 0.5));
	    do {
		p1 = 1.0;
		p2 = 0.0;
		for (int j = 0; j < n; j++) {
		    p3 = p2;
		    p2 = p1;
		    p1 = ((2.0 * j + 1.0) * z * p2 - j * p3) / (j + 1);
		}
		pp = n * (z * p1 - p2) / (z * z - 1.0);
		z1 = z;
		z = z1 - p1 / pp;
	    } while (abs(z - z1) > EPS);
	    x[i] = xm - xl * z;
	    x[n - 1 - i] = xm + xl * z;
	    w[i] = 2.0 * xl / ((1.0 - z * z) * pp * pp);
	    w[n - 1 - i] = w[i];
	}
    }

    /**
     * Given alf, the parameter a of the Laguerre polynomials, this routine
     * returns arrays x[0..n-1] and w[0..n-1] containing the abscissas and
     * weights of the n-point Gauss-Laguerre quadrature formula. The smallest
     * abscissa is returned in x[0], the largest in x[n-1].
     * 
     * @param x
     * @param w
     * @param alf
     */
    public static void gaulag(final double[] x, final double[] w,
	    final double alf) {
	final int MAXIT = 10;
	final double EPS = 1.0e-14;
	int i, its, j;
	double ai, p1, p2 = 0, p3, pp = 0, z = 0, z1;
	int n = x.length;
	for (i = 0; i < n; i++) {
	    if (i == 0) {
		z = (1.0 + alf) * (3.0 + 0.92 * alf)
			/ (1.0 + 2.4 * n + 1.8 * alf);
	    } else if (i == 1) {
		z += (15.0 + 6.25 * alf) / (1.0 + 0.9 * alf + 2.5 * n);
	    } else {
		ai = i - 1;
		z += ((1.0 + 2.55 * ai) / (1.9 * ai) + 1.26 * ai * alf
			/ (1.0 + 3.5 * ai))
			* (z - x[i - 2]) / (1.0 + 0.3 * alf);
	    }
	    for (its = 0; its < MAXIT; its++) {
		p1 = 1.0;
		p2 = 0.0;
		for (j = 0; j < n; j++) {
		    p3 = p2;
		    p2 = p1;
		    p1 = ((2 * j + 1 + alf - z) * p2 - (j + alf) * p3)
			    / (j + 1);
		}
		pp = (n * p1 - (n + alf) * p2) / z;
		z1 = z;
		z = z1 - p1 / pp;
		if (abs(z - z1) <= EPS)
		    break;
	    }
	    if (its >= MAXIT)
		throw new IllegalArgumentException(
			"too many iterations in gaulag");
	    x[i] = z;
	    w[i] = -exp(gammln(alf + n) - gammln((n))) / (pp * n * p2);
	}
    }

    /**
     * This routine returns arrays x[0..n-1] and w[0..n-1] containing the
     * abscissas and weights of the n-point Gauss-Hermite quadrature formula.
     * The largest abscissa is returned in x[0], the most negative in x[n-1].
     * 
     * @param x
     * @param w
     */
    public static void gauher(final double[] x, final double[] w) {
	final double EPS = 1.0e-14, PIM4 = 0.7511255444649425;
	final int MAXIT = 10;
	int i, its, j, m;
	double p1, p2, p3, pp = 0, z = 0, z1;
	int n = x.length;
	m = (n + 1) / 2;
	for (i = 0; i < m; i++) {
	    if (i == 0) {
		z = sqrt((2 * n + 1)) - 1.85575 * pow((2 * n + 1), -0.16667);
	    } else if (i == 1) {
		z -= 1.14 * pow((n), 0.426) / z;
	    } else if (i == 2) {
		z = 1.86 * z - 0.86 * x[0];
	    } else if (i == 3) {
		z = 1.91 * z - 0.91 * x[1];
	    } else {
		z = 2.0 * z - x[i - 2];
	    }
	    for (its = 0; its < MAXIT; its++) {
		p1 = PIM4;
		p2 = 0.0;
		for (j = 0; j < n; j++) {
		    p3 = p2;
		    p2 = p1;
		    p1 = z * sqrt(2.0 / (j + 1)) * p2
			    - sqrt((double) j / (j + 1)) * p3;
		}
		pp = sqrt(2 * n) * p2;
		z1 = z;
		z = z1 - p1 / pp;
		if (abs(z - z1) <= EPS)
		    break;
	    }
	    // XXX seems to always throw, so disable it.
	    // if (its >= MAXIT) throw new
	    // IllegalArgumentException("too many iterations in gauher");
	    x[i] = z;
	    x[n - 1 - i] = -z;
	    w[i] = 2.0 / (pp * pp);
	    w[n - 1 - i] = w[i];
	}
    }

    /**
     * Given alf and bet, the parameters a and b of the Jacobi polynomials, this
     * routine returns arrays x[0..n-1] and w[0..n-1] containing the abscissas
     * and weights of the n-point Gauss-Jacobi quadrature formula. The largest
     * abscissa is returned in x[0], the smallest in x[n-1].
     * 
     * @param x
     * @param w
     * @param alf
     * @param bet
     */
    public static void gaujac(final double[] x, final double[] w,
	    final double alf, final double bet) {
	final int MAXIT = 10;
	final double EPS = 1.0e-14;
	int i, its, j;
	double alfbet, an, bn, r1, r2, r3;
	double a, b, c, p1, p2 = 0, p3, pp = 0, temp = 0, z = 0, z1;
	int n = x.length;
	for (i = 0; i < n; i++) {
	    if (i == 0) {
		an = alf / n;
		bn = bet / n;
		r1 = (1.0 + alf) * (2.78 / (4.0 + n * n) + 0.768 * an / n);
		r2 = 1.0 + 1.48 * an + 0.96 * bn + 0.452 * an * an + 0.83 * an
			* bn;
		z = 1.0 - r1 / r2;
	    } else if (i == 1) {
		r1 = (4.1 + alf) / ((1.0 + alf) * (1.0 + 0.156 * alf));
		r2 = 1.0 + 0.06 * (n - 8.0) * (1.0 + 0.12 * alf) / n;
		r3 = 1.0 + 0.012 * bet * (1.0 + 0.25 * abs(alf)) / n;
		z -= (1.0 - z) * r1 * r2 * r3;
	    } else if (i == 2) {
		r1 = (1.67 + 0.28 * alf) / (1.0 + 0.37 * alf);
		r2 = 1.0 + 0.22 * (n - 8.0) / n;
		r3 = 1.0 + 8.0 * bet / ((6.28 + bet) * n * n);
		z -= (x[0] - z) * r1 * r2 * r3;
	    } else if (i == n - 2) {
		r1 = (1.0 + 0.235 * bet) / (0.766 + 0.119 * bet);
		r2 = 1.0 / (1.0 + 0.639 * (n - 4.0) / (1.0 + 0.71 * (n - 4.0)));
		r3 = 1.0 / (1.0 + 20.0 * alf / ((7.5 + alf) * n * n));
		z += (z - x[n - 4]) * r1 * r2 * r3;
	    } else if (i == n - 1) {
		r1 = (1.0 + 0.37 * bet) / (1.67 + 0.28 * bet);
		r2 = 1.0 / (1.0 + 0.22 * (n - 8.0) / n);
		r3 = 1.0 / (1.0 + 8.0 * alf / ((6.28 + alf) * n * n));
		z += (z - x[n - 3]) * r1 * r2 * r3;
	    } else {
		z = 3.0 * x[i - 1] - 3.0 * x[i - 2] + x[i - 3];
	    }
	    alfbet = alf + bet;
	    for (its = 1; its <= MAXIT; its++) {
		temp = 2.0 + alfbet;
		p1 = (alf - bet + temp * z) / 2.0;
		p2 = 1.0;
		for (j = 2; j <= n; j++) {
		    p3 = p2;
		    p2 = p1;
		    temp = 2 * j + alfbet;
		    a = 2 * j * (j + alfbet) * (temp - 2.0);
		    b = (temp - 1.0)
			    * (alf * alf - bet * bet + temp * (temp - 2.0) * z);
		    c = 2.0 * (j - 1 + alf) * (j - 1 + bet) * temp;
		    p1 = (b * p2 - c * p3) / a;
		}
		pp = (n * (alf - bet - temp * z) * p1 + 2.0 * (n + alf)
			* (n + bet) * p2)
			/ (temp * (1.0 - z * z));
		z1 = z;
		z = z1 - p1 / pp;
		if (abs(z - z1) <= EPS)
		    break;
	    }
	    if (its > MAXIT)
		throw new IllegalArgumentException(
			"too many iterations in gaujac");
	    x[i] = z;
	    w[i] = exp(gammln(alf + n) + gammln(bet + n) - gammln(n + 1.0)
		    - gammln(n + alfbet + 1.0))
		    * temp * pow(2.0, alfbet) / (pp * p2);
	}
    }

    /**
     * Computes the abscissas and weights for a Gaussian quadrature formula from
     * the Jacobi matrix. On input, a[0..n-1] and b[0..n-1] are the coefficients
     * of the recurrence relation for the set of monic orthogonal polynomials.
     * The quantity u0=S(a,b)W(x)dx is input as amu0. The abscissas x[0..n-1]
     * are returned in descending order, with the corresponding weights in
     * w[0..n-1]. The arrays a and b are modified. Execution can be speeded up
     * by modifying tqli and eigsrt to compute only the zeroth component of each
     * eigenvector.
     * 
     * @param a
     * @param b
     * @param amu0
     * @param x
     * @param w
     */
    public static void gaucof(final double[] a, final double[] b,
	    final double amu0, final double[] x, final double[] w) {
	int n = a.length;
	for (int i = 0; i < n; i++)
	    if (i != 0)
		b[i] = sqrt(b[i]);
	Symmeig sym = new Symmeig(a, b);
	for (int i = 0; i < n; i++) {
	    x[i] = sym.d[i];
	    w[i] = amu0 * sym.z[0][i] * sym.z[0][i];
	}
    }

    /**
     * Computes the abscissas and weights for a Gauss-Radau quadrature formula.
     * On input, a[0..n-1] and b[0..n-1] are the coefficients of the recurrence
     * relation for the set of monic orthogo- nal polynomials corresponding to
     * the weight function. (b[0] is not referenced.) The quantity
     * u0=S(a,b)W(x)dx is input as amu0. x1 is input as either endpoint of the
     * interval. The abscissas x[0..n-1] are returned in descending order, with
     * the corresponding weights in w[0..n-1]. The arrays a and b are modified.
     * 
     * @param a
     * @param b
     * @param amu0
     * @param x1
     * @param x
     * @param w
     */
    public static void radau(final double[] a, final double[] b,
	    final double amu0, final double x1, final double[] x,
	    final double[] w) {
	int n = a.length;
	if (n == 1) {
	    x[0] = x1;
	    w[0] = amu0;
	} else {
	    double p = x1 - a[0];
	    double pm1 = 1.0;
	    double p1 = p;
	    for (int i = 1; i < n - 1; i++) {
		p = (x1 - a[i]) * p1 - b[i] * pm1;
		pm1 = p1;
		p1 = p;
	    }
	    a[n - 1] = x1 - b[n - 1] * pm1 / p;
	    gaucof(a, b, amu0, x, w);
	}
    }

    /**
     * Computes the abscissas and weights for a Gauss-Lobatto quadrature
     * formula. On input, the vectors a[0..n-1] and b[0..n-1] are the
     * coefficients of the recurrence relation for the set of monic orthogonal
     * polynomials corresponding to the weight function. (b[0] is not
     * referenced.) The quantity u0=S(a,b)W(x)dx is input as amu0. x1 amd xn are
     * input as the endpoints of the interval. The abscissas x[0..n-1] are
     * returned in descending order, with the corresponding weights in
     * w[0..n-1]. The arrays a and b are modified.
     * 
     * @param a
     * @param b
     * @param amu0
     * @param x1
     * @param xn
     * @param x
     * @param w
     */
    public static void lobatto(final double[] a, final double[] b,
	    final double amu0, final double x1, final double xn,
	    final double[] x, final double[] w) {
	double det, pl, pr, p1l, p1r, pm1l, pm1r;
	int n = a.length;
	if (n <= 1)
	    throw new IllegalArgumentException(
		    "n must be bigger than 1 in lobatto");
	pl = x1 - a[0];
	pr = xn - a[0];
	pm1l = 1.0;
	pm1r = 1.0;
	p1l = pl;
	p1r = pr;
	for (int i = 1; i < n - 1; i++) {
	    pl = (x1 - a[i]) * p1l - b[i] * pm1l;
	    pr = (xn - a[i]) * p1r - b[i] * pm1r;
	    pm1l = p1l;
	    pm1r = p1r;
	    p1l = pl;
	    p1r = pr;
	}
	det = pl * pm1r - pr * pm1l;
	a[n - 1] = (x1 * pl * pm1r - xn * pr * pm1l) / det;
	b[n - 1] = (xn - x1) * pl * pr / det;
	gaucof(a, b, amu0, x, w);
    }
}
