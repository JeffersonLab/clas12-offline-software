package com.nr.sf;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;
import org.netlib.util.*;

/**
 * Elliptic Integrals and Jacobian Elliptic Functions Copyright (C) Numerical
 * Recipes Software 1986-2007 Java translation Copyright (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Elliptic {
    private Elliptic() {
    }

    public static double rc(final double x, final double y) {
	final double ERRTOL = 0.0012, THIRD = 1.0 / 3.0, C1 = 0.3, C2 = 1.0 / 7.0, C3 = 0.375, C4 = 9.0 / 22.0;
	final double TINY = 5.0 * Double.MIN_NORMAL, BIG = 0.2 * Double.MAX_VALUE, COMP1 = 2.236 / sqrt(TINY), COMP2 = SQR(TINY
		* BIG) / 25.0;
	double alamb, ave, s, w, xt, yt;
	if (x < 0.0 || y == 0.0 || (x + abs(y)) < TINY || (x + abs(y)) > BIG
		|| (y < -COMP1 && x > 0.0 && x < COMP2))
	    throw new IllegalArgumentException("invalid arguments in rc");
	if (y > 0.0) {
	    xt = x;
	    yt = y;
	    w = 1.0;
	} else {
	    xt = x - y;
	    yt = -y;
	    w = sqrt(x) / sqrt(xt);
	}
	do {
	    alamb = 2.0 * sqrt(xt) * sqrt(yt) + yt;
	    xt = 0.25 * (xt + alamb);
	    yt = 0.25 * (yt + alamb);
	    ave = THIRD * (xt + yt + yt);
	    s = (yt - ave) / ave;
	} while (abs(s) > ERRTOL);
	return w * (1.0 + s * s * (C1 + s * (C2 + s * (C3 + s * C4))))
		/ sqrt(ave);
    }

    public static double rd(final double x, final double y, final double z) {
	final double ERRTOL = 0.0015, C1 = 3.0 / 14.0, C2 = 1.0 / 6.0, C3 = 9.0 / 22.0, C4 = 3.0 / 26.0, C5 = 0.25 * C3, C6 = 1.5 * C4;
	final double TINY = 2.0 * pow(Double.MAX_VALUE, -2. / 3.), BIG = 0.1
		* ERRTOL * pow(Double.MIN_NORMAL, -2. / 3.);
	double alamb, ave, delx, dely, delz, ea, eb, ec, ed, ee, fac, sqrtx, sqrty, sqrtz, sum, xt, yt, zt;
	if (min(x, y) < 0.0 || min(x + y, z) < TINY || max(max(x, y), z) > BIG)
	    throw new IllegalArgumentException("invalid arguments in rd");
	xt = x;
	yt = y;
	zt = z;
	sum = 0.0;
	fac = 1.0;
	do {
	    sqrtx = sqrt(xt);
	    sqrty = sqrt(yt);
	    sqrtz = sqrt(zt);
	    alamb = sqrtx * (sqrty + sqrtz) + sqrty * sqrtz;
	    sum += fac / (sqrtz * (zt + alamb));
	    fac = 0.25 * fac;
	    xt = 0.25 * (xt + alamb);
	    yt = 0.25 * (yt + alamb);
	    zt = 0.25 * (zt + alamb);
	    ave = 0.2 * (xt + yt + 3.0 * zt);
	    delx = (ave - xt) / ave;
	    dely = (ave - yt) / ave;
	    delz = (ave - zt) / ave;
	} while (max(max(abs(delx), abs(dely)), abs(delz)) > ERRTOL);
	ea = delx * dely;
	eb = delz * delz;
	ec = ea - eb;
	ed = ea - 6.0 * eb;
	ee = ed + ec + ec;
	return 3.0
		* sum
		+ fac
		* (1.0 + ed * (-C1 + C5 * ed - C6 * delz * ee) + delz
			* (C2 * ee + delz * (-C3 * ec + delz * C4 * ea)))
		/ (ave * sqrt(ave));
    }

    public static double rf(final double x, final double y, final double z) {
	final double ERRTOL = 0.0025, THIRD = 1.0 / 3.0, C1 = 1.0 / 24.0, C2 = 0.1, C3 = 3.0 / 44.0, C4 = 1.0 / 14.0;
	final double TINY = 5.0 * Double.MIN_NORMAL, BIG = 0.2 * Double.MAX_VALUE;
	double alamb, ave, delx, dely, delz, e2, e3, sqrtx, sqrty, sqrtz, xt, yt, zt;
	if (min(min(x, y), z) < 0.0 || min(min(x + y, x + z), y + z) < TINY
		|| max(max(x, y), z) > BIG)
	    throw new IllegalArgumentException("invalid arguments in rf");
	xt = x;
	yt = y;
	zt = z;
	do {
	    sqrtx = sqrt(xt);
	    sqrty = sqrt(yt);
	    sqrtz = sqrt(zt);
	    alamb = sqrtx * (sqrty + sqrtz) + sqrty * sqrtz;
	    xt = 0.25 * (xt + alamb);
	    yt = 0.25 * (yt + alamb);
	    zt = 0.25 * (zt + alamb);
	    ave = THIRD * (xt + yt + zt);
	    delx = (ave - xt) / ave;
	    dely = (ave - yt) / ave;
	    delz = (ave - zt) / ave;
	} while (max(max(abs(delx), abs(dely)), abs(delz)) > ERRTOL);
	e2 = delx * dely - delz * delz;
	e3 = delx * dely * delz;
	return (1.0 + (C1 * e2 - C2 - C3 * e3) * e2 + C4 * e3) / sqrt(ave);
    }

    public static double rj(final double x, final double y, final double z,
	    final double p) {
	final double ERRTOL = 0.0015, C1 = 3.0 / 14.0, C2 = 1.0 / 3.0, C3 = 3.0 / 22.0, C4 = 3.0 / 26.0, C5 = 0.75 * C3, C6 = 1.5 * C4, C7 = 0.5 * C2, C8 = C3
		+ C3;
	final double TINY = pow(5.0 * Double.MIN_NORMAL, 1. / 3.), BIG = 0.3 * pow(
		0.2 * Double.MAX_VALUE, 1. / 3.);
	double a = 0, alamb, alpha, ans, ave, b = 0, beta, delp, delx, dely, delz, ea, eb, ec, ed, ee, fac, pt, rcx = 0, rho, sqrtx, sqrty, sqrtz, sum, tau, xt, yt, zt;
	if (min(min(x, y), z) < 0.0
		|| min(min(x + y, x + z), min(y + z, abs(p))) < TINY
		|| max(max(x, y), max(z, abs(p))) > BIG)
	    throw new IllegalArgumentException("invalid arguments in rj");
	sum = 0.0;
	fac = 1.0;
	if (p > 0.0) {
	    xt = x;
	    yt = y;
	    zt = z;
	    pt = p;
	} else {
	    xt = min(min(x, y), z);
	    zt = max(max(x, y), z);
	    yt = x + y + z - xt - zt;
	    a = 1.0 / (yt - p);
	    b = a * (zt - yt) * (yt - xt);
	    pt = yt + b;
	    rho = xt * zt / yt;
	    tau = p * pt / yt;
	    rcx = rc(rho, tau);
	}
	do {
	    sqrtx = sqrt(xt);
	    sqrty = sqrt(yt);
	    sqrtz = sqrt(zt);
	    alamb = sqrtx * (sqrty + sqrtz) + sqrty * sqrtz;
	    alpha = SQR(pt * (sqrtx + sqrty + sqrtz) + sqrtx * sqrty * sqrtz);
	    beta = pt * SQR(pt + alamb);
	    sum += fac * rc(alpha, beta);
	    fac = 0.25 * fac;
	    xt = 0.25 * (xt + alamb);
	    yt = 0.25 * (yt + alamb);
	    zt = 0.25 * (zt + alamb);
	    pt = 0.25 * (pt + alamb);
	    ave = 0.2 * (xt + yt + zt + pt + pt);
	    delx = (ave - xt) / ave;
	    dely = (ave - yt) / ave;
	    delz = (ave - zt) / ave;
	    delp = (ave - pt) / ave;
	} while (max(max(abs(delx), abs(dely)), max(abs(delz), abs(delp))) > ERRTOL);
	ea = delx * (dely + delz) + dely * delz;
	eb = delx * dely * delz;
	ec = delp * delp;
	ed = ea - 3.0 * ec;
	ee = eb + 2.0 * delp * (ea - ec);
	ans = 3.0
		* sum
		+ fac
		* (1.0 + ed * (-C1 + C5 * ed - C6 * ee) + eb
			* (C7 + delp * (-C8 + delp * C4)) + delp * ea
			* (C2 - delp * C3) - C2 * delp * ec)
		/ (ave * sqrt(ave));
	if (p <= 0.0)
	    ans = a * (b * ans + 3.0 * (rcx - rf(xt, yt, zt)));
	return ans;
    }

    public static double ellf(final double phi, final double ak) {
	double s = sin(phi);
	return s * rf(SQR(cos(phi)), (1.0 - s * ak) * (1.0 + s * ak), 1.0);
    }

    public static double elle(final double phi, final double ak) {
	double cc, q, s;
	s = sin(phi);
	cc = SQR(cos(phi));
	q = (1.0 - s * ak) * (1.0 + s * ak);
	return s * (rf(cc, q, 1.0) - (SQR(s * ak)) * rd(cc, q, 1.0) / 3.0);
    }

    public static double ellpi(final double phi, final double en,
	    final double ak) {
	double cc, enss, q, s;
	s = sin(phi);
	enss = en * s * s;
	cc = SQR(cos(phi));
	q = (1.0 - s * ak) * (1.0 + s * ak);
	return s * (rf(cc, q, 1.0) - enss * rj(cc, q, 1.0, 1.0 + enss) / 3.0);
    }

    public static void sncndn(final double uu, final double emmc,
	    final doubleW sn, final doubleW cn, final doubleW dn) {
	final double CA = 1.0e-8;
	boolean bo;
	int i, ii, l = 0;
	double a, b, c = 0, d = 0, emc, u;
	double[] em = new double[13];
	double[] en = new double[13];
	emc = emmc;
	u = uu;
	if (emc != 0.0) {
	    bo = (emc < 0.0);
	    if (bo) {
		d = 1.0 - emc;
		emc /= -1.0 / d;
		u *= (d = sqrt(d));
	    }
	    a = 1.0;
	    dn.val = 1.0;
	    for (i = 0; i < 13; i++) {
		l = i;
		em[i] = a;
		en[i] = (emc = sqrt(emc));
		c = 0.5 * (a + emc);
		if (abs(a - emc) <= CA * a)
		    break;
		emc *= a;
		a = c;
	    }
	    u *= c;
	    sn.val = sin(u);
	    cn.val = cos(u);
	    if (sn.val != 0.0) {
		a = cn.val / sn.val;
		c *= a;
		for (ii = l; ii >= 0; ii--) {
		    b = em[ii];
		    a *= c;
		    c *= dn.val;
		    dn.val = (en[ii] + a) / (b + a);
		    a = c / b;
		}
		a = 1.0 / sqrt(c * c + 1.0);
		sn.val = (sn.val >= 0.0 ? a : -a);
		cn.val = c * sn.val;
	    }
	    if (bo) {
		a = dn.val;
		dn.val = cn.val;
		cn.val = a;
		sn.val /= d;
	    }
	} else {
	    cn.val = 1.0 / cosh(u);
	    dn.val = cn.val;
	    sn.val = tanh(u);
	}
    }
}
