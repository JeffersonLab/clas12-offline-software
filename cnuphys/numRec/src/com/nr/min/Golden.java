package com.nr.min;

import static java.lang.Math.*;
import com.nr.UniVarRealValueFun;

/**
 * Golden Section Search in One Dimension
 * 
 * Copyright (C) Numerical Recipes Software 1986-2007 Java translation Copyright
 * (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class Golden extends Bracketmethod {
    double xmin, fmin;
    final double tol;

    public Golden() {
	this(3.0e-8);
    }

    public Golden(final double toll) {
	tol = toll;
    }

    public double minimize(final UniVarRealValueFun func) {
	final double R = 0.61803399, C = 1.0 - R;
	double x1, x2;
	double x0 = ax;
	double x3 = cx;
	if (abs(cx - bx) > abs(bx - ax)) {
	    x1 = bx;
	    x2 = bx + C * (cx - bx);
	} else {
	    x2 = bx;
	    x1 = bx - C * (bx - ax);
	}
	double f1 = func.funk(x1);
	double f2 = func.funk(x2);
	while (abs(x3 - x0) > tol * (abs(x1) + abs(x2))) {
	    if (f2 < f1) {
		// shft3(x0,x1,x2,R*x2+C*x3);
		double dum = R * x2 + C * x3;
		x0 = x1;
		x1 = x2;
		x2 = dum;
		// shft2(f1,f2,func.funk(x2));
		f1 = f2;
		f2 = func.funk(x2);
	    } else {
		// shft3(x3,x2,x1,R*x1+C*x0);
		double dum = R * x1 + C * x0;
		x3 = x2;
		x2 = x1;
		x1 = dum;
		// shft2(f2,f1,func.funk(x1));
		f2 = f1;
		f1 = func.funk(x1);
	    }
	}
	if (f1 < f2) {
	    xmin = x1;
	    fmin = f1;
	} else {
	    xmin = x2;
	    fmin = f2;
	}
	return xmin;
    }
}
