package com.nr.root;

import static com.nr.NRUtil.*;
import com.nr.RealMultiValueFun;
import com.nr.RealValueFun;

public class NRfmin implements RealValueFun {

    /**
     * the value of fvec modify after this method call. User nedd to call funk
     * before use fvec After fvec initialize, user should not assign fvec.
     */
    double[] fvec;
    final RealMultiValueFun func;
    int n;

    public NRfmin(final RealMultiValueFun funcc) {
	func = funcc;
    }

    @Override
    public double funk(final double[] x) {
	n = x.length;
	double sum = 0;
	// XXX n always as same as r.length ?
	double[] r = func.funk(x);
	if (fvec == null) {
	    fvec = new double[r.length];
	}
	System.arraycopy(r, 0, fvec, 0, r.length);

	for (int i = 0; i < n; i++)
	    sum += SQR(fvec[i]);
	return 0.5 * sum;
    }
}
