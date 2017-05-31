package com.nr.test.test_chapter4;

import static com.nr.fi.GaussianWeights.radau;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Test_radau {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=10;
    double amu0,check,sbeps,PI=acos(-1.0);
    double[] a= new double[N],b= new double[N],x= new double[N],w= new double[N];
    boolean localflag, globalflag=false;
    
    

    // Test radau
    System.out.println("Testing radau");

    // Test with Gauss-Chebyshev
    for (i=0;i<N-1;i++) {
      a[i]=0.0;
      b[i+1]=0.25;
    }
    a[N-1]=0.0;
    b[0]=-0.25;
    amu0=PI;
    radau(a,b,amu0,1.0,x,w);

//    System.out.printf(fixed << setprecision(6);
//    for (i=0;i<N;i++) 
//      System.out.printf(setw(3) << i << setw(15) << x[i] << setw(15) << w[i] << endl;

    for (check=0.0,i=0;i<N;i++) check += w[i];
//    System.out.printf(endl << "Check value: %f\n", setw(12) << check;
//    System.out.println("  should be: %f\n", setw(12) << PI << endl << endl;

    sbeps=5.e-15;
//    System.out.printf(setprecision(20) << fabs(check-PI) << endl;
    localflag = abs(check-PI) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** radau: Gaussian weights do not sum to correct value");
      
    }

    sbeps=1.e-15;
    localflag = abs(x[0]-1.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** radau: First element of x[] is not the requested value of 1.0");
      
    }

    // Since arrays a[],b[] are modified, we recompute them
    for (i=0;i<N-1;i++) {
      a[i]=0.0;
      b[i+1]=0.25;
    }
    a[N-1]=0.0;
    b[0]=-0.25;
    amu0=PI;
    radau(a,b,amu0,-1.0,x,w);
//    System.out.printf(fixed << setprecision(6);
//    for (i=0;i<N;i++) 
//      System.out.printf(setw(3) << i << setw(15) << x[i] << setw(15) << w[i] << endl;
    for (check=0.0,i=0;i<N;i++) check += w[i];
//    System.out.printf(endl << "Check value: %f\n", setw(12) << check;
//    System.out.println("  should be: %f\n", setw(12) << sqrt(PI) << endl << endl;

    sbeps=5.e-15;
//    System.out.printf(abs(check-PI) << endl;
    localflag = abs(check-PI) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** radau: Gaussian weights do not sum to correct value");
      
    }

    sbeps=1.e-15;
    localflag = abs(x[N-1]+1.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** radau: Last element of x[] is not the requested value of -1.0");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
