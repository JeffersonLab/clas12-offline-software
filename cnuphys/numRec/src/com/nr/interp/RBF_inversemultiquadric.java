package com.nr.interp;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;

public class RBF_inversemultiquadric implements RBF_fn {
    double r02;

    public RBF_inversemultiquadric() {
	this(1.0);
    }

    public RBF_inversemultiquadric(final double scale) {
	r02 = SQR(scale);
    }

    @Override
    public double rbf(final double r) {
	return 1. / sqrt(SQR(r) + r02);
    }

}
