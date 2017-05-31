package com.nr.fi;

import static com.nr.fi.GaussianWeights.qgaus;
import com.nr.RealValueFun;
import com.nr.UniVarRealValueFun;

public class NRf1 implements UniVarRealValueFun {
    RealValueFun y1;
    RealValueFun y2;
    NRf2 f2;

    public NRf1(final RealValueFun y1, final RealValueFun y2,
	    final RealValueFun z1, final RealValueFun z2) {
	this.y1 = y1;
	this.y2 = y2;
	this.f2 = new NRf2(z1, z2);
    }

    @Override
    public double funk(final double x) {
	f2.f3.xsav = x;
	return qgaus(f2, y1.funk(new double[] { x }),
		y2.funk(new double[] { x }));
    }
}
