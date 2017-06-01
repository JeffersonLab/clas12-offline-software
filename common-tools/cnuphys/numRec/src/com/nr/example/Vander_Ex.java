package com.nr.example;

import com.nr.NRUtil;
import com.nr.la.Vander;

public class Vander_Ex {

    public static void main(String[] args) {
	double[] x = new double[] { 1.0, 3.0, 1.0, 4.0, 2.2, -1.0 };
	double[] q = new double[] { 2.0, 4.0, 1.0, 2.0, 4.0, 2.0 };
	double[] w = new double[6];
	Vander.vander(x, w, q);
	System.out.println(NRUtil.toString(w));
    }
}
