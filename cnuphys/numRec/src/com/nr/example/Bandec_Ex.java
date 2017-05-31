package com.nr.example;

import com.nr.NRUtil;
import com.nr.la.Bandec;

public class Bandec_Ex {

    public static void main(String[] args) {
	double[] a = new double[] { 1.0, 2.0, 2.0, 4.0, -4.0, -3.0, 2.0, 4.0,
		5.0, 1, 2, 3, 4, 3, 2, 1 };
	double[][] aa = NRUtil.buildMatrix(4, 4, a);
	int mm1 = 1;
	int mm2 = 2;
	Bandec bdc = new Bandec(aa, mm1, mm2);

	double[] b = new double[] { 4.0, -4.0, -3.0, 1.0 };
	double[] x = new double[4];
	bdc.solve(b, x);
	System.out.println(NRUtil.toString(x));
	System.out.println(bdc.det());

    }
}
