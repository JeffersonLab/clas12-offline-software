package com.nr.fi;

import com.nr.UniVarRealValueFun;

public class Midinf extends Midpnt {

    @Override
    public double func(final double x) {
	return funk.funk(1.0 / x) / (x * x);
    }

    public Midinf(final UniVarRealValueFun funcc, final double aa,
	    final double bb) {
	super(funcc, aa, bb);
	a = 1.0 / bb;
	b = 1.0 / aa;
    }

}
