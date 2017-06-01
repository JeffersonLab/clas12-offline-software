/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * <hwh@gddsn.org.cn> wrote this file. As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return. Huang Wen Hui
 * ----------------------------------------------------------------------------
 *
 */

package com.nr.example;

import static java.lang.Math.sin;
import com.nr.UniVarRealValueFun;
import com.nr.fe.Chebyshev;

public class Chebyshev_Ex implements UniVarRealValueFun {

    public static void main(String[] args) {
	Chebyshev_Ex c_ex = new Chebyshev_Ex();

	// final int NVAL=40;
	// double[] c = new double[NVAL];
	final double PIO2 = 1.570796326794896619;
	int i, mval = 50;
	double a = (-PIO2), b = PIO2;
	double x;

	Chebyshev cv = new Chebyshev(c_ex, a, b, mval);

	for (i = -8; i <= 8; i++) {
	    x = i * PIO2 / 10.0;
	    System.out.printf("%f   %f    %f\n", x, c_ex.funk(x), cv.get(x));
	}
    }

    @Override
    public double funk(double x) {
	return x * x * (x * x - 2.0) * sin(x);
    }
}