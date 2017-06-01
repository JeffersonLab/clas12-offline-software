package com.nr.fi;

import static com.nr.fi.GaussianWeights.qgaus;
import com.nr.RealValueFun;
import com.nr.UniVarRealValueFun;

public class NRf2 implements UniVarRealValueFun {
    NRf3 f3 = new NRf3();
    RealValueFun z1;
    RealValueFun z2;

    public NRf2(final RealValueFun zz1, final RealValueFun zz2) {
	z1 = zz1;
	z2 = zz2;
    }

    @Override
    public double funk(final double y) {
	f3.ysav = y;
	return qgaus(f3, z1.funk(new double[] { f3.xsav, y }),
		z2.funk(new double[] { f3.xsav, y }));
    }
}
