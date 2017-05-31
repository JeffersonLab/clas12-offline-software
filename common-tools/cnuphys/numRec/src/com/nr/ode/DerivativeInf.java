package com.nr.ode;

public interface DerivativeInf {
    public void derivs(final double x, double[] y, double[] dydx);

    public void jacobian(final double x, double[] y, double[] dfdx,
	    double[][] dfdy);
}
