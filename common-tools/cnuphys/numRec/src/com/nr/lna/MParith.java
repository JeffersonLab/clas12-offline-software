package com.nr.lna;

import static java.lang.Math.*;
import static com.nr.fft.FFT.*;

public class MParith {
    private MParith() {
    }

    public static void mpadd(char[] w, char[] u, char[] v) {
	int j, n = u.length, m = v.length, p = w.length;
	int n_min = min(n, m), p_min = min(n_min, p - 1);
	int ireg = 0;
	for (j = p_min - 1; j >= 0; j--) {
	    ireg = u[j] + v[j] + hibyte(ireg);
	    w[j + 1] = lobyte(ireg);
	}
	w[0] = hibyte(ireg);
	if (p > p_min + 1)
	    for (j = p_min + 1; j < p; j++)
		w[j] = 0;
    }

    public static int mpsub(char[] w, char[] u, char[] v) {
	int is;
	int j, n = u.length, m = v.length, p = w.length;
	int n_min = min(n, m), p_min = min(n_min, p - 1);
	int ireg = 256;
	for (j = p_min - 1; j >= 0; j--) {
	    ireg = 255 + u[j] - v[j] + hibyte(ireg);
	    w[j] = lobyte(ireg);
	}
	is = hibyte(ireg) - 1;
	if (p > p_min)
	    for (j = p_min; j < p; j++)
		w[j] = 0;

	return is;
    }

    public static void mpsad(char[] w, char[] u, final int iv) {
	int j, n = u.length, p = w.length;
	int ireg = 256 * iv;
	for (j = n - 1; j >= 0; j--) {
	    ireg = u[j] + hibyte(ireg);
	    if (j + 1 < p)
		w[j + 1] = lobyte(ireg);
	}
	w[0] = hibyte(ireg);
	for (j = n + 1; j < p; j++)
	    w[j] = 0;
    }

    public static void mpsmu(char[] w, char[] u, final int iv) {
	int j, n = u.length, p = w.length;
	int ireg = 0;
	for (j = n - 1; j >= 0; j--) {
	    ireg = u[j] * iv + hibyte(ireg);
	    if (j < p - 1)
		w[j + 1] = lobyte(ireg);
	}
	w[0] = hibyte(ireg);
	for (j = n + 1; j < p; j++)
	    w[j] = 0;
    }

    public static int mpsdv(char[] w, char[] u, final int iv) {
	int ir;
	int i, j, n = u.length, p = w.length, p_min = min(n, p);
	ir = 0;
	for (j = 0; j < p_min; j++) {
	    i = 256 * ir + u[j];
	    w[j] = (char) (i / iv);
	    ir = i % iv;
	}
	if (p > p_min)
	    for (j = p_min; j < p; j++)
		w[j] = 0;
	return ir;
    }

    public static void mpneg(char[] u) {
	int j, n = u.length;
	int ireg = 256;
	for (j = n - 1; j >= 0; j--) {
	    ireg = 255 - u[j] + hibyte(ireg);
	    u[j] = lobyte(ireg);
	}
    }

    public static void mpmov(char[] u, char[] v) {
	int j, n = u.length, m = v.length, n_min = min(n, m);
	for (j = 0; j < n_min; j++)
	    u[j] = v[j];
	if (n > n_min)
	    for (j = n_min; j < n - 1; j++)
		u[j] = 0;
    }

    public static void mplsh(char[] u) {
	int j, n = u.length;
	for (j = 0; j < n - 1; j++)
	    u[j] = u[j + 1];
	u[n - 1] = 0;
    }

    private static char lobyte(int x) {
	return (char) (x & 0xff);
    }

    private static char hibyte(int x) {
	return (char) ((x >> 8) & 0xff);
    }

    public static void mpmul(char[] w, char[] u, char[] v) {
	final double RX = 256.0;
	int j, nn = 1, n = u.length, m = v.length, p = w.length, n_max = max(m,
		n);
	double cy, t;
	while (nn < n_max)
	    nn <<= 1;
	nn <<= 1;
	double[] a = new double[nn], b = new double[nn];
	for (j = 0; j < n; j++)
	    a[j] = u[j];
	for (j = 0; j < m; j++)
	    b[j] = v[j];
	realft(a, 1);
	realft(b, 1);
	b[0] *= a[0];
	b[1] *= a[1];
	for (j = 2; j < nn; j += 2) {
	    b[j] = (t = b[j]) * a[j] - b[j + 1] * a[j + 1];
	    b[j + 1] = t * a[j + 1] + b[j + 1] * a[j];
	}
	realft(b, -1);
	cy = 0.0;
	for (j = nn - 1; j >= 0; j--) {
	    t = b[j] / (nn >> 1) + cy + 0.5;
	    cy = (int) (t / RX);
	    b[j] = t - cy * RX;
	}
	if (cy >= RX)
	    throw new IllegalArgumentException("cannot happen in mpmul");
	for (j = 0; j < p; j++)
	    w[j] = 0;
	w[0] = (char) (cy);
	for (j = 1; j < min(n + m, p); j++)
	    w[j] = (char) (b[j - 1]);
    }

    public static void mpinv(char[] u, char[] v) {
	final int MF = 4;
	final double BI = 1.0 / 256.0;
	int i, j, n = u.length, m = v.length, mm = min(MF, m);
	double fu, fv = (v[mm - 1]);
	char[] s = new char[n + m], r = new char[2 * n + m];
	for (j = mm - 2; j >= 0; j--) {
	    fv *= BI;
	    fv += v[j];
	}
	fu = 1.0 / fv;
	for (j = 0; j < n; j++) {
	    i = (int) (fu);
	    u[j] = (char) (i);
	    fu = 256.0 * (fu - i);
	}
	for (;;) {
	    mpmul(s, u, v);
	    mplsh(s);
	    mpneg(s);
	    s[0] += (char) (2);
	    s[0] &= 0xFF; // XXX one char is 2 bytes
	    mpmul(r, s, u);
	    mplsh(r);
	    mpmov(u, r);
	    for (j = 1; j < n - 1; j++)
		if (s[j] != 0)
		    break;
	    if (j == n - 1)
		return;
	}
    }

    public static void mpdiv(char[] q, char[] r, char[] u, char[] v) {
	final int MACC = 1;
	int i, is, mm, n = u.length, m = v.length, p = r.length, n_min = min(m,
		p);
	if (m > n)
	    throw new IllegalArgumentException(
		    "Divisor longer than dividend in mpdiv");
	mm = m + MACC;
	char[] s = new char[mm], rr = new char[mm], ss = new char[mm + 1], qq = new char[n
		- m + 1], t = new char[n];
	mpinv(s, v);
	mpmul(rr, s, u);
	mpsad(ss, rr, 1);
	mplsh(ss);
	mplsh(ss);
	mpmov(qq, ss);
	mpmov(q, qq);
	mpmul(t, qq, v);
	mplsh(t);
	is = mpsub(t, u, t);
	if (is != 0)
	    throw new IllegalArgumentException("MACC too small in mpdiv");
	for (i = 0; i < n_min; i++)
	    r[i] = t[i + n - m];
	if (p > m)
	    for (i = m; i < p; i++)
		r[i] = 0;
    }

    @SuppressWarnings("unused")
    public static void mpsqrt(char[] w, char[] u, char[] v) {
	final int MF = 3;
	final double BI = 1.0 / 256.0;
	int i, ir, j, n = u.length, m = v.length, mm = min(m, MF);
	char[] r = new char[2 * n], x = new char[n + m], s = new char[2 * n + m], t = new char[3
		* n + m];
	double fu, fv = (v[mm - 1]);
	for (j = mm - 2; j >= 0; j--) {
	    fv *= BI;
	    fv += v[j];
	}
	fu = 1.0 / sqrt(fv);
	for (j = 0; j < n; j++) {
	    i = (int) (fu);
	    u[j] = (char) (i);
	    fu = 256.0 * (fu - i);
	}
	for (;;) {
	    mpmul(r, u, u);
	    mplsh(r);
	    mpmul(s, r, v);
	    mplsh(s);
	    mpneg(s);
	    s[0] += (char) (3);
	    s[0] &= 0xFF; // XXX one char is 2 bytes
	    ir = mpsdv(s, s, 2);
	    for (j = 1; j < n - 1; j++) {
		if (s[j] != 0) {
		    mpmul(t, s, u);
		    mplsh(t);
		    mpmov(u, t);
		    break;
		}
	    }
	    if (j < n - 1)
		continue;
	    mpmul(x, u, v);
	    mplsh(x);
	    mpmov(w, x);
	    return;
	}
    }

    public static String mp2dfr(char[] a) {
	StringBuilder s = new StringBuilder(128);
	final int IAZ = 48;
	// char[] buffer = new char[4];
	int j, m;

	int n = a.length;
	m = (int) (2.408 * n);
	s.append(String.format("%d", a[0] & 0xFFFF));
	// s=buffer;
	s.append('.');
	mplsh(a);
	for (j = 0; j < m; j++) {
	    mpsmu(a, a, 10);
	    s.append((char) (a[0] + IAZ));
	    mplsh(a);
	}
	return s.substring(0);
    }

    @SuppressWarnings("unused")
    public static String mppi(final int np) {
	final int MACC = 2;
	int ir, j, n = np + MACC;
	char mm;
	String s;
	char[] x = new char[n], y = new char[n], sx = new char[n], sxi = new char[n], z = new char[n], t = new char[n], pi = new char[n], ss = new char[2 * n], tt = new char[2 * n];
	t[0] = 2;
	for (j = 1; j < n; j++)
	    t[j] = 0;
	mpsqrt(x, x, t);
	mpadd(pi, t, x);
	mplsh(pi);
	mpsqrt(sx, sxi, x);
	mpmov(y, sx);
	for (;;) {
	    mpadd(z, sx, sxi);
	    mplsh(z);
	    ir = mpsdv(x, z, 2);
	    mpsqrt(sx, sxi, x);
	    mpmul(tt, y, sx);
	    mplsh(tt);
	    mpadd(tt, tt, sxi);
	    mplsh(tt);
	    x[0]++;
	    y[0]++;
	    mpinv(ss, y);
	    mpmul(y, tt, ss);
	    mplsh(y);
	    mpmul(tt, x, ss);
	    mplsh(tt);
	    mpmul(ss, pi, tt);
	    mplsh(ss);
	    mpmov(pi, ss);
	    mm = (char) (tt[0] - 1);
	    for (j = 1; j < n - 1; j++)
		if (tt[j] != mm)
		    break;
	    if (j == n - 1) {
		s = mp2dfr(pi);
		int len = (int) (2.408 * np);
		// s.erase((int)(2.408*np),s.length());
		return s.substring(0, len);
	    }
	}
    }
}
