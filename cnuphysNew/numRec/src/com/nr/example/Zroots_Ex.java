package com.nr.example;

import com.nr.Complex;
import static com.nr.root.Roots.*;

public class Zroots_Ex {

    public static void main(final String[] args) {

	Complex c0 = new Complex(0.000193, 0);
	Complex c1 = new Complex(0.098243, 0);
	Complex c2 = new Complex(25.024523, 0);
	Complex c3 = new Complex(6.238928, 0);
	Complex c4 = new Complex(1, 0);
	Complex[] c = new Complex[] { c0, c1, c2, c3, c4 };
	Complex[] roots = new Complex[4];
	zroots(c, roots, true);
	for (int i = 0; i < roots.length; i++)
	    System.out.println(roots[i]);
    }

}
