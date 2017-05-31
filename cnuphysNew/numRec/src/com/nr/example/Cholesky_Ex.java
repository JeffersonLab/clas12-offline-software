package com.nr.example;

import com.nr.NRUtil;
import com.nr.la.Cholesky;

public class Cholesky_Ex {

    public static void main(String[] args) {
	double[] a = new double[] { 1.0, 0.5, 1.0, 0.5, 2.2, -1.0, 1.0, -1.0,
		5.0 };
	double[][] aa = NRUtil.buildMatrix(3, 3, a);
	double[][] inv = new double[3][3];
	double[] b = new double[] { 2.0, 4.0, 1.0 };
	double[] x = new double[3];

	Cholesky ch = new Cholesky(aa);
	ch.solve(b, x);
	System.out.println(NRUtil.toString(x));
	ch.elsolve(b, x);
	System.out.println(NRUtil.toString(x));
	System.out.println(ch.logdet());

	ch.inverse(inv);

	System.out.println(NRUtil.toString(inv));
    }
}
