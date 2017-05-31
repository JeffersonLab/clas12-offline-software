package com.nr.example;

import com.nr.NRUtil;
import com.nr.la.Tridag;

public class Cyclic_Ex {

    public static void main(String[] args) {
	double[] a = new double[] { 1.0, 2.0, 2.0 };
	double[] b = new double[] { 4.0, -4.0, -3.0 };
	double[] c = new double[] { 2.0, 4.0, 5.0 };
	double[] r = new double[] { 2.0, 4.0, 5.0 };
	double[] u = new double[3];
	double alpha = 5.0;
	double beta = -5.1;
	Tridag.cyclic(a, b, c, alpha, beta, r, u);
	System.out.println(NRUtil.toString(u));
    }
}
