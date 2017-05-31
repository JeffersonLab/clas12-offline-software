package com.nr.test.test_chapter6;

import static com.nr.test.NRTestUtil.*;
import static com.nr.NRUtil.*;
import static com.nr.sf.Beta.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Test_beta {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void test() {
    int i,N=14;
    double sbeps=5.e-14;
    double z[]={0.5,1.0,1.0,2.0,2.0,1.0,3.0,3.0,2.0,3.0,10.0,20.0,10.0,20.0};
    double w[]={0.5,1.0,2.0,1.0,2.0,3.0,1.0,2.0,3.0,3.0,10.0,20.0,20.0,10.0};
    double y[]={3.141592653589793,1.0,0.5,0.5,1.666666666666666e-1,
      3.333333333333333e-1,3.333333333333333e-1,8.333333333333337e-2,
      8.333333333333337e-2,3.333333333333338e-2,1.082508822446901e-6,
      7.254444551924843e-13,4.992508740634730e-9,4.992508740634730e-9};
    double[] yy=new double[N],c =buildVector(N,1.0);
    boolean localflag, globalflag=false;

    

    // Test beta
    System.out.println("Testing beta");

    for (i=0;i<N;i++) yy[i]=beta(z[i],w[i])/y[i];
    System.out.printf("beta: Maximum fractional discrepancy = %f\n", maxel(vecsub(yy,c)));
    localflag = maxel(vecsub(yy,c)) > sbeps;
    globalflag = globalflag || localflag;
    if (localflag) {
      fail("*** beta: Incorrect function values");
      
    }

    if (globalflag) System.out.println("Failed\n");
    else System.out.println("Passed\n");
  }

}
