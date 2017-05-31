package com.nr.cg;

import static java.lang.Math.*;

/**
 * geometric polygon Copyright (C) Numerical Recipes Software 1986-2007 Java
 * translation Copyright (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Polygon {
    private final static int DIM = 2;

    private Polygon() {
    }

    public static int polywind(final Point[] vt, final Point pt) {
	for (int i = 0; i < vt.length; i++)
	    if (vt[i].dim() != DIM)
		throw new IllegalArgumentException("Need same dim!");
	int i, np, wind = 0;
	double d0, d1, p0, p1, pt0, pt1;
	np = vt.length;
	pt0 = pt.x[0];
	pt1 = pt.x[1];
	p0 = vt[np - 1].x[0];
	p1 = vt[np - 1].x[1];
	for (i = 0; i < np; i++) {
	    d0 = vt[i].x[0];
	    d1 = vt[i].x[1];
	    if (p1 <= pt1) {
		if (d1 > pt1
			&& (p0 - pt0) * (d1 - pt1) - (p1 - pt1) * (d0 - pt0) > 0)
		    wind++;
	    } else {
		if (d1 <= pt1
			&& (p0 - pt0) * (d1 - pt1) - (p1 - pt1) * (d0 - pt0) < 0)
		    wind--;
	    }
	    p0 = d0;
	    p1 = d1;
	}
	return wind;
    }

    public static int ispolysimple(final Point[] vt) {
	for (int i = 0; i < vt.length; i++)
	    if (vt[i].dim() != DIM)
		throw new IllegalArgumentException("Need same dim!");
	int i, ii, j, jj, np, schg = 0, wind = 0;
	double p0, p1, d0, d1, pp0, pp1, dd0, dd1, t, tp, t1, t2, crs, crsp = 0.0;
	np = vt.length;
	p0 = vt[0].x[0] - vt[np - 1].x[0];
	p1 = vt[0].x[1] - vt[np - 1].x[1];
	for (i = 0, ii = 1; i < np; i++, ii++) {
	    if (ii == np)
		ii = 0;
	    d0 = vt[ii].x[0] - vt[i].x[0];
	    d1 = vt[ii].x[1] - vt[i].x[1];
	    crs = p0 * d1 - p1 * d0;
	    if (crs * crsp < 0)
		schg = 1;
	    if (p1 <= 0.0) {
		if (d1 > 0.0 && crs > 0.0)
		    wind++;
	    } else {
		if (d1 <= 0.0 && crs < 0.0)
		    wind--;
	    }
	    p0 = d0;
	    p1 = d1;
	    if (crs != 0.0)
		crsp = crs;
	}
	if (abs(wind) != 1)
	    return 0;
	if (schg == 0)
	    return (wind > 0 ? 1 : -1);
	for (i = 0, ii = 1; i < np; i++, ii++) {
	    if (ii == np)
		ii = 0;
	    d0 = vt[ii].x[0];
	    d1 = vt[ii].x[1];
	    p0 = vt[i].x[0];
	    p1 = vt[i].x[1];
	    tp = 0.0;
	    for (j = i + 1, jj = i + 2; j < np; j++, jj++) {
		if (jj == np) {
		    if (i == 0)
			break;
		    jj = 0;
		}
		dd0 = vt[jj].x[0];
		dd1 = vt[jj].x[1];
		t = (dd0 - d0) * (p1 - d1) - (dd1 - d1) * (p0 - d0);
		if (t * tp <= 0.0 && j > i + 1) {
		    pp0 = vt[j].x[0];
		    pp1 = vt[j].x[1];
		    t1 = (p0 - dd0) * (pp1 - dd1) - (p1 - dd1) * (pp0 - dd0);
		    t2 = (d0 - dd0) * (pp1 - dd1) - (d1 - dd1) * (pp0 - dd0);
		    if (t1 * t2 <= 0.0)
			return 0;
		}
		tp = t;
	    }
	}
	return (wind > 0 ? 2 : -2);
    }
}
