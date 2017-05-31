package com.nr.la;

public class Vander {

    /**
     * Solves the Vandermonde linear system. - PTC Input consists of the vectors
     * x[0..n-1] and q[0..n-1]; the vector w[0..n-1] is output.
     * 
     * @param x
     * @param w
     * @param q
     */
    public static void vander(final double[] x, final double[] w,
	    final double[] q) {
	int i, j, k, n = q.length;
	double b, s, t, xx;
	double[] c = new double[n];
	if (n == 1)
	    w[0] = q[0];
	else {
	    for (i = 0; i < n; i++)
		c[i] = 0.0;
	    c[n - 1] = -x[0];
	    for (i = 1; i < n; i++) {
		xx = -x[i];
		for (j = (n - 1 - i); j < (n - 1); j++)
		    c[j] += xx * c[j + 1];
		c[n - 1] += xx;
	    }
	    for (i = 0; i < n; i++) {
		xx = x[i];
		t = b = 1.0;
		s = q[n - 1];
		for (k = n - 1; k > 0; k--) {
		    b = c[k] + xx * b;
		    s += q[k - 1] * b;
		    t = xx * t + b;
		}
		w[i] = s / t;
	    }
	}
    }

}
