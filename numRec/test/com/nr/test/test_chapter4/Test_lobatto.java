package com.nr.test.test_chapter4;

import static com.nr.fi.GaussianWeights.lobatto;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Test_lobatto {

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
    
    

    // Test lobatto
    System.out.println("Testing lobatto");
    // Test with Gauss-Chebyshev
    for (i=0;i<N-1;i++) {
      a[i]=0.0;
      b[i+1]=0.25;
    }
    a[N-1]=0.0;
    b[0]=0.25;
    amu0=PI;

    lobatto(a,b,amu0,-1.0,1.0,x,w);

//    System.out.printf(fixed << setprecision(6);
//    for (i=0;i<N;i++) 
//      System.out.printf(setw(3) << i << setw(15) << x[i] << setw(15) << w[i] << endl;

    for (check=0.0,i=0;i<N;i++) check += w[i];

    sbeps=6.e-15;
//    System.out.printf(setprecision(20) << fabs(check-PI) << endl;
    localflag = abs(check-PI) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** lobatto: Gaussian quadrature weights to not add to the correct value");
      
    }

    sbeps=1.e-15;
//    System.out.printf(abs(x[0]-1.0) << endl;
    localflag = abs(x[0]-1.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** lobatto: First element of x[] is not the requested value of 1.0");
      
    }

    sbeps=1.e-15;
//    System.out.printf(abs(x[N-1]+1.0) << endl;
    localflag = abs(x[N-1]+1.0) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** lobatto: Last element of x[] is not the requested value of -1.0");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
