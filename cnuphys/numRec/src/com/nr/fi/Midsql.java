package com.nr.fi;

import static java.lang.Math.*;
import com.nr.UniVarRealValueFun;

public class Midsql extends Midpnt {
    double aorig;

    @Override
    public double func(final double x) {
	return 2.0 * x * funk.funk(aorig + x * x);
    }

    public Midsql(final UniVarRealValueFun funcc, final double aa,
	    final double bb) {
	super(funcc, aa, bb);
	aorig = aa;
	a = 0;
	b = sqrt(bb - aa);
    }
}
