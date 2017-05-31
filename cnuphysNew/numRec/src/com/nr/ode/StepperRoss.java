package com.nr.ode;

import static com.nr.NRUtil.*;
import static java.lang.Math.*;
import org.netlib.util.*;

import com.nr.la.LUdcmp;

/**
 * fourth-order stiffly stable Rosenbrock stepper Copyright (C) Numerical
 * Recipes Software 1986-2007 Java translation Copyright (C) Huang Wen Hui 2012
 *
 * @author hwh
 *
 */
public class StepperRoss extends StepperBase {
    final static double c2 = 0.386;
    final static double c3 = 0.21;
    final static double c4 = 0.63;
    final static double bet2p = 0.0317;
    final static double bet3p = 0.0635;
    final static double bet4p = 0.3438;
    final static double d1 = 0.2500000000000000e+00;
    final static double d2 = -0.1043000000000000e+00;
    final static double d3 = 0.1035000000000000e+00;
    final static double d4 = -0.3620000000000023e-01;
    final static double a21 = 0.1544000000000000e+01;
    final static double a31 = 0.9466785280815826e+00;
    final static double a32 = 0.2557011698983284e+00;
    final static double a41 = 0.3314825187068521e+01;
    final static double a42 = 0.2896124015972201e+01;
    final static double a43 = 0.9986419139977817e+00;
    final static double a51 = 0.1221224509226641e+01;
    final static double a52 = 0.6019134481288629e+01;
    final static double a53 = 0.1253708332932087e+02;
    final static double a54 = -0.6878860361058950e+00;
    final static double c21 = -0.5668800000000000e+01;
    final static double c31 = -0.2430093356833875e+01;
    final static double c32 = -0.2063599157091915e+00;
    final static double c41 = -0.1073529058151375e+00;
    final static double c42 = -0.9594562251023355e+01;
    final static double c43 = -0.2047028614809616e+02;
    final static double c51 = 0.7496443313967647e+01;
    final static double c52 = -0.1024680431464352e+02;
    final static double c53 = -0.3399990352819905e+02;
    final static double c54 = 0.1170890893206160e+02;
    final static double c61 = 0.8083246795921522e+01;
    final static double c62 = -0.7981132988064893e+01;
    final static double c63 = -0.3152159432874371e+02;
    final static double c64 = 0.1631930543123136e+02;
    final static double c65 = -0.6058818238834054e+01;
    final static double gam = 0.2500000000000000e+00;
    final static double d21 = 0.1012623508344586e+02;
    final static double d22 = -0.7487995877610167e+01;
    final static double d23 = -0.3480091861555747e+02;
    final static double d24 = -0.7992771707568823e+01;
    final static double d25 = 0.1025137723295662e+01;
    final static double d31 = -0.6762803392801253e+00;
    final static double d32 = 0.6087714651680015e+01;
    final static double d33 = 0.1643084320892478e+02;
    final static double d34 = 0.2476722511418386e+02;
    final static double d35 = -0.6594389125716872e+01;
    double[][] dfdy;
    double[] dfdx;
    double[] k1, k2, k3, k4, k5, k6;
    double[] cont1, cont2, cont3, cont4;
    double[][] a;
    Controller con = new Controller();

    public static class Controller {
	double hnext;
	boolean reject;
	boolean first_step;
	double errold;
	double hold;

	public Controller() {
	    reject = false;
	    first_step = true;
	}

	public boolean success(final double err, final doubleW h) {
	    final double safe = 0.9, fac1 = 5.0, fac2 = 1.0 / 6.0;
	    double fac = max(fac2, min(fac1, pow(err, 0.25) / safe));
	    double hnew = h.val / fac;
	    if (err <= 1.0) {
		if (!first_step) {
		    double facpred = (hold / h.val)
			    * pow(err * err / errold, 0.25) / safe;
		    facpred = max(fac2, min(fac1, facpred));
		    fac = max(fac, facpred);
		    hnew = h.val / fac;
		}
		first_step = false;
		hold = h.val;
		errold = max(0.01, err);
		if (reject)
		    hnew = (h.val >= 0.0 ? min(hnew, h.val) : max(hnew, h.val));
		hnext = hnew;
		reject = false;
		return true;
	    } else {
		h.val = hnew;
		reject = true;
		return false;
	    }
	}
    }

    public StepperRoss() {

    }

    public StepperRoss(final double[] yy, final double[] dydxx,
	    final double xx, final double atoll, final double rtoll,
	    final boolean dense) {
	setParam(yy, dydxx, xx, atoll, rtoll, dense);
    }

    @Override
    public void setParam(final double[] yy, final double[] dydxx,
	    final double xx, final double atoll, final double rtoll,
	    final boolean dense) {
	super.setParam(yy, dydxx, xx, atoll, rtoll, dense);
	dfdy = new double[n][n];
	dfdx = new double[n];
	k1 = new double[n];
	k2 = new double[n];
	k3 = new double[n];
	k4 = new double[n];
	k5 = new double[n];
	k6 = new double[n];
	cont1 = new double[n];
	cont2 = new double[n];
	cont3 = new double[n];
	cont4 = new double[n];
	a = new double[n][n];
	EPS = DBL_EPSILON;
    }

    @Override
    public void step(final double htry, final DerivativeInf derivs) {
	double[] dydxnew = new double[n];
	doubleW h = new doubleW(htry);
	derivs.jacobian(x, y, dfdx, dfdy);
	for (;;) {
	    dy(h.val, derivs);
	    double err = error();
	    if (con.success(err, h))
		break;
	    if (abs(h.val) <= abs(x) * EPS)
		throw new IllegalArgumentException(
			"stepsize underflow in StepperRoss");
	}
	derivs.derivs(x + h.val, yout, dydxnew);
	if (dense)
	    prepare_dense(h.val, dydxnew);
	System.arraycopy(dydxnew, 0, dydx, 0, dydxnew.length);
	System.arraycopy(yout, 0, y, 0, y.length);
	// dydx=dydxnew;
	// y=yout;
	xold = x;
	x += (hdid = h.val);
	hnext = con.hnext;
    }

    public void dy(final double h, final DerivativeInf derivs) {
	double[] ytemp = new double[n], dydxnew = new double[n];
	int i;
	for (i = 0; i < n; i++) {
	    for (int j = 0; j < n; j++)
		a[i][j] = -dfdy[i][j];
	    a[i][i] += 1.0 / (gam * h);
	}
	LUdcmp alu = new LUdcmp(a);
	for (i = 0; i < n; i++)
	    ytemp[i] = dydx[i] + h * d1 * dfdx[i];
	alu.solve(ytemp, k1);
	for (i = 0; i < n; i++)
	    ytemp[i] = y[i] + a21 * k1[i];
	derivs.derivs(x + c2 * h, ytemp, dydxnew);
	for (i = 0; i < n; i++)
	    ytemp[i] = dydxnew[i] + h * d2 * dfdx[i] + c21 * k1[i] / h;
	alu.solve(ytemp, k2);
	for (i = 0; i < n; i++)
	    ytemp[i] = y[i] + a31 * k1[i] + a32 * k2[i];
	derivs.derivs(x + c3 * h, ytemp, dydxnew);
	for (i = 0; i < n; i++)
	    ytemp[i] = dydxnew[i] + h * d3 * dfdx[i]
		    + (c31 * k1[i] + c32 * k2[i]) / h;
	alu.solve(ytemp, k3);
	for (i = 0; i < n; i++)
	    ytemp[i] = y[i] + a41 * k1[i] + a42 * k2[i] + a43 * k3[i];
	derivs.derivs(x + c4 * h, ytemp, dydxnew);
	for (i = 0; i < n; i++)
	    ytemp[i] = dydxnew[i] + h * d4 * dfdx[i]
		    + (c41 * k1[i] + c42 * k2[i] + c43 * k3[i]) / h;
	alu.solve(ytemp, k4);
	for (i = 0; i < n; i++)
	    ytemp[i] = y[i] + a51 * k1[i] + a52 * k2[i] + a53 * k3[i] + a54
		    * k4[i];
	double xph = x + h;
	derivs.derivs(xph, ytemp, dydxnew);
	for (i = 0; i < n; i++)
	    k6[i] = dydxnew[i]
		    + (c51 * k1[i] + c52 * k2[i] + c53 * k3[i] + c54 * k4[i])
		    / h;
	alu.solve(k6, k5);
	for (i = 0; i < n; i++)
	    ytemp[i] += k5[i];
	derivs.derivs(xph, ytemp, dydxnew);
	for (i = 0; i < n; i++)
	    k6[i] = dydxnew[i]
		    + (c61 * k1[i] + c62 * k2[i] + c63 * k3[i] + c64 * k4[i] + c65
			    * k5[i]) / h;
	alu.solve(k6, yerr);
	for (i = 0; i < n; i++)
	    yout[i] = ytemp[i] + yerr[i];
    }

    public void prepare_dense(final double h, final double[] dydxnew) {
	for (int i = 0; i < n; i++) {
	    cont1[i] = y[i];
	    cont2[i] = yout[i];
	    cont3[i] = d21 * k1[i] + d22 * k2[i] + d23 * k3[i] + d24 * k4[i]
		    + d25 * k5[i];
	    cont4[i] = d31 * k1[i] + d32 * k2[i] + d33 * k3[i] + d34 * k4[i]
		    + d35 * k5[i];
	}
    }

    @Override
    public double dense_out(final int i, final double x, final double h) {
	double s = (x - xold) / h;
	double s1 = 1.0 - s;
	return cont1[i] * s1 + s * (cont2[i] + s1 * (cont3[i] + s * cont4[i]));
    }

    public double error() {
	double err = 0.0, sk;
	for (int i = 0; i < n; i++) {
	    sk = atol + rtol * max(abs(y[i]), abs(yout[i]));
	    err += SQR(yerr[i] / sk);
	}
	return sqrt(err / n);
    }
}
