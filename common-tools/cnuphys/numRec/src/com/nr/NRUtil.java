package com.nr;

import org.netlib.util.intW;

public class NRUtil {
    private NRUtil() {
    }

    public final static float FLT_EPSILON = 1.19209290E-07F;

    public final static double DBL_EPSILON = 2.2204460492503131E-16;

    public final static int FLT_RADIX = 2;

    public final static int DBL_MANT_DIG = 53;

    public final static int INT_DIGITS = 32;

    public static double SQR(final double a) {
	return a * a;
    }

    public static float SQR(final float a) {
	return a * a;
    }

    public static int SQR(final int a) {
	return a * a;
    }

    public static double SIGN(final double a, final double b) {
	return b >= 0 ? (a >= 0 ? a : -a) : (a >= 0 ? -a : a);
    }

    public static int SIGN(final int a, final int b) {
	return b >= 0 ? (a >= 0 ? a : -a) : (a >= 0 ? -a : a);
    }

    public static float SIGN(final float a, final double b) {
	return b >= 0 ? (a >= 0 ? a : -a) : (a >= 0 ? -a : a);
    }

    public static float SIGN(final double a, final float b) {
	return (float) (b >= 0 ? (a >= 0 ? a : -a) : (a >= 0 ? -a : a));
    }

    /**
     * Swaps x[a] with x[b].
     */
    public static void swap(double x[], int a, int b) {
	double t = x[a];
	x[a] = x[b];
	x[b] = t;
    }

    public static void swap(int x[], int a, int b) {
	int t = x[a];
	x[a] = x[b];
	x[b] = t;
    }

    public static void swap(char x[], int a, int b) {
	char t = x[a];
	x[a] = x[b];
	x[b] = t;
    }

    public static void swap(Object x[], int a, int b) {
	Object t = x[a];
	x[a] = x[b];
	x[b] = t;
    }

    public static double[] buildVector(final int n, final double a) {
	double[] v = new double[n];
	for (int i = 0; i < v.length; i++)
	    v[i] = a;
	return v;
    }

    public static int[] buildVector(final int n, final int a) {
	int[] v = new int[n];
	for (int i = 0; i < v.length; i++)
	    v[i] = a;
	return v;
    }

    public static double[] buildVector(final double[] b) {
	double[] v = new double[b.length];
	for (int i = 0; i < v.length; i++)
	    v[i] = b[i];
	return v;
    }

    public static int[] buildVector(final int[] b) {
	int[] v = new int[b.length];
	for (int i = 0; i < v.length; i++)
	    v[i] = b[i];
	return v;
    }

    /**
     * XXX should be use in the members of the class only.
     * 
     * @param v
     * @param newn
     * @return
     */
    public static double[] resize(double[] v, final int newn) {
	int nn = v.length;
	if (newn != nn) {
	    nn = newn;
	    v = nn > 0 ? new double[nn] : null;
	}
	return v;
    }

    public static void copyAssign(final double[] v, final double[] rhs) {
	// postcondition: normal assignment via copying has been performed;
	// if vector and rhs were different sizes, vector
	// has been resized to match the size of rhs
	int nn = v.length;
	int rnn = rhs.length;

	if (v != rhs) {
	    if (nn != rnn) {
		// nn=rnn;
		// v = new double[nn];
		throw new IllegalArgumentException("Must be same size.");
	    }
	    System.arraycopy(rhs, 0, v, 0, nn);
	}
	return;
    }

    public static double[][] buildMatrix(final int nn, final int mm,
	    final double a) {
	double[][] v = new double[nn][mm];
	for (int i = 0; i < nn; i++)
	    for (int j = 0; j < mm; j++)
		v[i][j] = a;
	return v;
    }

    public static double[][] buildMatrix(final int nn, final int mm,
	    final double[] b) {
	double[][] v = new double[nn][mm];
	for (int i = 0; i < nn; i++)
	    for (int j = 0; j < mm; j++)
		v[i][j] = b[i * mm + j];
	return v;
    }

    public static int[][] buildMatrix(final int nn, final int mm, final int[] b) {
	int[][] v = new int[nn][mm];
	for (int i = 0; i < nn; i++)
	    for (int j = 0; j < mm; j++)
		v[i][j] = b[i * mm + j];
	return v;
    }

    public static double[][] buildMatrix(final double[][] b) {
	int nn = b.length;
	int mm = b[0].length;
	double[][] v = new double[nn][mm];
	for (int i = 0; i < nn; i++)
	    for (int j = 0; j < mm; j++)
		v[i][j] = b[i][j];
	return v;
    }

    public static void copyAssign(final double[][] v, final double[][] rhs) {
	// postcondition: normal assignment via copying has been performed;
	// if matrix and rhs were different sizes, matrix
	// has been resized to match the size of rhs
	int nn = v.length;
	int mm = v[0].length;
	int rnn = rhs.length;
	int rmm = rhs[0].length;
	if (v != rhs) {
	    if (nn != rnn || mm != rmm) {
		throw new IllegalArgumentException("Must be same size.");
		// nn=rnn;
		// mm=rmm;
		// v = new double[nn][mm];
	    }
	    for (int i = 0; i < nn; i++)
		for (int j = 0; j < mm; j++)
		    v[i][j] = rhs[i][j];
	}
	return;
    }

    public static double[] getRow(final double[][] v, final int nn) {
	return v[nn];
    }

    public static double ldexp(final double v, final int w) {
	return v * Math.pow(2.0, w);
    }

    public static double frexp(final double v, final intW exp) {
	if (v == 0) {
	    exp.val = 0;
	    return 0;
	}
	long bits = Double.doubleToLongBits(v);
	double d = Double
		.longBitsToDouble((0x800fffffffffffffL & bits) | 0x3fe0000000000000L);
	exp.val = (int) ((0x7ff0000000000000L & bits) >> 52) - 1022;
	return d;
    }

    public static String toString(double[] v) {
	StringBuilder sb = new StringBuilder(256);
	int nn = v.length;
	int nb = 10;
	for (int i = 0; i < nn; i++) {
	    sb.append(String.format("%g", v[i]));
	    if (i != 0 && (i % nb) == 0)
		sb.append("\n");
	    else
		sb.append(" ");
	}

	return sb.substring(0);
    }

    public static String toString(double[][] v) {
	StringBuilder sb = new StringBuilder(256);
	int nn = v.length;
	for (int i = 0; i < nn; i++) {
	    for (int j = 0; j < v[i].length; j++)
		sb.append(String.format(" %g", v[i][j]));
	    sb.append("\n");
	}
	return sb.substring(0);
    }

}
