package com.nr.test.test_chapter4;

import static com.nr.fi.GaussianWeights.gaucof;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.sqrt;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Test_gaucof {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=10;
    double amu0,check,PI=acos(-1.0),sbeps;
    double[] a= new double[N],b= new double[N],x= new double[N],w= new double[N];
    boolean localflag, globalflag=false;
    
    // Test gaucof
    System.out.println("Testing gaucof");

    // Test with Gauss-Hermite
    for (i=0;i<N-1;i++) {
      a[i]=0.0;
      b[i+1]=(i+1)*0.5;
    }
    a[N-1]=0.0;
    // b[0] is arbitrary for call to tqli
    amu0=sqrt(PI);

    gaucof(a,b,amu0,x,w);
//    System.out.printf(fixed << setprecision(6);
//    for (i=0;i<N;i++) 
//      System.out.printf(setw(3) << i << setw(15) << x[i] << setw(15) << w[i] << endl;
    for (check=0.0,i=0;i<N;i++) check += w[i];
//    System.out.printf(endl << "Check value: %f\n", setw(12) << check;
//    System.out.println("  should be: %f\n", setw(12) << sqrt(PI) << endl << endl;

    sbeps=1.5e-15;
//    System.out.printf(fabs(check-sqrt(PI)) << endl;
    localflag = abs(check-sqrt(PI)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** gaucof: Gaussian weights do not sum to correct value");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
