package com.nr.stat;

import java.util.Arrays;

import org.netlib.util.doubleW;

import com.nr.UniVarRealValueFun;
import com.nr.sf.Beta;
import com.nr.sf.Gamma;
import com.nr.sf.KSdist;
import static com.nr.sf.Erf.*;
import static com.nr.sort.Sorter.*;
import static com.nr.stat.Moment.*;
import static java.lang.Math.*;
import static com.nr.NRUtil.*;

public class Stattests {
    private Stattests() {
    }

    public static void ttest(final double[] data1, final double[] data2,
	    final doubleW t, final doubleW prob) {
	double svar, df;
	doubleW ave1 = new doubleW(0);
	doubleW ave2 = new doubleW(0);
	doubleW var1 = new doubleW(0);
	doubleW var2 = new doubleW(0);

	int n1 = data1.length, n2 = data2.length;
	avevar(data1, ave1, var1);
	avevar(data2, ave2, var2);
	df = n1 + n2 - 2;
	svar = ((n1 - 1) * var1.val + (n2 - 1) * var2.val) / df;
	t.val = (ave1.val - ave2.val) / sqrt(svar * (1.0 / n1 + 1.0 / n2));
	prob.val = Beta.betai(0.5 * df, 0.5, df / (df + t.val * t.val));
    }

    public static void tutest(final double[] data1, final double[] data2,
	    final doubleW t, final doubleW prob) {
	doubleW ave1 = new doubleW(0);
	doubleW ave2 = new doubleW(0);
	doubleW var1 = new doubleW(0);
	doubleW var2 = new doubleW(0);
	double df;
	int n1 = data1.length, n2 = data2.length;
	avevar(data1, ave1, var1);
	avevar(data2, ave2, var2);
	t.val = (ave1.val - ave2.val) / sqrt(var1.val / n1 + var2.val / n2);
	df = SQR(var1.val / n1 + var2.val / n2)
		/ (SQR(var1.val / n1) / (n1 - 1) + SQR(var2.val / n2)
			/ (n2 - 1));
	prob.val = Beta.betai(0.5 * df, 0.5, df / (df + SQR(t.val)));
    }

    public static void tptest(final double[] data1, final double[] data2,
	    final doubleW t, final doubleW prob) {
	doubleW ave1 = new doubleW(0);
	doubleW ave2 = new doubleW(0);
	doubleW var1 = new doubleW(0);
	doubleW var2 = new doubleW(0);

	int j, n = data1.length;
	double sd, df, cov = 0.0;
	avevar(data1, ave1, var1);
	avevar(data2, ave2, var2);
	for (j = 0; j < n; j++)
	    cov += (data1[j] - ave1.val) * (data2[j] - ave2.val);
	cov /= (df = n - 1);
	sd = sqrt((var1.val + var2.val - 2.0 * cov) / n);
	t.val = (ave1.val - ave2.val) / sd;
	prob.val = Beta.betai(0.5 * df, 0.5, df / (df + t.val * t.val));
    }

    public static void ftest(final double[] data1, final double[] data2,
	    final doubleW f, final doubleW prob) {
	doubleW ave1 = new doubleW(0);
	doubleW ave2 = new doubleW(0);
	doubleW var1 = new doubleW(0);
	doubleW var2 = new doubleW(0);

	double df1, df2;
	int n1 = data1.length, n2 = data2.length;
	avevar(data1, ave1, var1);
	avevar(data2, ave2, var2);
	if (var1.val > var2.val) {
	    f.val = var1.val / var2.val;
	    df1 = n1 - 1;
	    df2 = n2 - 1;
	} else {
	    f.val = var2.val / var1.val;
	    df1 = n2 - 1;
	    df2 = n1 - 1;
	}
	prob.val = 2.0 * Beta.betai(0.5 * df2, 0.5 * df1, df2
		/ (df2 + df1 * f.val));
	if (prob.val > 1.0)
	    prob.val = 2. - prob.val;
    }

    public static void chsone(final double[] bins, final double[] ebins,
	    final doubleW df, final doubleW chsq, final doubleW prob) {
	chsone(bins, ebins, df, chsq, prob, 1);
    }

    public static void chsone(final double[] bins, final double[] ebins,
	    final doubleW df, final doubleW chsq, final doubleW prob,
	    final int knstrn) {
	Gamma gam = new Gamma();
	int j, nbins = bins.length;
	double temp;
	df.val = nbins - knstrn;
	chsq.val = 0.0;
	for (j = 0; j < nbins; j++) {
	    if (ebins[j] < 0.0 || (ebins[j] == 0. && bins[j] > 0.))
		throw new IllegalArgumentException(
			"Bad expected number in chsone");
	    if (ebins[j] == 0.0 && bins[j] == 0.0) {
		--df.val;
	    } else {
		temp = bins[j] - ebins[j];
		chsq.val += temp * temp / ebins[j];
	    }
	}
	prob.val = gam.gammq(0.5 * df.val, 0.5 * chsq.val);
    }

    public static void chstwo(final double[] bins1, final double[] bins2,
	    final doubleW df, final doubleW chsq, final doubleW prob) {
	chstwo(bins1, bins2, df, chsq, prob, 1);
    }

    public static void chstwo(final double[] bins1, final double[] bins2,
	    final doubleW df, final doubleW chsq, final doubleW prob,
	    final int knstrn) {
	Gamma gam = new Gamma();
	int j, nbins = bins1.length;
	double temp;
	df.val = nbins - knstrn;
	chsq.val = 0.0;
	for (j = 0; j < nbins; j++)
	    if (bins1[j] == 0.0 && bins2[j] == 0.0)
		--df.val;
	    else {
		temp = bins1[j] - bins2[j];
		chsq.val += temp * temp / (bins1[j] + bins2[j]);
	    }
	prob.val = gam.gammq(0.5 * df.val, 0.5 * chsq.val);
    }

    public static void cntab(final int[][] nn, final doubleW chisq,
	    final doubleW df, final doubleW prob, final doubleW cramrv,
	    final doubleW ccc) {
	final double TINY = 1.0e-30;
	Gamma gam = new Gamma();
	int i, j, nnj, nni, minij, ni = nn.length, nj = nn[0].length;
	double sum = 0.0, expctd, temp;
	double[] sumi = new double[ni], sumj = new double[nj];
	nni = ni;
	nnj = nj;
	for (i = 0; i < ni; i++) {
	    sumi[i] = 0.0;
	    for (j = 0; j < nj; j++) {
		sumi[i] += nn[i][j];
		sum += nn[i][j];
	    }
	    if (sumi[i] == 0.0)
		--nni;
	}
	for (j = 0; j < nj; j++) {
	    sumj[j] = 0.0;
	    for (i = 0; i < ni; i++)
		sumj[j] += nn[i][j];
	    if (sumj[j] == 0.0)
		--nnj;
	}
	df.val = nni * nnj - nni - nnj + 1;
	chisq.val = 0.0;
	for (i = 0; i < ni; i++) {
	    for (j = 0; j < nj; j++) {
		expctd = sumj[j] * sumi[i] / sum;
		temp = nn[i][j] - expctd;
		chisq.val += temp * temp / (expctd + TINY);
	    }
	}
	prob.val = gam.gammq(0.5 * df.val, 0.5 * chisq.val);
	minij = nni < nnj ? nni - 1 : nnj - 1;
	cramrv.val = sqrt(chisq.val / (sum * minij));
	ccc.val = sqrt(chisq.val / (chisq.val + sum));
    }

    public static void pearsn(final double[] x, final double[] y,
	    final doubleW r, final doubleW prob, final doubleW z) {
	final double TINY = 1.0e-20;
	int j, n = x.length;
	double yt, xt, t, df;
	double syy = 0.0, sxy = 0.0, sxx = 0.0, ay = 0.0, ax = 0.0;
	for (j = 0; j < n; j++) {
	    ax += x[j];
	    ay += y[j];
	}
	ax /= n;
	ay /= n;
	for (j = 0; j < n; j++) {
	    xt = x[j] - ax;
	    yt = y[j] - ay;
	    sxx += xt * xt;
	    syy += yt * yt;
	    sxy += xt * yt;
	}
	r.val = sxy / (sqrt(sxx * syy) + TINY);
	z.val = 0.5 * log((1.0 + r.val + TINY) / (1.0 - r.val + TINY));
	df = n - 2;
	t = r.val * sqrt(df / ((1.0 - r.val + TINY) * (1.0 + r.val + TINY)));
	prob.val = Beta.betai(0.5 * df, 0.5, df / (df + t * t));
	// prob=erfcc(abs(z*sqrt(n-1.0))/1.4142136);
    }

    public static void crank(final double[] w, final doubleW s) {
	int j = 1, ji, jt, n = w.length;
	double t, rank;
	s.val = 0.0;
	while (j < n) {
	    if (w[j] != w[j - 1]) {
		w[j - 1] = j;
		++j;
	    } else {
		for (jt = j + 1; jt <= n && w[jt - 1] == w[j - 1]; jt++)
		    ;
		rank = 0.5 * (j + jt - 1);
		for (ji = j; ji <= (jt - 1); ji++)
		    w[ji - 1] = rank;
		t = jt - j;
		s.val += (t * t * t - t);
		j = jt;
	    }
	}
	if (j == n)
	    w[n - 1] = n;
    }

    public static void spear(final double[] data1, final double[] data2,
	    final doubleW d, final doubleW zd, final doubleW probd,
	    final doubleW rs, final doubleW probrs) {
	int j, n = data1.length;
	double vard, t, fac, en3n, en, df, aved;
	doubleW sf = new doubleW(0);
	doubleW sg = new doubleW(0);

	double[] wksp1 = new double[n], wksp2 = new double[n];
	for (j = 0; j < n; j++) {
	    wksp1[j] = data1[j];
	    wksp2[j] = data2[j];
	}
	sort2(wksp1, wksp2);
	crank(wksp1, sf);
	sort2(wksp2, wksp1);
	crank(wksp2, sg);
	d.val = 0.0;
	for (j = 0; j < n; j++)
	    d.val += SQR(wksp1[j] - wksp2[j]);
	en = n;
	en3n = en * en * en - en;
	aved = en3n / 6.0 - (sf.val + sg.val) / 12.0;
	fac = (1.0 - sf.val / en3n) * (1.0 - sg.val / en3n);
	vard = ((en - 1.0) * en * en * SQR(en + 1.0) / 36.0) * fac;
	zd.val = (d.val - aved) / sqrt(vard);
	probd.val = erfcc(abs(zd.val) / 1.4142136);
	rs.val = (1.0 - (6.0 / en3n) * (d.val + (sf.val + sg.val) / 12.0))
		/ sqrt(fac);
	fac = (rs.val + 1.0) * (1.0 - rs.val);
	if (fac > 0.0) {
	    t = rs.val * sqrt((en - 2.0) / fac);
	    df = en - 2.0;
	    probrs.val = Beta.betai(0.5 * df, 0.5, df / (df + t * t));
	} else
	    probrs.val = 0.0;
    }

    public static void kendl1(final double[] data1, final double[] data2,
	    final doubleW tau, final doubleW z, final doubleW prob) {
	int is = 0, j, k, n2 = 0, n1 = 0, n = data1.length;
	double svar, aa, a2, a1;
	for (j = 0; j < n - 1; j++) {
	    for (k = j + 1; k < n; k++) {
		a1 = data1[j] - data1[k];
		a2 = data2[j] - data2[k];
		aa = a1 * a2;
		if (aa != 0.0) {
		    ++n1;
		    ++n2;
		    if (aa > 0.0)
			++is;
		    else
			--is;
		} else {
		    if (a1 != 0.0)
			++n1;
		    if (a2 != 0.0)
			++n2;
		}
	    }
	}
	tau.val = is / (sqrt(n1) * sqrt(n2));
	svar = (4.0 * n + 10.0) / (9.0 * n * (n - 1.0));
	z.val = tau.val / sqrt(svar);
	prob.val = erfcc(abs(z.val) / 1.4142136);
    }

    public static void kendl2(final double[][] tab, final doubleW tau,
	    final doubleW z, final doubleW prob) {
	int k, l, nn, mm, m2, m1, lj, li, kj, ki, i = tab.length, j = tab[0].length;
	double svar, s = 0.0, points, pairs, en2 = 0.0, en1 = 0.0;
	nn = i * j;
	points = tab[i - 1][j - 1];
	for (k = 0; k <= nn - 2; k++) {
	    ki = (k / j);
	    kj = k - j * ki;
	    points += tab[ki][kj];
	    for (l = k + 1; l <= nn - 1; l++) {
		li = l / j;
		lj = l - j * li;
		mm = (m1 = li - ki) * (m2 = lj - kj);
		pairs = tab[ki][kj] * tab[li][lj];
		if (mm != 0) {
		    en1 += pairs;
		    en2 += pairs;
		    s += (mm > 0 ? pairs : -pairs);
		} else {
		    if (m1 != 0)
			en1 += pairs;
		    if (m2 != 0)
			en2 += pairs;
		}
	    }
	}
	tau.val = s / sqrt(en1 * en2);
	svar = (4.0 * points + 10.0) / (9.0 * points * (points - 1.0));
	z.val = tau.val / sqrt(svar);
	prob.val = erfcc(abs(z.val) / 1.4142136);
    }

    /**
     * Kolmogorov-Smirnov tests in one dimension
     */
    public static void ksone(final double[] data,
	    final UniVarRealValueFun func, final doubleW d, final doubleW prob) {
	int j, n = data.length;
	double dt, en, ff, fn, fo = 0.0;
	KSdist ks = new KSdist();
	Arrays.sort(data);
	en = n;
	d.val = 0.0;
	for (j = 0; j < n; j++) {
	    fn = (j + 1) / en;
	    ff = func.funk(data[j]);
	    dt = max(abs(fo - ff), abs(fn - ff));
	    if (dt > d.val)
		d.val = dt;
	    fo = fn;
	}
	en = sqrt(en);
	prob.val = ks.qks((en + 0.12 + 0.11 / en) * d.val);
    }

    /**
     * Kolmogorov-Smirnov tests in one dimension
     */
    public static void kstwo(final double[] data1, final double[] data2,
	    final doubleW d, final doubleW prob) {
	int j1 = 0, j2 = 0, n1 = data1.length, n2 = data2.length;
	double d1, d2, dt, en1, en2, en, fn1 = 0.0, fn2 = 0.0;
	KSdist ks = new KSdist();
	Arrays.sort(data1);
	Arrays.sort(data2);
	en1 = n1;
	en2 = n2;
	d.val = 0.0;
	while (j1 < n1 && j2 < n2) {
	    if ((d1 = data1[j1]) <= (d2 = data2[j2]))
		do
		    fn1 = ++j1 / en1;
		while (j1 < n1 && d1 == data1[j1]);
	    if (d2 <= d1)
		do
		    fn2 = ++j2 / en2;
		while (j2 < n2 && d2 == data2[j2]);
	    if ((dt = abs(fn2 - fn1)) > d.val)
		d.val = dt;
	}
	en = sqrt(en1 * en2 / (en1 + en2));
	prob.val = ks.qks((en + 0.12 + 0.11 / en) * d.val);
    }

    public static void quadct(final double x, final double y,
	    final double[] xx, final double[] yy, final doubleW fa,
	    final doubleW fb, final doubleW fc, final doubleW fd) {
	int k, na, nb, nc, nd, nn = xx.length;
	double ff;
	na = nb = nc = nd = 0;
	for (k = 0; k < nn; k++) {
	    if (yy[k] == y && xx[k] == x)
		continue;
	    if (yy[k] > y)
		if (xx[k] > x)
		    ++na;
		else
		    ++nb;
	    else if (xx[k] > x)
		++nd;
	    else
		++nc;
	}
	ff = 1.0 / nn;
	fa.val = ff * na;
	fb.val = ff * nb;
	fc.val = ff * nc;
	fd.val = ff * nd;
    }

    public static void ks2d1s(final double[] x1, final double[] y1,
	    final QuadvlInf quadvl, final doubleW d1, final doubleW prob) {
	int j, n1 = x1.length;
	double rr, sqen;
	doubleW dum = new doubleW(0);
	doubleW dumm = new doubleW(0);
	doubleW r1 = new doubleW(0);

	doubleW fa = new doubleW(0);
	doubleW fb = new doubleW(0);
	doubleW fc = new doubleW(0);
	doubleW fd = new doubleW(0);

	doubleW ga = new doubleW(0);
	doubleW gb = new doubleW(0);
	doubleW gc = new doubleW(0);
	doubleW gd = new doubleW(0);

	KSdist ks = new KSdist();
	d1.val = 0.0;
	for (j = 0; j < n1; j++) {
	    quadct(x1[j], y1[j], x1, y1, fa, fb, fc, fd);
	    quadvl.quadvl(x1[j], y1[j], ga, gb, gc, gd);
	    if (fa.val > ga.val)
		fa.val += 1.0 / n1;
	    if (fb.val > gb.val)
		fb.val += 1.0 / n1;
	    if (fc.val > gc.val)
		fc.val += 1.0 / n1;
	    if (fd.val > gd.val)
		fd.val += 1.0 / n1;
	    d1.val = max(d1.val, abs(fa.val - ga.val));
	    d1.val = max(d1.val, abs(fb.val - gb.val));
	    d1.val = max(d1.val, abs(fc.val - gc.val));
	    d1.val = max(d1.val, abs(fd.val - gd.val));
	}
	pearsn(x1, y1, r1, dum, dumm);
	sqen = sqrt(n1);
	rr = sqrt(1.0 - r1.val * r1.val);
	prob.val = ks.qks(d1.val * sqen / (1.0 + rr * (0.25 - 0.75 / sqen)));
    }

    public static void ks2d2s(final double[] x1, final double[] y1,
	    final double[] x2, final double[] y2, final doubleW d,
	    final doubleW prob) {
	int j, n1 = x1.length, n2 = x2.length;
	doubleW dum = new doubleW(0);
	doubleW dumm = new doubleW(0);
	doubleW r1 = new doubleW(0);
	doubleW r2 = new doubleW(0);

	doubleW fa = new doubleW(0);
	doubleW fb = new doubleW(0);
	doubleW fc = new doubleW(0);
	doubleW fd = new doubleW(0);

	doubleW ga = new doubleW(0);
	doubleW gb = new doubleW(0);
	doubleW gc = new doubleW(0);
	doubleW gd = new doubleW(0);

	double d1, d2, rr, sqen;
	KSdist ks = new KSdist();
	d1 = 0.0;
	for (j = 0; j < n1; j++) {
	    quadct(x1[j], y1[j], x1, y1, fa, fb, fc, fd);
	    quadct(x1[j], y1[j], x2, y2, ga, gb, gc, gd);
	    if (fa.val > ga.val)
		fa.val += 1.0 / n1;
	    if (fb.val > gb.val)
		fb.val += 1.0 / n1;
	    if (fc.val > gc.val)
		fc.val += 1.0 / n1;
	    if (fd.val > gd.val)
		fd.val += 1.0 / n1;
	    d1 = max(d1, abs(fa.val - ga.val));
	    d1 = max(d1, abs(fb.val - gb.val));
	    d1 = max(d1, abs(fc.val - gc.val));
	    d1 = max(d1, abs(fd.val - gd.val));
	}
	d2 = 0.0;
	for (j = 0; j < n2; j++) {
	    quadct(x2[j], y2[j], x1, y1, fa, fb, fc, fd);
	    quadct(x2[j], y2[j], x2, y2, ga, gb, gc, gd);
	    if (ga.val > fa.val)
		ga.val += 1.0 / n1;
	    if (gb.val > fb.val)
		gb.val += 1.0 / n1;
	    if (gc.val > fc.val)
		gc.val += 1.0 / n1;
	    if (gd.val > fd.val)
		gd.val += 1.0 / n1;
	    d2 = max(d2, abs(fa.val - ga.val));
	    d2 = max(d2, abs(fb.val - gb.val));
	    d2 = max(d2, abs(fc.val - gc.val));
	    d2 = max(d2, abs(fd.val - gd.val));
	}
	d.val = 0.5 * (d1 + d2);
	sqen = sqrt(n1 * n2 / (n1 + n2));
	pearsn(x1, y1, r1, dum, dumm);
	pearsn(x2, y2, r2, dum, dumm);
	rr = sqrt(1.0 - 0.5 * (r1.val * r1.val + r2.val * r2.val));
	prob.val = ks.qks(d.val * sqen / (1.0 + rr * (0.25 - 0.75 / sqen)));
    }
}
