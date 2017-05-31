package com.nr.root;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;

import org.netlib.util.*;

import com.nr.RealMultiValueFun;
import com.nr.RealValueFun;
import com.nr.UniValRealValueFunWithDiff;
import com.nr.UniVarRealValueFun;
import com.nr.eig.Unsymmeig;
import com.nr.fe.Poly;
import com.nr.la.LUdcmp;
import com.nr.la.QRdcmp;
import com.nr.Complex;

public class Roots {
    private Roots() {
    }

    /**
     * Given a function or functor func and an initial guessed range x1 to x2,
     * the routine expands the range geometrically until a root is bracketed by
     * the returned values x1 and x2 (in which case zbrac returns true) or until
     * the range becomes unacceptably large (in which case zbrac returns false).
     * 
     * @param func
     * @param x1
     * @param x2
     * @return
     */
    public static boolean zbrac(final UniVarRealValueFun func,
	    final doubleW x1, final doubleW x2) {
	final int NTRY = 50;
	final double FACTOR = 1.6;
	if (x1 == x2)
	    throw new IllegalArgumentException("Bad initial range in zbrac");
	double f1 = func.funk(x1.val);
	double f2 = func.funk(x2.val);
	for (int j = 0; j < NTRY; j++) {
	    if (f1 * f2 < 0.0)
		return true;
	    if (abs(f1) < abs(f2))
		f1 = func.funk(x1.val += FACTOR * (x1.val - x2.val));
	    else
		f2 = func.funk(x2.val += FACTOR * (x2.val - x1.val));
	}
	return false;
    }

    /**
     * Given a function or functor fx defined on the interval [x1,x2], subdivide
     * the interval into n equally spaced segments, and search for zero
     * crossings of the function. nroot will be set to the number of bracketing
     * pairs found. If it is positive, the arrays xb1[0..nroot-1] and
     * xb2[0..nroot-1] will be filled sequentially with any bracketing pairs
     * that are found. On input, these vectors may have any size, including
     * zero; they will be resized to nroot.
     * 
     * @param fx
     * @param x1
     * @param x2
     * @param n
     * @param xb1
     * @param xb2
     * @param nroot
     */
    public static class Zbrak {
	public double[] xb1, xb2;
	public int nroot;

	public void zbrak(final UniVarRealValueFun fx, final double x1,
		final double x2, final int n) {
	    int nb = 20;
	    xb1 = new double[nb];
	    xb2 = new double[nb];
	    nroot = 0;
	    double dx = (x2 - x1) / n;
	    double x = x1;
	    double fp = fx.funk(x1);
	    for (int i = 0; i < n; i++) {
		double fc = fx.funk(x += dx);
		if (fc * fp <= 0.0) {
		    xb1[nroot] = x - dx;
		    xb2[nroot++] = x;
		    if (nroot == nb) {
			double[] tempvec1 = buildVector(xb1);
			double[] tempvec2 = buildVector(xb2);
			xb1 = resize(xb1, 2 * nb);
			xb2 = resize(xb2, 2 * nb);
			for (int j = 0; j < nb; j++) {
			    xb1[j] = tempvec1[j];
			    xb2[j] = tempvec2[j];
			}
			nb *= 2;
		    }
		}
		fp = fc;
	    }
	}
    }

    /**
     * Using bisection, return the root of a function or functor func known to
     * lie between x1 and x2. The root will be refined until its accuracy is
     * +/-xacc.
     * 
     * @param func
     * @param x1
     * @param x2
     * @param xacc
     * @return
     */
    public static double rtbis(final UniVarRealValueFun func, final double x1,
	    final double x2, final double xacc) {
	final int JMAX = 50;
	double dx, xmid, rtb;
	double f = func.funk(x1);
	double fmid = func.funk(x2);
	if (f * fmid >= 0.0)
	    throw new IllegalArgumentException(
		    "Root must be bracketed for bisection in rtbis");
	// rtb = f < 0.0 ? (dx=x2-x1,x1) : (dx=x1-x2,x2);
	rtb = 0;
	if (f < 0.0) {
	    dx = x2 - x1;
	    rtb = x1;
	} else {
	    dx = x1 - x2;
	    rtb = x2;
	}
	for (int j = 0; j < JMAX; j++) {
	    fmid = func.funk(xmid = rtb + (dx *= 0.5));
	    if (fmid <= 0.0)
		rtb = xmid;
	    if (abs(dx) < xacc || fmid == 0.0)
		return rtb;
	}
	throw new IllegalArgumentException("Too many bisections in rtbis");
    }

    /**
     * Using the false-position method, return the root of a function or functor
     * func known to lie between x1 and x2. The root is refined until its
     * accuracy is +/-acc
     * 
     * @param func
     * @param x1
     * @param x2
     * @param xacc
     * @return
     */
    public static double rtflsp(final UniVarRealValueFun func, final double x1,
	    final double x2, final double xacc) {
	final int MAXIT = 30;
	double xl, xh, del;
	double fl = func.funk(x1);
	double fh = func.funk(x2);
	if (fl * fh > 0.0)
	    throw new IllegalArgumentException(
		    "Root must be bracketed in rtflsp");
	if (fl < 0.0) {
	    xl = x1;
	    xh = x2;
	} else {
	    xl = x2;
	    xh = x1;
	    // SWAP(fl,fh);
	    double swap = fl;
	    fl = fh;
	    fh = swap;
	}
	double dx = xh - xl;
	for (int j = 0; j < MAXIT; j++) {
	    double rtf = xl + dx * fl / (fl - fh);
	    double f = func.funk(rtf);
	    if (f < 0.0) {
		del = xl - rtf;
		xl = rtf;
		fl = f;
	    } else {
		del = xh - rtf;
		xh = rtf;
		fh = f;
	    }
	    dx = xh - xl;
	    if (abs(del) < xacc || f == 0.0)
		return rtf;
	}
	throw new IllegalArgumentException(
		"Maximum number of iterations exceeded in rtflsp");
    }

    /**
     * Using the secant method, return the root of a function or functor func
     * thought to lie between x1 and x2. The root is refined until its accuracy
     * is +/-acc.
     * 
     * @param func
     * @param x1
     * @param x2
     * @param xacc
     * @return
     */
    public static double rtsec(final UniVarRealValueFun func, final double x1,
	    final double x2, final double xacc) {
	final int MAXIT = 30;
	double xl, rts;
	double fl = func.funk(x1);
	double f = func.funk(x2);
	if (abs(fl) < abs(f)) {
	    rts = x1;
	    xl = x2;
	    // SWAP(fl,f);
	    double swap = fl;
	    fl = f;
	    f = swap;
	} else {
	    xl = x1;
	    rts = x2;
	}
	for (int j = 0; j < MAXIT; j++) {
	    double dx = (xl - rts) * f / (f - fl);
	    xl = rts;
	    fl = f;
	    rts += dx;
	    f = func.funk(rts);
	    if (abs(dx) < xacc || f == 0.0)
		return rts;
	}
	throw new IllegalArgumentException(
		"Maximum number of iterations exceeded in rtsec");
    }

    /**
     * Using Ridders' method, return the root of a function or functor func
     * known to lie between x1 and x2. The root will be refined to an
     * approximate accuracy xacc.
     * 
     * @param func
     * @param x1
     * @param x2
     * @param xacc
     * @return
     */
    public static double zriddr(final UniVarRealValueFun func, final double x1,
	    final double x2, final double xacc) {
	final int MAXIT = 60;
	double fl = func.funk(x1);
	double fh = func.funk(x2);
	if ((fl > 0.0 && fh < 0.0) || (fl < 0.0 && fh > 0.0)) {
	    double xl = x1;
	    double xh = x2;
	    double ans = -9.99e99;
	    for (int j = 0; j < MAXIT; j++) {
		double xm = 0.5 * (xl + xh);
		double fm = func.funk(xm);
		double s = sqrt(fm * fm - fl * fh);
		if (s == 0.0)
		    return ans;
		double xnew = xm + (xm - xl)
			* ((fl >= fh ? 1.0 : -1.0) * fm / s);
		if (abs(xnew - ans) <= xacc)
		    return ans;
		ans = xnew;
		double fnew = func.funk(ans);
		if (fnew == 0.0)
		    return ans;
		if (SIGN(fm, fnew) != fm) {
		    xl = xm;
		    fl = fm;
		    xh = ans;
		    fh = fnew;
		} else if (SIGN(fl, fnew) != fl) {
		    xh = ans;
		    fh = fnew;
		} else if (SIGN(fh, fnew) != fh) {
		    xl = ans;
		    fl = fnew;
		} else
		    throw new IllegalArgumentException("never get here.");
		if (abs(xh - xl) <= xacc)
		    return ans;
	    }
	    throw new IllegalArgumentException(
		    "zriddr exceed maximum iterations");
	} else {
	    if (fl == 0.0)
		return x1;
	    if (fh == 0.0)
		return x2;
	    throw new IllegalArgumentException(
		    "root must be bracketed in zriddr.");
	}
    }

    /**
     * Using Brent's method, return the root of a function or functor func known
     * to lie between x1 and x2. The root will be refined until its accuracy is
     * tol.
     * 
     * @param func
     * @param x1
     * @param x2
     * @param tol
     * @return
     */
    public static double zbrent(final UniVarRealValueFun func, final double x1,
	    final double x2, final double tol) {
	final int ITMAX = 100;
	final double EPS = DBL_EPSILON;
	double a = x1, b = x2, c = x2, d = 0, e = 0, fa = func.funk(a), fb = func
		.funk(b), fc, p, q, r, s, tol1, xm;
	if ((fa > 0.0 && fb > 0.0) || (fa < 0.0 && fb < 0.0))
	    throw new IllegalArgumentException(
		    "Root must be bracketed in zbrent");
	fc = fb;
	for (int iter = 0; iter < ITMAX; iter++) {
	    if ((fb > 0.0 && fc > 0.0) || (fb < 0.0 && fc < 0.0)) {
		c = a;
		fc = fa;
		e = d = b - a;
	    }
	    if (abs(fc) < abs(fb)) {
		a = b;
		b = c;
		c = a;
		fa = fb;
		fb = fc;
		fc = fa;
	    }
	    tol1 = 2.0 * EPS * abs(b) + 0.5 * tol;
	    xm = 0.5 * (c - b);
	    if (abs(xm) <= tol1 || fb == 0.0)
		return b;
	    if (abs(e) >= tol1 && abs(fa) > abs(fb)) {
		s = fb / fa;
		if (a == c) {
		    p = 2.0 * xm * s;
		    q = 1.0 - s;
		} else {
		    q = fa / fc;
		    r = fb / fc;
		    p = s * (2.0 * xm * q * (q - r) - (b - a) * (r - 1.0));
		    q = (q - 1.0) * (r - 1.0) * (s - 1.0);
		}
		if (p > 0.0)
		    q = -q;
		p = abs(p);
		double min1 = 3.0 * xm * q - abs(tol1 * q);
		double min2 = abs(e * q);
		if (2.0 * p < (min1 < min2 ? min1 : min2)) {
		    e = d;
		    d = p / q;
		} else {
		    d = xm;
		    e = d;
		}
	    } else {
		d = xm;
		e = d;
	    }
	    a = b;
	    fa = fb;
	    if (abs(d) > tol1)
		b += d;
	    else
		b += SIGN(tol1, xm);
	    fb = func.funk(b);
	}
	throw new IllegalArgumentException(
		"Maximum number of iterations exceeded in zbrent");
    }

    /**
     * Using the Newton-Raphson method, return the root of a function known to
     * lie in the interval [x1, x2]. The root will be refined until its accuracy
     * is known within +/-acc. funcd is a user- supplied struct that returns the
     * function value as a functor and the first derivative of the function at
     * the point x as the function df.
     * 
     * @param funcd
     * @param x1
     * @param x2
     * @param xacc
     * @return
     */
    public static double rtnewt(final UniValRealValueFunWithDiff funcd,
	    final double x1, final double x2, final double xacc) {
	final int JMAX = 20;
	double rtn = 0.5 * (x1 + x2);
	for (int j = 0; j < JMAX; j++) {
	    double f = funcd.funk(rtn);
	    double df = funcd.df(rtn);
	    double dx = f / df;
	    rtn -= dx;
	    if ((x1 - rtn) * (rtn - x2) < 0.0)
		throw new IllegalArgumentException(
			"Jumped out of brackets in rtnewt");
	    if (abs(dx) < xacc)
		return rtn;
	}
	throw new IllegalArgumentException(
		"Maximum number of iterations exceeded in rtnewt");
    }

    /**
     * Using a combination of Newton-Raphson and bisection, return the root of a
     * function bracketed between x1 and x2. The root will be refined until its
     * accuracy is known within +/-acc. funcd is a user-supplied struct that
     * returns the function value as a functor and the first derivative of the
     * function at the point x as the function df.
     * 
     * @param funcd
     * @param x1
     * @param x2
     * @param xacc
     * @return
     */
    public static double rtsafe(final UniValRealValueFunWithDiff funcd,
	    final double x1, final double x2, final double xacc) {
	final int MAXIT = 100;
	double xh, xl;
	double fl = funcd.funk(x1);
	double fh = funcd.funk(x2);
	if ((fl > 0.0 && fh > 0.0) || (fl < 0.0 && fh < 0.0))
	    throw new IllegalArgumentException(
		    "Root must be bracketed in rtsafe");
	if (fl == 0.0)
	    return x1;
	if (fh == 0.0)
	    return x2;
	if (fl < 0.0) {
	    xl = x1;
	    xh = x2;
	} else {
	    xh = x1;
	    xl = x2;
	}
	double rts = 0.5 * (x1 + x2);
	double dxold = abs(x2 - x1);
	double dx = dxold;
	double f = funcd.funk(rts);
	double df = funcd.df(rts);
	for (int j = 0; j < MAXIT; j++) {
	    if ((((rts - xh) * df - f) * ((rts - xl) * df - f) > 0.0)
		    || (abs(2.0 * f) > abs(dxold * df))) {
		dxold = dx;
		dx = 0.5 * (xh - xl);
		rts = xl + dx;
		if (xl == rts)
		    return rts;
	    } else {
		dxold = dx;
		dx = f / df;
		double temp = rts;
		rts -= dx;
		if (temp == rts)
		    return rts;
	    }
	    if (abs(dx) < xacc)
		return rts;
	    f = funcd.funk(rts);
	    df = funcd.df(rts);
	    if (f < 0.0)
		xl = rts;
	    else
		xh = rts;
	}
	throw new IllegalArgumentException(
		"Maximum number of iterations exceeded in rtsafe");
    }

    /**
     * Bairstow's method for complex root
     * 
     * Given n+1 coefficients p[0..n] of a polynomial of degree n, and trial
     * values for the coefficients of a quadratic factor x*x+b*x+c, improve the
     * solution until the coefficients b,c change by less than eps.
     * 
     * 
     * @param p
     * @param b
     * @param c
     * @param eps
     */
    public static void qroot(final double[] p, final doubleW b,
	    final doubleW c, final double eps) {
	final int ITMAX = 20;
	final double TINY = 1.0e-14;
	double sc, sb, s, rc, rb, r, dv, delc, delb;
	int n = p.length - 1;
	double[] d = new double[3], q = new double[n + 1], qq = new double[n + 1], rem = new double[n + 1];
	d[2] = 1.0;
	for (int iter = 0; iter < ITMAX; iter++) {
	    d[1] = b.val;
	    d[0] = c.val;
	    Poly.poldiv(p, d, q, rem);
	    s = rem[0];
	    r = rem[1];
	    Poly.poldiv(q, d, qq, rem);
	    sb = -c.val * (rc = -rem[1]);
	    rb = -b.val * rc + (sc = -rem[0]);
	    dv = 1.0 / (sb * rc - sc * rb);
	    delb = (r * sc - s * rc) * dv;
	    delc = (-r * sb + s * rb) * dv;
	    b.val += delb;
	    c.val += delc;
	    if ((abs(delb) <= eps * abs(b.val) || abs(b.val) < TINY)
		    && (abs(delc) <= eps * abs(c.val) || abs(c.val) < TINY)) {
		return;
	    }
	}
	throw new IllegalArgumentException(
		"Too many iterations in routine qroot");
    }

    /**
     * polynomial roots by eigenvalue method
     * 
     * Find all the roots of a polynomial with real coefficients,
     * S(i=0,m)a[i]x^i, given the coefficients a[0..m]. The method is to
     * construct an upper Hessenberg matrix whose eigenvalues are the desired
     * roots and then use the routine Unsymmeig. The roots are returned in the
     * complex vector rt[0..m-1], sorted in descending order by their real
     * parts.
     * 
     * @param a
     * @param rt
     */
    public static void zrhqr(final double[] a, final Complex[] rt) {
	int m = a.length - 1;
	double[][] hess = new double[m][m];
	for (int k = 0; k < m; k++) {
	    hess[0][k] = -a[m - k - 1] / a[m];
	    for (int j = 1; j < m; j++)
		hess[j][k] = 0.0;
	    if (k != m - 1)
		hess[k + 1][k] = 1.0;
	}
	Unsymmeig h = new Unsymmeig(hess, false, true);
	for (int j = 0; j < m; j++)
	    rt[j] = h.wri[j];
    }

    private static final double[] frac = { 0.0, 0.5, 0.25, 0.75, 0.13, 0.38,
	    0.62, 0.88, 1.0 };

    /**
     * Given the m+1 complex coefficients a[0..m] of the polynomial
     * S(i=0,m)a[i]x^i, and given a complex value x, this routine improves x by
     * Laguerre's method until it converges, within the achievable roundoff
     * limit, to a root of the given polynomial. The number of iterations taken
     * is returned as its.
     * 
     * @param a
     * @param x
     * @param its
     * @return
     */
    public static Complex laguer(final Complex[] a, final Complex xx,
	    final intW its) {
	Complex x = xx;
	final int MR = 8, MT = 10, MAXIT = MT * MR;
	final double EPS = DBL_EPSILON;
	Complex dx = new Complex(0, 0), x1 = new Complex(0, 0), b = new Complex(
		0, 0), d = new Complex(0, 0), f = new Complex(0, 0), g = new Complex(
		0, 0), h = new Complex(0, 0), sq = new Complex(0, 0), gp = new Complex(
		0, 0), gm = new Complex(0, 0), g2 = new Complex(0, 0);
	int m = a.length - 1;
	for (int iter = 1; iter <= MAXIT; iter++) {
	    its.val = iter;
	    b = a[m];
	    double err = b.mod();
	    d = new Complex(0, 0);
	    f = new Complex(0, 0);
	    double abx = x.mod();
	    for (int j = m - 1; j >= 0; j--) {
		f = x.mul(f).add(d);
		d = x.mul(d).add(b);
		b = x.mul(b).add(a[j]);
		err = b.mod() + abx * err;
	    }
	    err *= EPS;
	    if (b.mod() <= err)
		return x;
	    g = d.div(b);
	    g2 = g.mul(g);
	    h = g2.sub(f.div(b).mul(2.0));
	    sq = (h.mul(m).sub(g2).mul(m - 1)).sqrt();
	    // sq=sqrt((m-1)*(m*h-g2));
	    gp = g.add(sq);
	    gm = g.sub(sq);
	    double abp = gp.mod();
	    double abm = gm.mod();
	    if (abp < abm)
		gp = gm;
	    dx = max(abp, abm) > 0.0 ? new Complex(m, 0).div(gp) : Complex
		    .polar(1 + abx, iter); // polar
	    x1 = x.sub(dx);
	    if (x.equals(x1))
		return x; // XXX "==" must to be replace to equals
	    if (iter % MT != 0)
		x = x1;
	    else {
		x = x.sub(new Complex(frac[iter / MT], 0).div(dx));
	    }
	}
	throw new IllegalArgumentException("too many iterations in laguer");
    }

    /**
     * roots of a polynomial - PTC
     * 
     * Given the m+1 complex coefficients a[0..m] of the polynomial
     * S(i=0,m)a[i]x^i, this routine successively calls laguer and finds all m
     * complex roots in roots[0..m-1]. The boolean variable polish should be
     * input as true if polishing (also by Laguerre's method) is desired, false
     * if the roots will be subsequently polished by other means.
     * 
     * 
     * @param a
     * @param roots
     * @param polish
     */
    public static void zroots(final Complex[] a, final Complex[] roots,
	    final boolean polish) {
	final double EPS = 1.0e-14;
	int i;
	intW its = new intW(0);
	Complex x = new Complex(0, 0), b = new Complex(0, 0), c = new Complex(
		0, 0);
	int m = a.length - 1;
	Complex[] ad = new Complex[m + 1];
	for (i = 0; i < ad.length; i++)
	    ad[i] = new Complex(0, 0);
	for (int j = 0; j <= m; j++)
	    ad[j] = a[j];
	for (int j = m - 1; j >= 0; j--) {
	    x = new Complex(0.0, 0);
	    Complex[] ad_v = new Complex[j + 2];
	    for (int jj = 0; jj < j + 2; jj++)
		ad_v[jj] = ad[jj];
	    x = laguer(ad_v, x, its);
	    if (abs(x.im()) <= 2.0 * EPS * abs(x.re()))
		x = new Complex(x.re(), 0.0);
	    roots[j] = x;
	    b = ad[j + 1];
	    for (int jj = j; jj >= 0; jj--) {
		c = ad[jj];
		ad[jj] = b;
		b = x.mul(b).add(c);
	    }
	}
	if (polish)
	    for (int j = 0; j < m; j++)
		roots[j] = laguer(a, roots[j], its);
	for (int j = 1; j < m; j++) {
	    x = roots[j];
	    for (i = j - 1; i >= 0; i--) {
		if (roots[i].re() <= x.re())
		    break;
		roots[i + 1] = roots[i];
	    }
	    roots[i + 1] = x;
	}
    }

    /**
     * Given an n-dimensional point xold[0..n-1], the value of the function and
     * gradient there, fold and g[0..n-1], and a direction p[0..n-1], finds a
     * new point x[0..n-1] along the direction p from xold where the function or
     * functor func has decreased "sufficiently." The new function value is
     * returned in f. stpmax is an input quantity that limits the length of the
     * steps so that you do not try to evaluate the function in regions where it
     * is undefined or subject to overflow. p is usually the Newton direction.
     * The output quantity check is false on a normal exit. It is true when x is
     * too close to xold. In a minimization algorithm, this usually signals
     * convergence and can be ignored. However, in a zero-finding algorithm the
     * calling program should check whether the convergence is spurious.
     * 
     * @param xold
     * @param fold
     * @param g
     * @param p
     * @param x
     * @param f
     * @param stpmax
     * @param check
     * @param func
     */
    public static void lnsrch(final double[] xold, final double fold,
	    final double[] g, final double[] p, final double[] x,
	    final doubleW f, final double stpmax, final booleanW check,
	    final RealValueFun func) {
	final double ALF = 1.0e-4, TOLX = DBL_EPSILON;
	double a, alam, alam2 = 0.0, alamin, b, disc, f2 = 0.0;
	double rhs1, rhs2, slope = 0.0, sum = 0.0, temp, test, tmplam;
	int i, n = xold.length;
	check.val = false;
	for (i = 0; i < n; i++)
	    sum += p[i] * p[i];
	sum = sqrt(sum);
	if (sum > stpmax)
	    for (i = 0; i < n; i++)
		p[i] *= stpmax / sum;
	for (i = 0; i < n; i++)
	    slope += g[i] * p[i];
	if (slope >= 0.0)
	    throw new IllegalArgumentException("Roundoff problem in lnsrch.");
	test = 0.0;
	for (i = 0; i < n; i++) {
	    temp = abs(p[i]) / max(abs(xold[i]), 1.0);
	    if (temp > test)
		test = temp;
	}
	alamin = TOLX / test;
	alam = 1.0;
	for (;;) {
	    for (i = 0; i < n; i++)
		x[i] = xold[i] + alam * p[i];
	    f.val = func.funk(x);
	    if (alam < alamin) {
		for (i = 0; i < n; i++)
		    x[i] = xold[i];
		check.val = true;
		return;
	    } else if (f.val <= fold + ALF * alam * slope)
		return;
	    else {
		if (alam == 1.0)
		    tmplam = -slope / (2.0 * (f.val - fold - slope));
		else {
		    rhs1 = f.val - fold - alam * slope;
		    rhs2 = f2 - fold - alam2 * slope;
		    a = (rhs1 / (alam * alam) - rhs2 / (alam2 * alam2))
			    / (alam - alam2);
		    b = (-alam2 * rhs1 / (alam * alam) + alam * rhs2
			    / (alam2 * alam2))
			    / (alam - alam2);
		    if (a == 0.0)
			tmplam = -slope / (2.0 * b);
		    else {
			disc = b * b - 3.0 * a * slope;
			if (disc < 0.0)
			    tmplam = 0.5 * alam;
			else if (b <= 0.0)
			    tmplam = (-b + sqrt(disc)) / (3.0 * a);
			else
			    tmplam = -slope / (b + sqrt(disc));
		    }
		    if (tmplam > 0.5 * alam)
			tmplam = 0.5 * alam;
		}
	    }
	    alam2 = alam;
	    f2 = f.val;
	    alam = max(tmplam, 0.1 * alam);
	}
    }

    /**
     * Given an initial guess x[0..n-1] for a root in n dimensions, find the
     * root by a globally convergent Newton's method. The vector of functions to
     * be zeroed, called fvec[0..n-1] in the routine below, is returned by the
     * user-supplied function or functor vecfunc (see text). The output quantity
     * check is false on a normal return and true if the routine has converged
     * to a local minimum of the function fmin defined below. In this case try
     * restarting from a different initial guess.
     * 
     * @param x
     * @param check
     * @param vecfunc
     */
    public static void newt(final double[] x, final booleanW check,
	    final RealMultiValueFun vecfunc) {
	final int MAXITS = 200;
	final double TOLF = 1.0e-8, TOLMIN = 1.0e-12, STPMX = 100.0;
	final double TOLX = DBL_EPSILON;
	int i, j, its, n = x.length;
	double den, fold, stpmax, sum, temp, test;
	doubleW f = new doubleW(0);
	double[] g = new double[n], p = new double[n], xold = new double[n];
	double[][] fjac = new double[n][n];
	NRfmin fmin = new NRfmin(vecfunc);
	NRfdjac fdjac = new NRfdjac(vecfunc);
	f.val = fmin.funk(x); // XXX init fmin.fvec first.
	double[] fvec = fmin.fvec;
	test = 0.0;
	for (i = 0; i < n; i++)
	    if (abs(fvec[i]) > test)
		test = abs(fvec[i]);
	if (test < 0.01 * TOLF) {
	    check.val = false;
	    return;
	}
	sum = 0.0;
	for (i = 0; i < n; i++)
	    sum += SQR(x[i]);
	stpmax = STPMX * max(sqrt(sum), n);
	for (its = 0; its < MAXITS; its++) {
	    fjac = fdjac.get(x, fvec);
	    for (i = 0; i < n; i++) {
		sum = 0.0;
		for (j = 0; j < n; j++)
		    sum += fjac[j][i] * fvec[j];
		g[i] = sum;
	    }
	    for (i = 0; i < n; i++)
		xold[i] = x[i];
	    fold = f.val;
	    for (i = 0; i < n; i++)
		p[i] = -fvec[i];
	    LUdcmp alu = new LUdcmp(fjac);
	    alu.solve(p, p);
	    lnsrch(xold, fold, g, p, x, f, stpmax, check, fmin);
	    test = 0.0;
	    for (i = 0; i < n; i++)
		if (abs(fvec[i]) > test)
		    test = abs(fvec[i]);
	    if (test < TOLF) {
		check.val = false;
		return;
	    }
	    if (check.val) {
		test = 0.0;
		den = max(f.val, 0.5 * n);
		for (i = 0; i < n; i++) {
		    temp = abs(g[i]) * max(abs(x[i]), 1.0) / den;
		    if (temp > test)
			test = temp;
		}
		check.val = (test < TOLMIN);
		return;
	    }
	    test = 0.0;
	    for (i = 0; i < n; i++) {
		temp = (abs(x[i] - xold[i])) / max(abs(x[i]), 1.0);
		if (temp > test)
		    test = temp;
	    }
	    if (test < TOLX)
		return;
	}
	throw new IllegalArgumentException("MAXITS exceeded in newt");
    }

    /**
     * Given an initial guess x[0..n-1] for a root in n dimensions, find the
     * root by Broyden's method embedded in a globally convergent strategy. The
     * vector of functions to be zeroed, called fvec[0..n-1] in the routine
     * below, is returned by the user-supplied function or functor vecfunc. The
     * routines NRfdjac and NRfmin from newt are used. The output quantity check
     * is false on a normal return and true if the routine has converged to a
     * local minimum of the function fmin or if Broyden's method can make no
     * further progress. In this case try restarting from a different initial
     * guess.
     * 
     * @param x
     * @param check
     * @param vecfunc
     */
    public static void broydn(final double[] x, final booleanW check,
	    final RealMultiValueFun vecfunc) {
	final int MAXITS = 200;
	final double EPS = DBL_EPSILON;
	final double TOLF = 1.0e-8, TOLX = EPS, STPMX = 100.0, TOLMIN = 1.0e-12;
	boolean restrt, skip;
	int i, its, j, n = x.length;
	double den, fold, stpmax, sum, temp, test;
	doubleW f = new doubleW(0);
	double[] fvcold = new double[n], g = new double[n], p = new double[n], s = new double[n], t = new double[n], w = new double[n], xold = new double[n];
	QRdcmp qr = null;
	NRfmin fmin = new NRfmin(vecfunc);
	NRfdjac fdjac = new NRfdjac(vecfunc);
	f.val = fmin.funk(x); // XXX init fmin.fvec first.
	double[] fvec = fmin.fvec;
	test = 0.0;
	for (i = 0; i < n; i++)
	    if (abs(fvec[i]) > test)
		test = abs(fvec[i]);
	if (test < 0.01 * TOLF) {
	    check.val = false;
	    return;
	}
	for (sum = 0.0, i = 0; i < n; i++)
	    sum += SQR(x[i]);
	stpmax = STPMX * max(sqrt(sum), n);
	restrt = true;
	for (its = 1; its <= MAXITS; its++) {
	    if (restrt) {
		qr = new QRdcmp(fdjac.get(x, fvec));
		if (qr.sing) {
		    double[][] one = new double[n][n];
		    for (i = 0; i < n; i++)
			one[i][i] = 1.0;
		    qr = new QRdcmp(one);
		}
	    } else {
		for (i = 0; i < n; i++)
		    s[i] = x[i] - xold[i];
		for (i = 0; i < n; i++) {
		    for (sum = 0.0, j = i; j < n; j++)
			sum += qr.r[i][j] * s[j];
		    t[i] = sum;
		}
		skip = true;
		for (i = 0; i < n; i++) {
		    for (sum = 0.0, j = 0; j < n; j++)
			sum += qr.qt[j][i] * t[j];
		    w[i] = fvec[i] - fvcold[i] - sum;
		    if (abs(w[i]) >= EPS * (abs(fvec[i]) + abs(fvcold[i])))
			skip = false;
		    else
			w[i] = 0.0;
		}
		if (!skip) {
		    qr.qtmult(w, t);
		    for (den = 0.0, i = 0; i < n; i++)
			den += SQR(s[i]);
		    for (i = 0; i < n; i++)
			s[i] /= den;
		    qr.update(t, s);
		    if (qr.sing)
			throw new IllegalArgumentException(
				"singular update in broydn");
		}
	    }
	    qr.qtmult(fvec, p);
	    for (i = 0; i < n; i++)
		p[i] = -p[i];
	    for (i = n - 1; i >= 0; i--) {
		for (sum = 0.0, j = 0; j <= i; j++)
		    sum -= qr.r[j][i] * p[j];
		g[i] = sum;
	    }
	    for (i = 0; i < n; i++) {
		xold[i] = x[i];
		fvcold[i] = fvec[i];
	    }
	    fold = f.val;
	    qr.rsolve(p, p);
	    double slope = 0.0;
	    for (i = 0; i < n; i++)
		slope += g[i] * p[i];
	    if (slope >= 0.0) {
		restrt = true;
		continue;
	    }
	    lnsrch(xold, fold, g, p, x, f, stpmax, check, fmin);
	    test = 0.0;
	    for (i = 0; i < n; i++)
		if (abs(fvec[i]) > test)
		    test = abs(fvec[i]);
	    if (test < TOLF) {
		check.val = false;
		return;
	    }
	    if (check.val) {
		if (restrt) {
		    return;
		} else {
		    test = 0.0;
		    den = max(f.val, 0.5 * n);
		    for (i = 0; i < n; i++) {
			temp = abs(g[i]) * max(abs(x[i]), 1.0) / den;
			if (temp > test)
			    test = temp;
		    }
		    if (test < TOLMIN) {
			return;
		    } else
			restrt = true;
		}
	    } else {
		restrt = false;
		test = 0.0;
		for (i = 0; i < n; i++) {
		    temp = (abs(x[i] - xold[i])) / max(abs(x[i]), 1.0);
		    if (temp > test)
			test = temp;
		}
		if (test < TOLX) {
		    return;
		}
	    }
	}
	throw new IllegalArgumentException("MAXITS exceeded in broydn");
    }

}
