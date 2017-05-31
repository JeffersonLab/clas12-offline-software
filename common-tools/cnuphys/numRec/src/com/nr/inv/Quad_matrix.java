package com.nr.inv;

import static java.lang.Math.*;
import com.nr.UniVarRealMultiValueFun;
import com.nr.la.LUdcmp;

public class Quad_matrix implements UniVarRealMultiValueFun {
    int n;
    double x;

    public Quad_matrix(final double[][] a) {
	n = a.length;
	double[] wt = new double[n];
	double h = PI / (n - 1);
	Wwghts w = new Wwghts(h, n, this);
	for (int j = 0; j < n; j++) {
	    x = j * h;
	    wt = w.weights();
	    double cx = cos(x);
	    for (int k = 0; k < n; k++)
		a[j][k] = wt[k] * cx * cos(k * h);
	    ++a[j][j];
	}
    }

    @Override
    public double[] funk(final double y) {
	double d, df, clog, x2, x3, x4, y2;
	double[] w = new double[4];
	if (y >= x) {
	    d = y - x;
	    df = 2.0 * sqrt(d) * d;
	    w[0] = df / 3.0;
	    w[1] = df * (x / 3.0 + d / 5.0);
	    w[2] = df * ((x / 3.0 + 0.4 * d) * x + d * d / 7.0);
	    w[3] = df
		    * (((x / 3.0 + 0.6 * d) * x + 3.0 * d * d / 7.0) * x + d
			    * d * d / 9.0);
	} else {
	    x3 = (x2 = x * x) * x;
	    x4 = x2 * x2;
	    y2 = y * y;
	    d = x - y;
	    w[0] = d * ((clog = log(d)) - 1.0);
	    w[1] = -0.25 * (3.0 * x + y - 2.0 * clog * (x + y)) * d;
	    w[2] = (-11.0 * x3 + y * (6.0 * x2 + y * (3.0 * x + 2.0 * y)) + 6.0
		    * clog * (x3 - y * y2)) / 18.0;
	    w[3] = (-25.0 * x4 + y
		    * (12.0 * x3 + y * (6.0 * x2 + y * (4.0 * x + 3.0 * y))) + 12.0
		    * clog * (x4 - (y2 * y2))) / 48.0;
	}
	return w;
    }

    public static void main(final String[] args) {
	final int N = 40;

	double[] g = new double[N];
	double[][] a = new double[N][N];
	new Quad_matrix(a);
	LUdcmp alu = new LUdcmp(a);
	for (int j = 0; j < N; j++)
	    g[j] = sin(j * PI / (N - 1));
	alu.solve(g, g);
	for (int j = 0; j < N; j++) {
	    double x = j * PI / (N - 1);
	    System.out.printf("%d  %f  %f\n", (j + 1), x, g[j]);
	}
    }
}
