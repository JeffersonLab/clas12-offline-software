package com.nr.interp;

import static com.nr.NRUtil.*;

import static java.lang.Math.*;

public class RBF_thinplate implements RBF_fn {
    double r0;

    public RBF_thinplate() {
	this(1.0);
    }

    public RBF_thinplate(final double scale) {
	r0 = scale;
    }

    @Override
    public double rbf(final double r) {
	return r <= 0. ? 0. : SQR(r) * log(r / r0);
    }
}
