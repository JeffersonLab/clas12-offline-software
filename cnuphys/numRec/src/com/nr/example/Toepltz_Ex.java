package com.nr.example;

import com.nr.NRUtil;
import com.nr.la.Toepltz;

public class Toepltz_Ex {

    public static void main(String[] args) {
	double[] r = new double[] { 1.0, 3.0, 1.0, 4.0, 2.2, -1.0, 2 };
	double[] y = new double[] { 2.0, 4.0, 1.0, -1 };
	double[] x = new double[4];
	Toepltz.toeplz(r, x, y);
	System.out.println(NRUtil.toString(x));
    }
}
