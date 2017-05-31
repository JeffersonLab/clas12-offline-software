package com.nr.fft;

import com.nr.Complex;

public class WrapVecDoub {

    private double[] v;

    private int n, mask;

    public WrapVecDoub(final int nn) {
	v = new double[nn];
	n = nn / 2;
	mask = n - 1;
	validate();
    }

    public WrapVecDoub(final double[] vec) {
	v = vec;
	n = vec.length / 2;
	mask = n - 1;
	validate();
    }

    private void validate() {
	if ((n & (n - 1)) != 0)
	    throw new IllegalArgumentException("vec size must be power of 2");
    }

    public void set(int i, Complex c) {
	int ii = (i & mask) << 1;
	v[ii] = c.re();
	v[ii + 1] = c.im();
    }

    public Complex get(int i) {
	int ii = (i & mask) << 1;
	return new Complex(v[ii], v[ii + 1]);
    }

    public void setReal(int i, double rr) {
	v[(i & mask) << 1] = rr;
    }

    public double real(int i) {
	return v[(i & mask) << 1];
    }

    public void setImag(int i, double rr) {
	v[((i & mask) << 1) + 1] = rr;
    }

    public double imag(int i) {
	return v[((i & mask) << 1) + 1];
    }

    public double[] get() {
	return v;
    }

}
