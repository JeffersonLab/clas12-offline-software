package com.nr.sp;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;

import org.netlib.util.doubleW;

import com.nr.Complex;

import com.nr.fft.FFT;
import com.nr.root.Roots;
import com.nr.stat.Moment;

/**
 * Fourier integrals using FFT Copyright (C) Numerical Recipes Software
 * 1986-2007 Java translation Copyright (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Fourier {
    private Fourier() {
    }

    /**
     * For an integral approximated by a discrete Fourier transform, this
     * routine computes the correction factor that multiplies the DFT and the
     * endpoint correction to be added. Input is the angular frequency w,
     * stepsize delta, lower and upper limits of the integral a and b, while the
     * array endpts contains the first 4 and last 4 function values. The
     * correction factor W .Â/ is returned as corfac, while the real and
     * imaginary parts of the endpoint correction are returned as corre and
     * corim.
     * 
     * @param w
     * @param delta
     * @param a
     * @param b
     * @param endpts
     * @param corre
     * @param corim
     * @param corfac
     */
    public static void dftcor(final double w, final double delta,
	    final double a, final double b, final double[] endpts,
	    final doubleW corre, final doubleW corim, final doubleW corfac) {
	double a0i, a0r, a1i, a1r, a2i, a2r, a3i, a3r, arg, c, cl, cr, s, sl, sr, t, t2, t4, t6, cth, ctth, spth2, sth, sth4i, stth, th, th2, th4, tmth2, tth4i;
	th = w * delta;
	if (a >= b || th < 0.0e0 || th > 3.1416e0)
	    throw new IllegalArgumentException("bad arguments to dftcor");
	if (abs(th) < 5.0e-2) {
	    t = th;
	    t2 = t * t;
	    t4 = t2 * t2;
	    t6 = t4 * t2;
	    corfac.val = 1.0 - (11.0 / 720.0) * t4 + (23.0 / 15120.0) * t6;
	    a0r = (-2.0 / 3.0) + t2 / 45.0 + (103.0 / 15120.0) * t4
		    - (169.0 / 226800.0) * t6;
	    a1r = (7.0 / 24.0) - (7.0 / 180.0) * t2 + (5.0 / 3456.0) * t4
		    - (7.0 / 259200.0) * t6;
	    a2r = (-1.0 / 6.0) + t2 / 45.0 - (5.0 / 6048.0) * t4 + t6 / 64800.0;
	    a3r = (1.0 / 24.0) - t2 / 180.0 + (5.0 / 24192.0) * t4 - t6
		    / 259200.0;
	    a0i = t
		    * (2.0 / 45.0 + (2.0 / 105.0) * t2 - (8.0 / 2835.0) * t4 + (86.0 / 467775.0)
			    * t6);
	    a1i = t
		    * (7.0 / 72.0 - t2 / 168.0 + (11.0 / 72576.0) * t4 - (13.0 / 5987520.0)
			    * t6);
	    a2i = t
		    * (-7.0 / 90.0 + t2 / 210.0 - (11.0 / 90720.0) * t4 + (13.0 / 7484400.0)
			    * t6);
	    a3i = t
		    * (7.0 / 360.0 - t2 / 840.0 + (11.0 / 362880.0) * t4 - (13.0 / 29937600.0)
			    * t6);
	} else {
	    cth = cos(th);
	    sth = sin(th);
	    ctth = cth * cth - sth * sth;
	    stth = 2.0e0 * sth * cth;
	    th2 = th * th;
	    th4 = th2 * th2;
	    tmth2 = 3.0e0 - th2;
	    spth2 = 6.0e0 + th2;
	    sth4i = 1.0 / (6.0e0 * th4);
	    tth4i = 2.0e0 * sth4i;
	    corfac.val = tth4i * spth2 * (3.0e0 - 4.0e0 * cth + ctth);
	    a0r = sth4i
		    * (-42.0e0 + 5.0e0 * th2 + spth2 * (8.0e0 * cth - ctth));
	    a0i = sth4i * (th * (-12.0e0 + 6.0e0 * th2) + spth2 * stth);
	    a1r = sth4i * (14.0e0 * tmth2 - 7.0e0 * spth2 * cth);
	    a1i = sth4i * (30.0e0 * th - 5.0e0 * spth2 * sth);
	    a2r = tth4i * (-4.0e0 * tmth2 + 2.0e0 * spth2 * cth);
	    a2i = tth4i * (-12.0e0 * th + 2.0e0 * spth2 * sth);
	    a3r = sth4i * (2.0e0 * tmth2 - spth2 * cth);
	    a3i = sth4i * (6.0e0 * th - spth2 * sth);
	}
	cl = a0r * endpts[0] + a1r * endpts[1] + a2r * endpts[2] + a3r
		* endpts[3];
	sl = a0i * endpts[0] + a1i * endpts[1] + a2i * endpts[2] + a3i
		* endpts[3];
	cr = a0r * endpts[7] + a1r * endpts[6] + a2r * endpts[5] + a3r
		* endpts[4];
	sr = -a0i * endpts[7] - a1i * endpts[6] - a2i * endpts[5] - a3i
		* endpts[4];
	arg = w * (b - a);
	c = cos(arg);
	s = sin(arg);
	corre.val = cl + c * cr - s * sr;
	corim.val = sl + s * cr + c * sr;
    }

    /**
     * Correlation and Autocorrelation Using the FFT
     * 
     * Computes the correlation of two real data sets data1[0..n-1] and
     * data2[0..n-1] (including any user-supplied zero padding). n must be an
     * integer power of 2. The answer is returned in ans[0..n-1] stored in
     * wraparound order, i.e., correlations at increasingly negative lags are in
     * ans[n-1] on down to ans[n/2], while correlations at increasingly positive
     * lags are in ans[0] (zero lag) on up to ans[n/2-1]. Sign convention of
     * this routine: if data1 lags data2, i.e., is shifted to the right of it,
     * then ans will show a peak at positive lags.
     * 
     * @param data1
     * @param data2
     * @param ans
     */
    public static void correl(final double[] data1, final double[] data2,
	    final double[] ans) {
	int no2, i, n = data1.length;
	double tmp;
	double[] temp = new double[n];
	for (i = 0; i < n; i++) {
	    ans[i] = data1[i];
	    temp[i] = data2[i];
	}
	FFT.realft(ans, 1);
	FFT.realft(temp, 1);
	no2 = n >> 1;
	for (i = 2; i < n; i += 2) {
	    tmp = ans[i];
	    ans[i] = (ans[i] * temp[i] + ans[i + 1] * temp[i + 1]) / no2;
	    ans[i + 1] = (ans[i + 1] * temp[i] - tmp * temp[i + 1]) / no2;
	}
	ans[0] = ans[0] * temp[0] / no2;
	ans[1] = ans[1] * temp[1] / no2;
	FFT.realft(ans, -1);
    }

    /**
     * Convolution and Deconvolution Using the FFT
     * 
     * Convolves or deconvolves a real data set data[0..n-1] (including any
     * user-supplied zero padding) with a response function respns[0..m-1],
     * where m is an odd integer <= n. The response function must be stored in
     * wraparound order: The first half of the array respns contains the impulse
     * response function at positive times, while the second half of the array
     * contains the impulse response function at negative times, counting down
     * from the highest element respns[m-1]. On input isign is C1 for
     * convolution, 1 for deconvolution. The answer is returned in ans[0..n-1].
     * n must be an integer power of 2.
     * 
     * @param data
     * @param respns
     * @param isign
     * @param ans
     */
    public static void convlv(final double[] data, final double[] respns,
	    final int isign, final double[] ans) {
	int i, no2, n = data.length, m = respns.length;
	double mag2, tmp;
	double[] temp = new double[n];
	temp[0] = respns[0];
	for (i = 1; i < (m + 1) / 2; i++) {
	    temp[i] = respns[i];
	    temp[n - i] = respns[m - i];
	}
	for (i = (m + 1) / 2; i < n - (m - 1) / 2; i++)
	    temp[i] = 0.0;
	for (i = 0; i < n; i++)
	    ans[i] = data[i];
	FFT.realft(ans, 1);
	FFT.realft(temp, 1);
	no2 = n >> 1;
	if (isign == 1) {
	    for (i = 2; i < n; i += 2) {
		tmp = ans[i];
		ans[i] = (ans[i] * temp[i] - ans[i + 1] * temp[i + 1]) / no2;
		ans[i + 1] = (ans[i + 1] * temp[i] + tmp * temp[i + 1]) / no2;
	    }
	    ans[0] = ans[0] * temp[0] / no2;
	    ans[1] = ans[1] * temp[1] / no2;
	} else if (isign == -1) {
	    for (i = 2; i < n; i += 2) {
		if ((mag2 = SQR(temp[i]) + SQR(temp[i + 1])) == 0.0)
		    throw new IllegalArgumentException(
			    "Deconvolving at response zero in convlv");
		tmp = ans[i];
		ans[i] = (ans[i] * temp[i] + ans[i + 1] * temp[i + 1]) / mag2
			/ no2;
		ans[i + 1] = (ans[i + 1] * temp[i] - tmp * temp[i + 1]) / mag2
			/ no2;
	    }
	    if (temp[0] == 0.0 || temp[1] == 0.0)
		throw new IllegalArgumentException(
			"Deconvolving at response zero in convlv");
	    ans[0] = ans[0] / temp[0] / no2;
	    ans[1] = ans[1] / temp[1] / no2;
	} else
	    throw new IllegalArgumentException("No meaning for isign in convlv");
	FFT.realft(ans, -1);
    }

    /**
     * 
     * @param y
     * @param yy
     * @param x
     * @param m
     */
    public static void spread(final double y, final double[] yy,
	    final double x, final int m) {
	int[] nfac = { 0, 1, 1, 2, 6, 24, 120, 720, 5040, 40320, 362880 };
	int ihi, ilo, ix, j, nden, n = yy.length;
	double fac;
	if (m > 10)
	    throw new IllegalArgumentException(
		    "factorial table too small in spread");
	ix = (int) (x);
	if (x == (ix))
	    yy[ix - 1] += y;
	else {
	    ilo = min(max((int) (x - 0.5 * m), 0), (n - m));
	    ihi = ilo + m;
	    nden = nfac[m];
	    fac = x - ilo - 1;
	    for (j = ilo + 1; j < ihi; j++)
		fac *= (x - j - 1);
	    yy[ihi - 1] += y * fac / (nden * (x - ihi));
	    for (j = ihi - 1; j > ilo; j--) {
		nden = (nden / (j - ilo)) * (j - ihi);
		yy[j - 1] += y * fac / (nden * (x - j));
	    }
	}
    }

    /**
     * Fast evaluation of Lomb periodogram Given n data points with abscissas
     * x[0..n-1] (which need not be equally spaced) and ordinates y[0..n-1], and
     * given a desired oversampling factor ofac (a typical value being 4 or
     * larger), this routine fills array px[0..nout-1] with an increasing
     * sequence of frequencies (not angular frequencies) up to hifac times the
     * "average" Nyquist frequency, and fills array py[0..nout-1] with the
     * values of the Lomb normalized periodogram at those frequencies. The
     * arrays x and y are not altered. The vectors px and py are resized to nout
     * (eq. 13.8.9) if their initial size is less than this; otherwise, only
     * their first nout components are filled. The routine also returns jmax
     * such that py[jmax] is the maximum element in py, and prob, an estimate of
     * the significance of that maximum against the hypothesis of random noise.
     * A small value of prob indicates that a significant periodic signal is
     * present.
     * 
     * @param x
     * @param y
     * @param ofac
     * @param hifac
     * @param pxw
     * @param pyw
     * @param nout
     * @param jmax
     * @param prob
     */
    public static class Fasper {
	public double[] px, py;
	public int nout, jmax;
	public double prob;

	public Fasper(double[] px, double[] py) {
	    this.px = px;
	    this.py = py;
	}

	public void fasper(final double[] x, final double[] y,
		final double ofac, final double hifac) {
	    final int MACC = 4;
	    int j, k, nwk, nfreq, nfreqt, n = x.length, np = px.length;
	    double ck, ckk, cterm, cwt, den, df, effm, expy, fac, fndim, hc2wt, hs2wt, hypo, pmax, sterm, swt, xdif, xmax, xmin;
	    doubleW ave = new doubleW(0);
	    doubleW var = new doubleW(0);

	    nout = (int) (0.5 * ofac * hifac * n);
	    nfreqt = (int) (ofac * hifac * n * MACC);
	    nfreq = 64;
	    while (nfreq < nfreqt)
		nfreq <<= 1;
	    nwk = nfreq << 1;
	    if (np < nout) {
		px = resize(px, nout);
		py = resize(py, nout);
	    }
	    Moment.avevar(y, ave, var);
	    if (var.val == 0.0)
		throw new IllegalArgumentException("zero variance in fasper");
	    xmin = x[0];
	    xmax = xmin;
	    for (j = 1; j < n; j++) {
		if (x[j] < xmin)
		    xmin = x[j];
		if (x[j] > xmax)
		    xmax = x[j];
	    }
	    xdif = xmax - xmin;
	    double[] wk1 = new double[nwk];
	    double[] wk2 = new double[nwk];
	    fac = nwk / (xdif * ofac);
	    fndim = nwk;
	    for (j = 0; j < n; j++) {
		ck = IEEEremainder((x[j] - xmin) * fac, fndim);
		ckk = 2.0 * (ck++);
		ckk = IEEEremainder(ckk, fndim);
		++ckk;
		spread(y[j] - ave.val, wk1, ck, MACC);
		spread(1.0, wk2, ckk, MACC);
	    }
	    FFT.realft(wk1, 1);
	    FFT.realft(wk2, 1);
	    df = 1.0 / (xdif * ofac);
	    pmax = -1.0;
	    for (k = 2, j = 0; j < nout; j++, k += 2) {
		hypo = sqrt(wk2[k] * wk2[k] + wk2[k + 1] * wk2[k + 1]);
		hc2wt = 0.5 * wk2[k] / hypo;
		hs2wt = 0.5 * wk2[k + 1] / hypo;
		cwt = sqrt(0.5 + hc2wt);
		swt = SIGN(sqrt(0.5 - hc2wt), hs2wt);
		den = 0.5 * n + hc2wt * wk2[k] + hs2wt * wk2[k + 1];
		cterm = SQR(cwt * wk1[k] + swt * wk1[k + 1]) / den;
		sterm = SQR(cwt * wk1[k + 1] - swt * wk1[k]) / (n - den);
		px[j] = (j + 1) * df;
		py[j] = (cterm + sterm) / (2.0 * var.val);
		if (py[j] > pmax) {
		    jmax = j;
		    pmax = py[jmax];
		}
	    }
	    expy = exp(-pmax);
	    effm = 2.0 * nout / ofac;
	    prob = effm * expy;
	    if (prob > 0.01)
		prob = 1.0 - pow(1.0 - expy, effm);
	}
    }

    /**
     * Lomb periodogram for spectral analysis
     * 
     * Given n data points with abscissas x[0..n-1] (which need not be equally
     * spaced) and ordinates y[0..n-1], and given a desired oversampling factor
     * ofac (a typical value being 4 or larger), this routine fills array
     * px[0..nout-1] with an increasing sequence of frequencies (not angular
     * frequencies) up to hifac times the "average" Nyquist frequency, and fills
     * array py[0..nout-1] with the values of the Lomb normalized periodogram at
     * those frequencies. The arrays x and y are not altered. The vectors px and
     * py are resized to nout (eq. 13.8.9) if their initial size is less than
     * this; otherwise, only their first nout components are filled. The routine
     * also returns jmax such that py[jmax] is the maximum element in py, and
     * prob, an estimate of the significance of that maximum against the
     * hypothesis of random noise. A small value of prob indicates that a
     * significant periodic signal is present.
     */
    public static class Period {
	public double[] px, py;
	public int nout, jmax;
	public double prob;

	public Period(double[] px, double[] py) {
	    this.px = px;
	    this.py = py;
	}

	public void period(final double[] x, final double[] y,
		final double ofac, final double hifac) {
	    final double TWOPI = 6.283185307179586476;
	    int i, j, n = x.length, np = px.length;
	    double c, cc, cwtau, effm, expy, pnow, pymax, s, ss, sumc, sumcy, sums, sumsh, sumsy, swtau, wtau, xave, xdif, xmax, xmin, yy, arg, wtemp;
	    doubleW ave = new doubleW(0);
	    doubleW var = new doubleW(0);

	    double[] wi = new double[n];
	    double[] wpi = new double[n];
	    double[] wpr = new double[n];
	    double[] wr = new double[n];
	    nout = (int) (0.5 * ofac * hifac * n);
	    if (np < nout) {
		px = resize(px, nout);
		py = resize(py, nout);
	    }
	    Moment.avevar(y, ave, var);
	    if (var.val == 0.0)
		throw new IllegalArgumentException("zero variance in period");
	    xmax = xmin = x[0];
	    for (j = 0; j < n; j++) {
		if (x[j] > xmax)
		    xmax = x[j];
		if (x[j] < xmin)
		    xmin = x[j];
	    }
	    xdif = xmax - xmin;
	    xave = 0.5 * (xmax + xmin);
	    pymax = 0.0;
	    pnow = 1.0 / (xdif * ofac);
	    for (j = 0; j < n; j++) {
		arg = TWOPI * ((x[j] - xave) * pnow);
		wpr[j] = -2.0 * SQR(sin(0.5 * arg));
		wpi[j] = sin(arg);
		wr[j] = cos(arg);
		wi[j] = wpi[j];
	    }
	    for (i = 0; i < nout; i++) {
		px[i] = pnow;
		sumsh = sumc = 0.0;
		for (j = 0; j < n; j++) {
		    c = wr[j];
		    s = wi[j];
		    sumsh += s * c;
		    sumc += (c - s) * (c + s);
		}
		wtau = 0.5 * atan2(2.0 * sumsh, sumc);
		swtau = sin(wtau);
		cwtau = cos(wtau);
		sums = sumc = sumsy = sumcy = 0.0;
		for (j = 0; j < n; j++) {
		    s = wi[j];
		    c = wr[j];
		    ss = s * cwtau - c * swtau;
		    cc = c * cwtau + s * swtau;
		    sums += ss * ss;
		    sumc += cc * cc;
		    yy = y[j] - ave.val;
		    sumsy += yy * ss;
		    sumcy += yy * cc;
		    wr[j] = ((wtemp = wr[j]) * wpr[j] - wi[j] * wpi[j]) + wr[j];
		    wi[j] = (wi[j] * wpr[j] + wtemp * wpi[j]) + wi[j];
		}
		py[i] = 0.5 * (sumcy * sumcy / sumc + sumsy * sumsy / sums)
			/ var.val;
		if (py[i] >= pymax) {
		    jmax = i;
		    pymax = py[jmax];
		}
		pnow += 1.0 / (ofac * xdif);
	    }
	    expy = exp(-pymax);
	    effm = 2.0 * nout / ofac;
	    prob = effm * expy;
	    if (prob > 0.01)
		prob = 1.0 - pow(1.0 - expy, effm);
	}
    }

    /**
     * Given a real vector of data[0..n-1], this routine returns m linear
     * prediction coefficients as d[0..m-1], and returns the mean square
     * discrepancy as xms.
     * 
     * @param data
     * @param xms
     * @param d
     */
    public static void memcof(final double[] data, final doubleW xms,
	    final double[] d) {
	int k, j, i, n = data.length, m = d.length;
	double p = 0.0;
	double[] wk1 = new double[n];
	double[] wk2 = new double[n];
	double[] wkm = new double[n];
	for (j = 0; j < n; j++)
	    p += SQR(data[j]);
	xms.val = p / n;
	wk1[0] = data[0];
	wk2[n - 2] = data[n - 1];
	for (j = 1; j < n - 1; j++) {
	    wk1[j] = data[j];
	    wk2[j - 1] = data[j];
	}
	for (k = 0; k < m; k++) {
	    double num = 0.0, denom = 0.0;
	    for (j = 0; j < (n - k - 1); j++) {
		num += (wk1[j] * wk2[j]);
		denom += (SQR(wk1[j]) + SQR(wk2[j]));
	    }
	    d[k] = 2.0 * num / denom;
	    xms.val *= (1.0 - SQR(d[k]));
	    for (i = 0; i < k; i++)
		d[i] = wkm[i] - d[k] * wkm[k - 1 - i];
	    if (k == m - 1)
		return;
	    for (i = 0; i <= k; i++)
		wkm[i] = d[i];
	    for (j = 0; j < (n - k - 2); j++) {
		wk1[j] -= (wkm[k] * wk2[j]);
		wk2[j] = wk2[j + 1] - wkm[k] * wk1[j + 1];
	    }
	}
	throw new IllegalArgumentException("never get here in memcof");
    }

    /**
     * Given the LP coefficients d[0..m-1], this routine finds all roots of the
     * characteristic polynomial (13.6.14), reflects any roots that are outside
     * the unit circle back inside, and then returns a modified set of
     * coefficients d[0..m-1].
     * 
     * @param d
     */
    public static void fixrts(final double[] d) {
	boolean polish = true;
	int i, j, m = d.length;
	Complex[] a = new Complex[m + 1];
	Complex[] roots = new Complex[m];
	a[m] = new Complex(1.0, 0.0);
	for (j = 0; j < m; j++)
	    a[j] = new Complex(-d[m - 1 - j], 0.0);
	Roots.zroots(a, roots, polish);
	for (j = 0; j < m; j++)
	    if (roots[j].mod() > 1.0)
		roots[j] = new Complex(1.0, 0).div(roots[j].conj());
	a[0] = roots[0].neg();
	a[1] = new Complex(1.0, 0);
	for (j = 1; j < m; j++) {
	    a[j + 1] = new Complex(1.0, 0);
	    for (i = j; i >= 1; i--)
		a[i] = a[i - 1].sub(roots[j].mul(a[i]));
	    a[0] = roots[j].neg().mul(a[0]);
	}
	for (j = 0; j < m; j++)
	    d[m - 1 - j] = -a[j].re();
    }

    /**
     * Given data[0..ndata-1], and given the data's LP coefficients d[0..m-1],
     * this routine applies equation (13.6.11) to predict the next nfut data
     * points, which it returns in the array future[0..nfut-1]. Note that the
     * routine references only the last m values of data, as initial values for
     * the prediction.
     * 
     * @param data
     * @param d
     * @param future
     */
    public static void predic(final double[] data, final double[] d,
	    final double[] future) {
	int k, j, ndata = data.length, m = d.length, nfut = future.length;
	double sum, discrp;
	double[] reg = new double[m];
	for (j = 0; j < m; j++)
	    reg[j] = data[ndata - 1 - j];
	for (j = 0; j < nfut; j++) {
	    discrp = 0.0;
	    sum = discrp;
	    for (k = 0; k < m; k++)
		sum += d[k] * reg[k];
	    for (k = m - 1; k >= 1; k--)
		reg[k] = reg[k - 1];
	    future[j] = reg[0] = sum;
	}
    }

    /**
     * Given d[0..m-1] and xms as returned by memcof, this function returns the
     * power spectrum estimate P(f) as a function of fdt = fd
     * 
     * @param fdt
     * @param d
     * @param xms
     * @return
     */
    public static double evlmem(final double fdt, final double[] d,
	    final double xms) {
	int i;
	double sumr = 1.0, sumi = 0.0, wr = 1.0, wi = 0.0, wpr, wpi, wtemp, theta;

	int m = d.length;
	theta = 6.28318530717959 * fdt;
	wpr = cos(theta);
	wpi = sin(theta);
	for (i = 0; i < m; i++) {
	    wr = (wtemp = wr) * wpr - wi * wpi;
	    wi = wi * wpr + wtemp * wpi;
	    sumr -= d[i] * wr;
	    sumi -= d[i] * wi;
	}
	return xms / (sumr * sumr + sumi * sumi);
    }
}
