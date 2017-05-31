package com.nr.example;

import com.nr.UniVarRealValueFun;
import com.nr.fe.Levin;
import com.nr.fi.Trapzd;
import com.nr.sf.Bessel;

public class Levin_Ex implements UniVarRealValueFun {

    @Override
    public double funk(double x) {
	if (x == 0.0)
	    return 0.0;
	else {
	    Bessel bess = new Bessel();
	    return x * bess.jnu(0.0, x) / (1.0 + x * x);
	}
    }

    public static void main(String[] args) {
	Levin_Ex le = new Levin_Ex();
	int nterm = 12;
	double beta = 1.0, a = 0.0, b = 0.0, sum = 0.0;
	Levin series = new Levin(100, 0.0);

	for (int n = 0; n <= nterm; n++) {
	    b += Math.PI;
	    double s = Trapzd.qromb(le, a, b, 1.e-8);
	    a = b;
	    sum += s;
	    double omega = (beta + n) * s;
	    double ans = series.next(sum, omega, beta);
	    System.out.printf("%f %f\n", sum, ans);
	}

    }
}
