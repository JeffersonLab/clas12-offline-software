package com.nr.model;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;

import org.netlib.util.doubleW;

import com.nr.UniVarRealValueFun;
import com.nr.min.Brent;
import com.nr.root.Roots;
import com.nr.sf.Gamma;
import com.nr.stat.Moment;

public class Fitexy {
    public double a, b, siga, sigb, chi2, q;
    int ndat;
    public double[] xx, yy, sx, sy, ww;
    public double aa, offs;

    public class Chixy implements UniVarRealValueFun {
	// XXX reference use it, remove it.
	// double[] xx,yy,sx,sy,ww;

	// double aa,offs;

	public Chixy() {
	}

	@Override
	public double funk(final double bang) {
	    return get(bang);
	}

	public double get(final double bang) {
	    final double BIG = 1.0e30;
	    int j, nn = xx.length;
	    double ans, avex = 0.0, avey = 0.0, sumw = 0.0, b;
	    b = tan(bang);
	    for (j = 0; j < nn; j++) {
		ww[j] = SQR(b * sx[j]) + SQR(sy[j]);
		sumw += (ww[j] = (ww[j] < 1.0 / BIG ? BIG : 1.0 / ww[j]));
		avex += ww[j] * xx[j];
		avey += ww[j] * yy[j];
	    }
	    avex /= sumw;
	    avey /= sumw;
	    aa = avey - b * avex;
	    for (ans = -offs, j = 0; j < nn; j++)
		ans += ww[j] * SQR(yy[j] - aa - b * xx[j]);
	    return ans;
	}
    }

    public Fitexy(final double[] x, final double[] y, final double[] sigx,
	    final double[] sigy) {
	ndat = x.length;
	xx = new double[ndat];
	yy = new double[ndat];
	sx = new double[ndat];
	sy = new double[ndat];
	ww = new double[ndat];

	final double POTN = 1.571000, BIG = 1.0e30, ACC = 1.0e-6;
	final double PI = 3.141592653589793238;
	Gamma gam = new Gamma();
	Brent brent = new Brent(ACC);
	Chixy chixy = new Chixy();
	int j;
	double amx, amn, scale, bmn, bmx, d1, d2, r2;
	double[] ang = new double[7];
	double[] ch = new double[7];
	doubleW varx = new doubleW(0);
	doubleW vary = new doubleW(0);
	doubleW dum1 = new doubleW(0);

	Moment.avevar(x, dum1, varx);
	Moment.avevar(y, dum1, vary);
	scale = sqrt(varx.val / vary.val);
	for (j = 0; j < ndat; j++) {
	    xx[j] = x[j];
	    yy[j] = y[j] * scale;
	    sx[j] = sigx[j];
	    sy[j] = sigy[j] * scale;
	    ww[j] = sqrt(SQR(sx[j]) + SQR(sy[j]));
	}
	Fitab fit = new Fitab(xx, yy, ww);
	b = fit.b;
	offs = ang[0] = 0.0;
	ang[1] = atan(b);
	ang[3] = 0.0;
	ang[4] = ang[1];
	ang[5] = POTN;
	for (j = 3; j < 6; j++)
	    ch[j] = chixy.get(ang[j]);
	brent.bracket(ang[0], ang[1], chixy);
	ang[0] = brent.ax;
	ang[1] = brent.bx;
	ang[2] = brent.cx;
	ch[0] = brent.fa;
	ch[1] = brent.fb;
	ch[2] = brent.fc;
	b = brent.minimize(chixy);
	chi2 = chixy.get(b);
	a = aa;
	q = gam.gammq(0.5 * (ndat - 2), chi2 * 0.5);
	r2 = 0.0;
	for (j = 0; j < ndat; j++)
	    r2 += ww[j];
	r2 = 1.0 / r2;
	bmx = bmn = BIG;
	offs = chi2 + 1.0;
	for (j = 0; j < 6; j++) {
	    if (ch[j] > offs) {
		d1 = abs(ang[j] - b);
		while (d1 >= PI)
		    d1 -= PI;
		d2 = PI - d1;
		if (ang[j] < b) {
		    // SWAP(d1,d2);
		    double swap = d1;
		    d1 = d2;
		    d2 = swap;
		}
		if (d1 < bmx)
		    bmx = d1;
		if (d2 < bmn)
		    bmn = d2;
	    }
	}
	if (bmx < BIG) {
	    bmx = Roots.zbrent(chixy, b, b + bmx, ACC) - b;
	    amx = aa - a;
	    bmn = Roots.zbrent(chixy, b, b - bmn, ACC) - b;
	    amn = aa - a;
	    sigb = sqrt(0.5 * (bmx * bmx + bmn * bmn)) / (scale * SQR(cos(b)));
	    siga = sqrt(0.5 * (amx * amx + amn * amn) + r2) / scale;
	} else
	    sigb = siga = BIG;
	a /= scale;
	b = tan(b) / scale;
    }

}
