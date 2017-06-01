package com.nr.example;

import com.nr.Complex;
import static com.nr.root.Roots.*;

public class Zrhqr_Ex {
    public static void main(final String[] args) {
	double[] cc = new double[] { 0.000193, 0.098243, 25.024523, 6.238928, 1 };
	Complex[] roots = new Complex[4];
	zrhqr(cc, roots);
	for (int i = 0; i < roots.length; i++)
	    System.out.println(roots[i]);
    }
}
