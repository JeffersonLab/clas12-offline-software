package com.nr.test;

import com.nr.ran.Ran;
import static java.lang.Math.*;

public class NRTestUtil {
  public static Ran ran = new Ran(10101); // global ranno generator
  
  private NRTestUtil(){}
  
  public static void ranmat(double[][] a) { // fill matrix with ran
    ranmat(a, 0.);
  }
  
  public static void ranmat(double[][] a, double dadd) { // fill matrix with ran
    int m = a.length, n=a[0].length;
    for (int i=0;i<m;i++) for (int j=0;j<n;j++) a[i][j] = 2.*ran.doub() - 1.;
    for (int i=0;i<min(m,n);i++) a[i][i] += dadd;
  }

  public static void ranvec(double[] a) { // fill vector with ran
    int m = a.length;
    for (int i=0;i<m;i++) a[i] = 2.*ran.doub() - 1.;
  }

  
  public static double[][] matmul(final double[][] a, final double[][] b) {
    int i,j,k,m=a.length, n=b[0].length, p=a[0].length;
    if (p != b.length) throw new IllegalArgumentException("impossible matrix multiply");
    double sum;
    double[][] c = new double[m][n];
    for (i=0;i<m;i++) for (j=0;j<n;j++) {
      sum = 0.;
      for (k=0;k<p;k++) sum += a[i][k]*b[k][j];
      c[i][j] = sum;
    }
    return c;
  }

  
  public static double[][] transpose(final double[][] a) {
    int i,j,m=a.length, n=a[0].length;
    double[][] c = new double[n][m];
    for (i=0;i<m;i++) for (j=0;j<n;j++) { c[j][i] = a[i][j]; }
    return c;
  }

  
  public static double[] matmul(final double[][] a, final double[] b) {
    int i,k,m=a.length;
    double sum;
    double[] c = new double[m];
    for (i=0;i<m;i++) {
      sum = 0.;
      for (k=0;k<m;k++) sum += a[i][k]*b[k];
      c[i] = sum;
    }
    return c;
  }

  
  public static double maxel(final double[][] a) {
    int i,j,m=a.length, n=a[0].length;
    double max = 0.;
    for (i=0;i<m;i++) for (j=0;j<n;j++) {
      if (abs(a[i][j]) > max) max = abs(a[i][j]);
    }
    return max;
  }

  
  public static double maxel(final double[] a) {
    int i,m=a.length;
    double max = 0.;
    for (i=0;i<m;i++) {
      if (abs(a[i]) > max) max = abs(a[i]);
    }
    return max;
  }

  
  public static double[][] ident(int n, double dum) {
    double[][] c;
    //c.assign(n,n,(T)0);
    c = new double[n][n];
    for (int i=0;i<n;i++) c[i][i] = 1;
    return c;
  }

  
  public static double[][] matsub(final double[][] a, final double[][] b) {
    int i,j,m=a.length, n=a[0].length;
    if (a.length != b.length || a[0].length != b[0].length) throw new IllegalArgumentException("bad matsub");
    double[][] c = new double[m][n];
    for (i=0;i<m;i++) for (j=0;j<n;j++) {
      c[i][j] = a[i][j]-b[i][j];
    }
    return c;
  }

  
  public static double[] vecsub(final double[] a, final double[] b) {
    int i,m=a.length;
    if (a.length != b.length) throw new IllegalArgumentException("bad vecsub");
    double[] c = new double[m];
    for (i=0;i<m;i++) {
      c[i] = a[i]-b[i];
    }
    return c;
  }

  
  public static double[] vecadd(final double[] a, final double[] b) {
    int i,m=a.length;
    if (a.length != b.length) throw new IllegalArgumentException("bad vecadd");
    double[] c = new double[m];
    for (i=0;i<m;i++) {
      c[i] = a[i]+b[i];
    }
    return c;
  }

  public static int[][] matmul(final int[][] a, final int[][] b) {
    int i,j,k,m=a.length, n=b[0].length, p=a[0].length;
    if (p != b.length) throw new IllegalArgumentException("impossible matrix multiply");
    int sum;
    int[][] c = new int[m][n];
    for (i=0;i<m;i++) for (j=0;j<n;j++) {
      sum = 0;
      for (k=0;k<p;k++) sum += a[i][k]*b[k][j];
      c[i][j] = sum;
    }
    return c;
  }

  
  public static int[][] transpose(final int[][] a) {
    int i,j,m=a.length, n=a[0].length;
    int[][] c = new int[n][m];
    for (i=0;i<m;i++) for (j=0;j<n;j++) { c[j][i] = a[i][j]; }
    return c;
  }

  
  public static int[] matmul(final int[][] a, final int[] b) {
    int i,k,m=a.length;
    int sum;
    int[] c = new int[m];
    for (i=0;i<m;i++) {
      sum = 0;
      for (k=0;k<m;k++) sum += a[i][k]*b[k];
      c[i] = sum;
    }
    return c;
  }

  
  public static int maxel(final int[][] a) {
    int i,j,m=a.length, n=a[0].length;
    int max = 0;
    for (i=0;i<m;i++) for (j=0;j<n;j++) {
      if (abs(a[i][j]) > max) max = abs(a[i][j]);
    }
    return max;
  }

  
  public static int maxel(final int[] a) {
    int i,m=a.length;
    int max = 0;
    for (i=0;i<m;i++) {
      if (abs(a[i]) > max) max = abs(a[i]);
    }
    return max;
  }

  
  public static int[][] ident(int n, int dum) {
    int[][] c;
    //c.assign(n,n,(T)0);
    c = new int[n][n];
    for (int i=0;i<n;i++) c[i][i] = 1;
    return c;
  }

  
  public static int[][] matsub(final int[][] a, final int[][] b) {
    int i,j,m=a.length, n=a[0].length;
    if (a.length != b.length || a[0].length != b[0].length) throw new IllegalArgumentException("bad matsub");
    int[][] c = new int[m][n];
    for (i=0;i<m;i++) for (j=0;j<n;j++) {
      c[i][j] = a[i][j]-b[i][j];
    }
    return c;
  }

  
  public static int[] vecsub(final int[] a, final int[] b) {
    int i,m=a.length;
    if (a.length != b.length) throw new IllegalArgumentException("bad vecsub");
    int[] c = new int[m];
    for (i=0;i<m;i++) {
      c[i] = a[i]-b[i];
    }
    return c;
  }

  
  public static int[] vecadd(final int[] a, final int[] b) {
    int i,m=a.length;
    if (a.length != b.length) throw new IllegalArgumentException("bad vecadd");
    int[] c = new int[m];
    for (i=0;i<m;i++) {
      c[i] = a[i]+b[i];
    }
    return c;
  }

}
