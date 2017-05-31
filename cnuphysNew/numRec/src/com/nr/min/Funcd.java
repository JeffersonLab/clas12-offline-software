package com.nr.min;

import static java.lang.Math.*;
import com.nr.RealValueFun;
import com.nr.RealValueFunWithDiff;

public class Funcd implements RealValueFunWithDiff {
    double EPS;
    RealValueFun func;
    double f;

    public Funcd(final RealValueFun funcc) {
	EPS = 1.0e-8;
	func = funcc;
    }

    @Override
    public double funk(final double[] x) {
	return get(x);
    }

    public double get(final double[] x) {
	return f = func.funk(x);
    }

    @Override
    public void df(final double[] x, final double[] df) {
	int n = x.length;
	double[] xh = x;
	double fold = f;
	for (int j = 0; j < n; j++) {
	    double temp = x[j];
	    double h = EPS * abs(temp);
	    if (h == 0.0)
		h = EPS;
	    xh[j] = temp + h;
	    h = xh[j] - temp;
	    double fh = get(xh);
	    xh[j] = temp;
	    df[j] = (fh - fold) / h;
	}
    }
}
