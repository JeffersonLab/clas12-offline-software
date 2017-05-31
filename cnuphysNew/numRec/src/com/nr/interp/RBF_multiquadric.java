package com.nr.interp;

import static com.nr.NRUtil.*;

import static java.lang.Math.*;

public class RBF_multiquadric implements RBF_fn {
    double r02;

    public RBF_multiquadric() {
	this(1.0);
    }

    public RBF_multiquadric(final double scale) {
	r02 = SQR(scale);
    }

    @Override
    public double rbf(final double r) {
	return sqrt(SQR(r) + r02);
    }
}
