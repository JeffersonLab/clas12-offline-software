package com.nr.sf;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;
import com.nr.Complex;

public class Integrals {
    private Integrals() {
    }

    /**
     * Exponential Integrals
     * 
     * @param n
     * @param x
     * @return
     */
    public static double expint(final int n, final double x) {
	final int MAXIT = 100;
	final double EULER = 0.577215664901533, EPS = DBL_EPSILON, BIG = Double.MAX_VALUE
		* EPS;
	int i, ii, nm1 = n - 1;
	double a, b, c, d, del, fact, h, psi, ans;
	if (n < 0 || x < 0.0 || (x == 0.0 && (n == 0 || n == 1)))
	    throw new IllegalArgumentException("bad arguments in expint");
	if (n == 0)
	    ans = exp(-x) / x;
	else {
	    if (x == 0.0)
		ans = 1.0 / nm1;
	    else {
		if (x > 1.0) {
		    b = x + n;
		    c = BIG;
		    d = 1.0 / b;
		    h = d;
		    for (i = 1; i <= MAXIT; i++) {
			a = -i * (nm1 + i);
			b += 2.0;
			d = 1.0 / (a * d + b);
			c = b + a / c;
			del = c * d;
			h *= del;
			if (abs(del - 1.0) <= EPS) {
			    ans = h * exp(-x);
			    return ans;
			}
		    }
		    throw new IllegalArgumentException(
			    "continued fraction failed in expint");
		} else {
		    ans = (nm1 != 0 ? 1.0 / nm1 : -log(x) - EULER);
		    fact = 1.0;
		    for (i = 1; i <= MAXIT; i++) {
			fact *= -x / i;
			if (i != nm1)
			    del = -fact / (i - nm1);
			else {
			    psi = -EULER;
			    for (ii = 1; ii <= nm1; ii++)
				psi += 1.0 / ii;
			    del = fact * (-log(x) + psi);
			}
			ans += del;
			if (abs(del) < abs(ans) * EPS)
			    return ans;
		    }
		    throw new IllegalArgumentException(
			    "series failed in expint");
		}
	    }
	}
	return ans;
    }

    /**
     * Exponential Integrals
     * 
     * @param x
     * @return
     */
    public static double ei(final double x) {
	final int MAXIT = 100;
	final double EULER = 0.577215664901533, EPS = DBL_EPSILON, FPMIN = Double.MIN_NORMAL
		/ EPS;
	int k;
	double fact, prev, sum, term;
	if (x <= 0.0)
	    throw new IllegalArgumentException("Bad argument in ei");
	if (x < FPMIN)
	    return log(x) + EULER;
	if (x <= -log(EPS)) {
	    sum = 0.0;
	    fact = 1.0;
	    for (k = 1; k <= MAXIT; k++) {
		fact *= x / k;
		term = fact / k;
		sum += term;
		if (term < EPS * sum)
		    break;
	    }
	    if (k > MAXIT)
		throw new IllegalArgumentException("Series failed in ei");
	    return sum + log(x) + EULER;
	} else {
	    sum = 0.0;
	    term = 1.0;
	    for (k = 1; k <= MAXIT; k++) {
		prev = term;
		term *= k / x;
		if (term < EPS)
		    break;
		if (term < prev)
		    sum += term;
		else {
		    sum -= prev;
		    break;
		}
	    }
	    return exp(x) * (1.0 + sum) / x;
	}
    }

    /**
     * Fermi-Dirac integrals
     * 
     * @param x
     * @return
     */
    public static Complex frenel(final double x) {
	final int MAXIT = 100;
	final double PIBY2 = (PI / 2.0), XMIN = 1.5, EPS = DBL_EPSILON, FPMIN = Double.MIN_NORMAL, BIG = Double.MAX_VALUE
		* EPS;
	boolean odd;
	int k, n;
	double a, ax, fact, pix2, sign, sum, sumc, sums, term, test;
	Complex b, cc, d, h, del, cs;
	if ((ax = abs(x)) < sqrt(FPMIN)) {
	    cs = new Complex(ax, 0);
	} else if (ax <= XMIN) {
	    sum = sums = 0.0;
	    sumc = ax;
	    sign = 1.0;
	    fact = PIBY2 * ax * ax;
	    odd = true;
	    term = ax;
	    n = 3;
	    for (k = 1; k <= MAXIT; k++) {
		term *= fact / k;
		sum += sign * term / n;
		test = abs(sum) * EPS;
		if (odd) {
		    sign = -sign;
		    sums = sum;
		    sum = sumc;
		} else {
		    sumc = sum;
		    sum = sums;
		}
		if (term < test)
		    break;
		odd = !odd;
		n += 2;
	    }
	    if (k > MAXIT)
		throw new IllegalArgumentException("series failed in frenel");
	    cs = new Complex(sumc, sums);
	} else {
	    pix2 = PI * ax * ax;
	    b = new Complex(1.0, -pix2);
	    cc = new Complex(BIG, 0);
	    d = new Complex(1.0, 0).div(b);
	    h = new Complex(1.0, 0).div(b);

	    n = -1;
	    for (k = 2; k <= MAXIT; k++) {
		n += 2;
		a = -n * (n + 1);
		b = b.add(new Complex(4.0, 0));
		d = new Complex(1.0, 0).div(new Complex(a, 0).mul(d).add(b)); // d=1.0/(a*d+b);
		cc = b.add(new Complex(a, 0).div(cc));
		del = cc.mul(d);
		h = h.mul(del);
		if (abs(del.re() - 1.0) + abs(del.im()) <= EPS)
		    break;
	    }
	    if (k > MAXIT)
		throw new IllegalArgumentException("cf failed in frenel");
	    h = h.mul(new Complex(ax, -ax));
	    cs = new Complex(0.5, 0.5)
		    .mul((new Complex(1.0, 0).sub((new Complex(cos(0.5 * pix2),
			    sin(0.5 * pix2)).mul(h)))));
	}
	if (x < 0.0)
	    cs = cs.neg();
	return cs;
    }

    /**
     * cosine and sine integrals
     * 
     * @param x
     * @return
     */
    public static Complex cisi(final double x) {
	final int MAXIT = 100;
	final double EULER = 0.577215664901533, PIBY2 = 1.570796326794897, TMIN = 2.0, EPS = DBL_EPSILON, FPMIN = Double.MIN_NORMAL * 4.0, BIG = Double.MAX_VALUE
		* EPS;
	int i, k;
	boolean odd;
	double a, err, fact, sign, sum, sumc, sums, t, term;
	Complex h, b, c, d, del, cs;
	if ((t = abs(x)) == 0.0)
	    return new Complex(-BIG, 0);
	if (t > TMIN) {
	    b = new Complex(1.0, t);
	    c = new Complex(BIG, 0.0);
	    d = new Complex(1.0, 0).div(b);
	    h = new Complex(1.0, 0).div(b);
	    for (i = 1; i < MAXIT; i++) {
		a = -i * i;
		b = b.add(new Complex(2.0, 0));
		d = new Complex(1.0, 0).div(new Complex(a, 0).mul(d).add(b)); // d=1.0/(a*d+b);
		c = b.add(new Complex(a, 0).div(c)); // c=b+a/c;
		del = c.mul(d);
		h = h.mul(del);
		if (abs(del.re() - 1.0) + abs(del.im()) <= EPS)
		    break;
	    }
	    if (i >= MAXIT)
		throw new IllegalArgumentException("cf failed in cisi");
	    h = new Complex(cos(t), -sin(t)).mul(h);
	    // cs= -conj(h)+Complex(0.0,PIBY2);
	    cs = new Complex(0.0, PIBY2).sub(h.conj());
	} else {
	    if (t < sqrt(FPMIN)) {
		sumc = 0.0;
		sums = t;
	    } else {
		sum = sums = sumc = 0.0;
		sign = fact = 1.0;
		odd = true;
		for (k = 1; k <= MAXIT; k++) {
		    fact *= t / k;
		    term = fact / k;
		    sum += sign * term;
		    err = term / abs(sum);
		    if (odd) {
			sign = -sign;
			sums = sum;
			sum = sumc;
		    } else {
			sumc = sum;
			sum = sums;
		    }
		    if (err < EPS)
			break;
		    odd = !odd;
		}
		if (k > MAXIT)
		    throw new IllegalArgumentException(
			    "maxits exceeded in cisi");
	    }
	    cs = new Complex(sumc + log(t) + EULER, sums);
	}
	if (x < 0.0)
	    cs = cs.conj();
	return cs;
    }

    private static final int NMAX = 6;
    private static double[] c = new double[NMAX];
    static {
	final double H = 0.4;
	for (int i = 0; i < NMAX; i++)
	    c[i] = exp(-SQR((2.0 * i + 1.0) * H));
    }

    /**
     * Dawson's integral
     * 
     * @param x
     * @return
     */
    public static double dawson(final double x) {
	final double H = 0.4, A1 = 2.0 / 3.0, A2 = 0.4, A3 = 2.0 / 7.0;
	int i, n0;
	double d1, d2, e1, e2, sum, x2, xp, xx, ans;
	if (abs(x) < 0.2) {
	    x2 = x * x;
	    ans = x * (1.0 - A1 * x2 * (1.0 - A2 * x2 * (1.0 - A3 * x2)));
	} else {
	    xx = abs(x);
	    n0 = 2 * (int) (0.5 * xx / H + 0.5);
	    xp = xx - n0 * H;
	    e1 = exp(2.0 * xp * H);
	    e2 = e1 * e1;
	    d1 = n0 + 1;
	    d2 = d1 - 2.0;
	    sum = 0.0;
	    for (i = 0; i < NMAX; i++, d1 += 2.0, d2 -= 2.0, e1 *= e2)
		sum += c[i] * (e1 / d1 + 1.0 / (d2 * e1));
	    ans = 0.5641895835 * SIGN(exp(-xp * xp), x) * sum;
	}
	return ans;
    }

    public static double invxlogx(final double y) {
	final double ooe = 0.367879441171442322;
	double t, u, to = 0.;
	if (y >= 0. || y <= -ooe)
	    throw new IllegalArgumentException("no such inverse value");
	if (y < -0.2)
	    u = log(ooe - sqrt(2 * ooe * (y + ooe)));
	else
	    u = -10.;
	do {
	    u += (t = (log(y / u) - u) * (u / (1. + u)));
	    if (t < 1.e-8 && abs(t + to) < 0.01 * abs(t))
		break;
	    to = t;
	} while (abs(t / u) > 1.e-15);
	return exp(u);
    }
}
