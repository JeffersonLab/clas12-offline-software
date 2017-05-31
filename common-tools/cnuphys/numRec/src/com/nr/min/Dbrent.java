package com.nr.min;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;
import com.nr.UniValRealValueFunWithDiff;

/**
 * Brent's method to find a minimum, modified to use derivatives.
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Dbrent extends Bracketmethod {
    double xmin, fmin;
    final double tol;

    public Dbrent() {
	this(3.0e-8);
    }

    public Dbrent(final double toll) {
	this.tol = toll;
    }

    public double minimize(final UniValRealValueFunWithDiff funcd) {
	final int ITMAX = 100;
	final double ZEPS = DBL_EPSILON * 1.0e-3;
	boolean ok1, ok2;
	double a, b, d = 0.0, d1, d2, du, dv, dw, dx, e = 0.0;
	double fu, fv, fw, fx, olde, tol1, tol2, u, u1, u2, v, w, x, xm;

	a = (ax < cx ? ax : cx);
	b = (ax > cx ? ax : cx);
	x = w = v = bx;
	fw = fv = fx = funcd.funk(x);
	dw = dv = dx = funcd.df(x);
	for (int iter = 0; iter < ITMAX; iter++) {
	    xm = 0.5 * (a + b);
	    tol1 = tol * abs(x) + ZEPS;
	    tol2 = 2.0 * tol1;
	    if (abs(x - xm) <= (tol2 - 0.5 * (b - a))) {
		fmin = fx;
		return xmin = x;
	    }
	    if (abs(e) > tol1) {
		d1 = 2.0 * (b - a);
		d2 = d1;
		if (dw != dx)
		    d1 = (w - x) * dx / (dx - dw);
		if (dv != dx)
		    d2 = (v - x) * dx / (dx - dv);
		u1 = x + d1;
		u2 = x + d2;
		ok1 = (a - u1) * (u1 - b) > 0.0 && dx * d1 <= 0.0;
		ok2 = (a - u2) * (u2 - b) > 0.0 && dx * d2 <= 0.0;
		olde = e;
		e = d;
		if (ok1 || ok2) {
		    if (ok1 && ok2)
			d = (abs(d1) < abs(d2) ? d1 : d2);
		    else if (ok1)
			d = d1;
		    else
			d = d2;
		    if (abs(d) <= abs(0.5 * olde)) {
			u = x + d;
			if (u - a < tol2 || b - u < tol2)
			    d = SIGN(tol1, xm - x);
		    } else {
			d = 0.5 * (e = (dx >= 0.0 ? a - x : b - x));
		    }
		} else {
		    d = 0.5 * (e = (dx >= 0.0 ? a - x : b - x));
		}
	    } else {
		d = 0.5 * (e = (dx >= 0.0 ? a - x : b - x));
	    }
	    if (abs(d) >= tol1) {
		u = x + d;
		fu = funcd.funk(u);
	    } else {
		u = x + SIGN(tol1, d);
		fu = funcd.funk(u);
		if (fu > fx) {
		    fmin = fx;
		    return xmin = x;
		}
	    }
	    du = funcd.df(u);
	    if (fu <= fx) {
		if (u >= x)
		    a = x;
		else
		    b = x;
		// mov3(v,fv,dv,w,fw,dw);
		v = w;
		fv = fw;
		dv = dw;
		// mov3(w,fw,dw,x,fx,dx);
		w = x;
		fw = fx;
		dw = dx;
		// mov3(x,fx,dx,u,fu,du);
		x = u;
		fx = fu;
		dx = du;
	    } else {
		if (u < x)
		    a = u;
		else
		    b = u;
		if (fu <= fw || w == x) {
		    // mov3(v,fv,dv,w,fw,dw);
		    v = w;
		    fv = fw;
		    dv = dw;
		    // mov3(w,fw,dw,u,fu,du);
		    w = u;
		    fw = fu;
		    dw = du;
		} else if (fu < fv || v == x || v == w) {
		    // mov3(v,fv,dv,u,fu,du);
		    v = u;
		    fv = fu;
		    dv = du;
		}
	    }
	}
	throw new IllegalArgumentException(
		"Too many iterations in routine dbrent");
    }

}
