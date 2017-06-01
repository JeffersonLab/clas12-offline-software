package com.nr.interp;

import static com.nr.NRUtil.*;

import static java.lang.Math.*;

public class RBF_gauss implements RBF_fn {
    double r0;

    public RBF_gauss() {
	this(1.0);
    }

    public RBF_gauss(final double scale) {
	r0 = scale;
    }

    @Override
    public double rbf(final double r) {
	return exp(-0.5 * SQR(r / r0));
    }

}
