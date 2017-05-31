package com.nr.fi;

import static java.lang.Math.*;
import com.nr.UniVarRealValueFun;

public class Midexp extends Midpnt {

    @Override
    public double func(final double x) {
	return funk.funk(-log(x)) / x;
    }

    public Midexp(final UniVarRealValueFun funcc, final double aa,
	    final double bb) {
	super(funcc, aa, bb);
	a = 0.0;
	b = exp(-aa);
    }
}
