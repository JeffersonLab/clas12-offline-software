package com.nr.lna;

import static java.lang.Math.*;
import static com.nr.NRUtil.*;

import java.io.*;

import com.nr.UniVarRealValueFun;

/**
 * PostScript plot
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class PSplot extends PSpage {

    double pll, qll, pur, qur;
    double xll, yll, xur, yur;
    double[] xbox, ybox;
    double majticsz, minticsz;

    public PSplot(final PSpage page, final double ppll, final double ppur,
	    final double qqll, final double qqur) {
	pll = ppll;
	qll = qqll;
	pur = ppur;
	qur = qqur;
	xll = ppll;
	yll = qqll;
	xur = ppur;
	yur = qqur;
	xbox = new double[4];
	ybox = new double[4];
	majticsz = 8.;
	minticsz = 4.;
	fontname = new String(page.fontname);
	fontsize = page.fontsize;
	PLT = page.PLT;
	setlimits(xll, xur, yll, yur);
    }

    double p(final double x) {
	return pll + (pur - pll) * (x - xll) / (xur - xll);
    }

    double q(final double y) {
	return qll + (qur - qll) * (y - yll) / (yur - yll);
    }

    public void setlimits(final double xxll, final double xxur,
	    final double yyll, final double yyur) {
	xbox[0] = xbox[3] = xll = xxll;
	ybox[0] = ybox[1] = yll = yyll;
	xbox[1] = xbox[2] = xur = xxur;
	ybox[2] = ybox[3] = yur = yyur;
    }

    @Override
    public void lineseg(final double xs, final double ys, final double xf,
	    final double yf) {
	super.lineseg(p(xs), q(ys), p(xf), q(yf));
    }

    @Override
    public void polyline(final double[] x, final double[] y) {
	polyline(x, y, false, false, false);
    }

    public void polyline(final double[] x, final double[] y,
	    final boolean close, final boolean fill) {
	polyline(x, y, close, fill, false);
    }

    @Override
    public void polyline(final double[] x, final double[] y,
	    final boolean close, final boolean fill, final boolean clip) {
	int i;
	double[] xx = buildVector(x);
	double[] yy = buildVector(y);
	for (i = 0; i < x.length; i++)
	    xx[i] = p(x[i]);
	for (i = 0; i < y.length; i++)
	    yy[i] = q(y[i]);
	super.polyline(xx, yy, close, fill, clip);
    }

    public void dot(final double x, final double y) {
	dot(x, y, 2);
    }

    public void dot(final double x, final double y, final double size) {
	super.pointsymbol(p(x), q(y), 108, size);
    }

    @Override
    public void pointsymbol(final double x, final double y, final int num,
	    final double size) {
	super.pointsymbol(p(x), q(y), num, size);
    }

    public void lineplot(final double[] x, final double[] y) {
	polyline(x, y);
    }

    public void frame() {
	polyline(xbox, ybox, true, false);
    }

    public void clear() {
	gsave();
	setgray(1.);
	polyline(xbox, ybox, true, true);
	grestore();
    }

    public void clip() {
	gsave();
	polyline(xbox, ybox, true, false, true);
    }

    public void clip(final double[] x, final double[] y) {
	gsave();
	polyline(x, y, true, false, true);
    }

    public void unclip() {
	grestore();
    }

    public void xlabel(final String text) {
	putctext(text, 0.5 * (pll + pur), qll - 2. * fontsize - 8.);
    }

    public void ylabel(final String text) {
	putctext(text, pll - 3. * fontsize - 8., 0.5 * (qll + qur), 90.);
    }

    public void label(final String text, final double x, final double y) {
	label(text, x, y, 0);
    }

    public void label(final String text, final double x, final double y,
	    final double rot) {
	puttext(text, p(x), q(y), rot);
    }

    public String scalestr(double x) {
	if (abs(x) < 1.e-15)
	    x = 0.;
	return String.format("%g", x);
    }

    public void scales(final double xmajd, final double xmind,
	    final double ymajd, final double ymind) {
	scales(xmajd, xmind, ymajd, ymind, 2, 2, 1, 1);
    }

    public void scales(final double xmajd, final double xmind,
	    final double ymajd, final double ymind, final int dox,
	    final int doy, final int doxx, final int doyy) {
	String str;
	double x, y, xlo, ylo;
	if (dox != 0 || doxx != 0) {
	    xlo = ceil(min(xll, xur) / xmajd) * xmajd;
	    for (x = xlo; x <= max(xll, xur); x += xmajd) {
		str = scalestr(x);
		if (dox > 1)
		    putctext(str, p(x), qll - fontsize - 2.);
		if (dox != 0)
		    super.lineseg(p(x), qll, p(x), qll + majticsz);
		if (doxx != 0)
		    super.lineseg(p(x), qur, p(x), qur - majticsz);
	    }
	    xlo = ceil(min(xll, xur) / xmind) * xmind;
	    for (x = xlo; x <= max(xll, xur); x += xmind) {
		if (dox != 0)
		    super.lineseg(p(x), qll, p(x), qll + minticsz);
		if (doxx != 0)
		    super.lineseg(p(x), qur, p(x), qur - minticsz);
	    }
	}
	if (doy != 0 || doyy != 0) {
	    ylo = ceil(min(yll, yur) / ymajd) * ymajd;
	    for (y = ylo; y <= max(yll, yur); y += ymajd) {
		str = scalestr(y);
		if (doy > 1)
		    putrtext(str, pll - 4., q(y) - 0.3 * fontsize);
		if (doy != 0)
		    super.lineseg(pll, q(y), pll + majticsz, q(y));
		if (doyy != 0)
		    super.lineseg(pur, q(y), pur - majticsz, q(y));
	    }
	    ylo = ceil(min(yll, yur) / ymind) * ymind;
	    for (y = ylo; y <= max(yll, yur); y += ymind) {
		if (doy != 0)
		    super.lineseg(pll, q(y), pll + minticsz, q(y));
		if (doyy != 0)
		    super.lineseg(pur, q(y), pur - minticsz, q(y));
	    }
	}
    }

    public void autoscales() {
	double xmajd, xmind, ymajd, ymind;
	xmajd = pow(10., ((int) (log10(abs(xur - xll)) - 1.1)));
	xmind = xmajd / 5.;
	ymajd = pow(10., ((int) (log10(abs(yur - yll)) - 1.1)));
	ymind = ymajd / 5.;
	scales(xmajd, xmind, ymajd, ymind);
    }

    public static void scrsho(final UniVarRealValueFun fx, final double x1,
	    final double x2, final File psfile) throws IOException {
	final int RES = 500;
	final double XLL = 75., XUR = 525., YLL = 250., YUR = 700.;
	double[] xx = new double[RES];
	double[] yy = new double[RES];
	int i;
	double ymax = -9.99e99, ymin = 9.99e99, del;
	for (i = 0; i < RES; i++) {
	    xx[i] = x1 + i * (x2 - x1) / (RES - 1.);
	    yy[i] = fx.funk(xx[i]);
	    if (yy[i] > ymax)
		ymax = yy[i];
	    if (yy[i] < ymin)
		ymin = yy[i];
	}
	del = 0.05 * ((ymax - ymin) + (ymax == ymin ? abs(ymax) : 0.));
	PSpage pg = new PSpage(psfile);
	PSplot plot = new PSplot(pg, XLL, XUR, YLL, YUR);
	plot.setlimits(x1, x2, ymin - del, ymax + del);
	plot.frame();
	plot.autoscales();
	plot.lineplot(xx, yy);
	if (ymax * ymin < 0.)
	    plot.lineseg(x1, 0., x2, 0.);
	plot.close();
    }
}
