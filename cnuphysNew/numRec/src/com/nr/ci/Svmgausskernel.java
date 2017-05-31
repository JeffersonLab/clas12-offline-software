package com.nr.ci;

import static java.lang.Math.*;
import static com.nr.NRUtil.*;

public class Svmgausskernel extends Svmgenkernel {
    int n;

    double sigma;

    public Svmgausskernel(final double[][] ddata, final double[] yy,
	    final double ssigma) {
	super(yy, ddata);
	n = data[0].length;
	sigma = ssigma;
	fill();
    }

    @Override
    public double kernel(final double[] xi, final double[] xj) {
	double dott = 0.;
	for (int k = 0; k < n; k++)
	    dott += SQR(xi[k] - xj[k]);
	return exp(-0.5 * dott / (sigma * sigma));
    }
}
