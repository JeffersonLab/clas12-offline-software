package com.nr.sf;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;

public class Gamma implements Gauleg18 {
    private static final int ASWITCH = 100;
    private static final double EPS = DBL_EPSILON;;
    private static final double FPMIN = Double.MIN_NORMAL / EPS;
    private double gln;

    public double gammp(final double a, final double x) {
	if (x < 0.0 || a <= 0.0)
	    throw new IllegalArgumentException("bad args in gammp");
	if (x == 0.0)
	    return 0.0;
	else if ((int) a >= ASWITCH)
	    return gammpapprox(a, x, 1);
	else if (x < a + 1.0)
	    return gser(a, x);
	else
	    return 1.0 - gcf(a, x);
    }

    public double gammq(final double a, final double x) {
	if (x < 0.0 || a <= 0.0)
	    throw new IllegalArgumentException("bad args in gammq");
	if (x == 0.0)
	    return 1.0;
	else if ((int) a >= ASWITCH)
	    return gammpapprox(a, x, 0);
	else if (x < a + 1.0)
	    return 1.0 - gser(a, x);
	else
	    return gcf(a, x);
    }

    public double gser(final double a, final double x) {
	double sum, del, ap;
	gln = gammln(a);
	ap = a;
	del = sum = 1.0 / a;
	for (;;) {
	    ++ap;
	    del *= x / ap;
	    sum += del;
	    if (abs(del) < abs(sum) * EPS) {
		return sum * exp(-x + a * log(x) - gln);
	    }
	}
    }

    public double gcf(final double a, final double x) {
	int i;
	double an, b, c, d, del, h;
	gln = gammln(a);
	b = x + 1.0 - a;
	c = 1.0 / FPMIN;
	d = 1.0 / b;
	h = d;
	for (i = 1;; i++) {
	    an = -i * (i - a);
	    b += 2.0;
	    d = an * d + b;
	    if (abs(d) < FPMIN)
		d = FPMIN;
	    c = b + an / c;
	    if (abs(c) < FPMIN)
		c = FPMIN;
	    d = 1.0 / d;
	    del = d * c;
	    h *= del;
	    if (abs(del - 1.0) <= EPS)
		break;
	}
	return exp(-x + a * log(x) - gln) * h;
    }

    public double gammpapprox(final double a, final double x, final int psig) {
	int j;
	double xu, t, sum, ans;
	double a1 = a - 1.0, lna1 = log(a1), sqrta1 = sqrt(a1);
	gln = gammln(a);
	if (x > a1)
	    xu = max(a1 + 11.5 * sqrta1, x + 6.0 * sqrta1);
	else
	    xu = max(0., min(a1 - 7.5 * sqrta1, x - 5.0 * sqrta1));
	sum = 0;
	for (j = 0; j < ngau; j++) {
	    t = x + (xu - x) * y[j];
	    sum += w[j] * exp(-(t - a1) + a1 * (log(t) - lna1));
	}
	ans = sum * (xu - x) * exp(a1 * (lna1 - 1.) - gln);
	return (psig != 0 ? (x > a1 ? 1.0 - ans : -ans) : (x > a1 ? ans
		: 1.0 + ans));
    }

    public double invgammp(final double p, final double a) {
	int j;
	double x, err, t, u, pp, lna1 = 0, afac = 0, a1 = a - 1;
	final double EPS = 1.e-8;
	gln = gammln(a);
	if (a <= 0.)
	    throw new IllegalArgumentException("a must be pos in invgammap");
	if (p >= 1.)
	    return max(100., a + 100. * sqrt(a));
	if (p <= 0.)
	    return 0.0;
	if (a > 1.) {
	    lna1 = log(a1);
	    afac = exp(a1 * (lna1 - 1.) - gln);
	    pp = (p < 0.5) ? p : 1. - p;
	    t = sqrt(-2. * log(pp));
	    x = (2.30753 + t * 0.27061) / (1. + t * (0.99229 + t * 0.04481))
		    - t;
	    if (p < 0.5)
		x = -x;
	    x = max(1.e-3, a * pow(1. - 1. / (9. * a) - x / (3. * sqrt(a)), 3));
	} else {
	    t = 1.0 - a * (0.253 + a * 0.12);
	    if (p < t)
		x = pow(p / t, 1. / a);
	    else
		x = 1. - log(1. - (p - t) / (1. - t));
	}
	for (j = 0; j < 12; j++) {
	    if (x <= 0.0)
		return 0.0;
	    err = gammp(a, x) - p;
	    if (a > 1.)
		t = afac * exp(-(x - a1) + a1 * (log(x) - lna1));
	    else
		t = exp(-x + a1 * log(x) - gln);
	    u = err / t;
	    x -= (t = u / (1. - 0.5 * min(1., u * ((a - 1.) / x - 1))));
	    if (x <= 0.)
		x = 0.5 * (x + t);
	    if (abs(t) < EPS * x)
		break;
	}
	return x;
    }

    private static final double[] cof = { 57.1562356658629235,
	    -59.5979603554754912, 14.1360979747417471, -0.491913816097620199,
	    .339946499848118887e-4, .465236289270485756e-4,
	    -.983744753048795646e-4, .158088703224912494e-3,
	    -.210264441724104883e-3, .217439618115212643e-3,
	    -.164318106536763890e-3, .844182239838527433e-4,
	    -.261908384015814087e-4, .368991826595316234e-5 };
    private static double[] a = new double[171];

    private static final int NTOP = 2000;
    private static double[] aa = new double[NTOP];

    static {
	a[0] = 1.;
	for (int i = 1; i < 171; i++)
	    a[i] = i * a[i - 1];

	for (int i = 0; i < NTOP; i++)
	    aa[i] = gammln(i + 1.);
    }

    public static double gammln(final double xx) {
	int j;
	double x, tmp, y, ser;
	if (xx <= 0)
	    throw new IllegalArgumentException("bad arg in gammln");
	y = x = xx;
	tmp = x + 5.24218750000000000; // Rational 671/128
	tmp = (x + 0.5) * log(tmp) - tmp;
	ser = 0.999999999999997092;
	for (j = 0; j < 14; j++)
	    ser += cof[j] / ++y;
	return tmp + log(2.5066282746310005 * ser / x);
    }

    /**
     * Returns the value n! as a floating-point number.
     * 
     * @param n
     * @return
     */
    public static double factrl(final int n) {
	if (n < 0 || n > 170)
	    throw new IllegalArgumentException("factrl out of range");
	return a[n];
    }

    /**
     * Returns ln(n!)
     * 
     * @param n
     * @return
     */
    public static double factln(final int n) {
	if (n < 0)
	    throw new IllegalArgumentException("negative arg in factln");
	if (n < NTOP)
	    return aa[n];
	return gammln(n + 1.);
    }

    /**
     * Returns the binomial coefficient (n,k) as a floating-point number.
     * 
     * @param n
     * @param k
     * @return
     */
    public static double bico(final int n, final int k) {
	if (n < 0 || k < 0 || k > n)
	    throw new IllegalArgumentException("bad args in bico");
	if (n < 171)
	    return floor(0.5 + factrl(n) / (factrl(k) * factrl(n - k)));
	return floor(0.5 + exp(factln(n) - factln(k) - factln(n - k)));
    }
}
