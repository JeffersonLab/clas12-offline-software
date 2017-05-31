package com.nr.fi;

import static com.nr.fi.GaussianWeights.qgaus;
import com.nr.RealValueFun;
import com.nr.UniVarRealValueFun;

public class NRf3 implements UniVarRealValueFun {
    double xsav, ysav;
    RealValueFun func3d;

    public NRf3() {

    }

    @Override
    public double funk(final double z) {
	return func3d.funk(new double[] { xsav, ysav, z });
    }

    /**
     * Returns the integral of a user-supplied function func over a
     * three-dimensional region specified by the limits x1, x2, and by the
     * user-supplied functions y1, y2, z1, and z2, as defined in (4.8.2).
     * Integration is performed by calling qgaus recursively.
     * 
     * @param func
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     * @param z1
     * @param z2
     * @return
     */
    public static double quad3d(final RealValueFun func, final double x1,
	    final double x2, final RealValueFun y1, final RealValueFun y2,
	    final RealValueFun z1, final RealValueFun z2) {
	NRf1 f1 = new NRf1(y1, y2, z1, z2);
	f1.f2.f3.func3d = func;
	return qgaus(f1, x1, x2);
    }
}
