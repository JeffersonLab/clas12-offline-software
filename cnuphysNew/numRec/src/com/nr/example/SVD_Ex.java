package com.nr.example;

import com.nr.NRUtil;
import com.nr.la.SVD;

public class SVD_Ex {

    public static void main(String[] args) {
	double[] a = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	double[][] m = NRUtil.buildMatrix(3, 3, a);
	SVD svd = new SVD(m);
	double b[] = { 3, 1, 2 };
	double[] x = new double[3];
	System.out.println(svd.inv_condition());
	System.out.println(svd.rank());
	System.out.println(svd.nullity());

	svd.solve(b, x);
	System.out.println(NRUtil.toString(x));
    }
}
