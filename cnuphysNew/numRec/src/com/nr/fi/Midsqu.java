package com.nr.fi;

import static java.lang.Math.*;
import com.nr.UniVarRealValueFun;

public class Midsqu extends Midpnt {
    double borig;

    @Override
    public double func(final double x) {
	return 2.0 * x * funk.funk(borig - x * x);
    }

    public Midsqu(final UniVarRealValueFun funcc, final double aa,
	    final double bb) {
	super(funcc, aa, bb);
	borig = bb;
	a = 0;
	b = sqrt(bb - aa);
    }
}
