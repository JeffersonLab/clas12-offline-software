package com.nr.example;

import com.nr.NRUtil;
import com.nr.la.LUdcmp;

public class LUdcmp_Ex {

    public static void main(String[] args) {
	// double[] a = new double[]{1.0, 2.0,2.0,4.0, -4.0, -3.0, 2.0,
	// 4.0,5.0};
	double[] a = new double[] { 0.666667, 0.166667, -0.166667, 2.16667,
		-0.0833333, -0.916667, -2.00000, 0.00000, 1.00000 };
	double[][] aa = NRUtil.buildMatrix(3, 3, a);
	LUdcmp lud = new LUdcmp(aa);
	double[][] inv = lud.inverse();
	System.out.println(NRUtil.toString(inv));
    }
}
